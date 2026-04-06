<template>
  <div class="top-wrapper">
    <el-card class="top-card">
      <div class="hero">
        <h1 class="title">My ToDo へようこそ</h1>
        <p class="subtitle">シンプルで使いやすいタスク管理アプリ</p>
      </div>

      <div class="features">
        <div class="feature-item">
          <span class="icon">📅</span>
          <h3>期限管理</h3>
          <p>締め切りを設定して、タスクのやり忘れを防ぎます。</p>
        </div>
        <div class="feature-item">
          <span class="icon">🚀</span>
          <h3>優先度設定</h3>
          <p>高・中・低の優先度で、重要なタスクから効率よく消化。</p>
        </div>
        <div class="feature-item">
          <span class="icon">✅</span>
          <h3>進捗トラッキング</h3>
          <p>完了したタスクをひと目で確認し、達成感をアップ。</p>
        </div>
      </div>

      <div class="actions">
        <el-button type="primary" size="large" @click="goToLogin">
          ログイン
        </el-button>
        <el-button size="large" @click="goToRegister">
          新規登録
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.top-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.top-card {
  max-width: 800px;
  width: 100%;
  padding: 40px 20px;
  text-align: center;
  border-radius: 12px;
}

.hero {
  margin-bottom: 40px;
}

.title {
  font-size: 36px;
  color: #303133;
  margin-bottom: 12px;
}

.subtitle {
  font-size: 18px;
  color: #606266;
}

.features {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 50px;
  flex-wrap: wrap;
}

.feature-item {
  flex: 1;
  min-width: 200px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.feature-item .icon {
  font-size: 32px;
  margin-bottom: 12px;
  display: inline-block;
}

.feature-item h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 8px;
}

.feature-item p {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.actions .el-button {
  width: 150px;
  font-size: 16px;
}
</style>

