<template>
  <div class="cat-page">

    <!-- 顶部分类标签栏 -->
    <div class="cat-tabs-wrap">
      <div class="cat-tabs">
        <span
          class="cat-tab" :class="{ active: !categoryId }"
          @click="switchCategory(null)"
        >推荐</span>
        <span
          v-for="cat in categories" :key="cat.id"
          class="cat-tab" :class="{ active: String(categoryId) === String(cat.id) }"
          @click="switchCategory(cat.id)"
        >{{ cat.name }}</span>
      </div>
    </div>

    <!-- 筛选排序栏 -->
    <div class="filter-bar">
      <div class="sort-tabs">
        <span
          v-for="s in sortOptions" :key="s.value"
          class="sort-tab" :class="{ active: sortBy === s.value }"
          @click="sortBy = s.value; loadProducts()"
        >{{ s.label }}</span>
      </div>
      <div class="filter-right">
        <div class="cond-tabs">
          <span
            v-for="c in condOptions" :key="c.value"
            class="cond-tab" :class="{ active: condition === c.value }"
            @click="condition = c.value; loadProducts()"
          >{{ c.label }}</span>
        </div>
        <div class="price-filter">
          <input v-model="minPrice" type="number" placeholder="最低价" class="price-input" @keyup.enter="loadProducts" />
          <span class="price-sep">—</span>
          <input v-model="maxPrice" type="number" placeholder="最高价" class="price-input" @keyup.enter="loadProducts" />
          <button class="price-btn" @click="loadProducts">筛选</button>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div v-loading="loading" class="products-wrap">
      <div v-if="!loading && products.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <p>暂无相关商品</p>
        <button @click="$router.push('/')">回到首页</button>
      </div>
      <div v-else class="products-grid">
        <div
          v-for="item in products" :key="item.id"
          class="product-card"
          @click="$router.push(`/product/${item.id}`)"
        >
          <div class="card-img-wrap">
            <img :src="getImage(item)" :alt="item.title" class="card-img" />
            <div class="card-sold-mask" v-if="item.status === 'SOLD'">已售出</div>
            <div class="card-cond-badge" v-if="item.condition && item.status !== 'SOLD'">
              {{ condText(item.condition) }}
            </div>
          </div>
          <div class="card-body">
            <div class="card-title">{{ item.title }}</div>
            <div class="card-location" v-if="item.seller?.username">
              <span class="card-cat" v-if="item.category?.name">{{ item.category.name }}</span>
              <span class="card-seller">{{ item.seller.username }}</span>
            </div>
            <div class="card-price-row">
              <span class="card-price">¥{{ Number(item.price).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="p => { currentPage = p; loadProducts() }"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProducts, getCategories } from '../api/product'

const route = useRoute()
const router = useRouter()

const products = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const sortBy = ref('newest')
const minPrice = ref('')
const maxPrice = ref('')
const condition = ref('')
const categories = ref([])

const sortOptions = [
  { label: '最新发布', value: 'newest' },
  { label: '价格最低', value: 'price_asc' },
  { label: '价格最高', value: 'price_desc' },
]

const condOptions = [
  { label: '全部成色', value: '' },
  { label: '全新', value: 'NEW' },
  { label: '99新', value: 'LIKE_NEW' },
  { label: '95新', value: 'EXCELLENT' },
  { label: '9成新', value: 'GOOD' },
  { label: '8成新', value: 'FAIR' },
]

const isSearch = computed(() => route.path.startsWith('/search'))
const categoryId = computed(() => route.params.id === 'all' ? null : route.params.id)
const keyword = computed(() => route.query.keyword || '')

const switchCategory = (id) => {
  currentPage.value = 1
  if (id === null) router.push('/category/all')
  else router.push(`/category/${id}`)
}

const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      status: 'APPROVED',
    }
    if (categoryId.value) params.categoryId = categoryId.value
    if (keyword.value) params.keyword = keyword.value
    if (condition.value) params.condition = condition.value
    if (minPrice.value) params.minPrice = minPrice.value
    if (maxPrice.value) params.maxPrice = maxPrice.value
    if (sortBy.value === 'price_asc') params.sort = 'price_asc'
    else if (sortBy.value === 'price_desc') params.sort = 'price_desc'
    else params.sort = 'newest'

    const data = await getProducts(params)
    products.value = data.content || []
    total.value = data.totalElements || 0
  } catch { products.value = []; total.value = 0 } finally { loading.value = false }
}

const getImage = (item) => item.images?.[0] || `https://picsum.photos/seed/${item.id}/300/220`
const condText = (c) => ({ NEW: '全新', LIKE_NEW: '99新', EXCELLENT: '95新', GOOD: '9成新', FAIR: '8成新' }[c] || c)

onMounted(async () => {
  try { categories.value = await getCategories() || [] } catch { }
  loadProducts()
})

watch(() => [route.params.id, route.query.keyword], () => {
  currentPage.value = 1
  loadProducts()
})
</script>

