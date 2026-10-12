# 设计文档

## 概述

二手电子产品交易系统是一个现代化的全栈Web应用，采用前后端分离架构。后端使用Spring Boot 3.x + JPA + MySQL构建高性能RESTful API，前端使用Vue 3 + Element Plus构建现代化响应式用户界面。系统具有完整的用户管理、商品交易、实时聊天和管理后台功能，采用现代化UI设计和优秀的用户体验。

## 系统特色

- **现代化设计**: Glassmorphism设计风格，渐变背景，流畅动画
- **实时通信**: 基于HTTP轮询的聊天系统，支持买卖双方实时沟通
- **完整功能**: 用户注册登录、商品发布交易、订单管理、地址管理
- **管理后台**: 全面的管理员功能，包括用户管理、商品审核、订单处理
- **安全可靠**: JWT认证、角色权限控制、数据验证、错误处理
- **响应式设计**: 支持桌面端和移动端，自适应各种屏幕尺寸

## 架构

### 系统架构

系统采用三层架构：

```
┌─────────────────────────────────────┐
│         前端层 (Vue 3)              │
│   - 用户界面                         │
│   - 管理员界面                       │
└─────────────┬───────────────────────┘
              │ HTTP/REST API
┌─────────────▼───────────────────────┐
│      应用层 (Spring Boot)           │
│   - Controller (API端点)            │
│   - Service (业务逻辑)               │
│   - Security (认证授权)              │
└─────────────┬───────────────────────┘
              │ JPA/Hibernate
┌─────────────▼───────────────────────┐
│      数据层 (MySQL)                 │
│   - 用户表                           │
│   - 商品表                           │
│   - 订单表                           │
│   - 地址表                           │
└─────────────────────────────────────┘
```

### 技术栈

**后端:**
- Spring Boot 3.x: 应用框架
- Spring Data JPA: 数据持久化
- Spring Security: 认证和授权
- MySQL 8.x: 关系型数据库
- Maven: 项目构建工具
- JWT: 令牌认证

**前端:**
- Vue 3: 前端框架
- Element Plus 2.12.0: UI组件库（最新稳定版）
- Vite: 构建工具和开发服务器
- Axios: HTTP客户端
- Vue Router: 路由管理
- Pinia: 状态管理
- date-fns: 日期处理库（聊天功能）

## 组件和接口

### 后端组件

#### 1. 用户管理模块 (User Management)

