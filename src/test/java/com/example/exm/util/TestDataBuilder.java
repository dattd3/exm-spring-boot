package com.example.exm.util;

import com.example.exm.dto.request.CreateOrderRequest;
import com.example.exm.dto.request.CreateProductRequest;
import com.example.exm.dto.request.CreateUserRequest;
import com.example.exm.dto.request.UpdateUserRequest;
import com.example.exm.dto.response.OrderResponse;
import com.example.exm.dto.response.ProductResponse;
import com.example.exm.dto.response.UserResponse;
import com.example.exm.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for building test data objects using the Builder pattern.
 * Provides fluent API for creating entities, DTOs, and request objects for testing.
 */
public class TestDataBuilder {

    // ================== USER BUILDERS ==================

    public static class UserBuilder {
        private User user = new User();

        private UserBuilder() {
            // Default values
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setEmail("john.doe@example.com");
            user.setPhoneNumber("+1234567890");
            user.setAddress("123 Main St, City, State 12345");
            user.setStatus(UserStatus.ACTIVE);
        }

        public static UserBuilder aUser() {
            return new UserBuilder();
        }

        public UserBuilder withId(Long id) {
            user.setId(id);
            return this;
        }

        public UserBuilder withFirstName(String firstName) {
            user.setFirstName(firstName);
            return this;
        }

        public UserBuilder withLastName(String lastName) {
            user.setLastName(lastName);
            return this;
        }

        public UserBuilder withEmail(String email) {
            user.setEmail(email);
            return this;
        }

        public UserBuilder withPhoneNumber(String phoneNumber) {
            user.setPhoneNumber(phoneNumber);
            return this;
        }

        public UserBuilder withAddress(String address) {
            user.setAddress(address);
            return this;
        }

        public UserBuilder withStatus(UserStatus status) {
            user.setStatus(status);
            return this;
        }

        public UserBuilder withCreatedAt(LocalDateTime createdAt) {
            user.setCreatedAt(createdAt);
            return this;
        }

        public UserBuilder withUpdatedAt(LocalDateTime updatedAt) {
            user.setUpdatedAt(updatedAt);
            return this;
        }

        public UserBuilder withOrders(List<Order> orders) {
            user.setOrders(orders);
            return this;
        }

        public User build() {
            return user;
        }
    }

    // ================== PRODUCT BUILDERS ==================

    public static class ProductBuilder {
        private Product product = new Product();

        private ProductBuilder() {
            // Default values
            product.setName("Test Product");
            product.setDescription("Test product description");
            product.setPrice(new BigDecimal("99.99"));
            product.setStockQuantity(50);
            product.setCategory("Electronics");
            product.setBrand("TestBrand");
            product.setImageUrl("https://example.com/image.jpg");
            product.setStatus(ProductStatus.ACTIVE);
        }

        public static ProductBuilder aProduct() {
            return new ProductBuilder();
        }

        public ProductBuilder withId(Long id) {
            product.setId(id);
            return this;
        }

        public ProductBuilder withName(String name) {
            product.setName(name);
            return this;
        }

        public ProductBuilder withDescription(String description) {
            product.setDescription(description);
            return this;
        }

        public ProductBuilder withPrice(BigDecimal price) {
            product.setPrice(price);
            return this;
        }

        public ProductBuilder withPrice(String price) {
            product.setPrice(new BigDecimal(price));
            return this;
        }

        public ProductBuilder withStockQuantity(Integer stockQuantity) {
            product.setStockQuantity(stockQuantity);
            return this;
        }

        public ProductBuilder withCategory(String category) {
            product.setCategory(category);
            return this;
        }

        public ProductBuilder withBrand(String brand) {
            product.setBrand(brand);
            return this;
        }

        public ProductBuilder withImageUrl(String imageUrl) {
            product.setImageUrl(imageUrl);
            return this;
        }

        public ProductBuilder withStatus(ProductStatus status) {
            product.setStatus(status);
            return this;
        }

        public ProductBuilder withCreatedAt(LocalDateTime createdAt) {
            product.setCreatedAt(createdAt);
            return this;
        }

        public ProductBuilder withUpdatedAt(LocalDateTime updatedAt) {
            product.setUpdatedAt(updatedAt);
            return this;
        }

        public ProductBuilder asLowStock() {
            product.setStockQuantity(5);
            return this;
        }

        public ProductBuilder asOutOfStock() {
            product.setStockQuantity(0);
            return this;
        }

        public ProductBuilder asDiscontinued() {
            product.setStatus(ProductStatus.DISCONTINUED);
            return this;
        }

        public Product build() {
            return product;
        }
    }

    // ================== ORDER BUILDERS ==================

