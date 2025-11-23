<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/apiService'
import { useAuthStore } from '@/stores/authStore'
import { MessagePlugin } from 'tdesign-vue-next'

const auth = useAuthStore()
const profile = ref(null)
const error = ref(null)
const loading = ref(false)

async function loadProfile() {
    loading.value = true
    error.value = null
    try {
        const res = await api.get('/users/profile')
        // 后端返回 { success, message, data }
        profile.value = res.data.data
    } catch (e) {
        error.value = e.response?.data?.message || '获取用户信息失败'
    } finally {
        loading.value = false
    }
}

async function saveProfile() {
    loading.value = true
    error.value = null
    try {
        // 发送 PUT /users/profile
        const res = await api.put('/users/profile', profile.value)
        profile.value = res.data.data
        MessagePlugin.success({
            content: '个人信息更新成功',
            duration: 3000,
            icon: true
        })
    } catch (e) {
        error.value = e.response?.data?.message || '更新失败'
        MessagePlugin.error({
            content: error.value,
            duration: 3000
        })
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    loadProfile()
})
</script>

<template>
  <div class="profile-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">个人中心</h1>
        <p class="page-subtitle">查看和编辑您的个人信息</p>
      </div>
    </div>
    <div v-if="loading">加载中...</div>
    <div v-else>
      <div v-if="error" class="error">{{ error }}</div>
      <div v-if="profile">
        <div>
          <label>用户名</label>
          <input v-model="profile.userName" />
        </div>
        <div>
          <label>邮箱</label>
          <input v-model="profile.userEmail" />
        </div>
        <div>
          <label>手机号</label>
          <input v-model="profile.phoneNumber" />
        </div>
        <div>
          <label>性别</label>
          <input v-model="profile.gender" />
        </div>
        <div>
          <label>单位/学校</label>
          <input v-model="profile.schoolWorkunit" />
        </div>
        <div>
          <button @click="saveProfile">保存</button>
        </div>
      </div>
      <div v-else>未获取到个人信息</div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
  color: #e0e6ed;
  animation: fadeIn 0.3s ease-in-out;
}

.page-header {
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #e0e6ed;
  margin: 0;
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 14px;
  color: #9ca3af;
  margin: 0;
}

.error { 
  color: #ef4444;
  padding: 12px;
  background: #7f1d1d;
  border-radius: 6px;
  border: 1px solid #ef4444;
  margin-bottom: 16px;
}

label { 
  display: block; 
  margin-top: 12px;
  color: #9ca3af;
  font-size: 14px;
  font-weight: 500;
}

input { 
  width: 100%; 
  padding: 12px; 
  margin-top: 8px;
  background: #0f1629;
  border: 1px solid #2a3458;
  border-radius: 6px;
  color: #e0e6ed;
  font-size: 14px;
}

input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

button { 
  margin-top: 16px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
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
</style>
