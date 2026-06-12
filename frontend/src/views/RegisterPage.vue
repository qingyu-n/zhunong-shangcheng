<template>
  <div class="register-page">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="container">
        <a class="logo" @click="navigateTo('home')">
          <i class="fas fa-leaf logo-icon"></i>
          <span class="logo-text">农鲜达</span>
        </a>
        <div class="nav-links">
          <a class="nav-link" @click="navigateTo('home')">首页</a>
          <a class="nav-link" @click="navigateTo('category')">分类</a>
          <a class="nav-link" @click="navigateTo('help')">助农</a>
          <a class="nav-link" @click="navigateTo('user')">我的</a>
        </div>
        <div class="nav-actions">
          <a class="cart-icon" @click="navigateTo('cart')">
            <i class="fas fa-shopping-cart"></i>
          </a>
          <button class="login-btn" @click="navigateTo('login')">登录</button>
        </div>
      </div>
    </nav>

    <!-- 注册表单区域 -->
    <div class="register-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">绿色农产品商城</h1>
        <p class="page-subtitle">新鲜直达，健康生活</p>
      </div>
      
      <!-- 表单卡片 -->
      <div class="register-card">
        <!-- 表单标题 -->
        <div class="card-header">
          <h2 class="card-title">创建新账号</h2>
          <p class="card-subtitle">填写以下信息完成注册</p>
        </div>
        
        <!-- 注册表单 -->
        <form class="register-form" @submit.prevent="handleRegister">
          <!-- 用户名输入框 -->
          <div class="form-item">
            <label for="username" class="form-label">用户名</label>
            <div class="input-wrapper">
              <i class="fas fa-lock input-icon"></i>
              <input 
                type="text" 
                id="username" 
                v-model="form.username"
                class="form-input" 
                placeholder="请输入用户名"
              >
              <button type="button" class="eye-btn" @click="toggleUsernameVisibility">
                <i :class="showUsername ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
          </div>
          
          <!-- 设置密码输入框 -->
          <div class="form-item">
            <label for="password" class="form-label">设置密码</label>
            <div class="input-wrapper">
              <i class="fas fa-lock input-icon"></i>
              <input 
                :type="showPassword ? 'text' : 'password'" 
                id="password" 
                v-model="form.password"
                class="form-input" 
                placeholder="请设置密码"
                @input="checkPasswordStrength"
              >
              <button type="button" class="eye-btn" @click="togglePassword">
                <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
            <!-- 密码强度条 -->
            <div class="password-strength">
              <div class="strength-bar" :class="{ active: passwordStrength >= 1 }"></div>
              <div class="strength-bar" :class="{ active: passwordStrength >= 2 }"></div>
              <div class="strength-bar" :class="{ active: passwordStrength >= 3 }"></div>
            </div>
            <p class="password-tip">密码需包含字母和数字，长度不少于6位</p>
          </div>
          
          <!-- 确认密码输入框 -->
          <div class="form-item">
            <label for="confirmPassword" class="form-label">确认密码</label>
            <div class="input-wrapper">
              <i class="fas fa-lock input-icon"></i>
              <input 
                :type="showConfirmPassword ? 'text' : 'password'" 
                id="confirmPassword" 
                v-model="form.confirmPassword"
                class="form-input" 
                placeholder="请再次输入密码"
              >
              <button type="button" class="eye-btn" @click="toggleConfirmPassword">
                <i :class="showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
          </div>
          
          <!-- 注册按钮 -->
          <button type="submit" class="register-btn">
            <span>注册账号</span>
            <i class="fas fa-arrow-right"></i>
          </button>
          
          <!-- 返回登录链接 -->
          <div class="login-link">
            <span class="text-gray">已有账号？</span>
            <a class="link-primary" @click="navigateTo('login')">立即登录</a>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

// 表单数据
const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

// 密码显示状态
const showUsername = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const passwordStrength = ref(0)
const loading = ref(false)

// 切换用户名显示
const toggleUsernameVisibility = () => {
  showUsername.value = !showUsername.value
}

// 切换密码显示
const togglePassword = () => {
  showPassword.value = !showPassword.value
}

