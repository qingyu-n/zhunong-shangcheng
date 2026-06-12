<template>
  <div class="login-page">
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
          <a class="nav-link" @click="navigateTo('help')">助农专区</a>
        </div>
        <div class="nav-icons">
          <a class="icon-link" @click="navigateTo('user')">
            <i class="fas fa-user"></i>
          </a>
          <a class="icon-link cart-icon" @click="navigateTo('cart')">
            <i class="fas fa-shopping-cart"></i>
            <span class="cart-badge">3</span>
          </a>
        </div>
      </div>
    </nav>

    <!-- 登录卡片区域 -->
    <div class="login-container">
      <div class="login-card">
        <!-- 页面标题 -->
        <div class="login-header">
          <h1 class="login-title">农产品商城登录</h1>
        </div>
        
        <!-- 登录表单 -->
        <form class="login-form" @submit.prevent="handleLogin">
          <!-- 用户名/手机号输入框 -->
          <div class="form-item">
            <label for="username" class="form-label">用户名/手机号</label>
            <div class="input-wrapper">
              <i class="fas fa-user input-icon"></i>
              <input 
                type="text" 
                id="username" 
                v-model="form.username"
                class="form-input" 
                placeholder="请输入用户名或手机号"
              >
            </div>
          </div>
          
          <!-- 密码输入框 -->
          <div class="form-item">
            <label for="password" class="form-label">密码</label>
            <div class="input-wrapper">
              <i class="fas fa-lock input-icon"></i>
              <input 
                :type="showPassword ? 'text' : 'password'" 
                id="password" 
                v-model="form.password"
                class="form-input" 
                placeholder="请输入密码"
              >
              <button type="button" class="eye-btn" @click="togglePassword">
                <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
          </div>
          
          <!-- 忘记密码链接 -->
          <div class="forgot-password">
            <a class="link-primary" @click="navigateTo('forgotPassword')">忘记密码?</a>
          </div>
          
          <!-- 登录按钮 -->
          <button type="submit" class="login-btn" :disabled="loading">
            <span>{{ loading ? '登录中...' : '登录' }}</span>
            <i class="fas fa-arrow-right" v-if="!loading"></i>
          </button>
          
          <!-- 注册链接 -->
          <div class="register-link">
            <span class="text-gray">还没有账号?</span>
            <a class="link-primary" @click="navigateTo('register')">立即注册</a>
          </div>
        </form>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <p class="copyright">© 2026 农产品商城 版权所有 | 助农热线：400-123-4567</p>
        <div class="footer-links">
          <a class="footer-link" @click="navigateTo('privacy')">隐私政策</a>
          <a class="footer-link" @click="navigateTo('terms')">用户协议</a>
          <a class="footer-link" @click="navigateTo('about')">关于我们</a>
          <a class="footer-link" @click="navigateTo('contact')">联系客服</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

// 表单数据
const form = reactive({
  username: '',
  password: ''
})

// 密码显示状态
const showPassword = ref(false)
const loading = ref(false)

// 切换密码显示
const togglePassword = () => {
  showPassword.value = !showPassword.value
}

// 登录处理
const handleLogin = async () => {
  if (!form.username.trim()) {
    ElMessage.warning('请输入用户名或手机号')
    return
  }
  if (!form.password.trim()) {
    ElMessage.warning('请输入密码')
    return
  }
  
  loading.value = true
  try {
    await userStore.login({
      username: form.username,
      password: form.password
    })
    ElMessage.success('登录成功')
    // 同步购物车数据
    await cartStore.fetchCartList()
    // 跳转到之前页面或首页
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 页面导航
const navigateTo = (page) => {
  console.log('导航到：', page)
  const routeMap = {
    home: '/',
    category: '/category',
    help: '/help',
    user: '/user',
    cart: '/cart',
    register: '/register',
    forgotPassword: '/forgot-password'
  }
  if (routeMap[page]) {
    router.push(routeMap[page])
  } else {
    const pageNames = {
      privacy: '隐私政策页',
      terms: '用户协议页',
      about: '关于我们页',
      contact: '联系客服页'
    }
    ElMessage.info(`跳转到：${pageNames[page] || page}`)
  }
}
</script>

<style scoped>
/* 基础样式 */
.login-page {
  min-height: 100vh;
  background-color: #f5f5f5;
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
  color: #4096ff;
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
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
  color: #4096ff;
}

.nav-icons {
  display: flex;
  align-items: center;
  gap: 20px;
}

.icon-link {
  font-size: 18px;
  color: #333;
  cursor: pointer;
  transition: color 0.3s;
  position: relative;
}

.icon-link:hover {
  color: #4096ff;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background-color: #ff4d4f;
  color: #fff;
  font-size: 11px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 登录容器 */
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 100px 24px 120px;
}

/* 登录卡片 */
.login-card {
  width: 100%;
  max-width: 420px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 48px 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-title {
  font-size: 26px;
  font-weight: 600;
  color: #333;
}

/* 登录表单 */
.login-form {
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
  padding: 0 14px 0 40px;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  font-size: 14px;
  color: #333;
  background-color: #fff;
  transition: all 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #4096ff;
  box-shadow: 0 0 0 3px rgba(64, 150, 255, 0.1);
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

/* 忘记密码 */
.forgot-password {
  display: flex;
  justify-content: flex-end;
}

.link-primary {
  font-size: 13px;
  color: #4096ff;
  cursor: pointer;
  transition: color 0.3s;
}

.link-primary:hover {
  color: #1677ff;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 46px;
  background: linear-gradient(135deg, #4096ff 0%, #1677ff 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s;
  margin-top: 4px;
}

.login-btn:hover {
  background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 150, 255, 0.3);
}

.login-btn i {
  font-size: 13px;
}

/* 注册链接 */
.register-link {
  text-align: center;
  margin-top: 8px;
}

.text-gray {
  font-size: 13px;
  color: #999;
}

.register-link .link-primary {
  margin-left: 6px;
}

/* 页脚 */
.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  padding: 16px 24px;
  box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.05);
}

.footer .container {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.copyright {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.footer-link {
  font-size: 12px;
  color: #999;
  cursor: pointer;
  transition: color 0.3s;
}

.footer-link:hover {
  color: #4096ff;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .nav-links {
    display: none;
  }
  
  .login-card {
    padding: 36px 24px;
  }
  
  .login-title {
    font-size: 22px;
  }
}
</style>
