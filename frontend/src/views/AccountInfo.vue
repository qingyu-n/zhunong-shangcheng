<template>
  <div class="account-info-page">
    <!-- 返回区域 -->
    <div class="back-section">
      <div class="back-container">
        <a href="#" class="back-btn" @click.prevent="goBack">
          <i class="fas fa-arrow-left"></i>
        </a>
        <span class="back-text">返回个人中心</span>
      </div>
    </div>

    <!-- 页面标题 -->
    <div class="page-title-section">
      <div class="title-container">
        <h1 class="page-title">账号信息</h1>
      </div>
    </div>

    <!-- 个人信息卡片 -->
    <div class="info-section">
      <div class="info-container">
        <div class="info-card">
          <h2 class="card-title">个人信息</h2>
          
          <div class="form-list">
            <!-- 用户名 -->
            <div class="form-item">
              <div class="form-label">
                <i class="fas fa-user"></i>
                <label>用户名</label>
              </div>
              <input
                type="text"
                v-model="form.username"
                placeholder="请输入用户名"
                class="form-input"
              />
            </div>

            <!-- 电子邮箱 -->
            <div class="form-item">
              <div class="form-label">
                <i class="fas fa-envelope"></i>
                <label>电子邮箱</label>
              </div>
              <input
                type="email"
                v-model="form.email"
                placeholder="请输入电子邮箱"
                class="form-input"
              />
            </div>

            <!-- 手机号码 -->
            <div class="form-item">
              <div class="form-label">
                <i class="fas fa-phone"></i>
                <label>手机号码</label>
              </div>
              <input
                type="tel"
                v-model="form.phone"
                placeholder="请输入手机号码"
                class="form-input"
              />
            </div>

            <!-- 性别 -->
            <div class="form-item">
              <div class="form-label">
                <i class="fas fa-venus-mars"></i>
                <label>性别</label>
              </div>
              <select v-model="form.gender" class="form-select">
                <option value="">请选择性别</option>
                <option value="male">男</option>
                <option value="female">女</option>
                <option value="other">其他</option>
              </select>
            </div>

            <!-- 出生日期 -->
            <div class="form-item">
              <div class="form-label">
                <i class="fas fa-calendar"></i>
                <label>出生日期</label>
              </div>
              <input
                type="date"
                v-model="form.birthday"
                class="form-input"
              />
            </div>
          </div>

          <div class="form-actions">
            <button class="save-btn" @click="saveProfile">保存修改</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 安全设置卡片 -->
    <div class="security-section">
      <div class="security-container">
        <div class="security-card">
          <h2 class="card-title">安全设置</h2>
          
          <div class="form-list">
            <!-- 当前密码 -->
            <div class="form-item">
              <div class="form-label">
                <i class="fas fa-lock"></i>
                <label>当前密码</label>
              </div>
              <div class="input-wrapper">
                <input
                  :type="showCurrentPassword ? 'text' : 'password'"
                  v-model="passwordForm.currentPassword"
                  placeholder="请输入当前密码"
                  class="form-input"
                />
                <button
                  type="button"
                  class="eye-btn"
                  @click="togglePassword('current')"
                >
                  <i :class="showCurrentPassword ? 'fas fa-eye' : 'fas fa-eye-slash'"></i>
                </button>
              </div>
            </div>

            <!-- 新密码 -->
            <div class="form-item">
              <div class="form-label">
                <i class="fas fa-key"></i>
                <label>新密码</label>
              </div>
              <div class="input-wrapper">
                <input
                  :type="showNewPassword ? 'text' : 'password'"
                  v-model="passwordForm.newPassword"
                  placeholder="请输入新密码"
                  class="form-input"
                />
                <button
                  type="button"
                  class="eye-btn"
                  @click="togglePassword('new')"
                >
                  <i :class="showNewPassword ? 'fas fa-eye' : 'fas fa-eye-slash'"></i>
                </button>
              </div>
            </div>

            <!-- 确认新密码 -->
            <div class="form-item">
              <div class="form-label">
                <i class="fas fa-key"></i>
                <label>确认新密码</label>
              </div>
              <div class="input-wrapper">
                <input
                  :type="showConfirmPassword ? 'text' : 'password'"
                  v-model="passwordForm.confirmPassword"
                  placeholder="请再次输入新密码"
                  class="form-input"
                />
                <button
                  type="button"
                  class="eye-btn"
                  @click="togglePassword('confirm')"
                >
                  <i :class="showConfirmPassword ? 'fas fa-eye' : 'fas fa-eye-slash'"></i>
                </button>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button class="save-btn" @click="updatePassword">更新密码</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 个人信息表单
