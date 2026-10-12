<template>
  <div class="complaint-mgmt">
    <div class="page-header">
      <h1>投诉管理</h1>
      <div class="breadcrumb">管理后台 / 投诉管理</div>
    </div>

    <!-- 筛选 -->
    <div class="content-card filter-bar">
      <el-select v-model="filterStatus" placeholder="全部状态" clearable @change="loadComplaints" style="width:140px">
        <el-option label="待处理" value="PENDING" />
        <el-option label="处理中" value="PROCESSING" />
        <el-option label="已解决" value="RESOLVED" />
        <el-option label="已驳回" value="REJECTED" />
      </el-select>
      <el-button @click="filterStatus = ''; loadComplaints()">重置</el-button>
    </div>

    <!-- 列表 -->
    <div class="content-card">
      <el-table :data="complaints" v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="投诉人" width="110">
          <template #default="{ row }">{{ row.complainant?.username }}</template>
        </el-table-column>
        <el-table-column label="被投诉商家" width="120">
          <template #default="{ row }">{{ row.respondent?.username }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="投诉原因" width="160" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="openHandle(row, 'RESOLVED')" :disabled="row.status !== 'PENDING' && row.status !== 'PROCESSING'">通过</el-button>
            <el-button size="small" type="danger" @click="openHandle(row, 'REJECTED')" :disabled="row.status !== 'PENDING' && row.status !== 'PROCESSING'">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total"
          layout="total, prev, pager, next" background @current-change="p => { currentPage = p; loadComplaints() }" />
      </div>
    </div>

    <!-- 处理弹窗 -->
    <el-dialog v-model="showHandleDialog" :title="handleAction === 'RESOLVED' ? '通过投诉' : '驳回投诉'" width="440px">
      <el-input v-model="resolution" type="textarea" :rows="3" placeholder="请填写处理说明（可选）" />
      <template #footer>
        <el-button @click="showHandleDialog = false">取消</el-button>
        <el-button :type="handleAction === 'RESOLVED' ? 'success' : 'danger'" :loading="handling" @click="confirmHandle">确认</el-button>
      </template>
    </el-dialog>

    <!-- 评价管理 -->
    <div class="page-header" style="margin-top:32px">
      <h1>评价管理</h1>
      <div class="breadcrumb">管理后台 / 评价管理（可删除恶评）</div>
    </div>
    <div class="content-card">
      <el-table :data="reviews" v-loading="loadingReviews" style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="买家" width="110">
          <template #default="{ row }">{{ row.buyer?.username }}</template>
        </el-table-column>
        <el-table-column label="商品" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.product?.title }}</template>
        </el-table-column>
        <el-table-column label="评分" width="80">
          <template #default="{ row }">
            <span style="color:#f5a623">{{ '★'.repeat(row.rating) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.deleted ? 'danger' : 'success'" size="small">{{ row.deleted ? '已删除' : '正常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" :disabled="row.deleted" @click="handleDeleteReview(row)">删除恶评</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="reviewPage" :page-size="reviewPageSize" :total="reviewTotal"
          layout="total, prev, pager, next" background @current-change="p => { reviewPage = p; loadReviews() }" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getComplaints, handleComplaint, deleteReview } from '../../api/review'
import request from '../../utils/request'

// 投诉
const complaints = ref([])
const loading = ref(false)
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const showHandleDialog = ref(false)
const currentComplaint = ref(null)
const handleAction = ref('')
const resolution = ref('')
const handling = ref(false)

// 评价
const reviews = ref([])
const loadingReviews = ref(false)
const reviewPage = ref(1)
const reviewPageSize = ref(20)
const reviewTotal = ref(0)

const loadComplaints = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (filterStatus.value) params.status = filterStatus.value
    const res = await getComplaints(params)
    complaints.value = res.content || []
    total.value = res.totalElements || 0
  } catch { ElMessage.error('加载投诉失败') } finally { loading.value = false }
}

const loadReviews = async () => {
  loadingReviews.value = true
  try {
    const res = await request.get('/reviews', { params: { page: reviewPage.value - 1, size: reviewPageSize.value } })
    reviews.value = res.content || []
    reviewTotal.value = res.totalElements || 0
  } catch { /* ignore */ } finally { loadingReviews.value = false }
}

const openHandle = (row, action) => {
  currentComplaint.value = row
  handleAction.value = action
  resolution.value = ''
  showHandleDialog.value = true
}

const confirmHandle = async () => {
  handling.value = true
  try {
    await handleComplaint(currentComplaint.value.id, { status: handleAction.value, resolution: resolution.value })
    ElMessage.success('处理成功')
    showHandleDialog.value = false
    loadComplaints()
  } catch { ElMessage.error('处理失败') } finally { handling.value = false }
}

const handleDeleteReview = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该评价？', '删除恶评', { type: 'warning' })
    await deleteReview(row.id)
    ElMessage.success('评价已删除')
    loadReviews()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const statusType = (s) => ({ PENDING: 'warning', PROCESSING: 'primary', RESOLVED: 'success', REJECTED: 'info' }[s] || 'info')
const statusText = (s) => ({ PENDING: '待处理', PROCESSING: '处理中', RESOLVED: '已解决', REJECTED: '已驳回' }[s] || s)
const fmt = (d) => d ? new Date(d).toLocaleString('zh-CN') : ''

onMounted(() => { loadComplaints(); loadReviews() })
</script>

<style scoped>
.complaint-mgmt { padding: 24px; }
.page-header { background: #fff; padding: 16px 24px; margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.page-header h1 { font-size: 20px; font-weight: 600; color: #262626; margin: 0; }
.breadcrumb { margin-top: 4px; color: #8c8c8c; font-size: 13px; }
.content-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); overflow: hidden; margin-bottom: 20px; }
.filter-bar { padding: 16px 20px; display: flex; gap: 10px; }
.pagination { padding: 16px 20px; display: flex; justify-content: center; }
</style>
