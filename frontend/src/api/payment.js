import request from '@/utils/request'

/**
 * 创建支付
 */
export function createPayment(orderId, paymentMethod) {
  return request({
    url: '/payments',
    method: 'post',
    params: { orderId, paymentMethod }
  })
}

/**
 * 处理支付（模拟支付）
 */
export function processPayment(paymentNumber) {
  return request({
    url: `/payments/${paymentNumber}/process`,
    method: 'post'
  })
}

/**
 * 取消支付
 */
export function cancelPayment(paymentNumber) {
  return request({
    url: `/payments/${paymentNumber}/cancel`,
    method: 'post'
  })
}

/**
 * 获取支付信息
 */
export function getPayment(paymentNumber) {
  return request({
    url: `/payments/${paymentNumber}`,
    method: 'get'
  })
}

/**
 * 根据订单ID获取支付信息
 */
export function getPaymentByOrderId(orderId) {
  return request({
    url: `/payments/order/${orderId}`,
    method: 'get'
  })
}
