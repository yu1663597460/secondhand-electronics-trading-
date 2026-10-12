<template>
  <div class="fav-page">
    <!-- 顶部 -->
    <div class="fav-header">
      <div class="fav-header-left">
        <h1>❤️ 我的收藏</h1>
        <span class="fav-count" v-if="totalElements > 0">{{ totalElements }} 件好物</span>
      </div>
      <el-input
        v-model="searchKw"
        placeholder="搜索收藏的商品..."
        clearable
        style="width:220px"
        @input="filterFavorites"
      >
        <template #prefix>🔍</template>
      </el-input>
    </div>

    <!-- 排序栏 -->
    <div class="sort-bar" v-if="favorites.length > 0">
      <span class="sort-label">排序：</span>
      <span class="sort-opt" :class="{ active: sortBy === 'time' }" @click="sortBy = 'time'">最近收藏</span>
      <span class="sort-opt" :class="{ active: sortBy === 'price_asc' }" @click="sortBy = 'price_asc'">价格↑</span>
      <span class="sort-opt" :class="{ active: sortBy === 'price_desc' }" @click="sortBy = 'price_desc'">价格↓</span>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="fav-loading">
      <div class="loading-dots"><span /><span /><span /></div>
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="displayList.length === 0" class="fav-empty">
      <div class="empty-icon">🤍</div>
      <h3>还没有收藏任何商品</h3>
      <p>浏览商品时点击心形图标即可收藏</p>
      <button class="btn-browse" @click="$router.push('/')">去逛逛</button>
    </div>

    <!-- 收藏网格 -->
    <div v-else class="fav-grid">
      <div
        v-for="fav in displayList"
        :key="fav.id"
        class="fav-card"
        :class="{ sold: fav.product?.status === 'SOLD' }"
      >
        <div class="fav-img" @click="goDetail(fav.product?.id)">
          <img :src="getImg(fav.product)" :alt="fav.product?.title" @error="onImgErr" />
          <div class="fav-img-overlay">
            <span>查看详情</span>
          </div>
          <div v-if="fav.product?.status !== 'APPROVED'" class="sold-mask">
            <span>{{ getStatusText(fav.product?.status) }}</span>
          </div>
        </div>

        <div class="fav-body">
          <div class="fav-tags">
            <span class="tag-cat" v-if="fav.product?.category?.name">{{ fav.product.category.name }}</span>
            <span class="tag-cond" v-if="fav.product?.condition">{{ getCondText(fav.product.condition) }}</span>
          </div>
          <h3 class="fav-title" @click="goDetail(fav.product?.id)">{{ fav.product?.title }}</h3>
          <p class="fav-desc">{{ fav.product?.description }}</p>
          <div class="fav-bottom">
            <span class="fav-price">¥{{ fav.product?.price }}</span>
            <span class="fav-time">{{ formatDate(fav.createdAt) }}</span>
          </div>
        </div>

        <div class="fav-actions">
          <button class="btn-detail" @click="goDetail(fav.product?.id)">查看商品</button>
          <button class="btn-remove" @click="removeFav(fav.product?.id)">取消收藏</button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="fav-pagination" v-if="totalPages > 1">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalElements"
        :page-sizes="[12, 24, 36]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="p => { pageSize = p; currentPage = 1; loadFavorites() }"
        @current-change="p => { currentPage = p; loadFavorites() }"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFavorites, removeFavorite } from '@/api/favorite'

const router = useRouter()
const favorites = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const totalElements = ref(0)
const totalPages = ref(0)
const searchKw = ref('')
const sortBy = ref('time')

const displayList = computed(() => {
  let list = [...favorites.value]
  if (searchKw.value) {
    const kw = searchKw.value.toLowerCase()
    list = list.filter(f => f.product?.title?.toLowerCase().includes(kw))
  }
  if (sortBy.value === 'price_asc') list.sort((a, b) => a.product?.price - b.product?.price)
  else if (sortBy.value === 'price_desc') list.sort((a, b) => b.product?.price - a.product?.price)
  return list
})

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getFavorites(currentPage.value - 1, pageSize.value)
    favorites.value = res.content || []
    totalElements.value = res.totalElements || 0
    totalPages.value = res.totalPages || 0
  } catch (e) {
    ElMessage.error('加载收藏失败')
  } finally {
    loading.value = false
  }
}

const removeFav = async (productId) => {
  try {
    await ElMessageBox.confirm('确定取消收藏这件商品吗？', '提示', { type: 'warning', confirmButtonText: '取消收藏', cancelButtonText: '再想想' })
    await removeFavorite(productId)
    ElMessage.success('已取消收藏')
    loadFavorites()
  } catch (e) { /* cancel */ }
}

