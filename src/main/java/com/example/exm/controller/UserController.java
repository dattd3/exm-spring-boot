package com.example.exm.controller;

import com.example.exm.dto.request.CreateUserRequest;
import com.example.exm.dto.request.UpdateUserRequest;
import com.example.exm.dto.response.UserResponse;
import com.example.exm.entity.UserStatus;
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
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) UserStatus status,
            Model model) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE), sort);

        Page<UserResponse> users;
        if (status != null) {
            users = userService.getUsersByStatus(status, pageable);
        } else {
            users = userService.getAllUsers(pageable);
        }

        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("userStatuses", UserStatus.values());

        return "users/list";
    }

    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        UserResponse user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "users/view";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new CreateUserRequest());
        model.addAttribute("userStatuses", UserStatus.values());
        return "users/create";
    }

    @PostMapping
    public String createUser(
            @Valid @ModelAttribute("user") CreateUserRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("userStatuses", UserStatus.values());
            return "users/create";
        }

        try {
            UserResponse createdUser = userService.createUser(request);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully");
            return "redirect:/users/" + createdUser.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("userStatuses", UserStatus.values());
            return "users/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        UserResponse user = userService.getUserById(id);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setFirstName(user.getFirstName());
        updateRequest.setLastName(user.getLastName());
        updateRequest.setEmail(user.getEmail());
        updateRequest.setPhoneNumber(user.getPhoneNumber());
        updateRequest.setAddress(user.getAddress());
        updateRequest.setStatus(user.getStatus());

        model.addAttribute("user", updateRequest);
        model.addAttribute("userId", id);
        model.addAttribute("userStatuses", UserStatus.values());

        return "users/edit";
    }

    @PostMapping("/{id}")
    public String updateUser(
            @PathVariable Long id,
            @Valid @ModelAttribute("user") UpdateUserRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("userStatuses", UserStatus.values());
            return "users/edit";
        }

        try {
            userService.updateUser(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
            return "redirect:/users/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("userId", id);
            model.addAttribute("userStatuses", UserStatus.values());
            return "users/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    @GetMapping("/search")
    public String searchUsers(@RequestParam String query, Model model) {
        model.addAttribute("users", userService.searchUsersByName(query));
        model.addAttribute("searchQuery", query);
        return "users/search-results";
    }
}