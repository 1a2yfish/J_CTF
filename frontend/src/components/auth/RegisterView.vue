<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const auth = useAuthStore()

const userPassword = ref('')
const phoneNumber = ref('')
const userEmail = ref('')
const userName = ref('')
const gender = ref('')
const schoolWorkunit = ref('')
const error = ref(null)

async function submit() {
    error.value = null
    try {
        const payload = {
            userPassword: userPassword.value,
            phoneNumber: phoneNumber.value,
            userEmail: userEmail.value,
            userName: userName.value,
            gender: gender.value,
            schoolWorkunit: schoolWorkunit.value
        }
        await auth.register(payload)
        // 注册成功后跳转登录
        router.push('/login')
    } catch (e) {
        error.value = e.message || '注册失败'
    }
}
</script>

<template>
  <div class="register-container">
    <div class="register-box">
      <h2>注册</h2>
      <div class="form-group">
        <label>用户名</label>
        <input v-model="userName" type="text" placeholder="请输入用户名" />
      </div>
      <div class="form-group">
        <label>密码</label>
        <input v-model="userPassword" type="password" placeholder="请输入密码" />
      </div>
      <div class="form-group">
        <label>邮箱</label>
        <input v-model="userEmail" type="email" placeholder="请输入邮箱" />
      </div>
      <div class="form-group">
        <label>手机号</label>
        <input v-model="phoneNumber" type="text" placeholder="请输入手机号" />
      </div>
      <div class="form-group">
        <label>性别</label>
        <input v-model="gender" type="text" placeholder="男/女/未知" />
      </div>
      <div class="form-group">
        <label>单位/学校</label>
        <input v-model="schoolWorkunit" type="text" placeholder="请输入单位或学校" />
      </div>
      <div class="form-group">
        <button @click="submit" :disabled="auth.loading">
          {{ auth.loading ? '注册中...' : '注册' }}
        </button>
      </div>
      <div v-if="error" class="error">{{ error }}</div>
      <div class="login-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: #0a0e27;
}

.register-box {
  background: #1a1f3a;
  border-radius: 16px;
  padding: 48px;
  width: 100%;
  max-width: 600px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  max-height: 90vh;
  overflow-y: auto;
  border: 1px solid #2a3458;
}

.register-box h2 {
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
  margin-bottom: 20px;
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

.login-link {
  text-align: center;
  margin-top: 24px;
  color: #9ca3af;
  font-size: 14px;
}

.login-link a {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.login-link a:hover {
  color: #60a5fa;
  text-decoration: underline;
}
</style>