    public static class OrderBuilder {
        private Order order = new Order();

        private OrderBuilder() {
            // Default values
            order.setOrderNumber("ORD-" + System.currentTimeMillis());
            order.setShippingAddress("123 Test St, City, State 12345");
            order.setStatus(OrderStatus.PENDING);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(new BigDecimal("99.99"));
            order.setOrderItems(new ArrayList<>());
        }

        public static OrderBuilder anOrder() {
            return new OrderBuilder();
        }

        public OrderBuilder withId(Long id) {
            order.setId(id);
            return this;
        }

        public OrderBuilder withOrderNumber(String orderNumber) {
            order.setOrderNumber(orderNumber);
            return this;
        }

        public OrderBuilder withUser(User user) {
            order.setUser(user);
            return this;
        }

        public OrderBuilder withShippingAddress(String shippingAddress) {
            order.setShippingAddress(shippingAddress);
            return this;
        }

        public OrderBuilder withStatus(OrderStatus status) {
            order.setStatus(status);
            return this;
        }

        public OrderBuilder withOrderDate(LocalDateTime orderDate) {
            order.setOrderDate(orderDate);
            return this;
        }

        public OrderBuilder withTotalAmount(BigDecimal totalAmount) {
            order.setTotalAmount(totalAmount);
            return this;
        }

        public OrderBuilder withTotalAmount(String totalAmount) {
            order.setTotalAmount(new BigDecimal(totalAmount));
            return this;
        }

        public OrderBuilder withNotes(String notes) {
            order.setNotes(notes);
            return this;
        }

        public OrderBuilder withOrderItems(List<OrderItem> orderItems) {
            order.setOrderItems(orderItems);
            return this;
        }

        public OrderBuilder withCreatedAt(LocalDateTime createdAt) {
            order.setCreatedAt(createdAt);
            return this;
        }

        public OrderBuilder withUpdatedAt(LocalDateTime updatedAt) {
            order.setUpdatedAt(updatedAt);
            return this;
        }

        public OrderBuilder asPending() {
            order.setStatus(OrderStatus.PENDING);
            return this;
        }

        public OrderBuilder asConfirmed() {
            order.setStatus(OrderStatus.CONFIRMED);
            return this;
        }

        public OrderBuilder asShipped() {
            order.setStatus(OrderStatus.SHIPPED);
            return this;
        }

        public OrderBuilder asDelivered() {
            order.setStatus(OrderStatus.DELIVERED);
            return this;
        }

        public OrderBuilder asCancelled() {
            order.setStatus(OrderStatus.CANCELLED);
            return this;
        }

        public OrderBuilder fromDaysAgo(int days) {
            order.setOrderDate(LocalDateTime.now().minusDays(days));
            return this;
        }

        public Order build() {
            return order;
        }
    }

    // ================== ORDER ITEM BUILDERS ==================

    public static class OrderItemBuilder {
        private OrderItem orderItem = new OrderItem();

        private OrderItemBuilder() {
            // Default values
            orderItem.setQuantity(1);
        }

        public static OrderItemBuilder anOrderItem() {
            return new OrderItemBuilder();
        }

        public OrderItemBuilder withId(Long id) {
            orderItem.setId(id);
            return this;
        }

        public OrderItemBuilder withOrder(Order order) {
            orderItem.setOrder(order);
            return this;
        }

        public OrderItemBuilder withProduct(Product product) {
            orderItem.setProduct(product);
            return this;
        }

        public OrderItemBuilder withQuantity(Integer quantity) {
            orderItem.setQuantity(quantity);
            return this;
        }

        public OrderItem build() {
            return orderItem;
        }
    }

    // ================== REQUEST DTO BUILDERS ==================

    public static class CreateUserRequestBuilder {
        private CreateUserRequest request = new CreateUserRequest();

        private CreateUserRequestBuilder() {
            // Default values
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setEmail("john.doe@example.com");
            request.setPhoneNumber("+1234567890");
            request.setAddress("123 Main St, City, State 12345");
            request.setStatus(UserStatus.ACTIVE);
        }

        public static CreateUserRequestBuilder aCreateUserRequest() {
            return new CreateUserRequestBuilder();
        }

        public CreateUserRequestBuilder withFirstName(String firstName) {
            request.setFirstName(firstName);
            return this;
        }

        public CreateUserRequestBuilder withLastName(String lastName) {
            request.setLastName(lastName);
            return this;
        }

        public CreateUserRequestBuilder withEmail(String email) {
            request.setEmail(email);
            return this;
        }

        public CreateUserRequestBuilder withPhoneNumber(String phoneNumber) {
            request.setPhoneNumber(phoneNumber);
            return this;
        }

