# 二手电子产品交易系统

一个基于Spring Boot + Vue 3的全栈二手电子产品交易平台。

## 技术栈

### 后端
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security + JWT
- MySQL 8.x
- Maven

### 前端
- Vue 3
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios

## 功能特性

### 用户功能
- ✅ 用户注册/登录
- ✅ 个人信息管理
- ✅ 实名认证
- ✅ 收货地址管理
- ✅ 商品浏览和搜索
- ✅ 商品分类筛选
- ✅ 价格排序
- ✅ 发布商品
- ✅ 管理我的商品
- ✅ 创建订单
- ✅ 订单管理
- ✅ 确认收货

### 管理员功能
- ✅ 用户管理（启用/禁用账号）
- ✅ 商品审核（批准/拒绝）
- ✅ 商品下架
- ✅ 订单管理
- ✅ 商品分类管理

## 快速开始

### 前置要求

- JDK 17+
- Node.js 16+
- MySQL 8.x
- Maven 3.6+

### 1. 克隆项目

```bash
git clone <repository-url>
cd secondhand-electronics-trading
```

### 2. 初始化数据库

```bash
# 使用MySQL命令行
mysql -u root -p < database/init.sql

# 或者手动执行database/init.sql中的SQL语句
```

### 3. 配置后端

编辑 `backend/src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/secondhand_electronics
    username: root
    password: your_password
```

### 4. 启动项目

#### Windows系统

**方式一：使用批处理脚本**
```bash
start.bat
```

**方式二：使用PowerShell脚本**
```powershell
.\start.ps1
```

#### 手动启动

**启动后端：**
```bash
cd backend
mvn spring-boot:run
```

**启动前端：**
```bash
cd frontend
npm install
npm run dev
```

### 5. 访问系统

- 前端地址: http://localhost:5173
- 后端API: http://localhost:8080/api

## 默认账号

### 管理员账号
- 用户名: `admin`
- 密码: `admin123`

### 测试用户账号
- 用户名: `testuser`
- 密码: `user123456`

## 项目结构

```
secondhand-electronics-trading/
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/secondhand/electronics/
│   │   │   │       ├── entity/          # 实体类
│   │   │   │       ├── repository/      # 数据访问层
│   │   │   │       ├── service/         # 业务逻辑层
│   │   │   │       ├── controller/      # 控制器层
│   │   │   │       ├── dto/             # 数据传输对象
│   │   │   │       ├── config/          # 配置类
│   │   │   │       ├── security/        # 安全相关
│   │   │   │       └── exception/       # 异常处理
│   │   │   └── resources/
│   │   │       └── application.yml      # 配置文件
│   │   └── test/                        # 测试代码
│   ├── pom.xml                          # Maven配置
│   └── README.md
│
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── api/            # API接口
│   │   ├── stores/         # 状态管理
│   │   ├── router/         # 路由配置
│   │   ├── views/          # 页面组件
│   │   ├── utils/          # 工具函数
│   │   ├── App.vue         # 根组件
│   │   └── main.js         # 入口文件
│   ├── package.json
│   ├── vite.config.js
│   └── README.md
│
├── database/               # 数据库脚本
│   ├── init.sql           # 初始化脚本
│   └── README.md
│
├── .kiro/                 # 项目规格文档
│   └── specs/
│       └── secondhand-electronics-trading/
│           ├── requirements.md  # 需求文档
│           ├── design.md        # 设计文档
│           └── tasks.md         # 任务列表
│
├── start.bat              # Windows启动脚本
├── start.ps1              # PowerShell启动脚本
└── README.md              # 项目说明
```

## API文档

### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 用户相关
- `GET /api/users/profile` - 获取用户信息
- `PUT /api/users/profile` - 更新用户信息
- `PUT /api/users/password` - 修改密码
- `POST /api/users/verification` - 提交实名认证

### 商品相关
- `GET /api/products` - 浏览商品列表
- `GET /api/products/{id}` - 获取商品详情
- `GET /api/products/my` - 获取我的商品
- `POST /api/products` - 发布商品
- `PUT /api/products/{id}` - 编辑商品
- `PUT /api/products/{id}/status` - 更新商品状态

### 订单相关
- `POST /api/orders` - 创建订单
- `GET /api/orders/my` - 获取我的订单
- `GET /api/orders/{id}` - 获取订单详情
- `PUT /api/orders/{id}/confirm` - 确认收货

### 地址相关
- `GET /api/addresses` - 获取地址列表
- `POST /api/addresses` - 添加地址
- `PUT /api/addresses/{id}` - 编辑地址
- `DELETE /api/addresses/{id}` - 删除地址
- `PUT /api/addresses/{id}/default` - 设置默认地址

### 管理员相关
- `GET /api/admin/users` - 获取用户列表
- `PUT /api/admin/users/{id}/status` - 更新用户状态
- `GET /api/admin/products` - 获取所有商品
- `PUT /api/admin/products/{id}/review` - 审核商品
- `PUT /api/admin/products/{id}/takedown` - 下架商品
- `GET /api/admin/categories` - 获取分类列表
- `POST /api/admin/categories` - 添加分类
- `GET /api/admin/orders` - 获取所有订单
- `PUT /api/admin/orders/{id}/note` - 添加订单备注

## 开发说明

### 后端开发

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 前端开发

```bash
cd frontend
npm run dev
```

### 构建生产版本

**后端：**
```bash
cd backend
mvn clean package
```

**前端：**
```bash
cd frontend
npm run build
```

## 常见问题

### 1. 后端启动失败

- 检查MySQL服务是否启动
- 检查数据库连接配置是否正确
- 确认已执行数据库初始化脚本

### 2. 前端无法连接后端

- 确认后端服务已启动
- 检查端口8080是否被占用
- 查看浏览器控制台错误信息

### 3. JWT令牌过期

- 默认令牌有效期为24小时
- 可在 `application.yml` 中修改 `jwt.expiration` 配置

## 许可证

MIT License

## 联系方式

如有问题，请提交Issue或联系开发团队。
