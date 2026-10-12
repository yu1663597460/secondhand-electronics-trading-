<template>
  <div class="statistics">
    <div class="page-header">
      <h1>数据统计</h1>
      <div class="breadcrumb">管理后台 / 数据统计</div>
      <el-button type="primary" @click="loadAll" :loading="loading" style="margin-left: auto">
        <el-icon><Refresh /></el-icon> 刷新数据
      </el-button>
    </div>

    <!-- 用户统计 -->
    <div class="section-title">用户统计</div>
    <div class="stats-grid" v-loading="loading">
      <div class="stat-card blue">
        <div class="stat-info">
          <h3>{{ userStats.totalUsers || 0 }}</h3>
          <p>用户总数</p>
        </div>
        <el-icon class="stat-icon"><User /></el-icon>
      </div>
      <div class="stat-card green">
        <div class="stat-info">
          <h3>{{ userStats.activeUsers || 0 }}</h3>
          <p>活跃用户</p>
        </div>
        <el-icon class="stat-icon"><UserFilled /></el-icon>
      </div>
      <div class="stat-card orange">
        <div class="stat-info">
          <h3>{{ userStats.newUsersLast30Days || 0 }}</h3>
          <p>30天新增</p>
        </div>
        <el-icon class="stat-icon"><TrendCharts /></el-icon>
      </div>
      <div class="stat-card red">
        <div class="stat-info">
          <h3>{{ userStats.disabledUsers || 0 }}</h3>
          <p>禁用用户</p>
        </div>
        <el-icon class="stat-icon"><CircleClose /></el-icon>
      </div>
    </div>

    <!-- 商品统计 -->
    <div class="section-title">商品统计</div>
    <div class="stats-grid" v-loading="loading">
      <div class="stat-card blue">
        <div class="stat-info">
          <h3>{{ productStats.total || 0 }}</h3>
          <p>商品总数</p>
        </div>
        <el-icon class="stat-icon"><Goods /></el-icon>
      </div>
      <div class="stat-card orange">
        <div class="stat-info">
          <h3>{{ productStats.pending || 0 }}</h3>
          <p>待审核</p>
        </div>
        <el-icon class="stat-icon"><Clock /></el-icon>
      </div>
      <div class="stat-card green">
        <div class="stat-info">
          <h3>{{ productStats.approved || 0 }}</h3>
          <p>在售商品</p>
        </div>
        <el-icon class="stat-icon"><Select /></el-icon>
      </div>
      <div class="stat-card purple">
        <div class="stat-info">
          <h3>{{ productStats.sold || 0 }}</h3>
          <p>已售出</p>
        </div>
        <el-icon class="stat-icon"><ShoppingCart /></el-icon>
      </div>
    </div>

    <!-- 订单统计 -->
    <div class="section-title">订单统计</div>
    <div class="stats-grid" v-loading="loading">
      <div class="stat-card blue">
        <div class="stat-info">
          <h3>{{ orderStats.total || 0 }}</h3>
          <p>订单总数</p>
        </div>
        <el-icon class="stat-icon"><List /></el-icon>
      </div>
      <div class="stat-card green">
        <div class="stat-info">
          <h3>{{ orderStats.completed || 0 }}</h3>
          <p>已完成</p>
        </div>
        <el-icon class="stat-icon"><CircleCheck /></el-icon>
      </div>
      <div class="stat-card red">
        <div class="stat-info">
          <h3>{{ orderStats.cancelled || 0 }}</h3>
          <p>已取消</p>
        </div>
        <el-icon class="stat-icon"><CircleClose /></el-icon>
      </div>
      <div class="stat-card orange">
        <div class="stat-info">
          <h3>{{ completionRate }}%</h3>
          <p>完成率</p>
        </div>
        <el-icon class="stat-icon"><TrendCharts /></el-icon>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <!-- 商品状态分布 -->
      <div class="chart-card">
        <h3>商品状态分布</h3>
        <div class="bar-chart">
          <div v-for="item in productChartData" :key="item.label" class="bar-item">
            <div class="bar-label">{{ item.label }}</div>
            <div class="bar-track">
              <div 
                class="bar-fill" 
                :style="{ width: item.percent + '%', background: item.color }"
              ></div>
            </div>
            <div class="bar-value">{{ item.value }}</div>
          </div>
        </div>
      </div>

      <!-- 订单状态分布 -->
      <div class="chart-card">
        <h3>订单状态分布</h3>
        <div class="bar-chart">
          <div v-for="item in orderChartData" :key="item.label" class="bar-item">
            <div class="bar-label">{{ item.label }}</div>
            <div class="bar-track">
              <div 
                class="bar-fill" 
                :style="{ width: item.percent + '%', background: item.color }"
              ></div>
            </div>
            <div class="bar-value">{{ item.value }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  User, UserFilled, TrendCharts, CircleClose, Goods, Clock, 
  Select, ShoppingCart, List, CircleCheck, Refresh
} from '@element-plus/icons-vue'
import { getUserStatistics, getProducts, getOrders } from '../../api/admin'

