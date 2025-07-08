package com.example.exm.repository.custom;

import com.example.exm.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomUserRepository {
    List<User> findUsersWithActiveOrders();
    Page<User> findUsersWithRecentActivity(int days, Pageable pageable);
    List<User> findTopCustomersByOrderCount(int limit);
}