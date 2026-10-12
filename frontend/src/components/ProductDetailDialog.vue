<template>
  <el-dialog
    v-model="visible"
    title="商品详情"
    width="900px"
    @close="handleClose"
  >
    <div v-if="product" class="product-detail">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="product-images">
            <el-carousel v-if="productImages.length > 0" height="240px" :autoplay="false">
              <el-carousel-item v-for="(image, index) in productImages" :key="index">
                <div class="img-wrap">
                  <img :src="image" :alt="`商品图片${index + 1}`" class="product-image" />
                  <el-button
                    class="del-img-btn"
                    type="danger"
                    size="small"
                    circle
                    :icon="Close"
                    @click="removeImage(index)"
                  />
                </div>
              </el-carousel-item>
            </el-carousel>
            <div v-else class="no-image">
              <el-icon size="60"><Picture /></el-icon>
              <p>暂无图片</p>
            </div>
          </div>

          <!-- 上传新图片 -->
          <el-upload
            class="img-uploader"
            action="/api/upload/image"
            :headers="uploadHeaders"
            :show-file-list="false"
            accept="image/*"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
          >
            <el-button type="primary" size="small" :icon="Upload" :loading="uploading">
              上传图片
            </el-button>
            <span class="upload-tip">支持 jpg/png，最大 2MB</span>
          </el-upload>
        </el-col>
        
        <el-col :span="12">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="商品ID">{{ product.id }}</el-descriptions-item>
            <el-descriptions-item label="商品标题">{{ product.title }}</el-descriptions-item>
            <el-descriptions-item label="价格">¥{{ product.price }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ product.categoryName }}</el-descriptions-item>
            <el-descriptions-item label="卖家">{{ product.sellerName }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(product.status)">
                {{ getStatusText(product.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ formatDate(product.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDate(product.updatedAt) }}</el-descriptions-item>
            <el-descriptions-item v-if="product.reviewedBy" label="审核人">{{ product.reviewedBy }}</el-descriptions-item>
            <el-descriptions-item v-if="product.reviewedAt" label="审核时间">{{ formatDate(product.reviewedAt) }}</el-descriptions-item>
            <el-descriptions-item v-if="product.reviewComment" label="审核意见">{{ product.reviewComment }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
      
      <div class="product-description">
        <h3>商品描述</h3>
        <p>{{ product.description }}</p>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <template v-if="product && product.status === 'PENDING'">
        <el-button type="danger" @click="handleReject">拒绝</el-button>
        <el-button type="success" @click="handleApprove">审核通过</el-button>
      </template>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { Picture, Upload, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveProduct, rejectProduct } from '../api/admin'
import request from '../utils/request'

const props = defineProps({
  modelValue: Boolean,
  product: Object
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = ref(false)
const uploading = ref(false)

// 本地维护图片列表，支持增删
const productImages = ref([])

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(() => props.product, (val) => {
  productImages.value = val?.images ? [...val.images] : []
}, { immediate: true })

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt2M) { ElMessage.error('图片大小不能超过 2MB'); return false }
  uploading.value = true
  return true
}

const handleUploadSuccess = async (response) => {
  uploading.value = false
  // el-upload 的 on-success 拿到的是原始响应，不经过 axios 拦截器
  // 后端结构: { success, data: { url } }
  const url = response?.data?.url
  if (!url) { ElMessage.error('上传失败，未获取到图片地址'); return }
  productImages.value.push(url)
  await saveImages()
}

const handleUploadError = () => {
  uploading.value = false
  ElMessage.error('图片上传失败')
}

const removeImage = async (index) => {
  productImages.value.splice(index, 1)
  await saveImages()
}

const saveImages = async () => {
  if (!props.product?.id) return
  try {
    await request.patch(`/admin/products/${props.product.id}/images`, {
      images: productImages.value
    })
    ElMessage.success('图片已保存')
    emit('refresh')
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handleApprove = async () => {
  try {
    const { value: comment } = await ElMessageBox.prompt('请输入审核意见（可选）', '审核通过', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '审核意见'
    })
    await approveProduct(props.product.id, { comment: comment || '审核通过' })
    ElMessage.success('商品审核通过')
    emit('refresh')
    handleClose()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('审核失败')
  }
}

const handleReject = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝商品', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '拒绝原因',
      inputValidator: (v) => (!v || !v.trim()) ? '请输入拒绝原因' : true
    })
    await rejectProduct(props.product.id, { reason })
    ElMessage.success('商品已拒绝')
    emit('refresh')
    handleClose()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
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
</script>

<style scoped>
.product-detail {
  padding: 20px 0;
}

.product-images {
  height: 240px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
}

.img-wrap {
  position: relative;
  width: 100%;
  height: 100%;
}

.del-img-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 10;
}

.img-uploader {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  background-color: #f5f7fa;
}

.product-description {
  margin-top: 30px;
}

.product-description h3 {
  margin-bottom: 15px;
  color: #303133;
}

.product-description p {
  line-height: 1.6;
  color: #606266;
}
</style>