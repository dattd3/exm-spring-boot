package com.example.exm.repository.custom;

import com.example.exm.entity.Order;
import com.example.exm.entity.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CustomOrderRepositoryImpl implements CustomOrderRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Order> findOrdersByDateRangeAndStatus(LocalDateTime startDate, LocalDateTime endDate, OrderStatus status) {
        String jpql = "SELECT o FROM Order o " +
                "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                "AND o.status = :status " +
                "ORDER BY o.orderDate DESC";

        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    public Page<Order> findOrdersWithTotalAmountGreaterThan(BigDecimal amount, Pageable pageable) {
        String jpql = "SELECT o FROM Order o " +
                "WHERE o.totalAmount > :amount " +
                "ORDER BY o.totalAmount DESC";

        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class);
        query.setParameter("amount", amount);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Order> orders = query.getResultList();

        String countJpql = "SELECT COUNT(o) FROM Order o WHERE o.totalAmount > :amount";
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        countQuery.setParameter("amount", amount);
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(orders, pageable, total);
    }

    @Override
    public List<Order> findOrdersWithMultipleItems(int minItems) {
        String jpql = "SELECT o FROM Order o " +
                "WHERE SIZE(o.orderItems) >= :minItems " +
                "ORDER BY SIZE(o.orderItems) DESC";

        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class);
        query.setParameter("minItems", minItems);
        return query.getResultList();
    }

    @Override
    public BigDecimal getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String jpql = "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
                "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
                "AND o.status IN ('DELIVERED', 'CONFIRMED')";

        TypedQuery<BigDecimal> query = entityManager.createQuery(jpql, BigDecimal.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getSingleResult();
    }
}