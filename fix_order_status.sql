USE secondhand_electronics;
-- 将旧的 CONFIRMED 状态迁移到新的 PAID 状态
UPDATE orders SET status='PAID' WHERE status='CONFIRMED';
-- 验证结果
SELECT status, COUNT(*) as cnt FROM orders GROUP BY status;
