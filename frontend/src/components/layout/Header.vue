<template>
  <div class="header">
    <button class="mobile-menu-btn" @click="toggleSidebar">
      <t-icon name="menu" />
    </button>
    <div class="header-left">
      <div class="header-title">{{ getPageTitle }}</div>
    </div>
    <div class="header-right">
      <t-badge :count="5" dot>
        <t-button variant="text" shape="circle">
          <t-icon name="notification" size="20" />
        </t-button>
      </t-badge>
      <div class="user-info" @click="showProfileMenu = !showProfileMenu">
        <div class="user-avatar">{{ userInitials }}</div>
        <span>{{ currentUser.name || '用户' }}</span>
        <t-icon name="chevron-down" size="16" style="margin-left: 5px;" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const route = useRoute()
const authStore = useAuthStore()
const showProfileMenu = ref(false)

const currentUser = computed(() => {
  return authStore.user
})

const userInitials = computed(() => {
  if (!currentUser.value?.name) return 'U'
  return currentUser.value.name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2)
})

const getPageTitle = computed(() => {
  const titles = {
    dashboard: '仪表盘',
    competitions: '竞赛列表',
    'competition-detail': '竞赛详情',
    problems: '题目管理',
    teams: '团队管理',
    profile: '个人中心'
  }
  return titles[route.name] || 'CTF竞赛平台'
})

const toggleSidebar = () => {
  // 实际项目中会触发一个事件或修改store中的状态
  console.log('Toggle sidebar')
}
</script>