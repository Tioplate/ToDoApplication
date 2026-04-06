<template>
  <div class="auth-wrapper">
    <el-card class="auth-card">
      <h2 class="auth-title">ログイン</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="ユーザー名" prop="username">
          <el-input v-model="form.username" placeholder="ユーザー名を入力" />
        </el-form-item>
        <el-form-item label="パスワード" prop="password">
          <el-input v-model="form.password" type="password" placeholder="パスワードを入力" show-password />
        </el-form-item>
        <el-button type="primary" class="auth-btn" :loading="loading" @click="handleLogin">
          ログイン
        </el-button>
      </el-form>
      <p class="auth-switch">
        アカウントをお持ちでないですか？
        <el-link type="primary" @click="$router.push('/register')">新規登録</el-link>
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { API_BASE_URL } from '../config.js'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: 'ユーザー名は必須です', trigger: 'blur' }],
  password: [{ required: true, message: 'パスワードは必須です', trigger: 'blur' }]
}

const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await fetch(`${API_BASE_URL}/api/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      })
      if (res.ok) {
        const data = await res.json()
        localStorage.setItem('token', data.token)
        ElMessage.success('ログインしました')
        router.push('/home')
      } else {
        const errorText = await res.text()
        ElMessage.error(errorText || 'ログインに失敗しました')
      }
    } catch {
      ElMessage.error('ネットワークエラー、もう一度お試しください')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.auth-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 56px);
  background-color: #f5f5f5;
}
.auth-card {
  width: 380px;
  border-radius: 12px;
  padding: 12px;
}
.auth-title {
  text-align: center;
  margin-bottom: 24px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}
.auth-btn {
  width: 100%;
  margin-top: 8px;
}
.auth-switch {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #909399;
}
</style>
