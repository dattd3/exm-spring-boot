package com.example.exm.repository;

import com.example.exm.entity.Order;
import com.example.exm.entity.OrderStatus;
import com.example.exm.entity.User;
import com.example.exm.entity.UserStatus;
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
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User activeUser1;
    private User activeUser2;
    private User inactiveUser;
    private User userWithOrders;
    private User userWithRecentOrders;
    private User userWithOldOrders;

    @BeforeEach
    void setUp() {
        // Create test users
        activeUser1 = new User();
        activeUser1.setFirstName("John");
        activeUser1.setLastName("Doe");
        activeUser1.setEmail("john.doe@example.com");
        activeUser1.setPhoneNumber("+1234567890");
        activeUser1.setAddress("123 Main St, City, State 12345");
        activeUser1.setStatus(UserStatus.ACTIVE);

        activeUser2 = new User();
        activeUser2.setFirstName("Jane");
        activeUser2.setLastName("Smith");
        activeUser2.setEmail("jane.smith@example.com");
        activeUser2.setPhoneNumber("+0987654321");
        activeUser2.setAddress("456 Oak Ave, City, State 67890");
        activeUser2.setStatus(UserStatus.ACTIVE);

        inactiveUser = new User();
        inactiveUser.setFirstName("Bob");
        inactiveUser.setLastName("Johnson");
        inactiveUser.setEmail("bob.johnson@example.com");
        inactiveUser.setPhoneNumber("+1122334455");
        inactiveUser.setAddress("789 Pine Rd, City, State 11111");
        inactiveUser.setStatus(UserStatus.INACTIVE);

        userWithOrders = new User();
        userWithOrders.setFirstName("Alice");
        userWithOrders.setLastName("Wilson");
        userWithOrders.setEmail("alice.wilson@example.com");
        userWithOrders.setPhoneNumber("+5566778899");
        userWithOrders.setAddress("321 Cedar Ln, City, State 22222");
        userWithOrders.setStatus(UserStatus.ACTIVE);

        userWithRecentOrders = new User();
        userWithRecentOrders.setFirstName("Charlie");
        userWithRecentOrders.setLastName("Brown");
        userWithRecentOrders.setEmail("charlie.brown@example.com");
        userWithRecentOrders.setPhoneNumber("+9988776655");
        userWithRecentOrders.setAddress("654 Maple Ave, City, State 33333");
        userWithRecentOrders.setStatus(UserStatus.ACTIVE);

        userWithOldOrders = new User();
        userWithOldOrders.setFirstName("David");
        userWithOldOrders.setLastName("Miller");
        userWithOldOrders.setEmail("david.miller@example.com");
        userWithOldOrders.setPhoneNumber("+1133557799");
        userWithOldOrders.setAddress("987 Birch St, City, State 44444");
        userWithOldOrders.setStatus(UserStatus.ACTIVE);

        // Persist users
        entityManager.persistAndFlush(activeUser1);
        entityManager.persistAndFlush(activeUser2);
        entityManager.persistAndFlush(inactiveUser);
        entityManager.persistAndFlush(userWithOrders);
        entityManager.persistAndFlush(userWithRecentOrders);
        entityManager.persistAndFlush(userWithOldOrders);

        // Create orders for testing custom queries
        createTestOrders();

        entityManager.clear();
    }

    private void createTestOrders() {
        // Active orders for userWithOrders
        Order activeOrder1 = new Order();
        activeOrder1.setUser(userWithOrders);
        activeOrder1.setOrderNumber("ORD-001");
        activeOrder1.setShippingAddress("Test Address 1");
        activeOrder1.setStatus(OrderStatus.PENDING);
        activeOrder1.setOrderDate(LocalDateTime.now().minusDays(1));
        activeOrder1.setTotalAmount(new BigDecimal("100.00"));

        Order activeOrder2 = new Order();
        activeOrder2.setUser(userWithOrders);
        activeOrder2.setOrderNumber("ORD-002");
        activeOrder2.setShippingAddress("Test Address 2");
        activeOrder2.setStatus(OrderStatus.CONFIRMED);
        activeOrder2.setOrderDate(LocalDateTime.now().minusDays(2));
        activeOrder2.setTotalAmount(new BigDecimal("200.00"));

        // Recent orders for userWithRecentOrders
        Order recentOrder1 = new Order();
        recentOrder1.setUser(userWithRecentOrders);
        recentOrder1.setOrderNumber("ORD-003");
        recentOrder1.setShippingAddress("Test Address 3");
        recentOrder1.setStatus(OrderStatus.DELIVERED);
        recentOrder1.setOrderDate(LocalDateTime.now().minusDays(3));
        recentOrder1.setTotalAmount(new BigDecimal("150.00"));

        Order recentOrder2 = new Order();
        recentOrder2.setUser(userWithRecentOrders);
        recentOrder2.setOrderNumber("ORD-004");
        recentOrder2.setShippingAddress("Test Address 4");
        recentOrder2.setStatus(OrderStatus.PENDING);
        recentOrder2.setOrderDate(LocalDateTime.now().minusDays(5));
        recentOrder2.setTotalAmount(new BigDecimal("75.00"));

        // Old orders for userWithOldOrders
        Order oldOrder1 = new Order();
        oldOrder1.setUser(userWithOldOrders);
        oldOrder1.setOrderNumber("ORD-005");
        oldOrder1.setShippingAddress("Test Address 5");
        oldOrder1.setStatus(OrderStatus.DELIVERED);
        oldOrder1.setOrderDate(LocalDateTime.now().minusDays(40));
        oldOrder1.setTotalAmount(new BigDecimal("300.00"));

        Order oldOrder2 = new Order();
        oldOrder2.setUser(userWithOldOrders);
        oldOrder2.setOrderNumber("ORD-006");
        oldOrder2.setShippingAddress("Test Address 6");
        oldOrder2.setStatus(OrderStatus.DELIVERED);
        oldOrder2.setOrderDate(LocalDateTime.now().minusDays(50));
        oldOrder2.setTotalAmount(new BigDecimal("125.00"));

        // Additional order for userWithRecentOrders to test top customers
        Order extraOrder = new Order();
        extraOrder.setUser(userWithRecentOrders);
        extraOrder.setOrderNumber("ORD-007");
        extraOrder.setShippingAddress("Test Address 7");
        extraOrder.setStatus(OrderStatus.DELIVERED);
        extraOrder.setOrderDate(LocalDateTime.now().minusDays(10));
        extraOrder.setTotalAmount(new BigDecimal("50.00"));

        entityManager.persistAndFlush(activeOrder1);
        entityManager.persistAndFlush(activeOrder2);
        entityManager.persistAndFlush(recentOrder1);
        entityManager.persistAndFlush(recentOrder2);
        entityManager.persistAndFlush(oldOrder1);
        entityManager.persistAndFlush(oldOrder2);
        entityManager.persistAndFlush(extraOrder);
    }

    @Test
    void shouldSaveAndFindUser() {
        // Given
        User newUser = new User();
        newUser.setFirstName("Test");
        newUser.setLastName("User");
        newUser.setEmail("test.user@example.com");
        newUser.setPhoneNumber("+1234567890");
        newUser.setAddress("Test Address");
        newUser.setStatus(UserStatus.ACTIVE);

        // When
        User savedUser = userRepository.save(newUser);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFirstName()).isEqualTo("Test");
        assertThat(foundUser.get().getLastName()).isEqualTo("User");
        assertThat(foundUser.get().getEmail()).isEqualTo("test.user@example.com");
        assertThat(foundUser.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void shouldFindUserByEmail() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");
        Optional<User> notFoundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFirstName()).isEqualTo("John");
        assertThat(foundUser.get().getLastName()).isEqualTo("Doe");

        assertThat(notFoundUser).isEmpty();
    }

    @Test
    void shouldFindUsersByStatus() {
        // When
        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
        List<User> inactiveUsers = userRepository.findByStatus(UserStatus.INACTIVE);

        // Then
        assertThat(activeUsers).hasSize(5);
        assertThat(activeUsers)
                .extracting(User::getFirstName)
                .containsExactlyInAnyOrder("John", "Jane", "Alice", "Charlie", "David");

        assertThat(inactiveUsers).hasSize(1);
        assertThat(inactiveUsers.get(0).getFirstName()).isEqualTo("Bob");
    }

    @Test
    void shouldFindUsersByStatusWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 3);

        // When
        Page<User> activeUsersPage = userRepository.findByStatus(UserStatus.ACTIVE, pageable);

        // Then
        assertThat(activeUsersPage.getContent()).hasSize(3);
        assertThat(activeUsersPage.getTotalElements()).isEqualTo(5);
        assertThat(activeUsersPage.getTotalPages()).isEqualTo(2);
        assertThat(activeUsersPage.isFirst()).isTrue();
        assertThat(activeUsersPage.hasNext()).isTrue();
    }

    @Test
    void shouldFindUsersByNameContaining() {
        // When
        List<User> johnUsers = userRepository.findByNameContaining("John");
        List<User> smithUsers = userRepository.findByNameContaining("Smith");
        List<User> jUsers = userRepository.findByNameContaining("J");

        // Then
        assertThat(johnUsers).hasSize(2); // John Doe and Bob Johnson
        assertThat(johnUsers)
                .extracting(User::getFirstName)
                .containsExactlyInAnyOrder("John", "Bob");

        assertThat(smithUsers).hasSize(1);
        assertThat(smithUsers.get(0).getFirstName()).isEqualTo("Jane");

        assertThat(jUsers).hasSize(3); // John, Jane, Bob Johnson
        assertThat(jUsers)
                .extracting(User::getFirstName)
                .containsExactlyInAnyOrder("John", "Jane", "Bob");
    }

    @Test
    void shouldCheckIfEmailExists() {
        // When
        boolean existingEmail = userRepository.existsByEmail("john.doe@example.com");
        boolean nonExistingEmail = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(existingEmail).isTrue();
        assertThat(nonExistingEmail).isFalse();
    }

    @Test
    void shouldFindUsersWithActiveOrders() {
        // When
        List<User> usersWithActiveOrders = userRepository.findUsersWithActiveOrders();

        // Then
        assertThat(usersWithActiveOrders).hasSize(2); // userWithOrders and userWithRecentOrders
        assertThat(usersWithActiveOrders)
                .extracting(User::getFirstName)
                .containsExactlyInAnyOrder("Alice", "Charlie");
    }

    @Test
    void shouldFindUsersWithRecentActivity() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> recentActiveUsers = userRepository.findUsersWithRecentActivity(30, pageable);

        // Then
        assertThat(recentActiveUsers.getContent()).hasSize(2); // userWithOrders and userWithRecentOrders
        assertThat(recentActiveUsers.getTotalElements()).isEqualTo(2);
        assertThat(recentActiveUsers.getContent())
                .extracting(User::getFirstName)
                .containsExactlyInAnyOrder("Alice", "Charlie");
    }

    @Test
    void shouldFindUsersWithRecentActivityWithDifferentTimeframes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> last7Days = userRepository.findUsersWithRecentActivity(7, pageable);
        Page<User> last60Days = userRepository.findUsersWithRecentActivity(60, pageable);

        // Then
        assertThat(last7Days.getContent()).hasSize(2); // Recent activity users
        assertThat(last60Days.getContent()).hasSize(3); // All users with orders
        assertThat(last60Days.getContent())
                .extracting(User::getFirstName)
                .containsExactlyInAnyOrder("Alice", "Charlie", "David");
    }

    @Test
    void shouldFindTopCustomersByOrderCount() {
        // When
        List<User> topCustomers = userRepository.findTopCustomersByOrderCount(3);

        // Then
        assertThat(topCustomers).hasSize(6); // All users including those with 0 orders
        // First user should be the one with most orders (Charlie with 3 orders)
        assertThat(topCustomers.get(0).getFirstName()).isEqualTo("Charlie");
        // Second should be Alice with 2 orders, third should be David with 2 orders
    }

    @Test
    void shouldFindTopCustomersByOrderCountWithLimit() {
        // When
        List<User> topTwoCustomers = userRepository.findTopCustomersByOrderCount(2);

        // Then
        assertThat(topTwoCustomers).hasSize(2);
        assertThat(topTwoCustomers.get(0).getFirstName()).isEqualTo("Charlie");
    }

    @Test
    void shouldDeleteUser() {
        // Given
        Long userId = activeUser1.getId();

        // When
        userRepository.deleteById(userId);
        Optional<User> deletedUser = userRepository.findById(userId);

        // Then
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void shouldUpdateUser() {
        // Given
        User userToUpdate = userRepository.findById(activeUser1.getId()).orElseThrow();
        userToUpdate.setPhoneNumber("+9999999999");
        userToUpdate.setAddress("Updated Address");

        // When
        User updatedUser = userRepository.save(userToUpdate);

        // Then
        assertThat(updatedUser.getPhoneNumber()).isEqualTo("+9999999999");
        assertThat(updatedUser.getAddress()).isEqualTo("Updated Address");
    }

    @Test
    void shouldFindAllUsers() {
        // When
        List<User> allUsers = userRepository.findAll();

        // Then
        assertThat(allUsers).hasSize(6);
        assertThat(allUsers)
                .extracting(User::getFirstName)
                .containsExactlyInAnyOrder("John", "Jane", "Bob", "Alice", "Charlie", "David");
    }

    @Test
    void shouldCountUsers() {
        // When
        long totalUsers = userRepository.count();

        // Then
        assertThat(totalUsers).isEqualTo(6);
    }

    @Test
    void shouldCheckIfUserExists() {
        // When
        boolean exists = userRepository.existsById(activeUser1.getId());
        boolean notExists = userRepository.existsById(999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldHandleEmptyResults() {
        // When
        List<User> nonExistentStatus = userRepository.findByStatus(UserStatus.SUSPENDED);
        List<User> nonExistentName = userRepository.findByNameContaining("XYZ");
        Optional<User> nonExistentEmail = userRepository.findByEmail("nobody@nowhere.com");

        // Then
        assertThat(nonExistentStatus).isEmpty();
        assertThat(nonExistentName).isEmpty();
        assertThat(nonExistentEmail).isEmpty();
    }

    @Test
    void shouldFindUsersWithNoOrders() {
        // When
        List<User> topCustomers = userRepository.findTopCustomersByOrderCount(10);
        List<User> usersWithActiveOrders = userRepository.findUsersWithActiveOrders();

        // Then
        // Users without orders should be at the end of the top customers list
        List<User> usersWithoutActiveOrders = topCustomers.stream()
                .filter(user -> !usersWithActiveOrders.contains(user))
                .toList();

        assertThat(usersWithoutActiveOrders).isNotEmpty();
        assertThat(usersWithoutActiveOrders)
                .extracting(User::getFirstName)
                .contains("John", "Jane", "Bob");
    }

    @Test
    void shouldFindUsersWithRecentActivityPagination() {
        // Given
        Pageable firstPage = PageRequest.of(0, 1);
        Pageable secondPage = PageRequest.of(1, 1);

        // When
        Page<User> firstPageResult = userRepository.findUsersWithRecentActivity(30, firstPage);
        Page<User> secondPageResult = userRepository.findUsersWithRecentActivity(30, secondPage);

        // Then
        assertThat(firstPageResult.getContent()).hasSize(1);
        assertThat(secondPageResult.getContent()).hasSize(1);
        assertThat(firstPageResult.getTotalElements()).isEqualTo(2);
        assertThat(secondPageResult.getTotalElements()).isEqualTo(2);
        assertThat(firstPageResult.hasNext()).isTrue();
        assertThat(secondPageResult.hasNext()).isFalse();
    }
}