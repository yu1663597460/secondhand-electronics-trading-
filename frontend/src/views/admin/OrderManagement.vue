<template>
  <div class="order-mgmt">
    <div class="page-header">
      <h1>订单管理</h1>
      <div class="breadcrumb">管理后台 / 订单管理</div>
    </div>

    <el-tabs v-model="mainTab" class="main-tabs">

      <!-- ── Tab1: 订单管理 ── -->
      <el-tab-pane label="订单管理" name="orders">
        <div class="content-card filter-row">
          <el-input v-model="searchKeyword" placeholder="搜索订单号、买家、卖家、商品" style="width:280px" clearable @input="handleSearch">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="statusFilter" placeholder="全部状态" style="width:140px" clearable @change="loadOrders">
            <el-option label="待支付" value="CREATED" />
            <el-option label="待发货" value="PAID" />
            <el-option label="待收货" value="SHIPPED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
          <el-button type="primary" @click="loadOrders"><el-icon><Refresh /></el-icon> 刷新</el-button>
        </div>

        <div class="content-card">
          <el-table :data="orders" v-loading="loading" style="width:100%" @row-click="openDetail">
            <el-table-column prop="orderNumber" label="订单号" width="200" />
            <el-table-column label="商品" width="220">
              <template #default="{ row }">
                <div class="prod-cell">
                  <img :src="getImg(row.product?.images?.[0])" class="prod-thumb" />
                  <div>
                    <div class="prod-name">{{ row.product?.title }}</div>
                    <div class="prod-price">¥{{ row.amount }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="买家" width="110">
              <template #default="{ row }">{{ row.buyer?.username }}</template>
            </el-table-column>
            <el-table-column label="卖家" width="110">
              <template #default="{ row }">{{ row.seller?.username }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="下单时间" width="160">
              <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="备注" show-overflow-tooltip>
              <template #default="{ row }">{{ row.adminNote || '—' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click.stop="openDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total"
              layout="total, prev, pager, next" background
              @current-change="p => { currentPage = p; loadOrders() }" />
          </div>
        </div>
      </el-tab-pane>

      <!-- ── Tab2: 投诉与评价 ── -->
      <el-tab-pane name="complaints">
        <template #label>
          投诉与评价
          <el-badge v-if="pendingComplaintCount > 0" :value="pendingComplaintCount" type="danger" style="margin-left:6px" />
        </template>

        <!-- 投诉列表 -->
        <div class="section-title">投诉记录</div>
        <div class="content-card">
          <div class="filter-row" style="padding:12px 16px">
            <el-select v-model="complaintStatusFilter" placeholder="全部状态" style="width:130px" clearable @change="loadComplaints">
              <el-option label="待处理" value="PENDING" />
              <el-option label="处理中" value="PROCESSING" />
              <el-option label="已解决" value="RESOLVED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
            <el-button @click="loadComplaints"><el-icon><Refresh /></el-icon> 刷新</el-button>
          </div>
          <el-table :data="complaints" v-loading="loadingComplaints" style="width:100%">
            <el-table-column label="订单号" width="180">
              <template #default="{ row }">{{ row.order?.orderNumber }}</template>
            </el-table-column>
            <el-table-column label="投诉人" width="110">
              <template #default="{ row }">{{ row.complainant?.username }}</template>
            </el-table-column>
            <el-table-column label="被投诉方" width="110">
              <template #default="{ row }">{{ row.respondent?.username }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="投诉原因" width="150" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="cStatusType(row.status)" size="small">{{ cStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="提交时间" width="160">
              <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="success"
                  :disabled="row.status !== 'PENDING' && row.status !== 'PROCESSING'"
                  @click="openHandleDialog(row, 'RESOLVED')">通过</el-button>
                <el-button size="small" type="danger"
                  :disabled="row.status !== 'PENDING' && row.status !== 'PROCESSING'"
                  @click="openHandleDialog(row, 'REJECTED')">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination v-model:current-page="complaintPage" :page-size="20" :total="complaintTotal"
              layout="total, prev, pager, next" background
              @current-change="p => { complaintPage = p; loadComplaints() }" />
          </div>
        </div>

        <!-- 评价列表 -->
        <div class="section-title" style="margin-top:8px">买家评价</div>
        <div class="content-card">
          <el-table :data="reviews" v-loading="loadingReviews" style="width:100%">
            <el-table-column label="商品" width="180" show-overflow-tooltip>
              <template #default="{ row }">{{ row.product?.title }}</template>
            </el-table-column>
            <el-table-column label="买家" width="110">
              <template #default="{ row }">{{ row.buyer?.username }}</template>
            </el-table-column>
            <el-table-column label="卖家" width="110">
              <template #default="{ row }">{{ row.seller?.username }}</template>
            </el-table-column>
            <el-table-column label="评分" width="130">
              <template #default="{ row }">
                <span style="color:#f5a623">{{ '★'.repeat(row.rating) }}{{ '☆'.repeat(5 - row.rating) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.deleted ? 'danger' : 'success'" size="small">{{ row.deleted ? '已删除' : '正常' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="danger" :disabled="row.deleted" @click="doDeleteReview(row)">删除恶评</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination v-model:current-page="reviewPage" :page-size="20" :total="reviewTotal"
              layout="total, prev, pager, next" background
              @current-change="p => { reviewPage = p; loadReviews() }" />
          </div>
        </div>
      </el-tab-pane>

    </el-tabs>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="showDetail" title="订单详情" width="680px" :close-on-click-modal="false">
      <div v-if="detail" class="detail-wrap">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号" :span="2">{{ detail.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.status)" size="small">{{ statusText(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ detail.amount }}</el-descriptions-item>
          <el-descriptions-item label="买家">{{ detail.buyer?.username }}</el-descriptions-item>
          <el-descriptions-item label="卖家">{{ detail.seller?.username }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ fmt(detail.createdAt) }}</el-descriptions-item>
        </el-descriptions>
        <div class="prod-detail-row" v-if="detail.product">
          <img :src="getImg(detail.product.images?.[0])" class="prod-detail-img" />
          <div>
            <div class="prod-detail-name">{{ detail.product.title }}</div>
            <div class="prod-detail-desc">{{ detail.product.description }}</div>
          </div>
        </div>
        <div class="note-section">
          <div class="section-label">管理员备注</div>
          <el-input v-model="adminNote" type="textarea" :rows="3" placeholder="添加备注..." maxlength="500" show-word-limit />
          <el-button type="primary" size="small" :loading="noteLoading" @click="saveNote" style="margin-top:8px">保存备注</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 处理投诉弹窗 -->
    <el-dialog v-model="showHandleDialog" :title="handleAction === 'RESOLVED' ? '通过投诉' : '驳回投诉'" width="440px">
      <el-input v-model="resolution" type="textarea" :rows="3" placeholder="填写处理说明（通过时必填）..." />
      <template #footer>
        <el-button @click="showHandleDialog = false">取消</el-button>
        <el-button :type="handleAction === 'RESOLVED' ? 'success' : 'danger'" :loading="handling" @click="confirmHandle">确认</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  getOrders, getOrderDetail, addOrderNote,
  getOrderComplaint, handleOrderComplaint,
  getOrderReview, deleteOrderReview
} from '../../api/admin'
import { getComplaints, handleComplaint } from '../../api/review'
import request from '../../utils/request'

const mainTab = ref('orders')

// 订单
const orders = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const showDetail = ref(false)
const detail = ref(null)
const adminNote = ref('')
const noteLoading = ref(false)
let searchTimer = null

// 投诉
const complaints = ref([])
const loadingComplaints = ref(false)
const complaintStatusFilter = ref('')
const complaintPage = ref(1)
const complaintTotal = ref(0)
const pendingComplaintCount = ref(0)
const showHandleDialog = ref(false)
const currentComplaint = ref(null)
const handleAction = ref('')
const resolution = ref('')
const handling = ref(false)

// 评价
const reviews = ref([])
const loadingReviews = ref(false)
const reviewPage = ref(1)
const reviewTotal = ref(0)

onMounted(() => {
  loadOrders()
  loadComplaints()
  loadReviews()
})

// 订单
async function loadOrders() {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (statusFilter.value) params.status = statusFilter.value
    const res = await getOrders(params)
    orders.value = res.content || []
    total.value = res.totalElements || 0
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

function handleSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { currentPage.value = 1; loadOrders() }, 400)
}

async function openDetail(row) {
  try {
    detail.value = await getOrderDetail(row.id)
    adminNote.value = detail.value.adminNote || ''
    showDetail.value = true
  } catch { ElMessage.error('获取详情失败') }
}

async function saveNote() {
  noteLoading.value = true
  try {
    await addOrderNote(detail.value.id, { note: adminNote.value })
    detail.value.adminNote = adminNote.value
    const idx = orders.value.findIndex(o => o.id === detail.value.id)
    if (idx !== -1) orders.value[idx].adminNote = adminNote.value
    ElMessage.success('备注已保存')
  } catch { ElMessage.error('保存失败') } finally { noteLoading.value = false }
}

// 投诉
async function loadComplaints() {
  loadingComplaints.value = true
  try {
    const params = { page: complaintPage.value, size: 20 }
    if (complaintStatusFilter.value) params.status = complaintStatusFilter.value
    const res = await getComplaints(params)
    complaints.value = res.content || []
    complaintTotal.value = res.totalElements || 0
    pendingComplaintCount.value = complaints.value.filter(c => c.status === 'PENDING').length
  } catch { } finally { loadingComplaints.value = false }
}

function openHandleDialog(row, action) {
  currentComplaint.value = row
  handleAction.value = action
  resolution.value = ''
  showHandleDialog.value = true
}

async function confirmHandle() {
  if (handleAction.value === 'RESOLVED' && !resolution.value.trim()) {
    ElMessage.warning('请填写处理说明'); return
  }
  handling.value = true
  try {
    await handleComplaint(currentComplaint.value.id, { status: handleAction.value, resolution: resolution.value })
    currentComplaint.value.status = handleAction.value
    currentComplaint.value.resolution = resolution.value
    pendingComplaintCount.value = complaints.value.filter(c => c.status === 'PENDING').length
    ElMessage.success('处理成功')
    showHandleDialog.value = false
  } catch { ElMessage.error('处理失败') } finally { handling.value = false }
}

// 评价
async function loadReviews() {
  loadingReviews.value = true
  try {
    const res = await request.get('/reviews', { params: { page: reviewPage.value - 1, size: 20 } })
    reviews.value = res.content || []
    reviewTotal.value = res.totalElements || 0
  } catch { } finally { loadingReviews.value = false }
}

async function doDeleteReview(row) {
  try {
    await ElMessageBox.confirm('确认删除该评价？', '删除恶评', { type: 'warning' })
    await request.delete(`/reviews/${row.id}`)
    row.deleted = true
    ElMessage.success('评价已删除')
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const getImg = (url) => url ? (url.startsWith('http') ? url : `/api${url}`) : ''
const fmt = (d) => d ? new Date(d).toLocaleString('zh-CN') : '—'
const statusType = (s) => ({ CREATED: 'warning', PAID: 'primary', SHIPPED: 'info', COMPLETED: 'success', CANCELLED: 'danger' }[s] || 'info')
const statusText = (s) => ({ CREATED: '待支付', PAID: '待发货', SHIPPED: '待收货', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s)
const cStatusType = (s) => ({ PENDING: 'danger', PROCESSING: 'warning', RESOLVED: 'success', REJECTED: 'info' }[s] || 'info')
const cStatusText = (s) => ({ PENDING: '待处理', PROCESSING: '处理中', RESOLVED: '已解决', REJECTED: '已驳回' }[s] || s)
</script>

<style scoped>
.order-mgmt { padding: 24px; }
.page-header { background: #fff; padding: 16px 24px; margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.page-header h1 { font-size: 20px; font-weight: 600; color: #262626; margin: 0; }
.breadcrumb { margin-top: 4px; color: #8c8c8c; font-size: 13px; }

.main-tabs { background: #fff; border-radius: 8px; padding: 0 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); margin-bottom: 0; }
.main-tabs :deep(.el-tabs__header) { margin-bottom: 16px; }
.main-tabs :deep(.el-tabs__content) { padding-bottom: 20px; }

.content-card { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); overflow: hidden; margin-bottom: 16px; border: 1px solid #f0f0f0; }
.filter-row { padding: 14px 16px; display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.pagination { padding: 14px 16px; display: flex; justify-content: center; }

.section-title { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 10px; padding-left: 2px; }

.prod-cell { display: flex; align-items: center; gap: 10px; }
.prod-thumb { width: 44px; height: 44px; object-fit: cover; border-radius: 6px; border: 1px solid #f0f0f0; flex-shrink: 0; }
.prod-name { font-size: 13px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 130px; }
.prod-price { font-size: 13px; color: #e4393c; font-weight: 600; }

:deep(.el-table .el-table__row) { cursor: pointer; }

.detail-wrap { max-height: 60vh; overflow-y: auto; }
.prod-detail-row { display: flex; gap: 14px; align-items: flex-start; margin: 16px 0; padding: 14px; background: #fafafa; border-radius: 8px; }
.prod-detail-img { width: 80px; height: 60px; object-fit: cover; border-radius: 6px; flex-shrink: 0; }
.prod-detail-name { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 6px; }
.prod-detail-desc { font-size: 12px; color: #999; line-height: 1.5; }
.note-section { margin-top: 16px; }
.section-label { font-size: 13px; font-weight: 600; color: #555; margin-bottom: 8px; }
</style>
