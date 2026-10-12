<template>
  <el-button
    :class="['favorite-button', { 'is-favorited': isFavorited }]"
    :icon="isFavorited ? StarFilled : Star"
    :loading="loading"
    :type="isFavorited ? 'danger' : 'default'"
    :size="size"
    @click.stop="handleToggleFavorite"
  >
    {{ isFavorited ? '已收藏' : '收藏' }}
  </el-button>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  productId: {
    type: Number,
    required: true
  },
  size: {
    type: String,
    default: 'default'
  }
})

const emit = defineEmits(['favoriteChanged'])

const userStore = useUserStore()
const isFavorited = ref(false)
const loading = ref(false)

// 检查收藏状态
const checkFavoriteStatus = async () => {
  if (!userStore.isLoggedIn) {
    return
  }

  try {
    const response = await checkFavorite(props.productId)
    isFavorited.value = response.isFavorited
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 切换收藏状态
const handleToggleFavorite = async () => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    if (isFavorited.value) {
      // 取消收藏
      await removeFavorite(props.productId)
      isFavorited.value = false
      ElMessage.success('取消收藏成功')
    } else {
      // 添加收藏
      await addFavorite(props.productId)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
    
    // 触发事件通知父组件
    emit('favoriteChanged', isFavorited.value)
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// 监听商品ID变化
watch(() => props.productId, () => {
  checkFavoriteStatus()
})

// 初始化
onMounted(() => {
  checkFavoriteStatus()
})
</script>

<style scoped>
.favorite-button {
  transition: all 0.3s ease;
}

.favorite-button.is-favorited {
  animation: heartbeat 0.3s ease;
}

@keyframes heartbeat {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.favorite-button:hover {
  transform: translateY(-2px);
}
</style>
