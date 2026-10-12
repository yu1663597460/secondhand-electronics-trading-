<template>
  <div class="layout">
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-content">
          <div class="logo" @click="$router.push('/')">
            <div class="logo-icon">♻️</div>
            <div class="logo-text">
              <span class="logo-main">闲置数码</span><span class="logo-sub">交易平台</span>
              <div class="logo-badge">二手好物</div>
            </div>
          </div>
          
          <div class="nav-menu" v-if="userStore.isLoggedIn">
            <el-menu mode="horizontal" :default-active="$route.path" router class="nav-menu-list">
              <el-menu-item index="/">商品浏览</el-menu-item>
              <el-menu-item index="/favorites">我的收藏</el-menu-item>
              <el-menu-item index="/my-products">我的商品</el-menu-item>
              <el-menu-item index="/orders">我的订单</el-menu-item>
              <el-menu-item index="/chat">消息</el-menu-item>
              <el-menu-item index="/admin" v-if="userStore.isAdmin">管理后台</el-menu-item>
            </el-menu>
          </div>
          
          <div class="user-actions">
            <template v-if="userStore.isLoggedIn">
              <el-badge :value="cartCount" :hidden="cartCount === 0">
                <el-button :icon="ShoppingCart" @click="$router.push('/cart')">
                  购物车
                </el-button>
              </el-badge>
              
              <el-badge :value="unreadCount" :hidden="unreadCount === 0">
                <el-button :icon="ChatDotRound" @click="$router.push('/chat')">
                  消息
                </el-button>
              </el-badge>
              
              <el-button type="primary" :icon="Plus" @click="$router.push('/publish')">
                发布商品
              </el-button>
              
              <el-dropdown @command="handleCommand">
                <div class="avatar-trigger">
                  <div class="nav-avatar" :class="{ 'has-img': userStore.avatar }">
                    <img v-if="userStore.avatar" :src="userStore.avatar" alt="头像" />
                    <span v-else>{{ userStore.username?.charAt(0)?.toUpperCase() }}</span>
                  </div>
                  <span class="nav-username">{{ userStore.username }}</span>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <div class="dropdown-header">
                      <div class="dh-avatar" :class="{ 'has-img': userStore.avatar }">
                        <img v-if="userStore.avatar" :src="userStore.avatar" />
                        <span v-else>{{ userStore.username?.charAt(0)?.toUpperCase() }}</span>
                      </div>
                      <div class="dh-info">
                        <div class="dh-name">{{ userStore.username }}</div>
                        <div class="dh-role">{{ userStore.isAdmin ? '管理员' : '普通用户' }}</div>
                      </div>
                    </div>
                    <el-dropdown-item divided command="profile">👤 个人中心</el-dropdown-item>
                    <el-dropdown-item command="favorites">❤️ 我的收藏</el-dropdown-item>
                    <el-dropdown-item command="my-products">📦 我的商品</el-dropdown-item>
                    <el-dropdown-item command="orders">🧾 我的订单</el-dropdown-item>
                    <el-dropdown-item command="admin" v-if="userStore.isAdmin">🛡️ 管理后台</el-dropdown-item>
                    <el-dropdown-item divided command="logout">🚪 退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            
            <template v-else>
              <el-button @click="$router.push('/login')">登录</el-button>
              <el-button type="primary" @click="$router.push('/register')">注册</el-button>
            </template>
          </div>
        </div>
      </el-header>
      
      <!-- 主要内容 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="slide-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Plus, ArrowDown, ShoppingCart } from '@element-plus/icons-vue'
import { getUnreadMessageCount } from '../api/chat'
import { getCartItemCount } from '../api/cart'

const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)
const cartCount = ref(0)

const loadUnreadCount = async () => {
  if (!userStore.isLoggedIn) return
  
  try {
    const data = await getUnreadMessageCount()
    unreadCount.value = data?.count || 0
  } catch (error) {
    console.error('Failed to load unread count:', error)
    unreadCount.value = 0
  }
}

const loadCartCount = async () => {
  if (!userStore.isLoggedIn) return
  
  try {
    const response = await getCartItemCount()
    cartCount.value = response?.count || 0
  } catch (error) {
    console.error('Failed to load cart count:', error)
    cartCount.value = 0
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'favorites':
      router.push('/favorites')
      break
    case 'my-products':
      router.push('/my-products')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
  ElMessage.success('退出登录成功')
}

onMounted(() => {
  loadCartCount()
  loadUnreadCount()
  
  // 定期刷新购物车数量和未读消息
  setInterval(loadCartCount, 30000)
  setInterval(loadUnreadCount, 30000)
})
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: #F8FAF0;
}

