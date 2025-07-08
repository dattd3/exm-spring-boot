package com.example.exm.service.impl;

import com.example.exm.entity.Order;
import com.example.exm.entity.OrderItem;
import com.example.exm.entity.Product;
import com.example.exm.exception.BusinessException;
import com.example.exm.exception.ResourceNotFoundException;
import com.example.exm.repository.OrderItemRepository;
import com.example.exm.service.OrderItemService;
import com.example.exm.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;

    @Override
    public List<OrderItem> createOrderItems(Long orderId, List<OrderItem> orderItems) {
        log.info("Creating order items for order ID: {}", orderId);

        for (OrderItem item : orderItems) {
            validateOrderItem(item);
        }

        List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);
        log.info("Created {} order items for order ID: {}", savedItems.size(), orderId);

        return savedItems;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByProductId(Long productId) {
        return orderItemRepository.findByProductId(productId);
    }

    @Override
    public void updateOrderItem(Long id, Integer quantity) {
        log.info("Updating order item ID: {} with quantity: {}", id, quantity);

        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", id));

        if (quantity <= 0) {
            throw new BusinessException("Quantity must be greater than 0");
        }

        // Check if product is available in required quantity
        Product product = orderItem.getProduct();
        int currentQuantity = orderItem.getQuantity();
        int stockChange = quantity - currentQuantity;

        if (stockChange > 0 && product.getStockQuantity() < stockChange) {
            throw new BusinessException("Insufficient stock for product: " + product.getName());
        }

        orderItem.setQuantity(quantity);
        orderItem.setTotalPrice(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));

        orderItemRepository.save(orderItem);

        // Update product stock
        int newStock = product.getStockQuantity() - stockChange;
        productService.updateProductStock(product.getId(), newStock);

        log.info("Order item updated successfully with ID: {}", id);
    }

    @Override
    public void deleteOrderItem(Long id) {
        log.info("Deleting order item with ID: {}", id);

        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", id));

        // Restore product stock
        Product product = orderItem.getProduct();
        int restoredStock = product.getStockQuantity() + orderItem.getQuantity();
        productService.updateProductStock(product.getId(), restoredStock);

        orderItemRepository.delete(orderItem);

        log.info("Order item deleted successfully with ID: {}", id);
    }

    private void validateOrderItem(OrderItem orderItem) {
        if (orderItem.getQuantity() <= 0) {
            throw new BusinessException("Order item quantity must be greater than 0");
        }

        if (orderItem.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Order item unit price must be greater than 0");
        }

        if (orderItem.getProduct() == null) {
            throw new BusinessException("Order item must have a product");
        }

        if (orderItem.getOrder() == null) {
            throw new BusinessException("Order item must be associated with an order");
        }
    }
}