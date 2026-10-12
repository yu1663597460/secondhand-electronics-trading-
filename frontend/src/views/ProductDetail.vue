<template>
  <div class="detail-page" v-loading="loading">
    <div v-if="product" class="detail-wrap">

      <!-- 面包屑 -->
      <div class="breadcrumb">
        <span @click="$router.push('/')">首页</span>
        <span class="sep">›</span>
        <span @click="$router.push('/?category=' + product.category?.id)">{{ product.category?.name }}</span>
        <span class="sep">›</span>
        <span class="current">{{ product.title }}</span>
      </div>

      <div class="detail-main">
        <!-- 左侧图片 -->
        <div class="img-section">
          <div class="main-img-wrap">
            <img :src="currentImage" :alt="product.title" @click="showImageViewer = true" class="main-img" />
            <div class="img-zoom-hint">🔍 点击放大</div>
            <div v-if="product.status === 'SOLD'" class="sold-overlay">已售出</div>
          </div>
          <div class="thumb-list" v-if="product.images?.length > 1">
            <div
              v-for="(img, i) in product.images"
              :key="i"
              class="thumb"
              :class="{ active: currentImage === img }"
              @click="currentImage = img"
            >
              <img :src="img" />
            </div>
          </div>
        </div>

        <!-- 右侧信息 -->
        <div class="info-section">
          <!-- 标签行 -->
          <div class="info-tags">
            <span class="tag-status" :class="product.status?.toLowerCase()">{{ getStatusText(product.status) }}</span>
            <span class="tag-cond" v-if="product.condition">{{ getConditionText(product.condition) }}</span>
            <span class="tag-cat" v-if="product.category">{{ product.category.name }}</span>
          </div>

          <h1 class="prod-title">{{ product.title }}</h1>

          <!-- 价格区 -->
          <div class="price-block">
            <div class="price-main">
              <span class="price-symbol">¥</span>
              <span class="price-num">{{ formatPrice(product.price) }}</span>
            </div>
            <div class="price-hint">二手价格 · 支持议价</div>
          </div>

          <!-- 卖家信息 -->
          <div class="seller-block">
            <div class="seller-avatar">
              <img v-if="product.seller?.avatar" :src="product.seller.avatar" class="seller-avatar-img" :alt="product.seller.username" />
              <span v-else>{{ product.seller?.username?.charAt(0)?.toUpperCase() }}</span>
            </div>
            <div class="seller-info">
              <div class="seller-name">{{ product.seller?.nickname || product.seller?.username }}</div>
              <div class="seller-label">卖家</div>
            </div>
            <button class="btn-chat" @click="contactSeller" :disabled="isOwnProduct">
              💬 联系卖家
            </button>
          </div>

          <!-- 商品属性 -->
          <div class="attr-list">
            <div class="attr-item">
              <span class="attr-key">发布时间</span>
              <span class="attr-val">{{ formatDate(product.createdAt) }}</span>
            </div>
            <div class="attr-item">
              <span class="attr-key">商品成色</span>
              <span class="attr-val">{{ getConditionText(product.condition) }}</span>
            </div>
            <div class="attr-item">
              <span class="attr-key">商品分类</span>
              <span class="attr-val">{{ product.category?.name }}</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-btns" v-if="product.status === 'APPROVED'">
            <button class="btn-cart" @click="handleAddToCart" :disabled="isOwnProduct">
              🛒 加入购物车
            </button>
            <button class="btn-buy" @click="handlePurchase" :disabled="isOwnProduct">
              {{ isOwnProduct ? '这是您的商品' : '立即购买' }}
            </button>
          </div>
          <div v-else class="status-notice">
            <span>{{ product.status === 'SOLD' ? '😔 该商品已售出' : '该商品暂不可购买' }}</span>
          </div>

          <!-- 收藏 -->
          <div class="fav-row">
            <FavoriteButton :product-id="product.id" size="default" />
            <span class="fav-hint">收藏后可随时查看</span>
          </div>
        </div>
      </div>

      <!-- 商品详情 + 相关推荐 -->
      <div class="detail-bottom">
        <div class="desc-card">
          <div class="card-title">商品详情</div>
          <p class="desc-text">{{ product.description }}</p>
        </div>

        <div class="related-card" v-if="relatedProducts.length > 0">
          <div class="card-title">相关推荐</div>
          <div class="related-grid">
            <div v-for="item in relatedProducts" :key="item.id" class="related-item" @click="viewProduct(item.id)">
              <img :src="getProductImage(item)" :alt="item.title" />
              <div class="related-info">
                <div class="related-title">{{ item.title }}</div>
                <div class="related-price">¥{{ formatPrice(item.price) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品评价 -->
      <div class="reviews-card" v-if="reviews.length > 0">
        <div class="card-title">
          买家评价
          <span class="review-count">{{ reviews.length }} 条</span>
          <span class="avg-rating">平均 {{ avgRating }} ★</span>
        </div>
        <div class="review-list">
          <div v-for="r in reviews" :key="r.id" class="review-item">
            <div class="review-header">
              <div class="reviewer-avatar">{{ r.buyer?.username?.charAt(0)?.toUpperCase() }}</div>
              <div class="reviewer-info">
                <span class="reviewer-name">{{ r.buyer?.username }}</span>
                <span class="review-stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
              </div>
              <span class="review-date">{{ formatDate(r.createdAt) }}</span>
            </div>
            <p class="review-content">{{ r.content || '该用户没有填写评价' }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片查看器 -->
    <el-image-viewer
      v-if="showImageViewer"
      :url-list="product?.images || [currentImage]"
      :initial-index="getCurrentImageIndex()"
      @close="showImageViewer = false"
    />

    <!-- 支付弹窗 -->
    <el-dialog v-model="showPaymentDialog" :title="payDialogTitle" width="560px"
      :close-on-click-modal="false" :show-close="paymentStep < 2" class="pay-dialog">

      <!-- Step 0: 确认订单 + 选地址 -->
      <div v-if="paymentStep === 0" class="pay-step">

        <!-- 商品信息 -->
        <div class="pay-section-title">商品信息</div>
        <div class="pay-product">
          <img :src="currentImage" class="pay-img" />
          <div class="pay-prod-detail">
            <div class="pay-prod-name">{{ product?.title }}</div>
            <div class="pay-prod-meta">
              <span v-if="product?.condition">{{ getConditionText(product.condition) }}</span>
              <span v-if="product?.category?.name">{{ product.category.name }}</span>
            </div>
            <div class="pay-amount">¥{{ formatPrice(product?.price) }}</div>
          </div>
        </div>

        <!-- 收货地址 -->
        <div class="pay-section-title">
          收货地址
          <span class="addr-manage" @click="$router.push('/profile'); showPaymentDialog = false">管理地址 ›</span>
        </div>
        <div v-if="loadingAddresses" class="addr-loading">加载地址中...</div>
        <div v-else-if="addresses.length === 0" class="addr-empty">
          <span>暂无收货地址，</span>
          <span class="addr-add-link" @click="showAddAddrInline = true">立即添加</span>
        </div>
        <div v-else class="addr-list">
          <div
            v-for="addr in addresses"
            :key="addr.id"
            class="addr-item"
            :class="{ selected: selectedAddressId === addr.id }"
            @click="selectedAddressId = addr.id"
          >
            <div class="addr-radio">
              <div class="radio-dot" :class="{ active: selectedAddressId === addr.id }" />
            </div>
            <div class="addr-body">
              <div class="addr-person">
                <span class="addr-name">{{ addr.receiverName }}</span>
                <span class="addr-phone">{{ addr.phone }}</span>
                <span v-if="addr.isDefault" class="addr-default-tag">默认</span>
              </div>
              <div class="addr-text">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detailAddress }}</div>
            </div>
          </div>
        </div>

        <!-- 内联添加地址 -->
        <div v-if="showAddAddrInline" class="inline-addr-form">
          <div class="inline-addr-title">新增收货地址</div>
          <div class="inline-form-grid">
            <input v-model="newAddr.receiverName" placeholder="收货人姓名" class="inline-input" />
            <input v-model="newAddr.phone" placeholder="手机号码" class="inline-input" />
            <input v-model="newAddr.province" placeholder="省份" class="inline-input" />
            <input v-model="newAddr.city" placeholder="城市" class="inline-input" />
            <input v-model="newAddr.district" placeholder="区县" class="inline-input" />
          </div>
          <input v-model="newAddr.detailAddress" placeholder="详细地址（街道、门牌号）" class="inline-input full" />
          <div class="inline-addr-btns">
            <button class="inline-btn cancel" @click="showAddAddrInline = false">取消</button>
            <button class="inline-btn confirm" @click="saveNewAddress" :disabled="savingAddr">保存地址</button>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="pay-section-title">支付方式</div>
        <div class="pay-methods">
          <div v-for="m in paymentMethods" :key="m.value" class="pay-method"
            :class="{ active: paymentMethod === m.value }" @click="paymentMethod = m.value">
            <span class="pay-icon">{{ m.icon }}</span>
            <span>{{ m.label }}</span>
            <span v-if="paymentMethod === m.value" class="pay-check">✓</span>
          </div>
        </div>

        <!-- 碳积分抵扣 -->
        <div class="pay-section-title">碳积分抵扣</div>
        <div class="carbon-redeem-block" v-if="carbonAccount && carbonAccount.availablePoints >= 100">
          <div class="carbon-redeem-info">
            <span class="carbon-redeem-icon">🌱</span>
            <div>
              <div class="carbon-redeem-title">可用 {{ carbonAccount.availablePoints }} 积分</div>
              <div class="carbon-redeem-sub">100积分抵扣1元，本单最多抵扣 {{ Math.min(Math.floor(carbonAccount.availablePoints / 100), 5) }} 元</div>
            </div>
          </div>
          <el-switch v-model="useCarbonPoints" active-color="#52c41a" />
        </div>
        <div class="carbon-redeem-block disabled" v-else>
          <span class="carbon-redeem-icon">🌱</span>
          <span class="carbon-redeem-sub">{{ carbonAccount ? '积分不足100，无法抵扣（发布闲置、完成交易可获积分）' : '加载积分中...' }}</span>
        </div>

        <!-- 订单汇总 -->
        <div class="pay-summary">
          <div class="summary-row">
            <span>商品金额</span>
            <span>¥{{ formatPrice(product?.price) }}</span>
          </div>
          <div class="summary-row" v-if="useCarbonPoints && carbonPointsToUse > 0">
            <span>碳积分抵扣 <span class="discount-pts">-{{ carbonPointsToUse }}积分</span></span>
            <span class="discount-val">-¥{{ carbonDiscount.toFixed(2) }}</span>
          </div>
          <div class="summary-row">
            <span>运费</span>
            <span class="free">免运费</span>
          </div>
          <div class="summary-row total">
            <span>实付金额</span>
            <span class="total-price">¥{{ finalPrice }}</span>
          </div>
        </div>
      </div>

      <!-- Step 1: 支付中 -->
      <div v-if="paymentStep === 1" class="pay-loading">
        <div class="pay-spinner" />
        <p>支付处理中，请稍候...</p>
        <p class="pay-hint">请勿关闭此窗口</p>
      </div>

      <!-- Step 2: 支付成功 -->
      <div v-if="paymentStep === 2" class="pay-success">
        <div class="success-icon">✅</div>
        <h3>支付成功</h3>
        <p class="success-amount">¥{{ finalPrice }}</p>
        <div class="success-addr" v-if="selectedAddress">
          <span>📦 将寄送至：</span>
          <span>{{ selectedAddress.receiverName }} {{ selectedAddress.phone }}</span>
          <span>{{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }}{{ selectedAddress.detailAddress }}</span>
        </div>
        <p class="success-hint">订单已生成，感谢您的购买</p>
      </div>

      <template #footer>
        <div v-if="paymentStep === 0" class="pay-footer">
          <el-button @click="showPaymentDialog = false">取消</el-button>
          <el-button type="primary" :loading="paymentLoading" :disabled="addresses.length > 0 && !selectedAddressId" @click="confirmPayment">
            确认支付 ¥{{ finalPrice }}
          </el-button>
        </div>
        <div v-if="paymentStep === 2">
          <el-button type="primary" @click="goToOrders">查看订单</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 联系卖家弹窗 -->
    <el-dialog v-model="showContactDialog" title="发消息给卖家" width="480px">
      <el-form :model="contactForm" label-position="top">
        <el-form-item label="留言内容">
          <el-input v-model="contactForm.message" type="textarea" :rows="4" placeholder="描述您想咨询的问题，如：成色、配件、能否议价等..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showContactDialog = false">取消</el-button>
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductById, getProducts } from '../api/product'
import { createChatSession } from '../api/chat'
import { addToCart, removeFromCart } from '../api/cart'
import { createOrder, payOrder } from '../api/order'
import { createPayment, processPayment } from '../api/payment'
import { getAddresses, createAddress } from '../api/address'
import { useUserStore } from '../stores/user'
import FavoriteButton from '../components/FavoriteButton.vue'
import { getProductReviews } from '../api/review'
import { getCarbonAccount } from '../api/carbon'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const relatedProducts = ref([])
const reviews = ref([])
const avgRating = computed(() => {
  if (!reviews.value.length) return 0
  return (reviews.value.reduce((s, r) => s + r.rating, 0) / reviews.value.length).toFixed(1)
})
const loading = ref(false)
const showImageViewer = ref(false)
const showContactDialog = ref(false)
const showPaymentDialog = ref(false)
const currentImage = ref('')
const paymentStep = ref(0)
const paymentMethod = ref('ALIPAY')
const paymentLoading = ref(false)
const contactForm = ref({ message: '' })

