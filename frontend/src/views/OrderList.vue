<template>
  <div class="orders-page">
    <!-- 顶部 -->
    <div class="orders-header">
      <div class="header-left">
        <h1>我的订单</h1>
        <span class="order-total" v-if="allOrders.length">共 {{ allOrders.length }} 笔订单</span>
      </div>
      <el-input v-model="searchKeyword" placeholder="搜索订单号或商品名称" clearable @clear="loadOrders" @keyup.enter="handleSearch" style="width:240px">
        <template #prefix>🔍</template>
      </el-input>
    </div>

    <!-- 状态 Tab -->
    <div class="order-tabs">
      <div v-for="tab in tabs" :key="tab.value" class="order-tab" :class="{ active: activeTab === tab.value }" @click="handleTabChange(tab.value)">
        {{ tab.label }}
        <span v-if="tab.count > 0" class="tab-dot">{{ tab.count }}</span>
      </div>
    </div>

    <!-- 列表 -->
    <div v-loading="loading" class="orders-body">
      <div v-if="orders.length === 0 && !loading" class="orders-empty">
        <div class="empty-icon">📦</div>
        <p>暂无订单记录</p>
        <button class="btn-go" @click="$router.push('/')">去逛逛</button>
      </div>

      <div v-else class="orders-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <!-- 订单头 -->
          <div class="order-head">
            <div class="order-head-left">
              <div class="order-tags">
                <span class="order-no">订单号：{{ order.orderNumber }}</span>
                <span class="identity-tag" :class="isSeller(order) ? 'seller' : 'buyer'">
                  {{ isSeller(order) ? '我是卖家' : '我是买家' }}
                </span>
              </div>
              <span class="order-time">{{ formatDateTime(order.createdAt) }}</span>
            </div>
            <div class="order-status" :class="order.status?.toLowerCase()">
              {{ getStatusText(order.status) }}
            </div>
          </div>

          <!-- 商品行 -->
          <div class="order-product" @click="viewProduct(order.product?.id)">
            <img :src="getProductImage(order.product)" :alt="order.product?.title" class="order-prod-img" />
            <div class="order-prod-info">
              <div class="order-prod-title">{{ order.product?.title }}</div>
              <div class="order-prod-desc">{{ truncate(order.product?.description, 60) }}</div>
            </div>
            <div class="order-prod-price">¥{{ formatPrice(order.amount) }}</div>
          </div>

          <!-- 物流提示 -->
          <div class="order-logistics-tip" v-if="order.status === 'PAID' || order.status === 'SHIPPED'">
            <template v-if="order.status === 'PAID'">
              <span v-if="isBuyer(order)" class="tip-waiting">⏳ 等待卖家发货...</span>
              <span v-if="isSeller(order)" class="tip-action">📦 买家已付款，请尽快发货</span>
            </template>
            <template v-if="order.status === 'SHIPPED'">
              <span v-if="isBuyer(order)" class="tip-action">🚚 卖家已发货，请注意查收</span>
              <span v-if="isSeller(order)" class="tip-waiting">⏳ 等待买家确认收货...</span>
            </template>
          </div>

          <!-- 订单底部 -->
          <div class="order-foot">
            <div class="order-sum">合计：<strong>¥{{ formatPrice(order.amount) }}</strong></div>
            <div class="order-ops">
              <button class="op-btn" @click="viewOrderDetail(order)">详情</button>
              <!-- 买家：待支付 -->
              <button class="op-btn primary" v-if="order.status === 'CREATED' && isBuyer(order)" @click="handlePayOrder(order)">立即付款</button>
              <!-- 卖家：待发货 -->
              <button class="op-btn primary" v-if="order.status === 'PAID' && isSeller(order)" @click="handleShipOrder(order)">确认发货</button>
              <!-- 买家：待收货 -->
              <button class="op-btn success" v-if="order.status === 'SHIPPED' && isBuyer(order)" @click="handleConfirmReceived(order)">确认收货</button>
              <!-- 买家：可取消（待支付阶段） -->
              <button class="op-btn danger" v-if="order.status === 'CREATED' && isBuyer(order)" @click="handleCancelOrder(order)">取消订单</button>
              <!-- 联系对方 -->
              <button class="op-btn" v-if="order.status === 'COMPLETED'" @click="contactOther(order)">联系对方</button>
              <!-- 买家：已完成可评价/投诉 -->
              <button class="op-btn review" v-if="order.status === 'COMPLETED' && isBuyer(order) && !reviewedOrders.has(order.id)" @click="openReviewDialog(order)">评价</button>
              <button class="op-btn warn" v-if="order.status === 'COMPLETED' && isBuyer(order)" @click="openComplaintDialog(order)">投诉</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="orders-pagination" v-if="total > pageSize">
      <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total" layout="prev, pager, next" background @current-change="handlePageChange" />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showOrderDetailDialog" title="订单详情" width="600px">
      <div v-if="currentOrderDetail" class="detail-dialog">
        <el-steps :active="getStepActive(currentOrderDetail.status)" finish-status="success" align-center style="margin-bottom:24px">
          <el-step title="待支付" />
          <el-step title="待发货" />
          <el-step title="待收货" />
          <el-step title="已完成" />
        </el-steps>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号" :span="2">{{ currentOrderDetail.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDateTime(currentOrderDetail.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">
            <span style="color:#e4393c;font-weight:700;font-size:16px">¥{{ formatPrice(currentOrderDetail.amount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="订单状态" :span="2">
            <span class="order-status" :class="currentOrderDetail.status?.toLowerCase()" style="display:inline-block;padding:3px 12px;border-radius:20px;font-size:13px">
              {{ getStatusText(currentOrderDetail.status) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="物流状态" :span="2">
            <span style="color:#666;font-size:13px">{{ getLogisticsText(currentOrderDetail) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="我的身份" :span="2">
            <el-tag :type="isSeller(currentOrderDetail) ? 'warning' : 'success'" size="small">
              {{ isSeller(currentOrderDetail) ? '卖家' : '买家' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="对方信息" :span="2" v-if="isSeller(currentOrderDetail) && currentOrderDetail.buyer">
            <span>买家：{{ currentOrderDetail.buyer.username || '未知' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="对方信息" :span="2" v-if="isBuyer(currentOrderDetail) && currentOrderDetail.seller">
            <span>卖家：{{ currentOrderDetail.seller.username || '未知' }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="currentOrderDetail.product" class="detail-prod">
          <img :src="getProductImage(currentOrderDetail.product)" class="detail-prod-img" />
          <div>
            <div style="font-weight:600;margin-bottom:4px">{{ currentOrderDetail.product.title }}</div>
            <div style="color:#999;font-size:13px">{{ truncate(currentOrderDetail.product.description, 80) }}</div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="showReviewDialog" title="评价商品" width="480px">
      <div class="review-form">
        <div class="review-product" v-if="reviewOrder">
          <img :src="getProductImage(reviewOrder.product)" class="review-prod-img" />
          <span class="review-prod-title">{{ reviewOrder.product?.title }}</span>
        </div>
        <div class="rating-row">
          <span class="rating-label">评分</span>
          <div class="stars">
            <span
              v-for="n in 5" :key="n"
              class="star" :class="{ active: reviewForm.rating >= n }"
              @click="reviewForm.rating = n"
            >★</span>
          </div>
          <span class="rating-text">{{ ['','很差','较差','一般','不错','非常好'][reviewForm.rating] }}</span>
        </div>
        <el-input
          v-model="reviewForm.content"
          type="textarea"
          :rows="4"
          placeholder="分享你的购买体验，帮助其他买家做决策..."
          maxlength="500"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button type="primary" :loading="submittingReview" @click="handleSubmitReview">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- 投诉弹窗 -->
    <el-dialog v-model="showComplaintDialog" title="投诉商家" width="480px">
      <div class="complaint-form">
        <el-select v-model="complaintForm.reason" placeholder="请选择投诉原因" style="width:100%;margin-bottom:16px">
          <el-option label="商品与描述不符" value="商品与描述不符" />
          <el-option label="商品存在质量问题" value="商品存在质量问题" />
          <el-option label="卖家态度恶劣" value="卖家态度恶劣" />
          <el-option label="虚假发货" value="虚假发货" />
          <el-option label="其他问题" value="其他问题" />
        </el-select>
        <el-input
          v-model="complaintForm.description"
          type="textarea"
          :rows="4"
          placeholder="请详细描述投诉内容..."
          maxlength="1000"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="showComplaintDialog = false">取消</el-button>
        <el-button type="danger" :loading="submittingComplaint" @click="handleSubmitComplaint">提交投诉</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyOrders, confirmReceipt, cancelOrder, shipOrder } from '../api/order'
import { createPayment, processPayment } from '../api/payment'
import { payOrder } from '../api/order'
import { useUserStore } from '../stores/user'
import { submitReview, hasReviewed, submitComplaint } from '../api/review'
import { createChatSession } from '../api/chat'

const router = useRouter()
const userStore = useUserStore()
const orders = ref([])
const allOrders = ref([])
const loading = ref(false)
const activeTab = ref('all')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showOrderDetailDialog = ref(false)
const currentOrderDetail = ref(null)

// 评价相关
const showReviewDialog = ref(false)
const reviewOrder = ref(null)
const submittingReview = ref(false)
const reviewedOrders = ref(new Set())
const reviewForm = ref({ rating: 5, content: '' })

// 投诉相关
const showComplaintDialog = ref(false)
const complaintOrder = ref(null)
const submittingComplaint = ref(false)
const complaintForm = ref({ reason: '', description: '' })

const tabs = computed(() => [
  { value: 'all', label: '全部', count: 0 },
  { value: 'CREATED', label: '待支付', count: allOrders.value.filter(o => o.status === 'CREATED').length },
  { value: 'PAID', label: '待发货', count: allOrders.value.filter(o => o.status === 'PAID').length },
  { value: 'SHIPPED', label: '待收货', count: allOrders.value.filter(o => o.status === 'SHIPPED').length },
  { value: 'COMPLETED', label: '已完成', count: 0 },
  { value: 'CANCELLED', label: '已取消', count: 0 }
])

const loadOrders = async () => {
  try {
    loading.value = true
    const data = await getMyOrders()
    allOrders.value = data || []
    let filtered = [...allOrders.value]
    if (activeTab.value !== 'all') filtered = filtered.filter(o => o.status === activeTab.value)
    if (searchKeyword.value) {
      const kw = searchKeyword.value.toLowerCase()
      filtered = filtered.filter(o => o.orderNumber?.toLowerCase().includes(kw) || o.product?.title?.toLowerCase().includes(kw))
    }
    orders.value = filtered
    total.value = filtered.length
    // 批量检查已完成订单是否已评价
    const completedIds = allOrders.value.filter(o => o.status === 'COMPLETED').map(o => o.id)
    const checks = await Promise.allSettled(completedIds.map(id => hasReviewed(id)))
    const reviewed = new Set()
    completedIds.forEach((id, i) => {
      if (checks[i].status === 'fulfilled' && checks[i].value?.reviewed) reviewed.add(id)
    })
    reviewedOrders.value = reviewed
  } catch { ElMessage.error('加载订单失败') } finally { loading.value = false }
}

const handleTabChange = (tab) => { activeTab.value = tab; currentPage.value = 1; loadOrders() }
const handleSearch = () => { currentPage.value = 1; loadOrders() }
const handlePageChange = (page) => { currentPage.value = page; loadOrders() }
const viewProduct = (id) => { if (id) router.push(`/product/${id}`) }
const viewOrderDetail = (order) => { currentOrderDetail.value = order; showOrderDetailDialog.value = true }

const contactOther = async (order) => {
  // 通过订单的商品ID创建或获取聊天会话
  try {
    const productId = order.product?.id
    if (!productId) {
      ElMessage.warning('无法获取商品信息')
      router.push('/chat')
      return
    }
    
    // 创建或获取聊天会话
    const session = await createChatSession({
      productId: productId,
      initialMessage: `你好，关于订单 ${order.orderNumber} 的商品「${order.product?.title}」我想咨询一下。`
    })
    
    // 跳转到聊天页面，带上会话ID
    router.push(`/chat?sessionId=${session.id}`)
  } catch (error) {
    console.error('创建聊天会话失败:', error)
    // 失败时跳转到聊天列表
    router.push('/chat')
  }
}

// 判断当前用户身份
const isBuyer = (order) => order.buyer?.id === userStore.user?.id || order.buyer?.username === userStore.username
const isSeller = (order) => order.seller?.id === userStore.user?.id || order.seller?.username === userStore.username

const handlePayOrder = async (order) => {
  try {
    await ElMessageBox.confirm(`确认支付订单 ${order.orderNumber}？`, '确认支付', { confirmButtonText: '确认支付', cancelButtonText: '取消', type: 'warning' })
    const payment = await createPayment(order.id, 'ALIPAY')
    await processPayment(payment.paymentNumber)
    ElMessage.success('支付成功，等待卖家发货')
    loadOrders()
  } catch (e) { if (e !== 'cancel') ElMessage.error('支付失败') }
}

const handleShipOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确认已发货？发货后买家将收到通知。', '确认发货', { confirmButtonText: '确认发货', cancelButtonText: '取消', type: 'info' })
    await shipOrder(order.id)
    ElMessage.success('发货成功，等待买家确认收货')
    loadOrders()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const handleConfirmReceived = async (order) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？确认后订单将完成。', '确认收货', { confirmButtonText: '确认收货', cancelButtonText: '取消', type: 'success' })
    await confirmReceipt(order.id)
    ElMessage.success('确认收货成功，订单已完成')
    loadOrders()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const handleCancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm(`确认取消订单 ${order.orderNumber}？`, '取消订单', { confirmButtonText: '确认取消', cancelButtonText: '不取消', type: 'warning' })
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (e) { if (e !== 'cancel') ElMessage.error('取消失败') }
}

const openReviewDialog = (order) => {
  reviewOrder.value = order
  reviewForm.value = { rating: 5, content: '' }
  showReviewDialog.value = true
}

const handleSubmitReview = async () => {
  if (!reviewForm.value.rating) { ElMessage.warning('请选择评分'); return }
  submittingReview.value = true
  try {
    await submitReview({ orderId: reviewOrder.value.id, rating: reviewForm.value.rating, content: reviewForm.value.content })
    ElMessage.success('评价提交成功')
    reviewedOrders.value.add(reviewOrder.value.id)
    showReviewDialog.value = false
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '提交失败')
  } finally { submittingReview.value = false }
}

const openComplaintDialog = (order) => {
  complaintOrder.value = order
  complaintForm.value = { reason: '', description: '' }
  showComplaintDialog.value = true
}

const handleSubmitComplaint = async () => {
  if (!complaintForm.value.reason) { ElMessage.warning('请选择投诉原因'); return }
  submittingComplaint.value = true
  try {
    await submitComplaint({ orderId: complaintOrder.value.id, reason: complaintForm.value.reason, description: complaintForm.value.description })
    ElMessage.success('投诉已提交，管理员将在3个工作日内处理')
    showComplaintDialog.value = false
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '提交失败')
  } finally { submittingComplaint.value = false }
}

const getStepActive = (s) => ({ CREATED: 1, PAID: 2, SHIPPED: 3, COMPLETED: 4, CANCELLED: 0 }[s] ?? 0)
const getProductImage = (p) => p?.images?.[0] || 'https://picsum.photos/seed/electronics/200/150'
const getStatusText = (s) => ({ CREATED: '待支付', PAID: '待发货', SHIPPED: '待收货', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s)
const getLogisticsText = (order) => {
  if (!order) return '-'
  const status = order.status
  const asBuyer = isBuyer(order)
  
  const texts = {
    CREATED: asBuyer ? '您尚未付款，请尽快付款' : '买家尚未付款，等待买家付款',
    PAID: asBuyer ? '您已付款，等待卖家发货' : '买家已付款，请尽快发货',
    SHIPPED: asBuyer ? '卖家已发货，请注意查收' : '您已发货，等待买家确认收货',
    COMPLETED: '交易已完成',
    CANCELLED: '订单已取消'
  }
  return texts[status] || '-'
}
const formatPrice = (p) => Number(p || 0).toFixed(2)
const formatDateTime = (d) => { try { return new Date(d).toLocaleString('zh-CN') } catch { return '' } }
const truncate = (t, n) => !t ? '' : t.length > n ? t.slice(0, n) + '...' : t

onMounted(loadOrders)
</script>

<style scoped>
.orders-page { max-width: 900px; margin: 0 auto; padding: 24px 20px 48px; }

.orders-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-left { display: flex; align-items: baseline; gap: 12px; }
.orders-header h1 { font-size: 22px; font-weight: 700; color: #1a1a2e; margin: 0; }
.order-total { font-size: 13px; color: #999; }

.order-tabs {
  display: flex;
  background: #fff;
  border-radius: 12px;
  padding: 6px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  gap: 4px;
}
.order-tab {
  flex: 1;
  text-align: center;
  padding: 10px 8px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.order-tab:hover { background: #f5f5f5; color: #333; }
.order-tab.active { background: #e4393c; color: #fff; font-weight: 600; }
.tab-dot {
  background: rgba(255,255,255,0.35);
  border-radius: 10px;
  padding: 0 6px;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
}
.order-tab:not(.active) .tab-dot { background: #f56c6c; color: #fff; }

.orders-body { min-height: 300px; }
.orders-empty { text-align: center; padding: 80px 0; }
.empty-icon { font-size: 56px; margin-bottom: 12px; }
.orders-empty p { color: #999; margin-bottom: 20px; }
.btn-go { padding: 10px 28px; background: #e4393c; color: #fff; border: none; border-radius: 24px; font-size: 14px; cursor: pointer; }

.orders-list { display: flex; flex-direction: column; gap: 16px; }

.order-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07);
  transition: box-shadow 0.2s;
}
.order-card:hover { box-shadow: 0 4px 18px rgba(0,0,0,0.11); }

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px 12px;
  border-bottom: 1px solid #f5f5f5;
  background: #fafafa;
}
.order-head-left { display: flex; flex-direction: column; gap: 4px; }
.order-tags { display: flex; align-items: center; gap: 8px; }
.order-no { font-size: 13px; font-weight: 600; color: #333; font-family: monospace; }
.order-time { font-size: 12px; color: #bbb; }

.identity-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}
.identity-tag.buyer {
  background: #e6f7ff;
  color: #1890ff;
}
.identity-tag.seller {
  background: #fff7e6;
  color: #fa8c16;
}

/* 物流提示 */
.order-logistics-tip {
  padding: 10px 18px;
  background: #f8f9fa;
  border-bottom: 1px solid #f0f0f0;
}
.order-logistics-tip span {
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.tip-waiting {
  color: #999;
}
.tip-action {
  color: #e6a23c;
  font-weight: 500;
}

.order-status {
  font-size: 13px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
}
.order-status.created   { background: #fdf6ec; color: #e6a23c; }
.order-status.paid      { background: #ecf5ff; color: #409eff; }
.order-status.shipped   { background: #f0f4ff; color: #6366f1; }
.order-status.completed { background: #f0f9eb; color: #67c23a; }
.order-status.cancelled { background: #fef0f0; color: #f56c6c; }

.order-product {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  cursor: pointer;
  transition: background 0.15s;
}
.order-product:hover { background: #fafafa; }
.order-prod-img { width: 72px; height: 54px; object-fit: cover; border-radius: 8px; flex-shrink: 0; }
.order-prod-info { flex: 1; min-width: 0; }
.order-prod-title { font-size: 15px; font-weight: 600; color: #222; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 4px; }
.order-prod-desc { font-size: 12px; color: #999; }
.order-prod-price { font-size: 18px; font-weight: 700; color: #e4393c; flex-shrink: 0; }

.order-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 18px 14px;
  border-top: 1px solid #f5f5f5;
}
.order-sum { font-size: 14px; color: #666; }
.order-sum strong { color: #e4393c; font-size: 16px; }
.order-ops { display: flex; gap: 8px; }

.op-btn {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid #e8e8e8;
  background: #fff;
  color: #555;
  transition: all 0.15s;
}
.op-btn:hover { border-color: #333; color: #333; }
.op-btn.primary { background: #e4393c; border-color: #e4393c; color: #fff; }
.op-btn.primary:hover { background: #c0292b; }
.op-btn.success { background: #67c23a; border-color: #67c23a; color: #fff; }
.op-btn.success:hover { background: #5aad33; }
.op-btn.danger { color: #f56c6c; border-color: #f56c6c; }
.op-btn.danger:hover { background: #fef0f0; }

.op-btn.review { background: #f0f9eb; color: #67c23a; border-color: #67c23a; }
.op-btn.review:hover { background: #67c23a; color: #fff; }
.op-btn.warn { background: #fff7e6; color: #e6a23c; border-color: #e6a23c; }
.op-btn.warn:hover { background: #e6a23c; color: #fff; }

/* 评价弹窗 */
.review-form { padding: 4px 0; }
.review-product { display: flex; align-items: center; gap: 12px; padding: 12px; background: #f8f9fa; border-radius: 8px; margin-bottom: 16px; }
.review-prod-img { width: 56px; height: 42px; object-fit: cover; border-radius: 6px; flex-shrink: 0; }
.review-prod-title { font-size: 14px; font-weight: 600; color: #333; }
.rating-row { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.rating-label { font-size: 14px; color: #666; flex-shrink: 0; }
.stars { display: flex; gap: 4px; }
.star { font-size: 28px; color: #ddd; cursor: pointer; transition: color 0.15s; user-select: none; }
.star.active { color: #f5a623; }
.star:hover { color: #f5a623; }
.rating-text { font-size: 13px; color: #e6a23c; font-weight: 600; }

/* 投诉弹窗 */
.complaint-form { padding: 4px 0; }

.detail-dialog { padding: 4px 0; }
.detail-prod { display: flex; gap: 14px; align-items: center; margin-top: 20px; padding: 14px; background: #f8f9fa; border-radius: 8px; }
.detail-prod-img { width: 80px; height: 60px; object-fit: cover; border-radius: 6px; flex-shrink: 0; }

@media (max-width: 768px) {
  .orders-page { padding: 12px; }
  .orders-header { flex-direction: column; gap: 12px; align-items: flex-start; }
  .order-tabs { flex-wrap: wrap; }
  .order-tab { flex: none; padding: 8px 12px; }
  .order-foot { flex-direction: column; gap: 10px; align-items: flex-start; }
}
</style>
