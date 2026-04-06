<template>
  <div class="user-wrapper">
    <el-card class="user-card">
      <h2 class="user-title">アカウント設定</h2>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @validate="onValidate" status-icon>
        <el-form-item label="ユーザー名" prop="username">
          <el-input v-model="form.username" placeholder="新しいユーザー名を入力" />
        </el-form-item>
        <el-divider />
        <el-form-item label="新しいパスワード" prop="password">
          <el-input v-model="form.password" type="password" placeholder="変更しない場合は空白" show-password />
        </el-form-item>
        <el-form-item label="新しいパスワード(確認用)" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="新しいパスワード(確認用)" show-password />
        </el-form-item>
        <div class="btn-row">
          <el-button @click="$router.push('/home')">戻る</el-button>
          <el-button type="primary" :loading="loading" :disabled="!isFormValid" @click="handleUpdate">変更を保存</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { API_BASE_URL } from '../config.js'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const fieldStatus = reactive({
  username: true,
  password: true,
  confirmPassword: true
})

const onValidate = (prop, isValid) => {
  fieldStatus[prop] = isValid
}

const currentUsername = ref(null)

const isModified = computed(() => {
  return form.username !== currentUsername.value || !!form.password
})

const isFormValid = computed(() => fieldStatus.username && fieldStatus.password && fieldStatus.confirmPassword && isModified.value)

const validatePassword = (rule, value, callback) => {
  // 密码为空时表示不修改，跳过验证
  if (!value) return callback()
  if (value.length < 8) {
    callback(new Error('パスワードは8文字以上である必要があります'))
  } else if (!/[a-zA-Z]/.test(value) || !/[0-9]/.test(value)) {
    callback(new Error('パスワードは英字と数字の両方を含む必要があります'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (form.password && value !== form.password) {
    callback(new Error('パスワードが一致しません'))
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
  if (value === currentUsername.value) {
    return callback()
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
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const getToken = () => localStorage.getItem('token')
const authHeaders = () => ({
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${getToken()}`
})

// 从 token 解析用户名，初始化表单
const loadUserInfo = () => {
  const token = getToken()
  if (!token) return
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    currentUsername.value = payload.sub
    form.username = currentUsername.value
  } catch {
    ElMessage.error('トークンからユーザー情報の取得に失敗しました')
  }
}

const handleUpdate = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const body = { username: form.username }
      // 只有填了新密码才传递
      if (form.password) body.password = form.password

      // 调用后端新增的按旧用户名更新接口，不使用 userId
      const res = await fetch(`${API_BASE_URL}/api/users/updateByUsername/${currentUsername.value}`, {
        method: 'PUT',
        headers: authHeaders(),
        body: JSON.stringify(body)
      })
      if (res.ok) {
        ElMessage.success('プロフィールを更新しました。再度ログインしてください')

        // 调用后端 logout 清除 Redis 中的 token
        try {
          await fetch(`${API_BASE_URL}/api/users/logout`, {
            method: 'POST',
            headers: authHeaders()
          })
        } catch (e) {
          console.error('Logout failed on server', e)
        }

        // 清除本地 token 并返回 Top 页
        localStorage.removeItem('token')
        router.push('/')
      } else {
        const text = await res.text()
        ElMessage.error(text || 'プロフィールの更新に失敗しました')
      }
    } catch {
      ElMessage.error('ネットワークエラーが発生しました')
    } finally {
      loading.value = false
    }
  })
}

onMounted(loadUserInfo)
</script>

<style scoped>
.user-wrapper {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: calc(100vh - 56px);
  background-color: #f5f5f5;
  padding-top: 60px;
}
.user-card {
  width: 420px;
  border-radius: 12px;
  padding: 12px;
}
.user-title {
  text-align: center;
  margin-bottom: 24px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}
.btn-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}
</style>
