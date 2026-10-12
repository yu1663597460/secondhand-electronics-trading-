<template>
  <div class="publish-product">
    <el-card class="publish-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h2><el-icon><Plus /></el-icon> 发布商品</h2>
          <p>填写商品信息，让更多人看到你的好物</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
        @submit.prevent="handleSubmit"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="商品标题" prop="title">
              <el-input
                v-model="form.title"
                placeholder="请输入商品标题，简洁明了"
                maxlength="100"
                show-word-limit
                clearable
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-select
                v-model="form.categoryId"
                placeholder="请选择商品分类"
                style="width: 100%"
                clearable
              >
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                >
                  <span>{{ category.name }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">
                    {{ category.description }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="商品价格" prop="price">
              <el-input-number
                v-model="form.price"
                :min="0.01"
                :max="999999"
                :precision="2"
                :step="1"
                style="width: 100%"
                placeholder="请输入商品价格"
              >
                <template #prefix>¥</template>
              </el-input-number>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="商品成色" prop="condition">
              <el-select
                v-model="form.condition"
                placeholder="请选择商品成色"
                style="width: 100%"
              >
                <el-option label="全新" value="NEW">
                  <el-icon><Star /></el-icon> 全新 - 未拆封或使用极少
                </el-option>
                <el-option label="99新" value="LIKE_NEW">
                  <el-icon><StarFilled /></el-icon> 99新 - 几乎全新，无明显使用痕迹
                </el-option>
                <el-option label="95新" value="EXCELLENT">
                  <el-icon><Medal /></el-icon> 95新 - 轻微使用痕迹，功能完好
                </el-option>
                <el-option label="9成新" value="GOOD">
                  <el-icon><CircleCheck /></el-icon> 9成新 - 有使用痕迹，功能正常
                </el-option>
                <el-option label="8成新" value="FAIR">
                  <el-icon><Warning /></el-icon> 8成新 - 明显使用痕迹，功能正常
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述商品的外观、功能、购买时间、使用情况等信息，越详细越容易成交"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="商品图片" prop="images">
          <div class="image-upload-section">
            <el-upload
              v-model:file-list="fileList"
              :action="uploadAction"
              :headers="uploadHeaders"
              :on-success="handleImageSuccess"
              :on-remove="handleImageRemove"
              :before-upload="beforeImageUpload"
              :limit="6"
              list-type="picture-card"
              accept="image/*"
            >
              <el-icon><Plus /></el-icon>
              <template #tip>
                <div class="upload-tip">
                  <p>最多上传6张图片，支持 JPG、PNG 格式</p>
                  <p>建议图片尺寸：800x600 像素，大小不超过 2MB</p>
                </div>
              </template>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button size="large" @click="handleReset">
              <el-icon><Refresh /></el-icon> 重置
            </el-button>
            <el-button size="large" @click="handleSaveDraft">
              <el-icon><Document /></el-icon> 保存草稿
            </el-button>
            <el-button size="large" @click="showDraftDrawer = true">
              <el-icon><FolderOpened /></el-icon> 草稿箱
              <el-badge v-if="draftCount > 0" :value="draftCount" style="margin-left:4px" />
            </el-button>
            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              @click="handleSubmit"
            >
              <el-icon><Upload /></el-icon> 发布商品
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 发布须知 -->
    <el-card class="notice-card" shadow="hover">
      <template #header>
        <h3><el-icon><InfoFilled /></el-icon> 发布须知</h3>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="(notice, index) in notices"
          :key="index"
          :icon="notice.icon"
          :type="notice.type"
        >
          <h4>{{ notice.title }}</h4>
          <p>{{ notice.content }}</p>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>

  <!-- 草稿箱抽屉 -->
  <el-drawer v-model="showDraftDrawer" title="草稿箱" direction="rtl" size="400px">
    <div v-if="drafts.length === 0" class="draft-empty">
      <el-empty description="暂无草稿" />
    </div>
    <div v-else class="draft-list">
      <div v-for="(draft, index) in drafts" :key="draft.savedAt" class="draft-item">
        <div class="draft-info">
          <div class="draft-title">{{ draft.title || '（无标题）' }}</div>
          <div class="draft-meta">
            <span v-if="draft.price">¥{{ draft.price }}</span>
            <span class="draft-time">{{ formatDraftTime(draft.savedAt) }}</span>
          </div>
        </div>
        <div class="draft-actions">
          <el-button size="small" type="primary" @click="restoreDraft(index)">恢复</el-button>
          <el-button size="small" type="danger" @click="deleteDraft(index)">删除</el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategories, createProduct } from '../api/product'
import { useUserStore } from '../stores/user'
import {
  Plus, Star, StarFilled, Medal, CircleCheck, Warning,
  Refresh, Document, Upload, InfoFilled, Check, Clock, FolderOpened
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const submitting = ref(false)
const categories = ref([])
const fileList = ref([])
const showDraftDrawer = ref(false)
const drafts = ref([])
const draftCount = ref(0)

const DRAFT_KEY = 'productDrafts'

const loadDrafts = () => {
  try {
    drafts.value = JSON.parse(localStorage.getItem(DRAFT_KEY) || '[]')
    draftCount.value = drafts.value.length
  } catch {
    drafts.value = []
  }
}

const saveDraftsToStorage = () => {
  localStorage.setItem(DRAFT_KEY, JSON.stringify(drafts.value))
  draftCount.value = drafts.value.length
}

const formatDraftTime = (ts) => {
  if (!ts) return ''
  return new Date(ts).toLocaleString('zh-CN')
}

const restoreDraft = (index) => {
  const draft = drafts.value[index]
  Object.assign(form, {
    title: draft.title || '',
    description: draft.description || '',
    price: draft.price || null,
    categoryId: draft.categoryId || null,
    condition: draft.condition || '',
    images: draft.images || []
  })
  showDraftDrawer.value = false
  ElMessage.success('草稿已恢复')
}

const deleteDraft = (index) => {
  drafts.value.splice(index, 1)
  saveDraftsToStorage()
  ElMessage.success('草稿已删除')
}

const form = reactive({
  title: '',
  description: '',
  price: null,
  categoryId: null,
  condition: '',
  images: []
})

const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 20, max: 1000, message: '描述长度在 20 到 1000 个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于 0', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  condition: [
    { required: true, message: '请选择商品成色', trigger: 'change' }
  ]
}

const notices = [
  {
    title: '真实描述',
    content: '请如实描述商品的外观、功能和使用情况，上传真实的商品图片',
    icon: Check,
    type: 'success'
  },
  {
    title: '审核时间',
    content: '商品发布后需要管理员审核，通常在 24 小时内完成审核',
    icon: Clock,
    type: 'warning'
  },
  {
    title: '交易安全',
    content: '建议使用平台推荐的交易方式，保障双方权益',
    icon: Check,
    type: 'info'
  }
]

const uploadAction = `/api/upload/image`
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

const handleImageSuccess = (response, file) => {
  if (response.success) {
    form.images.push(response.data.url)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const handleImageRemove = (file) => {
  const index = form.images.findIndex(url => url === file.response?.data?.url)
  if (index > -1) {
    form.images.splice(index, 1)
  }
}

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (form.images.length === 0) {
      ElMessage.warning('请至少上传一张商品图片')
      return
    }

    submitting.value = true
    
    await createProduct(form)
    
    ElMessage.success('商品发布成功，等待审核')
    router.push('/my-products')
    
  } catch (error) {
    console.error('发布商品失败:', error)
    ElMessage.error(error.message || '发布商品失败')
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  formRef.value?.resetFields()
  form.images = []
  fileList.value = []
}

const handleSaveDraft = () => {
  const draft = { ...form, images: [...form.images], savedAt: Date.now() }
  drafts.value.unshift(draft)
  // 最多保留10条草稿
  if (drafts.value.length > 10) drafts.value = drafts.value.slice(0, 10)
  saveDraftsToStorage()
  ElMessage.success('草稿已保存')
}

const loadDraft = () => {
  // 兼容旧版单条草稿
  const oldDraft = localStorage.getItem('productDraft')
  if (oldDraft) {
    try {
      const parsed = JSON.parse(oldDraft)
      parsed.savedAt = Date.now()
      drafts.value.unshift(parsed)
      saveDraftsToStorage()
      localStorage.removeItem('productDraft')
    } catch {}
  }
}

onMounted(() => {
  loadCategories()
  loadDrafts()
  loadDraft()
})
</script>

<style scoped>
.publish-product {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.publish-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.card-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.image-upload-section {
  width: 100%;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  text-align: center;
  margin-top: 8px;
}

.upload-tip p {
  margin: 4px 0;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.notice-card {
  border-radius: 12px;
}

.notice-card h3 {
  margin: 0;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
}

:deep(.el-timeline-item__content) {
  padding-left: 16px;
}

:deep(.el-timeline-item__content h4) {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
}

:deep(.el-timeline-item__content p) {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .publish-product {
    padding: 16px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
}

.draft-empty {
  padding: 40px 0;
}

.draft-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 4px 0;
}

.draft-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fafafa;
}

.draft-title {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 4px;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.draft-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #909399;
}

.draft-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}
</style>