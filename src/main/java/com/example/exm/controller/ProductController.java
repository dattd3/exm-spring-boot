package com.example.exm.controller;

import com.example.exm.dto.request.CreateProductRequest;
import com.example.exm.dto.response.ProductResponse;
import com.example.exm.entity.ProductStatus;
import com.example.exm.service.ProductService;
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

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String category,
            Model model) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE), sort);

        Page<ProductResponse> products;
        if (status != null) {
            products = productService.getProductsByStatus(status, pageable);
        } else if (category != null && !category.trim().isEmpty()) {
            products = productService.getProductsByCategory(category, pageable);
        } else {
            products = productService.getAllProducts(pageable);
        }

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("productStatuses", ProductStatus.values());

        return "products/list";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        ProductResponse product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "products/view";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new CreateProductRequest());
        model.addAttribute("productStatuses", ProductStatus.values());
        return "products/create";
    }

    @PostMapping
    public String createProduct(
            @Valid @ModelAttribute("product") CreateProductRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("productStatuses", ProductStatus.values());
            return "products/create";
        }

        try {
            ProductResponse createdProduct = productService.createProduct(request);
            redirectAttributes.addFlashAttribute("successMessage", "Product created successfully");
            return "redirect:/products/" + createdProduct.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("productStatuses", ProductStatus.values());
            return "products/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        ProductResponse product = productService.getProductById(id);

        CreateProductRequest updateRequest = new CreateProductRequest();
        updateRequest.setName(product.getName());
        updateRequest.setDescription(product.getDescription());
        updateRequest.setPrice(product.getPrice());
        updateRequest.setStockQuantity(product.getStockQuantity());
        updateRequest.setCategory(product.getCategory());
        updateRequest.setBrand(product.getBrand());
        updateRequest.setImageUrl(product.getImageUrl());
        updateRequest.setStatus(product.getStatus());

        model.addAttribute("product", updateRequest);
        model.addAttribute("productId", id);
        model.addAttribute("productStatuses", ProductStatus.values());

        return "products/edit";
    }

    @PostMapping("/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") CreateProductRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            model.addAttribute("productStatuses", ProductStatus.values());
            return "products/edit";
        }

        try {
            productService.updateProduct(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully");
            return "redirect:/products/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("productId", id);
            model.addAttribute("productStatuses", ProductStatus.values());
            return "products/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam String query, Model model) {
        List<ProductResponse> products = productService.searchProductsByName(query);
        model.addAttribute("products", products);
        model.addAttribute("searchQuery", query);
        return "products/search-results";
    }

    @GetMapping("/low-stock")
    public String lowStockProducts(Model model) {
        List<ProductResponse> products = productService.getLowStockProducts();
        model.addAttribute("products", products);
        return "products/low-stock";
    }

    @PostMapping("/{id}/stock")
    public String updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            RedirectAttributes redirectAttributes) {

        try {
            productService.updateProductStock(id, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Stock updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products/" + id;
    }
}