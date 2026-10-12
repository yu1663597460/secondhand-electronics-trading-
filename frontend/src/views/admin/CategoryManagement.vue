<template>
  <div class="category-management">
    <div class="page-header">
      <h1>分类管理</h1>
      <div class="breadcrumb">管理后台 / 分类管理</div>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog = true" :icon="Plus">
          添加分类
        </el-button>
        <el-button @click="loadCategories" :icon="Refresh" :loading="loading">
          刷新
        </el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-info">
          <h3>{{ categories.length }}</h3>
          <p>分类总数</p>
        </div>
        <el-icon class="stat-icon" color="#1890ff"><Menu /></el-icon>
      </div>
      
      <div class="stat-card">
        <div class="stat-info">
          <h3>{{ totalProducts }}</h3>
          <p>商品总数</p>
        </div>
        <el-icon class="stat-icon" color="#52c41a"><Goods /></el-icon>
      </div>
      
      <div class="stat-card">
        <div class="stat-info">
          <h3>{{ activeCategories }}</h3>
          <p>有商品分类</p>
        </div>
        <el-icon class="stat-icon" color="#faad14"><Star /></el-icon>
      </div>
      
      <div class="stat-card">
        <div class="stat-info">
          <h3>{{ emptyCategories }}</h3>
          <p>空分类</p>
        </div>
        <el-icon class="stat-icon" color="#f5222d"><Warning /></el-icon>
      </div>
    </div>

    <div class="content-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索分类名称"
            style="width: 300px"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #append>
              <el-button @click="handleSearch" :icon="Search">搜索</el-button>
            </template>
          </el-input>
        </div>
        
        <div class="toolbar-right">
          <el-button @click="exportCategories" :icon="Download">导出</el-button>
          <el-button type="primary" @click="showAddDialog = true" :icon="Plus">
            添加分类
          </el-button>
        </div>
      </div>
      
      <el-table :data="filteredCategories" v-loading="loading" style="width: 100%" @sort-change="handleSortChange">
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="name" label="分类名称" min-width="150">
          <template #default="{ row }">
            <div class="category-name">
              <el-icon class="category-icon" :color="getCategoryColor(row.id)"><Menu /></el-icon>
              <strong>{{ row.name }}</strong>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="{ row }">
            <el-text class="description-text" :title="row.description">
              {{ row.description || '暂无描述' }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column prop="productCount" label="商品数量" width="120" sortable>
          <template #default="{ row }">
            <el-tag :type="row.productCount > 0 ? 'success' : 'info'">
              {{ row.productCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.productCount > 0 ? 'success' : 'warning'">
              {{ row.productCount > 0 ? '活跃' : '空闲' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" sortable>
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewCategoryProducts(row)" :icon="View">
              查看商品
            </el-button>
            <el-button size="small" @click="editCategory(row)" :icon="Edit">
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteCategoryHandler(row)"
              :disabled="row.productCount > 0"
              :icon="Delete"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑分类弹窗 -->
    <el-dialog 
      v-model="showAddDialog" 
      :title="editingCategory ? '编辑分类' : '添加分类'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="categoryForm" :rules="categoryRules" ref="categoryFormRef" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input 
            v-model="categoryForm.name" 
            placeholder="请输入分类名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="分类描述" prop="description">
          <el-input 
            v-model="categoryForm.description" 
            type="textarea" 
            :rows="4"
            placeholder="请输入分类描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="saveCategory" :loading="saving">
          {{ editingCategory ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 分类商品查看弹窗 -->
    <el-dialog 
      v-model="showProductsDialog" 
      :title="`${selectedCategory?.name} - 商品列表`"
      width="80%"
    >
      <el-table :data="categoryProducts" v-loading="loadingProducts" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="商品名称" min-width="200" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="sellerName" label="卖家" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getProductStatusType(row.status)">
              {{ getProductStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Menu, Plus, Refresh, Search, Download, View, Edit, Delete,
  Goods, Star, Warning
} from '@element-plus/icons-vue'
import { 
  getCategories, 
  createCategory, 
  updateCategory, 
  deleteCategory,
  getProducts
} from '../../api/admin'

const loading = ref(false)
const saving = ref(false)
const loadingProducts = ref(false)
const categories = ref([])
const categoryProducts = ref([])
const searchKeyword = ref('')
const showAddDialog = ref(false)
const showProductsDialog = ref(false)
const editingCategory = ref(null)
const selectedCategory = ref(null)
const categoryFormRef = ref(null)

const categoryForm = ref({
  name: '',
  description: ''
})

const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '分类名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const filteredCategories = computed(() => {
  if (!searchKeyword.value) return categories.value
  return categories.value.filter(category => 
    category.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
    (category.description && category.description.toLowerCase().includes(searchKeyword.value.toLowerCase()))
  )
})

const totalProducts = computed(() => {
  return categories.value.reduce((total, category) => total + (category.productCount || 0), 0)
})

const activeCategories = computed(() => {
  return categories.value.filter(category => (category.productCount || 0) > 0).length
})

const emptyCategories = computed(() => {
  return categories.value.filter(category => (category.productCount || 0) === 0).length
})

// 方法
const loadCategories = async () => {
  loading.value = true
  try {
    const response = await getCategories()
    categories.value = response.map(category => ({
      ...category,
      productCount: category.productCount || 0
    }))
  } catch (error) {
    console.error('加载分类列表失败:', error)
    ElMessage.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

const exportCategories = () => {
  ElMessage.info('导出功能开发中...')
}

const handleSortChange = ({ column, prop, order }) => {
  if (!order) return
  
  categories.value.sort((a, b) => {
    let aVal = a[prop]
    let bVal = b[prop]
    
    if (prop === 'createdAt') {
      aVal = new Date(aVal)
      bVal = new Date(bVal)
    }
    
    if (order === 'ascending') {
      return aVal > bVal ? 1 : -1
    } else {
      return aVal < bVal ? 1 : -1
    }
  })
}

const editCategory = (category) => {
  editingCategory.value = category
  categoryForm.value = {
    name: category.name,
    description: category.description || ''
  }
  showAddDialog.value = true
}

const cancelEdit = () => {
  showAddDialog.value = false
  editingCategory.value = null
  categoryForm.value = {
    name: '',
    description: ''
  }
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
}

const saveCategory = async () => {
  if (!categoryFormRef.value) return
  
  try {
    await categoryFormRef.value.validate()
    saving.value = true
    
    if (editingCategory.value) {
      // 更新分类
      const response = await updateCategory(editingCategory.value.id, categoryForm.value)
      const index = categories.value.findIndex(c => c.id === editingCategory.value.id)
      if (index !== -1) {
        categories.value[index] = {
          ...response,
          productCount: categories.value[index].productCount
        }
      }
      ElMessage.success('分类更新成功')
    } else {
      // 添加分类
      const response = await createCategory(categoryForm.value)
      const newCategory = {
        ...response,
        productCount: 0
      }
      categories.value.unshift(newCategory)
      ElMessage.success('分类添加成功')
    }
    
    cancelEdit()
  } catch (error) {
    console.error('保存分类失败:', error)
    ElMessage.error('保存分类失败')
  } finally {
    saving.value = false
  }
}

const deleteCategoryHandler = async (category) => {
  if (category.productCount > 0) {
    ElMessage.warning('该分类下还有商品，无法删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${category.name}"吗？此操作不可恢复。`,
      '删除分类',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteCategory(category.id)
    
    const index = categories.value.findIndex(c => c.id === category.id)
    if (index !== -1) {
      categories.value.splice(index, 1)
    }
    
    ElMessage.success('分类删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除分类失败')
    }
  }
}

const viewCategoryProducts = async (category) => {
  selectedCategory.value = category
  showProductsDialog.value = true
  
  if (category.productCount === 0) {
    categoryProducts.value = []
    return
  }
  
  loadingProducts.value = true
  try {
    const response = await getProducts({ categoryId: category.id, size: 100 })
    categoryProducts.value = response.content || []
  } catch (error) {
    ElMessage.error('加载商品列表失败')
  } finally {
    loadingProducts.value = false
  }
}

const getCategoryColor = (id) => {
  const colors = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2']
  return colors[id % colors.length]
}

const getProductStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'SOLD': 'info'
  }
  return types[status] || 'info'
}

const getProductStatusText = (status) => {
  const texts = {
    'PENDING': '待审核',
    'APPROVED': '在售',
    'SOLD': '已售出'
  }
  return texts[status] || '未知'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-management {
  padding: 24px;
  background: #f5f5f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  background: white;
  padding: 20px 24px;
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.breadcrumb {
  margin-top: 4px;
  color: #8c8c8c;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-info h3 {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #262626;
}

.stat-info p {
  color: #8c8c8c;
  margin: 0;
  font-size: 14px;
  font-weight: 500;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.8;
}

.content-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.toolbar {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  gap: 12px;
}

.category-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-icon {
  font-size: 16px;
}

.description-text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .category-management {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .toolbar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .toolbar-left,
  .toolbar-right {
    width: 100%;
    justify-content: center;
  }
}
</style>