// 地址相关
const addresses = ref([])
const loadingAddresses = ref(false)
const selectedAddressId = ref(null)
const showAddAddrInline = ref(false)
const savingAddr = ref(false)
const newAddr = ref({ receiverName: '', phone: '', province: '', city: '', district: '', detailAddress: '' })

// 碳积分抵扣
const carbonAccount = ref(null)
const useCarbonPoints = ref(false)
const carbonPointsToUse = computed(() => {
  if (!useCarbonPoints.value || !carbonAccount.value) return 0
  const maxByBalance = Math.floor(carbonAccount.value.availablePoints / 100) * 100
  return Math.min(maxByBalance, 500) // 最多500积分=5元
})
const carbonDiscount = computed(() => carbonPointsToUse.value / 100)
const finalPrice = computed(() => {
  const base = Number(product.value?.price || 0)
  return Math.max(0, base - carbonDiscount.value).toFixed(2)
})

const selectedAddress = computed(() => addresses.value.find(a => a.id === selectedAddressId.value))
const payDialogTitle = computed(() => ['确认订单', '支付中...', '支付成功'][paymentStep.value])

const paymentMethods = [
  { value: 'ALIPAY', label: '支付宝', icon: '💙' },
  { value: 'WECHAT', label: '微信支付', icon: '💚' },
  { value: 'BANK_CARD', label: '银行卡', icon: '💳' }
]

