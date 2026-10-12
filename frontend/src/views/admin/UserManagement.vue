<template>
  <div class="user-management">
    <div class="page-header">
      <h1>用户管理</h1>
      <div class="breadcrumb">管理后台 / 用户管理</div>
    </div>
    
    <!-- 搜索栏 -->
    <div class="content-card">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名、邮箱或手机号"
          style="width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch" :icon="Search">搜索</el-button>
          </template>
        </el-input>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="primary" @click="loadUserStatistics">刷新统计</el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-info">
          <h3>{{ userStats.totalUsers || 0 }}</h3>
          <p>总用户数</p>
        </div>
        <el-icon class="stat-icon" color="#1890ff"><User /></el-icon>
      </div>
      
      <div class="stat-card">
        <div class="stat-info">
          <h3>{{ userStats.activeUsers || 0 }}</h3>
          <p>活跃用户</p>
        </div>
        <el-icon class="stat-icon" color="#52c41a"><UserFilled /></el-icon>
      </div>
      
      <div class="stat-card">
        <div class="stat-info">
          <h3>{{ userStats.newUsersLast30Days || 0 }}</h3>
          <p>30天新增</p>
        </div>
        <el-icon class="stat-icon" color="#faad14"><TrendCharts /></el-icon>
      </div>
      
      <div class="stat-card">
        <div class="stat-info">
          <h3>{{ userStats.disabledUsers || 0 }}</h3>
          <p>禁用用户</p>
        </div>
        <el-icon class="stat-icon" color="#f5222d"><CircleClose /></el-icon>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="content-card">
      <el-table :data="users" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="verificationStatus" label="认证状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getVerificationTagType(row.verificationStatus)">
              {{ getVerificationText(row.verificationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button size="small" @click="viewUserDetail(row)">详情</el-button>
            <el-button
              size="small"
              :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
              @click="toggleUserStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-button
              v-if="row.verificationStatus === 'PENDING'"
              size="small"
              type="warning"
              @click="viewUserDetail(row)"
            >
              待审核
            </el-button>
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
    </div>

    <!-- 用户详情弹窗 -->
    <UserDetailDialog 
      v-model="showUserDetail" 
      :user="selectedUser" 
      @refresh="loadUsers"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, User, UserFilled, TrendCharts, CircleClose } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, updateUserStatus, getUserStatistics } from '../../api/admin'
import UserDetailDialog from '../../components/UserDetailDialog.vue'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const searchKeyword = ref('')
const showUserDetail = ref(false)
const selectedUser = ref(null)
const userStats = ref({})

const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    const response = await getUsers(params)
    users.value = response.content
    total.value = response.totalElements
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const loadUserStatistics = async () => {
  try {
    const response = await getUserStatistics()
    userStats.value = response
  } catch (error) {
    ElMessage.error('加载用户统计失败')
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

const resetSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  loadUsers()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadUsers()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadUsers()
}

const viewUserDetail = (user) => {
  selectedUser.value = user
  showUserDetail.value = true
}

const toggleUserStatus = async (user) => {
  const newStatus = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const action = newStatus === 'DISABLED' ? '禁用' : '启用'
  
  try {
    const { value: reason } = await ElMessageBox.prompt(`请输入${action}原因`, `${action}用户`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入原因'
    })
    
    await updateUserStatus(user.id, {
      status: newStatus,
      reason: reason || '管理员操作'
    })
    
    ElMessage.success(`用户${action}成功`)
    loadUsers()
    loadUserStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`用户${action}失败`)
    }
  }
}

const getVerificationTagType = (status) => {
  const types = {
    'VERIFIED': 'success',
    'PENDING': 'warning',
    'UNVERIFIED': 'info'
  }
  return types[status] || 'info'
}

const getVerificationText = (status) => {
  const texts = {
    'VERIFIED': '已认证',
    'PENDING': '待审核',
    'UNVERIFIED': '未认证'
  }
  return texts[status] || '未知'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}

onMounted(() => {
  loadUsers()
  loadUserStatistics()
})
</script>

<style scoped>
.user-management {
  padding: 24px;
}

.page-header {
  background: white;
  padding: 16px 24px;
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-header h1 {
  font-size: 20px;
  font-weight: 500;
  color: #262626;
  margin: 0;
}

.breadcrumb {
  margin-top: 8px;
  color: #8c8c8c;
  font-size: 14px;
}

.content-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 24px;
}

.search-bar {
  padding: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
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
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-info h3 {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.stat-info p {
  color: #8c8c8c;
  margin: 0;
  font-size: 14px;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.8;
}

.pagination {
  padding: 20px;
  display: flex;
  justify-content: center;
}
</style>