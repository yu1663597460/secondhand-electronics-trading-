<template>
  <div class="product-management">
    <!-- 搜索和筛选栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品标题"
        style="width: 300px"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch" :icon="Search">搜索</el-button>
        </template>
      </el-input>
      
      <el-select v-model="selectedStatus" placeholder="商品状态" style="width: 150px" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="已上架" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
        <el-option label="已下架" value="TAKEN_DOWN" />
        <el-option label="已售出" value="SOLD" />
      </el-select>
      
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="primary" @click="loadPendingCount">刷新</el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ pendingCount }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ total }}</div>
            <div class="stat-label">商品总数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 商品列表 -->
    <el-table :data="products" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="商品图片" width="100">
        <template #default="{ row }">
          <el-image
            v-if="row.imageUrls && row.imageUrls.length > 0"
            :src="row.imageUrls[0]"
            :preview-src-list="row.imageUrls"
            style="width: 60px; height: 60px;"
            fit="cover"
          />
          <div v-else class="no-image">
            <el-icon><Picture /></el-icon>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="商品标题" width="200" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">
          ¥{{ row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="sellerName" label="卖家" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="发布时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button 
            v-if="row.status === 'PENDING'" 
            size="small" 
            type="success"
            @click="approveProduct(row)"
          >
            审核通过
          </el-button>
          <el-button 
            v-if="row.status === 'PENDING'" 
            size="small" 
            type="danger"
            @click="rejectProduct(row)"
          >
            拒绝
          </el-button>
          <el-button 
            v-if="row.status === 'APPROVED'" 
            size="small" 
            type="warning"
            @click="removeProduct(row)"
          >
            下架
          </el-button>
          <el-button size="small" @click="viewProduct(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 商品详情弹窗 -->
    <ProductDetailDialog 
      v-model="showProductDetail" 
      :product="selectedProduct" 
      @refresh="loadProducts"
    />
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { Search, Picture } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getProducts, 
  approveProduct as approveProductApi, 
  rejectProduct as rejectProductApi, 
  removeProduct as removeProductApi, 
  getPendingProductCount 
} from '../api/admin'
import ProductDetailDialog from './ProductDetailDialog.vue'

const loading = ref(false)
const products = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const searchKeyword = ref('')
const selectedStatus = ref('')
const showProductDetail = ref(false)
const selectedProduct = ref(null)
const pendingCount = ref(0)

const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (selectedStatus.value) {
      params.status = selectedStatus.value
    }
    
    const response = await getProducts(params)
    products.value = response.content
    total.value = response.totalElements
  } catch (error) {
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const loadPendingCount = async () => {
  try {
    const count = await getPendingProductCount()
    pendingCount.value = count
  } catch (error) {
    console.error('Failed to load pending count:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadProducts()
}

const resetSearch = () => {
  searchKeyword.value = ''
  selectedStatus.value = ''
  currentPage.value = 1
  loadProducts()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadProducts()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadProducts()
}
const approveProduct = async (product) => {
  try {
    const { value: comment } = await ElMessageBox.prompt('请输入审核意见（可选）', '审核通过', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '审核意见'
    })
    
    await approveProductApi(product.id, {
      comment: comment || '审核通过'
    })
    
    ElMessage.success('商品审核通过')
    loadProducts()
    loadPendingCount()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败')
    }
  }
}

const rejectProduct = async (product) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝商品', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '拒绝原因',
      inputValidator: (value) => {
        if (!value || !value.trim()) {
          return '请输入拒绝原因'
        }
        return true
      }
    })
    
    await rejectProductApi(product.id, {
      reason: reason
    })
    
    ElMessage.success('商品已拒绝')
    loadProducts()
    loadPendingCount()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('拒绝失败')
    }
  }
}

const removeProduct = async (product) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入下架原因', '下架商品', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '下架原因',
      inputValidator: (value) => {
        if (!value || !value.trim()) {
          return '请输入下架原因'
        }
        return true
      }
    })
    
    await removeProductApi(product.id, {
      reason: reason
    })
    
    ElMessage.success('商品已下架')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('下架失败')
    }
  }
}

const viewProduct = (product) => {
  selectedProduct.value = product
  showProductDetail.value = true
}

const getStatusTagType = (status) => {
  const types = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'TAKEN_DOWN': 'info',
    'SOLD': 'success'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待审核',
    'APPROVED': '已上架',
    'REJECTED': '已拒绝',
    'TAKEN_DOWN': '已下架',
    'SOLD': '已售出'
  }
  return texts[status] || '未知'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}

onMounted(() => {
  loadProducts()
  loadPendingCount()
})
</script>

<style scoped>
.product-management {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 20px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #E6A23C;
  margin-bottom: 5px;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.no-image {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  border-radius: 4px;
}
</style>