// 切换确认密码显示
const toggleConfirmPassword = () => {
  showConfirmPassword.value = !showConfirmPassword.value
}

// 检查密码强度
const checkPasswordStrength = () => {
  const pwd = form.password
  let strength = 0

  if (pwd.length >= 6) strength++
  if (/[a-zA-Z]/.test(pwd) && /[0-9]/.test(pwd)) strength++
  if (pwd.length >= 10 && /[^a-zA-Z0-9]/.test(pwd)) strength++

  passwordStrength.value = strength
}

// 注册处理
const handleRegister = async () => {
  if (!form.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!form.password.trim()) {
    ElMessage.warning('请设置密码')
    return
  }
  if (form.password.length < 6) {
    ElMessage.warning('密码长度不少于6位')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    await userStore.register({
      username: form.username,
      password: form.password
    })
    ElMessage.success('注册成功！请登录')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

// 页面导航
const navigateTo = (page) => {
  console.log('导航到：', page)
  const routes = {
    home: '/',
    category: '/category',
    help: '/help',
    user: '/user',
    cart: '/cart',
    login: '/login'
  }
  router.push(routes[page] || '/')
}
</script>

<style scoped>
/* 基础样式 */
.register-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  font-family: 'Alibaba PuHuiTi', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 顶部导航栏 */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  z-index: 100;
  height: 64px;
}

.navbar .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.logo-icon {
  color: #4a6cf7;
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-link {
  font-size: 15px;
  color: #333;
  cursor: pointer;
  transition: color 0.3s;
}

.nav-link:hover {
  color: #4a6cf7;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cart-icon {
  font-size: 18px;
  color: #333;
  cursor: pointer;
  transition: color 0.3s;
}

.cart-icon:hover {
  color: #4a6cf7;
}

.login-btn {
  padding: 8px 20px;
  background-color: #4a6cf7;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-btn:hover {
  background-color: #3d5bd9;
}

/* 注册容器 */
.register-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 24px 60px;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 14px;
  color: #666;
}

/* 注册卡片 */
.register-card {
  width: 100%;
  max-width: 420px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 40px;
}

/* 卡片标题 */
.card-header {
  margin-bottom: 28px;
}

.card-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.card-subtitle {
  font-size: 13px;
  color: #999;
}

/* 注册表单 */
.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  color: #999;
  font-size: 14px;
}

.form-input {
  width: 100%;
  height: 46px;
  padding: 0 40px 0 40px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
  background-color: #fff;
  transition: all 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #4a6cf7;
  box-shadow: 0 0 0 3px rgba(74, 108, 247, 0.1);
}

.form-input::placeholder {
  color: #bbb;
}

.eye-btn {
  position: absolute;
  right: 14px;
  background: none;
  border: none;
  color: #999;
  font-size: 14px;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.3s;
}

.eye-btn:hover {
  color: #666;
}

/* 密码强度条 */
.password-strength {
  display: flex;
  gap: 6px;
  margin-top: 8px;
}

.strength-bar {
  flex: 1;
  height: 3px;
  background-color: #e0e0e0;
  border-radius: 2px;
  transition: background-color 0.3s;
}

.strength-bar.active {
  background-color: #4a6cf7;
}

.password-tip {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}

/* 注册按钮 */
.register-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #4a6cf7 0%, #3d5bd9 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s;
  margin-top: 8px;
}

.register-btn:hover {
  background: linear-gradient(135deg, #3d5bd9 0%, #2f4ac7 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(74, 108, 247, 0.3);
}

.register-btn i {
  font-size: 13px;
}

/* 登录链接 */
.login-link {
  text-align: center;
  margin-top: 4px;
}

.text-gray {
  font-size: 13px;
  color: #999;
}

.link-primary {
  font-size: 13px;
  color: #4a6cf7;
  cursor: pointer;
  transition: color 0.3s;
  margin-left: 4px;
}

.link-primary:hover {
  color: #3d5bd9;
  text-decoration: underline;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .nav-links {
    display: none;
  }
  
  .page-title {
    font-size: 26px;
  }
  
  .register-card {
    padding: 32px 24px;
  }
  
  .card-title {
    font-size: 20px;
  }
}
</style>
