import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '../stores/admin'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'users', component: () => import('../views/UserManagement.vue') },
      { path: 'products', component: () => import('../views/ProductManagement.vue') },
      { path: 'orders', component: () => import('../views/OrderManagement.vue') },
      { path: 'categories', component: () => import('../views/CategoryManagement.vue') },
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const store = useAdminStore()
  if (!to.meta.public && !store.isLoggedIn) return '/login'
})

export default router
