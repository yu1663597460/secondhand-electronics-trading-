# 设计文档 - 完整平台功能

## 概述

本设计文档详细说明了将二手电子产品交易平台从75%完成度提升到100%所需的技术实现方案。文档涵盖数据模型、API接口、前端组件和技术实现细节。

---

## 第一阶段: 核心功能完善 (P0)

### 1. 商品收藏功能

#### 数据模型

```java
@Entity
@Table(name = "favorites")
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // 唯一约束: 用户+商品
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "product_id"})
    })
}
```

#### API接口

**FavoriteController**

```java
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    
    // 添加收藏
    @PostMapping
    public ResponseEntity<ApiResponse> addFavorite(@RequestBody FavoriteDTO dto);
    
    // 取消收藏
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> removeFavorite(@PathVariable Long productId);
    
    // 获取收藏列表
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getFavorites(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    );
    
    // 检查是否已收藏
    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> isFavorited(@PathVariable Long productId);
}
```

#### 前端组件

**FavoriteList.vue**
- 收藏商品列表展示
- 商品卡片（图片、标题、价格、状态）
- 取消收藏按钮
- 商品状态标记（已售出/已下架）
- 分页加载

**ProductCard组件增强**
- 添加收藏按钮（心形图标）
- 收藏状态切换动画
- 收藏数量显示

---

### 2. 购物车功能

#### 数据模型

```java
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // 唯一约束: 用户+商品
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "product_id"})
    })
}
```

#### API接口

**CartController**

```java
@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    // 添加到购物车
    @PostMapping
    public ResponseEntity<ApiResponse> addToCart(@RequestBody CartItemDTO dto);
    
    // 从购物车移除
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> removeFromCart(@PathVariable Long productId);
    
    // 获取购物车列表
    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCartItems();
    
    // 清空购物车
    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart();
    
    // 批量结算
    @PostMapping("/checkout")
    public ResponseEntity<List<OrderDTO>> checkoutCart(
        @RequestBody CheckoutDTO dto
    );
}
```

#### 前端组件

**ShoppingCart.vue**
- 购物车商品列表
- 商品选择（复选框）
- 批量删除
- 总价计算
- 结算按钮
- 空购物车提示

**导航栏增强**
- 购物车图标
- 商品数量徽章
- 下拉预览

---

### 3. 支付流程

#### 数据模型

```java
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod method; // BALANCE, ALIPAY, WECHAT, MOCK
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PENDING, SUCCESS, FAILED, CANCELLED
    
    private String transactionId; // 第三方交易号
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    private LocalDateTime paidAt;
}

// 枚举类型
public enum PaymentMethod {
    BALANCE("余额支付"),
    ALIPAY("支付宝"),
    WECHAT("微信支付"),
    MOCK("模拟支付");
}

public enum PaymentStatus {
    PENDING("待支付"),
    SUCCESS("支付成功"),
    FAILED("支付失败"),
    CANCELLED("已取消");
}
```

#### API接口

**PaymentController**

```java
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    // 创建支付
    @PostMapping
    public ResponseEntity<PaymentDTO> createPayment(@RequestBody CreatePaymentDTO dto);
    
    // 执行支付
    @PostMapping("/{paymentId}/pay")
    public ResponseEntity<ApiResponse> executePayment(
        @PathVariable Long paymentId,
        @RequestBody PaymentExecuteDTO dto
    );
    
    // 查询支付状态
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDTO> getPaymentStatus(@PathVariable Long paymentId);
    
    // 取消支付
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse> cancelPayment(@PathVariable Long paymentId);
}
```

#### 业务逻辑

**PaymentService**

```java
@Service
public class PaymentService {
    
    // 创建支付记录
    public Payment createPayment(Order order, PaymentMethod method) {
        // 验证订单状态
        // 创建支付记录
        // 设置支付超时（30分钟）
    }
    
    // 执行支付
    @Transactional
    public void executePayment(Long paymentId, PaymentMethod method) {
        // 根据支付方式调用不同的支付处理
        switch(method) {
            case BALANCE: processBalancePayment(paymentId); break;
            case MOCK: processMockPayment(paymentId); break;
            // 其他支付方式...
        }
    }
    
    // 模拟支付
    private void processMockPayment(Long paymentId) {
        // 模拟支付成功
        // 更新支付状态
        // 更新订单状态
        // 发送通知
    }
    
    // 支付超时处理（定时任务）
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void handlePaymentTimeout() {
        // 查找超时未支付的订单
        // 取消支付和订单
        // 释放商品
    }
}
```

