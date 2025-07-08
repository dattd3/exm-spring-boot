package com.example.exm.service;

import com.example.exm.dto.request.CreateUserRequest;
import com.example.exm.dto.request.UpdateUserRequest;
import com.example.exm.dto.response.UserResponse;
import com.example.exm.entity.User;
import com.example.exm.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    UserResponse getUserById(Long id);
    User findUserById(Long id);
    UserResponse getUserByEmail(String email);
    Page<UserResponse> getAllUsers(Pageable pageable);
    Page<UserResponse> getUsersByStatus(UserStatus status, Pageable pageable);
    List<UserResponse> searchUsersByName(String name);
    void deleteUser(Long id);
    boolean existsByEmail(String email);
    List<UserResponse> getUsersWithActiveOrders();
    List<UserResponse> getTopCustomers(int limit);
}