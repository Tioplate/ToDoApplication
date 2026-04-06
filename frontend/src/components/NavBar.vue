<template>
  <div class="navbar">
    <div class="navbar-left">
      <img src="../assets/vite.svg" class="navbar-logo" alt="logo" />
      <span class="navbar-title">ToDoApp</span>
    </div>
    <div class="navbar-right">
      <template v-if="isLoggedIn">
        <el-tag type="success" round>{{ username }}</el-tag>
        <el-button size="small" @click="$router.push('/user')">アカウント</el-button>
        <el-button type="danger" plain size="small" @click="handleLogout">ログアウト</el-button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { API_BASE_URL } from '../config.js'

const emit = defineEmits(['logout'])
const router = useRouter()
const route = useRoute()

// 使用响应式的 ref 存储 token
const token = ref(localStorage.getItem('token'))

// 监听路由变化，在路由跳转（如登录/登出）时重新获取 token
watch(route, () => {
  token.value = localStorage.getItem('token')
})

const isLoggedIn = computed(() => !!token.value)

const username = computed(() => {
  if (!token.value) return ''
  try {
    const payload = JSON.parse(atob(token.value.split('.')[1]))
    return payload.sub || ''
  } catch {
    return ''
  }
})

const handleLogout = async () => {
  if (token.value) {
    try {
      await fetch(`${API_BASE_URL}/api/users/logout`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token.value}`
        }
      })
    } catch (e) {
      console.error('Logout failed on server', e)
    }
  }

  localStorage.removeItem('token')
  token.value = null // 立即更新本地响应式状态
  ElMessage.success('ログアウトしました')

  emit('logout')
  router.push('/')
}
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  height: 56px;
  background-color: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
  box-sizing: border-box;
  width: 100%;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.navbar-logo {
  height: 28px;
  width: 28px;
}

.navbar-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 0.5px;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