#### 前端组件

**Payment.vue**
- 订单信息展示
- 支付方式选择
- 支付金额显示
- 支付按钮
- 支付结果页面
- 倒计时提示

---

### 4. 交易评价系统

#### 数据模型

```java
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer; // 评价人
    
    @ManyToOne
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee; // 被评价人
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private Integer rating; // 1-5星
    
    @Column(columnDefinition = "TEXT")
    private String content; // 评价内容
    
    @ElementCollection
    private List<String> tags; // 评价标签（描述相符、物流快、态度好等）
    
    @Enumerated(EnumType.STRING)
    private ReviewType type; // BUYER_TO_SELLER, SELLER_TO_BUYER
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}

public enum ReviewType {
    BUYER_TO_SELLER("买家评价卖家"),
    SELLER_TO_BUYER("卖家评价买家");
}
```

#### API接口

**ReviewController**

```java
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    
    // 提交评价
    @PostMapping
    public ResponseEntity<ApiResponse> submitReview(@RequestBody ReviewDTO dto);
    
    // 获取商品评价列表
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewDTO>> getProductReviews(
        @PathVariable Long productId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    );
    
    // 获取用户收到的评价
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReviewDTO>> getUserReviews(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    );
    
    // 获取待评价订单列表
    @GetMapping("/pending")
    public ResponseEntity<List<OrderDTO>> getPendingReviews();
}
```

#### 前端组件

**ReviewForm.vue**
- 评分选择（星级）
- 评价内容输入
- 标签选择
- 提交按钮

**ReviewList.vue**
- 评价列表展示
- 评分显示
- 评价内容
- 评价时间
- 评价人信息

---

### 5. 信用体系

#### 数据模型

```java
// 扩展User实体
@Entity
public class User {
    // ... 现有字段
    
    @Column(nullable = false)
    private Integer creditScore = 100; // 信用分，初始100分
    
    @Enumerated(EnumType.STRING)
    private CreditLevel creditLevel = CreditLevel.NORMAL; // 信用等级
    
    private Integer totalTransactions = 0; // 总交易次数
    private Integer successfulTransactions = 0; // 成功交易次数
    private Integer violationCount = 0; // 违规次数
}

public enum CreditLevel {
    EXCELLENT("优秀", 90, 100),    // 90-100分
    GOOD("良好", 70, 89),          // 70-89分
    NORMAL("正常", 50, 69),        // 50-69分
    POOR("较差", 30, 49),          // 30-49分
    BAD("差", 0, 29);              // 0-29分
    
    private String label;
    private int minScore;
    private int maxScore;
}

@Entity
@Table(name = "credit_records")
public class CreditRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    private CreditChangeType type;
    
    private Integer scoreChange; // 分数变化（正数或负数）
    
    private String reason; // 变化原因
    
    @ManyToOne
    @JoinColumn(name = "related_order_id")
    private Order relatedOrder; // 关联订单
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}

public enum CreditChangeType {
    GOOD_REVIEW("+5", "收到好评"),
    BAD_REVIEW("-5", "收到差评"),
    TRANSACTION_SUCCESS("+2", "交易成功"),
    VIOLATION("-10", "违规行为"),
    COMPLAINT_CONFIRMED("-15", "投诉确认"),
    MANUAL_ADJUST("±N", "人工调整");
}
```

#### 业务逻辑

**CreditService**

