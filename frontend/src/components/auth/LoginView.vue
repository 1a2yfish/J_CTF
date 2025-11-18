<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div style="font-size: 2.5rem; margin-bottom: 12px;">🛡️</div>
        <h1>CTF竞赛平台</h1>
        <p style="opacity: 0.9;">Capture The Flag 竞赛系统</p>
      </div>
      <div class="login-body">
        <div class="login-tabs">
          <div
              class="login-tab"
              :class="{ active: activeTab === 'login' }"
              @click="activeTab = 'login'"
          >
            登录
          </div>
          <div
              class="login-tab"
              :class="{ active: activeTab === 'register' }"
              @click="activeTab = 'register'"
          >
            注册
          </div>
        </div>

        <!-- 登录表单 -->
        <div v-if="activeTab === 'login'">
          <div class="form-group">
            <label>账号</label>
            <t-input v-model="loginForm.account" placeholder="请输入邮箱或手机号" size="large">
              <template #prefix-icon>
                <t-icon name="user" />
              </template>
            </t-input>
          </div>
          <div class="form-group">
            <label>密码</label>
            <t-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" @keyup.enter="handleLogin">
              <template #prefix-icon>
                <t-icon name="lock-on" />
              </template>
            </t-input>
          </div>
          <div class="form-footer">
            <t-checkbox v-model="rememberMe">记住我</t-checkbox>
            <a href="#" style="color: var(--primary-color);">忘记密码？</a>
          </div>
          <t-button
              class="submit-btn"
              size="large"
              theme="primary"
              @click="handleLogin"
              :loading="loading"
          >
            登录
          </t-button>
        </div>

        <!-- 注册表单 -->
        <div v-if="activeTab === 'register'">
          <div class="form-group">
            <label>账号</label>
            <t-input v-model="registerForm.account" placeholder="请输入邮箱或手机号" size="large">
              <template #prefix-icon>
                <t-icon name="user" />
              </template>
            </t-input>
          </div>
          <div class="form-group">
            <label>密码</label>
            <t-input v-model="registerForm.password" type="password" placeholder="请输入密码" size="large">
              <template #prefix-icon>
                <t-icon name="lock-on" />
              </template>
            </t-input>
          </div>
          <div class="form-group">
            <label>角色</label>
            <div class="role-select">
              <div
                  class="role-option"
                  :class="{ selected: registerForm.role === 'USER' }"
                  @click="registerForm.role = 'USER'"
              >
                普通用户
              </div>
              <div
                  class="role-option"
                  :class="{ selected: registerForm.role === 'ADMIN' }"
                  @click="registerForm.role = 'ADMIN'"
              >
                管理员
              </div>
            </div>
          </div>
          <t-button
              class="submit-btn"
              size="large"
              theme="primary"
              @click="handleRegister"
              :loading="loading"
          >
            注册
          </t-button>
          <div style="text-align: center; margin-top: 16px; color: #646a73; font-size: 0.9rem;">
            已有账号？<a href="#" style="color: var(--primary-color);" @click="activeTab = 'login'">立即登录</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { Message } from 'tdesign-vue-next'

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('login')
const rememberMe = ref(false)
const loading = ref(false)

const loginForm = ref({
  account: '',
  password: ''
})

const registerForm = ref({
  account: '',
  password: '',
  role: 'USER'
})

const handleLogin = async () => {
  if (!loginForm.value.account || !loginForm.value.password) {
    Message.warning('请输入账号和密码')
    return
  }

  try {
    loading.value = true
    await authStore.login(loginForm.value.account, loginForm.value.password)

    // 重定向到仪表盘
    await router.push('/dashboard')
  } catch (error) {
    Message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.value.account || !registerForm.value.password) {
    Message.warning('请输入账号和密码')
    return
  }

  try {
    loading.value = true
    await authStore.register(
        registerForm.value.account,
        registerForm.value.password,
        registerForm.value.role
    )

    Message.success('注册成功，请登录')
    activeTab.value = 'login'
  } catch (error) {
    Message.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>