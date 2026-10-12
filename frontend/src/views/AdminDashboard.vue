<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h1>管理员控制台</h1>
      <p>欢迎使用二手电子产品交易平台管理系统</p>
    </div>

    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ userStats.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ userStats.activeUsers || 0 }}</div>
            <div class="stat-label">活跃用户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ userStats.newUsersLast30Days || 0 }}</div>
            <div class="stat-label">30天新增</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ userStats.disabledUsers || 0 }}</div>
            <div class="stat-label">禁用用户</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" class="admin-tabs">
      <el-tab-pane label="用户管理" name="users">
        <AdminUserManagement />
      </el-tab-pane>
      <el-tab-pane label="商品管理" name="products">
        <AdminProductManagement />
      </el-tab-pane>
      <el-tab-pane label="订单管理" name="orders">
        <div class="coming-soon">订单管理功能开发中...</div>
      </el-tab-pane>
      <el-tab-pane label="系统设置" name="settings">
        <div class="coming-soon">系统设置功能开发中...</div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getUserStatistics } from '../api/admin'
import AdminUserManagement from '../components/AdminUserManagement.vue'
import AdminProductManagement from '../components/AdminProductManagement.vue'

const activeTab = ref('users')
const userStats = ref({})

const loadUserStatistics = async () => {
  try {
    const response = await getUserStatistics()
    userStats.value = response
  } catch (error) {
    console.error('Failed to load user statistics:', error)
  }
}

onMounted(() => {
  loadUserStatistics()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.dashboard-header {
  margin-bottom: 30px;
}

.dashboard-header h1 {
  color: #303133;
  margin-bottom: 10px;
}

.dashboard-header p {
  color: #606266;
  font-size: 14px;
}

.stats-cards {
  margin-bottom: 30px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 20px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

.admin-tabs {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.coming-soon {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
  font-size: 16px;
}
</style>