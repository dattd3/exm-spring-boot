select * from order_items;
select * from users;

-- Add version and audit columns to order_items table
ALTER TABLE users
    ADD COLUMN version BIGINT DEFAULT 0,
--    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN created_by VARCHAR(255),
--    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_by VARCHAR(255);

-- Add version and audit columns to order_items table
ALTER TABLE order_items
    ADD COLUMN version BIGINT DEFAULT 0,
--    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN created_by VARCHAR(255),
--    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_by VARCHAR(255);

-- Add version and audit columns to orders table
ALTER TABLE orders
    ADD COLUMN version BIGINT DEFAULT 0,
--    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN created_by VARCHAR(255),
--    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_by VARCHAR(255);

-- Add version and audit columns to products table
ALTER TABLE products
    add column in_stock BOOLEAN default true;
ADD COLUMN version BIGINT DEFAULT 0,
--    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN created_by VARCHAR(255),
--    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_by VARCHAR(255);

-- Add indexes for better query performance on audit columns
CREATE INDEX idx_order_items_created_at ON order_items(created_at);
CREATE INDEX idx_order_items_updated_at ON order_items(updated_at);

CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_updated_at ON orders(updated_at);

CREATE INDEX idx_products_created_at ON products(created_at);
CREATE INDEX idx_products_updated_at ON products(updated_at);