```java
@Service
public class CreditService {
    
    // 计算信用分
    public void updateCreditScore(Long userId, CreditChangeType type, 
                                  Integer scoreChange, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 更新信用分
        int newScore = user.getCreditScore() + scoreChange;
        newScore = Math.max(0, Math.min(100, newScore)); // 限制在0-100
        user.setCreditScore(newScore);
        
        // 更新信用等级
        user.setCreditLevel(calculateCreditLevel(newScore));
        
        // 记录信用变化
        CreditRecord record = new CreditRecord();
        record.setUser(user);
        record.setType(type);
        record.setScoreChange(scoreChange);
        record.setReason(reason);
        creditRecordRepository.save(record);
        
        userRepository.save(user);
    }
    
    // 根据分数计算等级
    private CreditLevel calculateCreditLevel(int score) {
        for (CreditLevel level : CreditLevel.values()) {
            if (score >= level.getMinScore() && score <= level.getMaxScore()) {
                return level;
            }
        }
        return CreditLevel.NORMAL;
    }
    
    // 交易完成后更新信用
    @Transactional
    public void handleTransactionComplete(Order order) {
        // 更新买家和卖家的交易统计
        User buyer = order.getBuyer();
        User seller = order.getSeller();
        
        buyer.setTotalTransactions(buyer.getTotalTransactions() + 1);
        buyer.setSuccessfulTransactions(buyer.getSuccessfulTransactions() + 1);
        seller.setTotalTransactions(seller.getTotalTransactions() + 1);
        seller.setSuccessfulTransactions(seller.getSuccessfulTransactions() + 1);
        
        // 增加信用分
        updateCreditScore(buyer.getId(), CreditChangeType.TRANSACTION_SUCCESS, 
                         2, "完成交易");
        updateCreditScore(seller.getId(), CreditChangeType.TRANSACTION_SUCCESS, 
                         2, "完成交易");
    }
    
    // 评价后更新信用
    public void handleReviewSubmitted(Review review) {
        if (review.getRating() >= 4) {
            // 好评
            updateCreditScore(review.getReviewee().getId(), 
                            CreditChangeType.GOOD_REVIEW, 5, "收到好评");
        } else if (review.getRating() <= 2) {
            // 差评
            updateCreditScore(review.getReviewee().getId(), 
                            CreditChangeType.BAD_REVIEW, -5, "收到差评");
        }
    }
}
```

#### API接口

**CreditController**

```java
@RestController
@RequestMapping("/api/credit")
public class CreditController {
    
    // 获取用户信用信息
    @GetMapping("/user/{userId}")
    public ResponseEntity<CreditInfoDTO> getUserCredit(@PathVariable Long userId);
    
    // 获取信用变化记录
    @GetMapping("/records")
    public ResponseEntity<Page<CreditRecordDTO>> getCreditRecords(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    );
}
```

#### 前端组件

**CreditDisplay组件**
- 信用分显示
- 信用等级徽章
- 交易统计
- 信用权益说明

**CreditRecords.vue**
- 信用变化记录列表
- 时间线展示
- 分数变化标记

---

## 数据库索引优化

```sql
-- 收藏表索引
CREATE INDEX idx_favorites_user_id ON favorites(user_id);
CREATE INDEX idx_favorites_product_id ON favorites(product_id);
CREATE INDEX idx_favorites_created_at ON favorites(created_at);

-- 购物车表索引
CREATE INDEX idx_cart_items_user_id ON cart_items(user_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);

-- 支付表索引
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);

-- 评价表索引
CREATE INDEX idx_reviews_order_id ON reviews(order_id);
CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_reviewee_id ON reviews(reviewee_id);
CREATE INDEX idx_reviews_created_at ON reviews(created_at);

-- 信用记录表索引
CREATE INDEX idx_credit_records_user_id ON credit_records(user_id);
CREATE INDEX idx_credit_records_created_at ON credit_records(created_at);
```

---

## 前端路由配置

```javascript
// router/index.js 新增路由
const routes = [
  // ... 现有路由
  
  // 收藏相关
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/FavoriteList.vue'),
    meta: { requiresAuth: true }
  },
  
  // 购物车
  {
    path: '/cart',
    name: 'ShoppingCart',
    component: () => import('@/views/ShoppingCart.vue'),
    meta: { requiresAuth: true }
  },
  
  // 支付
  {
    path: '/payment/:orderId',
    name: 'Payment',
    component: () => import('@/views/Payment.vue'),
    meta: { requiresAuth: true }
  },
  
  // 评价
  {
    path: '/review/:orderId',
    name: 'ReviewForm',
    component: () => import('@/views/ReviewForm.vue'),
    meta: { requiresAuth: true }
  },
  
  // 信用中心
  {
    path: '/credit',
    name: 'CreditCenter',
    component: () => import('@/views/CreditCenter.vue'),
    meta: { requiresAuth: true }
  }
];
```

---

## API封装

