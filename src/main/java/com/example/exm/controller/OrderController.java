package com.example.exm.controller;

import com.example.exm.dto.request.CreateOrderRequest;
import com.example.exm.dto.response.OrderResponse;
import com.example.exm.dto.response.UserResponse;
import com.example.exm.entity.OrderStatus;
import com.example.exm.service.OrderService;
import com.example.exm.service.UserService;
import com.example.exm.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long userId,
            Model model) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE), sort);

        Page<OrderResponse> orders;
        if (userId != null) {
            orders = orderService.getOrdersByUserId(userId, pageable);
        } else if (status != null) {
            orders = orderService.getOrdersByStatus(status, pageable);
        } else {
            orders = orderService.getAllOrders(pageable);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("orderStatuses", OrderStatus.values());

        return "orders/list";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        OrderResponse order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "orders/view";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("order", new CreateOrderRequest());
        Page<UserResponse> users = userService.getAllUsers(PageRequest.of(0, 100, Sort.by("firstName")));
        model.addAttribute("users", users.getContent());
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "orders/create";
    }

    @PostMapping
    public String createOrder(
            @Valid @ModelAttribute("order") CreateOrderRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            Page<UserResponse> users = userService.getAllUsers(PageRequest.of(0, 100, Sort.by("firstName")));
            model.addAttribute("users", users.getContent());
            model.addAttribute("orderStatuses", OrderStatus.values());
            return "orders/create";
        }

        try {
            OrderResponse createdOrder = orderService.createOrder(request);
            redirectAttributes.addFlashAttribute("successMessage", "Order created successfully");
            return "redirect:/orders/" + createdOrder.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            Page<UserResponse> users = userService.getAllUsers(PageRequest.of(0, 100, Sort.by("firstName")));
            model.addAttribute("users", users.getContent());
            model.addAttribute("orderStatuses", OrderStatus.values());
            return "orders/create";
        }
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            RedirectAttributes redirectAttributes) {

        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Order cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @GetMapping("/user/{userId}")
    public String listOrdersByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        UserResponse user = userService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE),
                Sort.by("orderDate").descending());

        Page<OrderResponse> orders = orderService.getOrdersByUserId(userId, pageable);

        model.addAttribute("orders", orders);
        model.addAttribute("user", user);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());

        return "orders/user-orders";
    }
}