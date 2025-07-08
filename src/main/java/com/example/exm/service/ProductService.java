package com.example.exm.service;

import com.example.exm.dto.request.CreateProductRequest;
import com.example.exm.dto.response.ProductResponse;
import com.example.exm.entity.Product;
import com.example.exm.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse updateProduct(Long id, CreateProductRequest request);
    ProductResponse getProductById(Long id);
    Product findProductById(Long id);
    Page<ProductResponse> getAllProducts(Pageable pageable);
    Page<ProductResponse> getProductsByStatus(ProductStatus status, Pageable pageable);
    Page<ProductResponse> getProductsByCategory(String category, Pageable pageable);
    List<ProductResponse> searchProductsByName(String name);
    List<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    List<ProductResponse> getLowStockProducts();
    void deleteProduct(Long id);
    void updateProductStock(Long id, Integer quantity);
    boolean isProductInStock(Long id, Integer requiredQuantity);
}