<template>
  <div class="home-wrapper">
    <!-- 顶部操作栏 -->
    <div class="toolbar">
      <span class="page-title">マイ ToDo リスト</span>
      <div class="toolbar-right">
        <span class="sort-label">並び替え:</span>
        <el-select v-model="orderType" size="small" style="width:130px" @change="fetchTodos">
          <el-option label="作成日時" value="createdAt" />
          <el-option label="期限" value="dueDate" />
          <el-option label="優先度" value="priority" />
        </el-select>
      </div>
    </div>

    <!-- 新增 ToDo 表单 -->
    <el-card class="add-card">
      <el-form :model="newTodo" ref="addFormRef" :rules="addRules" inline class="add-form">
        <el-form-item prop="title">
          <el-input v-model="newTodo.title" placeholder="ToDoのタイトル" style="width:220px" />
        </el-form-item>
        <el-form-item prop="dueDate">
          <el-date-picker v-model="newTodo.dueDate" type="date" placeholder="期限"
            value-format="YYYY-MM-DD" style="width:150px" />
        </el-form-item>
        <el-form-item prop="priority">
          <el-select v-model="newTodo.priority" placeholder="優先度" style="width:110px">
            <el-option label="🔴 高" :value="2" />
            <el-option label="🟡 中" :value="1" />
            <el-option label="🟢 低" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAdd" :loading="adding">追加</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- ToDo 列表 -->
    <el-card class="list-card" v-loading="loading">
      <el-empty v-if="todos.length === 0" description="ToDoがありません。上から追加してください！" />
      <transition-group name="list" tag="div">
        <div v-for="todo in todos" :key="todo.id"
          :class="['todo-item', { completed: todo.isCompleted }]">

          <!-- 完成复选框 -->
          <el-checkbox :model-value="todo.isCompleted"
            @change="(val) => handleToggleComplete(todo, val)" />

          <!-- 展示模式 -->
          <template v-if="editingId !== todo.id">
            <div class="todo-content">
              <div class="todo-header">
                <span class="todo-title">{{ todo.title }}</span>
                <div class="todo-actions">
                  <el-button size="small" @click="startEdit(todo)">編集</el-button>
                  <el-button size="small" type="danger" @click="handleDelete(todo.id)">削除</el-button>
                </div>
              </div>
              <div class="todo-meta">
                <el-tag size="small" :type="priorityType(todo.priority)" effect="light">
                  {{ priorityLabel(todo.priority) }}
                </el-tag>
                <span class="todo-date" v-if="todo.dueDate">📅 {{ todo.dueDate }}</span>
              </div>
            </div>
          </template>

          <!-- ���辑模式 -->
          <template v-else>
            <div class="edit-form">
              <el-input v-model="editForm.title" size="small" style="width:180px" />
              <el-date-picker v-model="editForm.dueDate" type="date" size="small"
                value-format="YYYY-MM-DD" style="width:140px" />
              <el-select v-model="editForm.priority" size="small" style="width:100px">
                <el-option label="🔴 高" :value="2" />
                <el-option label="🟡 中" :value="1" />
                <el-option label="🟢 低" :value="0" />
              </el-select>
              <el-button size="small" type="primary" @click="handleUpdate(todo.id)">保存</el-button>
              <el-button size="small" @click="editingId = null">キャンセル</el-button>
            </div>
          </template>
        </div>
      </transition-group>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { API_BASE_URL } from '../config.js'

const router = useRouter()

const loading = ref(false)
const adding = ref(false)
const todos = ref([])
const orderType = ref('createdAt')
const editingId = ref(null)
const addFormRef = ref(null)

const newTodo = reactive({ title: '', dueDate: '', priority: 1 })
const editForm = reactive({ title: '', dueDate: '', priority: 1 })

const addRules = {
  title: [{ required: true, message: 'タイトルは必須です', trigger: 'blur' }],
  dueDate: [{ required: true, message: '期限は必須です', trigger: 'change' }],
  priority: [{ required: true, message: '優先度は必須です', trigger: 'change' }]
}

const getToken = () => localStorage.getItem('token')

