import request from '../utils/request'

export const getAddresses = () => {
  return request.get('/addresses')
}

export const createAddress = (data) => {
  return request.post('/addresses', data)
}

export const updateAddress = (id, data) => {
  return request.put(`/addresses/${id}`, data)
}

export const deleteAddress = (id) => {
  return request.delete(`/addresses/${id}`)
}

export const setDefaultAddress = (id) => {
  return request.put(`/addresses/${id}/default`)
}
