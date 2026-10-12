import request from '../utils/request'

// 用户管理相关API
export const getUsers = (params) => {
  return request.get('/admin/users', { params })
}

export const getUserDetail = (id) => {
  return request.get(`/admin/users/${id}`)
}

export const updateUserStatus = (id, data) => {
  return request.put(`/admin/users/${id}/status`, data)
}

export const searchUsers = (keyword) => {
  return request.get('/admin/users/search', { params: { keyword } })
}

export const getUserStatistics = () => {
  return request.get('/admin/users/statistics')
}

export const reviewVerification = (id, data) => {
  return request.put(`/admin/users/${id}/verification`, data)
}

export const getPendingVerificationUsers = (params) => {
  return request.get('/admin/users/pending-verification', { params })
}

// 商品管理相关API
export const getProducts = (params) => {
  return request.get('/admin/products', { params })
}

export const approveProduct = (id, data) => {
  return request.put(`/admin/products/${id}/approve`, data)
}

export const rejectProduct = (id, data) => {
  return request.put(`/admin/products/${id}/reject`, data)
}

export const removeProduct = (id, data) => {
  return request.put(`/admin/products/${id}/remove`, data)
}

export const getPendingProductCount = () => {
  return request.get('/admin/products/pending-count')
}

// 订单管理相关API
export const getOrders = (params) => {
  return request.get('/admin/orders', { params })
}

export const getOrderDetail = (id) => {
  return request.get(`/admin/orders/${id}`)
}

export const addOrderNote = (id, data) => {
  return request.put(`/admin/orders/${id}/note`, data)
}

export const getOrderComplaint = (orderId) =>
  request.get(`/admin/orders/${orderId}/complaint`)

export const handleOrderComplaint = (orderId, complaintId, data) =>
  request.put(`/admin/orders/${orderId}/complaint/${complaintId}/handle`, data)

export const getOrderReview = (orderId) =>
  request.get(`/admin/orders/${orderId}/review`)

export const deleteOrderReview = (orderId, reviewId) =>
  request.delete(`/admin/orders/${orderId}/review/${reviewId}`)

// 分类管理相关API
export const getCategories = () => {
  return request.get('/admin/categories')
}

export const createCategory = (data) => {
  return request.post('/admin/categories', data)
}

export const updateCategory = (id, data) => {
  return request.put(`/admin/categories/${id}`, data)
}

export const deleteCategory = (id) => {
  return request.delete(`/admin/categories/${id}`)
}

export const getCategoryProducts = (categoryId, params) => {
  return request.get(`/admin/categories/${categoryId}/products`, { params })
}

// 系统设置相关API
export const getSystemSettings = () => {
  return request.get('/admin/settings')
}

export const updateSystemSettings = (data) => {
  return request.put('/admin/settings', data)
}