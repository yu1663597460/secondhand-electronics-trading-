<template>
  <div class="shopping-cart-container">
    <div class="cart-header">
      <h1>
        <el-icon><ShoppingCart /></el-icon>
        我的购物车
      </h1>
      <el-button 
        v-if="cartItems.length > 0" 
        type="danger" 
        plain 
        @click="handleClearCart"
      >
        清空购物车
      </el-button>
    </div>

    <!-- 空购物车提示 -->
    <el-empty 
      v-if="cartItems.length === 0 && !loading" 
      description="购物车是空的"
      :image-size="200"
    >
      <el-button type="primary" @click="$router.push('/products')">
        去逛逛
      </el-button>
    </el-empty>

    <!-- 购物车列表 -->
    <div v-else class="cart-content">
      <el-card 
        v-for="item in cartItems" 
        :key="item.id" 
        class="cart-item-card"
        shadow="hover"
      >
        <div class="cart-item">
          <!-- 商品图片 -->
          <div class="item-image">
            <el-image
              :src="item.product.images && item.product.images.length > 0 
                ? item.product.images[0] 
                : '/placeholder.png'"
              fit="cover"
              @click="goToProduct(item.product.id)"
              style="cursor: pointer;"
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>

          <!-- 商品信息 -->
          <div class="item-info">
            <h3 @click="goToProduct(item.product.id)" class="item-title">
              {{ item.product.title }}
            </h3>
            <p class="item-description">{{ item.product.description }}</p>
            <div class="item-meta">
              <el-tag v-if="item.product.categoryName" size="small">
                {{ item.product.categoryName }}
              </el-tag>
              <el-tag v-if="item.product.condition" type="info" size="small">
                {{ item.product.condition }}
              </el-tag>
              <span class="seller-info">
                卖家: {{ item.product.seller?.username }}
              </span>
            </div>
          </div>

          <!-- 价格和操作 -->
          <div class="item-actions">
            <div class="item-price">
              ¥{{ item.product.price }}
            </div>
            <el-button 
              type="danger" 
              plain 
              size="small"
              @click="handleRemove(item.productId)"
            >
              移除
            </el-button>
            <el-button 
              type="primary" 
              size="small"
              @click="handleBuyNow(item.product.id)"
            >
              立即购买
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 底部操作栏 -->
      <div class="cart-footer">
        <div class="total-info">
          <span class="total-label">共 {{ cartItems.length }} 件商品</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Picture } from '@element-plus/icons-vue'
import { getCartItems, removeFromCart, clearCart } from '@/api/cart'

const router = useRouter()
const cartItems = ref([])
const loading = ref(false)

// 加载购物车
const loadCart = async () => {
  loading.value = true
  try {
    const response = await getCartItems()
    cartItems.value = response || []
  } catch (error) {
    console.error('加载购物车失败:', error)
    ElMessage.error('加载购物车失败')
  } finally {
    loading.value = false
  }
}

// 移除商品
const handleRemove = async (productId) => {
  try {
    await ElMessageBox.confirm('确定要从购物车移除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await removeFromCart(productId)
    ElMessage.success('已移除')
    loadCart()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除失败:', error)
      ElMessage.error('移除失败')
    }
  }
}

// 清空购物车
const handleClearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await clearCart()
    ElMessage.success('购物车已清空')
    cartItems.value = []
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空失败:', error)
      ElMessage.error('清空失败')
    }
  }
}

// 立即购买
const handleBuyNow = (productId) => {
  router.push(`/product/${productId}`)
}

// 跳转到商品详情
const goToProduct = (productId) => {
  router.push(`/product/${productId}`)
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.shopping-cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
}

.cart-header h1 {
  font-size: 28px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
}

.cart-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.cart-item-card {
  transition: all 0.3s ease;
}

.cart-item-card:hover {
  transform: translateY(-2px);
}

.cart-item {
  display: flex;
  gap: 20px;
  align-items: center;
}

.item-image {
  flex-shrink: 0;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
}

.item-image .el-image {
  width: 100%;
  height: 100%;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 30px;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 18px;
  color: #333;
  margin: 0 0 10px 0;
  cursor: pointer;
  transition: color 0.3s;
}

.item-title:hover {
  color: #409eff;
}

.item-description {
  font-size: 14px;
  color: #666;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.seller-info {
  font-size: 13px;
  color: #999;
}

.item-actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.item-price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.cart-footer {
  margin-top: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.total-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-label {
  font-size: 18px;
  color: white;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cart-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .item-image {
    width: 100%;
    height: 200px;
  }

  .item-actions {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }

  .cart-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
}
</style>
