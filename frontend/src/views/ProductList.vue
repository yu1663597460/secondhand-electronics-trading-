<template>
  <div class="home-page">

    <!-- 轮播 + 搜索叠层 -->
    <div class="hero-section">
      <el-carousel class="hero-carousel" :interval="5000" arrow="hover" indicator-position="outside">
        <el-carousel-item v-for="banner in banners" :key="banner.img">
          <div class="slide" :style="{ backgroundImage: `url(${banner.img})` }">
            <div class="slide-overlay" />
            <div class="slide-text">
              <div class="slide-tag">{{ banner.tag }}</div>
              <h1 v-html="banner.title" />
              <p>{{ banner.desc }}</p>
              <button class="slide-cta" :class="banner.btnClass" @click="$router.push(banner.link)">{{ banner.btnText }}</button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 搜索区 -->
    <div class="search-wrap animate-on-scroll fade-up">
      <div class="search-box">
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索手机、电脑、耳机、相机..."
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>
      <div class="hot-tags">
        <span class="hot-label">热门：</span>
        <span v-for="kw in hotKeywords" :key="kw" class="hot-tag" @click="searchKeyword = kw; handleSearch()">{{ kw }}</span>
      </div>
    </div>

    <!-- 快捷功能入口 -->
    <div class="shortcuts-wrap animate-on-scroll fade-up">
      <div class="shortcuts">
        <div class="shortcut-item" @click="$router.push('/publish')">
          <div class="sc-icon" style="background:#fff3e0">📦</div>
          <span>发布闲置</span>
        </div>
        <div class="shortcut-item" @click="$router.push('/orders')">
          <div class="sc-icon" style="background:#e3f2fd">📋</div>
          <span>我的订单</span>
        </div>
        <div class="shortcut-item" @click="$router.push('/favorites')">
          <div class="sc-icon" style="background:#fce4ec">❤️</div>
          <span>我的收藏</span>
        </div>
        <div class="shortcut-item" @click="$router.push('/cart')">
          <div class="sc-icon" style="background:#e8f5e9">🛒</div>
          <span>购物车</span>
        </div>
        <div class="shortcut-item" @click="$router.push('/profile?tab=carbon')">
          <div class="sc-icon" style="background:#f3e5f5">🌱</div>
          <span>碳积分</span>
        </div>
        <div class="shortcut-item" @click="$router.push('/chat')">
          <div class="sc-icon" style="background:#e0f7fa">💬</div>
          <span>消息</span>
        </div>
      </div>
    </div>

    <!-- 分类标题 -->
    <div class="section-hd animate-on-scroll fade-up" style="padding-bottom: 0;">
      <div class="section-hd-title">— 闲置数码 —</div>
      <div class="section-hd-sub">每笔交易都在为地球减负</div>
    </div>

    <!-- 商品分类 -->
    <div class="category-outer animate-on-scroll fade-up">
    <div class="category-wrap">
      <div class="section-header">
        <span class="section-title">商品分类</span>
        <span class="section-more" @click="$router.push('/category/all')">查看全部 ›</span>
      </div>
      <div class="cat-grid" v-loading="loadingCats">
        <!-- 全部 -->
        <div class="cat-item" @click="$router.push('/category/all')">
          <div class="cat-icon-circle">
            <img src="/images/categories/all.png" class="cat-img" alt="全部" />
          </div>
          <span class="cat-name">全部</span>
        </div>
        <div
          v-for="cat in categories" :key="cat.id"
          class="cat-item"
          @click="$router.push(`/category/${cat.id}`)"
        >
          <div class="cat-icon-circle">
            <img :src="getCatImage(cat.name)" :alt="cat.name" class="cat-img" />
          </div>
          <span class="cat-name">{{ cat.name }}</span>
        </div>
      </div>
    </div>
    </div>

    <!-- 环保宣传区 -->
    <div class="eco-wrap animate-on-scroll fade-up">
      <div class="section-hd">
        <div class="section-hd-title">— 绿色交易 —</div>
        <div class="section-hd-sub">每一次二手交易，都是对地球的一份承诺</div>
      </div>
      <div class="eco-grid">
        <div class="eco-card animate-on-scroll fade-up stagger-1" style="background: #2d3a2e;">
          <img src="/images/eco/eco-1.jpg" alt="减少碳排放" class="eco-img" />
          <div class="eco-label">· 减少碳排放 ·</div>
        </div>
        <div class="eco-card animate-on-scroll fade-up stagger-2" style="background: #3a2d4a;">
          <img src="/images/eco/eco-2.jpg" alt="循环再利用" class="eco-img" />
          <div class="eco-label">· 循环再利用 ·</div>
        </div>
        <div class="eco-card animate-on-scroll fade-up stagger-3" style="background: #2d4a3a;">
          <img src="/images/eco/eco-3.jpg" alt="碳积分奖励" class="eco-img" />
          <div class="eco-label">· 碳积分奖励 ·</div>
        </div>
        <div class="eco-card animate-on-scroll fade-up stagger-4" style="background: #4a3a2d;">
          <img src="/images/eco/eco-4.jpg" alt="闲置变现" class="eco-img" />
          <div class="eco-label">· 闲置变现 ·</div>
        </div>
        <div class="eco-card animate-on-scroll fade-up stagger-5" style="background: #2d3a4a;">
          <img src="/images/eco/eco-5.jpg" alt="绿色生活" class="eco-img" />
          <div class="eco-label">· 绿色生活 ·</div>
        </div>
      </div>
    </div>

    <!-- 底部宣传横幅 -->
    <div class="eco-banner animate-on-scroll fade-up">
      <div class="eco-banner-img">
        <img src="/images/eco/eco-6.jpg" alt="闲置二手交易减碳" />
        <div class="eco-banner-overlay" />
        <div class="eco-banner-title">闲置二手交易减碳</div>
      </div>
      <div class="eco-banner-footer">
        <span class="eco-banner-sub">♻️ 由武汉工程大学邮电与信息工程学院 余洪刚制作</span>
        <span class="eco-banner-sub">🌱 加入我们，让闲置流动，让绿色延续</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories } from '../api/product'

