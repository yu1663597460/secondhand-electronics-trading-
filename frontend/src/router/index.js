import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/ProductList.vue')
      },
      {
        path: 'products',
        redirect: '/'
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('../views/ProductDetail.vue')
      },
      {
        path: 'my-products',
        name: 'MyProducts',
        component: () => import('../views/MyProducts.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'publish',
        name: 'PublishProduct',
        component: () => import('../views/PublishProduct.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('../views/OrderList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/UserProfile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('../views/ChatList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('../views/FavoriteList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'cart',
        name: 'ShoppingCart',
        component: () => import('../views/ShoppingCart.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'category/:id',
        name: 'CategoryProducts',
        component: () => import('../views/CategoryProducts.vue')
      },
      {
        path: 'search',
        name: 'SearchProducts',
        component: () => import('../views/CategoryProducts.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/UserManagement.vue')
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('../views/admin/ProductManagement.vue')
      },
      {
        path: 'review',
        name: 'AdminReview',
        component: () => import('../views/admin/ProductReview.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/OrderManagement.vue')
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('../views/admin/ProductManagement.vue')
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('../views/admin/Statistics.vue')
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('../views/admin/SystemSettings.vue')
      },
      {
        path: 'logs',
        name: 'AdminLogs',
        component: () => import('../views/admin/OperationLogs.vue')
      },
      {
        path: 'complaints',
        name: 'AdminComplaints',
        component: () => import('../views/admin/ComplaintManagement.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else if (to.meta.requiresAdmin && userStore.role !== 'ADMIN') {
    next('/')
  } else {
    next()
  }
})

export default router
