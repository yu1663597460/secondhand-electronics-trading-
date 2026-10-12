<template>
  <div class="profile-page">

    <!-- 顶部封面区 -->
    <div class="profile-cover">
      <div class="cover-bg" :style="heroBgStyle" />
      <input ref="bgInputRef" type="file" accept="image/*" hidden @change="handleBgUpload" />
      <button class="btn-change-cover" @click="triggerBgInput">更换封面</button>

      <div class="cover-content">
        <div class="avatar-area">
          <div class="avatar-ring" @click="triggerAvatarInput">
            <img v-if="userStore.avatar" :src="userStore.avatar" class="avatar-img" />
            <span v-else class="avatar-letter">{{ userInfo.username?.charAt(0)?.toUpperCase() || 'U' }}</span>
            <div class="avatar-overlay">换头像</div>
          </div>
          <div class="verified-badge" v-if="userInfo.verificationStatus === 'VERIFIED'">已认证</div>
          <input ref="avatarInputRef" type="file" accept="image/*" hidden @change="handleAvatarUpload" />
        </div>

        <div class="cover-meta">
          <div class="cover-name-row">
            <h1>{{ userInfo.username }}</h1>
            <span class="role-chip" :class="userInfo.role?.toLowerCase()">
              {{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </span>
          </div>
          <div class="cover-sub">
            {{ userInfo.email || '未设置邮箱' }}
            <span class="dot-sep">·</span>
            {{ userInfo.phone || '未绑定手机' }}
          </div>
          <div class="cover-stats" v-if="carbonAccount">
            <div class="stat-item">
              <span class="stat-num">{{ carbonAccount.totalPoints }}</span>
              <span class="stat-label">累计积分</span>
            </div>
            <div class="stat-divider" />
            <div class="stat-item">
              <span class="stat-num">{{ (carbonAccount.carbonSavedGrams / 1000).toFixed(1) }}kg</span>
              <span class="stat-label">减碳量</span>
            </div>
            <div class="stat-divider" />
            <div class="stat-item">
              <span class="stat-num level-chip" :style="{ color: levelMeta.color }">{{ levelMeta.icon }} {{ levelMeta.label }}</span>
              <span class="stat-label">环保等级</span>
            </div>
          </div>
        </div>

        <div class="cover-actions">
          <button class="cta-btn primary" @click="$router.push('/publish')">发布闲置</button>
          <button class="cta-btn" @click="$router.push('/orders')">我的订单</button>
          <button class="cta-btn" @click="$router.push('/my-products')">我的商品</button>
        </div>
      </div>
    </div>

    <!-- 主体 -->
    <div class="profile-body">
      <!-- 左侧导航 -->
      <nav class="side-nav">
        <div
          v-for="tab in tabs" :key="tab.key"
          class="nav-item" :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          <span class="nav-icon">{{ tab.icon }}</span>
          <span class="nav-label">{{ tab.label }}</span>
          <span class="nav-arrow" v-if="activeTab === tab.key">›</span>
        </div>
      </nav>

      <!-- 右侧面板 -->
      <div class="panel-area">

        <!-- 基本信息 -->
        <div v-show="activeTab === 'basic'" class="panel">
          <div class="panel-header">
            <span class="panel-title">基本信息</span>
            <span class="panel-desc">管理你的账号资料</span>
          </div>
          <el-form ref="basicFormRef" :model="basicForm" :rules="basicRules" label-position="top">
            <div class="form-grid">
              <el-form-item label="用户名">
                <el-input v-model="basicForm.username" disabled>
                  <template #suffix><span style="color:#bbb;font-size:12px">不可修改</span></template>
                </el-input>
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="basicForm.nickname" placeholder="设置你的显示昵称（2-20字符）" />
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input v-model="basicForm.realName" placeholder="用于实名认证" />
              </el-form-item>
              <el-form-item label="邮箱地址" prop="email">
                <el-input v-model="basicForm.email" placeholder="接收交易通知" />
              </el-form-item>
              <el-form-item label="手机号码" prop="phone">
                <el-input v-model="basicForm.phone" placeholder="买卖联系方式" />
              </el-form-item>
            </div>
            <div class="form-footer">
              <el-button type="primary" :loading="updating" @click="updateBasicInfo">保存修改</el-button>
            </div>
          </el-form>
        </div>

        <!-- 收货地址 -->
        <div v-show="activeTab === 'address'" class="panel">
          <div class="panel-header">
            <span class="panel-title">收货地址</span>
            <button class="header-action-btn" @click="openAddressDialog(null)">+ 新增地址</button>
          </div>
          <div v-loading="loadingAddresses">
            <div v-if="addresses.length === 0" class="empty-state">
              <div class="empty-icon">📍</div>
              <p>还没有收货地址</p>
              <button class="empty-btn" @click="openAddressDialog(null)">添加第一个地址</button>
            </div>
            <div v-else class="addr-list">
              <div v-for="addr in addresses" :key="addr.id" class="addr-card" :class="{ 'is-default': addr.isDefault }">
                <div class="addr-tag" v-if="addr.isDefault">默认</div>
                <div class="addr-body">
                  <div class="addr-top">
                    <span class="addr-name">{{ addr.receiverName }}</span>
                    <span class="addr-phone">{{ addr.phone }}</span>
                  </div>
                  <div class="addr-text">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detailAddress }}</div>
                </div>
                <div class="addr-ops">
                  <button class="addr-op-btn" @click="openAddressDialog(addr)">编辑</button>
                  <button class="addr-op-btn" :disabled="addr.isDefault" @click="handleSetDefault(addr.id)">设为默认</button>
                  <button class="addr-op-btn del" @click="handleDeleteAddress(addr.id)">删除</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 安全设置 -->
        <div v-show="activeTab === 'security'" class="panel">
          <div class="panel-header">
            <span class="panel-title">安全设置</span>
            <span class="panel-desc">定期更换密码保护账号安全</span>
          </div>
          <div class="security-notice">
            <span>🔐</span>
            <span>密码长度 8-20 位，建议包含字母和数字</span>
          </div>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top" style="max-width:380px">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="8-20 位" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <div class="form-footer">
              <el-button type="primary" :loading="updatingPassword" @click="handleChangePassword">修改密码</el-button>
            </div>
          </el-form>
        </div>

        <!-- 实名认证 -->
        <div v-show="activeTab === 'verify'" class="panel">
          <div class="panel-header">
            <span class="panel-title">实名认证</span>
            <span class="panel-desc">认证后交易更有保障，买家更信任</span>
          </div>
          <div v-if="userInfo.verificationStatus === 'VERIFIED'" class="verify-result success">
            <div class="verify-result-icon">✅</div>
            <div>
              <div class="verify-result-title">已完成实名认证</div>
              <div class="verify-result-sub">你的账号已通过认证，享有更高交易信誉</div>
            </div>
          </div>
          <div v-else-if="userInfo.verificationStatus === 'PENDING'" class="verify-result pending">
            <div class="verify-result-icon">⏳</div>
            <div>
              <div class="verify-result-title">审核中</div>
              <div class="verify-result-sub">预计 1-3 个工作日完成审核，请耐心等待</div>
            </div>
          </div>
          <div v-else>
            <div class="verify-tips">
              <div class="verify-tip-item"><span>🛡️</span><span>认证信息仅用于身份核验，不会对外展示</span></div>
              <div class="verify-tip-item"><span>📈</span><span>认证用户在搜索结果中优先展示</span></div>
            </div>
            <el-form ref="verifyFormRef" :model="verifyForm" :rules="verifyRules" label-position="top" style="max-width:380px">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="verifyForm.realName" placeholder="与身份证一致" />
              </el-form-item>
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="verifyForm.idCard" placeholder="18 位身份证号码" maxlength="18" />
              </el-form-item>
              <div class="form-footer">
                <el-button type="primary" :loading="submittingVerify" @click="handleSubmitVerification">提交认证</el-button>
              </div>
            </el-form>
          </div>
        </div>

        <!-- 碳积分 -->
        <div v-show="activeTab === 'carbon'" class="panel carbon-panel">
          <div v-if="carbonAccount">

            <!-- 顶部：积分总览 + 签到 -->
            <div class="carbon-hero" :style="{ '--level-color': levelMeta.color }">
              <div class="carbon-hero-left">
                <div class="carbon-level-badge">
                  <span class="level-emoji">{{ levelMeta.icon }}</span>
                  <span class="level-name">{{ levelMeta.label }}</span>
                </div>
                <div class="carbon-pts-row">
                  <span class="carbon-pts-num">{{ carbonAccount.availablePoints }}</span>
                  <span class="carbon-pts-unit">可用积分</span>
                </div>
                <div class="carbon-co2">
                  已累计减少碳排放
                  <strong>{{ (carbonAccount.carbonSavedGrams / 1000).toFixed(2) }} kg</strong>
                  CO₂
                </div>
              </div>
              <div class="carbon-hero-right">
                <button
                  class="sign-btn"
                  :class="{ signed: carbonAccount.signedToday }"
                  :disabled="carbonAccount.signedToday || signing"
                  @click="handleDailySign"
                >
                  <span class="sign-icon">{{ carbonAccount.signedToday ? '✓' : '📅' }}</span>
                  <span>{{ carbonAccount.signedToday ? '已签到' : '每日签到' }}</span>
                  <span class="sign-reward" v-if="!carbonAccount.signedToday">+2分</span>
                </button>
                <div class="sign-hint">{{ carbonAccount.signedToday ? '明天再来' : '签到得积分' }}</div>
              </div>
            </div>

            <!-- 等级进度 -->
            <div class="carbon-progress-section">
              <div class="progress-header">
                <span>等级进度</span>
                <span class="progress-pts-text" v-if="levelMeta.next">
                  {{ carbonAccount.totalPoints }} / {{ levelMeta.next }} 分
                </span>
                <span class="progress-pts-text" v-else>已达最高等级 🎉</span>
              </div>
              <div class="progress-track">
                <div class="progress-fill" :style="{ width: levelProgress + '%', background: levelMeta.color }" />
              </div>
              <div class="level-stages">
                <span v-for="(lv, i) in levelStages" :key="i"
                  class="level-stage" :class="{ reached: carbonAccount.totalPoints >= lv.min }">
                  {{ lv.icon }} {{ lv.label }}
                </span>
              </div>
            </div>

            <!-- 获取积分方式 -->
            <div class="earn-section">
              <div class="section-title">获取积分</div>
              <div class="earn-grid">
                <div class="earn-item">
                  <div class="earn-icon">📦</div>
                  <div class="earn-info">
                    <div class="earn-name">发布闲置</div>
                    <div class="earn-desc">每件商品</div>
                  </div>
                  <div class="earn-pts">+10</div>
                </div>
                <div class="earn-item">
                  <div class="earn-icon">🤝</div>
                  <div class="earn-info">
                    <div class="earn-name">完成交易</div>
                    <div class="earn-desc">买卖双方各得</div>
                  </div>
                  <div class="earn-pts">+20</div>
                </div>
                <div class="earn-item">
                  <div class="earn-icon">✍️</div>
                  <div class="earn-info">
                    <div class="earn-name">写评价</div>
                    <div class="earn-desc">每次评价</div>
                  </div>
                  <div class="earn-pts">+5</div>
                </div>
                <div class="earn-item">
                  <div class="earn-icon">📅</div>
                  <div class="earn-info">
                    <div class="earn-name">每日签到</div>
                    <div class="earn-desc">每天一次</div>
                  </div>
                  <div class="earn-pts">+2</div>
                </div>
              </div>
              <div class="redeem-tip">
                <span>💡</span>
                <span>100 积分可在结算时抵扣 1 元，单笔最多抵扣 5 元</span>
              </div>
            </div>

            <!-- 积分明细 -->
            <div class="records-section">
              <div class="section-title">积分明细</div>
              <div v-loading="loadingCarbon" class="records-list">
                <div v-if="carbonRecords.length === 0" class="records-empty">
                  暂无记录，去发布闲置或完成交易获取积分吧
                </div>
                <div v-else>
                  <div v-for="r in carbonRecords" :key="r.id" class="record-row">
                    <div class="record-dot" :class="r.points > 0 ? 'plus' : 'minus'" />
                    <div class="record-main">
                      <span class="record-type">{{ ACTION_LABELS[r.actionType] || r.actionType }}</span>
                      <span class="record-note">{{ r.description }}</span>
                    </div>
                    <div class="record-tail">
                      <span class="record-val" :class="r.points > 0 ? 'plus' : 'minus'">
                        {{ r.points > 0 ? '+' : '' }}{{ r.points }}
                      </span>
                      <span class="record-date">{{ fmtDate(r.createdAt) }}</span>
                    </div>
                  </div>
                  <div class="records-pager" v-if="carbonRecordTotal > 8">
                    <el-pagination
                      v-model:current-page="carbonRecordPageDisplay"
                      :page-size="8" :total="carbonRecordTotal"
                      layout="prev, pager, next" small
                      @current-change="p => { carbonRecordPage = p - 1; loadCarbonRecords() }"
                    />
                  </div>
                </div>
              </div>
            </div>

          </div>
          <div v-else class="carbon-loading">加载中...</div>
        </div>

      </div>
    </div>

    <!-- 地址弹窗 -->
    <el-dialog v-model="showAddressDialog" :title="currentAddress ? '编辑地址' : '新增地址'" width="500px">
      <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-position="top">
        <div class="form-grid">
          <el-form-item label="收货人" prop="receiverName">
            <el-input v-model="addressForm.receiverName" placeholder="收货人姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="receiverPhone">
            <el-input v-model="addressForm.receiverPhone" placeholder="手机号码" />
          </el-form-item>
          <el-form-item label="省份" prop="province">
            <el-input v-model="addressForm.province" placeholder="如：广东省" />
          </el-form-item>
          <el-form-item label="城市" prop="city">
            <el-input v-model="addressForm.city" placeholder="如：广州市" />
          </el-form-item>
          <el-form-item label="区县" prop="district">
            <el-input v-model="addressForm.district" placeholder="如：天河区" />
          </el-form-item>
        </div>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="addressForm.detailAddress" type="textarea" :rows="2" placeholder="街道、门牌号等" />
        </el-form-item>
        <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingAddress" @click="handleSaveAddress">保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getProfile, updateProfile, changePassword, submitVerification, uploadAvatar } from '../api/user'
import { getAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress } from '../api/address'
import { getCarbonAccount, dailySign, getCarbonRecords } from '../api/carbon'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('basic')
const tabs = [
  { key: 'basic',    icon: '👤', label: '基本信息' },
  { key: 'address',  icon: '📍', label: '收货地址' },
  { key: 'security', icon: '🔒', label: '安全设置' },
  { key: 'verify',   icon: '✅', label: '实名认证' },
  { key: 'carbon',   icon: '🌱', label: '碳积分'   },
]

// 头像 & 封面
const avatarInputRef = ref(null)
const bgInputRef = ref(null)
const heroBg = ref(localStorage.getItem('profileBg') || '')
const heroBgStyle = computed(() => heroBg.value
  ? { backgroundImage: `url(${heroBg.value})`, backgroundSize: 'cover', backgroundPosition: 'center' }
  : {}
)
const triggerAvatarInput = () => avatarInputRef.value?.click()
const triggerBgInput = () => bgInputRef.value?.click()

const handleBgUpload = async (e) => {
  const file = e.target.files[0]
  if (!file || !file.type.startsWith('image/')) { ElMessage.error('请选择图片'); return }
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('图片不超过 5MB'); return }
  try {
    const fd = new FormData(); fd.append('file', file)
    const res = await uploadAvatar(fd)
    heroBg.value = res.avatarUrl
    localStorage.setItem('profileBg', res.avatarUrl)
    ElMessage.success('封面已更新')
  } catch { ElMessage.error('上传失败') } finally { e.target.value = '' }
}

