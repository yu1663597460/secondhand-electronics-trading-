-- 创建数据库
CREATE DATABASE IF NOT EXISTS secondhand_electronics 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE secondhand_electronics;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    verification_status VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    real_name VARCHAR(50),
    id_card VARCHAR(18),
    phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 商品分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 商品表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category_id BIGINT,
    seller_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    review_note VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_seller (seller_id),
    INDEX idx_category (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 商品图片表
CREATE TABLE IF NOT EXISTS product_images (
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 收货地址表
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    district VARCHAR(50) NOT NULL,
    detail_address VARCHAR(200) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    address_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    admin_note VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE CASCADE,
    INDEX idx_buyer (buyer_id),
    INDEX idx_seller (seller_id),
    INDEX idx_order_number (order_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入初始数据

-- 插入管理员账号 (密码: admin123)
-- BCrypt加密后的密码 (使用标准BCrypt算法)
INSERT INTO users (username, email, password, role, status) VALUES 
('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'ACTIVE');

-- 插入测试用户 (密码: user123456)
-- BCrypt加密后的密码 (使用标准BCrypt算法)
INSERT INTO users (username, email, password, role, status) VALUES 
('testuser', 'user@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'USER', 'ACTIVE');

-- 插入商品分类
INSERT INTO categories (name, description) VALUES 
('手机', '各类二手手机'),
('电脑', '笔记本电脑、台式机等'),
('平板', '平板电脑'),
('配件', '耳机、充电器等配件'),
('相机', '数码相机、单反相机'),
('游戏机', '游戏主机及配件');

-- 插入示例商品
INSERT INTO products (title, description, price, category_id, seller_id, status) VALUES 
('iPhone 13 Pro 256GB', '9成新，无划痕，配件齐全', 4999.00, 1, 2, 'APPROVED'),
('MacBook Pro 2021', 'M1芯片，16GB内存，512GB存储', 8999.00, 2, 2, 'APPROVED'),
('iPad Air 第五代', '64GB，WiFi版本，几乎全新', 3299.00, 3, 2, 'APPROVED');

-- 插入商品图片
INSERT INTO product_images (product_id, image_url) VALUES 
(1, 'https://via.placeholder.com/400x300?text=iPhone+13+Pro'),
(2, 'https://via.placeholder.com/400x300?text=MacBook+Pro'),
(3, 'https://via.placeholder.com/400x300?text=iPad+Air');

-- 插入测试地址
INSERT INTO addresses (user_id, receiver_name, phone, province, city, district, detail_address, is_default) VALUES 
(2, '张三', '13800138000', '北京市', '北京市', '朝阳区', '某某街道某某小区1号楼101室', TRUE);

COMMIT;