const isOwnProduct = computed(() => product.value?.seller?.id === userStore.user?.id)

const loadProduct = async () => {
  try {
    loading.value = true
    product.value = await getProductById(route.params.id)
    currentImage.value = product.value.images?.[0] || 'https://picsum.photos/seed/electronics/600/450'
    loadRelatedProducts()
    // 加载评价
    try { reviews.value = await getProductReviews(product.value.id) } catch { /* ignore */ }
  } catch {
    ElMessage.error('商品不存在或已下架')
    router.push('/')
  } finally {
    loading.value = false
  }
}

const loadRelatedProducts = async () => {
  if (!product.value?.category?.id) return
  try {
    const data = await getProducts({ categoryId: product.value.category.id, page: 0, size: 4 })
    relatedProducts.value = (data.content || []).filter(i => i.id !== product.value.id).slice(0, 4)
  } catch { /* ignore */ }
}

const loadAddressesForPay = async () => {
  loadingAddresses.value = true
  try {
    addresses.value = await getAddresses() || []
    // 默认选中默认地址
    const def = addresses.value.find(a => a.isDefault)
    selectedAddressId.value = def?.id || addresses.value[0]?.id || null
  } catch { /* ignore */ } finally {
    loadingAddresses.value = false
  }
}

const saveNewAddress = async () => {
  const { receiverName, phone, province, city, district, detailAddress } = newAddr.value
  if (!receiverName || !phone || !province || !city || !district || !detailAddress) {
    ElMessage.warning('请填写完整地址信息'); return
  }
  savingAddr.value = true
  try {
    const saved = await createAddress({ ...newAddr.value, isDefault: addresses.value.length === 0 })
    await loadAddressesForPay()
    selectedAddressId.value = saved?.id || addresses.value[addresses.value.length - 1]?.id
    showAddAddrInline.value = false
    newAddr.value = { receiverName: '', phone: '', province: '', city: '', district: '', detailAddress: '' }
    ElMessage.success('地址已保存')
  } catch { ElMessage.error('保存地址失败') } finally { savingAddr.value = false }
}

