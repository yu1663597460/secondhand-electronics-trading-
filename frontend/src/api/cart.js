import request from '@/utils/request'

/**
 * 添加商品到购物车
 */
export function addToCart(productId) {
  return request({
    url: '/cart/items',
    method: 'post',
    params: { productId }
  })
}

/**
 * 获取购物车列表
 */
export function getCartItems() {
  return request({
    url: '/cart/items',
    method: 'get'
  })
}

/**
 * 从购物车移除商品
 */
export function removeFromCart(productId) {
  return request({
    url: `/cart/items/${productId}`,
    method: 'delete'
  })
}

/**
 * 清空购物车
 */
export function clearCart() {
  return request({
    url: '/cart/items',
    method: 'delete'
  })
}

/**
 * 获取购物车商品数量
 */
export function getCartItemCount() {
  return request({
    url: '/cart/count',
    method: 'get'
  })
}
