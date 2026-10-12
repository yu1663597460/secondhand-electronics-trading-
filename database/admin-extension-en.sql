-- Admin System Database Extension
USE secondhand_electronics;

-- Admin Operation Logs Table
CREATE TABLE IF NOT EXISTS admin_operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT,
    operation_details JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_admin_id (admin_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_target_type (target_type),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- System Settings Table
CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    setting_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    description TEXT,
    updated_by BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_setting_key (setting_key),
    INDEX idx_updated_at (updated_at),
    
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Order Disputes Table
CREATE TABLE IF NOT EXISTS order_disputes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    dispute_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    evidence_urls JSON,
    status ENUM('PENDING', 'INVESTIGATING', 'RESOLVED', 'CLOSED') DEFAULT 'PENDING',
    resolution TEXT,
    resolved_by BIGINT,
    resolved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_order_id (order_id),
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add review fields to products table
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS reviewed_by BIGINT,
ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS review_comment TEXT,
ADD INDEX IF NOT EXISTS idx_reviewed_by (reviewed_by),
ADD INDEX IF NOT EXISTS idx_reviewed_at (reviewed_at);

-- Insert default system settings
INSERT IGNORE INTO system_settings (setting_key, setting_value, setting_type, description) VALUES
('site.name', 'Secondhand Electronics Trading Platform', 'STRING', 'Website name'),
('site.logo', '/images/logo.png', 'STRING', 'Website logo path'),
('site.contact_email', 'admin@example.com', 'STRING', 'Contact email'),
('trading.commission_rate', '0.05', 'NUMBER', 'Trading commission rate'),
('trading.min_price', '1.00', 'NUMBER', 'Minimum product price'),
('payment.alipay_enabled', 'true', 'BOOLEAN', 'Alipay payment enabled'),
('payment.wechat_enabled', 'true', 'BOOLEAN', 'WeChat payment enabled'),
('security.max_login_attempts', '5', 'NUMBER', 'Maximum login attempts'),
('security.session_timeout', '86400', 'NUMBER', 'Session timeout in seconds');

-- Verify tables
SELECT 'admin_operation_logs' as table_name, COUNT(*) as record_count FROM admin_operation_logs
UNION ALL
SELECT 'system_settings' as table_name, COUNT(*) as record_count FROM system_settings
UNION ALL  
SELECT 'order_disputes' as table_name, COUNT(*) as record_count FROM order_disputes;

SELECT 'Database extension completed!' as message;