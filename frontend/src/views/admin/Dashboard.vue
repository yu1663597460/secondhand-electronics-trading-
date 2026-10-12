<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>仪表盘</h1>
      <div class="breadcrumb">管理后台 / 仪表盘</div>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="refreshData" :loading="loading">刷新数据</el-button>
        <el-button type="primary" :icon="Download" @click="exportReport">导出报表</el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" @click="navigateTo('/admin/users')">
        <div class="stat-info">
          <h3>{{ stats.totalUsers || 0 }}</h3>
          <p>总用户数</p>
          <div class="stat-trend">
            <el-icon color="#52c41a"><CaretTop /></el-icon>
            <span class="trend-text">+{{ stats.newUsersToday || 0 }} 今日新增</span>
          </div>
        </div>
        <el-icon class="stat-icon" color="#1890ff"><User /></el-icon>
      </div>
      
      <div class="stat-card" @click="navigateTo('/admin/products')">
        <div class="stat-info">
          <h3>{{ stats.totalProducts || 0 }}</h3>
          <p>商品总数</p>
          <div class="stat-trend">
            <el-icon color="#faad14"><Clock /></el-icon>
            <span class="trend-text">{{ stats.pendingProducts || 0 }} 待审核</span>
          </div>
        </div>
        <el-icon class="stat-icon" color="#52c41a"><Goods /></el-icon>
      </div>
      
      <div class="stat-card" @click="navigateTo('/admin/orders')">
        <div class="stat-info">
          <h3>{{ stats.totalOrders || 0 }}</h3>
          <p>订单总数</p>
          <div class="stat-trend">
            <el-icon color="#52c41a"><CaretTop /></el-icon>
            <span class="trend-text">¥{{ formatMoney(stats.totalRevenue || 0) }} 总收入</span>
          </div>
        </div>
        <el-icon class="stat-icon" color="#f5222d"><Document /></el-icon>
      </div>
      
      <div class="stat-card">
        <div class="stat-info">
          <h3>{{ stats.activeUsers || 0 }}</h3>
          <p>活跃用户</p>
          <div class="stat-trend">
            <el-icon color="#52c41a"><CaretTop /></el-icon>
            <span class="trend-text">{{ stats.onlineUsers || 0 }} 在线</span>
          </div>
        </div>
        <el-icon class="stat-icon" color="#722ed1"><UserFilled /></el-icon>
      </div>
    </div>
    
    <!-- 快速操作 -->
    <div class="quick-actions">
      <div class="content-card">
        <div class="card-header">
          <h3>快速操作</h3>
        </div>
        <div class="actions-grid">
          <div class="action-item" @click="navigateTo('/admin/products')">
            <el-icon size="24" color="#faad14"><Clock /></el-icon>
            <div class="action-info">
              <h4>商品审核</h4>
              <p>{{ stats.pendingProducts || 0 }} 个商品待审核</p>
            </div>
            <el-icon class="action-arrow"><ArrowRight /></el-icon>
          </div>
          
          <div class="action-item" @click="navigateTo('/admin/users')">
            <el-icon size="24" color="#1890ff"><User /></el-icon>
            <div class="action-info">
              <h4>用户管理</h4>
              <p>管理系统用户</p>
            </div>
            <el-icon class="action-arrow"><ArrowRight /></el-icon>
          </div>
          
          <div class="action-item" @click="navigateTo('/admin/categories')">
            <el-icon size="24" color="#52c41a"><Menu /></el-icon>
            <div class="action-info">
              <h4>分类管理</h4>
              <p>管理商品分类</p>
            </div>
            <el-icon class="action-arrow"><ArrowRight /></el-icon>
          </div>
          
          <div class="action-item" @click="navigateTo('/admin/settings')">
            <el-icon size="24" color="#722ed1"><Setting /></el-icon>
            <div class="action-info">
              <h4>系统设置</h4>
              <p>配置系统参数</p>
            </div>
            <el-icon class="action-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 数据概览 -->
    <el-row :gutter="24">
      <el-col :span="16">
        <div class="content-card">
          <div class="card-header">
            <h3>系统概览</h3>
            <el-radio-group v-model="overviewPeriod" size="small">
              <el-radio-button label="today">今日</el-radio-button>
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
            </el-radio-group>
          </div>
          <div class="overview-grid">
            <div class="overview-item">
              <div class="overview-label">新增用户</div>
              <div class="overview-value">{{ overviewData.newUsers || 0 }}</div>
              <div class="overview-change positive">+{{ overviewData.userGrowth || 0 }}%</div>
            </div>
            <div class="overview-item">
              <div class="overview-label">新增商品</div>
              <div class="overview-value">{{ overviewData.newProducts || 0 }}</div>
              <div class="overview-change positive">+{{ overviewData.productGrowth || 0 }}%</div>
            </div>
            <div class="overview-item">
              <div class="overview-label">新增订单</div>
              <div class="overview-value">{{ overviewData.newOrders || 0 }}</div>
              <div class="overview-change positive">+{{ overviewData.orderGrowth || 0 }}%</div>
            </div>
            <div class="overview-item">
              <div class="overview-label">交易金额</div>
              <div class="overview-value">¥{{ formatMoney(overviewData.revenue || 0) }}</div>
              <div class="overview-change positive">+{{ overviewData.revenueGrowth || 0 }}%</div>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="8">
        <div class="content-card">
          <div class="card-header">
            <h3>系统状态</h3>
          </div>
          <div class="system-status">
            <div class="status-item">
              <div class="status-indicator online"></div>
              <div class="status-info">
                <div class="status-label">数据库</div>
                <div class="status-value">正常运行</div>
              </div>
            </div>
            <div class="status-item">
              <div class="status-indicator online"></div>
              <div class="status-info">
                <div class="status-label">缓存服务</div>
                <div class="status-value">正常运行</div>
              </div>
            </div>
            <div class="status-item">
              <div class="status-indicator online"></div>
              <div class="status-info">
                <div class="status-label">文件存储</div>
                <div class="status-value">正常运行</div>
              </div>
            </div>
            <div class="status-item">
              <div class="status-indicator warning"></div>
              <div class="status-info">
                <div class="status-label">邮件服务</div>
                <div class="status-value">配置中</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 最近活动和待办事项 -->
    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :span="14">
        <div class="content-card">
          <div class="card-header">
            <h3>最近活动</h3>
            <el-button size="small" text @click="viewAllLogs">查看全部</el-button>
          </div>
          <div class="activity-list">
            <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
              <div class="activity-avatar">
                <el-avatar :size="32" :icon="User">{{ activity.user?.charAt(0) }}</el-avatar>
              </div>
              <div class="activity-content">
                <div class="activity-title">
                  <strong>{{ activity.user }}</strong> {{ activity.action }}
                </div>
                <div class="activity-description">{{ activity.description }}</div>
                <div class="activity-time">{{ formatTime(activity.time) }}</div>
              </div>
              <div class="activity-status">
                <el-tag :type="getStatusType(activity.status)" size="small">
                  {{ getStatusText(activity.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="10">
        <div class="content-card">
          <div class="card-header">
            <h3>待办事项</h3>
            <el-badge :value="todoList.length" class="todo-badge">
              <el-icon><Bell /></el-icon>
            </el-badge>
          </div>
          <div class="todo-list">
            <div v-for="todo in todoList" :key="todo.id" class="todo-item" :class="{ urgent: todo.urgent }">
              <div class="todo-content">
                <div class="todo-title">{{ todo.title }}</div>
                <div class="todo-description">{{ todo.description }}</div>
              </div>
              <div class="todo-actions">
                <el-button size="small" type="primary" @click="handleTodo(todo)">
                  处理
                </el-button>
              </div>
            </div>
            
            <div v-if="todoList.length === 0" class="empty-todo">
              <el-icon size="48" color="#d9d9d9"><Check /></el-icon>
              <p>暂无待办事项</p>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, Goods, Clock, Document, UserFilled, Refresh, Download,
  CaretTop, ArrowRight, Menu, Setting, Bell, Check
} from '@element-plus/icons-vue'
import { getUserStatistics, getPendingProductCount } from '../../api/admin'

const router = useRouter()
const userStore = useUserStore()

const stats = ref({})
const recentActivities = ref([])
const todoList = ref([])
const overviewData = ref({})
const overviewPeriod = ref('today')
const loading = ref(false)
let timer = null

const loadDashboardData = async () => {
  if (!userStore.isAdmin) return
  try {
    loading.value = true
    
    // 加载用户统计
    const userStats = await getUserStatistics()
    const pendingCount = await getPendingProductCount()
    
    stats.value = {
      totalUsers: userStats.totalUsers || 0,
      totalProducts: 45, // 模拟数据
      pendingProducts: pendingCount || 0,
      totalOrders: 128, // 模拟数据
      activeUsers: Math.floor((userStats.totalUsers || 0) * 0.3),
      onlineUsers: Math.floor((userStats.totalUsers || 0) * 0.1),
      newUsersToday: 5,
      totalRevenue: 25680.50
    }
    
    // 模拟概览数据
    overviewData.value = {
      newUsers: overviewPeriod.value === 'today' ? 5 : overviewPeriod.value === 'week' ? 32 : 156,
      newProducts: overviewPeriod.value === 'today' ? 8 : overviewPeriod.value === 'week' ? 45 : 203,
      newOrders: overviewPeriod.value === 'today' ? 12 : overviewPeriod.value === 'week' ? 78 : 342,
      revenue: overviewPeriod.value === 'today' ? 1250.80 : overviewPeriod.value === 'week' ? 8960.50 : 35420.30,
      userGrowth: 12.5,
      productGrowth: 8.3,
      orderGrowth: 15.2,
      revenueGrowth: 22.1
    }
    
    // 模拟最近活动数据
    recentActivities.value = [
      {
        id: 1,
        time: new Date(Date.now() - 5 * 60 * 1000),
        user: 'admin',
        action: '审核了商品',
        description: 'iPhone 14 Pro Max 256GB 深空黑色',
        status: 'success'
      },
      {
        id: 2,
        time: new Date(Date.now() - 15 * 60 * 1000),
        user: 'testuser',
        action: '发布了商品',
        description: 'MacBook Pro 13寸 M2芯片',
        status: 'pending'
      },
      {
        id: 3,
        time: new Date(Date.now() - 30 * 60 * 1000),
        user: 'admin',
        action: '更新了系统设置',
        description: '修改了商品审核规则',
        status: 'success'
      },
      {
        id: 4,
        time: new Date(Date.now() - 45 * 60 * 1000),
        user: 'user123',
        action: '注册了账号',
        description: '新用户注册',
        status: 'success'
      }
    ]
    
    // 模拟待办事项
    todoList.value = [
      {
        id: 1,
        title: '商品审核',
        description: `${pendingCount || 0} 个商品等待审核`,
        urgent: (pendingCount || 0) > 5,
        action: 'products'
      },
      {
        id: 2,
        title: '用户举报',
        description: '2 个用户举报待处理',
        urgent: true,
        action: 'reports'
      },
      {
        id: 3,
        title: '系统更新',
        description: '检查系统更新和安全补丁',
        urgent: false,
        action: 'system'
      }
    ].filter(todo => {
      if (todo.action === 'products' && (pendingCount || 0) === 0) return false
      return true
    })
    
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
    // 静默处理，不弹错误提示
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadDashboardData()
  ElMessage.success('数据已刷新')
}

const exportReport = () => {
  ElMessage.info('报表导出功能开发中...')
}

const navigateTo = (path) => {
  router.push(path)
}

const viewAllLogs = () => {
  router.push('/admin/logs')
}

const handleTodo = (todo) => {
  switch (todo.action) {
    case 'products':
      router.push('/admin/products')
      break
    case 'reports':
      ElMessage.info('用户举报处理功能开发中...')
      break
    case 'system':
      router.push('/admin/settings')
      break
    default:
      ElMessage.info('功能开发中...')
  }
}

const formatMoney = (amount) => {
  return new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount)
}

const formatTime = (time) => {
  const now = new Date()
  const diff = now - new Date(time)
  const minutes = Math.floor(diff / 60000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (minutes < 1440) return `${Math.floor(minutes / 60)}小时前`
  return new Date(time).toLocaleDateString('zh-CN')
}

const getStatusType = (status) => {
  const types = {
    success: 'success',
    pending: 'warning',
    failed: 'danger',
    processing: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    success: '成功',
    pending: '待处理',
    failed: '失败',
    processing: '处理中'
  }
  return texts[status] || '未知'
}

// 监听概览周期变化
watch(overviewPeriod, () => {
  loadDashboardData()
})

onMounted(() => {
  if (userStore.isAdmin) {
    loadDashboardData()
    timer = setInterval(loadDashboardData, 30000)
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background: transparent;
  min-height: calc(100vh - 70px);
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  padding: 32px;
  margin-bottom: 32px;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: space-between;
  animation: fadeInUp 0.6s ease-out;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.5px;
}

.breadcrumb {
  margin-top: 4px;
  color: #8c8c8c;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  padding: 32px;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-info h3 {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #262626;
}

.stat-info p {
  color: #8c8c8c;
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
}

.trend-text {
  font-size: 12px;
  color: #52c41a;
  font-weight: 500;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.8;
}

.quick-actions {
  margin-bottom: 24px;
}

.actions-grid {
  padding: 24px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.action-item:hover {
  background: #f0f9ff;
  border-color: #91d5ff;
}

.action-info {
  flex: 1;
  margin: 0 16px;
}

.action-info h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.action-info p {
  margin: 0;
  font-size: 12px;
  color: #8c8c8c;
}

.action-arrow {
  color: #bfbfbf;
}

.content-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
  animation: fadeInUp 0.6s ease-out;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.overview-grid {
  padding: 24px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
}

.overview-item {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  background: #fafafa;
}

.overview-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.overview-value {
  font-size: 24px;
  font-weight: 700;
  color: #262626;
  margin-bottom: 8px;
}

.overview-change {
  font-size: 12px;
  font-weight: 500;
}

.overview-change.positive {
  color: #52c41a;
}

.overview-change.negative {
  color: #ff4d4f;
}

.system-status {
  padding: 24px;
}

.status-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.status-item:last-child {
  border-bottom: none;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 12px;
}

.status-indicator.online {
  background: #52c41a;
}

.status-indicator.warning {
  background: #faad14;
}

.status-indicator.offline {
  background: #ff4d4f;
}

.status-info {
  flex: 1;
}

.status-label {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
}

.status-value {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 2px;
}

.activity-list {
  padding: 24px;
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-avatar {
  margin-right: 12px;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  color: #262626;
  margin-bottom: 4px;
}

.activity-description {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #bfbfbf;
}

.activity-status {
  margin-left: 12px;
}

.todo-list {
  padding: 24px;
  max-height: 400px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  border-radius: 8px;
  background: #fafafa;
  border-left: 4px solid #d9d9d9;
}

.todo-item.urgent {
  border-left-color: #ff4d4f;
  background: #fff2f0;
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.todo-description {
  font-size: 13px;
  color: #8c8c8c;
}

.todo-actions {
  margin-left: 12px;
}

.empty-todo {
  text-align: center;
  padding: 40px 20px;
  color: #bfbfbf;
}

.empty-todo p {
  margin-top: 12px;
  font-size: 14px;
}

.todo-badge {
  margin-left: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
  
  .overview-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>