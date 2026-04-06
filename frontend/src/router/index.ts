import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import HomeView from '../views/HomeView.vue'
import UserView from '../views/UserView.vue'
import TopView from '../views/TopView.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Top',
    component: TopView,
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView,
    meta: { requiresAuth: false }
  },
  {
    path: '/home',
    name: 'Home',
    component: HomeView,
    meta: { requiresAuth: true }  // 需要登录才能访问
  },
  {
    path: '/user',
    name: 'User',
    component: UserView,
    meta: { requiresAuth: true }  // 需要登录才能访问
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫：未登录时拦截需要认证的页面
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    // 未登录，强制跳回登录页
    next('/login')
  } else if (!to.meta.requiresAuth && token && to.path !== '/home') {
    // 已登录还访问不需要认证的页面（如登录/注册页、Top页），直接跳到主页
    next('/home')
  } else {
    next()
  }
})

export default router
