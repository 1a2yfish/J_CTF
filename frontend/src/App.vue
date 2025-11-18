<template>
  <router-view />
</template>

<script setup>
import { useAuthStore } from './stores/authStore'
import { onMounted } from 'vue'

const authStore = useAuthStore()

onMounted(() => {
  // 从localStorage检查认证状态
  authStore.checkAuth()
})
</script>