const router = useRouter()
const searchKeyword = ref('')
const categories = ref([])
const loadingCats = ref(false)

const hotKeywords = ['iPhone', '笔记本', 'AirPods', '相机', '游戏机', '平板', '机械键盘']

const banners = [
  {
    img: '/images/banners/vx.jpg',
    pos: 'center center',
    tag: '二手数码 · 品质保障',
    title: '让闲置流动<br/>让价值延续',
    desc: '把不用的好物传递给需要的人，减少浪费从你我做起',
    btnText: '立即发布闲置',
    btnClass: '',
    link: '/publish'
  },
  {
    img: '/images/banners/02.jpg',
    pos: 'center center',
    tag: '碳积分 · 环保有奖',
    title: '交易即减碳<br/>积分换优惠',
    desc: '每笔交易都在为地球减负，累计碳积分可在结算时抵扣现金',
    btnText: '查看我的积分',
    btnClass: 'green',
    link: '/profile'
  },
  {
    img: '/images/banners/banner-2.jpg',
    pos: 'center center',
    tag: '正品保障 · 放心购买',
    title: '好物不贵<br/>品质二手',
    desc: '实名认证卖家，真实商品描述，让每一笔交易都安心',
    btnText: '浏览全部商品',
    btnClass: '',
    link: '/category/all'
  },
  {
    img: '/images/banners/banner-3.jpg',
    pos: 'center center',
    tag: '限时特惠 · 精选好货',
    title: '淘到心仪好物<br/>省钱又环保',
    desc: '海量二手数码等你来挑，品质保障，价格实惠',
    btnText: '马上去逛逛',
    btnClass: '',
    link: '/category/all'
  }
]

// 使用环保主题图片作为分类图标，体现绿色交易理念
const CAT_IMAGES = {
  '手机': '/images/categories/phone.png',
  '电脑': '/images/categories/computer.png',
  '笔记本': '/images/categories/laptop.png',
  '平板': '/images/categories/laptop.png',
  '耳机': '/images/categories/headphone.png',
  '配件': '/images/categories/headphone.png',
  '相机': '/images/categories/camera.png',
  '游戏': '/images/categories/gaming.png',
}

