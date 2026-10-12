-- member 1
-- Run manually after setup.sql, with the application stopped, one session at a time.
-- Insert missing demo data only; preserve existing records, prices and quantities.
USE mini_store;
START TRANSACTION;

INSERT INTO categories (name)
SELECT 'Stationery' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Stationery');
INSERT INTO categories (name)
SELECT 'Books' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Books');
SET @seed_stationery = (SELECT MIN(id) FROM categories WHERE name = 'Stationery');
SET @seed_books = (SELECT MIN(id) FROM categories WHERE name = 'Books');

INSERT INTO products (name, description, price, category_id)
SELECT 'Notebook', 'Demo notebook', 250.00, @seed_stationery
WHERE NOT EXISTS (SELECT 1 FROM products
  WHERE name = 'Notebook' AND category_id = @seed_stationery AND price = 250.00);
INSERT INTO products (name, description, price, category_id)
SELECT 'Pen', 'Demo pen', 100.00, @seed_stationery
WHERE NOT EXISTS (SELECT 1 FROM products
  WHERE name = 'Pen' AND category_id = @seed_stationery AND price = 100.00);
INSERT INTO products (name, description, price, category_id)
SELECT 'Spring Notes', 'Demo Spring Boot notes', 150.00, @seed_books
WHERE NOT EXISTS (SELECT 1 FROM products
  WHERE name = 'Spring Notes' AND category_id = @seed_books AND price = 150.00);
SET @seed_notebook = (SELECT MIN(id) FROM products
  WHERE name = 'Notebook' AND category_id = @seed_stationery AND price = 250.00);
SET @seed_pen = (SELECT MIN(id) FROM products
  WHERE name = 'Pen' AND category_id = @seed_stationery AND price = 100.00);

INSERT INTO users (name, email)
SELECT 'Demo Customer', 'demo@ministore.example'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'demo@ministore.example');
INSERT INTO users (name, email)
SELECT 'Team Customer', 'team@ministore.example'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'team@ministore.example');
SET @seed_demo = (SELECT id FROM users WHERE email = 'demo@ministore.example');
SET @seed_team = (SELECT id FROM users WHERE email = 'team@ministore.example');

-- Hibernate UUID binary storage uses standard byte order, without MySQL's time swap.
-- user_id is unique: reuse a customer's existing cart without changing its UUID.
INSERT INTO carts (id, date_created, user_id)
SELECT UUID_TO_BIN(UUID(), 0), CURDATE(), @seed_demo
WHERE NOT EXISTS (SELECT 1 FROM carts WHERE user_id = @seed_demo);
INSERT INTO carts (id, date_created, user_id)
SELECT UUID_TO_BIN(UUID(), 0), CURDATE(), @seed_team
WHERE NOT EXISTS (SELECT 1 FROM carts WHERE user_id = @seed_team);
SET @seed_cart = (SELECT id FROM carts WHERE user_id = @seed_demo);

INSERT INTO cart_item (cart_id, product_id, quantity)
SELECT @seed_cart, @seed_notebook, 3
WHERE NOT EXISTS (SELECT 1 FROM cart_item WHERE cart_id = @seed_cart AND product_id = @seed_notebook);
INSERT INTO cart_item (cart_id, product_id, quantity)
SELECT @seed_cart, @seed_pen, 1
WHERE NOT EXISTS (SELECT 1 FROM cart_item WHERE cart_id = @seed_cart AND product_id = @seed_pen);

COMMIT;
