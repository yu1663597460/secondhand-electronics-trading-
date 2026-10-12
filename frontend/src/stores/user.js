import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, getProfile } from '../api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '',
    role: localStorage.getItem('role') || '',
    avatar: localStorage.getItem('avatar') || ''
  }),
  
  actions: {
    async login(credentials) {
      const data = await loginApi(credentials)
      
      this.token = data.token
      this.username = data.username
      this.role = data.role
      
      localStorage.setItem('token', data.token)
      localStorage.setItem('username', data.username)
      localStorage.setItem('role', data.role)

      // 登录后拉取完整用户信息，同步头像
      try {
        const profile = await getProfile()
        if (profile.avatar) {
          this.avatar = profile.avatar
          localStorage.setItem('avatar', profile.avatar)
        }
      } catch { /* 不影响登录流程 */ }

      return data
    },
    
    async register(userData) {
      const data = await registerApi(userData)
      this.token = data.token
      this.username = data.username
      this.role = data.role
      
      localStorage.setItem('token', data.token)
      localStorage.setItem('username', data.username)
      localStorage.setItem('role', data.role)
    },

    setAvatar(url) {
      this.avatar = url
      localStorage.setItem('avatar', url)
    },
    
    logout() {
      this.token = ''
      this.username = ''
      this.role = ''
      this.avatar = ''
      
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('avatar')
    }
  },
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.role === 'ADMIN'
  }
})
