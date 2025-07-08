package com.example.exm.specification;

import com.example.exm.entity.Order;
import com.example.exm.entity.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSpecification {

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Order> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<Order> hasOrderNumber(String orderNumber) {
        return (root, query, criteriaBuilder) -> {
            if (orderNumber == null || orderNumber.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("orderNumber"), orderNumber);
        };
    }

    public static Specification<Order> totalAmountGreaterThan(BigDecimal amount) {
        return (root, query, criteriaBuilder) -> {
            if (amount == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThan(root.get("totalAmount"), amount);
        };
    }

    public static Specification<Order> totalAmountLessThan(BigDecimal amount) {
        return (root, query, criteriaBuilder) -> {
            if (amount == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThan(root.get("totalAmount"), amount);
        };
    }

    public static Specification<Order> totalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) -> {
            if (minAmount == null && maxAmount == null) {
                return criteriaBuilder.conjunction();
            }
            if (minAmount == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), maxAmount);
            }
            if (maxAmount == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), minAmount);
            }
            return criteriaBuilder.between(root.get("totalAmount"), minAmount, maxAmount);
        };
    }

    public static Specification<Order> orderDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (startDate == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), endDate);
            }
            if (endDate == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), startDate);
            }
            return criteriaBuilder.between(root.get("orderDate"), startDate, endDate);
        };
    }

    public static Specification<Order> hasShippingAddress(String shippingAddress) {
        return (root, query, criteriaBuilder) -> {
            if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("shippingAddress")),
                    "%" + shippingAddress.toLowerCase() + "%"
            );
        };
    }
}