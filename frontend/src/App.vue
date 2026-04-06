<script setup>
import { useRouter } from 'vue-router'
import NavBar from './components/NavBar.vue'

const router = useRouter()

const handleLogout = () => {
  router.push('/login')
}
</script>

<template>
  <NavBar @logout="handleLogout" />
  <RouterView />
</template>
