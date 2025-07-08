package com.example.exm.service;

import com.example.exm.dto.mapper.ProductMapper;
import com.example.exm.dto.request.CreateProductRequest;
import com.example.exm.dto.response.ProductResponse;
import com.example.exm.entity.Product;
import com.example.exm.entity.ProductStatus;
import com.example.exm.exception.BusinessException;
import com.example.exm.exception.ResourceNotFoundException;
import com.example.exm.repository.ProductRepository;
import com.example.exm.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private CreateProductRequest createProductRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(50);
        testProduct.setCategory("Electronics");
        testProduct.setBrand("TestBrand");
        testProduct.setStatus(ProductStatus.ACTIVE);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());

        createProductRequest = new CreateProductRequest();
        createProductRequest.setName("Test Product");
        createProductRequest.setDescription("Test Description");
        createProductRequest.setPrice(new BigDecimal("99.99"));
        createProductRequest.setStockQuantity(50);
        createProductRequest.setCategory("Electronics");
        createProductRequest.setBrand("TestBrand");
        createProductRequest.setStatus(ProductStatus.ACTIVE);

        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Test Product");
        productResponse.setPrice(new BigDecimal("99.99"));
        productResponse.setStockQuantity(50);
        productResponse.setStatus(ProductStatus.ACTIVE);
    }

    @Test
    void createProduct_Success() {
        // Given
        when(productMapper.toEntity(createProductRequest)).thenReturn(testProduct);
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(productMapper.toResponse(testProduct)).thenReturn(productResponse);

        // When
        ProductResponse result = productService.createProduct(createProductRequest);

        // Then
        assertNotNull(result);
        assertEquals(productResponse.getId(), result.getId());
        assertEquals(productResponse.getName(), result.getName());
        verify(productRepository).save(testProduct);
    }

    @Test
    void getProductById_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toResponse(testProduct)).thenReturn(productResponse);

        // When
        ProductResponse result = productService.getProductById(1L);

        // Then
        assertNotNull(result);
        assertEquals(productResponse.getId(), result.getId());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(1L));

        assertEquals("Product not found with id: 1", exception.getMessage());
    }

    @Test
    void updateProductStock_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProduct)).thenReturn(testProduct);

        // When
        productService.updateProductStock(1L, 25);

        // Then
        assertEquals(25, testProduct.getStockQuantity());
        verify(productRepository).save(testProduct);
    }

    @Test
    void updateProductStock_NegativeQuantity_ThrowsBusinessException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.updateProductStock(1L, -5));

        assertEquals("Stock quantity cannot be negative", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void isProductInStock_True() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        boolean result = productService.isProductInStock(1L, 25);

        // Then
        assertTrue(result);
    }

    @Test
    void isProductInStock_InsufficientStock_False() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        boolean result = productService.isProductInStock(1L, 100);

        // Then
        assertFalse(result);
    }

    @Test
    void isProductInStock_InactiveProduct_False() {
        // Given
        testProduct.setStatus(ProductStatus.DISCONTINUED);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        boolean result = productService.isProductInStock(1L, 25);

        // Then
        assertFalse(result);
    }

    @Test
    void deleteProduct_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProduct)).thenReturn(testProduct);

        // When
        productService.deleteProduct(1L);

        // Then
        assertEquals(ProductStatus.DISCONTINUED, testProduct.getStatus());
        verify(productRepository).save(testProduct);
    }
}