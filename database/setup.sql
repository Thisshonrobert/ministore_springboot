-- member 1
-- Run once in MySQL Workbench. Safe to rerun; does not reset existing records.
CREATE DATABASE IF NOT EXISTS mini_store CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mini_store;
CREATE TABLE IF NOT EXISTS categories (
  id TINYINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS products (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(1000),
  price DECIMAL(10,2) NOT NULL,
  category_id TINYINT NOT NULL,
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id),
  CONSTRAINT chk_price CHECK (price > 0)
);
CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NULL
);
CREATE TABLE IF NOT EXISTS carts (
  id BINARY(16) NOT NULL PRIMARY KEY,
  date_created DATE NOT NULL,
  user_id BIGINT NOT NULL UNIQUE,
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS cart_item (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  cart_id BINARY(16) NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  CONSTRAINT uq_cart_product UNIQUE (cart_id, product_id),
  CONSTRAINT fk_item_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
  CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT chk_quantity CHECK (quantity BETWEEN 1 AND 100)
);

-- member 4: customer reviews and the cached AI summary of them.
-- One review per customer per product, and at most one summary per product.
CREATE TABLE IF NOT EXISTS reviews (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  rating INT NOT NULL,
  comment VARCHAR(1000) NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT uq_review_product_user UNIQUE (product_id, user_id),
  CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5)
);
CREATE TABLE IF NOT EXISTS review_summary (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  content VARCHAR(2000) NOT NULL,
  generated_at DATETIME NOT NULL,
  expires_at DATETIME NOT NULL,
  CONSTRAINT uq_summary_product UNIQUE (product_id),
  CONSTRAINT fk_summary_product FOREIGN KEY (product_id) REFERENCES products(id)
);