**Controller: UserController**
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/users/profile` - 获取用户信息
- `PUT /api/users/profile` - 更新用户信息
- `PUT /api/users/password` - 修改密码
- `POST /api/users/verification` - 提交实名认证

**Controller: AdminUserController**
- `GET /api/admin/users` - 获取所有用户列表
- `GET /api/admin/users/{id}` - 获取用户详情
- `PUT /api/admin/users/{id}/status` - 更新用户账号状态
- `GET /api/admin/users/search` - 搜索用户

**Service: UserService**
- `registerUser(UserDTO)` - 注册新用户
- `authenticateUser(username, password)` - 验证用户登录
- `updateUserProfile(userId, UserDTO)` - 更新用户资料
- `changePassword(userId, oldPassword, newPassword)` - 修改密码
- `submitVerification(userId, VerificationDTO)` - 提交实名认证

**Service: AdminUserService**
- `getAllUsers(page, size)` - 分页获取用户列表
- `getUserById(userId)` - 获取用户详情
- `updateUserStatus(userId, status)` - 更新用户状态
- `searchUsers(keyword)` - 搜索用户

#### 2. 商品管理模块 (Product Management)

**Controller: ProductController**
- `POST /api/products` - 发布商品
- `PUT /api/products/{id}` - 编辑商品
- `GET /api/products` - 浏览商品列表
- `GET /api/products/{id}` - 获取商品详情
- `GET /api/products/my` - 获取我的商品
- `PUT /api/products/{id}/status` - 更新商品状态

**Controller: AdminProductController**
- `GET /api/admin/products` - 获取所有商品
- `PUT /api/admin/products/{id}/review` - 审核商品
- `PUT /api/admin/products/{id}/takedown` - 下架商品
- `GET /api/admin/categories` - 获取分类列表
- `POST /api/admin/categories` - 添加分类
- `PUT /api/admin/categories/{id}` - 编辑分类
- `DELETE /api/admin/categories/{id}` - 删除分类

**Service: ProductService**
- `createProduct(userId, ProductDTO)` - 创建商品
- `updateProduct(productId, ProductDTO)` - 更新商品
- `getProductById(productId)` - 获取商品详情
- `getProductsByUser(userId)` - 获取用户的商品
- `searchProducts(keyword, category, priceSort)` - 搜索和筛选商品
- `updateProductStatus(productId, status)` - 更新商品状态

**Service: AdminProductService**
- `getAllProducts(page, size)` - 分页获取所有商品
- `reviewProduct(productId, approved, reason)` - 审核商品
- `takedownProduct(productId, reason)` - 下架商品
- `manageCategories()` - 管理商品分类

#### 3. 订单管理模块 (Order Management)

**Controller: OrderController**
- `POST /api/orders` - 创建订单
- `GET /api/orders/my` - 获取我的订单
- `GET /api/orders/{id}` - 获取订单详情
- `PUT /api/orders/{id}/confirm` - 确认收货

**Controller: AdminOrderController**
- `GET /api/admin/orders` - 获取所有订单
- `GET /api/admin/orders/{id}` - 获取订单详情
- `PUT /api/admin/orders/{id}/note` - 添加订单备注

**Service: OrderService**
- `createOrder(buyerId, productId, addressId)` - 创建订单
- `getOrdersByUser(userId)` - 获取用户订单
- `getOrderById(orderId)` - 获取订单详情
- `confirmReceipt(orderId)` - 确认收货

**Service: AdminOrderService**
- `getAllOrders(page, size)` - 分页获取所有订单
- `searchOrders(keyword)` - 搜索订单
- `addOrderNote(orderId, note)` - 添加订单备注

#### 4. 地址管理模块 (Address Management)

**Controller: AddressController**
- `POST /api/addresses` - 添加地址
- `PUT /api/addresses/{id}` - 编辑地址
- `DELETE /api/addresses/{id}` - 删除地址
- `GET /api/addresses` - 获取地址列表
- `PUT /api/addresses/{id}/default` - 设置默认地址

**Service: AddressService**
- `addAddress(userId, AddressDTO)` - 添加地址
- `updateAddress(addressId, AddressDTO)` - 更新地址
- `deleteAddress(addressId)` - 删除地址
- `getAddressesByUser(userId)` - 获取用户地址列表
- `setDefaultAddress(addressId)` - 设置默认地址

#### 5. 聊天管理模块 (Chat Management)

**Controller: ChatController**
- `POST /api/chat/sessions` - 创建或获取聊天会话
- `GET /api/chat/sessions` - 获取用户的聊天会话列表
- `GET /api/chat/sessions/{sessionId}/messages` - 获取聊天消息
- `POST /api/chat/sessions/{sessionId}/messages` - 发送消息
- `PUT /api/chat/messages/{messageId}/read` - 标记消息为已读
- `GET /api/chat/unread-count` - 获取未读消息数量

**Service: ChatService**
- `createOrGetSession(user1Id, user2Id, productId)` - 创建或获取聊天会话
- `getUserSessions(userId)` - 获取用户的聊天会话
- `getSessionMessages(sessionId)` - 获取会话消息
- `sendMessage(sessionId, senderId, content)` - 发送消息
- `markMessageAsRead(messageId)` - 标记消息已读
- `getUnreadMessageCount(userId)` - 获取未读消息数量

### 前端组件

#### 用户模块组件
- `LoginView.vue` - 现代化登录页面，glassmorphism设计
- `RegisterView.vue` - 注册页面
- `SimpleHome.vue` - 首页，包含滚动横幅和快速导航
- `Layout.vue` - 主布局组件，现代化导航栏设计
- `UserProfile.vue` - 用户中心
- `AddressManagement.vue` - 地址管理
- `ProductList.vue` - 商品浏览，现代化卡片设计
- `ProductDetail.vue` - 商品详情
- `MyProducts.vue` - 我的商品
- `PublishProduct.vue` - 发布商品
- `OrderList.vue` - 订单列表
- `OrderDetail.vue` - 订单详情
- `ChatList.vue` - 聊天列表
- `ChatWindow.vue` - 聊天窗口

#### 管理员模块组件
- `AdminDashboard.vue` - 管理员仪表板，现代化设计
- `AdminUserManagement.vue` - 用户管理
- `AdminProductManagement.vue` - 商品管理
- `AdminOrderManagement.vue` - 订单管理
- `CategoryManagement.vue` - 分类管理

## 数据模型

### User (用户)
```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password; // 加密存储
    
    @Enumerated(EnumType.STRING)
    private UserRole role; // USER, ADMIN
    
    @Enumerated(EnumType.STRING)
    private AccountStatus status; // ACTIVE, DISABLED
    
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus; // UNVERIFIED, PENDING, VERIFIED
    
    private String realName;
    private String idCard;
    private String phone;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Product (商品)
