package com.example.exm.service.impl;

import com.example.exm.dto.mapper.OrderMapper;
import com.example.exm.dto.request.CreateOrderRequest;
import com.example.exm.dto.response.OrderResponse;
import com.example.exm.entity.*;
import com.example.exm.exception.BusinessException;
import com.example.exm.exception.ResourceNotFoundException;
import com.example.exm.repository.OrderRepository;
import com.example.exm.service.OrderItemService;
import com.example.exm.service.ProductService;
import com.example.exm.service.UserService;
import com.example.exm.util.OrderNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private OrderItemService orderItemService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private User testUser;
    private Product testProduct;
    private CreateOrderRequest createOrderRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setStatus(UserStatus.ACTIVE);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(50);
        testProduct.setStatus(ProductStatus.ACTIVE);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNumber("ORD20240101120000001");
        testOrder.setUser(testUser);
        testOrder.setTotalAmount(new BigDecimal("199.98"));
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setShippingAddress("123 Main St");
        testOrder.setOrderItems(new ArrayList<>());

        CreateOrderRequest.OrderItemRequest orderItemRequest = new CreateOrderRequest.OrderItemRequest();
        orderItemRequest.setProductId(1L);
        orderItemRequest.setQuantity(2);

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setUserId(1L);
        createOrderRequest.setShippingAddress("123 Main St");
        createOrderRequest.setNotes("Test order");
        createOrderRequest.setOrderItems(Arrays.asList(orderItemRequest));

        orderResponse = new OrderResponse();
        orderResponse.setId(1L);
        orderResponse.setOrderNumber("ORD20240101120000001");
        orderResponse.setTotalAmount(new BigDecimal("199.98"));
        orderResponse.setStatus(OrderStatus.PENDING);
    }

    @Test
    void createOrder_Success() {
        // Given
        when(userService.findUserById(1L)).thenReturn(testUser);
        when(productService.isProductInStock(1L, 2)).thenReturn(true);
        when(productService.findProductById(1L)).thenReturn(testProduct);
        when(orderNumberGenerator.generateOrderNumber()).thenReturn("ORD20240101120000001");
        when(orderMapper.toEntity(createOrderRequest)).thenReturn(testOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.createOrder(createOrderRequest);

        // Then
        assertNotNull(result);
        assertEquals(orderResponse.getId(), result.getId());
        assertEquals(orderResponse.getOrderNumber(), result.getOrderNumber());
        verify(productService).updateProductStock(1L, 48); // 50 - 2 = 48
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_InsufficientStock_ThrowsBusinessException() {
        // Given
        when(userService.findUserById(1L)).thenReturn(testUser);
        when(productService.isProductInStock(1L, 2)).thenReturn(false);
        when(productService.findProductById(1L)).thenReturn(testProduct);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.createOrder(createOrderRequest));

        assertTrue(exception.getMessage().contains("not available in requested quantity"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderById_Success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toResponse(testOrder)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.getOrderById(1L);

        // Then
        assertNotNull(result);
        assertEquals(orderResponse.getId(), result.getId());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(1L));

        assertEquals("Order not found with id: 1", exception.getMessage());
    }

    @Test
    void updateOrderStatus_Success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(testOrder)).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void updateOrderStatus_InvalidTransition_ThrowsBusinessException() {
        // Given
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.updateOrderStatus(1L, OrderStatus.PENDING));

        assertTrue(exception.getMessage().contains("Invalid status transition"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_Success() {
        // Given
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(2);
        testOrder.setOrderItems(Arrays.asList(orderItem));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(testOrder)).thenReturn(testOrder);

        // When
        orderService.cancelOrder(1L);

        // Then
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(productService).updateProductStock(1L, 52); // 50 + 2 = 52 (restore stock)
        verify(orderRepository).save(testOrder);
    }

    @Test
    void cancelOrder_AlreadyDelivered_ThrowsBusinessException() {
        // Given
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(1L));

        assertEquals("Cannot cancel a delivered order", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderByOrderNumber_Success() {
        // Given
        when(orderRepository.findByOrderNumber("ORD20240101120000001")).thenReturn(Optional.of(testOrder));
        when(orderMapper.toResponse(testOrder)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.getOrderByOrderNumber("ORD20240101120000001");

        // Then
        assertNotNull(result);
        assertEquals(orderResponse.getOrderNumber(), result.getOrderNumber());
        verify(orderRepository).findByOrderNumber("ORD20240101120000001");
    }
}