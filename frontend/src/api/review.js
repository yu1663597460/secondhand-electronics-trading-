import request from '../utils/request'

export const submitReview = (data) => request.post('/reviews', data)
export const getProductReviews = (productId) => request.get(`/reviews/product/${productId}`)
export const getSellerReviews = (sellerId) => request.get(`/reviews/seller/${sellerId}`)
export const hasReviewed = (orderId) => request.get(`/reviews/order/${orderId}/exists`)
export const deleteReview = (id) => request.delete(`/reviews/${id}`)

export const submitComplaint = (data) => request.post('/complaints', data)
export const getComplaints = (params) => request.get('/complaints', { params })
export const handleComplaint = (id, data) => request.put(`/complaints/${id}/handle`, data)
