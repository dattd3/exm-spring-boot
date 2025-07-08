package com.example.exm.repository.custom;

import com.example.exm.entity.Order;
import com.example.exm.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CustomOrderRepository {
    List<Order> findOrdersByDateRangeAndStatus(LocalDateTime startDate, LocalDateTime endDate, OrderStatus status);
    Page<Order> findOrdersWithTotalAmountGreaterThan(BigDecimal amount, Pageable pageable);
    List<Order> findOrdersWithMultipleItems(int minItems);
    BigDecimal getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);

}