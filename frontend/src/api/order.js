import request from '../utils/request'

export const createOrder = (data) => {
  return request.post('/orders', data)
}

export const getMyOrders = () => {
  return request.get('/orders/my')
}

export const getOrderById = (id) => {
  return request.get(`/orders/${id}`)
}

export const payOrder = (id) => {
  return request.put(`/orders/${id}/pay`)
}

export const shipOrder = (id) => {
  return request.put(`/orders/${id}/ship`)
}

export const confirmReceipt = (id) => {
  return request.put(`/orders/${id}/confirm`)
}

export const cancelOrder = (id) => {
  return request.put(`/orders/${id}/cancel`)
}
