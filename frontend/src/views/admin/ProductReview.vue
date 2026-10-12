<template>
  <div class="product-review">
    <div class="page-header">
      <div class="header-left">
        <h1>商品审核</h1>
        <el-tag type="warning" size="large" v-if="pendingCount > 0">
          {{ pendingCount }} 件待审核
        </el-tag>
        <el-tag type="success" size="large" v-else>暂无待审核商品</el-tag>
      </div>
      <div class="breadcrumb">管理后台 / 商品审核</div>
    </div>

    <!-- 审核队列 -->
    <div v-loading="loading">
      <el-empty v-if="products.length === 0 && !loading" description="暂无待审核商品，所有商品已处理完毕">
        <el-button type="primary" @click="$router.push('/admin/products')">查看全部商品</el-button>
      </el-empty>

      <div v-else class="review-list">
        <div v-for="product in products" :key="product.id" class="review-card">
          <!-- 左侧图片 -->
          <div class="review-images">
            <el-carousel v-if="product.images && product.images.length > 0" height="220px" :autoplay="false">
              <el-carousel-item v-for="(img, i) in product.images" :key="i">
                <img :src="getImgUrl(img)" class="review-img" />
              </el-carousel-item>
            </el-carousel>
            <div v-else class="no-image">
              <el-icon :size="48" color="#c0c4cc"><Picture /></el-icon>
              <p>暂无图片</p>
            </div>
          </div>

          <!-- 中间信息 -->
          <div class="review-info">
            <div class="review-title">{{ product.title }}</div>

            <el-descriptions :column="2" size="small" border class="review-desc-table">
              <el-descriptions-item label="价格">
                <span class="price-text">¥{{ product.price }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="分类">{{ product.categoryName || '未分类' }}</el-descriptions-item>
              <el-descriptions-item label="卖家">{{ product.sellerName }}</el-descriptions-item>
              <el-descriptions-item label="成色">{{ getConditionText(product.condition) }}</el-descriptions-item>
              <el-descriptions-item label="发布时间" :span="2">{{ formatDate(product.createdAt) }}</el-descriptions-item>
            </el-descriptions>

            <div class="review-description">
              <div class="desc-label">商品描述</div>
              <div class="desc-content">{{ product.description || '暂无描述' }}</div>
            </div>
          </div>

          <!-- 右侧操作 -->
          <div class="review-actions">
            <div class="action-title">审核操作</div>

            <el-input
              v-model="product._comment"
              type="textarea"
              :rows="3"
              placeholder="审核意见（可选）"
              class="action-input"
            />

            <el-button
              type="success"
              size="large"
              :icon="CircleCheck"
              :loading="product._approving"
              @click="approveProduct(product)"
              style="width:100%"
            >
              审核通过
            </el-button>

            <el-input
              v-model="product._rejectReason"
              type="textarea"
              :rows="3"
              placeholder="拒绝原因（必填）"
              class="action-input"
              style="margin-top:12px"
            />

            <el-button
              type="danger"
              size="large"
              :icon="CircleClose"
              :loading="product._rejecting"
              @click="rejectProduct(product)"
              style="width:100%"
            >
              拒绝发布
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        background
        @current-change="loadProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { getProducts, approveProduct as approveApi, rejectProduct as rejectApi, getPendingProductCount } from '../../api/admin'

const loading = ref(false)
const products = ref([])
const total = ref(0)
const pendingCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getProducts({ page: currentPage.value - 1, size: pageSize.value, status: 'PENDING' })
    // 给每条记录加上本地操作字段
    products.value = (res.content || []).map(p => ({
      ...p,
      _comment: '',
      _rejectReason: '',
      _approving: false,
      _rejecting: false
    }))
    total.value = res.totalElements || 0
    pendingCount.value = total.value
  } catch (e) {
    ElMessage.error('加载待审核商品失败')
  } finally {
    loading.value = false
  }
}

const approveProduct = async (product) => {
  product._approving = true
  try {
    await approveApi(product.id, { comment: product._comment || '审核通过' })
    ElMessage.success(`"${product.title}" 审核通过，已上架到商城`)
    loadProducts()
  } catch (e) {
    ElMessage.error('审核失败，请重试')
  } finally {
    product._approving = false
  }
}

const rejectProduct = async (product) => {
  if (!product._rejectReason?.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  product._rejecting = true
  try {
    await rejectApi(product.id, { reason: product._rejectReason })
    ElMessage.success(`"${product.title}" 已拒绝`)
    loadProducts()
  } catch (e) {
    ElMessage.error('操作失败，请重试')
  } finally {
    product._rejecting = false
  }
}

const getConditionText = (c) => ({ NEW: '全新', LIKE_NEW: '99新', EXCELLENT: '95新', GOOD: '9成新', FAIR: '8成新' }[c] || c || '-')
const formatDate = (d) => { try { return new Date(d).toLocaleString('zh-CN') } catch { return '' } }
const getImgUrl = (url) => url ? (url.startsWith('http') ? url : `/api${url}`) : ''

onMounted(() => loadProducts())
</script>

<style scoped>
.product-review {
  padding: 24px;
}

.page-header {
  background: white;
  padding: 16px 24px;
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h1 {
  font-size: 20px;
  font-weight: 500;
  color: #262626;
  margin: 0;
}

.breadcrumb {
  color: #8c8c8c;
  font-size: 14px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-card {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
  display: flex;
  gap: 0;
  overflow: hidden;
  border-left: 4px solid #e6a23c;
}

/* 左侧图片 */
.review-images {
  width: 280px;
  flex-shrink: 0;
  background: #f5f7fa;
}

.review-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}

.no-image p { margin: 8px 0 0; font-size: 13px; }

/* 中间信息 */
.review-info {
  flex: 1;
  padding: 20px 24px;
  min-width: 0;
}

.review-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  line-height: 1.4;
}

.review-desc-table {
  margin-bottom: 16px;
}

.price-text {
  font-size: 16px;
  font-weight: 700;
  color: #f56c6c;
}

.desc-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.desc-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.7;
  max-height: 100px;
  overflow-y: auto;
  background: #f8f9fa;
  padding: 10px 12px;
  border-radius: 6px;
}

/* 右侧操作 */
.review-actions {
  width: 220px;
  flex-shrink: 0;
  padding: 20px 16px;
  background: #fafafa;
  border-left: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.action-input {
  width: 100%;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}
</style>
