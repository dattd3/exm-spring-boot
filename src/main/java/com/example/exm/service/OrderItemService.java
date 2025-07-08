package com.example.exm.service;

import com.example.exm.entity.OrderItem;

import java.util.List;

public interface OrderItemService {
    List<OrderItem> createOrderItems(Long orderId, List<OrderItem> orderItems);
    List<OrderItem> getOrderItemsByOrderId(Long orderId);
    List<OrderItem> getOrderItemsByProductId(Long productId);
    void updateOrderItem(Long id, Integer quantity);
    void deleteOrderItem(Long id);
}