const handleAvatarUpload = async (e) => {
  const file = e.target.files[0]
  if (!file || !file.type.startsWith('image/')) { ElMessage.error('请选择图片'); return }
  if (file.size > 2 * 1024 * 1024) { ElMessage.error('图片不超过 2MB'); return }
  try {
    const fd = new FormData(); fd.append('file', file)
    const res = await uploadAvatar(fd)
    userStore.setAvatar(res.avatarUrl)
    ElMessage.success('头像已更新')
  } catch { ElMessage.error('上传失败') } finally { e.target.value = '' }
}

// 用户信息
const userInfo = ref({ username: '', role: 'USER', verificationStatus: 'UNVERIFIED' })
const basicForm = reactive({ username: '', nickname: '', email: '', phone: '', realName: '' })
const updating = ref(false)
const basicFormRef = ref(null)
const basicRules = {
  nickname: [{ min: 2, max: 20, message: '昵称长度须在2-20个字符之间', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

const loadProfile = async () => {
  try {
    const data = await getProfile()
    userInfo.value = data
    Object.assign(basicForm, { username: data.username || '', nickname: data.nickname || '', email: data.email || '', phone: data.phone || '', realName: data.realName || '' })
  } catch {
    basicForm.username = userStore.username || ''
    userInfo.value.username = userStore.username || ''
  }
}

const updateBasicInfo = async () => {
  try {
    await basicFormRef.value.validate()
    updating.value = true
    await updateProfile({ nickname: basicForm.nickname, email: basicForm.email, phone: basicForm.phone, realName: basicForm.realName })
    ElMessage.success('信息已更新')
    loadProfile()
  } catch (e) { if (e?.message) ElMessage.error(e.message) } finally { updating.value = false }
}

// 地址
const addresses = ref([])
const loadingAddresses = ref(false)
const showAddressDialog = ref(false)
const currentAddress = ref(null)
const savingAddress = ref(false)
const addressForm = reactive({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: false })
const addressFormRef = ref(null)
const addressRules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverPhone: [{ required: true, trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const loadAddresses = async () => {
  loadingAddresses.value = true
  try { addresses.value = await getAddresses() || [] } catch { } finally { loadingAddresses.value = false }
}

const openAddressDialog = (addr) => {
  currentAddress.value = addr
  if (addr) Object.assign(addressForm, { receiverName: addr.receiverName, receiverPhone: addr.receiverPhone, province: addr.province, city: addr.city, district: addr.district, detailAddress: addr.detailAddress, isDefault: addr.isDefault })
  else Object.assign(addressForm, { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: false })
  showAddressDialog.value = true
}

const handleSaveAddress = async () => {
  try {
    await addressFormRef.value.validate()
    savingAddress.value = true
    if (currentAddress.value) await updateAddress(currentAddress.value.id, addressForm)
    else await createAddress(addressForm)
    ElMessage.success(currentAddress.value ? '地址已更新' : '地址已添加')
    showAddressDialog.value = false
    loadAddresses()
  } catch (e) { if (e?.message) ElMessage.error(e.message) } finally { savingAddress.value = false }
}

const handleSetDefault = async (id) => {
  try { await setDefaultAddress(id); ElMessage.success('已设为默认'); loadAddresses() } catch { ElMessage.error('设置失败') }
}

const handleDeleteAddress = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除此地址？', '提示', { type: 'warning' })
    await deleteAddress(id); ElMessage.success('已删除'); loadAddresses()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

// 密码
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const updatingPassword = ref(false)
const passwordFormRef = ref(null)
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, trigger: 'blur' }, { min: 8, max: 20, message: '8-20位', trigger: 'blur' }],
  confirmPassword: [{ required: true, trigger: 'blur' }, {
    validator: (r, v, cb) => v !== passwordForm.newPassword ? cb(new Error('两次密码不一致')) : cb(), trigger: 'blur'
  }]
}

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    updatingPassword.value = true
    await changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    ElMessage.success('密码已修改，请重新登录')
    Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
    setTimeout(() => { userStore.logout(); router.push('/login') }, 1500)
  } catch (e) { if (e?.message) ElMessage.error(e.message) } finally { updatingPassword.value = false }
}

