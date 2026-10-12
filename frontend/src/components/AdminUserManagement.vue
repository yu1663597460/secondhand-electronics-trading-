<template>
  <div class="user-management">
    <!-- 搜索栏 -->
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
    </div>

    <!-- 用户列表 -->
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
      <el-table-column prop="createdAt" label="注册时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="viewUserDetail(row)">详情</el-button>
          <el-button 
            size="small" 
            :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
            @click="toggleUserStatus(row)"
          >
            {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
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
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, updateUserStatus } from '../api/admin'
import UserDetailDialog from './UserDetailDialog.vue'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const searchKeyword = ref('')
const showUserDetail = ref(false)
const selectedUser = ref(null)

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
    await ElMessageBox.prompt(`请输入${action}原因`, `${action}用户`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入原因'
    })
    
    await updateUserStatus(user.id, {
      status: newStatus,
      reason: '管理员操作'
    })
    
    ElMessage.success(`用户${action}成功`)
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`用户${action}失败`)
    }
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>