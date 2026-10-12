<template>
  <div class="admin-layout">
    <!-- 顶部导航栏 -->
    <div class="admin-header">
      <div class="header-left">
        <el-icon size="24" color="white"><Setting /></el-icon>
        <span class="logo-text">管理后台</span>
      </div>
      
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="admin-info">
            <el-icon><User /></el-icon>
            {{ userStore.username }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="userSite">用户前台</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <div class="admin-body">
      <!-- 侧边栏 -->
      <div class="admin-sidebar">
        <el-menu
          :default-active="$route.path"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/admin">
            <el-icon><DataBoard /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
            <el-badge v-if="pendingCount > 0" :value="pendingCount" class="menu-badge" />
          </el-menu-item>

          <el-menu-item index="/admin/review">
            <el-icon><CircleCheck /></el-icon>
            <span>商品审核</span>
            <el-badge v-if="pendingCount > 0" :value="pendingCount" class="menu-badge" type="warning" />
          </el-menu-item>
          
          <el-menu-item index="/admin/orders">
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/statistics">
            <el-icon><TrendCharts /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/settings">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/logs">
            <el-icon><Document /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
        </el-menu>
      </div>
      
      <!-- 主内容区 -->
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting, User, ArrowDown, DataBoard, Goods, Document, Menu, 
  TrendCharts, CircleCheck, Warning
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getPendingProductCount } from '../api/admin'

const router = useRouter()
const userStore = useUserStore()

const pendingCount = ref(0)

const loadPendingCount = async () => {
  try {
    // 检查用户是否有管理员权限
    if (!userStore.isLoggedIn || !userStore.isAdmin) {
      console.log('用户没有管理员权限，跳过加载待审核数量')
      return
    }
    
    // 延迟加载，确保用户已完全登录
    setTimeout(async () => {
      try {
        const count = await getPendingProductCount()
        pendingCount.value = count
      } catch (error) {
        console.error('Failed to load pending count:', error)
        // 静默处理权限错误
      }
    }, 1000)
  } catch (error) {
    console.error('Failed to load pending count:', error)
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人信息功能开发中')
      break
    case 'userSite':
      router.push('/')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  // 只有管理员才加载待审核数量
  if (userStore.isLoggedIn && userStore.isAdmin) {
    loadPendingCount()
    // 每30秒刷新一次待审核商品数量
    setInterval(loadPendingCount, 30000)
  }
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.admin-header {
  height: 64px;
  background: #001529;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  color: white;
}

.logo-text {
  margin-left: 12px;
  font-size: 18px;
  font-weight: 600;
}

.header-right {
  color: white;
}

.admin-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.admin-info:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.admin-info .el-icon {
  margin: 0 4px;
}

.admin-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.admin-sidebar {
  width: 256px;
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.sidebar-menu .el-menu-item {
  height: 56px;
  line-height: 56px;
  position: relative;
}

.menu-badge {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
}

.admin-content {
  flex: 1;
  overflow-y: auto;
  background: #f0f2f5;
}
</style>