<template>
  <div class="chat-page">

    <!-- 左侧会话列表 -->
    <div class="chat-sidebar">
      <!-- 顶部 -->
      <div class="sidebar-header">
        <div class="sidebar-title">
          <span>消息</span>
          <span v-if="totalUnreadCount > 0" class="total-unread">{{ totalUnreadCount > 99 ? '99+' : totalUnreadCount }}</span>
        </div>
        <div class="search-box">
          <span class="search-icon">🔍</span>
          <input v-model="searchKw" placeholder="搜索联系人或商品" class="search-input" />
        </div>
      </div>

      <!-- 会话列表 -->
      <div class="session-list" v-if="!loading">
        <div v-if="filteredSessions.length === 0" class="list-empty">
          <div class="empty-icon">💬</div>
          <p>暂无聊天记录</p>
          <button class="btn-browse" @click="$router.push('/')">去逛逛</button>
        </div>

        <div
          v-for="session in filteredSessions"
          :key="session.id"
          class="session-item"
          :class="{ active: selectedSession?.id === session.id, unread: session.unreadCount > 0 }"
          @click="openChat(session)"
        >
          <!-- 头像 + 未读红点 -->
          <div class="item-avatar-wrap">
            <div class="item-avatar">
              <img v-if="session.otherUserAvatar" :src="session.otherUserAvatar" />
              <span v-else>{{ session.otherUserName?.charAt(0)?.toUpperCase() }}</span>
            </div>
            <span v-if="session.unreadCount > 0" class="unread-dot">
              {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
            </span>
          </div>

          <!-- 会话信息 -->
          <div class="item-body">
            <div class="item-top">
              <span class="item-name" :class="{ bold: session.unreadCount > 0 }">{{ session.otherUserName }}</span>
              <span class="item-time">{{ formatTime(session.lastMessageTime) }}</span>
            </div>
            <div class="item-preview">
              <span class="item-msg" :class="{ bold: session.unreadCount > 0 }">
                {{ session.lastMessage || '暂无消息' }}
              </span>
            </div>
            <!-- 商品标签 -->
            <div class="item-product" v-if="session.productTitle">
              <img v-if="session.productImage" :src="session.productImage" class="prod-thumb" />
              <span class="prod-name">{{ session.productTitle }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 骨架屏 -->
      <div v-else class="skeleton-list">
        <div v-for="i in 5" :key="i" class="skeleton-item">
          <div class="sk-avatar" />
          <div class="sk-body">
            <div class="sk-line w60" />
            <div class="sk-line w90" />
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧聊天窗口 / 空状态 -->
    <div class="chat-main">
      <ChatWindow
        v-if="showChatWindow && selectedSession"
        :session="selectedSession"
        @close="closeChatWindow"
        @message-sent="handleMessageSent"
      />
      <div v-else class="chat-placeholder">
        <div class="placeholder-icon">💬</div>
        <p>选择一个会话开始聊天</p>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getChatSessions, getUnreadMessageCount } from '../api/chat'
import ChatWindow from '../components/ChatWindow.vue'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'

const route = useRoute()
const sessions = ref([])
const loading = ref(true)
const showChatWindow = ref(false)
const selectedSession = ref(null)
const totalUnreadCount = ref(0)
const searchKw = ref('')

const filteredSessions = computed(() => {
  if (!searchKw.value) return sessions.value
  const kw = searchKw.value.toLowerCase()
  return sessions.value.filter(s =>
    s.otherUserName?.toLowerCase().includes(kw) ||
    s.productTitle?.toLowerCase().includes(kw)
  )
})

const loadChatSessions = async () => {
  try {
    loading.value = true
    const data = await getChatSessions()
    sessions.value = data || []
    totalUnreadCount.value = sessions.value.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
    
    // 检查URL参数中是否有sessionId，如果有则自动打开对应会话
    const sessionIdFromUrl = route.query.sessionId
    if (sessionIdFromUrl) {
      const targetSession = sessions.value.find(s => s.id === Number(sessionIdFromUrl))
      if (targetSession) {
        openChat(targetSession)
      }
    }
  } catch {
    ElMessage.error('加载聊天记录失败')
    sessions.value = []
  } finally {
    loading.value = false
  }
}

const openChat = (session) => {
  selectedSession.value = session
  showChatWindow.value = true
  // 标记已读（本地先清零）
  session.unreadCount = 0
  totalUnreadCount.value = sessions.value.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
}

const closeChatWindow = () => {
  showChatWindow.value = false
  selectedSession.value = null
  // 重新加载会话列表以更新最新消息
  loadChatSessions()
}

const handleMessageSent = () => loadChatSessions()

const formatTime = (time) => {
  if (!time) return ''
  try {
    const date = new Date(time)
    const now = new Date()
    const diff = now - date
    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
    if (diff < 86400000) return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    if (diff < 604800000) return ['周日','周一','周二','周三','周四','周五','周六'][date.getDay()]
    return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
  } catch { return '' }
}

onMounted(loadChatSessions)
</script>

<style scoped>
.chat-page {
  display: flex;
  height: calc(100vh - 112px);
  max-width: 1100px;
  margin: 0 auto;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
}

/* ===== 左侧 ===== */
.chat-sidebar {
  width: 320px;
  flex-shrink: 0;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.sidebar-header {
  padding: 20px 16px 12px;
  border-bottom: 1px solid #f5f5f5;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 12px;
}

.total-unread {
  background: #e4393c;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f5f5f5;
  border-radius: 20px;
  padding: 7px 14px;
}
.search-icon { font-size: 13px; flex-shrink: 0; }
.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  color: #333;
}
.search-input::placeholder { color: #bbb; }

.session-list {
  flex: 1;
  overflow-y: auto;
}
.session-list::-webkit-scrollbar { width: 4px; }
.session-list::-webkit-scrollbar-thumb { background: #e8e8e8; border-radius: 2px; }

.list-empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.list-empty p { font-size: 14px; margin-bottom: 16px; }
.btn-browse {
  padding: 8px 24px;
  background: #07c160;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.15s;
  position: relative;
}
.session-item:hover { background: #f7f7f7; }
.session-item.active { background: #f0faf4; }
.session-item.unread { background: #fafafa; }

/* 头像 */
.item-avatar-wrap { position: relative; flex-shrink: 0; }
.item-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #07c160, #00a854);
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.item-avatar img { width: 100%; height: 100%; object-fit: cover; }
.unread-dot {
  position: absolute;
  top: -3px;
  right: -3px;
  background: #e4393c;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 3px;
  border: 1.5px solid #fff;
}

/* 会话信息 */
.item-body { flex: 1; min-width: 0; }
.item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3px;
}
.item-name { font-size: 15px; color: #1a1a2e; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-name.bold { font-weight: 700; }
.item-time { font-size: 11px; color: #bbb; flex-shrink: 0; margin-left: 8px; }
.item-preview { margin-bottom: 3px; }
.item-msg { font-size: 13px; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: block; }
.item-msg.bold { color: #333; font-weight: 500; }
.item-product {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
}
.prod-thumb { width: 16px; height: 16px; border-radius: 3px; object-fit: cover; flex-shrink: 0; }
.prod-name { font-size: 11px; color: #bbb; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* 骨架屏 */
.skeleton-list { padding: 8px 0; }
.skeleton-item { display: flex; gap: 12px; padding: 12px 16px; }
.sk-avatar { width: 48px; height: 48px; border-radius: 50%; background: #f0f0f0; flex-shrink: 0; animation: shimmer 1.2s infinite; }
.sk-body { flex: 1; display: flex; flex-direction: column; gap: 8px; justify-content: center; }
.sk-line { height: 12px; border-radius: 6px; background: #f0f0f0; animation: shimmer 1.2s infinite; }
.w60 { width: 60%; }
.w90 { width: 90%; }
@keyframes shimmer { 0%,100% { opacity: 1; } 50% { opacity: 0.5; } }

/* ===== 右侧 ===== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f7f7f7;
}

.chat-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #bbb;
  gap: 12px;
}
.placeholder-icon { font-size: 56px; }
.chat-placeholder p { font-size: 14px; }

@media (max-width: 768px) {
  .chat-page { height: calc(100vh - 80px); border-radius: 0; }
  .chat-sidebar { width: 100%; border-right: none; }
  .chat-main { display: none; }
  .chat-sidebar.has-active .chat-main { display: flex; }
}
</style>
