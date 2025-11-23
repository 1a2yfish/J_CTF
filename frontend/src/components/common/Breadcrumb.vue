<template>
  <div class="breadcrumb-container">
    <t-breadcrumb>
      <t-breadcrumb-item 
        v-for="(item, index) in breadcrumbItems" 
        :key="index"
        :to="item.path"
        :disabled="index === breadcrumbItems.length - 1"
      >
        <t-icon v-if="item.icon" :name="item.icon" style="margin-right: 4px;" />
        {{ item.label }}
      </t-breadcrumb-item>
    </t-breadcrumb>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const breadcrumbItems = computed(() => {
  const items = []
  const path = route.path
  const matched = route.matched

  // 首页
  if (path === '/dashboard' || path === '/') {
    return [{ label: '仪表盘', path: '/dashboard', icon: 'dashboard' }]
  }

  // 根据路径生成面包屑
  if (path.startsWith('/competitions')) {
    items.push({ label: '竞赛列表', path: '/competitions', icon: 'flag' })
    if (route.params.id) {
      items.push({ label: '竞赛详情', path: '', icon: 'file' })
    }
  } else if (path.startsWith('/teams')) {
    items.push({ label: '团队管理', path: '/teams', icon: 'team' })
  } else if (path.startsWith('/profile')) {
    items.push({ label: '个人中心', path: '/profile', icon: 'user' })
  } else if (path.startsWith('/admin')) {
    items.push({ label: '管理后台', path: '/admin', icon: 'setting' })
  }

  return items
})
</script>

<style scoped>
.breadcrumb-container {
  margin-bottom: 20px;
  padding: 12px 0;
}

.breadcrumb-container :deep(.t-breadcrumb) {
  color: #9ca3af;
}

.breadcrumb-container :deep(.t-breadcrumb-item) {
  color: #9ca3af;
  font-size: 14px;
}

.breadcrumb-container :deep(.t-breadcrumb-item:hover) {
  color: #60a5fa;
}

.breadcrumb-container :deep(.t-breadcrumb-item--disabled) {
  color: #e0e6ed;
  cursor: default;
}
</style>