const getCatImage = (name) => {
  const key = Object.keys(CAT_IMAGES).find(k => name?.includes(k))
  return key ? CAT_IMAGES[key] : '/images/categories/phone.png'
}

const loadCategories = async () => {
  loadingCats.value = true
  try { categories.value = await getCategories() || [] } catch { } finally { loadingCats.value = false }
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) return
  router.push({ path: '/search', query: { keyword: searchKeyword.value.trim() } })
}

// 滚动动画 - Intersection Observer
let scrollObserver = null

const initScrollAnimations = () => {
  scrollObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animate-in')
      }
    })
  }, {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  })

  // 观察所有需要动画的元素
  document.querySelectorAll('.animate-on-scroll').forEach(el => {
    scrollObserver.observe(el)
  })
}

onMounted(() => {
  loadCategories()
  setTimeout(initScrollAnimations, 100)
})

onUnmounted(() => {
  if (scrollObserver) {
    scrollObserver.disconnect()
  }
})
</script>

<style scoped>
.home-page { background: #f4f5f7; min-height: 100vh; }

/* ── 滚动动画 ── */
.animate-on-scroll {
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.6s ease-out, transform 0.6s ease-out;
}

.animate-on-scroll.animate-in {
  opacity: 1;
  transform: translateY(0);
}

/* 交错动画延迟 */
.stagger-1 { transition-delay: 0.1s; }
.stagger-2 { transition-delay: 0.2s; }
.stagger-3 { transition-delay: 0.3s; }
.stagger-4 { transition-delay: 0.4s; }
.stagger-5 { transition-delay: 0.5s; }

/* 从左侧滑入 */
.fade-left {
  opacity: 0;
  transform: translateX(-40px);
}
.fade-left.animate-in {
  opacity: 1;
  transform: translateX(0);
}

/* 从右侧滑入 */
.fade-right {
  opacity: 0;
  transform: translateX(40px);
}
.fade-right.animate-in {
  opacity: 1;
  transform: translateX(0);
}

/* 缩放淡入 */
.fade-scale {
  opacity: 0;
  transform: scale(0.9);
}
.fade-scale.animate-in {
  opacity: 1;
  transform: scale(1);
}

/* ── 轮播英雄区 ── */
.hero-section {
  position: relative;
  width: 100%;
}
.hero-carousel {
  width: 100%;
  height: 75vh;
  min-height: 480px;
  max-height: 700px;
}
.hero-carousel :deep(.el-carousel__container) { height: 100%; }
.hero-carousel :deep(.el-carousel__arrow) {
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.25);
  color: #fff;
  width: 44px; height: 44px;
}
.hero-carousel :deep(.el-carousel__arrow:hover) { background: rgba(255,255,255,0.3); }

