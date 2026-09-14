-- ============================================================
-- Currency Converter — Database Schema
-- MySQL 8.x | MySQL Workbench compatible
-- Combined OOP II + Database Programming project
--
-- Normalization note (2NF):
--   2NF only becomes an issue where a table's primary key is
--   composite and a non-key column depends on PART of that key
--   rather than the WHOLE key. To avoid partial dependencies,
--   every table here uses a single-column surrogate PK (an
--   auto-increment ID), and natural composite candidate keys
--   (e.g. base_currency + target_currency + date) are instead
--   enforced as UNIQUE constraints. This keeps every non-key
--   column dependent on the whole key by construction, while
--   still preserving the real-world composite relationships.
--   The comments on exchange_rates, rate_history and
--   currency_pairs call this out explicitly since those are the
--   tables where 2NF actually matters.
-- ============================================================

DROP DATABASE IF EXISTS currency_converter;
CREATE DATABASE currency_converter;
USE currency_converter;

-- ---------- Reference data ----------

CREATE TABLE countries (
    country_id      INT AUTO_INCREMENT PRIMARY KEY,
    country_name    VARCHAR(100) NOT NULL,
    country_code    CHAR(2) NOT NULL UNIQUE   -- ISO 3166-1 alpha-2
);

CREATE TABLE currencies (
    currency_id     INT AUTO_INCREMENT PRIMARY KEY,
    currency_code   CHAR(3) NOT NULL UNIQUE,   -- ISO 4217, e.g. UGX, USD
    currency_name   VARCHAR(100) NOT NULL,
    symbol          VARCHAR(5),
    country_id      INT,
    FOREIGN KEY (country_id) REFERENCES countries(country_id)
);

CREATE TABLE roles (
    role_id         INT AUTO_INCREMENT PRIMARY KEY,
    role_name       VARCHAR(50) NOT NULL UNIQUE   -- e.g. ADMIN, USER
);

CREATE TABLE rate_sources (
    source_id       INT AUTO_INCREMENT PRIMARY KEY,
    source_name     VARCHAR(100) NOT NULL,
    source_type     ENUM('API','MANUAL','BANK') NOT NULL,
    api_endpoint    VARCHAR(255)
);

-- ---------- Currency pairs & rates ----------

-- Composite candidate key: (base_currency_id, target_currency_id).
-- Surrogate PK avoids partial dependency; UNIQUE enforces the real
-- business key so is_active depends on the whole pair, not one side.
CREATE TABLE currency_pairs (
    pair_id             INT AUTO_INCREMENT PRIMARY KEY,
    base_currency_id    INT NOT NULL,
    target_currency_id  INT NOT NULL,
    is_active           BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (base_currency_id) REFERENCES currencies(currency_id),
    FOREIGN KEY (target_currency_id) REFERENCES currencies(currency_id),
    UNIQUE KEY uq_pair (base_currency_id, target_currency_id)
);

-- Composite candidate key: (base, target, rate_date). rate depends
-- on all three together, so a surrogate PK + UNIQUE constraint keeps
-- this in 2NF rather than a composite PK with a partial dependency.
CREATE TABLE exchange_rates (
    rate_id             INT AUTO_INCREMENT PRIMARY KEY,
    base_currency_id    INT NOT NULL,
    target_currency_id  INT NOT NULL,
    source_id           INT,
    rate                DECIMAL(18,6) NOT NULL,
    rate_date           DATE NOT NULL,
    FOREIGN KEY (base_currency_id) REFERENCES currencies(currency_id),
    FOREIGN KEY (target_currency_id) REFERENCES currencies(currency_id),
    FOREIGN KEY (source_id) REFERENCES rate_sources(source_id),
    UNIQUE KEY uq_rate_per_day (base_currency_id, target_currency_id, rate_date)
);

