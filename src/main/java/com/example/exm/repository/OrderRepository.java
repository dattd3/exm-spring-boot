package com.example.exm.repository;

import com.example.exm.entity.Order;
import com.example.exm.entity.OrderStatus;
import com.example.exm.entity.User;
import com.example.exm.repository.custom.CustomOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>,
        JpaSpecificationExecutor<Order>,
        CustomOrderRepository {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUser(User user);

    Page<Order> findByUser(User user, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId,
                                      @Param("status") OrderStatus status);

    List<Order> findByUserEmail(String email);

    List<Order> findByOrderDateAfter(LocalDateTime threeDaysAgo);

    long countByStatus(OrderStatus orderStatus);

    List<Order> findByTotalAmountGreaterThan(BigDecimal threshold);

    List<Order> findByStatusAndOrderDateBetween(OrderStatus orderStatus, LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findAllByOrderByOrderDateDesc();

    List<Order> findByUserAndStatus(User testUser1, OrderStatus orderStatus);

    List<Order> findByShippingAddressContainingIgnoreCase(String mainSt);
}