        public CreateUserRequestBuilder withAddress(String address) {
            request.setAddress(address);
            return this;
        }

        public CreateUserRequestBuilder withStatus(UserStatus status) {
            request.setStatus(status);
            return this;
        }

        public CreateUserRequest build() {
            return request;
        }
    }

    public static class CreateProductRequestBuilder {
        private CreateProductRequest request = new CreateProductRequest();

        private CreateProductRequestBuilder() {
            // Default values
            request.setName("Test Product");
            request.setDescription("Test product description");
            request.setPrice(new BigDecimal("99.99"));
            request.setStockQuantity(50);
            request.setCategory("Electronics");
            request.setBrand("TestBrand");
            request.setImageUrl("https://example.com/image.jpg");
            request.setStatus(ProductStatus.ACTIVE);
        }

        public static CreateProductRequestBuilder aCreateProductRequest() {
            return new CreateProductRequestBuilder();
        }

        public CreateProductRequestBuilder withName(String name) {
            request.setName(name);
            return this;
        }

        public CreateProductRequestBuilder withDescription(String description) {
            request.setDescription(description);
            return this;
        }

        public CreateProductRequestBuilder withPrice(BigDecimal price) {
            request.setPrice(price);
            return this;
        }

        public CreateProductRequestBuilder withPrice(String price) {
            request.setPrice(new BigDecimal(price));
            return this;
        }

        public CreateProductRequestBuilder withStockQuantity(Integer stockQuantity) {
            request.setStockQuantity(stockQuantity);
            return this;
        }

        public CreateProductRequestBuilder withCategory(String category) {
            request.setCategory(category);
            return this;
        }

        public CreateProductRequestBuilder withBrand(String brand) {
            request.setBrand(brand);
            return this;
        }

        public CreateProductRequestBuilder withImageUrl(String imageUrl) {
            request.setImageUrl(imageUrl);
            return this;
        }

        public CreateProductRequestBuilder withStatus(ProductStatus status) {
            request.setStatus(status);
            return this;
        }

        public CreateProductRequest build() {
            return request;
        }
    }

    public static class CreateOrderRequestBuilder {
        private CreateOrderRequest request = new CreateOrderRequest();

        private CreateOrderRequestBuilder() {
            // Default values
            request.setUserId(1L);
            request.setShippingAddress("123 Test St, City, State 12345");
            request.setNotes("Test order notes");
            request.setOrderItems(new ArrayList<>());
        }

        public static CreateOrderRequestBuilder aCreateOrderRequest() {
            return new CreateOrderRequestBuilder();
        }

        public CreateOrderRequestBuilder withUserId(Long userId) {
            request.setUserId(userId);
            return this;
        }

        public CreateOrderRequestBuilder withShippingAddress(String shippingAddress) {
            request.setShippingAddress(shippingAddress);
            return this;
        }

        public CreateOrderRequestBuilder withNotes(String notes) {
            request.setNotes(notes);
            return this;
        }

        public CreateOrderRequestBuilder withOrderItems(List<CreateOrderRequest.OrderItemRequest> orderItems) {
            request.setOrderItems(orderItems);
            return this;
        }

        public CreateOrderRequestBuilder withOrderItem(Long productId, Integer quantity) {
            CreateOrderRequest.OrderItemRequest orderItem = new CreateOrderRequest.OrderItemRequest();
            orderItem.setProductId(productId);
            orderItem.setQuantity(quantity);
            request.getOrderItems().add(orderItem);
            return this;
        }

        public CreateOrderRequest build() {
            return request;
        }
    }

    // ================== RESPONSE DTO BUILDERS ==================

    public static class UserResponseBuilder {
        private UserResponse response = new UserResponse();

        private UserResponseBuilder() {
            // Default values
            response.setId(1L);
            response.setFirstName("John");
            response.setLastName("Doe");
            response.setEmail("john.doe@example.com");
            response.setPhoneNumber("+1234567890");
            response.setAddress("123 Main St, City, State 12345");
            response.setStatus(UserStatus.ACTIVE);
            response.setCreatedAt(LocalDateTime.now());
            response.setUpdatedAt(LocalDateTime.now());
        }

        public static UserResponseBuilder aUserResponse() {
            return new UserResponseBuilder();
        }

        public UserResponseBuilder withId(Long id) {
            response.setId(id);
            return this;
        }

        public UserResponseBuilder withFirstName(String firstName) {
            response.setFirstName(firstName);
            return this;
        }

        public UserResponseBuilder withLastName(String lastName) {
            response.setLastName(lastName);
            return this;
        }

        public UserResponseBuilder withEmail(String email) {
            response.setEmail(email);
            return this;
        }

