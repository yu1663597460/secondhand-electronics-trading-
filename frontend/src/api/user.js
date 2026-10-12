import request from '../utils/request'

export const register = (data) => {
  return request.post('/auth/register', data)
}

export const login = (data) => {
  return request.post('/auth/login', data)
}

export const getProfile = () => {
  return request.get('/users/profile')
}

export const updateProfile = (data) => {
  return request.put('/users/profile', data)
}

export const changePassword = (data) => {
  return request.put('/users/password', data)
}

export const submitVerification = (data) => {
  return request.post('/users/verification', data)
}

export const getUserStats = () => {
  return request.get('/users/stats')
}

export const uploadAvatar = (formData) => {
  return request.post('/users/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