const contactSeller = () => {
  if (!userStore.token) { ElMessage.warning('请先登录'); router.push('/login'); return }
  if (isOwnProduct.value) { ElMessage.warning('不能联系自己'); return }
  showContactDialog.value = true
}

const handlePurchase = () => {
  if (!userStore.token) { ElMessage.warning('请先登录'); router.push('/login'); return }
  showPaymentDialog.value = true
  paymentStep.value = 0
  paymentMethod.value = 'ALIPAY'
  useCarbonPoints.value = false
  loadAddressesForPay()
  // 加载碳积分账户
  getCarbonAccount().then(data => { carbonAccount.value = data }).catch(() => {})
}

const handleAddToCart = async () => {
  if (!userStore.token) { ElMessage.warning('请先登录'); router.push('/login'); return }
  try {
    await addToCart(product.value.id)
    ElMessage.success('已加入购物车')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加入购物车失败')
  }
}

const confirmPayment = async () => {
  if (!selectedAddressId.value && addresses.value.length > 0) {
    ElMessage.warning('请选择收货地址'); return
  }
  paymentLoading.value = true
  paymentStep.value = 1
  try {
    // 1. 创建订单（状态 CREATED，商品不变）
    const orderData = { productId: product.value.id }
    if (selectedAddressId.value) orderData.addressId = selectedAddressId.value
    if (carbonPointsToUse.value > 0) orderData.carbonPoints = carbonPointsToUse.value
    const order = await createOrder(orderData)
    // 2. 模拟支付
    const payment = await createPayment(order.id, paymentMethod.value)
    await processPayment(payment.paymentNumber)
    try { await removeFromCart(product.value.id) } catch { /* ignore */ }
    product.value.status = 'SOLD'
    paymentStep.value = 2
  } catch (e) {
    paymentStep.value = 0
    ElMessage.error(e?.response?.data?.message || '支付失败，请重试')
  } finally {
    paymentLoading.value = false
  }
}