.header {
  background: #ffffff;
  border-bottom: 1px solid #eef2e6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 1000;
  height: auto !important;
  padding: 0 !important;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
}

.logo {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: opacity 0.2s ease;
}

.logo:hover {
  opacity: 0.85;
}

.logo-icon {
  font-size: 28px;
  line-height: 1;
  filter: drop-shadow(0 2px 4px rgba(0,181,120,0.3));
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1;
  gap: 3px;
}

.logo-main {
  font-size: 20px;
  font-weight: 800;
  color: #00B578;
  letter-spacing: -0.5px;
}

.logo-sub {
  font-size: 20px;
  font-weight: 800;
  color: #333;
  letter-spacing: -0.5px;
}

.logo-badge {
  font-size: 10px;
  color: #fff;
  background: linear-gradient(90deg, #00B578, #00d68f);
  border-radius: 20px;
  padding: 1px 7px;
  width: fit-content;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.nav-menu {
  flex: 1;
  margin: 0 24px;
  display: flex;
  align-items: center;
}

.nav-menu :deep(.el-menu) {
  background: transparent;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-evenly;
}

.nav-menu :deep(.el-menu-item) {
  color: #555;
  font-weight: 500;
  border-radius: 8px;
  flex: 1;
  text-align: center;
  justify-content: center;
  transition: all 0.2s ease;
  padding: 0 8px;
}

.nav-menu :deep(.el-menu-item:hover) {
  background: rgba(0, 181, 120, 0.08);
  color: #00B578;
}

.nav-menu :deep(.el-menu-item.is-active) {
  background: rgba(0, 181, 120, 0.12);
  color: #00B578;
  border-bottom: 2px solid #00B578;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-actions :deep(.el-button--primary) {
  background: #00B578;
  border-color: #00B578;
  box-shadow: 0 2px 8px rgba(0, 181, 120, 0.3);
}

.user-actions :deep(.el-button--primary:hover) {
  background: #00a06a;
  border-color: #00a06a;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 20px;
  background: #f4f9f0;
  border: 1px solid #dce8d4;
  transition: all 0.2s ease;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.user-info:hover {
  background: rgba(0, 181, 120, 0.08);
  border-color: #00B578;
  color: #00B578;
}

/* 头像触发器 */
.avatar-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px 4px 4px;
  border-radius: 24px;
  border: 1px solid #e8e8e8;
  background: #fff;
  transition: all 0.2s;
}
.avatar-trigger:hover { border-color: #00B578; background: #f4fdf8; }

.nav-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00B578, #00d68f);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
  border: 2px solid #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.12);
}
.nav-avatar img { width: 100%; height: 100%; object-fit: cover; }
.nav-username { font-size: 13px; font-weight: 600; color: #333; max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* 下拉菜单头部 */
.dropdown-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px 10px;
  border-bottom: 1px solid #f0f0f0;
}
.dh-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00B578, #00d68f);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}
.dh-avatar img { width: 100%; height: 100%; object-fit: cover; }
.dh-name { font-size: 14px; font-weight: 600; color: #222; }
.dh-role { font-size: 12px; color: #999; margin-top: 2px; }

/* 头像上传按钮 */
.dropdown-avatar-upload {
  padding: 6px 16px 8px;
  border-bottom: 1px solid #f0f0f0;
}
.upload-avatar-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 6px;
  transition: background 0.15s;
  width: 100%;
}
.upload-avatar-btn:hover { background: #f5f5f5; color: #00B578; }

.main-content {
  background: #F8FAF0;
  min-height: calc(100vh - 64px);
  padding: 24px;
}

/* 页面切换动画 */
.slide-fade-enter-active {
  transition: all 0.35s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.25s ease-in;
}
.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(30px);
}
.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

@media (max-width: 1024px) {
  .nav-menu { margin: 0 16px; }
  .header-content { padding: 0 16px; }
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 12px;
    height: 56px;
  }
  .nav-menu { display: none; }
  .nav-username { display: none; }
  .user-actions { gap: 6px; }
  .user-actions :deep(.el-button) { padding: 6px 10px !important; font-size: 12px !important; }
  .main-content { padding: 12px; }
  .logo-main, .logo-sub { font-size: 16px; }
  .logo-badge { display: none; }
}

@media (max-width: 480px) {
  .header-content { padding: 0 8px; }
  .main-content { padding: 8px; }
  /* 小屏只保留购物车和头像 */
  .user-actions :deep(.el-badge:first-child) { display: none; }
}
</style>
