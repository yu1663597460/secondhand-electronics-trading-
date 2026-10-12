# 二手电子产品交易系统 - 后端

## 技术栈

- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- MySQL 8.x
- JWT认证
- Maven

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/secondhand/electronics/
│   │   │   ├── entity/          # 实体类
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── service/         # 业务逻辑层
│   │   │   ├── controller/      # 控制器层
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── config/          # 配置类
│   │   │   ├── security/        # 安全相关
│   │   │   ├── exception/       # 异常处理
│   │   │   └── ElectronicsTradingApplication.java
│   │   └── resources/
│   │       ├── application.yml           # 主配置文件
│   │       ├── application-dev.yml       # 开发环境配置
│   │       └── application-prod.yml      # 生产环境配置
│   └── test/                    # 测试代码
└── pom.xml                      # Maven配置
```

## 快速开始

### 前置要求

- JDK 17+
- Maven 3.6+
- MySQL 8.x

### 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE secondhand_electronics CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `application.yml` 中的数据库连接信息（如需要）

### 运行项目

开发环境：
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

生产环境：
```bash
mvn clean package
java -jar target/electronics-trading-1.0.0.jar --spring.profiles.active=prod
```

## API文档

服务器启动后，API基础路径为：`http://localhost:8080/api`

### 认证相关
- POST `/auth/register` - 用户注册
- POST `/auth/login` - 用户登录

### 用户相关
- GET `/users/profile` - 获取用户信息
- PUT `/users/profile` - 更新用户信息
- PUT `/users/password` - 修改密码

### 商品相关
- GET `/products` - 浏览商品列表
- GET `/products/{id}` - 获取商品详情
- POST `/products` - 发布商品
- PUT `/products/{id}` - 编辑商品

### 订单相关
- POST `/orders` - 创建订单
- GET `/orders/my` - 获取我的订单
- GET `/orders/{id}` - 获取订单详情

### 管理员相关
- GET `/admin/users` - 获取用户列表
- GET `/admin/products` - 获取商品列表
- PUT `/admin/products/{id}/review` - 审核商品

## 开发说明

### 环境配置

- 开发环境使用 `dev` profile
- 生产环境使用 `prod` profile

### JWT配置

JWT密钥配置在 `application.yml` 中，生产环境请务必修改为安全的密钥。

### 文件上传

上传的文件默认存储在 `./uploads` 目录，可在配置文件中修改。