const goToOrders = () => { showPaymentDialog.value = false; router.push('/orders') }

const sendMessage = async () => {
  if (!contactForm.value.message.trim()) { ElMessage.warning('请输入留言内容'); return }
  try {
    await createChatSession({ productId: product.value.id, initialMessage: contactForm.value.message })
    ElMessage.success('消息已发送')
    showContactDialog.value = false
    contactForm.value.message = ''
    router.push('/chat')
  } catch { ElMessage.error('发送失败，请稍后重试') }
}

const viewProduct = (id) => router.push(`/product/${id}`)
const getCurrentImageIndex = () => product.value?.images?.findIndex(img => img === currentImage.value) || 0
const getProductImage = (item) => item.images?.[0] || `https://picsum.photos/seed/${item.id}/200/150`
const getStatusText = (s) => ({ PENDING: '待审核', APPROVED: '在售', REJECTED: '已拒绝', SOLD: '已售出' }[s] || s)
const getConditionText = (c) => ({ NEW: '全新', LIKE_NEW: '99新', EXCELLENT: '95新', GOOD: '9成新', FAIR: '8成新' }[c] || c)
const formatPrice = (p) => Number(p || 0).toFixed(2)
const formatDate = (d) => new Date(d).toLocaleDateString('zh-CN')

