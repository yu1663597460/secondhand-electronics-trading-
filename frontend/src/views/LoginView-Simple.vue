<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>用户登录</h2>
        <p>请输入您的账号信息</p>
      </div>
      
      <el-form :model="form" class="login-form">
        <el-form-item>
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleLogin" 
            :loading="loading" 
            size="large"
            style="width: 100%"
          >
            {{ loading ? '登录中...' : '立即登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="demo-accounts">
        <h4>演示账号</h4>
        <div class="demo-item">
          <span>管理员：admin / admin123</span>
          <el-button size="small" @click="quickLogin('admin', 'admin123')">
            快速登录
          </el-button>
        </div>
        <div class="demo-item">
          <span>普通用户：testuser / user123456</span>
          <el-button size="small" @click="quickLogin('testuser', 'user123456')">
            快速登录
          </el-button>
        </div>
      </div>
      
      <div class="register-link">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)

const handleLogin = async () => {
  console.log('handleLogin 被调用了')
  console.log('表单数据:', form.value)
  
  if (!form.value.username || !form.value.password) {
    ElMessage.error('请输入用户名和密码')
    return
  }
  
  try {
    loading.value = true
    console.log('开始登录，用户名:', form.value.username)
    
    await userStore.login(form.value)
    
    console.log('登录成功，用户角色:', userStore.role)
    
    if (userStore.role === 'ADMIN') {
      ElMessage.success('管理员登录成功！正在跳转到管理后台...')
      setTimeout(() => {
        router.push('/admin')
      }, 500)
    } else {
      ElMessage.success('登录成功！欢迎回来')
      setTimeout(() => {
        router.push('/')
      }, 500)
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

const quickLogin = (username, password) => {
  form.value.username = username
  form.value.password = password
  handleLogin()
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.login-header p {
  color: #666;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.demo-accounts {
  margin: 24px 0;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.demo-accounts h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #666;
}

.demo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.register-link {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.register-link a:hover {
  color: #764ba2;
}
</style>