CREATE TABLE rate_history (
    history_id          INT AUTO_INCREMENT PRIMARY KEY,
    base_currency_id    INT NOT NULL,
    target_currency_id  INT NOT NULL,
    rate                DECIMAL(18,6) NOT NULL,
    recorded_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (base_currency_id) REFERENCES currencies(currency_id),
    FOREIGN KEY (target_currency_id) REFERENCES currencies(currency_id)
);

CREATE TABLE fee_structures (
    fee_id          INT AUTO_INCREMENT PRIMARY KEY,
    source_id       INT NOT NULL,
    fee_percentage  DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    min_fee         DECIMAL(10,2) DEFAULT 0.00,
    max_fee         DECIMAL(10,2),
    FOREIGN KEY (source_id) REFERENCES rate_sources(source_id)
);

-- ---------- Users & activity ----------

CREATE TABLE users (
    user_id         INT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role_id         INT NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

CREATE TABLE user_sessions (
    session_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    login_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
    logout_time     DATETIME,
    ip_address      VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE conversions (
    conversion_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id             INT,
    base_currency_id    INT NOT NULL,
    target_currency_id  INT NOT NULL,
    amount              DECIMAL(18,2) NOT NULL,
    converted_amount    DECIMAL(18,2) NOT NULL,
    rate_used           DECIMAL(18,6) NOT NULL,
    conversion_date     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (base_currency_id) REFERENCES currencies(currency_id),
    FOREIGN KEY (target_currency_id) REFERENCES currencies(currency_id)
);

CREATE TABLE favorites (
    favorite_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    pair_id         INT NOT NULL,
    added_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (pair_id) REFERENCES currency_pairs(pair_id),
    UNIQUE KEY uq_user_pair (user_id, pair_id)
);

CREATE TABLE alerts (
    alert_id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    pair_id         INT NOT NULL,
    target_rate     DECIMAL(18,6) NOT NULL,
    triggered       BOOLEAN DEFAULT FALSE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (pair_id) REFERENCES currency_pairs(pair_id)
);

-- ---------- Logging & audit ----------

CREATE TABLE transaction_log (
    log_id          INT AUTO_INCREMENT PRIMARY KEY,
    conversion_id   INT NOT NULL,
    status          ENUM('SUCCESS','FAILED') NOT NULL,
    message         VARCHAR(255),
    logged_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversion_id) REFERENCES conversions(conversion_id)
);

CREATE TABLE audit_trail (
    audit_id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT,
    action          VARCHAR(50) NOT NULL,      -- INSERT / UPDATE / DELETE
    table_affected  VARCHAR(64) NOT NULL,
    record_id       INT,
    changed_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE error_log (
    error_id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT,
    error_message   VARCHAR(500) NOT NULL,
    stack_trace     TEXT,
    occurred_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ---------- Seed data (minimal, for demo) ----------

INSERT INTO countries (country_name, country_code) VALUES
    ('Uganda', 'UG'), ('United States', 'US'), ('United Kingdom', 'GB'), ('Kenya', 'KE');

INSERT INTO currencies (currency_code, currency_name, symbol, country_id) VALUES
    ('UGX', 'Ugandan Shilling', 'USh', 1),
    ('USD', 'US Dollar', '$', 2),
    ('GBP', 'British Pound', '£', 3),
    ('KES', 'Kenyan Shilling', 'KSh', 4);

INSERT INTO roles (role_name) VALUES ('ADMIN'), ('USER');

INSERT INTO rate_sources (source_name, source_type, api_endpoint) VALUES
    ('Manual Entry', 'MANUAL', NULL),
    ('ExchangeRate-API', 'API', 'https://api.exchangerate-api.com/v4/latest');

INSERT INTO currency_pairs (base_currency_id, target_currency_id) VALUES
    (2, 1), (1, 2), (2, 3), (3, 2);

INSERT INTO exchange_rates (base_currency_id, target_currency_id, source_id, rate, rate_date) VALUES
    (2, 1, 1, 3700.00, CURDATE()),
    (1, 2, 1, 0.00027, CURDATE());