const filterFavorites = () => { /* computed 自动处理 */ }
const goDetail = (id) => { if (id) router.push(`/product/${id}`) }

const getImg = (p) => {
  if (p?.images?.length > 0) return p.images[0]
  return `https://picsum.photos/seed/${p?.id || 'item'}/400/300`
}
const onImgErr = (e) => { e.target.src = 'https://picsum.photos/seed/default/400/300' }
const getStatusText = (s) => ({ PENDING: '审核中', SOLD: '已售出', REJECTED: '已拒绝', TAKEN_DOWN: '已下架' }[s] || s)
const getCondText = (c) => ({ NEW: '全新', LIKE_NEW: '99新', EXCELLENT: '95新', GOOD: '9成新', FAIR: '8成新' }[c] || c)
const formatDate = (d) => {
  if (!d) return ''
  const diff = Date.now() - new Date(d)
  const days = Math.floor(diff / 86400000)
  if (days === 0) return '今天'
  if (days < 30) return `${days}天前`
  return new Date(d).toLocaleDateString('zh-CN')
}

onMounted(loadFavorites)
</script>

<style scoped>
.fav-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 48px;
}

.fav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.fav-header-left { display: flex; align-items: baseline; gap: 12px; }
.fav-header h1 { font-size: 24px; font-weight: 700; color: #1a1a2e; margin: 0; }
.fav-count { font-size: 13px; color: #999; background: #f5f5f5; padding: 2px 10px; border-radius: 20px; }

.sort-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 13px;
}
.sort-label { color: #999; }
.sort-opt {
  padding: 4px 14px;
  border-radius: 20px;
  border: 1px solid #e8e8e8;
  cursor: pointer;
  color: #666;
  transition: all 0.15s;
}
.sort-opt:hover { border-color: #e4393c; color: #e4393c; }
.sort-opt.active { background: #e4393c; border-color: #e4393c; color: #fff; }

.fav-loading {
  text-align: center;
  padding: 80px 0;
  color: #999;
}
.loading-dots { display: flex; justify-content: center; gap: 6px; margin-bottom: 12px; }
.loading-dots span {
  width: 8px; height: 8px; border-radius: 50%; background: #e4393c;
  animation: bounce 1.2s infinite;
}
.loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.loading-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce { 0%,80%,100% { transform: scale(0.6); } 40% { transform: scale(1); } }

.fav-empty {
  text-align: center;
  padding: 80px 0;
}
.empty-icon { font-size: 64px; margin-bottom: 16px; }
.fav-empty h3 { font-size: 18px; color: #333; margin: 0 0 8px; }
.fav-empty p { color: #999; font-size: 14px; margin: 0 0 24px; }
.btn-browse {
  padding: 10px 32px;
  background: #e4393c;
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.btn-browse:hover { opacity: 0.85; }

.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.fav-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07);
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}
.fav-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.11); }
.fav-card.sold { opacity: 0.7; }

.fav-img {
  position: relative;
  height: 200px;
  overflow: hidden;
  cursor: pointer;
  background: #f5f5f5;
}
.fav-img img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.fav-card:hover .fav-img img { transform: scale(1.05); }
.fav-img-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}
.fav-card:hover .fav-img-overlay { opacity: 1; }
.sold-mask {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}

.fav-body { padding: 14px 16px 10px; flex: 1; }
.fav-tags { display: flex; gap: 6px; margin-bottom: 8px; flex-wrap: wrap; }
.tag-cat, .tag-cond {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f0f2f5;
  color: #666;
}
.fav-title {
  font-size: 15px;
  font-weight: 600;
  color: #222;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}
.fav-title:hover { color: #e4393c; }
.fav-desc {
  font-size: 12px;
  color: #999;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}
.fav-bottom { display: flex; justify-content: space-between; align-items: center; }
.fav-price { font-size: 18px; font-weight: 700; color: #e4393c; }
.fav-time { font-size: 11px; color: #bbb; }

.fav-actions {
  display: flex;
  gap: 8px;
  padding: 10px 16px 14px;
  border-top: 1px solid #f5f5f5;
}
.btn-detail, .btn-remove {
  flex: 1;
  height: 34px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  border: none;
  transition: all 0.15s;
}
.btn-detail { background: #e4393c; color: #fff; }
.btn-detail:hover { background: #c0292b; }
.btn-remove { background: #f5f5f5; color: #666; }
.btn-remove:hover { background: #ffe0e0; color: #e4393c; }

.fav-pagination { display: flex; justify-content: center; padding: 16px 0; }
</style>
