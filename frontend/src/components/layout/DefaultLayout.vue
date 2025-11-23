<template>
  <div class="app-container">
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <Header />
      
      <!-- 内容区域 -->
      <div class="content-wrapper">
        <!-- 面包屑导航 -->
        <Breadcrumb v-if="showBreadcrumb" />
        
        <!-- 页面内容 -->
        <div class="page-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from './Header.vue'
import Breadcrumb from '../common/Breadcrumb.vue'

const route = useRoute()

const showBreadcrumb = computed(() => {
  // 在登录、注册、仪表盘页面不显示面包屑
  const hideBreadcrumbRoutes = ['/dashboard', '/login', '/register']
  return !hideBreadcrumbRoutes.includes(route.path)
})
</script>

<style scoped>
.app-container {
  display: flex;
  min-height: 100vh;
  position: relative;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #0f1629;
  min-height: 100vh;
  width: 100%;
}

.content-wrapper {
  flex: 1;
  padding: 24px 32px;
  overflow-y: auto;
  min-height: calc(100vh - 70px);
  max-width: 1600px;
  margin: 0 auto;
  width: 100%;
}

.page-content {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-wrapper {
    padding: 16px;
  }
}
</style>