```java
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;
    
    @ElementCollection
    private List<String> images;
    
    @Enumerated(EnumType.STRING)
    private ProductStatus status; // PENDING, APPROVED, REJECTED, SOLD, TAKEN_DOWN
    
    private String reviewNote; // 审核备注
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Category (分类)
```java
@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    private String description;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### Order (订单)
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String orderNumber;
    
    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;
    
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address shippingAddress;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status; // CREATED, CONFIRMED, COMPLETED, CANCELLED
    
    private String adminNote; // 管理员备注
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Address (地址)
```java
@Entity
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private String receiverName;
    
    @Column(nullable = false)
    private String phone;
    
    @Column(nullable = false)
    private String province;
    
    @Column(nullable = false)
    private String city;
    
    @Column(nullable = false)
    private String district;
    
    @Column(nullable = false)
    private String detailAddress;
    
    private Boolean isDefault;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### ChatSession (聊天会话)
```java
@Entity
public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user1_id")
    private User user1;
    
    @ManyToOne
    @JoinColumn(name = "user2_id")
    private User user2;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Message (消息)
```java
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "session_id")
    private ChatSession session;
    
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```


## 正确性属性

*属性是指在系统的所有有效执行中都应该成立的特征或行为——本质上是关于系统应该做什么的形式化陈述。属性作为人类可读规范和机器可验证正确性保证之间的桥梁。*

### 属性反思

在分析所有可测试的验收标准后，我识别出以下可以合并或简化的冗余属性：

1. **用户状态管理**: 属性1.3（禁用账号）和1.4（启用账号）可以合并为一个往返属性
2. **商品审核流程**: 属性2.2（批准）和2.3（拒绝）都是审核操作的不同结果，可以合并
3. **商品状态转换**: 属性6.4（下架）和6.5（重新上架）是状态转换的往返，可以合并
4. **列表显示属性**: 多个"显示列表"的属性（1.1, 3.1, 5.5等）本质上都在测试API返回包含必需字段，可以用一个通用模式表达

经过反思，我将重点关注以下核心属性：

### 属性 1: 用户账号状态往返一致性
*对于任意*用户账号，禁用后再启用应该恢复账号的可登录状态
**验证需求: 1.3, 1.4**

### 属性 2: 用户搜索结果相关性
*对于任意*搜索关键词，所有返回的用户结果的用户名或邮箱都应该包含该关键词
**验证需求: 1.2**

### 属性 3: 商品初始状态正确性
*对于任意*新发布的商品，其初始状态应该为待审核
**验证需求: 2.1, 6.1**

### 属性 4: 商品审核状态转换正确性
*对于任意*待审核商品，管理员批准后状态应变为上架，拒绝后状态应变为已拒绝
**验证需求: 2.2, 2.3**

### 属性 5: 商品下架后不可见
*对于任意*已下架或已售的商品，在公开商品列表中不应该出现
**验证需求: 6.4, 7.1**

### 属性 6: 商品分类过滤正确性
*对于任意*分类筛选条件，返回的所有商品都应该属于该分类
**验证需求: 7.2**

### 属性 7: 商品搜索结果相关性
*对于任意*搜索关键词，所有返回的商品的标题或描述都应该包含该关键词
**验证需求: 7.3**

