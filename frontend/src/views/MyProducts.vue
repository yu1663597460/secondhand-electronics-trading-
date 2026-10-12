<template>
  <div class="my-products">
    <!-- 顶部横幅 -->
    <div class="shop-banner">
      <div class="banner-content">
        <div class="banner-left">
          <el-avatar :size="64" class="shop-avatar">
            <el-icon :size="32"><Shop /></el-icon>
          </el-avatar>
          <div class="shop-info">
            <h2>我的店铺</h2>
            <p>管理您发布的所有商品</p>
          </div>
        </div>
        <el-button type="primary" size="large" :icon="Plus" @click="$router.push('/publish')">
          发布新商品
        </el-button>
      </div>
      <!-- 统计数字 -->
      <div class="banner-stats">
        <div class="bstat" @click="filterStatus = ''; applyFilter()">
          <span class="bstat-num">{{ stats.total }}</span>
          <span class="bstat-label">全部</span>
        </div>
        <div class="bstat-divider" />
        <div class="bstat approved" @click="filterStatus = 'APPROVED'; applyFilter()">
          <span class="bstat-num">{{ stats.approved }}</span>
          <span class="bstat-label">在售中</span>
        </div>
        <div class="bstat-divider" />
        <div class="bstat pending" @click="filterStatus = 'PENDING'; applyFilter()">
          <span class="bstat-num">{{ stats.pending }}</span>
          <span class="bstat-label">待审核</span>
        </div>
        <div class="bstat-divider" />
        <div class="bstat sold" @click="filterStatus = 'SOLD'; applyFilter()">
          <span class="bstat-num">{{ stats.sold }}</span>
          <span class="bstat-label">已售出</span>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品标题..."
        :prefix-icon="Search"
        clearable
        @clear="loadProducts"
        @keyup.enter="handleSearch"
        style="width: 260px"
      />
      <el-select v-model="filterStatus" placeholder="全部状态" clearable @change="applyFilter" style="width: 140px">
        <el-option label="待审核" value="PENDING" />
        <el-option label="在售中" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
        <el-option label="已售出" value="SOLD" />
      </el-select>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <!-- 商品卡片网格 -->
    <div v-loading="loading" class="products-grid-wrap">
      <el-empty v-if="products.length === 0 && !loading" description="暂无商品，快去发布吧">
        <el-button type="primary" @click="$router.push('/publish')">发布第一个商品</el-button>
      </el-empty>

      <div v-else class="products-grid">
        <div v-for="row in products" :key="row.id" class="product-card">
          <!-- 状态色条 -->
          <div class="status-stripe" :class="row.status?.toLowerCase()" />

          <!-- 商品图片 -->
          <div class="card-image" @click="viewProduct(row.id)">
            <img :src="getProductImage(row)" :alt="row.title" />
            <div class="card-status-badge">
              <el-tag :type="getStatusType(row.status)" size="small" effect="dark">
                {{ getStatusText(row.status) }}
              </el-tag>
            </div>
          </div>

          <!-- 商品信息 -->
          <div class="card-body">
            <h3 class="card-title" @click="viewProduct(row.id)">{{ row.title }}</h3>
            <div class="card-tags">
              <el-tag size="small" type="info" v-if="row.category?.name">{{ row.category.name }}</el-tag>
              <el-tag size="small" v-if="row.condition">{{ getConditionText(row.condition) }}</el-tag>
            </div>
            <div class="card-price">¥{{ formatPrice(row.price) }}</div>
            <div class="card-date">{{ formatDate(row.createdAt) }} 发布</div>
          </div>

          <!-- 操作区 -->
          <div class="card-actions">
            <el-button size="small" :icon="View" @click="viewProduct(row.id)">查看</el-button>
            <el-button size="small" :icon="Edit" @click="openEditDialog(row)" :disabled="row.status === 'SOLD'">编辑</el-button>
            <el-dropdown @command="handleAction" trigger="click">
              <el-button size="small" :icon="More" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{ action: 'sold', product: row }" :disabled="row.status !== 'APPROVED'">
                    <el-icon><Check /></el-icon> 标记已售
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'resubmit', product: row }" :disabled="row.status !== 'REJECTED'">
                    <el-icon><Refresh /></el-icon> 重新提交
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'delete', product: row }" divided>
                    <el-icon><Delete /></el-icon> 删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑商品" width="700px" :before-close="handleEditClose">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="editForm.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="editForm.price" :min="0.01" :precision="2" style="width: 200px" />
        </el-form-item>
        <el-form-item label="商品成色" prop="condition">
          <el-select v-model="editForm.condition" style="width: 200px">
            <el-option label="全新" value="NEW" />
            <el-option label="99新" value="LIKE_NEW" />
            <el-option label="95新" value="EXCELLENT" />
            <el-option label="9成新" value="GOOD" />
            <el-option label="8成新" value="FAIR" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="4" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="商品图片">
          <!-- 已有图片 -->
          <div class="edit-images">
            <div
              v-for="(img, idx) in editImages"
              :key="idx"
              class="edit-img-item"
            >
              <img :src="img" :alt="`图片${idx+1}`" />
              <el-button
                class="edit-img-del"
                type="danger"
                size="small"
                circle
                :icon="Close"
                @click="removeEditImage(idx)"
              />
            </div>
            <!-- 上传按钮 -->
            <el-upload
              action="/api/upload/image"
              :headers="uploadHeaders"
              :show-file-list="false"
              accept="image/*"
              :on-success="handleImageUploadSuccess"
              :on-error="handleImageUploadError"
              :before-upload="beforeImageUpload"
              class="edit-img-upload"
            >
              <div class="upload-placeholder">
                <el-icon :size="24"><Upload /></el-icon>
                <span>上传图片</span>
              </div>
            </el-upload>
          </div>
          <div class="img-tip">最多5张，每张不超过2MB</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="updating" @click="handleUpdate">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyProducts, updateProduct, updateProductStatus, deleteProduct as deleteProductApi } from '../api/product'
