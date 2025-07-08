package com.example.exm.repository.custom;

import com.example.exm.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<User> findUsersWithActiveOrders() {
        String jpql = "SELECT DISTINCT u FROM User u " +
                "JOIN u.orders o " +
                "WHERE o.status IN ('PENDING', 'CONFIRMED', 'PROCESSING')";

        TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
        return query.getResultList();
    }

    @Override
    public Page<User> findUsersWithRecentActivity(int days, Pageable pageable) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

        String jpql = "SELECT DISTINCT u FROM User u " +
                "JOIN u.orders o " +
                "WHERE o.orderDate >= :cutoffDate " +
                "ORDER BY o.orderDate DESC";

        TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
        query.setParameter("cutoffDate", cutoffDate);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<User> users = query.getResultList();

        // Count query
        String countJpql = "SELECT COUNT(DISTINCT u) FROM User u " +
                "JOIN u.orders o " +
                "WHERE o.orderDate >= :cutoffDate";

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        countQuery.setParameter("cutoffDate", cutoffDate);
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(users, pageable, total);
    }

    @Override
    public List<User> findTopCustomersByOrderCount(int limit) {
        String jpql = "SELECT u FROM User u " +
                "LEFT JOIN u.orders o " +
                "GROUP BY u " +
                "ORDER BY COUNT(o) DESC";

        TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}