### 属性 8: 商品价格排序正确性
*对于任意*商品列表，按价格升序排序后，任意相邻两个商品的价格应该满足前者小于等于后者
**验证需求: 7.4**

### 属性 9: 订单创建更新商品状态
*对于任意*商品，当订单创建成功后，该商品的状态应该更新为已售
**验证需求: 8.2**

### 属性 10: 用户订单隔离性
*对于任意*用户，其订单列表中的所有订单都应该是该用户作为买家或卖家的订单
**验证需求: 8.3**

### 属性 11: 订单确认收货状态转换
*对于任意*订单，买家确认收货后，订单状态应该更新为已完成
**验证需求: 8.5**

### 属性 12: 默认地址唯一性
*对于任意*用户，在其所有收货地址中，最多只有一个地址被标记为默认地址
**验证需求: 5.4**

### 属性 13: 地址更新隔离性
*对于任意*用户的多个地址，更新其中一个地址后，其他地址的信息应该保持不变
**验证需求: 5.2**

### 属性 14: 必填字段验证
*对于任意*包含空必填字段的表单提交，系统应该拒绝并返回验证错误
**验证需求: 9.1**

### 属性 15: 邮箱格式验证
*对于任意*不符合邮箱格式的字符串，系统应该拒绝并返回格式错误提示
**验证需求: 9.2**

### 属性 16: 密码长度验证
*对于任意*长度不在8-20字符范围内的密码，系统应该拒绝并返回长度要求提示
**验证需求: 9.3**

### 属性 17: 商品价格正数验证
*对于任意*小于等于零的价格值，系统应该拒绝并返回价格必须为正数的提示
**验证需求: 9.5**

### 属性 18: 未授权操作拒绝
*对于任意*用户尝试执行超出其权限的操作，系统应该返回403错误并拒绝执行
**验证需求: 10.3**

### 属性 19: 用户商品隔离性
*对于任意*用户，其"我的商品"列表中的所有商品都应该是该用户发布的商品
**验证需求: 6.3**

### 属性 20: 密码修改验证
*对于任意*用户，只有在提供正确的原密码时，新密码才应该被成功更新
**验证需求: 4.5**

## 错误处理

### 错误类型

1. **验证错误 (400 Bad Request)**
   - 必填字段缺失
   - 数据格式不正确
   - 业务规则违反（如价格为负数）

2. **认证错误 (401 Unauthorized)**
   - 未登录或令牌无效
   - 令牌过期

3. **授权错误 (403 Forbidden)**
   - 用户权限不足
   - 账号被禁用

4. **资源不存在 (404 Not Found)**
   - 请求的商品、订单或用户不存在

5. **服务器错误 (500 Internal Server Error)**
   - 数据库连接失败
   - 未预期的系统异常

### 错误响应格式

```json
{
  "timestamp": "2024-12-08T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "商品价格必须为正数",
  "path": "/api/products"
}
```

### 错误处理策略

1. **全局异常处理器**: 使用`@ControllerAdvice`统一处理所有异常
2. **日志记录**: 所有错误都应该记录到日志系统，包含请求上下文
3. **用户友好提示**: 向用户显示清晰、可操作的错误信息
4. **敏感信息保护**: 不向客户端暴露系统内部实现细节
5. **事务回滚**: 数据库操作失败时自动回滚事务

## 测试策略

### 双重测试方法

系统将采用单元测试和基于属性的测试相结合的方法：

- **单元测试**验证特定示例、边缘情况和错误条件
- **基于属性的测试**验证应该在所有输入上成立的通用属性
- 两者互补：单元测试捕获具体错误，属性测试验证通用正确性

### 单元测试

**测试框架**: JUnit 5 + Mockito

**测试范围**:
- Service层业务逻辑
- Controller层API端点
- Repository层数据访问
- 工具类和验证器

**示例测试**:
- 用户注册成功场景
- 商品创建失败场景（缺少必填字段）
- 订单创建时商品已售出的冲突场景
- 权限验证失败场景

### 基于属性的测试

**测试框架**: jqwik (Java的属性测试库)