import request from '../utils/request'
import {
  Shop, Plus, Search, Refresh, View, Edit, More, Check, Delete, ArrowDown, Upload, Close
} from '@element-plus/icons-vue'

const router = useRouter()
const products = ref([])
const loading = ref(false)
const updating = ref(false)
const searchKeyword = ref('')
const filterStatus = ref('')
const showEditDialog = ref(false)
const editFormRef = ref()
const currentEditProduct = ref(null)

const editForm = reactive({ title: '', description: '', price: null, condition: '' })
const editImages = ref([])  // 当前编辑商品的图片列表
const imageUploading = ref(false)

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const editRules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }, { min: 5, max: 100, message: '5-100个字符', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }, { min: 20, max: 1000, message: '20-1000个字符', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }, { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' }],
  condition: [{ required: true, message: '请选择成色', trigger: 'change' }]
}

const allProducts = ref([])  // 保存全量数据用于本地筛选

const stats = computed(() => ({
  total: allProducts.value.length,
  approved: allProducts.value.filter(p => p.status === 'APPROVED').length,
  pending: allProducts.value.filter(p => p.status === 'PENDING').length,
  sold: allProducts.value.filter(p => p.status === 'SOLD').length
}))

const loadProducts = async () => {
  try {
    loading.value = true
    const data = await getMyProducts()
    allProducts.value = data || []
    applyFilter()
  } catch (error) {
    ElMessage.error('加载商品失败')
  } finally {
    loading.value = false
  }
}

const applyFilter = () => {
  let list = [...allProducts.value]
  if (filterStatus.value) list = list.filter(p => p.status === filterStatus.value)
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(p => p.title?.toLowerCase().includes(kw))
  }
  products.value = list
}

const handleSearch = () => applyFilter()
const handleReset = () => { searchKeyword.value = ''; filterStatus.value = ''; applyFilter() }
const viewProduct = (id) => router.push(`/product/${id}`)

const handleAction = async ({ action, product }) => {
  if (action === 'edit') openEditDialog(product)
  else if (action === 'sold') await markAsSold(product)
  else if (action === 'resubmit') await resubmitProduct(product)
  else if (action === 'delete') await deleteProduct(product)
}

const openEditDialog = (product) => {
  currentEditProduct.value = product
  editForm.title = product.title
  editForm.description = product.description
  editForm.price = Number(product.price)
  editForm.condition = product.condition
  editImages.value = product.images ? [...product.images] : []
  showEditDialog.value = true
}

const handleUpdate = async () => {
  if (!editFormRef.value) return
  try {
    await editFormRef.value.validate()
    updating.value = true
    await updateProduct(currentEditProduct.value.id, {
      ...editForm,
      images: editImages.value,
      categoryId: currentEditProduct.value.category?.id
    })
    ElMessage.success('商品更新成功')
    showEditDialog.value = false
    loadProducts()
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    updating.value = false
  }
}

const handleImageUploadSuccess = (response) => {
  imageUploading.value = false
  const url = response?.data?.url
  if (!url) { ElMessage.error('上传失败'); return }
  editImages.value.push(url)
}

const handleImageUploadError = () => {
  imageUploading.value = false
  ElMessage.error('图片上传失败')
}

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt2M) { ElMessage.error('图片大小不能超过 2MB'); return false }
  imageUploading.value = true
  return true
}

const removeEditImage = (index) => {
  editImages.value.splice(index, 1)
}

const handleEditClose = (done) => { editFormRef.value?.resetFields(); currentEditProduct.value = null; done() }

