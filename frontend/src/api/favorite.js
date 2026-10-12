import request from '@/utils/request'

/**
 * 收藏相关API
 */

// 添加收藏
export function addFavorite(productId) {
  return request({
    url: '/favorites',
    method: 'post',
    data: { productId }
  })
}

// 取消收藏
export function removeFavorite(productId) {
  return request({
    url: `/favorites/${productId}`,
    method: 'delete'
  })
}

// 获取收藏列表
export function getFavorites(page = 0, size = 20) {
  return request({
    url: '/favorites',
    method: 'get',
    params: { page, size }
  })
}

// 检查是否已收藏
export function checkFavorite(productId) {
  return request({
    url: `/favorites/check/${productId}`,
    method: 'get'
  })
}

// 获取收藏数量
export function getFavoriteCount() {
  return request({
    url: '/favorites/count',
    method: 'get'
  })
}
