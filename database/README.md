# 数据库初始化

## 使用方法

### 方式一：使用MySQL命令行

```bash
mysql -u root -p < init.sql
```

### 方式二：使用MySQL客户端

1. 连接到MySQL服务器
2. 执行 `init.sql` 文件中的SQL语句

## 初始账号

### 管理员账号
- 用户名: `admin`
- 密码: `admin123`
- 角色: ADMIN

### 测试用户账号
- 用户名: `testuser`
- 密码: `user123456`
- 角色: USER

## 初始数据

系统会自动创建以下初始数据：
- 6个商品分类（手机、电脑、平板、配件、相机、游戏机）
- 3个示例商品
- 1个测试地址

## 注意事项

- 数据库名称: `secondhand_electronics`
- 字符集: `utf8mb4`
- 排序规则: `utf8mb4_unicode_ci`
- 确保MySQL版本 >= 5.7
