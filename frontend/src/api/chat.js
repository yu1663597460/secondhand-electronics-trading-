import request from '../utils/request'

// 获取聊天会话列表
export const getChatSessions = () => {
  return request.get('/chat/sessions')
}

// 创建聊天会话
export const createChatSession = (data) => {
  return request.post('/chat/sessions', data)
}

// 获取会话消息
export const getSessionMessages = (sessionId, params = {}) => {
  return request.get(`/chat/sessions/${sessionId}/messages`, { params })
}

// 发送消息
export const sendMessage = (sessionId, data) => {
  return request.post(`/chat/sessions/${sessionId}/messages`, data)
}

// 标记消息为已读
export const markMessagesAsRead = (sessionId) => {
  return request.put(`/chat/sessions/${sessionId}/read`)
}

// 获取未读消息数量
export const getUnreadMessageCount = () => {
  return request.get('/chat/unread-count')
}

// 删除消息
export const deleteMessage = (messageId) => {
  return request.delete(`/chat/messages/${messageId}`)
}