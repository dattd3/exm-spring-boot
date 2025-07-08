package com.example.exm.service;

import com.example.exm.dto.request.CreateOrderRequest;
import com.example.exm.dto.response.OrderResponse;
import com.example.exm.entity.Order;
import com.example.exm.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse updateOrderStatus(Long id, OrderStatus status);
    OrderResponse getOrderById(Long id);
    Order findOrderById(Long id);
    OrderResponse getOrderByOrderNumber(String orderNumber);
    Page<OrderResponse> getAllOrders(Pageable pageable);
    Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable);
    Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);
    List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate);
    void cancelOrder(Long id);
    List<OrderResponse> getOrdersWithMultipleItems(int minItems);
}