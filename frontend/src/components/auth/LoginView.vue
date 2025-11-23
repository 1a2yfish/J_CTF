<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const auth = useAuthStore()

const account = ref('')
const password = ref('')
const error = ref(null)

async function submit() {
    error.value = null
    try {
        await auth.login(account.value, password.value)
        router.push('/dashboard')
    } catch (e) {
        error.value = e.message || '登录失败'
    }
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <h2>登录</h2>
      <div class="form-group">
        <label>账号</label>
        <input v-model="account" type="text" placeholder="请输入账号/邮箱/手机号" @keyup.enter="submit" />
      </div>
      <div class="form-group">
        <label>密码</label>
        <input v-model="password" type="password" placeholder="请输入密码" @keyup.enter="submit" />
      </div>
      <div class="form-group">
        <button @click="submit" :disabled="auth.loading">
          {{ auth.loading ? '登录中...' : '登录' }}
        </button>
      </div>
      <div v-if="error" class="error">{{ error }}</div>
      <div class="register-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: #0a0e27;
}

.login-box {
  background: #1a1f3a;
  border-radius: 16px;
  padding: 48px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  border: 1px solid #2a3458;
}

.login-box h2 {
  text-align: center;
  margin-bottom: 32px;
  color: #e0e6ed;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #9ca3af;
  font-size: 14px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #2a3458;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: #0f1629;
  color: #e0e6ed;
}

.form-group input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

.form-group button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.form-group button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.3);
}

.form-group button:active {
  transform: translateY(0);
}

.error {
  color: #ef4444;
  margin-top: 12px;
  text-align: center;
  font-size: 14px;
  padding: 8px;
  background: #7f1d1d;
  border-radius: 6px;
  border: 1px solid #ef4444;
}

.register-link {
  text-align: center;
  margin-top: 24px;
  color: #9ca3af;
  font-size: 14px;
}

.register-link a {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.register-link a:hover {
  color: #60a5fa;
  text-decoration: underline;
}
</style>
