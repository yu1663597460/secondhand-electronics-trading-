<template>
  <div class="register-page">
    <div class="reg-left">
      <router-link to="/login" class="back-link">← 返回登录</router-link>
      <div class="reg-brand">
        <div class="brand-logo">♻️</div>
        <h1>加入我们</h1>
        <p>注册账号，开启你的二手交易之旅</p>
      </div>
      <div class="reg-steps">
        <div class="step" :class="{ active: currentStep >= 1, done: currentStep > 1 }">
          <div class="step-num">1</div>
          <span>填写基本信息</span>
        </div>
        <div class="step-line" />
        <div class="step" :class="{ active: currentStep >= 2, done: currentStep > 2 }">
          <div class="step-num">2</div>
          <span>设置账号密码</span>
        </div>
        <div class="step-line" />
        <div class="step" :class="{ active: currentStep >= 3 }">
          <div class="step-num">3</div>
          <span>完成注册</span>
        </div>
      </div>
    </div>

    <div class="reg-right">
      <div class="reg-box">
        <h2>创建新账号</h2>
        <p class="reg-sub">已有账号？<router-link to="/login">立即登录</router-link></p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-row">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="3-20位字母、数字或下划线" :prefix-icon="User" @input="currentStep = form.username ? 1 : 0" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Phone" />
            </el-form-item>
          </div>

          <el-form-item label="邮箱地址" prop="email">
            <el-input v-model="form.email" placeholder="请输入常用邮箱" :prefix-icon="Message" @input="currentStep = form.email ? 2 : 1" />
          </el-form-item>

          <div class="form-row">
            <el-form-item label="登录密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="6-20位，区分大小写" :prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" :prefix-icon="Lock" show-password @keyup.enter="handleRegister" />
            </el-form-item>
          </div>

          <!-- 密码强度 -->
          <div class="pwd-strength" v-if="form.password">
            <span class="strength-label">密码强度：</span>
            <div class="strength-bars">
              <div class="bar" :class="{ active: pwdStrength >= 1, weak: pwdStrength === 1 }" />
              <div class="bar" :class="{ active: pwdStrength >= 2, medium: pwdStrength === 2 }" />
              <div class="bar" :class="{ active: pwdStrength >= 3, strong: pwdStrength >= 3 }" />
            </div>
            <span class="strength-text">{{ ['', '弱', '中', '强'][pwdStrength] }}</span>
          </div>

          <div class="agree-row">
            <el-checkbox v-model="agreeTerms">
              我已阅读并同意
              <a href="#" class="link">《用户协议》</a>
              和
              <a href="#" class="link">《隐私政策》</a>
            </el-checkbox>
          </div>

          <button class="btn-register" :class="{ loading }" @click.prevent="handleRegister" :disabled="loading || !agreeTerms">
            <span v-if="!loading">立即注册</span>
            <span v-else>注册中...</span>
          </button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const agreeTerms = ref(false)
const currentStep = ref(0)

const form = ref({ username: '', email: '', phone: '', password: '', confirmPassword: '' })

const pwdStrength = computed(() => {
  const p = form.value.password
  if (!p) return 0
  let s = 0
  if (p.length >= 6) s++
  if (/[A-Z]/.test(p) || /\d/.test(p)) s++
  if (p.length >= 10 && /[^a-zA-Z0-9]/.test(p)) s++
  return Math.min(s, 3)
})

const validatePassword = (rule, value, callback) => {
  if (!value) callback(new Error('请再次输入密码'))
  else if (value !== form.value.password) callback(new Error('两次密码不一致'))
  else callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!agreeTerms.value) { ElMessage.warning('请先同意用户协议'); return }
  try {
    await formRef.value.validate()
    loading.value = true
    await userStore.register({
      username: form.value.username,
      email: form.value.email,
      phone: form.value.phone,
      password: form.value.password
    })
    currentStep.value = 3
    ElMessage.success('注册成功，欢迎加入！')
    setTimeout(() => router.push('/'), 600)
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  min-height: 100vh;
}

.reg-left {
  width: 360px;
  background: linear-gradient(160deg, #0f3460 0%, #533483 100%);
  padding: 40px 36px;
  display: flex;
  flex-direction: column;
  color: #fff;
  flex-shrink: 0;
}

.back-link {
  color: rgba(255,255,255,0.7);
  text-decoration: none;
  font-size: 13px;
  margin-bottom: 48px;
  display: inline-block;
  transition: color 0.2s;
}
.back-link:hover { color: #fff; }

.reg-brand { margin-bottom: 48px; }
.brand-logo { font-size: 52px; margin-bottom: 12px; }
.reg-brand h1 { font-size: 30px; font-weight: 700; margin: 0 0 8px; }
.reg-brand p { font-size: 14px; opacity: 0.65; margin: 0; line-height: 1.6; }

.reg-steps { display: flex; flex-direction: column; gap: 0; }
.step {
  display: flex;
  align-items: center;
  gap: 14px;
  opacity: 0.45;
  transition: opacity 0.3s;
  font-size: 14px;
}
.step.active { opacity: 1; }
.step.done { opacity: 0.75; }
.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}
.step.active .step-num { border-color: #fff; background: rgba(255,255,255,0.15); }
.step.done .step-num { border-color: #67c23a; background: #67c23a; }
.step-line { width: 2px; height: 24px; background: rgba(255,255,255,0.2); margin-left: 13px; }

.reg-right {
  flex: 1;
  background: #f7f8fc;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.reg-box {
  width: 100%;
  max-width: 560px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 44px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.07);
}

.reg-box h2 { font-size: 26px; font-weight: 700; color: #1a1a2e; margin: 0 0 6px; }
.reg-sub { font-size: 13px; color: #999; margin: 0 0 28px; }
.reg-sub a { color: #0f3460; font-weight: 600; text-decoration: none; }
.reg-sub a:hover { text-decoration: underline; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

:deep(.el-form-item__label) { font-size: 13px; font-weight: 600; color: #444; padding-bottom: 6px; }
:deep(.el-input__wrapper) { border-radius: 8px; }

.pwd-strength {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: -8px 0 16px;
  font-size: 12px;
}
.strength-label { color: #999; }
.strength-bars { display: flex; gap: 4px; }
.bar { width: 32px; height: 4px; border-radius: 2px; background: #eee; transition: background 0.3s; }
.bar.active.weak { background: #f56c6c; }
.bar.active.medium { background: #e6a23c; }
.bar.active.strong { background: #67c23a; }
.strength-text { color: #666; }

.agree-row { margin-bottom: 20px; font-size: 13px; }
.link { color: #0f3460; text-decoration: none; }
.link:hover { text-decoration: underline; }

.btn-register {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #0f3460, #533483);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.15s;
  letter-spacing: 2px;
}
.btn-register:hover:not(:disabled) { opacity: 0.9; transform: translateY(-1px); }
.btn-register:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 768px) {
  .reg-left { display: none; }
  .reg-right { padding: 20px; }
  .reg-box { padding: 28px 20px; }
  .form-row { grid-template-columns: 1fr; }
}
</style>