**配置要求**:
- 每个属性测试至少运行100次迭代
- 每个属性测试必须用注释明确引用设计文档中的正确性属性
- 注释格式: `// Feature: secondhand-electronics-trading, Property {number}: {property_text}`

**测试范围**:
- 实现设计文档中定义的所有20个正确性属性
- 每个正确性属性对应一个独立的属性测试

**示例属性测试**:
```java
@Property
// Feature: secondhand-electronics-trading, Property 8: 商品价格排序正确性
void productPriceSortingIsCorrect(@ForAll List<Product> products) {
    List<Product> sorted = productService.sortByPrice(products, "asc");
    
    for (int i = 0; i < sorted.size() - 1; i++) {
        assertTrue(sorted.get(i).getPrice()
            .compareTo(sorted.get(i + 1).getPrice()) <= 0);
    }
}
```

### 集成测试

**测试框架**: Spring Boot Test + TestContainers

**测试范围**:
- API端到端流程
- 数据库集成
- 认证和授权流程

### 前端测试

**测试框架**: Vitest + Vue Test Utils

**测试范围**:
- 组件单元测试
- 用户交互测试
- 路由导航测试

## 安全考虑

### 认证和授权

1. **JWT令牌认证**
   - 用户登录后颁发JWT令牌
   - 令牌有效期24小时
   - 刷新令牌机制

2. **角色基础访问控制 (RBAC)**
   - USER角色: 普通用户权限
   - ADMIN角色: 管理员权限
   - 使用Spring Security实现

### 数据安全

1. **密码加密**: 使用BCrypt加密存储密码
2. **SQL注入防护**: 使用JPA参数化查询
3. **XSS防护**: 前端输入验证和输出转义
4. **CSRF防护**: 使用CSRF令牌

### 文件上传安全

1. **文件类型验证**: 只允许图片格式（jpg, png, gif）
2. **文件大小限制**: 单张图片不超过5MB
3. **文件名处理**: 重命名上传文件，防止路径遍历攻击

## 性能考虑

### 数据库优化

1. **索引策略**
   - 用户表: username, email
   - 商品表: seller_id, category_id, status
   - 订单表: buyer_id, seller_id, order_number

2. **分页查询**: 所有列表接口使用分页，默认每页20条

3. **懒加载**: 使用JPA懒加载减少不必要的关联查询

### 缓存策略

1. **商品分类缓存**: 使用Spring Cache缓存分类列表
2. **热门商品缓存**: 缓存首页展示的商品列表

### 前端优化

1. **图片懒加载**: 商品列表图片按需加载
2. **路由懒加载**: 按需加载页面组件
3. **API请求防抖**: 搜索输入使用防抖减少请求

## 部署架构

### 开发环境

```
前端: npm run dev (Vite开发服务器)
后端: mvn spring-boot:run
数据库: MySQL 8.x (本地或Docker)
```

### 生产环境

```
前端: Nginx静态文件服务
后端: Spring Boot JAR + JVM
数据库: MySQL 8.x (独立服务器)
反向代理: Nginx
```

### 环境配置

使用Spring Profiles管理不同环境配置：
- `application-dev.yml`: 开发环境
- `application-prod.yml`: 生产环境

## 已实现功能

1. ✅ **实时聊天**: 买卖双方可以实时沟通，包含聊天列表和聊天窗口
2. ✅ **现代化UI**: 采用glassmorphism设计，渐变背景，响应式布局
3. ✅ **完整的管理后台**: 用户管理、商品审核、订单处理等功能
4. ✅ **安全认证**: JWT令牌认证，角色权限控制
5. ✅ **错误处理**: 全局异常处理，友好的错误提示

## 未来扩展

1. **支付集成**: 集成第三方支付平台（支付宝、微信支付）
2. **消息通知**: 实现邮件通知和短信通知
3. **评价系统**: 买家对卖家和商品的评价功能
4. **推荐系统**: 基于用户行为的商品推荐算法
5. **移动端App**: 开发React Native或Flutter移动应用
6. **实时通知**: 使用WebSocket实现实时消息推送
7. **数据分析**: 添加用户行为分析和商品销售统计
