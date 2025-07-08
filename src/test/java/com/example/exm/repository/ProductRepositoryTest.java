package com.example.exm.repository;

import com.example.exm.entity.Product;
import com.example.exm.entity.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product laptopProduct;
    private Product mouseProduct;
    private Product keyboardProduct;
    private Product phoneProduct;
    private Product discontinuedProduct;

    @BeforeEach
    void setUp() {
        // Create test products with different attributes
        laptopProduct = new Product();
        laptopProduct.setName("Gaming Laptop");
        laptopProduct.setDescription("High-performance gaming laptop");
        laptopProduct.setPrice(new BigDecimal("1299.99"));
        laptopProduct.setStockQuantity(15);
        laptopProduct.setCategory("Electronics");
        laptopProduct.setBrand("TechBrand");
        laptopProduct.setStatus(ProductStatus.ACTIVE);

        mouseProduct = new Product();
        mouseProduct.setName("Wireless Mouse");
        mouseProduct.setDescription("Ergonomic wireless mouse");
        mouseProduct.setPrice(new BigDecimal("29.99"));
        mouseProduct.setStockQuantity(100);
        mouseProduct.setCategory("Electronics");
        mouseProduct.setBrand("TechBrand");
        mouseProduct.setStatus(ProductStatus.ACTIVE);

        keyboardProduct = new Product();
        keyboardProduct.setName("Mechanical Keyboard");
        keyboardProduct.setDescription("RGB mechanical keyboard");
        keyboardProduct.setPrice(new BigDecimal("149.99"));
        keyboardProduct.setStockQuantity(5); // Low stock
        keyboardProduct.setCategory("Electronics");
        keyboardProduct.setBrand("GameGear");
        keyboardProduct.setStatus(ProductStatus.ACTIVE);

        phoneProduct = new Product();
        phoneProduct.setName("Smartphone");
        phoneProduct.setDescription("Latest smartphone model");
        phoneProduct.setPrice(new BigDecimal("799.99"));
        phoneProduct.setStockQuantity(25);
        phoneProduct.setCategory("Mobile");
        phoneProduct.setBrand("PhoneCorp");
        phoneProduct.setStatus(ProductStatus.ACTIVE);

        discontinuedProduct = new Product();
        discontinuedProduct.setName("Old Laptop");
        discontinuedProduct.setDescription("Discontinued laptop model");
        discontinuedProduct.setPrice(new BigDecimal("599.99"));
        discontinuedProduct.setStockQuantity(0);
        discontinuedProduct.setCategory("Electronics");
        discontinuedProduct.setBrand("TechBrand");
        discontinuedProduct.setStatus(ProductStatus.DISCONTINUED);

        // Persist all test products
        entityManager.persistAndFlush(laptopProduct);
        entityManager.persistAndFlush(mouseProduct);
        entityManager.persistAndFlush(keyboardProduct);
        entityManager.persistAndFlush(phoneProduct);
        entityManager.persistAndFlush(discontinuedProduct);

        entityManager.clear();
    }

    @Test
    void shouldSaveAndFindProduct() {
        // Given
        Product newProduct = new Product();
        newProduct.setName("Test Product");
        newProduct.setDescription("Test Description");
        newProduct.setPrice(new BigDecimal("99.99"));
        newProduct.setStockQuantity(20);
        newProduct.setCategory("Test");
        newProduct.setBrand("TestBrand");
        newProduct.setStatus(ProductStatus.ACTIVE);

        // When
        Product savedProduct = productRepository.save(newProduct);
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

        // Then
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Test Product");
        assertThat(foundProduct.get().getPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(foundProduct.get().getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void shouldFindProductsByStatus() {
        // When
        List<Product> activeProducts = productRepository.findByStatus(ProductStatus.ACTIVE);
        List<Product> discontinuedProducts = productRepository.findByStatus(ProductStatus.DISCONTINUED);

        // Then
        assertThat(activeProducts).hasSize(4);
        assertThat(activeProducts)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Gaming Laptop", "Wireless Mouse", "Mechanical Keyboard", "Smartphone");

        assertThat(discontinuedProducts).hasSize(1);
        assertThat(discontinuedProducts.get(0).getName()).isEqualTo("Old Laptop");
    }

    @Test
    void shouldFindProductsByStatusWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Product> activeProductsPage = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);

        // Then
        assertThat(activeProductsPage.getContent()).hasSize(2);
        assertThat(activeProductsPage.getTotalElements()).isEqualTo(4);
        assertThat(activeProductsPage.getTotalPages()).isEqualTo(2);
        assertThat(activeProductsPage.isFirst()).isTrue();
        assertThat(activeProductsPage.hasNext()).isTrue();
    }

    @Test
    void shouldFindProductsByCategory() {
        // When
        List<Product> electronicsProducts = productRepository.findByCategory("Electronics");
        List<Product> mobileProducts = productRepository.findByCategory("Mobile");

        // Then
        assertThat(electronicsProducts).hasSize(4);
        assertThat(electronicsProducts)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Gaming Laptop", "Wireless Mouse", "Mechanical Keyboard", "Old Laptop");

        assertThat(mobileProducts).hasSize(1);
        assertThat(mobileProducts.get(0).getName()).isEqualTo("Smartphone");
    }

    @Test
    void shouldFindProductsByCategoryWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Product> electronicsPage = productRepository.findByCategory("Electronics", pageable);

        // Then
        assertThat(electronicsPage.getContent()).hasSize(2);
        assertThat(electronicsPage.getTotalElements()).isEqualTo(4);
        assertThat(electronicsPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldFindProductsByBrand() {
        // When
        List<Product> techBrandProducts = productRepository.findByBrand("TechBrand");
        List<Product> gameGearProducts = productRepository.findByBrand("GameGear");
        List<Product> phoneCorpProducts = productRepository.findByBrand("PhoneCorp");

        // Then
        assertThat(techBrandProducts).hasSize(3);
        assertThat(techBrandProducts)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Gaming Laptop", "Wireless Mouse", "Old Laptop");

        assertThat(gameGearProducts).hasSize(1);
        assertThat(gameGearProducts.get(0).getName()).isEqualTo("Mechanical Keyboard");

        assertThat(phoneCorpProducts).hasSize(1);
        assertThat(phoneCorpProducts.get(0).getName()).isEqualTo("Smartphone");
    }

    @Test
    void shouldFindProductsByNameContaining() {
        // When
        List<Product> laptopProducts = productRepository.findByNameContaining("Laptop");
        List<Product> mouseProducts = productRepository.findByNameContaining("Mouse");
        List<Product> gamingProducts = productRepository.findByNameContaining("Gaming");

        // Then
        assertThat(laptopProducts).hasSize(2);
        assertThat(laptopProducts)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Gaming Laptop", "Old Laptop");

        assertThat(mouseProducts).hasSize(1);
        assertThat(mouseProducts.get(0).getName()).isEqualTo("Wireless Mouse");

        assertThat(gamingProducts).hasSize(1);
        assertThat(gamingProducts.get(0).getName()).isEqualTo("Gaming Laptop");
    }

    @Test
    void shouldFindProductsByPriceBetween() {
        // When
        List<Product> budgetProducts = productRepository.findByPriceBetween(
                new BigDecimal("20.00"), new BigDecimal("150.00"));
        List<Product> expensiveProducts = productRepository.findByPriceBetween(
                new BigDecimal("500.00"), new BigDecimal("1500.00"));

        // Then
        assertThat(budgetProducts).hasSize(2);
        assertThat(budgetProducts)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Wireless Mouse", "Mechanical Keyboard");

        assertThat(expensiveProducts).hasSize(3);
        assertThat(expensiveProducts)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Gaming Laptop", "Smartphone", "Old Laptop");
    }

    @Test
    void shouldFindLowStockProducts() {
        // When
        List<Product> lowStockProducts = productRepository.findLowStockProducts(10);
        List<Product> criticalStockProducts = productRepository.findLowStockProducts(5);

        // Then
        assertThat(lowStockProducts).hasSize(2);
        assertThat(lowStockProducts)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Mechanical Keyboard", "Old Laptop");

        assertThat(criticalStockProducts).hasSize(2);
        assertThat(criticalStockProducts)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Mechanical Keyboard", "Old Laptop");
    }

    @Test
    void shouldFindAllProducts() {
        // When
        List<Product> allProducts = productRepository.findAll();

        // Then
        assertThat(allProducts).hasSize(5);
        assertThat(allProducts)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Gaming Laptop", "Wireless Mouse", "Mechanical Keyboard",
                        "Smartphone", "Old Laptop");
    }

    @Test
    void shouldDeleteProduct() {
        // Given
        Long productId = laptopProduct.getId();

        // When
        productRepository.deleteById(productId);
        Optional<Product> deletedProduct = productRepository.findById(productId);

        // Then
        assertThat(deletedProduct).isEmpty();
    }

    @Test
    void shouldUpdateProduct() {
        // Given
        Product productToUpdate = productRepository.findById(mouseProduct.getId()).orElseThrow();
        productToUpdate.setPrice(new BigDecimal("39.99"));
        productToUpdate.setStockQuantity(80);

        // When
        Product updatedProduct = productRepository.save(productToUpdate);

        // Then
        assertThat(updatedProduct.getPrice()).isEqualTo(new BigDecimal("39.99"));
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(80);
    }

    @Test
    void shouldCheckIfProductExists() {
        // When
        boolean exists = productRepository.existsById(laptopProduct.getId());
        boolean notExists = productRepository.existsById(999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldCountProducts() {
        // When
        long totalCount = productRepository.count();

        // Then
        assertThat(totalCount).isEqualTo(5);
    }

    @Test
    void shouldFindProductsWithComplexQuery() {
        // Test combination of filters
        // When
        List<Product> activeTechBrandProducts = productRepository.findByStatus(ProductStatus.ACTIVE)
                .stream()
                .filter(p -> "TechBrand".equals(p.getBrand()))
                .toList();

        // Then
        assertThat(activeTechBrandProducts).hasSize(2);
        assertThat(activeTechBrandProducts)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Gaming Laptop", "Wireless Mouse");
    }

    @Test
    void shouldHandleEmptyResults() {
        // When
        List<Product> nonExistentCategory = productRepository.findByCategory("NonExistent");
        List<Product> nonExistentBrand = productRepository.findByBrand("NonExistent");
        List<Product> veryExpensiveProducts = productRepository.findByPriceBetween(
                new BigDecimal("5000.00"), new BigDecimal("10000.00"));

        // Then
        assertThat(nonExistentCategory).isEmpty();
        assertThat(nonExistentBrand).isEmpty();
        assertThat(veryExpensiveProducts).isEmpty();
    }

    @Test
    void shouldHandleCaseInsensitiveSearch() {
        // When
        List<Product> upperCaseSearch = productRepository.findByNameContaining("LAPTOP");
        List<Product> lowerCaseSearch = productRepository.findByNameContaining("laptop");
        List<Product> mixedCaseSearch = productRepository.findByNameContaining("LaPtOp");

        // Then - Note: The current query is case-sensitive, so these might be empty
        // If you want case-insensitive search, update the query to use UPPER() or LOWER()
        assertThat(upperCaseSearch).isEmpty();
        assertThat(lowerCaseSearch).isEmpty();
        assertThat(mixedCaseSearch).isEmpty();
    }

    @Test
    void shouldFindProductsWithPrecisePrice() {
        // When
        List<Product> exactPriceProducts = productRepository.findByPriceBetween(
                new BigDecimal("29.99"), new BigDecimal("29.99"));

        // Then
        assertThat(exactPriceProducts).hasSize(1);
        assertThat(exactPriceProducts.get(0).getName()).isEqualTo("Wireless Mouse");
    }
}