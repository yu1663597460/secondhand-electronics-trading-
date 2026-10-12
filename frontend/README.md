# 二手电子产品交易系统 - 前端

## 技术栈

- Vue 3
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发环境运行

```bash
npm run dev
```

访问 http://localhost:5173

### 生产环境构建

```bash
npm run build
```

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API接口
│   ├── stores/           # Pinia状态管理
│   ├── router/           # 路由配置
│   ├── views/            # 页面组件
│   ├── utils/            # 工具函数
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html
├── vite.config.js
└── package.json
```

## 功能模块

### 用户模块
- 用户注册/登录
- 个人信息管理
- 收货地址管理

### 商品模块
- 商品浏览
- 商品搜索和筛选
- 商品详情
- 发布商品
- 我的商品管理

### 订单模块
- 创建订单
- 订单列表
- 订单详情
- 确认收货

### 管理员模块
- 用户管理
- 商品审核
- 订单管理
- 分类管理

## 注意事项

- 确保后端服务已启动（默认端口8080）
- 前端开发服务器会自动代理API请求到后端
