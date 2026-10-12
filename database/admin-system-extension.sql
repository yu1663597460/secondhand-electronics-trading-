-- ========================================
-- 管理员系统功能扩展 - 数据库结构
-- ========================================

USE secondhand_electronics;

-- ========================================
-- 1. 管理员操作日志表
-- ========================================
CREATE TABLE IF NOT EXISTS admin_operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(50) NOT NULL COMMENT '目标类型(USER/PRODUCT/ORDER/CATEGORY)',
    target_id BIGINT COMMENT '目标ID',
    operation_details JSON COMMENT '操作详情',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_admin_id (admin_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_target_type (target_type),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

-- ========================================
-- 2. 系统设置表
-- ========================================
CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) NOT NULL UNIQUE COMMENT '设置键',
    setting_value TEXT COMMENT '设置值',
    setting_type VARCHAR(20) NOT NULL DEFAULT 'STRING' COMMENT '设置类型(STRING/NUMBER/BOOLEAN/JSON)',
    description TEXT COMMENT '设置描述',
    updated_by BIGINT COMMENT '更新人ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_setting_key (setting_key),
    INDEX idx_updated_at (updated_at),
    
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统设置表';

-- ========================================
-- 3. 订单纠纷表（扩展订单管理功能）
-- ========================================
CREATE TABLE IF NOT EXISTS order_disputes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    reporter_id BIGINT NOT NULL COMMENT '举报人ID',
    dispute_type VARCHAR(50) NOT NULL COMMENT '纠纷类型',
    description TEXT NOT NULL COMMENT '纠纷描述',
    evidence_urls JSON COMMENT '证据图片URLs',
    status ENUM('PENDING', 'INVESTIGATING', 'RESOLVED', 'CLOSED') DEFAULT 'PENDING' COMMENT '纠纷状态',
    resolution TEXT COMMENT '处理结果',
    resolved_by BIGINT COMMENT '处理人ID',
    resolved_at TIMESTAMP NULL COMMENT '处理时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_order_id (order_id),
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单纠纷表';

-- ========================================
-- 4. 为现有表添加管理相关字段
-- ========================================

-- 为products表添加审核相关字段
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS reviewed_by BIGINT COMMENT '审核人ID',
ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP NULL COMMENT '审核时间',
ADD COLUMN IF NOT EXISTS review_comment TEXT COMMENT '审核备注',
ADD INDEX idx_reviewed_by (reviewed_by),
ADD INDEX idx_reviewed_at (reviewed_at);

-- 为products表添加外键约束（如果不存在）
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE 
                  WHERE TABLE_SCHEMA = 'secondhand_electronics' 
                  AND TABLE_NAME = 'products' 
                  AND CONSTRAINT_NAME = 'fk_products_reviewed_by');

SET @sql = IF(@fk_exists = 0, 
    'ALTER TABLE products ADD CONSTRAINT fk_products_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL',
    'SELECT "Foreign key already exists" as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 5. 插入默认系统设置
-- ========================================
INSERT IGNORE INTO system_settings (setting_key, setting_value, setting_type, description) VALUES
('site.name', '二手电子产品交易平台', 'STRING', '网站名称'),
('site.logo', '/images/logo.png', 'STRING', '网站Logo路径'),
('site.contact_email', 'admin@example.com', 'STRING', '联系邮箱'),
('site.contact_phone', '400-123-4567', 'STRING', '联系电话'),
('trading.commission_rate', '0.05', 'NUMBER', '交易手续费率'),
('trading.min_price', '1.00', 'NUMBER', '最低商品价格'),
('trading.max_price', '999999.99', 'NUMBER', '最高商品价格'),
('trading.auto_approve_threshold', '1000.00', 'NUMBER', '自动审核通过金额阈值'),
('payment.alipay_enabled', 'true', 'BOOLEAN', '支付宝支付是否启用'),
('payment.wechat_enabled', 'true', 'BOOLEAN', '微信支付是否启用'),
('payment.bank_transfer_enabled', 'false', 'BOOLEAN', '银行转账是否启用'),
('security.max_login_attempts', '5', 'NUMBER', '最大登录尝试次数'),
('security.session_timeout', '86400', 'NUMBER', '会话超时时间(秒)');

-- ========================================
-- 6. 验证表创建结果
-- ========================================
SELECT 'admin_operation_logs' as table_name, COUNT(*) as record_count FROM admin_operation_logs
UNION ALL
SELECT 'system_settings' as table_name, COUNT(*) as record_count FROM system_settings
UNION ALL  
SELECT 'order_disputes' as table_name, COUNT(*) as record_count FROM order_disputes;

SELECT '数据库扩展完成！' as message;