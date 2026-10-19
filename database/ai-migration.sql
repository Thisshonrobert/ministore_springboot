-- member 4
-- AI review migration for an existing mini_store database.
-- Run once with an account that may CREATE tables (for example a MySQL administrator).
-- Safe to rerun: both tables use IF NOT EXISTS.
--
-- The chatbot needs no schema at all: it reads the existing products table and
-- calls the AI provider through the backend. Only reviews and their cached
-- summary need new tables. The Spring Boot app uses ddl-auto: validate, so the
-- tables must exist before the app starts.
USE mini_store;

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