// 实名认证
const verifyForm = reactive({ realName: '', idCard: '' })
const submittingVerify = ref(false)
const verifyFormRef = ref(null)
const verifyRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [{ required: true, trigger: 'blur' }, { pattern: /^\d{17}[\dX]$/, message: '身份证号格式不正确', trigger: 'blur' }]
}

const handleSubmitVerification = async () => {
  try {
    await verifyFormRef.value.validate()
    submittingVerify.value = true
    await submitVerification(verifyForm)
    ElMessage.success('认证申请已提交')
    userInfo.value.verificationStatus = 'PENDING'
  } catch (e) { if (e?.message) ElMessage.error(e.message) } finally { submittingVerify.value = false }
}

// 碳积分
const carbonAccount = ref(null)
const carbonRecords = ref([])
const carbonRecordTotal = ref(0)
const carbonRecordPage = ref(0)
const carbonRecordPageDisplay = ref(1)
const loadingCarbon = ref(false)
const signing = ref(false)

const LEVEL_META = {
  SPROUT:   { label: '绿芽',     color: '#52c41a', icon: '🌱', next: 50,  min: 0   },
  LEAF:     { label: '绿叶',     color: '#389e0d', icon: '🍃', next: 200, min: 50  },
  TREE:     { label: '绿树',     color: '#237804', icon: '🌳', next: 500, min: 200 },
  GUARDIAN: { label: '森林守护者', color: '#092b00', icon: '🌲', next: null, min: 500 }
}

