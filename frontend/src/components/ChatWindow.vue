<template>
  <div class="chat-window-container">
    <div class="chat-header">
      <div class="header-info">
        <img :src="session.productImage" class="header-image" />
        <div class="header-text">
          <div class="header-title">{{ session.productTitle }}</div>
          <div class="header-subtitle">与 {{ session.otherUserName }} 的对话</div>
        </div>
      </div>
      <button class="close-btn" @click="handleClose">✕</button>
    </div>
    
    <div class="chat-body">
      <!-- 商品信息栏 -->
      
      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef">
        <div v-if="loadingMessages" class="loading-messages">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else class="messages">
          <div 
            v-for="message in messages" 
            :key="message.id"
            class="message-item"
            :class="{ 'is-mine': message.isMine }"
          >
            <div class="message-avatar">
              <el-avatar 
                :src="message.senderAvatar" 
                :size="32"
              >
                {{ message.senderName?.charAt(0) }}
              </el-avatar>
            </div>
            
            <div class="message-content">
              <div class="message-info">
                <span class="sender-name">{{ message.senderName }}</span>
                <span class="message-time">{{ formatMessageTime(message.createdAt) }}</span>
              </div>
              <div class="message-bubble-wrapper">
                <div class="message-bubble">
                  {{ message.content }}
                </div>
                <button class="delete-btn" @click="handleDeleteMessage(message)" title="删除消息">
                  🗑️
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 输入框 -->
      <div class="message-input">
        <el-input
          v-model="newMessage"
          type="textarea"
          :rows="3"
          placeholder="输入消息..."
          :disabled="sending"
          @keydown.ctrl.enter="sendMessage"
          @keydown.meta.enter="sendMessage"
        />
        <div class="input-actions">
          <div class="input-tip">
            Ctrl + Enter 发送
          </div>
          <el-button 
            type="primary" 
            :loading="sending"
            :disabled="!newMessage.trim()"
            @click="sendMessage"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSessionMessages, sendMessage as sendMessageApi, markMessagesAsRead, deleteMessage as deleteMessageApi } from '../api/chat'
import { format } from 'date-fns'
import { zhCN } from 'date-fns/locale'

const props = defineProps({
  session: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'message-sent'])

const messages = ref([])
const newMessage = ref('')
const sending = ref(false)
const loadingMessages = ref(true)
const messageListRef = ref(null)

const loadMessages = async () => {
  try {
    loadingMessages.value = true
    const data = await getSessionMessages(props.session.id, {
      page: 0,
      size: 50
    })
    messages.value = data.content || []
    
    // 标记消息为已读
    await markMessagesAsRead(props.session.id)
    
    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    console.error('Failed to load messages:', error)
    ElMessage.error('加载消息失败')
  } finally {
    loadingMessages.value = false
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || sending.value) return
  
  try {
    sending.value = true
    const messageContent = newMessage.value.trim()
    
    await sendMessageApi(props.session.id, {
      content: messageContent,
      messageType: 'TEXT'
    })
    
    newMessage.value = ''
    
    // 重新加载消息
    await loadMessages()
    
    // 通知父组件消息已发送
    emit('message-sent')
    
    ElMessage.success('消息发送成功')
  } catch (error) {
    console.error('Failed to send message:', error)
    ElMessage.error('消息发送失败')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const formatMessageTime = (time) => {
  if (!time) return ''
  
  try {
    const date = new Date(time)
    const now = new Date()
    const diffInHours = (now - date) / (1000 * 60 * 60)
    
    if (diffInHours < 24) {
      return format(date, 'HH:mm')
    } else {
      return format(date, 'MM-dd HH:mm')
    }
  } catch (error) {
    return ''
  }
}

const handleClose = () => {
  emit('close')
}

// 删除消息
const handleDeleteMessage = async (message) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条消息吗？删除后将不再显示。',
      '删除消息',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteMessageApi(message.id)
    
    // 从本地列表中移除消息
    messages.value = messages.value.filter(m => m.id !== message.id)
    
    // 通知父组件
    emit('message-sent')
    
    ElMessage.success('消息已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete message:', error)
      ElMessage.error('删除消息失败')
    }
  }
}

// 监听会话变化
watch(() => props.session, (newSession) => {
  if (newSession) {
    loadMessages()
  }
}, { immediate: true })

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.chat-window-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-image {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-title {
  font-weight: 600;
  color: #1a202c;
  font-size: 16px;
}

.header-subtitle {
  font-size: 13px;
  color: #64748b;
}

.close-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: #f0f0f0;
  color: #666;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #e0e0e0;
  color: #333;
}

.chat-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #fafafa;
}

.loading-messages {
  padding: 20px;
}

.messages {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.message-item.is-mine {
  flex-direction: row-reverse;
}

.message-item.is-mine .message-content {
  align-items: flex-end;
}

.message-item.is-mine .message-bubble {
  background: #07c160;
  color: white;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 70%;
}

.message-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 12px;
  color: #64748b;
}

.sender-name {
  font-weight: 500;
}

.message-time {
  opacity: 0.8;
}

.message-bubble-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-bubble {
  background: white;
  padding: 12px 16px;
  border-radius: 18px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  word-wrap: break-word;
  line-height: 1.4;
}

.delete-btn {
  opacity: 0;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}

.message-item:hover .delete-btn {
  opacity: 0.6;
}

.delete-btn:hover {
  opacity: 1 !important;
  background: #f5f5f5;
}

.message-input {
  padding: 16px 20px;
  background: white;
  border-top: 1px solid #e2e8f0;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.input-tip {
  font-size: 12px;
  color: #94a3b8;
}

/* 滚动条样式 */
.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-content {
    max-width: 85%;
  }
  
  .message-list {
    padding: 12px 16px;
  }
  
  .message-input {
    padding: 12px 16px;
  }
}
</style>