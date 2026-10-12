USE secondhand_electronics;

-- 碳积分账户表（每个用户一条记录）
CREATE TABLE IF NOT EXISTS carbon_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    total_points INT NOT NULL DEFAULT 0,       -- 累计获得积分
    available_points INT NOT NULL DEFAULT 0,   -- 当前可用积分
    level VARCHAR(20) NOT NULL DEFAULT 'SPROUT', -- 等级: SPROUT/LEAF/TREE/GUARDIAN
    carbon_saved_grams INT NOT NULL DEFAULT 0, -- 累计减碳量(克)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 碳积分明细表
CREATE TABLE IF NOT EXISTS carbon_point_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    points INT NOT NULL,                        -- 正数=获得，负数=消耗
    action_type VARCHAR(30) NOT NULL,           -- PUBLISH/COMPLETE_ORDER/REVIEW/DAILY_SIGN/REDEEM
    description VARCHAR(200),
    ref_id BIGINT,                              -- 关联的订单/商品ID
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

COMMIT;
