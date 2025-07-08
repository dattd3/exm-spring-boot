-- Insert sample users
INSERT INTO users (id, first_name, last_name, email, phone_number, address, status, created_at, updated_at)
VALUES (1, 'John', 'Doe', 'john.doe@example.com', '+1234567890', '123 Main St, New York, NY 10001', 'ACTIVE',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 'Jane', 'Smith', 'jane.smith@example.com', '+1234567891', '456 Oak Ave, Los Angeles, CA 90001', 'ACTIVE',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 'Bob', 'Johnson', 'bob.johnson@example.com', '+1234567892', '789 Pine Rd, Chicago, IL 60001', 'ACTIVE',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, 'Alice', 'Williams', 'alice.williams@example.com', '+1234567893', '321 Elm St, Houston, TX 77001',
        'INACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, 'Charlie', 'Brown', 'charlie.brown@example.com', '+1234567894', '654 Maple Dr, Phoenix, AZ 85001', 'ACTIVE',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample products
INSERT INTO products (id, name, description, price, stock_quantity, category, brand, image_url, status, created_at,
                      updated_at)
VALUES (1, 'Laptop Computer', 'High-performance laptop for professional use', 1299.99, 25, 'Electronics', 'TechBrand',
        'https://example.com/laptop.jpg', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 'Wireless Mouse', 'Ergonomic wireless mouse with precision tracking', 29.99, 50, 'Electronics', 'TechBrand',
        'https://example.com/mouse.jpg', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 'Office Desk', 'Modern office desk with built-in storage', 299.99, 15, 'Furniture', 'OfficeMax',
        'https://example.com/desk.jpg', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, 'Coffee Maker', 'Automatic coffee maker with programmable timer', 89.99, 30, 'Appliances', 'BrewMaster',
        'https://example.com/coffee.jpg', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, 'Running Shoes', 'Lightweight running shoes for athletes', 129.99, 8, 'Sports', 'RunFast',
        'https://example.com/shoes.jpg', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (6, 'Smartphone', 'Latest smartphone with advanced camera', 699.99, 20, 'Electronics', 'PhoneBrand',
        'https://example.com/phone.jpg', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (7, 'Book: Java Programming', 'Comprehensive guide to Java programming', 49.99, 100, 'Books', 'TechBooks',
        'https://example.com/book.jpg', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (8, 'Gaming Headset', 'High-quality gaming headset with surround sound', 159.99, 12, 'Electronics', 'GameGear',
        'https://example.com/headset.jpg', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (9, 'Water Bottle', 'Insulated stainless steel water bottle', 24.99, 75, 'Sports', 'HydroMax',
        'https://example.com/bottle.jpg', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (10, 'Tablet', '10-inch tablet for entertainment and productivity', 449.99, 18, 'Electronics', 'TabletCorp',
        'https://example.com/tablet.jpg', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample orders
INSERT INTO orders (id, order_number, user_id, total_amount, status, order_date, shipping_address, notes, created_at,
                    updated_at)
VALUES (1, 'ORD20240101120000001', 1, 1329.98, 'DELIVERED', '2024-01-15 10:30:00', '123 Main St, New York, NY 10001',
        'Please deliver during business hours', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 'ORD20240102140000002', 2, 89.99, 'CONFIRMED', '2024-01-20 14:15:00', '456 Oak Ave, Los Angeles, CA 90001',
        'Leave at front door if no answer', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 'ORD20240103160000003', 3, 569.97, 'PROCESSING', '2024-01-25 16:45:00', '789 Pine Rd, Chicago, IL 60001', '',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, 'ORD20240104180000004', 1, 159.99, 'SHIPPED', '2024-02-01 18:20:00', '123 Main St, New York, NY 10001',
        'Gift wrapping requested', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, 'ORD20240105200000005', 5, 24.99, 'PENDING', '2024-02-05 20:10:00', '654 Maple Dr, Phoenix, AZ 85001', '',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample order items
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, total_price, created_at, updated_at)
VALUES
-- Order 1 items
(1, 1, 1, 1, 1299.99, 1299.99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 2, 1, 29.99, 29.99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Order 2 items
(3, 2, 4, 1, 89.99, 89.99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Order 3 items
(4, 3, 3, 1, 299.99, 299.99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 3, 5, 2, 129.99, 259.98, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 3, 9, 1, 24.99, 24.99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Order 4 items
(7, 4, 8, 1, 159.99, 159.99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Order 5 items
(8, 5, 9, 1, 24.99, 24.99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Update sequences for H2 database
ALTER SEQUENCE users_seq RESTART WITH 6;
ALTER SEQUENCE products_seq RESTART WITH 11;
ALTER SEQUENCE orders_seq RESTART WITH 6;
ALTER SEQUENCE order_items_seq RESTART WITH 9;