import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const user = ref(JSON.parse(localStorage.getItem('admin_user') || 'null'))
  const isLoggedIn = computed(() => !!token.value)

  function setAuth(tokenVal, userVal) {
    token.value = tokenVal
    user.value = userVal
    localStorage.setItem('admin_token', tokenVal)
    localStorage.setItem('admin_user', JSON.stringify(userVal))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
  }

  return { token, user, isLoggedIn, setAuth, logout }
})
