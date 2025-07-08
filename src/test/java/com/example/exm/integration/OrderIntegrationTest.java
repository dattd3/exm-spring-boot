package com.example.exm.integration;

import com.example.exm.config.JpaAuditingConfig;
import com.example.exm.dto.request.CreateOrderRequest;
import com.example.exm.dto.request.CreateProductRequest;
import com.example.exm.dto.request.CreateUserRequest;
import com.example.exm.dto.response.OrderResponse;
import com.example.exm.dto.response.ProductResponse;
import com.example.exm.dto.response.UserResponse;
import com.example.exm.entity.OrderStatus;
import com.example.exm.entity.ProductStatus;
import com.example.exm.entity.UserStatus;
import com.example.exm.repository.OrderRepository;
import com.example.exm.repository.ProductRepository;
import com.example.exm.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
//@Import(JpaAuditingConfig.class)
class OrderIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createOrder_Success() throws Exception {
        // Create user
        Long userId = createTestUser();

        // Create product
        Long productId = createTestProduct();

        // Create order
        CreateOrderRequest.OrderItemRequest orderItemRequest = new CreateOrderRequest.OrderItemRequest();
        orderItemRequest.setProductId(productId);
        orderItemRequest.setQuantity(2);

        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddress("123 Test St");
        orderRequest.setNotes("Test order");
        orderRequest.setOrderItems(Arrays.asList(orderItemRequest));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andExpect(jsonPath("$.data.totalAmount", is(199.98)));
    }

    @Test
    void createOrder_InsufficientStock_ReturnsBadRequest() throws Exception {
        // Create user
        Long userId = createTestUser();

        // Create product with limited stock
        Long productId = createTestProductWithStock(5);

        // Try to order more than available
        CreateOrderRequest.OrderItemRequest orderItemRequest = new CreateOrderRequest.OrderItemRequest();
        orderItemRequest.setProductId(productId);
        orderItemRequest.setQuantity(10);

        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddress("123 Test St");
        orderRequest.setOrderItems(Arrays.asList(orderItemRequest));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void updateOrderStatus_Success() throws Exception {
        // Create and get order
        Long orderId = createTestOrder();

        mockMvc.perform(put("/api/orders/{id}/status", orderId)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("CONFIRMED")));
    }

    @Test
    void getOrderById_Success() throws Exception {
        // Create order
        Long orderId = createTestOrder();

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(orderId.intValue())));
    }

    @Test
    void cancelOrder_Success() throws Exception {
        // Create order
        Long orderId = createTestOrder();

        mockMvc.perform(put("/api/orders/{id}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Verify order is cancelled
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("CANCELLED")));
    }

    private Long createTestUser() throws Exception {
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setFirstName("Test");
        userRequest.setLastName("User");
        userRequest.setEmail("test@example.com");
        userRequest.setStatus(UserStatus.ACTIVE);

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponse userResponse = objectMapper.readTree(response).get("data").traverse(objectMapper).readValueAs(UserResponse.class);
        return userResponse.getId();
    }

    private Long createTestProduct() throws Exception {
        return createTestProductWithStock(50);
    }

    private Long createTestProductWithStock(Integer stock) throws Exception {
        CreateProductRequest productRequest = new CreateProductRequest();
        productRequest.setName("Test Product");
        productRequest.setDescription("Test Description");
        productRequest.setPrice(new BigDecimal("99.99"));
        productRequest.setStockQuantity(stock);
        productRequest.setCategory("Electronics");
        productRequest.setBrand("TestBrand");
        productRequest.setStatus(ProductStatus.ACTIVE);

        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ProductResponse productResponse = objectMapper.readTree(response).get("data").traverse(objectMapper).readValueAs(ProductResponse.class);
        return productResponse.getId();
    }

    private Long createTestOrder() throws Exception {
        Long userId = createTestUser();
        Long productId = createTestProduct();

        CreateOrderRequest.OrderItemRequest orderItemRequest = new CreateOrderRequest.OrderItemRequest();
        orderItemRequest.setProductId(productId);
        orderItemRequest.setQuantity(2);

        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddress("123 Test St");
        orderRequest.setOrderItems(Arrays.asList(orderItemRequest));

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        OrderResponse orderResponse = objectMapper.readTree(response).get("data").traverse(objectMapper).readValueAs(OrderResponse.class);
        return orderResponse.getId();
    }
}