.slide {
  width: 100%; height: 100%;
  display: flex; align-items: center;
  padding: 0 8%;
  position: relative; overflow: hidden;
  background-size: cover;
  background-position: center;
}
.slide-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(90deg, rgba(0,0,0,0.65) 0%, rgba(0,0,0,0.3) 50%, rgba(0,0,0,0.05) 100%);
}
.slide-text {
  z-index: 2; max-width: 520px;
  display: flex; flex-direction: column; gap: 16px;
}
.slide-tag {
  display: inline-block;
  background: rgba(255,255,255,0.15);
  color: rgba(255,255,255,0.9);
  font-size: 13px; padding: 4px 14px;
  border-radius: 20px; width: fit-content;
  border: 1px solid rgba(255,255,255,0.2);
  backdrop-filter: blur(4px);
}
.slide-text h1 {
  font-size: clamp(32px, 4vw, 52px);
  font-weight: 800; color: #fff; margin: 0;
  line-height: 1.2;
  text-shadow: 0 2px 20px rgba(0,0,0,0.3);
}
.slide-text p {
  font-size: 15px; color: rgba(255,255,255,0.75);
  margin: 0; line-height: 1.6; max-width: 400px;
}
.slide-cta {
  display: inline-flex; align-items: center;
  padding: 13px 28px; border-radius: 8px;
  background: #e4393c; color: #fff;
  border: none; font-size: 15px; font-weight: 600;
  cursor: pointer; width: fit-content;
  transition: all 0.2s;
  box-shadow: 0 4px 20px rgba(228,57,60,0.4);
}
.slide-cta:hover { background: #c0292b; transform: translateY(-2px); }
.slide-cta.green { background: #52c41a; box-shadow: 0 4px 20px rgba(82,196,26,0.4); }
.slide-cta.green:hover { background: #389e0d; }

/* 搜索区 */
.search-wrap {
  max-width: 720px; margin: 0 auto;
  padding: 28px 20px 0;
}
.search-box {
  display: flex; align-items: center;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}
.search-input {
  flex: 1; border: none; outline: none;
  font-size: 15px; color: #333;
  background: transparent; padding: 13px 16px;
}
.search-input::placeholder { color: #bbb; }
.search-btn {
  padding: 13px 32px; background: #e4393c; color: #fff;
  border: none; font-size: 15px; font-weight: 600;
  cursor: pointer; transition: background 0.15s; flex-shrink: 0;
}
.search-btn:hover { background: #c0292b; }
.hot-tags {
  display: flex; align-items: center; gap: 8px;
  margin-top: 12px; flex-wrap: wrap;
}
.hot-label { font-size: 12px; color: #999; flex-shrink: 0; }
.hot-tag {
  font-size: 12px; color: #666;
  cursor: pointer; padding: 3px 10px;
  border-radius: 12px; background: #fff;
  border: 1px solid #e8e8e8;
  transition: all 0.15s;
}
.hot-tag:hover { border-color: #e4393c; color: #e4393c; }

/* ── 快捷入口 ── */
.shortcuts-wrap {
  max-width: 1200px; margin: 24px auto 0;
  padding: 0 20px;
}
.shortcuts {
  background: #fff; border-radius: 16px;
  padding: 20px 24px;
  display: grid; grid-template-columns: repeat(6, 1fr);
  box-shadow: 0 2px 16px rgba(0,0,0,0.06);
}
.shortcut-item {
  display: flex; flex-direction: column;
  align-items: center; gap: 8px;
  padding: 14px 8px; border-radius: 12px;
  cursor: pointer; transition: background 0.15s;
}
.shortcut-item:hover { background: #f8f8f8; }
.sc-icon {
  width: 48px; height: 48px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.shortcut-item span { font-size: 12px; color: #555; font-weight: 500; }

/* ── 区块标题通用 ── */
.section-hd {
  text-align: center;
  padding: 40px 20px 20px;
}
.section-hd-title {
  font-size: 24px; font-weight: 800;
  color: #1a1a1a; letter-spacing: 4px;
  margin-bottom: 8px;
}
.section-hd-sub {
  font-size: 14px; color: #999; letter-spacing: 1px;
}

/* ── 商品分类外层 ── */
.category-outer {
  max-width: 1200px; margin: 0 auto;
  padding: 0 20px;
}

/* ── 商品分类 ── */
.category-wrap {
  padding: 0 20px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.06);
}
.section-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 4px 12px;
}
.section-title { font-size: 17px; font-weight: 700; color: #1a1a1a; }
.section-more { font-size: 13px; color: #999; cursor: pointer; transition: color 0.15s; }
.section-more:hover { color: #e4393c; }

.cat-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px 0;
}
.cat-item {
  display: flex; flex-direction: column;
  align-items: center; gap: 10px;
  padding: 20px 8px 16px;
  cursor: pointer;
  transition: transform 0.2s;
  border-radius: 12px;
}
.cat-item:hover { transform: translateY(-4px); background: #fafafa; }
.cat-item:hover .cat-name { color: #e4393c; }
.cat-icon-circle {
  width: 96px; height: 96px;
  display: flex; align-items: center; justify-content: center;
}
.cat-item:hover .cat-icon-circle { background: transparent; }
.cat-img {
  width: 90px; height: 90px;
  object-fit: contain;
  filter: drop-shadow(0 3px 6px rgba(0,0,0,0.12));
  transition: transform 0.3s ease, filter 0.3s ease;
}
.cat-item:hover .cat-img { 
  transform: scale(1.08) translateY(-3px); 
  filter: drop-shadow(0 6px 16px rgba(0,0,0,0.18));
}
.cat-name { font-size: 14px; color: #444; font-weight: 500; text-align: center; }

/* ── 环保宣传区 ── */
.eco-wrap {
  max-width: 1200px; margin: 0 auto;
  padding: 0 20px 48px;
}
.eco-wrap .section-hd { padding: 32px 20px 16px; }
.eco-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-top: 4px;
}
.eco-card {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  aspect-ratio: 1 / 1;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 2px 12px rgba(0,0,0,0.15);
}
.eco-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.2); }
.eco-img {
  width: 100%; height: 100%;
  object-fit: cover;
  opacity: 0.75;
  transition: opacity 0.2s, transform 0.3s;
}
.eco-card:hover .eco-img { opacity: 0.9; transform: scale(1.04); }
.eco-label {
  position: absolute;
  top: 12px; left: 0; right: 0;
  text-align: center;
  color: #fff;
  font-size: 13px; font-weight: 600;
  letter-spacing: 1px;
  text-shadow: 0 1px 6px rgba(0,0,0,0.5);
}

/* ── 底部宣传横幅 ── */
.eco-banner {
  width: 100%;
  border-radius: 0;
  overflow: hidden;
  margin-top: 0;
}
.eco-banner-img {
  position: relative;
  width: 100%;
  height: 320px;
  overflow: hidden;
}
.eco-banner-img img {
  width: 100%; height: 100%;
  object-fit: cover;
  display: block;
}
.eco-banner-overlay {
  position: absolute; inset: 0;
  background: rgba(0, 0, 0, 0.45);
}
.eco-banner-title {
  position: absolute;
  inset: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: clamp(36px, 6vw, 72px);
  font-weight: 900;
  color: #fff;
  letter-spacing: 4px;
  text-shadow: 0 2px 20px rgba(0,0,0,0.5);
}
.eco-banner-footer {
  background: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 48px;
  padding: 24px 20px;
  border-top: 1px solid #f0f0f0;
}
.eco-banner-sub {
  font-size: 15px;
  color: #555;
  letter-spacing: 0.5px;
}

@media (max-width: 768px) {
  .eco-banner-img { height: 200px; }
  .eco-banner-footer { flex-direction: column; gap: 10px; padding: 18px 16px; }
  .eco-banner-sub { font-size: 13px; text-align: center; }
}
@media (max-width: 1024px) {
  .cat-grid { grid-template-columns: repeat(5, 1fr); }
}
@media (max-width: 768px) {
  .hero-carousel { height: 55vw; min-height: 260px; max-height: 420px; }
  .slide { padding: 0 5%; }
  .slide-cta { padding: 10px 18px; font-size: 13px; }
  .shortcuts-wrap { padding: 16px 12px 0; }
  .shortcuts { grid-template-columns: repeat(4, 1fr); padding: 14px 10px; }
  .sc-icon { width: 40px; height: 40px; font-size: 18px; }
  .search-wrap { padding: 16px 12px 0; }
  .section-hd { padding: 28px 12px 16px; }
  .section-hd-title { font-size: 20px; }
  .category-outer { padding: 0 12px; }
  .category-wrap { padding: 0 12px 20px; }
  .cat-grid { grid-template-columns: repeat(4, 1fr); }
  .cat-icon-circle { width: 72px; height: 72px; }
  .cat-img { width: 52px; height: 52px; }
  .eco-grid { grid-template-columns: repeat(3, 1fr); gap: 8px; }
  .eco-wrap { padding: 0 12px 32px; }
}
@media (max-width: 480px) {
  .hero-carousel { height: 52vw; min-height: 200px; }
  .shortcuts { grid-template-columns: repeat(3, 1fr); }
  .cat-grid { grid-template-columns: repeat(4, 1fr); }
  .eco-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
