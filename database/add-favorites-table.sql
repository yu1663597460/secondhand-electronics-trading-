-- 创建收藏表
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    
    -- 唯一约束：同一用户不能重复收藏同一商品
    CONSTRAINT uk_user_product UNIQUE (user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品收藏表';

-- 创建索引以优化查询性能
CREATE INDEX idx_favorites_user_id ON favorites(user_id);
CREATE INDEX idx_favorites_product_id ON favorites(product_id);
CREATE INDEX idx_favorites_created_at ON favorites(created_at);

-- 插入测试数据（可选）
-- 假设用户ID 1 收藏商品ID 1, 2, 3
-- INSERT INTO favorites (user_id, product_id) VALUES (1, 1);
-- INSERT INTO favorites (user_id, product_id) VALUES (1, 2);
-- INSERT INTO favorites (user_id, product_id) VALUES (1, 3);
