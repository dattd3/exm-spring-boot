package com.example.exm.integration;

import com.example.exm.config.JpaAuditingConfig;
import com.example.exm.entity.Product;
import com.example.exm.entity.ProductStatus;
import com.example.exm.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
//@Import(JpaAuditingConfig.class)
//@TestPropertySource(properties = {
//        "spring.jpa.hibernate.ddl-auto=create-drop",
//        "spring.datasource.url=jdbc:h2:mem:testdb"
//})
class ProductIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clean up database
        productRepository.deleteAll();

        // Create test product
        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("29.99"))
                .stockQuantity(50)
                .inStock(true)
                .category("Electronics")
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void shouldCreateProduct() throws Exception {
        String productJson = objectMapper.writeValueAsString(testProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.category").value("Electronics"));

        List<Product> products = productRepository.findAll();
        assertEquals(1, products.size());
        assertEquals("Test Product", products.get(0).getName());
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        // Save test product
        productRepository.save(testProduct);
        entityManager.flush();

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void shouldGetProductById() throws Exception {
        Product savedProduct = productRepository.save(testProduct);
        entityManager.flush();

        mockMvc.perform(get("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.id").value(savedProduct.getId()));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        Product savedProduct = productRepository.save(testProduct);
        entityManager.flush();

        Product updatedProduct = Product.builder()
                .id(savedProduct.getId())
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(39.99))
                .category("Electronics")
                .inStock(false)
                .build();

        String updatedProductJson = objectMapper.writeValueAsString(updatedProduct);

        mockMvc.perform(put("/api/products/{id}", savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedProductJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(39.99))
                .andExpect(jsonPath("$.inStock").value(false));

        Product retrievedProduct = productRepository.findById(savedProduct.getId()).orElse(null);
        assertNotNull(retrievedProduct);
        assertEquals("Updated Product", retrievedProduct.getName());
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        Product savedProduct = productRepository.save(testProduct);
        entityManager.flush();

        mockMvc.perform(delete("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isNoContent());

        assertFalse(productRepository.findById(savedProduct.getId()).isPresent());
    }

    @Test
    void shouldReturnNotFoundForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateProductCreation() throws Exception {
        Product invalidProduct = Product.builder()
                .name("") // Invalid empty name
                .price(BigDecimal.valueOf(-10)) // Invalid negative price
                .build();

        String invalidProductJson = objectMapper.writeValueAsString(invalidProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidProductJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSearchProductsByCategory() throws Exception {
        productRepository.save(testProduct);

        Product anotherProduct = Product.builder()
                .name("Another Product")
                .description("Another Description")
                .price(BigDecimal.valueOf(19.99))
                .category("Books")
                .inStock(true)
                .build();
        productRepository.save(anotherProduct);
        entityManager.flush();

        mockMvc.perform(get("/api/products")
                        .param("category", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].category").value("Electronics"));
    }

    @Test
    void shouldHandleConcurrentProductUpdates() throws Exception {
        Product savedProduct = productRepository.save(testProduct);
        entityManager.flush();

        // This test would require additional setup for actual concurrency testing
        // For now, it's a placeholder for testing optimistic locking if implemented

        Product updatedProduct = Product.builder()
                .id(savedProduct.getId())
                .name("Concurrent Update")
                .description("Concurrent Description")
                .price(BigDecimal.valueOf(49.99))
                .category("Electronics")
                .inStock(true)
                .build();

        String updatedProductJson = objectMapper.writeValueAsString(updatedProduct);

        mockMvc.perform(put("/api/products/{id}", savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedProductJson))
                .andExpect(status().isOk());
    }
}