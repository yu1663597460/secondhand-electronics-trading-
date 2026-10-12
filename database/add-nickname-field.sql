-- 添加用户昵称字段
ALTER TABLE users ADD COLUMN nickname VARCHAR(50) NULL COMMENT '用户昵称（可修改）';

-- 初始化昵称为用户名
UPDATE users SET nickname = username WHERE nickname IS NULL;
