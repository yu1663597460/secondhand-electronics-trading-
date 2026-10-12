<template>
  <div class="prod-mgmt">
    <div class="page-header">
      <h1>商品与分类管理</h1>
      <div class="breadcrumb">管理后台 / 商品管理</div>
    </div>

    <el-tabs v-model="activeTab" class="mgmt-tabs">

      <!-- Tab1: 商品列表 -->
      <el-tab-pane label="商品列表" name="products">
        <!-- 搜索筛选 -->
        <div class="content-card filter-row">
          <el-input v-model="searchKeyword" placeholder="搜索商品标题" style="width:260px" clearable @keyup.enter="handleSearch">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="selectedStatus" placeholder="全部状态" style="width:130px" clearable @change="handleSearch">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已上架" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已下架" value="TAKEN_DOWN" />
            <el-option label="已售出" value="SOLD" />
          </el-select>
          <el-button type="primary" @click="loadProducts"><el-icon><Refresh /></el-icon> 刷新</el-button>
          <div class="pending-badge" v-if="pendingCount > 0">
            <el-badge :value="pendingCount" type="danger">待审核</el-badge>
          </div>
        </div>

        <!-- 商品列表 -->
        <div class="content-card">
          <el-table :data="products" v-loading="loading" style="width:100%">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column label="商品" width="240">
              <template #default="{ row }">
                <div class="prod-cell">
                  <img :src="getImg(row.images?.[0])" class="prod-thumb" />
                  <div>
                    <div class="prod-name">{{ row.title }}</div>
                    <div class="prod-price">¥{{ row.price }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="分类" width="100">
              <template #default="{ row }">{{ row.category?.name || '—' }}</template>
            </el-table-column>
            <el-table-column label="卖家" width="110">
              <template #default="{ row }">{{ row.seller?.username }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="发布时间" width="160">
              <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="approveProduct(row)">通过</el-button>
                <el-button v-if="row.status === 'PENDING'" size="small" type="danger" @click="rejectProduct(row)">拒绝</el-button>
                <el-button v-if="row.status === 'APPROVED'" size="small" type="warning" @click="removeProduct(row)">下架</el-button>
                <el-button size="small" @click="viewProduct(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total"
              layout="total, prev, pager, next" background @current-change="p => { currentPage = p; loadProducts() }" />
          </div>
        </div>
      </el-tab-pane>

      <!-- Tab2: 分类管理 -->
      <el-tab-pane label="分类管理" name="categories">
        <div class="content-card">
          <div class="cat-toolbar">
            <el-button type="primary" @click="openCatDialog(null)"><el-icon><Plus /></el-icon> 添加分类</el-button>
            <el-button @click="loadCategories"><el-icon><Refresh /></el-icon> 刷新</el-button>
          </div>
          <el-table :data="categories" v-loading="loadingCats" style="width:100%">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="name" label="分类名称" width="140" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column label="商品数" width="100">
              <template #default="{ row }">
                <el-tag :type="row.productCount > 0 ? 'success' : 'info'" size="small">{{ row.productCount || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="160">
              <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openCatDialog(row)">编辑</el-button>
                <el-button size="small" type="danger" :disabled="row.productCount > 0" @click="deleteCat(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

    </el-tabs>

    <!-- 分类弹窗 -->
    <el-dialog v-model="showCatDialog" :title="editingCat ? '编辑分类' : '添加分类'" width="480px">
      <el-form ref="catFormRef" :model="catForm" :rules="catRules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="catForm.name" placeholder="如：手机" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="catForm.description" type="textarea" :rows="3" placeholder="分类描述（可选）" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCatDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingCat" @click="saveCat">{{ editingCat ? '更新' : '添加' }}</el-button>
      </template>
    </el-dialog>

    <!-- 商品详情弹窗 -->
    <el-dialog v-model="showProdDetail" title="商品详情" width="680px">
      <div v-if="selectedProd" class="prod-detail">
        <div class="prod-imgs">
          <img v-for="(img, i) in selectedProd.images" :key="i" :src="getImg(img)" class="prod-detail-img" />
        </div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="商品ID">{{ selectedProd.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(selectedProd.status)" size="small">{{ statusText(selectedProd.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ selectedProd.title }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ selectedProd.description }}</el-descriptions-item>
          <el-descriptions-item label="价格">¥{{ selectedProd.price }}</el-descriptions-item>
          <el-descriptions-item label="成色">{{ selectedProd.condition }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ selectedProd.category?.name }}</el-descriptions-item>
          <el-descriptions-item label="卖家">{{ selectedProd.seller?.username }}</el-descriptions-item>
          <el-descriptions-item label="发布时间" :span="2">{{ fmt(selectedProd.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="审核备注" :span="2" v-if="selectedProd.reviewNote">{{ selectedProd.reviewNote }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getProducts, approveProduct as approveApi, rejectProduct as rejectApi,
  removeProduct as removeApi, getPendingProductCount,
  getCategories, createCategory, updateCategory, deleteCategory
} from '../../api/admin'

const activeTab = ref('products')

// 商品
const products = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const selectedStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const pendingCount = ref(0)
const showProdDetail = ref(false)
const selectedProd = ref(null)

// 分类
const categories = ref([])
const loadingCats = ref(false)
const showCatDialog = ref(false)
const editingCat = ref(null)
const savingCat = ref(false)
const catForm = ref({ name: '', description: '' })
const catFormRef = ref(null)
const catRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }, { min: 2, max: 50, message: '2-50字符', trigger: 'blur' }]
}

onMounted(() => { loadProducts(); loadPendingCount(); loadCategories() })

async function loadProducts() {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (selectedStatus.value) params.status = selectedStatus.value
    const res = await getProducts(params)
    products.value = res.content || []
    total.value = res.totalElements || 0
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

async function loadPendingCount() {
  try { pendingCount.value = await getPendingProductCount() } catch { }
}

function handleSearch() {
  currentPage.value = 1
  loadProducts()
}

async function approveProduct(row) {
  try {
    const { value } = await ElMessageBox.prompt('审核意见（可选）', '审核通过', { inputPlaceholder: '审核意见' })
    await approveApi(row.id, { comment: value || '审核通过' })
    ElMessage.success('已通过')
    loadProducts(); loadPendingCount()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

async function rejectProduct(row) {
  try {
    const { value } = await ElMessageBox.prompt('拒绝原因', '拒绝商品', {
      inputPlaceholder: '拒绝原因',
      inputValidator: v => v?.trim() ? true : '请输入拒绝原因'
    })
    await rejectApi(row.id, { reason: value })
    ElMessage.success('已拒绝')
    loadProducts(); loadPendingCount()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

async function removeProduct(row) {
  try {
    const { value } = await ElMessageBox.prompt('下架原因', '下架商品', {
      inputPlaceholder: '下架原因',
      inputValidator: v => v?.trim() ? true : '请输入下架原因'
    })
    await removeApi(row.id, { reason: value })
    ElMessage.success('已下架')
    loadProducts()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

function viewProduct(row) {
  selectedProd.value = row
  showProdDetail.value = true
}

// 分类
async function loadCategories() {
  loadingCats.value = true
  try {
    categories.value = (await getCategories()).map(c => ({ ...c, productCount: c.productCount || 0 }))
  } catch { ElMessage.error('加载分类失败') } finally { loadingCats.value = false }
}

function openCatDialog(cat) {
  editingCat.value = cat
  catForm.value = cat ? { name: cat.name, description: cat.description || '' } : { name: '', description: '' }
  showCatDialog.value = true
}

async function saveCat() {
  try {
    await catFormRef.value.validate()
    savingCat.value = true
    if (editingCat.value) {
      const res = await updateCategory(editingCat.value.id, catForm.value)
      const idx = categories.value.findIndex(c => c.id === editingCat.value.id)
      if (idx !== -1) categories.value[idx] = { ...res, productCount: categories.value[idx].productCount }
      ElMessage.success('分类已更新')
    } else {
      const res = await createCategory(catForm.value)
      categories.value.unshift({ ...res, productCount: 0 })
      ElMessage.success('分类已添加')
    }
    showCatDialog.value = false
  } catch (e) { if (e?.message) ElMessage.error(e.message) } finally { savingCat.value = false }
}

async function deleteCat(row) {
  if (row.productCount > 0) { ElMessage.warning('该分类下还有商品，无法删除'); return }
  try {
    await ElMessageBox.confirm(`确认删除分类"${row.name}"？`, '删除分类', { type: 'warning' })
    await deleteCategory(row.id)
    categories.value = categories.value.filter(c => c.id !== row.id)
    ElMessage.success('分类已删除')
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const getImg = (url) => url ? (url.startsWith('http') ? url : `/api${url}`) : ''
const fmt = (d) => d ? new Date(d).toLocaleString('zh-CN') : '—'
const statusType = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', TAKEN_DOWN: 'info', SOLD: 'success' }[s] || 'info')
const statusText = (s) => ({ PENDING: '待审核', APPROVED: '已上架', REJECTED: '已拒绝', TAKEN_DOWN: '已下架', SOLD: '已售出' }[s] || s)
</script>

<style scoped>
.prod-mgmt { padding: 24px; }
.page-header { background: #fff; padding: 16px 24px; margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.page-header h1 { font-size: 20px; font-weight: 600; color: #262626; margin: 0; }
.breadcrumb { margin-top: 4px; color: #8c8c8c; font-size: 13px; }

.mgmt-tabs { background: #fff; border-radius: 8px; padding: 16px 20px 0; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.mgmt-tabs :deep(.el-tabs__header) { margin-bottom: 16px; }

.content-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); overflow: hidden; margin-bottom: 16px; }
.filter-row { padding: 14px 18px; display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.pending-badge { margin-left: auto; }
.pagination { padding: 16px 20px; display: flex; justify-content: center; }

.prod-cell { display: flex; align-items: center; gap: 10px; }
.prod-thumb { width: 50px; height: 50px; object-fit: cover; border-radius: 6px; border: 1px solid #f0f0f0; flex-shrink: 0; }
.prod-name { font-size: 13px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 150px; }
.prod-price { font-size: 13px; color: #e4393c; font-weight: 600; margin-top: 2px; }

.cat-toolbar { padding: 14px 18px; border-bottom: 1px solid #f0f0f0; display: flex; gap: 10px; }

.prod-detail { padding: 4px 0; }
.prod-imgs { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.prod-detail-img { width: 100px; height: 75px; object-fit: cover; border-radius: 6px; border: 1px solid #f0f0f0; }
</style>
