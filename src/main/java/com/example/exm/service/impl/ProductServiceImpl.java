package com.example.exm.service.impl;

import com.example.exm.dto.mapper.ProductMapper;
import com.example.exm.dto.request.CreateProductRequest;
import com.example.exm.dto.response.ProductResponse;
import com.example.exm.entity.Product;
import com.example.exm.entity.ProductStatus;
import com.example.exm.exception.BusinessException;
import com.example.exm.exception.ResourceNotFoundException;
import com.example.exm.repository.ProductRepository;
import com.example.exm.service.ProductService;
import com.example.exm.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product: {}", request.getName());

        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return productMapper.toResponse(savedProduct);
    }

    @Override
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = findProductById(id);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        product.setBrand(request.getBrand());
        product.setImageUrl(request.getImageUrl());
        product.setStatus(request.getStatus());

        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = findProductById(id);
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByStatus(ProductStatus status, Pageable pageable) {
        return productRepository.findByStatus(status, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProductsByName(String name) {
        return productRepository.findByNameContaining(name)
                .stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts(Constants.LOW_STOCK_THRESHOLD)
                .stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = findProductById(id);
        product.setStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);

        log.info("Product marked as discontinued with ID: {}", id);
    }

    @Override
    public void updateProductStock(Long id, Integer quantity) {
        log.info("Updating stock for product ID: {} with quantity: {}", id, quantity);

        Product product = findProductById(id);

        if (quantity < 0) {
            throw new BusinessException("Stock quantity cannot be negative");
        }

        product.setStockQuantity(quantity);
        productRepository.save(product);

        log.info("Stock updated successfully for product ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductInStock(Long id, Integer requiredQuantity) {
        Product product = findProductById(id);
        return product.getStockQuantity() >= requiredQuantity &&
                product.getStatus() == ProductStatus.ACTIVE;
    }
}