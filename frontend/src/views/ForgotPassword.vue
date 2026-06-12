<template>
  <div class="forgot-password-page">
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
      </div>
    </nav>

    <!-- 密码找回卡片区域 -->
    <div class="forgot-container">
      <div class="forgot-card">
        <!-- 标题区域 -->
        <div class="card-header">
          <h2 class="card-title">找回密码</h2>
          <p class="card-subtitle">请输入您的账号信息，设置新密码</p>
        </div>
        
        <!-- 表单区域 -->
        <form class="forgot-form" @submit.prevent="handleReset">
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
            </div>
          </div>
          
          <!-- 旧密码输入框 -->
          <div class="form-item">
            <label for="oldPassword" class="form-label">旧密码</label>
            <div class="input-wrapper">
              <i class="fas fa-key input-icon"></i>
              <input 
                :type="showOldPassword ? 'text' : 'password'" 
                id="oldPassword" 
                v-model="form.oldPassword"
                class="form-input" 
                placeholder="请输入旧密码"
              >
            </div>
          </div>
          
          <!-- 新密码输入框 -->
          <div class="form-item">
            <label for="newPassword" class="form-label">新密码</label>
            <div class="input-wrapper">
              <i class="fas fa-lock input-icon"></i>
              <input 
                :type="showNewPassword ? 'text' : 'password'" 
                id="newPassword" 
                v-model="form.newPassword"
                class="form-input" 
                placeholder="请设置新密码"
              >
              <button type="button" class="eye-btn" @click="toggleNewPassword">
                <i :class="showNewPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
          </div>
          
          <!-- 确认重置按钮 -->
          <button type="submit" class="reset-btn">确认重置</button>
          
          <!-- 返回登录链接 -->
          <div class="login-link">
            <a class="link-primary" @click="navigateTo('login')">返回登录</a>
          </div>
        </form>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <p class="copyright">© 2026 农产品商城 版权所有</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 表单数据
const form = reactive({
  username: '',
  oldPassword: '',
  newPassword: ''
})

// 密码显示状态
const showOldPassword = ref(false)
const showNewPassword = ref(false)

// 切换旧密码显示
const toggleOldPassword = () => {
  showOldPassword.value = !showOldPassword.value
}

// 切换新密码显示
const toggleNewPassword = () => {
  showNewPassword.value = !showNewPassword.value
}

// 重置密码处理
const handleReset = () => {
  if (!form.username.trim()) {
    alert('请输入用户名')
    return
  }
  if (!form.oldPassword.trim()) {
    alert('请输入旧密码')
    return
  }
  if (!form.newPassword.trim()) {
    alert('请设置新密码')
    return
  }
  if (form.newPassword.length < 6) {
    alert('新密码长度不少于6位')
    return
  }
  console.log('重置密码信息：', form)
  alert('密码重置成功！')
  router.push('/login')
}

// 页面导航
const navigateTo = (page) => {
  console.log('导航到：', page)
  const routes = {
    home: '/',
    category: '/category',
    help: '/help',
    user: '/user',
    login: '/login'
  }
  router.push(routes[page] || '/')
}
</script>

<style scoped>
/* 基础样式 */
.forgot-password-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family: 'Alibaba PuHuiTi', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  display: flex;
  flex-direction: column;
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
  color: #4096ff;
}

/* 找回密码容器 */
.forgot-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 100px 24px 100px;
}

/* 找回密码卡片 */
.forgot-card {
  width: 100%;
  max-width: 420px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 40px;
}

/* 卡片标题 */
.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.card-subtitle {
  font-size: 14px;
  color: #666;
}

/* 找回密码表单 */
.forgot-form {
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
  border: 1px solid #e0e0e0;
  border-radius: 8px;
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

/* 确认重置按钮 */
.reset-btn {
  width: 100%;
  height: 46px;
  background: linear-gradient(135deg, #4096ff 0%, #1677ff 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 8px;
}

.reset-btn:hover {
  background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 150, 255, 0.3);
}

/* 登录链接 */
.login-link {
  text-align: center;
  margin-top: 4px;
}

.link-primary {
  font-size: 14px;
  color: #4096ff;
  cursor: pointer;
  transition: color 0.3s;
}

.link-primary:hover {
  color: #1677ff;
  text-decoration: underline;
}

/* 页脚 */
.footer {
  padding: 16px 24px;
  background-color: #fff;
  border-top: 1px solid #f0f0f0;
}

.footer .container {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.copyright {
  font-size: 13px;
  color: #666;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .nav-links {
    display: none;
  }
  
  .forgot-card {
    padding: 32px 24px;
  }
  
  .card-title {
    font-size: 22px;
  }
}
</style>