const form = ref({
  username: '',
  email: '',
  phone: '',
  gender: '',
  birthday: ''
})

// 密码表单
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码显示状态
const showCurrentPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

// 页面加载时获取用户信息
onMounted(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      form.value.username = user.username || user.userName || ''
      form.value.email = user.email || ''
      form.value.phone = user.phone || ''
      form.value.gender = user.gender || ''
      form.value.birthday = user.birthday || ''
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
})

// 返回上一页
const goBack = () => {
  router.back()
}

// 页面跳转
const navigateTo = (page) => {
  switch (page) {
    case 'home':
      router.push('/')
      break
    case 'category':
      router.push('/category')
      break
    case 'help':
      router.push('/help')
      break
    case 'user':
      router.push('/user')
      break
    case 'cart':
      router.push('/cart')
      break
  }
}

// 切换密码显示
const togglePassword = (type) => {
  switch (type) {
    case 'current':
      showCurrentPassword.value = !showCurrentPassword.value
      break
    case 'new':
      showNewPassword.value = !showNewPassword.value
      break
    case 'confirm':
      showConfirmPassword.value = !showConfirmPassword.value
      break
  }
}

// 保存个人信息
const saveProfile = async () => {
  if (!form.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  try {
    ElMessage.info('正在保存...')
    const updateData = {
      nickname: form.value.username || undefined,
      email: form.value.email || undefined,
      phone: form.value.phone || undefined,
      gender: form.value.gender ? Number(form.value.gender) : undefined,
      birthday: form.value.birthday || undefined
    }
    // 过滤掉undefined值，避免Jackson序列化错误
    Object.keys(updateData).forEach(key => {
      if (updateData[key] === undefined) delete updateData[key]
    })
    console.log('提交的用户信息:', updateData)
    await authApi.updateUserInfo(updateData)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败: ' + (error.message || '网络错误'))
  }
}

// 更新密码
const updatePassword = async () => {
  if (!passwordForm.value.currentPassword) {
    ElMessage.warning('请输入当前密码')
    return
  }
  if (!passwordForm.value.newPassword || passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  try {
    ElMessage.info('正在修改密码...')
    await authApi.changePassword({
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  } catch (error) {
    console.error('修改密码失败:', error)
    if (error.message?.includes('401')) {
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else if (error.message?.includes('当前密码')) {
      ElMessage.error('当前密码错误，请重新输入')
    } else {
      ElMessage.error('修改失败: ' + (error.message || '网络错误'))
    }
  }
}
</script>

<style scoped>
.account-info-page {
  min-height: 100vh;
  background-color: #f3f4f6;
  padding-bottom: 48px;
}

/* 返回区域 */
.back-section {
  width: 100%;
  padding: 24px 0;
}

.back-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  width: 32px;
  height: 32px;
  background-color: #f3f4f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1f2937;
  text-decoration: none;
  transition: all 0.2s;
}

.back-btn:hover {
  background-color: #4a6cf7;
  color: #ffffff;
}

.back-text {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
}

/* 页面标题 */
.page-title-section {
  width: 100%;
  margin-bottom: 24px;
}

.title-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: center;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #1f2937;
}

/* 个人信息区域 */
.info-section {
  width: 100%;
  margin-bottom: 24px;
}

.info-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 24px;
}

.info-card {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 24px;
}

.card-title {
  font-size: 20px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 24px;
}

/* 表单样式 */
.form-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  align-items: center;
}

.form-label {
  width: 25%;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-size: 14px;
}

.form-label i {
  font-size: 14px;
  width: 16px;
}

.form-input,
.form-select {
  width: 75%;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  color: #1f2937;
  background-color: #ffffff;
  outline: none;
  transition: all 0.2s;
}

.form-input:focus,
.form-select:focus {
  border-color: #4a6cf7;
  box-shadow: 0 0 0 3px rgba(74, 108, 247, 0.1);
}

.form-input::placeholder {
  color: #9ca3af;
}

.form-select {
  cursor: pointer;
}

.input-wrapper {
  width: 75%;
  position: relative;
}

.input-wrapper .form-input {
  width: 100%;
  padding-right: 40px;
}

.eye-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
}

.eye-btn:hover {
  color: #4a6cf7;
}

/* 表单操作 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

.save-btn {
  width: 128px;
  height: 40px;
  background-color: #4a6cf7;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.save-btn:hover {
  background-color: #3d5ce0;
}

/* 安全设置区域 */
.security-section {
  width: 100%;
}

.security-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 24px;
}

.security-card {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 24px;
}
</style>
