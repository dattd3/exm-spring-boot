package com.example.exm.controller;

import com.example.exm.controller.rest.OrderRestController;
import com.example.exm.dto.request.CreateOrderRequest;
import com.example.exm.dto.response.ApiResponse;
import com.example.exm.dto.response.OrderResponse;
import com.example.exm.dto.response.UserResponse;
import com.example.exm.entity.OrderStatus;
import com.example.exm.exception.BusinessException;
import com.example.exm.exception.ResourceNotFoundException;
import com.example.exm.service.OrderService;
import com.example.exm.service.UserService;
import com.example.exm.util.TestDataBuilder;
import com.example.exm.util.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({OrderRestController.class, OrderController.class})
@DisplayName("Order Controller Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserService userService;

    private OrderResponse orderResponse;
    private CreateOrderRequest createOrderRequest;
    private UserResponse userResponse;
    private List<OrderResponse> orderList;
    private Page<OrderResponse> orderPage;

    @BeforeEach
    void setUp() {
        orderResponse = TestDataBuilder.OrderResponseBuilder.anOrderResponse()
                .withId(1L)
                .withOrderNumber("ORD-20240101001")
                .withTotalAmount(new BigDecimal("299.99"))
                .withStatus(OrderStatus.PENDING)
                .build();

        createOrderRequest = TestDataBuilder.CreateOrderRequestBuilder.aCreateOrderRequest()
                .withUserId(1L)
                .withShippingAddress("123 Test Street, Test City")
                .withOrderItem(1L, 2)
                .withOrderItem(2L, 1)
                .build();

        userResponse = TestDataBuilder.UserResponseBuilder.aUserResponse()
                .withId(1L)
                .withFirstName("John")
                .withLastName("Doe")
                .withEmail("john.doe@example.com")
                .build();

        orderList = Arrays.asList(
                orderResponse,
                TestDataBuilder.OrderResponseBuilder.anOrderResponse()
                        .withId(2L)
                        .withOrderNumber("ORD-20240101002")
                        .withStatus(OrderStatus.CONFIRMED)
                        .build()
        );

        orderPage = TestUtils.createPage(orderList, PageRequest.of(0, 20), 2);
    }

    @Nested
    @DisplayName("REST API Endpoints")
    class RestApiTests {

        @Test
        @DisplayName("POST /api/orders - Should create order successfully")
        void createOrder_Success() throws Exception {
            // Given
            when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(orderResponse);

            // When & Then
            MvcResult result = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestUtils.asJsonString(createOrderRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Order created successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.orderNumber").value("ORD-20240101001"))
                    .andExpect(jsonPath("$.data.totalAmount").value(299.99))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andReturn();

            // Verify response structure
            ApiResponse<OrderResponse> response = TestUtils.fromMvcResult(result, ApiResponse.class);
            assertTrue(response.isSuccess());
            assertEquals("Order created successfully", response.getMessage());

            verify(orderService).createOrder(any(CreateOrderRequest.class));
        }

        @Test
        @DisplayName("POST /api/orders - Should return bad request for invalid data")
        void createOrder_InvalidData_ReturnsBadRequest() throws Exception {
            // Given
            CreateOrderRequest invalidRequest = new CreateOrderRequest();
            invalidRequest.setUserId(null); // Invalid - null user ID

            // When & Then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestUtils.asJsonString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
        }

        @Test
        @DisplayName("POST /api/orders - Should handle business exception")
        void createOrder_BusinessException_ReturnsConflict() throws Exception {
            // Given
            when(orderService.createOrder(any(CreateOrderRequest.class)))
                    .thenThrow(new BusinessException("Insufficient stock"));

            // When & Then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestUtils.asJsonString(createOrderRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Insufficient stock"));

            verify(orderService).createOrder(any(CreateOrderRequest.class));
        }

        @Test
        @DisplayName("GET /api/orders/{id} - Should return order by ID")
        void getOrderById_Success() throws Exception {
            // Given
            when(orderService.getOrderById(1L)).thenReturn(orderResponse);

            // When & Then
            mockMvc.perform(get("/api/orders/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.orderNumber").value("ORD-20240101001"))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));

            verify(orderService).getOrderById(1L);
        }

        @Test
        @DisplayName("GET /api/orders/{id} - Should return not found for non-existent order")
        void getOrderById_NotFound() throws Exception {
            // Given
            when(orderService.getOrderById(999L))
                    .thenThrow(new ResourceNotFoundException("Order not found with id: 999"));

            // When & Then
            mockMvc.perform(get("/api/orders/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Order not found with id: 999"));

            verify(orderService).getOrderById(999L);
        }

        @Test
        @DisplayName("GET /api/orders/order-number/{orderNumber} - Should return order by order number")
        void getOrderByOrderNumber_Success() throws Exception {
            // Given
            when(orderService.getOrderByOrderNumber("ORD-20240101001")).thenReturn(orderResponse);

            // When & Then
            mockMvc.perform(get("/api/orders/order-number/ORD-20240101001"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderNumber").value("ORD-20240101001"));

            verify(orderService).getOrderByOrderNumber("ORD-20240101001");
        }

        @Test
        @DisplayName("GET /api/orders - Should return paginated orders")
        void getAllOrders_Success() throws Exception {
            // Given
            when(orderService.getAllOrders(any(Pageable.class))).thenReturn(orderPage);

            // When & Then
            mockMvc.perform(get("/api/orders")
                            .param("page", "0")
                            .param("size", "20")
                            .param("sortBy", "orderDate")
                            .param("sortDirection", "desc"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(2)))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").value(true));

            verify(orderService).getAllOrders(any(Pageable.class));
        }

        @Test
        @DisplayName("GET /api/orders/user/{userId} - Should return user's orders")
        void getOrdersByUserId_Success() throws Exception {
            // Given
            when(orderService.getOrdersByUserId(eq(1L), any(Pageable.class))).thenReturn(orderPage);

            // When & Then
            mockMvc.perform(get("/api/orders/user/1")
                            .param("page", "0")
                            .param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(2)));

            verify(orderService).getOrdersByUserId(eq(1L), any(Pageable.class));
        }

        @Test
        @DisplayName("GET /api/orders/status/{status} - Should return orders by status")
        void getOrdersByStatus_Success() throws Exception {
            // Given
            when(orderService.getOrdersByStatus(eq(OrderStatus.PENDING), any(Pageable.class)))
                    .thenReturn(orderPage);

            // When & Then
            mockMvc.perform(get("/api/orders/status/PENDING")
                            .param("page", "0")
                            .param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(2)));

            verify(orderService).getOrdersByStatus(eq(OrderStatus.PENDING), any(Pageable.class));
        }

        @Test
        @DisplayName("GET /api/orders/date-range - Should return orders in date range")
        void getOrdersByDateRange_Success() throws Exception {
            // Given
            LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
            when(orderService.getOrdersByDateRange(startDate, endDate)).thenReturn(orderList);

            // When & Then
            mockMvc.perform(get("/api/orders/date-range")
                            .param("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .param("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(2)));

            verify(orderService).getOrdersByDateRange(startDate, endDate);
        }

        @Test
        @DisplayName("GET /api/orders/revenue - Should return total revenue")
        void getTotalRevenue_Success() throws Exception {
            // Given
            LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
            BigDecimal revenue = new BigDecimal("5000.00");
            when(orderService.getTotalRevenue(startDate, endDate)).thenReturn(revenue);

            // When & Then
            mockMvc.perform(get("/api/orders/revenue")
                            .param("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .param("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Total revenue calculated successfully"))
                    .andExpect(jsonPath("$.data").value(5000.00));

            verify(orderService).getTotalRevenue(startDate, endDate);
        }

        @Test
        @DisplayName("GET /api/orders/multiple-items - Should return orders with multiple items")
        void getOrdersWithMultipleItems_Success() throws Exception {
            // Given
            when(orderService.getOrdersWithMultipleItems(2)).thenReturn(orderList);

            // When & Then
            mockMvc.perform(get("/api/orders/multiple-items")
                            .param("minItems", "2"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(2)));

            verify(orderService).getOrdersWithMultipleItems(2);
        }

        @Test
        @DisplayName("PUT /api/orders/{id}/status - Should update order status")
        void updateOrderStatus_Success() throws Exception {
            // Given
            OrderResponse updatedOrder = TestDataBuilder.OrderResponseBuilder.anOrderResponse()
                    .withId(1L)
                    .withStatus(OrderStatus.CONFIRMED)
                    .build();
            when(orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED)).thenReturn(updatedOrder);

            // When & Then
            mockMvc.perform(put("/api/orders/1/status")
                            .param("status", "CONFIRMED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Order status updated successfully"))
                    .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

            verify(orderService).updateOrderStatus(1L, OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("PUT /api/orders/{id}/cancel - Should cancel order")
        void cancelOrder_Success() throws Exception {
            // Given
            doNothing().when(orderService).cancelOrder(1L);

            // When & Then
            mockMvc.perform(put("/api/orders/1/cancel"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Order cancelled successfully"));

            verify(orderService).cancelOrder(1L);
        }

        @Test
        @DisplayName("PUT /api/orders/{id}/cancel - Should handle business exception")
        void cancelOrder_BusinessException() throws Exception {
            // Given
            doThrow(new BusinessException("Cannot cancel delivered order"))
                    .when(orderService).cancelOrder(1L);

            // When & Then
            mockMvc.perform(put("/api/orders/1/cancel"))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Cannot cancel delivered order"));

            verify(orderService).cancelOrder(1L);
        }
    }

    @Nested
    @DisplayName("MVC Endpoints")
    class MvcTests {

        @Test
        @DisplayName("GET /orders - Should display orders list page")
        void listOrders_Success() throws Exception {
            // Given
            when(orderService.getAllOrders(any(Pageable.class))).thenReturn(orderPage);

            // When & Then
            mockMvc.perform(get("/orders"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders/list"))
                    .andExpect(model().attributeExists("orders"))
                    .andExpect(model().attribute("orders", orderPage))
                    .andExpect(model().attribute("currentPage", 0))
                    .andExpect(model().attribute("totalPages", 1))
                    .andExpect(model().attribute("sortBy", "orderDate"))
                    .andExpect(model().attribute("sortDirection", "desc"))
                    .andExpect(model().attributeExists("orderStatuses"));

            verify(orderService).getAllOrders(any(Pageable.class));
        }

        @Test
        @DisplayName("GET /orders - Should filter by status")
        void listOrders_FilterByStatus() throws Exception {
            // Given
            when(orderService.getOrdersByStatus(eq(OrderStatus.PENDING), any(Pageable.class)))
                    .thenReturn(orderPage);

            // When & Then
            mockMvc.perform(get("/orders")
                            .param("status", "PENDING"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders/list"))
                    .andExpect(model().attribute("selectedStatus", OrderStatus.PENDING));

            verify(orderService).getOrdersByStatus(eq(OrderStatus.PENDING), any(Pageable.class));
        }

        @Test
        @DisplayName("GET /orders - Should filter by user ID")
        void listOrders_FilterByUserId() throws Exception {
            // Given
            when(orderService.getOrdersByUserId(eq(1L), any(Pageable.class))).thenReturn(orderPage);

            // When & Then
            mockMvc.perform(get("/orders")
                            .param("userId", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders/list"))
                    .andExpect(model().attribute("selectedUserId", 1L));

            verify(orderService).getOrdersByUserId(eq(1L), any(Pageable.class));
        }

        @Test
        @DisplayName("GET /orders/{id} - Should display order details")
        void viewOrder_Success() throws Exception {
            // Given
            when(orderService.getOrderById(1L)).thenReturn(orderResponse);

            // When & Then
            mockMvc.perform(get("/orders/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders/view"))
                    .andExpect(model().attribute("order", orderResponse))
                    .andExpect(model().attributeExists("orderStatuses"));

            verify(orderService).getOrderById(1L);
        }

        @Test
        @DisplayName("GET /orders/new - Should display create order form")
        void showCreateForm_Success() throws Exception {
            // Given
            Page<UserResponse> userPage = TestUtils.createPage(Arrays.asList(userResponse));
            when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

            // When & Then
            mockMvc.perform(get("/orders/new"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders/create"))
                    .andExpect(model().attributeExists("order"))
                    .andExpect(model().attribute("users", userPage.getContent()))
                    .andExpect(model().attributeExists("orderStatuses"));

            verify(userService).getAllUsers(any(PageRequest.class));
        }

        @Test
        @DisplayName("POST /orders - Should create order and redirect")
        void createOrder_MVC_Success() throws Exception {
            // Given
            when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(orderResponse);

            // When & Then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("userId", "1")
                            .param("shippingAddress", "123 Test Street")
                            .param("notes", "Test order"))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/orders/1"))
                    .andExpect(flash().attribute("successMessage", "Order created successfully"));

            verify(orderService).createOrder(any(CreateOrderRequest.class));
        }

        @Test
        @DisplayName("POST /orders - Should handle validation errors")
        void createOrder_MVC_ValidationError() throws Exception {
            // Given
            Page<UserResponse> userPage = TestUtils.createPage(Arrays.asList(userResponse));
            when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

            // When & Then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("userId", "")) // Invalid - empty user ID
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders/create"))
                    .andExpect(model().attributeExists("order"))
                    .andExpect(model().attribute("users", userPage.getContent()));

            verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
            verify(userService).getAllUsers(any(PageRequest.class));
        }

        @Test
        @DisplayName("POST /orders - Should handle business exception")
        void createOrder_MVC_BusinessException() throws Exception {
            // Given
            Page<UserResponse> userPage = TestUtils.createPage(Arrays.asList(userResponse));
            when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);
            when(orderService.createOrder(any(CreateOrderRequest.class)))
                    .thenThrow(new BusinessException("Insufficient stock"));

            // When & Then
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("userId", "1")
                            .param("shippingAddress", "123 Test Street"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders/create"))
                    .andExpect(model().attribute("errorMessage", "Insufficient stock"));

            verify(orderService).createOrder(any(CreateOrderRequest.class));
        }

        @Test
        @DisplayName("POST /orders/{id}/status - Should update order status")
        void updateOrderStatus_MVC_Success() throws Exception {
            // Given
            when(orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED)).thenReturn(orderResponse);

            // When & Then
            mockMvc.perform(post("/orders/1/status")
                            .param("status", "CONFIRMED"))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/orders/1"))
                    .andExpect(flash().attribute("successMessage", "Order status updated successfully"));

            verify(orderService).updateOrderStatus(1L, OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("POST /orders/{id}/cancel - Should cancel order")
        void cancelOrder_MVC_Success() throws Exception {
            // Given
            doNothing().when(orderService).cancelOrder(1L);

            // When & Then
            mockMvc.perform(post("/orders/1/cancel"))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/orders/1"))
                    .andExpect(flash().attribute("successMessage", "Order cancelled successfully"));

            verify(orderService).cancelOrder(1L);
        }

        @Test
        @DisplayName("GET /orders/user/{userId} - Should display user's orders")
        void listOrdersByUser_Success() throws Exception {
            // Given
            when(userService.getUserById(1L)).thenReturn(userResponse);
            when(orderService.getOrdersByUserId(eq(1L), any(Pageable.class))).thenReturn(orderPage);

            // When & Then
            mockMvc.perform(get("/orders/user/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders/user-orders"))
                    .andExpect(model().attribute("orders", orderPage))
                    .andExpect(model().attribute("user", userResponse))
                    .andExpect(model().attribute("currentPage", 0))
                    .andExpect(model().attribute("totalPages", 1));

            verify(userService).getUserById(1L);
            verify(orderService).getOrdersByUserId(eq(1L), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Parameter Validation Tests")
    class ParameterValidationTests {

        @Test
        @DisplayName("Should handle invalid enum values gracefully")
        void handleInvalidEnumValues() throws Exception {
            mockMvc.perform(get("/api/orders/status/INVALID_STATUS"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate date format in date range query")
        void validateDateFormat() throws Exception {
            mockMvc.perform(get("/api/orders/date-range")
                            .param("startDate", "invalid-date")
                            .param("endDate", "2024-01-31T23:59:59"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle large page sizes by limiting to max")
        void handleLargePageSizes() throws Exception {
            // Given
            when(orderService.getAllOrders(any(Pageable.class))).thenReturn(orderPage);

            // When & Then
            mockMvc.perform(get("/api/orders")
                            .param("size", "1000")) // Large size should be limited
                    .andDo(print())
                    .andExpect(status().isOk());

            // Verify that the page size was limited (assuming Constants.MAX_PAGE_SIZE = 100)
            verify(orderService).getAllOrders(argThat(pageable ->
                    pageable.getPageSize() <= 100));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty order list gracefully")
        void handleEmptyOrderList() throws Exception {
            // Given
            Page<OrderResponse> emptyPage = TestUtils.createPage(Arrays.asList());
            when(orderService.getAllOrders(any(Pageable.class))).thenReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/orders"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(0)))
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("Should handle service layer exceptions")
        void handleServiceExceptions() throws Exception {
            // Given
            when(orderService.getAllOrders(any(Pageable.class)))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            mockMvc.perform(get("/api/orders"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should handle null parameters in date range")
        void handleNullDateParameters() throws Exception {
            mockMvc.perform(get("/api/orders/date-range"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}