package com.example.exm.dto.request;

import com.example.exm.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private String shippingAddress;

    private String notes;

    private OrderStatus status = OrderStatus.PENDING;

    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> orderItems;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}