```javascript
// api/favorite.js
export const favoriteAPI = {
  addFavorite: (productId) => request.post('/api/favorites', { productId }),
  removeFavorite: (productId) => request.delete(`/api/favorites/${productId}`),
  getFavorites: (page, size) => request.get('/api/favorites', { params: { page, size } }),
  isFavorited: (productId) => request.get(`/api/favorites/check/${productId}`)
};

// api/cart.js
export const cartAPI = {
  addToCart: (productId) => request.post('/api/cart', { productId }),
  removeFromCart: (productId) => request.delete(`/api/cart/${productId}`),
  getCartItems: () => request.get('/api/cart'),
  clearCart: () => request.delete('/api/cart'),
  checkout: (addressId, productIds) => request.post('/api/cart/checkout', { addressId, productIds })
};

// api/payment.js
export const paymentAPI = {
  createPayment: (orderId, method) => request.post('/api/payments', { orderId, method }),
  executePayment: (paymentId, method) => request.post(`/api/payments/${paymentId}/pay`, { method }),
  getPaymentStatus: (paymentId) => request.get(`/api/payments/${paymentId}`),
  cancelPayment: (paymentId) => request.post(`/api/payments/${paymentId}/cancel`)
};

// api/review.js
export const reviewAPI = {
  submitReview: (data) => request.post('/api/reviews', data),
  getProductReviews: (productId, page, size) => 
    request.get(`/api/reviews/product/${productId}`, { params: { page, size } }),
  getUserReviews: (userId, page, size) => 
    request.get(`/api/reviews/user/${userId}`, { params: { page, size } }),
  getPendingReviews: () => request.get('/api/reviews/pending')
};

// api/credit.js
export const creditAPI = {
  getUserCredit: (userId) => request.get(`/api/credit/user/${userId}`),
  getCreditRecords: (page, size) => request.get('/api/credit/records', { params: { page, size } })
};
```

---

## 错误处理

### 业务异常定义

```java
// 支付相关异常
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}

public class PaymentTimeoutException extends PaymentException {
    public PaymentTimeoutException() {
        super("支付超时，订单已取消");
    }
}

public class InsufficientBalanceException extends PaymentException {
    public InsufficientBalanceException() {
        super("余额不足");
    }
}

// 评价相关异常
public class ReviewException extends RuntimeException {
    public ReviewException(String message) {
        super(message);
    }
}

public class DuplicateReviewException extends ReviewException {
    public DuplicateReviewException() {
        super("该订单已评价");
    }
}
```

---

## 性能优化

### 1. 缓存策略

```java
@Service
public class ProductService {
    
    // 缓存热门商品
    @Cacheable(value = "hotProducts", key = "'hot_' + #category")
    public List<Product> getHotProducts(String category) {
        // 查询热门商品
    }
    
    // 缓存商品详情
    @Cacheable(value = "productDetail", key = "#productId")
    public Product getProductById(Long productId) {
        // 查询商品详情
    }
}
```

### 2. 数据库查询优化

```java
// 使用JOIN FETCH避免N+1问题
@Query("SELECT f FROM Favorite f " +
       "JOIN FETCH f.product p " +
       "JOIN FETCH p.seller " +
       "WHERE f.user.id = :userId")
List<Favorite> findByUserIdWithProduct(@Param("userId") Long userId);
```

---

## 测试策略

### 单元测试示例

```java
@SpringBootTest
class FavoriteServiceTest {
    
    @Test
    void testAddFavorite() {
        // 测试添加收藏
    }
    
    @Test
    void testDuplicateFavorite() {
        // 测试重复收藏
    }
    
    @Test
    void testRemoveFavorite() {
        // 测试取消收藏
    }
}
```

---

## 部署注意事项

1. **数据库迁移**: 使用Flyway或Liquibase管理数据库版本
2. **环境变量**: 支付相关配置使用环境变量
3. **定时任务**: 确保支付超时检查任务正常运行
4. **监控告警**: 添加支付失败率监控

---

## 下一步

完成第一阶段(P0)后，继续实现：
- 第二阶段(P1): 举报投诉、消息通知、物流跟踪、售后服务、数据统计
- 第三阶段(P2): 商品分享、搜索历史、浏览历史、商品推荐、操作日志
- 第四阶段(P3): 优惠券、用户余额、实时推送、系统设置、密码找回