const authHeaders = () => ({
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${getToken()}`
})

// 拉取 ToDo 列表
const fetchTodos = async () => {
  loading.value = true
  try {
    const res = await fetch(`${API_BASE_URL}/api/todos?orderType=${orderType.value}`, {
      headers: authHeaders()
    })

    if (res.status === 401) {
      ElMessage.warning('ログインの有効期限が切れました。再度ログインしてください。')
      localStorage.removeItem('token')
      router.push('/login')
      return
    }

    if (!res.ok) throw new Error('Failed to fetch todos')
    todos.value = await res.json()
  } catch {
    ElMessage.error('ネットワークエラーが発生しました')
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  addFormRef.value.validate(async (valid) => {
    if (!valid) return
    adding.value = true
    try {
      const res = await fetch(`${API_BASE_URL}/api/todos`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify(newTodo)
      })
      if (res.ok) {
        ElMessage.success('ToDoを追加しました')
        newTodo.title = ''
        newTodo.dueDate = ''
        newTodo.priority = 1
        addFormRef.value.resetFields()
        await fetchTodos()
      } else {
        ElMessage.error('ToDoの追加に失敗しました')
      }
    } catch {
      ElMessage.error('ネットワークエラーが発生しました')
    } finally {
      adding.value = false
    }
  })
}

// 开始编辑
const startEdit = (todo) => {
  editingId.value = todo.id
  editForm.title = todo.title
  editForm.dueDate = todo.dueDate
  editForm.priority = todo.priority
}

// 保存编辑
const handleUpdate = async (id) => {
  if (!editForm.title.trim()) {
    ElMessage.warning('タイトルは必須です')
    return
  }
  if (!editForm.dueDate) {
    ElMessage.warning('期限は必須です')
    return
  }
  try {
    const res = await fetch(`${API_BASE_URL}/api/todos/${id}`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify({
        title: editForm.title,
        dueDate: editForm.dueDate,
        priority: editForm.priority
      })
    })

    if (res.status === 401) {
      ElMessage.warning('ログインの有効期限が切れました。再度ログインしてください。')
      localStorage.removeItem('token')
      router.push('/login')
      return
    }

    if (res.ok) {
      ElMessage.success('ToDoを更新しました')
      editingId.value = null
      await fetchTodos()
    } else {
      ElMessage.error('ToDoの更新に失敗しました')
    }
  } catch {
    ElMessage.error('ネットワークエラーが発生しました')
  }
}

// 切换完成状态
const handleToggleComplete = async (todo, val) => {
  try {
    const res = await fetch(`${API_BASE_URL}/api/todos/${todo.id}`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify({
        title: todo.title,
        dueDate: todo.dueDate,
        priority: todo.priority,
        isCompleted: val
      })
    })

    if (res.status === 401) {
      ElMessage.warning('ログインの有効期限が切れました。再度ログインしてください。')
      localStorage.removeItem('token')
      router.push('/login')
      return
    }

    if (res.ok) {
      await fetchTodos()
    } else {
      ElMessage.error('ステータスの更新に失敗しました')
    }
  } catch {
    ElMessage.error('ネットワークエラーが発生しま���た')
  }
}

// 删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('このToDoを削除してもよろしいですか？', '確認', {
      confirmButtonText: '削除',
      cancelButtonText: 'キャンセル',
      type: 'warning'
    })
    const res = await fetch(`${API_BASE_URL}/api/todos/${id}`, {
      method: 'DELETE',
      headers: authHeaders()
    })

    if (res.status === 401) {
      ElMessage.warning('ログインの有効期限が切れました。再度ログインしてください。')
      localStorage.removeItem('token')
      router.push('/login')
      return
    }

    if (res.ok) {
      ElMessage.success('ToDoを削除しました')
      await fetchTodos()
    } else {
      ElMessage.error('ToDoの削除に失敗しました')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('ネットワークエラーが発生しました')
  }
}

const priorityLabel = (p) => ['低', '中', '高'][p] ?? '中'
const priorityType = (p) => ['success', 'warning', 'danger'][p] ?? 'warning'

onMounted(fetchTodos)
</script>

<style scoped>
.home-wrapper {
  max-width: 820px;
  margin: 32px auto;
  padding: 0 16px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.sort-label {
  font-size: 14px;
  color: #606266;
}
.add-card {
  margin-bottom: 16px;
}
.add-form {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: flex-start;
}
.add-form .el-form-item {
  margin-bottom: 0;
}
.list-card {
  min-height: 200px;
}
.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}
.todo-item:last-child {
  border-bottom: none;
}
.todo-item.completed .todo-title {
  text-decoration: line-through;
  color: #aaa;
}
.todo-item.completed {
  background-color: #fafafa;
  opacity: 0.7;
}
.todo-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.todo-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.todo-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
  word-break: break-word;
  transition: color 0.3s;
}
.todo-item:hover:not(.completed) .todo-title {
  color: #409eff;
}
.todo-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}
.todo-date {
  font-size: 12px;
  color: #909399;
}
.todo-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
  margin-left: 12px;
}
.edit-form {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  flex-wrap: wrap;
}
/* 列表动画 */
.list-enter-active, .list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from, .list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
