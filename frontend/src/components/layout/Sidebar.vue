<template>
  <div class="sidebar">
    <div class="sidebar-logo">
      <t-icon name="shield" style="margin-right: 10px; font-size: 1.5rem;" />
      <span>CTF竞赛平台</span>
    </div>

    <div class="sidebar-menu">
      <div
          class="menu-item"
          :class="{ active: currentRoute === 'dashboard' }"
          @click="navigate('dashboard')"
      >
        <t-icon name="dashboard" />
        <span>仪表盘</span>
      </div>

      <div
          class="menu-item"
          :class="{ active: currentRoute === 'competitions' }"
          @click="navigate('competitions')"
      >
        <t-icon name="flag" />
        <span>竞赛列表</span>
      </div>

      <div
          class="menu-item"
          :class="{ active: currentRoute === 'problems' }"
          @click="navigate('problems')"
      >
        <t-icon name="app" />
        <span>题目管理</span>
      </div>

      <div
          class="menu-item"
          :class="{ active: currentRoute === 'teams' }"
          @click="navigate('teams')"
      >
        <t-icon name="team" />
        <span>团队管理</span>
      </div>

      <div class="menu-divider"></div>

      <div
          class="menu-item"
          :class="{ active: currentRoute === 'profile' }"
          @click="navigate('profile')"
      >
        <t-icon name="user" />
        <span>个人中心</span>
      </div>

      <div
          class="menu-item"
          @click="logout"
      >
        <t-icon name="power" />
        <span>退出登录</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const currentRoute = computed(() => {
  return router.currentRoute.value.name || 'dashboard'
})

const navigate = (route) => {
  router.push({ path: `/${route}` })
}

const logout = () => {
  authStore.logout()
  router.push('/login')
}
</script>