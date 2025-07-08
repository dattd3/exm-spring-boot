package com.example.exm.repository;

import com.example.exm.entity.Order;
import com.example.exm.entity.OrderItem;
import com.example.exm.entity.OrderStatus;
import com.example.exm.entity.Product;
import com.example.exm.entity.ProductStatus;
import com.example.exm.entity.User;
import com.example.exm.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private Product testProduct1;
    private Product testProduct2;
    private User testUser1;
    private User testUser2;
    private Order testOrder1;
    private Order testOrder2;
    private Order testOrder3;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = new User();
        testUser1.setFirstName("John");
        testUser1.setLastName("Doe");
        testUser1.setEmail("john.doe@example.com");
        testUser1.setPhoneNumber("+1234567890");
        testUser1.setAddress("123 Main St, City, State 12345");
        testUser1.setStatus(UserStatus.ACTIVE);

        testUser2 = new User();
        testUser2.setFirstName("Jane");
        testUser2.setLastName("Smith");
        testUser2.setEmail("jane.smith@example.com");
        testUser2.setPhoneNumber("+0987654321");
        testUser2.setAddress("456 Oak Ave, City, State 67890");
        testUser2.setStatus(UserStatus.ACTIVE);

        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);

        // Create test products
        testProduct1 = new Product();
        testProduct1.setName("Laptop");
        testProduct1.setDescription("Gaming Laptop");
        testProduct1.setPrice(new BigDecimal("999.99"));
        testProduct1.setStockQuantity(10);
        testProduct1.setCategory("Electronics");
        testProduct1.setBrand("TestBrand");
        testProduct1.setStatus(ProductStatus.ACTIVE);

        testProduct2 = new Product();
        testProduct2.setName("Mouse");
        testProduct2.setDescription("Wireless Mouse");
        testProduct2.setPrice(new BigDecimal("29.99"));
        testProduct2.setStockQuantity(50);
        testProduct2.setCategory("Electronics");
        testProduct2.setBrand("TestBrand");
        testProduct2.setStatus(ProductStatus.ACTIVE);

        entityManager.persistAndFlush(testProduct1);
        entityManager.persistAndFlush(testProduct2);

        // Create test orders
        testOrder1 = new Order();
        testOrder1.setUser(testUser1);
        testOrder1.setShippingAddress("123 Main St, City, State 12345");
        testOrder1.setStatus(OrderStatus.PENDING);
        testOrder1.setOrderDate(LocalDateTime.now().minusDays(2));
        testOrder1.setTotalAmount(new BigDecimal("1029.98"));
        testOrder1.setOrderNumber("ORD-001");

        testOrder2 = new Order();
        testOrder2.setUser(testUser2);
        testOrder2.setShippingAddress("456 Oak Ave, City, State 67890");
        testOrder2.setStatus(OrderStatus.CONFIRMED);
        testOrder2.setOrderDate(LocalDateTime.now().minusDays(1));
        testOrder2.setTotalAmount(new BigDecimal("999.99"));
        testOrder2.setOrderNumber("ORD-002");

        testOrder3 = new Order();
        testOrder3.setUser(testUser1);
        testOrder3.setShippingAddress("789 Pine Rd, City, State 11111");
        testOrder3.setStatus(OrderStatus.SHIPPED);
        testOrder3.setOrderDate(LocalDateTime.now().minusDays(5));
        testOrder3.setTotalAmount(new BigDecimal("29.99"));
        testOrder3.setOrderNumber("ORD-003");

        entityManager.persistAndFlush(testOrder1);
        entityManager.persistAndFlush(testOrder2);
        entityManager.persistAndFlush(testOrder3);

        // Create order items
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setOrder(testOrder1);
        orderItem1.setProduct(testProduct1);
        orderItem1.setQuantity(1);

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setOrder(testOrder1);
        orderItem2.setProduct(testProduct2);
        orderItem2.setQuantity(1);

        OrderItem orderItem3 = new OrderItem();
        orderItem3.setOrder(testOrder2);
        orderItem3.setProduct(testProduct1);
        orderItem3.setQuantity(1);

        OrderItem orderItem4 = new OrderItem();
        orderItem4.setOrder(testOrder3);
        orderItem4.setProduct(testProduct2);
        orderItem4.setQuantity(1);

        entityManager.persistAndFlush(orderItem1);
        entityManager.persistAndFlush(orderItem2);
        entityManager.persistAndFlush(orderItem3);
        entityManager.persistAndFlush(orderItem4);

        entityManager.clear();
    }

    @Test
    void shouldSaveAndFindOrder() {
        // Given
        Order newOrder = new Order();
        newOrder.setUser(testUser1);
        newOrder.setShippingAddress("Test Address");
        newOrder.setStatus(OrderStatus.PENDING);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setTotalAmount(new BigDecimal("100.00"));
        newOrder.setOrderNumber("ORD-TEST");

        // When
        Order savedOrder = orderRepository.save(newOrder);
        Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());

        // Then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getUser().getFirstName()).isEqualTo("John");
        assertThat(foundOrder.get().getUser().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(foundOrder.get().getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(foundOrder.get().getTotalAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(foundOrder.get().getOrderNumber()).isEqualTo("ORD-TEST");
    }

    @Test
    void shouldFindOrdersByStatus() {
        // When
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        List<Order> confirmedOrders = orderRepository.findByStatus(OrderStatus.CONFIRMED);
        List<Order> shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);

        // Then
        assertThat(pendingOrders).hasSize(1);
        assertThat(pendingOrders.get(0).getUser().getFirstName()).isEqualTo("John");

        assertThat(confirmedOrders).hasSize(1);
        assertThat(confirmedOrders.get(0).getUser().getFirstName()).isEqualTo("Jane");

        assertThat(shippedOrders).hasSize(1);
        assertThat(shippedOrders.get(0).getUser().getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldFindOrdersByUserId() {
        // When
        List<Order> user1Orders = orderRepository.findByUser(testUser1);
        List<Order> user2Orders = orderRepository.findByUser(testUser2);

        // Then
        assertThat(user1Orders).hasSize(2); // testOrder1 and testOrder3
        assertThat(user2Orders).hasSize(1); // testOrder2
        assertThat(user2Orders.get(0).getUser().getFirstName()).isEqualTo("Jane");
    }

    @Test
    void shouldFindOrdersByUserEmail() {
        // When
        List<Order> orders = orderRepository.findByUserEmail("john.doe@example.com");

        // Then
        assertThat(orders).hasSize(2); // testOrder1 and testOrder3
        assertThat(orders.get(0).getUser().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldFindOrdersByDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(3);
        LocalDateTime endDate = LocalDateTime.now();

        // When
        List<Order> ordersInRange = orderRepository.findByOrderDateBetween(startDate, endDate);

        // Then
        assertThat(ordersInRange).hasSize(2); // Orders from testOrder1 and testOrder2
        assertThat(ordersInRange)
            .extracting(order -> order.getUser().getFirstName())
            .containsExactlyInAnyOrder("John", "Jane");
    }

    @Test
    void shouldFindRecentOrders() {
        // Given
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        // When
        List<Order> recentOrders = orderRepository.findByOrderDateAfter(threeDaysAgo);

        // Then
        assertThat(recentOrders).hasSize(2);
        assertThat(recentOrders)
            .extracting(order -> order.getUser().getFirstName())
            .containsExactlyInAnyOrder("John", "Jane");
    }

    @Test
    void shouldCountOrdersByStatus() {
        // When
        long pendingCount = orderRepository.countByStatus(OrderStatus.PENDING);
        long confirmedCount = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long shippedCount = orderRepository.countByStatus(OrderStatus.SHIPPED);
        long deliveredCount = orderRepository.countByStatus(OrderStatus.DELIVERED);

        // Then
        assertThat(pendingCount).isEqualTo(1);
        assertThat(confirmedCount).isEqualTo(1);
        assertThat(shippedCount).isEqualTo(1);
        assertThat(deliveredCount).isEqualTo(0);
    }

    @Test
    void shouldFindOrdersWithItems() {
        // When
        List<Order> orders = orderRepository.findAll();

        // Then
        assertThat(orders).hasSize(3);
        
        // Verify that orders have items
        orders.forEach(order -> {
            entityManager.refresh(order);
            assertThat(order.getOrderItems()).isNotEmpty();
        });
    }

    @Test
    void shouldCalculateOrderTotal() {
        // When
        Optional<Order> order = orderRepository.findById(testOrder1.getId());

        // Then
        assertThat(order).isPresent();
        assertThat(order.get().getTotalAmount()).isEqualTo(new BigDecimal("1029.98"));
    }

    @Test
    void shouldDeleteOrder() {
        // Given
        Long orderId = testOrder1.getId();

        // When
        orderRepository.deleteById(orderId);
        Optional<Order> deletedOrder = orderRepository.findById(orderId);

        // Then
        assertThat(deletedOrder).isEmpty();
    }

    @Test
    void shouldFindOrdersByTotalAmountGreaterThan() {
        // Given
        BigDecimal threshold = new BigDecimal("100.00");

        // When
        List<Order> expensiveOrders = orderRepository.findByTotalAmountGreaterThan(threshold);

        // Then
        assertThat(expensiveOrders).hasSize(2); // testOrder1 and testOrder2
        assertThat(expensiveOrders)
            .extracting(order -> order.getUser().getFirstName())
            .containsExactlyInAnyOrder("John", "Jane");
    }

    @Test
    void shouldFindOrdersByStatusAndDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(6);
        LocalDateTime endDate = LocalDateTime.now();

        // When
        List<Order> pendingOrdersInRange = orderRepository.findByStatusAndOrderDateBetween(
            OrderStatus.PENDING, startDate, endDate);

        // Then
        assertThat(pendingOrdersInRange).hasSize(1);
        assertThat(pendingOrdersInRange.get(0).getUser().getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldFindAllOrdersOrderedByDateDesc() {
        // When
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();

        // Then
        assertThat(orders).hasSize(3);
        // Most recent order should be first (testOrder2)
        assertThat(orders.get(0).getUser().getFirstName()).isEqualTo("Jane");
        assertThat(orders.get(1).getUser().getFirstName()).isEqualTo("John");
        assertThat(orders.get(2).getUser().getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldFindOrderByOrderNumber() {
        // When
        Optional<Order> order = orderRepository.findByOrderNumber("ORD-001");

        // Then
        assertThat(order).isPresent();
        assertThat(order.get().getUser().getFirstName()).isEqualTo("John");
        assertThat(order.get().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void shouldCheckIfOrderExists() {
        // When
        boolean exists = orderRepository.existsById(testOrder1.getId());
        boolean notExists = orderRepository.existsById(999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldFindOrdersByUserAndStatus() {
        // When
        List<Order> userPendingOrders = orderRepository.findByUserAndStatus(testUser1, OrderStatus.PENDING);
        List<Order> userShippedOrders = orderRepository.findByUserAndStatus(testUser1, OrderStatus.SHIPPED);

        // Then
        assertThat(userPendingOrders).hasSize(1);
        assertThat(userShippedOrders).hasSize(1);
        assertThat(userPendingOrders.get(0).getOrderNumber()).isEqualTo("ORD-001");
        assertThat(userShippedOrders.get(0).getOrderNumber()).isEqualTo("ORD-003");
    }

    @Test
    void shouldFindOrdersByShippingAddressContaining() {
        // When
        List<Order> ordersOnMainSt = orderRepository.findByShippingAddressContainingIgnoreCase("main st");
        List<Order> ordersOnOakAve = orderRepository.findByShippingAddressContainingIgnoreCase("oak ave");

        // Then
        assertThat(ordersOnMainSt).hasSize(1);
        assertThat(ordersOnMainSt.get(0).getUser().getFirstName()).isEqualTo("John");
        
        assertThat(ordersOnOakAve).hasSize(1);
        assertThat(ordersOnOakAve.get(0).getUser().getFirstName()).isEqualTo("Jane");
    }
}