<style scoped>
.cat-page { background: #f4f5f7; min-height: 100vh; padding-bottom: 60px; }

/* ── 分类标签栏 ── */
.cat-tabs-wrap {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  position: sticky; top: 0; z-index: 10;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.cat-tabs {
  max-width: 1200px; margin: 0 auto;
  display: flex; align-items: center;
  padding: 0 20px;
  overflow-x: auto;
  scrollbar-width: none;
  gap: 0;
}
.cat-tabs::-webkit-scrollbar { display: none; }
.cat-tab {
  flex-shrink: 0;
  padding: 14px 22px;
  font-size: 14px; font-weight: 500;
  color: #555; cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.15s;
  white-space: nowrap;
}
.cat-tab:hover { color: #e4393c; }
.cat-tab.active {
  color: #e4393c;
  font-weight: 700;
  border-bottom-color: #e4393c;
}

/* ── 筛选栏 ── */
.filter-bar {
  max-width: 1200px; margin: 16px auto 0;
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; border-radius: 10px; padding: 12px 16px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
  flex-wrap: wrap; gap: 10px;
}
.filter-right { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.sort-tabs { display: flex; gap: 4px; }
.sort-tab {
  padding: 6px 14px; border-radius: 6px; font-size: 13px;
  cursor: pointer; color: #666; transition: all 0.15s;
}
.sort-tab:hover { background: #f5f5f5; }
.sort-tab.active { background: #e4393c; color: #fff; }
.cond-tabs { display: flex; gap: 4px; flex-wrap: wrap; }
.cond-tab {
  padding: 5px 11px; border-radius: 6px; font-size: 12px;
  cursor: pointer; color: #666; border: 1px solid #e8e8e8;
  background: #fff; transition: all 0.15s;
}
.cond-tab:hover { border-color: #e4393c; color: #e4393c; }
.cond-tab.active { background: #fff0f0; border-color: #e4393c; color: #e4393c; font-weight: 600; }
.price-filter { display: flex; align-items: center; gap: 6px; }
.price-input {
  width: 80px; padding: 6px 10px; border: 1px solid #e8e8e8;
  border-radius: 6px; font-size: 13px; outline: none; color: #333;
}
.price-input:focus { border-color: #e4393c; }
.price-sep { color: #ccc; }
.price-btn {
  padding: 6px 14px; background: #e4393c; color: #fff;
  border: none; border-radius: 6px; font-size: 13px; cursor: pointer;
}
.price-btn:hover { background: #c0292b; }

/* ── 商品网格 ── */
.products-wrap {
  max-width: 1200px; margin: 16px auto 0;
  padding: 0 20px;
  min-height: 300px;
}
.empty-state { text-align: center; padding: 80px 0; color: #bbb; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-state p { margin-bottom: 16px; font-size: 15px; }
.empty-state button {
  padding: 10px 24px; background: #e4393c; color: #fff;
  border: none; border-radius: 8px; cursor: pointer; font-size: 14px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

/* ── 商品卡片 ── */
.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.product-card:hover { box-shadow: 0 6px 20px rgba(0,0,0,0.12); }

.card-img-wrap {
  position: relative;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: #f5f5f5;
}
.card-img {
  width: 100%; height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.product-card:hover .card-img { transform: scale(1.04); }

.card-sold-mask {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.45);
  color: #fff; display: flex;
  align-items: center; justify-content: center;
  font-size: 15px; font-weight: 600;
}

/* 成色角标 */
.card-cond-badge {
  position: absolute;
  bottom: 0; left: 0;
  background: rgba(0,0,0,0.55);
  color: #fff;
  font-size: 11px;
  padding: 3px 8px;
  border-top-right-radius: 6px;
}

.card-body { padding: 10px 10px 12px; }

.card-title {
  font-size: 13px; font-weight: 500; color: #222;
  margin-bottom: 6px;
  overflow: hidden; text-overflow: ellipsis;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  line-height: 1.4; min-height: 36px;
}

.card-location {
  display: flex; align-items: center; gap: 6px;
  margin-bottom: 6px;
}
.card-cat {
  font-size: 11px; color: #999;
  background: #f5f5f5; padding: 1px 6px; border-radius: 3px;
}
.card-seller { font-size: 11px; color: #bbb; }

.card-price-row { display: flex; align-items: baseline; gap: 4px; }
.card-price { font-size: 18px; font-weight: 700; color: #e4393c; }

/* ── 分页 ── */
.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; }

/* ── 响应式 ── */
@media (max-width: 1024px) {
  .products-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 768px) {
  .products-grid { grid-template-columns: repeat(3, 1fr); gap: 8px; }
  .products-wrap { padding: 0 12px; }
  .filter-bar { margin: 12px 12px 0; }
  .price-input { width: 64px; }
  .cat-tab { padding: 12px 14px; font-size: 13px; }
}
@media (max-width: 480px) {
  .products-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