const loading = ref(false)
const userStats = ref({})
const productStats = ref({ total: 0, pending: 0, approved: 0, sold: 0, rejected: 0 })
const orderStats = ref({ total: 0, completed: 0, cancelled: 0, created: 0, confirmed: 0 })

const completionRate = computed(() => {
  if (!orderStats.value.total) return 0
  return Math.round((orderStats.value.completed / orderStats.value.total) * 100)
})

const productChartData = computed(() => {
  const total = productStats.value.total || 1
  return [
    { label: '在售', value: productStats.value.approved || 0, color: '#52c41a', percent: Math.round(((productStats.value.approved || 0) / total) * 100) },
    { label: '待审核', value: productStats.value.pending || 0, color: '#faad14', percent: Math.round(((productStats.value.pending || 0) / total) * 100) },
    { label: '已售出', value: productStats.value.sold || 0, color: '#1890ff', percent: Math.round(((productStats.value.sold || 0) / total) * 100) },
    { label: '已拒绝', value: productStats.value.rejected || 0, color: '#f5222d', percent: Math.round(((productStats.value.rejected || 0) / total) * 100) },
  ]
})

const orderChartData = computed(() => {
  const total = orderStats.value.total || 1
  return [
    { label: '已完成', value: orderStats.value.completed || 0, color: '#52c41a', percent: Math.round(((orderStats.value.completed || 0) / total) * 100) },
    { label: '已确认', value: orderStats.value.confirmed || 0, color: '#1890ff', percent: Math.round(((orderStats.value.confirmed || 0) / total) * 100) },
    { label: '已创建', value: orderStats.value.created || 0, color: '#faad14', percent: Math.round(((orderStats.value.created || 0) / total) * 100) },
    { label: '已取消', value: orderStats.value.cancelled || 0, color: '#f5222d', percent: Math.round(((orderStats.value.cancelled || 0) / total) * 100) },
  ]
})

const loadAll = async () => {
  loading.value = true
  try {
    const [userRes, productRes, orderRes] = await Promise.all([
      getUserStatistics(),
      getProducts({ page: 0, size: 1 }),
      getOrders({ page: 0, size: 1 })
    ])
    userStats.value = userRes

    // 分别获取各状态商品数量
    const [pendingRes, approvedRes, soldRes, rejectedRes] = await Promise.all([
      getProducts({ page: 0, size: 1, status: 'PENDING' }),
      getProducts({ page: 0, size: 1, status: 'APPROVED' }),
      getProducts({ page: 0, size: 1, status: 'SOLD' }),
      getProducts({ page: 0, size: 1, status: 'REJECTED' }),
    ])
    productStats.value = {
      total: productRes.totalElements || 0,
      pending: pendingRes.totalElements || 0,
      approved: approvedRes.totalElements || 0,
      sold: soldRes.totalElements || 0,
      rejected: rejectedRes.totalElements || 0,
    }

    // 分别获取各状态订单数量
    const [createdRes, confirmedRes, completedRes, cancelledRes] = await Promise.all([
      getOrders({ page: 0, size: 1, status: 'CREATED' }),
      getOrders({ page: 0, size: 1, status: 'CONFIRMED' }),
      getOrders({ page: 0, size: 1, status: 'COMPLETED' }),
      getOrders({ page: 0, size: 1, status: 'CANCELLED' }),
    ])
    orderStats.value = {
      total: orderRes.totalElements || 0,
      created: createdRes.totalElements || 0,
      confirmed: confirmedRes.totalElements || 0,
      completed: completedRes.totalElements || 0,
      cancelled: cancelledRes.totalElements || 0,
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.statistics {
  padding: 24px;
  background: #f5f5f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  background: white;
  padding: 16px 24px;
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-header h1 {
  font-size: 20px;
  font-weight: 500;
  color: #262626;
  margin: 0;
}

.breadcrumb {
  color: #8c8c8c;
  font-size: 14px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-left: 8px;
  border-left: 4px solid #1890ff;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 20px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-left: 4px solid transparent;
}

.stat-card.blue { border-left-color: #1890ff; }
.stat-card.green { border-left-color: #52c41a; }
.stat-card.orange { border-left-color: #faad14; }
.stat-card.red { border-left-color: #f5222d; }
.stat-card.purple { border-left-color: #722ed1; }

.stat-info h3 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 4px 0;
  color: #262626;
}

.stat-info p {
  color: #8c8c8c;
  margin: 0;
  font-size: 14px;
}

.stat-icon {
  font-size: 40px;
  opacity: 0.2;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.chart-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  padding: 24px;
}

.chart-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
}

.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bar-label {
  width: 60px;
  font-size: 13px;
  color: #606266;
  text-align: right;
  flex-shrink: 0;
}

.bar-track {
  flex: 1;
  height: 20px;
  background: #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.6s ease;
  min-width: 4px;
}

.bar-value {
  width: 40px;
  font-size: 13px;
  color: #303133;
  font-weight: 600;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>
