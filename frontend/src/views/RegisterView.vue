<template>
  <div class="auth-wrapper">
    <el-card class="auth-card">
      <h2 class="auth-title">新規登録</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @validate="onValidate" status-icon>
        <el-form-item label="ユーザー名" prop="username">
          <el-input v-model="form.username" placeholder="ユーザー名を入力" />
        </el-form-item>
        <el-form-item label="パスワード" prop="password">
          <el-input v-model="form.password" type="password" placeholder="パスワードを入力" show-password />
        </el-form-item>
        <el-form-item label="パスワード(確認用)" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="パスワード(確認用)を入力" show-password />
        </el-form-item>
        <el-button type="primary" class="auth-btn" :loading="loading" :disabled="!isFormValid" @click="handleRegister">
          新規登録
        </el-button>
      </el-form>
      <p class="auth-switch">
        すでにアカウントをお持ちですか？
        <el-link type="primary" @click="$router.push('/login')">ログイン</el-link>
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { API_BASE_URL } from '../config.js'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '', confirmPassword: '' })

const fieldStatus = reactive({
  username: false,
  password: false,
  confirmPassword: false
})

const onValidate = (prop, isValid) => {
  fieldStatus[prop] = isValid
}

const isFormValid = computed(() => fieldStatus.username && fieldStatus.password && fieldStatus.confirmPassword)

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('パスワードが一致しません'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('パスワードは必須です'))
  } else if (value.length < 8) {
    callback(new Error('パスワードは8文字以上である必要があります'))
  } else if (!/[a-zA-Z]/.test(value) || !/[0-9]/.test(value)) {
    callback(new Error('パスワードは英字と数字の両方を含む必要があります'))
  } else {
    callback()
  }
}

const validateUsername = async (rule, value, callback) => {
  if (!value) {
    return callback(new Error('ユーザー名は必須です'))
  }
  if (value.length < 3 || value.length > 20) {
    return callback(new Error('長さは3〜20文字である必要があります'))
  }

  try {
    const res = await fetch(`${API_BASE_URL}/api/users/check?username=${value}`)
    if (res.ok) {
      const exists = await res.json()
      if (exists === true) {
        return callback(new Error('ユーザー名はすでに存在します'))
      }
    }
    callback()
  } catch (e) {
    callback()
  }
}

const rules = {
  username: [
    { required: true, validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'パスワード(確認用)を入力してください', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await fetch(`${API_BASE_URL}/api/users/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: form.username, password: form.password })
      })
      const text = await res.text()
      if (res.ok) {
        ElMessage.success('登録に成功しました。ログインしてください')
        router.push('/login')
      } else {
        ElMessage.error(text || '登録に失敗しました')
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
