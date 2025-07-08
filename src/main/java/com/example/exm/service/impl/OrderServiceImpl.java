package com.example.exm.service.impl;

import com.example.exm.dto.mapper.OrderMapper;
import com.example.exm.dto.request.CreateOrderRequest;
import com.example.exm.dto.response.OrderResponse;
import com.example.exm.entity.*;
import com.example.exm.exception.BusinessException;
import com.example.exm.exception.ResourceNotFoundException;
import com.example.exm.repository.OrderRepository;
import com.example.exm.service.OrderItemService;
import com.example.exm.service.OrderService;
import com.example.exm.service.ProductService;
import com.example.exm.service.UserService;
import com.example.exm.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderNumberGenerator orderNumberGenerator;
    private final UserService userService;
    private final ProductService productService;
    private final OrderItemService orderItemService;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user ID: {}", request.getUserId());

        // Validate user exists
        User user = userService.findUserById(request.getUserId());

        // Validate products and stock
        validateOrderItems(request.getOrderItems());

        // Create order
        Order order = orderMapper.toEntity(request);
        order.setUser(user);
        order.setOrderNumber(orderNumberGenerator.generateOrderNumber());
        order.setOrderDate(LocalDateTime.now());

        // Calculate total amount and create order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
            Product product = productService.findProductById(itemRequest.getProductId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());

            // Update product stock
            int newStock = product.getStockQuantity() - itemRequest.getQuantity();
            productService.updateProductStock(product.getId(), newStock);
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with ID: {} and order number: {}",
                savedOrder.getId(), savedOrder.getOrderNumber());

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        log.info("Updating order status for ID: {} to {}", id, status);

        Order order = findOrderById(id);
        OrderStatus previousStatus = order.getStatus();

        validateStatusTransition(previousStatus, status);

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order status updated successfully for ID: {}", id);
        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = findOrderById(id);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        User user = userService.findUserById(userId);
        return orderRepository.findByUser(user, pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate)
                .stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.getTotalRevenueByDateRange(startDate, endDate);
    }

    @Override
    public void cancelOrder(Long id) {
        log.info("Cancelling order with ID: {}", id);

        Order order = findOrderById(id);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel a delivered order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Order is already cancelled");
        }

        // Restore product stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            int restoredStock = product.getStockQuantity() + item.getQuantity();
            productService.updateProductStock(product.getId(), restoredStock);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Order cancelled successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersWithMultipleItems(int minItems) {
        return orderRepository.findOrdersWithMultipleItems(minItems)
                .stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateOrderItems(List<CreateOrderRequest.OrderItemRequest> orderItems) {
        for (CreateOrderRequest.OrderItemRequest item : orderItems) {
            if (!productService.isProductInStock(item.getProductId(), item.getQuantity())) {
                Product product = productService.findProductById(item.getProductId());
                throw new BusinessException(
                        String.format("Product '%s' is not available in requested quantity. Available: %d, Requested: %d",
                                product.getName(), product.getStockQuantity(), item.getQuantity()));
            }
        }
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        // Define valid transitions
        switch (from) {
            case PENDING:
                if (to != OrderStatus.CONFIRMED && to != OrderStatus.CANCELLED) {
                    throw new BusinessException("Invalid status transition from PENDING to " + to);
                }
                break;
            case CONFIRMED:
                if (to != OrderStatus.PROCESSING && to != OrderStatus.CANCELLED) {
                    throw new BusinessException("Invalid status transition from CONFIRMED to " + to);
                }
                break;
            case PROCESSING:
                if (to != OrderStatus.SHIPPED && to != OrderStatus.CANCELLED) {
                    throw new BusinessException("Invalid status transition from PROCESSING to " + to);
                }
                break;
            case SHIPPED:
                if (to != OrderStatus.DELIVERED) {
                    throw new BusinessException("Invalid status transition from SHIPPED to " + to);
                }
                break;
            case DELIVERED:
                if (to != OrderStatus.REFUNDED) {
                    throw new BusinessException("Invalid status transition from DELIVERED to " + to);
                }
                break;
            case CANCELLED:
            case REFUNDED:
                throw new BusinessException("Cannot change status from " + from);
        }
    }
}