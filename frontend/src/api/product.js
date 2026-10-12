import request from '../utils/request'

export const getProducts = (params) => {
  return request.get('/products', { params })
}

export const getProductById = (id) => {
  return request.get(`/products/${id}`)
}

export const getMyProducts = () => {
  return request.get('/products/my')
}

export const createProduct = (data) => {
  return request.post('/products', data)
}

export const updateProduct = (id, data) => {
  return request.put(`/products/${id}`, data)
}

export const updateProductStatus = (id, status) => {
  return request.put(`/products/${id}/status`, { status })
}

export const deleteProduct = (id) => {
  return request.delete(`/products/${id}`)
}

export const getCategories = () => {
  return request.get('/categories')
}