        public UserResponseBuilder withStatus(UserStatus status) {
            response.setStatus(status);
            return this;
        }

        public UserResponse build() {
            return response;
        }
    }

    public static class ProductResponseBuilder {
        private ProductResponse response = new ProductResponse();

        private ProductResponseBuilder() {
            // Default values
            response.setId(1L);
            response.setName("Test Product");
            response.setDescription("Test product description");
            response.setPrice(new BigDecimal("99.99"));
            response.setStockQuantity(50);
            response.setCategory("Electronics");
            response.setBrand("TestBrand");
            response.setStatus(ProductStatus.ACTIVE);
            response.setCreatedAt(LocalDateTime.now());
            response.setUpdatedAt(LocalDateTime.now());
        }

        public static ProductResponseBuilder aProductResponse() {
            return new ProductResponseBuilder();
        }

        public ProductResponseBuilder withId(Long id) {
            response.setId(id);
            return this;
        }

        public ProductResponseBuilder withName(String name) {
            response.setName(name);
            return this;
        }

        public ProductResponseBuilder withPrice(BigDecimal price) {
            response.setPrice(price);
            return this;
        }

        public ProductResponseBuilder withStockQuantity(Integer stockQuantity) {
            response.setStockQuantity(stockQuantity);
            return this;
        }

        public ProductResponseBuilder withStatus(ProductStatus status) {
            response.setStatus(status);
            return this;
        }

        public ProductResponse build() {
            return response;
        }
    }

    public static class OrderResponseBuilder {
        private OrderResponse response = new OrderResponse();

        private OrderResponseBuilder() {
            // Default values
            response.setId(1L);
            response.setOrderNumber("ORD-" + System.currentTimeMillis());
            response.setTotalAmount(new BigDecimal("99.99"));
            response.setStatus(OrderStatus.PENDING);
            response.setOrderDate(LocalDateTime.now());
            response.setShippingAddress("123 Test St, City, State 12345");
        }

        public static OrderResponseBuilder anOrderResponse() {
            return new OrderResponseBuilder();
        }

        public OrderResponseBuilder withId(Long id) {
            response.setId(id);
            return this;
        }

        public OrderResponseBuilder withOrderNumber(String orderNumber) {
            response.setOrderNumber(orderNumber);
            return this;
        }

        public OrderResponseBuilder withTotalAmount(BigDecimal totalAmount) {
            response.setTotalAmount(totalAmount);
            return this;
        }

        public OrderResponseBuilder withStatus(OrderStatus status) {
            response.setStatus(status);
            return this;
        }

        public OrderResponse build() {
            return response;
        }
    }

    // ================== CONVENIENCE FACTORY METHODS ==================

    /**
     * Creates a complete test scenario with related entities
     */
    public static class TestScenarioBuilder {

        public static TestScenario aCompleteOrderScenario() {
            User user = UserBuilder.aUser()
                    .withId(1L)
                    .withEmail("customer@example.com")
                    .build();

            Product product = ProductBuilder.aProduct()
                    .withId(1L)
                    .withName("Gaming Laptop")
                    .withPrice("1299.99")
                    .withStockQuantity(10)
                    .build();

            OrderItem orderItem = OrderItemBuilder.anOrderItem()
                    .withProduct(product)
                    .withQuantity(2)
                    .build();

            Order order = OrderBuilder.anOrder()
                    .withId(1L)
                    .withUser(user)
                    .withOrderItems(Arrays.asList(orderItem))
                    .withTotalAmount("2599.98")
                    .build();

            orderItem.setOrder(order);

            return new TestScenario(user, product, order, orderItem);
        }

        public static TestScenario aLowStockScenario() {
            Product product = ProductBuilder.aProduct()
                    .withId(1L)
                    .asLowStock()
                    .build();

            User user = UserBuilder.aUser()
                    .withId(1L)
                    .build();

            return new TestScenario(user, product, null, null);
        }

        public static TestScenario anInactiveUserScenario() {
            User user = UserBuilder.aUser()
                    .withId(1L)
                    .withStatus(UserStatus.INACTIVE)
                    .build();

            return new TestScenario(user, null, null, null);
        }
    }

    public static class TestScenario {
        private final User user;
        private final Product product;
        private final Order order;
        private final OrderItem orderItem;

        public TestScenario(User user, Product product, Order order, OrderItem orderItem) {
            this.user = user;
            this.product = product;
            this.order = order;
            this.orderItem = orderItem;
        }

        public User getUser() { return user; }
        public Product getProduct() { return product; }
        public Order getOrder() { return order; }
        public OrderItem getOrderItem() { return orderItem; }
    }
}