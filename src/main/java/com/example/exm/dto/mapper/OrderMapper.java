package com.example.exm.dto.mapper;

import com.example.exm.dto.request.CreateOrderRequest;
import com.example.exm.dto.response.OrderResponse;
import com.example.exm.entity.Order;
import com.example.exm.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order toEntity(CreateOrderRequest request) {
        Order order = new Order();
        order.setShippingAddress(request.getShippingAddress());
        order.setNotes(request.getNotes());
        order.setStatus(request.getStatus());
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setUserId(order.getUser().getId());
        response.setUserFullName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setOrderDate(order.getOrderDate());
        response.setShippingAddress(order.getShippingAddress());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        if (order.getOrderItems() != null) {
            response.setOrderItems(order.getOrderItems().stream()
                    .map(this::toOrderItemResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private OrderResponse.OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        OrderResponse.OrderItemResponse response = new OrderResponse.OrderItemResponse();
        response.setId(orderItem.getId());
        response.setProductId(orderItem.getProduct().getId());
        response.setProductName(orderItem.getProduct().getName());
        response.setQuantity(orderItem.getQuantity());
        response.setUnitPrice(orderItem.getUnitPrice());
        response.setTotalPrice(orderItem.getTotalPrice());
        return response;
    }
}