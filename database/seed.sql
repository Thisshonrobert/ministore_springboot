-- member 1
-- Run manually after setup.sql (and ai-migration.sql on an existing database),
-- with the application stopped, one session at a time.
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

-- member 4: demo reviews, so the AI review summary can be demonstrated.
-- The summary needs at least three reviews (app.reviews.minimum-for-summary),
-- and one customer may review a product only once, so four separate reviewers
-- are seeded. The comments mix praise with a durability complaint, which gives
-- the summary both a positive point and a caveat. The summary itself is not
-- seeded: the app generates and caches it on the first summary request.
-- Requires the reviews table, so run ai-migration.sql (or the current
-- setup.sql) before this script on an existing database.
INSERT INTO users (name, email)
SELECT 'Demo Reader One', 'reader1@ministore.example'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'reader1@ministore.example');
INSERT INTO users (name, email)
SELECT 'Demo Reader Two', 'reader2@ministore.example'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'reader2@ministore.example');
INSERT INTO users (name, email)
SELECT 'Demo Reader Three', 'reader3@ministore.example'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'reader3@ministore.example');
INSERT INTO users (name, email)
SELECT 'Demo Reader Four', 'reader4@ministore.example'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'reader4@ministore.example');
SET @seed_reader1 = (SELECT MIN(id) FROM users WHERE email = 'reader1@ministore.example');
SET @seed_reader2 = (SELECT MIN(id) FROM users WHERE email = 'reader2@ministore.example');
SET @seed_reader3 = (SELECT MIN(id) FROM users WHERE email = 'reader3@ministore.example');
SET @seed_reader4 = (SELECT MIN(id) FROM users WHERE email = 'reader4@ministore.example');

INSERT INTO reviews (product_id, user_id, rating, comment, created_at)
SELECT @seed_notebook, @seed_reader1, 5,
       'Lovely smooth paper and the binding held up for a whole term.', NOW() - INTERVAL 8 DAY
WHERE NOT EXISTS (SELECT 1 FROM reviews
  WHERE product_id = @seed_notebook AND user_id = @seed_reader1);
INSERT INTO reviews (product_id, user_id, rating, comment, created_at)
SELECT @seed_notebook, @seed_reader2, 5,
       'Great value for the price. I already ordered a second one.', NOW() - INTERVAL 6 DAY
WHERE NOT EXISTS (SELECT 1 FROM reviews
  WHERE product_id = @seed_notebook AND user_id = @seed_reader2);
INSERT INTO reviews (product_id, user_id, rating, comment, created_at)
SELECT @seed_notebook, @seed_reader3, 4,
       'Good notebook overall, but the cover is a little thin for a backpack.', NOW() - INTERVAL 4 DAY
WHERE NOT EXISTS (SELECT 1 FROM reviews
  WHERE product_id = @seed_notebook AND user_id = @seed_reader3);
INSERT INTO reviews (product_id, user_id, rating, comment, created_at)
SELECT @seed_notebook, @seed_reader4, 2,
       'The pages started to come loose after a few weeks of daily use.', NOW() - INTERVAL 2 DAY
WHERE NOT EXISTS (SELECT 1 FROM reviews
  WHERE product_id = @seed_notebook AND user_id = @seed_reader4);

COMMIT;