onMounted(loadProduct)

// 路由参数变化时重新加载（相关推荐跳转同一组件时触发）
watch(() => route.params.id, (newId) => {
  if (newId) {
    product.value = null
    relatedProducts.value = []
    showPaymentDialog.value = false
    loadProduct()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
})
</script>

<style scoped>
.detail-page { max-width: 1200px; margin: 0 auto; padding: 20px; }
.detail-wrap { }

.breadcrumb {
  font-size: 13px;
  color: #999;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.breadcrumb span:not(.sep):not(.current) { cursor: pointer; }
.breadcrumb span:not(.sep):not(.current):hover { color: #e4393c; }
.sep { color: #ddd; }
.current { color: #333; }

.detail-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  margin-bottom: 28px;
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.07);
}

/* 图片区 */
.main-img-wrap {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f5f5;
  margin-bottom: 12px;
}
.main-img {
  width: 100%;
  height: 380px;
  object-fit: contain;
  cursor: zoom-in;
  display: block;
}
.img-zoom-hint {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(0,0,0,0.45);
  color: #fff;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 20px;
}
.sold-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  font-weight: 700;
}
.thumb-list { display: flex; gap: 8px; flex-wrap: wrap; }
.thumb {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  transition: border-color 0.15s;
}
.thumb.active { border-color: #e4393c; }
.thumb img { width: 100%; height: 100%; object-fit: cover; }

/* 信息区 */
.info-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 12px; }
.tag-status, .tag-cond, .tag-cat {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 4px;
}
.tag-status.approved { background: #f0f9eb; color: #67c23a; }
.tag-status.sold { background: #f4f4f5; color: #909399; }
.tag-status.pending { background: #fdf6ec; color: #e6a23c; }
.tag-cond { background: #ecf5ff; color: #409eff; }
.tag-cat { background: #f5f5f5; color: #666; }

.prod-title { font-size: 22px; font-weight: 700; color: #1a1a2e; margin: 0 0 20px; line-height: 1.4; }

.price-block {
  background: linear-gradient(135deg, #fff5f5, #fff);
  border: 1px solid #ffe0e0;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
}
.price-main { display: flex; align-items: baseline; gap: 4px; }
.price-symbol { font-size: 18px; color: #e4393c; font-weight: 600; }
.price-num { font-size: 36px; color: #e4393c; font-weight: 700; }
.price-hint { font-size: 12px; color: #bbb; margin-top: 4px; }

.seller-block {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #f8f9fa;
  border-radius: 10px;
  margin-bottom: 20px;
}
.seller-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}
.seller-avatar-img {
  width: 100%; height: 100%;
  object-fit: cover;
}
.seller-info { flex: 1; }
.seller-name { font-size: 15px; font-weight: 600; color: #333; }
.seller-label { font-size: 12px; color: #999; }
.btn-chat {
  padding: 8px 16px;
  background: #fff;
  border: 1.5px solid #e4393c;
  color: #e4393c;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
  flex-shrink: 0;
}
.btn-chat:hover:not(:disabled) { background: #e4393c; color: #fff; }
.btn-chat:disabled { opacity: 0.4; cursor: not-allowed; }

.attr-list { margin-bottom: 20px; }
.attr-item { display: flex; padding: 8px 0; border-bottom: 1px solid #f5f5f5; font-size: 14px; }
.attr-key { width: 80px; color: #999; flex-shrink: 0; }
.attr-val { color: #333; }

.action-btns { display: flex; gap: 12px; margin-bottom: 16px; }
.btn-cart, .btn-buy {
  flex: 1;
  height: 48px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.15s;
}
.btn-cart { background: #fff3e0; color: #e6a23c; border: 1.5px solid #e6a23c; }
.btn-cart:hover:not(:disabled) { background: #e6a23c; color: #fff; }
.btn-buy { background: #e4393c; color: #fff; }
.btn-buy:hover:not(:disabled) { background: #c0292b; }
.btn-cart:disabled, .btn-buy:disabled { opacity: 0.5; cursor: not-allowed; }

.status-notice {
  padding: 14px;
  background: #f4f4f5;
  border-radius: 8px;
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin-bottom: 16px;
}

.fav-row { display: flex; align-items: center; gap: 10px; }
.fav-hint { font-size: 12px; color: #bbb; }

/* 底部 */
.detail-bottom { display: grid; grid-template-columns: 1fr 320px; gap: 20px; }
.desc-card, .related-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
}
.card-title { font-size: 16px; font-weight: 700; color: #1a1a2e; margin-bottom: 16px; padding-bottom: 10px; border-bottom: 2px solid #f0f0f0; }
.desc-text { font-size: 14px; line-height: 1.8; color: #555; white-space: pre-wrap; margin: 0; }

.related-grid { display: flex; flex-direction: column; gap: 12px; }
.related-item { display: flex; gap: 12px; cursor: pointer; padding: 8px; border-radius: 8px; transition: background 0.15s; }
.related-item:hover { background: #f8f9fa; }
.related-item img { width: 64px; height: 48px; object-fit: cover; border-radius: 6px; flex-shrink: 0; }
.related-info { flex: 1; min-width: 0; }
.related-title { font-size: 13px; color: #333; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.related-price { font-size: 14px; font-weight: 700; color: #e4393c; }

/* 支付弹窗 */
.pay-step { padding: 4px 0; }

.pay-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #888;
  margin: 16px 0 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.pay-section-title:first-child { margin-top: 0; }
.addr-manage { font-size: 12px; color: #e4393c; cursor: pointer; font-weight: 400; text-transform: none; }
.addr-manage:hover { text-decoration: underline; }

.pay-product { display: flex; gap: 14px; align-items: center; padding: 14px; background: #f8f9fa; border-radius: 10px; }
.pay-img { width: 72px; height: 72px; object-fit: cover; border-radius: 8px; flex-shrink: 0; }
.pay-prod-detail { flex: 1; }
.pay-prod-name { font-size: 14px; font-weight: 600; color: #222; margin-bottom: 4px; }
.pay-prod-meta { display: flex; gap: 6px; margin-bottom: 6px; }
.pay-prod-meta span { font-size: 11px; background: #eee; color: #666; padding: 2px 7px; border-radius: 4px; }
.pay-amount { font-size: 20px; font-weight: 700; color: #e4393c; }

.addr-loading { color: #999; font-size: 13px; padding: 12px 0; }
.addr-empty { font-size: 13px; color: #999; padding: 12px 0; }
.addr-add-link { color: #e4393c; cursor: pointer; text-decoration: underline; }

.addr-list { display: flex; flex-direction: column; gap: 8px; max-height: 180px; overflow-y: auto; }
.addr-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 14px;
  border: 1.5px solid #e8e8e8;
  border-radius: 10px;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}
.addr-item:hover { border-color: #e4393c; background: #fff8f8; }
.addr-item.selected { border-color: #e4393c; background: #fff8f8; }
.addr-radio { padding-top: 2px; flex-shrink: 0; }
.radio-dot {
  width: 16px; height: 16px;
  border-radius: 50%;
  border: 2px solid #ddd;
  transition: all 0.15s;
  position: relative;
}
.radio-dot.active { border-color: #e4393c; }
.radio-dot.active::after {
  content: '';
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  background: #e4393c;
}
.addr-body { flex: 1; }
.addr-person { display: flex; align-items: center; gap: 8px; margin-bottom: 3px; }
.addr-name { font-size: 14px; font-weight: 600; color: #222; }
.addr-phone { font-size: 13px; color: #666; }
.addr-default-tag { font-size: 11px; background: #e4393c; color: #fff; padding: 1px 6px; border-radius: 3px; }
.addr-text { font-size: 12px; color: #888; line-height: 1.5; }

/* 内联添加地址 */
.inline-addr-form {
  border: 1.5px dashed #e4393c;
  border-radius: 10px;
  padding: 14px;
  margin-top: 8px;
  background: #fff8f8;
}
.inline-addr-title { font-size: 13px; font-weight: 600; color: #e4393c; margin-bottom: 10px; }
.inline-form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 8px; }
.inline-input {
  width: 100%;
  height: 34px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 0 10px;
  font-size: 13px;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.15s;
}
.inline-input:focus { border-color: #e4393c; }
.inline-input.full { width: 100%; margin-bottom: 10px; }
.inline-addr-btns { display: flex; gap: 8px; justify-content: flex-end; }
.inline-btn {
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  border: none;
  transition: opacity 0.15s;
}
.inline-btn.cancel { background: #f5f5f5; color: #666; }
.inline-btn.confirm { background: #e4393c; color: #fff; }
.inline-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.pay-methods { display: flex; gap: 8px; }
.pay-method {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 8px;
  border: 1.5px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  font-size: 13px;
}
.pay-method:hover { border-color: #409eff; }
.pay-method.active { border-color: #409eff; background: #ecf5ff; }
.pay-icon { font-size: 18px; }
.pay-check { color: #409eff; font-weight: 700; font-size: 12px; }

/* 碳积分抵扣 */
.carbon-redeem-block {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
  margin-bottom: 4px;
}
.carbon-redeem-block.disabled {
  background: #fafafa;
  border-color: #e8e8e8;
  gap: 8px;
}
.carbon-redeem-info { display: flex; align-items: center; gap: 10px; }
.carbon-redeem-icon { font-size: 22px; }
.carbon-redeem-title { font-size: 13px; font-weight: 600; color: #135200; }
.carbon-redeem-sub { font-size: 12px; color: #8c8c8c; margin-top: 2px; }
.discount-pts { font-size: 11px; color: #52c41a; background: #f6ffed; padding: 1px 6px; border-radius: 4px; margin-left: 4px; }
.discount-val { color: #52c41a; font-weight: 600; }

.pay-summary {
  margin-top: 16px;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
  padding: 4px 0;
}
.summary-row.total { font-size: 15px; font-weight: 700; color: #222; margin-top: 6px; padding-top: 8px; border-top: 1px solid #f0f0f0; }
.free { color: #67c23a; }
.total-price { color: #e4393c; font-size: 18px; }

.pay-footer { display: flex; justify-content: flex-end; gap: 10px; }

.pay-loading { text-align: center; padding: 40px 0; }
.pay-spinner {
  width: 48px; height: 48px;
  border: 4px solid #f0f0f0;
  border-top-color: #409eff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px;
}
@keyframes spin { to { transform: rotate(360deg); } }
.pay-loading p { color: #666; margin: 4px 0; }
.pay-hint { font-size: 12px; color: #bbb; }

.pay-success { text-align: center; padding: 28px 0; }
.success-icon { font-size: 56px; margin-bottom: 12px; }
.pay-success h3 { font-size: 20px; color: #333; margin: 0 0 6px; }
.success-amount { font-size: 26px; font-weight: 700; color: #e4393c; margin: 0 0 14px; }
.success-addr {
  display: flex;
  flex-direction: column;
  gap: 3px;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px 16px;
  margin: 0 auto 12px;
  max-width: 360px;
  text-align: left;
  font-size: 13px;
  color: #555;
}
.success-hint { color: #999; font-size: 13px; margin: 0; }

@media (max-width: 900px) {
  .detail-main { grid-template-columns: 1fr; }
  .detail-bottom { grid-template-columns: 1fr; }
  .main-img { height: 260px; }
}

/* 评价区 */
.reviews-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
  margin-top: 20px;
}
.review-count { font-size: 13px; color: #999; font-weight: 400; margin-left: 8px; }
.avg-rating { font-size: 13px; color: #f5a623; font-weight: 600; margin-left: 12px; }
.review-list { display: flex; flex-direction: column; gap: 16px; margin-top: 16px; }
.review-item { padding: 14px; background: #fafafa; border-radius: 8px; }
.review-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.reviewer-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; font-size: 13px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.reviewer-info { flex: 1; display: flex; align-items: center; gap: 8px; }
.reviewer-name { font-size: 14px; font-weight: 600; color: #333; }
.review-stars { color: #f5a623; font-size: 14px; }
.review-date { font-size: 12px; color: #bbb; }
.review-content { font-size: 14px; color: #555; line-height: 1.6; margin: 0; }
</style>