const levelStages = [
  { icon: '🌱', label: '绿芽', min: 0   },
  { icon: '🍃', label: '绿叶', min: 50  },
  { icon: '🌳', label: '绿树', min: 200 },
  { icon: '🌲', label: '守护者', min: 500 },
]

const levelMeta = computed(() => LEVEL_META[carbonAccount.value?.level] || LEVEL_META.SPROUT)
const levelProgress = computed(() => {
  if (!carbonAccount.value) return 0
  const meta = LEVEL_META[carbonAccount.value.level]
  if (!meta.next) return 100
  const prev = { SPROUT: 0, LEAF: 50, TREE: 200, GUARDIAN: 500 }[carbonAccount.value.level] || 0
  return Math.min(100, Math.round(((carbonAccount.value.totalPoints - prev) / (meta.next - prev)) * 100))
})

const ACTION_LABELS = {
  PUBLISH: '发布闲置', COMPLETE_ORDER: '交易完成', REVIEW: '写评价', DAILY_SIGN: '每日签到', REDEEM: '积分抵扣'
}

const loadCarbonAccount = async () => {
  try {
    carbonAccount.value = await getCarbonAccount()
    await loadCarbonRecords()
  } catch { }
}

const loadCarbonRecords = async () => {
  loadingCarbon.value = true
  try {
    const res = await getCarbonRecords(carbonRecordPage.value, 8)
    carbonRecords.value = res.content || []
    carbonRecordTotal.value = res.totalElements || 0
  } catch { } finally { loadingCarbon.value = false }
}