const markAsSold = async (product) => {
  try {
    await ElMessageBox.confirm(`确认将"${product.title}"标记为已售出？`, '确认操作', { type: 'warning' })
    await updateProductStatus(product.id, 'SOLD')
    ElMessage.success('已标记为已售出')
    loadProducts()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const resubmitProduct = async (product) => {
  try {
    await ElMessageBox.confirm(`确认重新提交"${product.title}"审核？`, '确认操作', { type: 'info' })
    await updateProductStatus(product.id, 'PENDING')
    ElMessage.success('已重新提交审核')
    loadProducts()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const deleteProduct = async (product) => {
  try {
    await ElMessageBox.confirm(`确认删除"${product.title}"？此操作不可恢复！`, '确认删除', { type: 'danger' })
    await deleteProductApi(product.id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const getProductImage = (product) => {
  if (product.images && product.images.length > 0) return product.images[0]
  return 'https://picsum.photos/seed/electronics/400/300'
}

const getStatusType = (status) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', SOLD: 'info' }[status] || 'info')
const getStatusText = (status) => ({ PENDING: '待审核', APPROVED: '在售中', REJECTED: '已拒绝', SOLD: '已售出' }[status] || status)
const getConditionText = (c) => ({ NEW: '全新', LIKE_NEW: '99新', EXCELLENT: '95新', GOOD: '9成新', FAIR: '8成新' }[c] || c)
const formatPrice = (price) => Number(price).toFixed(2)
const formatDate = (d) => new Date(d).toLocaleDateString('zh-CN')

onMounted(() => loadProducts())
</script>

<style scoped>
.my-products {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 顶部横幅 */
.shop-banner {
  background: url('/images/banners/背景.webp') center/cover no-repeat;
  border-radius: 16px;
  padding: 28px 32px 20px;
  margin-bottom: 20px;
  color: #fff;
  position: relative;
}
.shop-banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  border-radius: 16px;
}
.shop-banner > * { position: relative; z-index: 1; }

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.shop-avatar {
  background: rgba(255,255,255,0.2);
  color: #fff;
}

.shop-info h2 {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
}

.shop-info p {
  margin: 0;
  opacity: 0.8;
  font-size: 14px;
}

.banner-stats {
  display: flex;
  align-items: center;
  gap: 0;
  background: rgba(255,255,255,0.15);
  border-radius: 12px;
  padding: 12px 24px;
}

.bstat {
  text-align: center;
  padding: 0 24px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.bstat:hover { opacity: 0.75; }

.bstat-num {
  display: block;
  font-size: 26px;
  font-weight: 700;
}

.bstat-label {
  font-size: 12px;
  opacity: 0.85;
}

.bstat-divider {
  width: 1px;
  height: 36px;
  background: rgba(255,255,255,0.3);
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

/* 商品网格 */
.products-grid-wrap {
  min-height: 300px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

/* 状态色条 */
.status-stripe {
  height: 4px;
}
.status-stripe.approved { background: #67c23a; }
.status-stripe.pending  { background: #e6a23c; }
.status-stripe.rejected { background: #f56c6c; }
.status-stripe.sold     { background: #909399; }

.card-image {
  position: relative;
  height: 160px;
  overflow: hidden;
  cursor: pointer;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .card-image img {
  transform: scale(1.05);
}

.card-status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
}

.card-body {
  padding: 14px 16px 10px;
  flex: 1;
}

.card-title {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.card-title:hover { color: #409eff; }

.card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.card-price {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
  margin-bottom: 4px;
}

.card-date {
  font-size: 12px;
  color: #c0c4cc;
}

.card-actions {
  display: flex;
  gap: 6px;
  padding: 10px 16px 14px;
  border-top: 1px solid #f5f5f5;
}

@media (max-width: 768px) {
  .my-products { padding: 16px; }
  .banner-content { flex-direction: column; gap: 16px; }
  .banner-stats { flex-wrap: wrap; justify-content: center; }
  .products-grid { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); }
}

/* 编辑弹窗图片区域 */
.edit-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.edit-img-item {
  position: relative;
  width: 90px;
  height: 90px;
  border-radius: 8px;
  overflow: visible;
  border: 1px solid #e4e7ed;
}

.edit-img-item img {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: 8px;
  display: block;
}

.edit-img-del {
  position: absolute;
  top: -8px;
  right: -8px;
  z-index: 10;
  width: 22px !important;
  height: 22px !important;
  min-height: unset !important;
}

.edit-img-upload {
  width: 90px;
  height: 90px;
}

.upload-placeholder {
  width: 90px;
  height: 90px;
  border: 1px dashed #c0c4cc;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  color: #909399;
  font-size: 12px;
  transition: border-color 0.2s;
}

.upload-placeholder:hover {
  border-color: #00B578;
  color: #00B578;
}

.img-tip {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}
</style>
