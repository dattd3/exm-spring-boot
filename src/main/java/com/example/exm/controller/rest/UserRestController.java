package com.example.exm.controller.rest;

import com.example.exm.dto.request.CreateUserRequest;
import com.example.exm.dto.request.UpdateUserRequest;
import com.example.exm.dto.response.ApiResponse;
import com.example.exm.dto.response.UserResponse;
import com.example.exm.entity.UserStatus;
import com.example.exm.service.UserService;
import com.example.exm.util.Constants;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserRestController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<ApiResponse<UserResponse>> createUser(
      @Valid @RequestBody CreateUserRequest request) {
    UserResponse userResponse = userService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("User created successfully", userResponse));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> updateUser(
      @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
    UserResponse userResponse = userService.updateUser(id, request);
    return ResponseEntity.ok(ApiResponse.success("User updated successfully", userResponse));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
    UserResponse userResponse = userService.getUserById(id);
    return ResponseEntity.ok(ApiResponse.success(userResponse));
  }

  @GetMapping("/email/{email}")
  public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
    UserResponse userResponse = userService.getUserByEmail(email);
    return ResponseEntity.ok(ApiResponse.success(userResponse));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDirection) {

    Sort sort =
        sortDirection.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

    Pageable pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE), sort);
    Page<UserResponse> users = userService.getAllUsers(pageable);

    return ResponseEntity.ok(ApiResponse.success(users));
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByStatus(
      @PathVariable UserStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDirection) {

    Sort sort =
        sortDirection.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

    Pageable pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE), sort);
    Page<UserResponse> users = userService.getUsersByStatus(status, pageable);

    return ResponseEntity.ok(ApiResponse.success(users));
  }

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsersByName(
      @RequestParam String name) {
    List<UserResponse> users = userService.searchUsersByName(name);
    return ResponseEntity.ok(ApiResponse.success(users));
  }

  @GetMapping("/with-active-orders")
  public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersWithActiveOrders() {
    List<UserResponse> users = userService.getUsersWithActiveOrders();
    return ResponseEntity.ok(ApiResponse.success(users));
  }

  @GetMapping("/top-customers")
  public ResponseEntity<ApiResponse<List<UserResponse>>> getTopCustomers(
      @RequestParam(defaultValue = "10") int limit) {
    List<UserResponse> users = userService.getTopCustomers(limit);
    return ResponseEntity.ok(ApiResponse.success(users));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
  }

  @GetMapping("/exists")
  public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@RequestParam String email) {
    boolean exists = userService.existsByEmail(email);
    return ResponseEntity.ok(ApiResponse.success(exists));
  }
}
