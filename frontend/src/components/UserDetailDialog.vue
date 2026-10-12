<template>
  <el-dialog
    v-model="visible"
    title="用户详情"
    width="800px"
    @close="handleClose"
  >
    <div v-if="userDetail" class="user-detail">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ userDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ userDetail.username }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userDetail.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userDetail.phone || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ userDetail.realName || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ userDetail.idCard || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="账户状态">
          <el-tag :type="userDetail.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ userDetail.status === 'ACTIVE' ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="认证状态">
          <el-tag :type="getVerificationTagType(userDetail.verificationStatus)">
            {{ getVerificationText(userDetail.verificationStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDate(userDetail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="最后更新">{{ formatDate(userDetail.updatedAt) }}</el-descriptions-item>
      </el-descriptions>

      <div class="statistics-section">
        <h3>统计信息</h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic title="发布商品数" :value="userDetail.productCount || 0" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="订单数量" :value="userDetail.orderCount || 0" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="完成订单" :value="userDetail.completedOrderCount || 0" />
          </el-col>
        </el-row>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <!-- 实名认证审核按钮 -->
      <template v-if="userDetail?.verificationStatus === 'PENDING'">
        <el-button type="danger" @click="handleVerification('REJECT')">拒绝认证</el-button>
        <el-button type="success" @click="handleVerification('APPROVE')">通过认证</el-button>
      </template>
      <el-button
        type="primary"
        @click="toggleStatus"
        :disabled="!userDetail"
      >
        {{ userDetail?.status === 'ACTIVE' ? '禁用用户' : '启用用户' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserDetail, updateUserStatus, reviewVerification } from '../api/admin'

const props = defineProps({
  modelValue: Boolean,
  user: Object
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = ref(false)
const userDetail = ref(null)
const loading = ref(false)

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.user) {
    loadUserDetail()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const loadUserDetail = async () => {
  if (!props.user?.id) return
  
  loading.value = true
  try {
    const response = await getUserDetail(props.user.id)
    userDetail.value = response
  } catch (error) {
    ElMessage.error('加载用户详情失败')
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  visible.value = false
  userDetail.value = null
}

const handleVerification = async (action) => {
  const label = action === 'APPROVE' ? '通过' : '拒绝'
  try {
    let reason = ''
    if (action === 'APPROVE') {
      await ElMessageBox.confirm(
        `确认通过该用户（${userDetail.value.realName || userDetail.value.username}）的实名认证？`,
        '通过实名认证',
        { confirmButtonText: '确认通过', cancelButtonText: '取消', type: 'success' }
      )
    } else {
      const res = await ElMessageBox.prompt(
        '请输入拒绝原因（可选）',
        '拒绝实名认证',
        { confirmButtonText: '确认拒绝', cancelButtonText: '取消', inputPlaceholder: '请输入原因' }
      )
      reason = res.value || ''
    }
    await reviewVerification(userDetail.value.id, { action, reason })
    ElMessage.success(`实名认证已${label}`)
    userDetail.value.verificationStatus = action === 'APPROVE' ? 'VERIFIED' : 'UNVERIFIED'
    emit('refresh')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

const toggleStatus = async () => {
  if (!userDetail.value) return
  
  const newStatus = userDetail.value.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const action = newStatus === 'DISABLED' ? '禁用' : '启用'
  
  try {
    const { value: reason } = await ElMessageBox.prompt(`请输入${action}原因`, `${action}用户`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入原因'
    })
    
    await updateUserStatus(userDetail.value.id, {
      status: newStatus,
      reason: reason || '管理员操作'
    })
    
    ElMessage.success(`用户${action}成功`)
    userDetail.value.status = newStatus
    emit('refresh')
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
</script>

<style scoped>
.user-detail {
  padding: 20px 0;
}

.statistics-section {
  margin-top: 30px;
}

.statistics-section h3 {
  margin-bottom: 20px;
  color: #303133;
}
</style>