const handleDailySign = async () => {
  signing.value = true
  try {
    const res = await dailySign()
    carbonAccount.value = res
    ElMessage.success('签到成功，+2 碳积分 🌱')
    await loadCarbonRecords()
  } catch (e) {
    ElMessage.warning(e?.response?.data?.message || '今天已签到')
    // 刷新账户状态确保按钮正确
    carbonAccount.value = await getCarbonAccount()
  } finally { signing.value = false }
}

const fmtDate = (d) => {
  try {
    const dt = new Date(d)
    return `${dt.getMonth() + 1}/${dt.getDate()}`
  } catch { return '' }
}

onMounted(() => { loadProfile(); loadAddresses(); loadCarbonAccount() })
</script>

<style scoped>
.profile-page { max-width: 1080px; margin: 0 auto; padding: 0 16px 60px; }

/* ── 封面区 ── */
.profile-cover {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 20px;
  min-height: 220px;
  background: #fff;
  box-shadow: 0 2px 16px rgba(0,0,0,0.08);
}
.cover-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
  background-size: cover;
  background-position: center;
}
.btn-change-cover {
  position: absolute;
  top: 14px; right: 16px;
  z-index: 5;
  background: rgba(0,0,0,0.4);
  color: rgba(255,255,255,0.85);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 6px;
  padding: 5px 12px;
  font-size: 12px;
  cursor: pointer;
  backdrop-filter: blur(6px);
  transition: background 0.2s;
}
.btn-change-cover:hover { background: rgba(0,0,0,0.6); }

.cover-content {
  position: relative;
  z-index: 2;
  padding: 28px 28px 24px;
  display: flex;
  align-items: flex-end;
  gap: 20px;
  flex-wrap: wrap;
}

