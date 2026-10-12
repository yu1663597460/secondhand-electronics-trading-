<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand">
        <div class="brand-logo">♻️</div>
        <h1>闲置好物</h1>
        <p>二手电子产品交易平台</p>
      </div>
    </div>

    <div class="login-right">
      <div class="login-box">
        <div class="login-title">
          <h2>欢迎回来</h2>
          <p>登录您的账号，开始交易</p>
        </div>

        <div class="form-group">
          <label>用户名</label>
          <div class="input-wrap" :class="{ focused: focusField === 'username' }">
            <span class="input-icon">👤</span>
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              @focus="focusField = 'username'"
              @blur="focusField = ''"
              @keyup.enter="handleLogin"
            />
          </div>
        </div>

        <div class="form-group">
          <label>密码</label>
          <div class="input-wrap" :class="{ focused: focusField === 'password' }">
            <span class="input-icon">🔑</span>
            <input
              v-model="form.password"
              :type="showPwd ? 'text' : 'password'"
              placeholder="请输入密码"
              @focus="focusField = 'password'"
              @blur="focusField = ''"
              @keyup.enter="handleLogin"
            />
            <span class="pwd-toggle" @click="showPwd = !showPwd">{{ showPwd ? '🙈' : '👁️' }}</span>
          </div>
        </div>

        <div class="login-opts">
          <label class="remember"><input type="checkbox" v-model="rememberMe" /> 记住我</label>
          <a href="#" class="forgot">忘记密码？</a>
        </div>

        <button class="btn-login" :class="{ loading }" @click="handleLogin" :disabled="loading">
          <span v-if="!loading">登 录</span>
          <span v-else class="spin">⏳</span>
        </button>

        <div class="divider"><span>快速体验</span></div>

        <div class="quick-btns">
          <button class="btn-quick admin" @click="quickLogin('admin', 'admin123')">
            <span>🛡️</span> 管理员账号
          </button>
          <button class="btn-quick user" @click="quickLogin('testuser', 'user123456')">
            <span>👤</span> 普通用户
          </button>
        </div>

        <p class="to-register">还没有账号？<router-link to="/register">立即注册</router-link></p>
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

const form = ref({ username: '', password: '' })
const loading = ref(false)
const showPwd = ref(false)
const rememberMe = ref(false)
const focusField = ref('')

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.error('请输入用户名和密码')
    return
  }
  if (form.value.username.length < 3) { ElMessage.error('用户名至少3个字符'); return }
  if (form.value.password.length < 6) { ElMessage.error('密码至少6个字符'); return }

  try {
    loading.value = true
    await userStore.login(form.value)
    if (userStore.role === 'ADMIN') {
      ElMessage.success('管理员登录成功')
      setTimeout(() => router.push('/admin'), 800)
    } else {
      ElMessage.success('登录成功，欢迎回来')
      setTimeout(() => router.push('/'), 800)
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '用户名或密码错误')
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
.login-page {
  display: flex;
  min-height: 100vh;
  background: #f0f2f5;
}

.login-left {
  flex: 1;
  background: url('/images/banners/01.jpeg') center/cover no-repeat;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px 40px;
  color: #fff;
  position: relative;
}

.login-left::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
}

.brand { text-align: center; margin-bottom: 60px; position: relative; z-index: 1; }
.brand-logo { font-size: 64px; margin-bottom: 16px; }
.brand h1 { font-size: 36px; font-weight: 700; margin: 0 0 8px; letter-spacing: 2px; }
.brand p { font-size: 15px; opacity: 0.65; margin: 0; }



.login-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fff;
}

.login-box { width: 100%; max-width: 380px; }

.login-title { margin-bottom: 36px; }
.login-title h2 { font-size: 28px; font-weight: 700; color: #1a1a2e; margin: 0 0 6px; }
.login-title p { font-size: 14px; color: #999; margin: 0; }

.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; color: #444; margin-bottom: 8px; }

.input-wrap {
  display: flex;
  align-items: center;
  border: 1.5px solid #e8e8e8;
  border-radius: 10px;
  padding: 0 14px;
  height: 48px;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: #fafafa;
}
.input-wrap.focused {
  border-color: #0f3460;
  box-shadow: 0 0 0 3px rgba(15,52,96,0.08);
  background: #fff;
}
.input-icon { font-size: 16px; margin-right: 10px; flex-shrink: 0; }
.input-wrap input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: #333;
}
.input-wrap input::placeholder { color: #bbb; }
.pwd-toggle { cursor: pointer; font-size: 16px; flex-shrink: 0; user-select: none; }

.login-opts {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 13px;
}
.remember { display: flex; align-items: center; gap: 6px; color: #666; cursor: pointer; }
.remember input { accent-color: #0f3460; }
.forgot { color: #0f3460; text-decoration: none; }
.forgot:hover { text-decoration: underline; }

.btn-login {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #0f3460, #1a6bb5);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.15s;
}
.btn-login:hover:not(:disabled) { opacity: 0.9; transform: translateY(-1px); }
.btn-login:disabled { opacity: 0.6; cursor: not-allowed; }
.spin { display: inline-block; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 24px 0;
  color: #ccc;
  font-size: 12px;
}
.divider::before, .divider::after { content: ''; flex: 1; height: 1px; background: #eee; }

.quick-btns { display: flex; gap: 12px; margin-bottom: 24px; }
.btn-quick {
  flex: 1;
  height: 40px;
  border: 1.5px solid #e8e8e8;
  border-radius: 8px;
  background: #fff;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s;
  color: #555;
}
.btn-quick:hover { border-color: #0f3460; color: #0f3460; background: #f0f4ff; }
.btn-quick.admin:hover { border-color: #e6a23c; color: #e6a23c; background: #fdf6ec; }

.to-register { text-align: center; font-size: 13px; color: #999; margin: 0; }
.to-register a { color: #0f3460; font-weight: 600; text-decoration: none; margin-left: 4px; }
.to-register a:hover { text-decoration: underline; }

@media (max-width: 768px) {
  .login-left { display: none; }
  .login-right { width: 100%; padding: 24px; }
}
</style>
