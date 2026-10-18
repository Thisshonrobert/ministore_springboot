-- member 3
-- Phase 2 authentication migration for an existing mini_store database.
-- Run once with an account that may ALTER the schema (for example a MySQL administrator),
-- after database/setup.sql and (optionally) database/seed.sql. Safe to rerun.
USE mini_store;

SET @password_hash_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'password_hash'
);

SET @ddl = IF(
  @password_hash_exists = 0,
  'ALTER TABLE users ADD COLUMN password_hash VARCHAR(255) NULL AFTER email',
  'SELECT ''password_hash column already exists'' AS migration_status'
);
PREPARE migration_statement FROM @ddl;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

-- Optional local demo credentials. The demo password is StoreDemo123! and is
-- stored only as a salted PBKDF2-HMAC-SHA256 hash. Existing hashes are preserved.
UPDATE users
SET password_hash = 'pbkdf2_sha256$210000$ojBV0oqqbZpyO2frAlT9WQ$2pZwc2v_Bx7rwOqIouvEHKEbAgFBC5VwHd2Y77hrL2g'
WHERE email = 'demo@ministore.example' AND password_hash IS NULL;

UPDATE users
SET password_hash = 'pbkdf2_sha256$210000$tL6_15qPSS8Ix421nR45Xg$D8VnO0sRCb_yFd22_yT8DdT1tdsuvf_vlzyTNCihTnM'
WHERE email = 'team@ministore.example' AND password_hash IS NULL;