/* 头像 */
.avatar-area { position: relative; flex-shrink: 0; }
.avatar-ring {
  width: 88px; height: 88px;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.9);
  box-shadow: 0 4px 16px rgba(0,0,0,0.25);
  cursor: pointer;
  overflow: hidden;
  position: relative;
  background: linear-gradient(135deg, #e4393c, #ff6b6b);
  display: flex; align-items: center; justify-content: center;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-letter { font-size: 34px; font-weight: 700; color: #fff; }
.avatar-overlay {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.5);
  color: #fff; font-size: 12px;
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s;
}
.avatar-ring:hover .avatar-overlay { opacity: 1; }
.verified-badge {
  position: absolute; bottom: 0; right: -2px;
  background: #52c41a; color: #fff;
  font-size: 10px; padding: 2px 6px;
  border-radius: 10px; border: 2px solid #fff;
  white-space: nowrap;
}

/* 封面信息 */
.cover-meta { flex: 1; min-width: 0; padding-bottom: 2px; }
.cover-name-row { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.cover-name-row h1 { font-size: 22px; font-weight: 700; color: #fff; margin: 0; text-shadow: 0 1px 6px rgba(0,0,0,0.4); }
.role-chip {
  font-size: 11px; padding: 2px 8px; border-radius: 4px;
  background: rgba(255,255,255,0.15); color: rgba(255,255,255,0.9);
  border: 1px solid rgba(255,255,255,0.2);
}
.role-chip.admin { background: rgba(228,57,60,0.4); border-color: rgba(228,57,60,0.6); }
.cover-sub { font-size: 13px; color: rgba(255,255,255,0.6); margin-bottom: 14px; }
.dot-sep { margin: 0 6px; }

.cover-stats { display: flex; align-items: center; gap: 0; }
.stat-item { display: flex; flex-direction: column; align-items: center; padding: 0 16px; }
.stat-item:first-child { padding-left: 0; }
.stat-num { font-size: 18px; font-weight: 700; color: #fff; line-height: 1.2; }
.stat-label { font-size: 11px; color: rgba(255,255,255,0.5); margin-top: 2px; }
.stat-divider { width: 1px; height: 28px; background: rgba(255,255,255,0.2); }
.level-chip { font-size: 14px !important; }

/* 封面操作 */
.cover-actions { display: flex; gap: 8px; flex-wrap: wrap; padding-bottom: 2px; }
.cta-btn {
  height: 34px; padding: 0 16px;
  border-radius: 6px; font-size: 13px; font-weight: 500;
  cursor: pointer; border: 1px solid rgba(255,255,255,0.3);
  background: rgba(255,255,255,0.12); color: #fff;
  backdrop-filter: blur(4px);
  transition: background 0.15s;
}
.cta-btn:hover { background: rgba(255,255,255,0.22); }
.cta-btn.primary { background: #e4393c; border-color: #e4393c; }
.cta-btn.primary:hover { background: #c0292b; }

/* ── 主体布局 ── */
.profile-body { display: flex; gap: 16px; align-items: flex-start; }

/* 侧边导航 */
.side-nav {
  width: 168px; flex-shrink: 0;
  background: #fff; border-radius: 12px;
  padding: 6px; box-shadow: 0 2px 10px rgba(0,0,0,0.06);
}
.nav-item {
  display: flex; align-items: center; gap: 8px;
  padding: 11px 12px; border-radius: 8px;
  cursor: pointer; font-size: 14px; color: #555;
  transition: all 0.15s; position: relative;
}
.nav-item:hover { background: #f5f5f5; }
.nav-item.active { background: #fff0f0; color: #e4393c; font-weight: 600; }
.nav-icon { font-size: 15px; flex-shrink: 0; }
.nav-label { flex: 1; }
.nav-arrow { font-size: 16px; color: #e4393c; }

/* 面板区 */
.panel-area { flex: 1; min-width: 0; }
.panel {
  background: #fff; border-radius: 12px;
  padding: 24px 28px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
  min-height: 360px;
}

.panel-header {
  display: flex; align-items: baseline; justify-content: space-between;
  margin-bottom: 22px; padding-bottom: 14px;
  border-bottom: 1px solid #f0f0f0;
}
.panel-title { font-size: 16px; font-weight: 700; color: #1a1a1a; }
.panel-desc { font-size: 12px; color: #bbb; }
.header-action-btn {
  padding: 6px 14px; background: #e4393c; color: #fff;
  border: none; border-radius: 6px; font-size: 13px; cursor: pointer;
  transition: opacity 0.2s;
}
.header-action-btn:hover { opacity: 0.85; }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 4px; }
.form-footer { margin-top: 16px; }

/* 地址 */
.empty-state { text-align: center; padding: 48px 0; color: #bbb; }
.empty-icon { font-size: 40px; margin-bottom: 10px; }
.empty-state p { margin-bottom: 16px; font-size: 14px; }
.empty-btn {
  padding: 8px 20px; background: #e4393c; color: #fff;
  border: none; border-radius: 6px; font-size: 13px; cursor: pointer;
}
.addr-list { display: flex; flex-direction: column; gap: 10px; }
.addr-card {
  border: 1.5px solid #ebebeb; border-radius: 10px;
  padding: 14px 16px; display: flex;
  justify-content: space-between; align-items: center;
  gap: 12px; position: relative; transition: border-color 0.2s;
}
.addr-card.is-default { border-color: #e4393c; background: #fffafa; }
.addr-tag {
  position: absolute; top: -1px; left: 14px;
  background: #e4393c; color: #fff;
  font-size: 10px; padding: 2px 7px;
  border-radius: 0 0 5px 5px;
}
.addr-body { flex: 1; }
.addr-top { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.addr-name { font-weight: 600; font-size: 14px; }
.addr-phone { color: #888; font-size: 13px; }
.addr-text { font-size: 13px; color: #999; }
.addr-ops { display: flex; gap: 6px; flex-shrink: 0; }
.addr-op-btn {
  padding: 4px 10px; border: 1px solid #e8e8e8;
  border-radius: 5px; background: #fff;
  font-size: 12px; cursor: pointer; color: #666;
  transition: all 0.15s;
}
.addr-op-btn:hover { border-color: #e4393c; color: #e4393c; }
.addr-op-btn.del:hover { background: #fff0f0; }
.addr-op-btn:disabled { opacity: 0.35; cursor: not-allowed; }

/* 安全 */
.security-notice {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 14px; background: #fffbe6;
  border-left: 3px solid #faad14; border-radius: 6px;
  font-size: 13px; color: #7a5c00; margin-bottom: 20px;
}

/* 认证 */
.verify-result {
  display: flex; align-items: center; gap: 16px;
  padding: 20px; border-radius: 10px; margin-bottom: 16px;
}
.verify-result.success { background: #f6ffed; border: 1px solid #b7eb8f; }
.verify-result.pending { background: #fffbe6; border: 1px solid #ffe58f; }
.verify-result-icon { font-size: 36px; flex-shrink: 0; }
.verify-result-title { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 4px; }
.verify-result-sub { font-size: 13px; color: #888; }
.verify-tips { margin-bottom: 20px; display: flex; flex-direction: column; gap: 8px; }
.verify-tip-item {
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; color: #666;
  padding: 8px 12px; background: #f8f9fa; border-radius: 6px;
}

/* ── 碳积分面板 ── */
.carbon-panel { padding: 0; overflow: hidden; }
.carbon-loading { padding: 60px; text-align: center; color: #bbb; }

.carbon-hero {
  padding: 24px 28px;
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid #e8f5e9;
}
.carbon-hero-left { display: flex; flex-direction: column; gap: 8px; }
.carbon-level-badge {
  display: inline-flex; align-items: center; gap: 6px;
  background: rgba(255,255,255,0.7); border-radius: 20px;
  padding: 4px 12px; width: fit-content;
}
.level-emoji { font-size: 16px; }
.level-name { font-size: 13px; font-weight: 600; color: #237804; }
.carbon-pts-row { display: flex; align-items: baseline; gap: 6px; }
.carbon-pts-num { font-size: 42px; font-weight: 800; color: #237804; line-height: 1; }
.carbon-pts-unit { font-size: 14px; color: #52c41a; }
.carbon-co2 { font-size: 13px; color: #5a7a5a; }
.carbon-co2 strong { color: #237804; }

.carbon-hero-right { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.sign-btn {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: 14px 20px; border-radius: 12px;
  background: #52c41a; color: #fff;
  border: none; cursor: pointer; font-size: 13px; font-weight: 600;
  transition: all 0.2s; min-width: 90px;
  box-shadow: 0 4px 12px rgba(39, 45, 37, 0.35);
}
.sign-btn:hover:not(:disabled) { background: #389e0d; transform: translateY(-1px); }
.sign-btn.signed { background: #d9f7be; color: #52c41a; box-shadow: none; cursor: default; }
.sign-btn:disabled { cursor: not-allowed; }
.sign-icon { font-size: 20px; }
.sign-reward { font-size: 11px; background: rgba(255,255,255,0.25); padding: 1px 6px; border-radius: 8px; }
.sign-hint { font-size: 11px; color: #8c8c8c; }

.carbon-progress-section { padding: 20px 28px; border-bottom: 1px solid #f0f0f0; }
.progress-header { display: flex; justify-content: space-between; font-size: 13px; color: #555; margin-bottom: 10px; font-weight: 500; }
.progress-pts-text { color: #8c8c8c; font-weight: 400; }
.progress-track { height: 8px; background: #f0f0f0; border-radius: 4px; overflow: hidden; margin-bottom: 12px; }
.progress-fill { height: 100%; border-radius: 4px; transition: width 0.8s cubic-bezier(0.4,0,0.2,1); }
.level-stages { display: flex; justify-content: space-between; }
.level-stage { font-size: 11px; color: #bbb; transition: color 0.3s; }
.level-stage.reached { color: #52c41a; font-weight: 600; }

.earn-section { padding: 20px 28px; border-bottom: 1px solid #f0f0f0; }
.section-title { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 14px; }
.earn-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 12px; }
.earn-item {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 14px; background: #fafafa;
  border-radius: 8px; border: 1px solid #f0f0f0;
}
.earn-icon { font-size: 22px; flex-shrink: 0; }
.earn-info { flex: 1; }
.earn-name { font-size: 13px; font-weight: 600; color: #333; }
.earn-desc { font-size: 11px; color: #bbb; margin-top: 1px; }
.earn-pts { font-size: 18px; font-weight: 800; color: #52c41a; flex-shrink: 0; }
.redeem-tip {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 14px; background: #fffbe6;
  border-radius: 6px; font-size: 12px; color: #7a5c00;
}

.records-section { padding: 20px 28px; }
.records-list { }
.records-empty { text-align: center; padding: 32px 0; color: #bbb; font-size: 13px; }
.record-row {
  display: flex; align-items: center; gap: 12px;
  padding: 11px 0; border-bottom: 1px solid #f5f5f5;
}
.record-row:last-child { border-bottom: none; }
.record-dot {
  width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
}
.record-dot.plus { background: #52c41a; }
.record-dot.minus { background: #ff4d4f; }
.record-main { flex: 1; min-width: 0; }
.record-type { font-size: 13px; font-weight: 600; color: #333; margin-right: 8px; }
.record-note { font-size: 12px; color: #bbb; }
.record-tail { display: flex; flex-direction: column; align-items: flex-end; gap: 2px; flex-shrink: 0; }
.record-val { font-size: 15px; font-weight: 700; }
.record-val.plus { color: #52c41a; }
.record-val.minus { color: #ff4d4f; }
.record-date { font-size: 11px; color: #ccc; }
.records-pager { display: flex; justify-content: center; margin-top: 14px; }

@media (max-width: 768px) {
  .profile-body { flex-direction: column; }
  .side-nav { width: 100%; display: flex; flex-wrap: wrap; gap: 4px; padding: 6px; }
  .nav-item { flex: 1; justify-content: center; min-width: 70px; padding: 10px 8px; }
  .nav-arrow { display: none; }
  .form-grid { grid-template-columns: 1fr; }
  .cover-content { flex-direction: column; align-items: flex-start; }
  .carbon-hero { flex-direction: column; gap: 20px; align-items: flex-start; }
  .earn-grid { grid-template-columns: 1fr; }
}
</style>
