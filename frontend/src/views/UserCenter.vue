<template>
  <div class="user-center-page">
    <!-- 导航区 -->
    <header class="header">
      <div class="header-container">
        <div class="logo" @click="navigateTo('home')">
          <i class="fas fa-leaf logo-icon"></i>
          <span class="logo-text">农鲜生</span>
        </div>
        <nav class="nav-menu">
          <a class="nav-link" @click="navigateTo('home')">
            <i class="fas fa-home"></i>
            <span>首页</span>
          </a>
          <a class="nav-link" @click="navigateTo('category')">
            <i class="fas fa-th-large"></i>
            <span>分类</span>
          </a>
          <a class="nav-link" @click="navigateTo('help')">
            <i class="fas fa-heart"></i>
            <span>助农</span>
          </a>
          <a class="nav-link active">
            <i class="fas fa-user"></i>
            <span>我的</span>
          </a>
        </nav>
      </div>
    </header>

    <!-- 页面标题区 -->
    <section class="page-title-section">
      <div class="container">
        <h1 class="page-title">个人中心</h1>
      </div>
    </section>

    <!-- 用户信息区 -->
    <section class="user-info-section">
      <div class="container">
        <div class="user-info-card">
          <img :src="userStore.userInfo?.avatar || 'https://assets.mockplus.cn/ai/newImages/pexels/357.jpg'" alt="用户头像" class="user-avatar">
          <div class="user-details">
            <div class="user-name-row">
              <h2 class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.userName || '用户' }}</h2>
              <span class="member-badge">{{ (userStore.userInfo?.roles && userStore.userInfo.roles.includes('ADMIN')) ? '管理员' : '普通会员' }}</span>
            </div>
            <div class="user-meta-item">
              <i class="fas fa-id-card"></i>
              <span>账号：{{ userStore.userInfo?.userName || '-' }}</span>
            </div>
            <div class="user-meta-item">
              <i class="fas fa-phone"></i>
              <span>手机号：{{ userStore.userInfo?.phone || '未绑定' }}</span>
            </div>
            <div class="user-meta-item">
              <i class="fas fa-envelope"></i>
              <span>邮箱：{{ userStore.userInfo?.email || '未绑定' }}</span>
            </div>
          </div>
          <div class="user-actions">
            <button class="edit-profile-btn" @click="openEditDialog">
              <i class="fas fa-user-edit"></i>
              编辑资料
            </button>
            <div class="last-login">上次登录：{{ formatDate(userStore.userInfo?.updateTime) }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能入口区 -->
    <section class="function-grid-section">
      <div class="container">
        <div class="function-grid">
          <!-- 我的订单 -->
          <div class="function-card" @click="navigateTo('orders')">
            <div class="function-icon-wrapper">
              <i class="fas fa-shopping-bag"></i>
            </div>
            <span class="function-name">我的订单</span>
          </div>
          
          <!-- 购物车 -->
          <div class="function-card" @click="navigateTo('cart')">
            <div class="function-icon-wrapper">
              <i class="fas fa-shopping-cart"></i>
              <span class="cart-badge" v-if="cartStore.totalCount > 0">{{ cartStore.totalCount }}</span>
            </div>
            <span class="function-name">购物车</span>
          </div>
          
          <!-- 收货地址 -->
          <div class="function-card" @click="navigateTo('address')">
            <div class="function-icon-wrapper">
              <i class="fas fa-map-marker-alt"></i>
            </div>
            <span class="function-name">收货地址</span>
          </div>
          
          <!-- 账号信息 -->
          <div class="function-card" @click="navigateTo('account')">
            <div class="function-icon-wrapper">
              <i class="fas fa-user-cog"></i>
            </div>
            <span class="function-name">账号信息</span>
          </div>
          
          <!-- 我的收藏 -->
          <div class="function-card" @click="navigateTo('favorites')">
            <div class="function-icon-wrapper">
              <i class="fas fa-heart"></i>
            </div>
            <span class="function-name">我的收藏</span>
          </div>
          
          <!-- 消息中心 -->
          <div class="function-card" @click="navigateTo('messages')">
            <div class="function-icon-wrapper">
              <i class="fas fa-bell"></i>
            </div>
            <span class="function-name">消息中心</span>
          </div>
          
          <!-- 帮助中心 -->
          <div class="function-card" @click="navigateTo('help')">
            <div class="function-icon-wrapper">
              <i class="fas fa-question-circle"></i>
            </div>
            <span class="function-name">帮助中心</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 农户专区 -->
    <section class="farmer-section" v-if="userStore.isLoggedIn">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">
            <i class="fas fa-seedling"></i>
            农户专区
          </h2>
          <span class="section-desc">成为农户，发布您的优质农产品</span>
        </div>

        <!-- 已是农户 - 显示农户功能入口 -->
        <div class="farmer-grid" v-if="userStore.isFarmer">
          <div class="farmer-card" @click="navigateTo('my-shop')">
            <div class="farmer-card-icon shop-icon">
              <i class="fas fa-store"></i>
            </div>
            <div class="farmer-card-info">
              <h3>我的店铺</h3>
              <p>管理店铺信息与装修</p>
            </div>
            <i class="fas fa-chevron-right farmer-card-arrow"></i>
          </div>

          <div class="farmer-card" @click="navigateTo('publish')">
            <div class="farmer-card-icon publish-icon">
              <i class="fas fa-plus-circle"></i>
            </div>
            <div class="farmer-card-info">
              <h3>发布商品</h3>
              <p>上架新的农产品</p>
            </div>
            <i class="fas fa-chevron-right farmer-card-arrow"></i>
          </div>

          <div class="farmer-card" @click="navigateTo('my-products')">
            <div class="farmer-card-icon products-icon">
              <i class="fas fa-boxes"></i>
            </div>
            <div class="farmer-card-info">
              <h3>我的商品</h3>
              <p>管理已发布的商品</p>
            </div>
            <i class="fas fa-chevron-right farmer-card-arrow"></i>
          </div>

          <div class="farmer-card" @click="navigateTo('statistics')">
            <div class="farmer-card-icon stats-icon">
              <i class="fas fa-chart-line"></i>
            </div>
            <div class="farmer-card-info">
              <h3>数据统计</h3>
              <p>查看销售数据与趋势</p>
            </div>
            <i class="fas fa-chevron-right farmer-card-arrow"></i>
          </div>

          <div class="farmer-card" @click="navigateTo('messages')">
            <div class="farmer-card-icon message-icon">
              <i class="fas fa-envelope"></i>
            </div>
            <div class="farmer-card-info">
              <h3>消息通知</h3>
              <p>审核结果与系统通知</p>
            </div>
            <i class="fas fa-chevron-right farmer-card-arrow"></i>
          </div>

          <div class="farmer-card" @click="navigateTo('farmer-orders')">
            <div class="farmer-card-icon" style="color: #409eff;">
              <i class="fas fa-shipping-fast"></i>
            </div>
            <div class="farmer-card-info">
              <h3>订单发货</h3>
              <p>管理订单与物流发货</p>
            </div>
            <i class="fas fa-chevron-right farmer-card-arrow"></i>
          </div>
        </div>

        <!-- 农户申请审核中 -->
        <div class="farmer-grid" v-else-if="userStore.userInfo?.farmerApplyStatus === 0">
          <div class="farmer-card apply-card" style="border-color: #e6a23c; background: #fdf6ec;">
            <div class="farmer-card-icon" style="color: #e6a23c;">
              <i class="fas fa-clock"></i>
            </div>
            <div class="farmer-card-info">
              <h3 style="color: #e6a23c;">农户申请审核中</h3>
              <p>您的申请已提交，请耐心等待审核结果</p>
            </div>
          </div>
        </div>

        <!-- 农户申请被拒绝 -->
        <div class="farmer-grid" v-else-if="userStore.userInfo?.farmerApplyStatus === 2">
          <div class="farmer-card apply-card" @click="navigateTo('apply-farmer')" style="border-color: #f56c6c; background: #fef0f0;">
            <div class="farmer-card-icon" style="color: #f56c6c;">
              <i class="fas fa-redo"></i>
            </div>
            <div class="farmer-card-info">
              <h3 style="color: #f56c6c;">重新申请成为农户</h3>
              <p>上次申请被拒绝，点击重新提交</p>
            </div>
            <i class="fas fa-chevron-right farmer-card-arrow"></i>
          </div>
        </div>

        <!-- 非农户用户 - 显示申请入口 -->
        <div class="farmer-grid" v-else>
          <div class="farmer-card apply-card" @click="navigateTo('apply-farmer')">
            <div class="farmer-card-icon">
              <i class="fas fa-user-plus"></i>
            </div>
            <div class="farmer-card-info">
              <h3>申请成为农户</h3>
              <p>提交资质审核，开启助农之旅</p>
            </div>
            <i class="fas fa-chevron-right farmer-card-arrow"></i>
          </div>
        </div>
      </div>
    </section>

    <!-- 页脚区 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-links">
            <a class="footer-link" @click="handleLogout">退出登录</a>
            <a class="footer-link" @click="navigateTo('about')">关于我们</a>
          </div>
          <div class="copyright">© 2026 农鲜生. 保留所有权利</div>
        </div>
      </div>
    </footer>

    <!-- 编辑资料弹窗 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑资料"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="头像">
          <div class="avatar-upload">
            <img :src="editForm.avatar || 'https://assets.mockplus.cn/ai/newImages/pexels/357.jpg'" class="preview-avatar">
            <el-upload
              class="avatar-uploader"
              action="/api/common/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
            >
              <el-button type="primary" size="small">更换头像</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :label="0">保密</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker
            v-model="editForm.birthday"
            type="date"
            placeholder="选择出生日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

// 编辑弹窗
const showEditDialog = ref(false)
const saving = ref(false)

// 编辑表单
const editForm = reactive({
  nickname: '',
  phone: '',
  email: '',
  gender: 0,
  birthday: '',
  avatar: ''
})

// 打开编辑弹窗时初始化表单
const openEditDialog = () => {
  const userInfo = userStore.userInfo
  editForm.nickname = userInfo?.nickname || ''
  editForm.phone = userInfo?.phone || ''
  editForm.email = userInfo?.email || ''
  editForm.gender = userInfo?.gender ?? 0
  editForm.birthday = userInfo?.birthday || ''
  editForm.avatar = userInfo?.avatar || ''
  showEditDialog.value = true
}

// 保存资料
const saveProfile = async () => {
  saving.value = true
  try {
    const submitData = {
      nickname: editForm.nickname,
      phone: editForm.phone,
      email: editForm.email,
      gender: Number(editForm.gender),
      birthday: editForm.birthday,
      avatar: editForm.avatar
    }
    await userStore.updateUserInfo(submitData)
    await userStore.fetchUserInfo()
    ElMessage.success('保存成功')
    showEditDialog.value = false
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 头像上传成功
const handleAvatarSuccess = (res) => {
  if (res.code === 200) {
    editForm.avatar = res.data
    ElMessage.success('头像上传成功')
  }
}

// 头像上传前检查
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('只支持 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 页面跳转
const navigateTo = (page) => {
  const routes = {
    home: '/',
    category: '/category',
    help: '/help',
    orders: '/orders',
    cart: '/cart',
    address: '/address',
    account: '/account',
    favorites: '/favorites',
    about: '/about',
    messages: '/messages',
    'apply-farmer': '/apply-farmer',
    'my-shop': '/my-shop',
    publish: '/publish',
    'my-products': '/my-products',
    statistics: '/statistics',
    'farmer-orders': '/farmer-orders'
  }
  if (routes[page]) {
    router.push(routes[page])
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    cartStore.resetCart()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(async () => {
  // 获取最新用户信息
  console.log('UserCenter onMounted - isLoggedIn:', userStore.isLoggedIn)
  console.log('UserCenter onMounted - userInfo:', userStore.userInfo)
  if (userStore.isLoggedIn) {
    try {
      const userInfo = await userStore.fetchUserInfo()
      console.log('UserCenter fetchUserInfo success:', userInfo)
    } catch (error) {
      console.error('UserCenter fetchUserInfo error:', error)
    }
    cartStore.fetchCartList()
  }
})
</script>

<style scoped>
/* 基础样式 */
.user-center-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 导航区 */
.header {
  width: 100%;
  height: 64px;
  background: linear-gradient(135deg, #43A047 0%, #4CAF50 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-container {
  width: 1200px;
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.logo-icon {
  font-size: 24px;
  color: #ffffff;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  transition: all 0.2s ease;
  cursor: pointer;
}

.nav-link:hover {
  color: #ffffff;
}

.nav-link.active {
  color: #ffffff;
  font-weight: 500;
}

/* 页面标题区 */
.page-title-section {
  padding: 32px 0 16px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #333333;
}

/* 用户信息区 */
.user-info-section {
  padding: 16px 0 32px;
}

.user-info-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 32px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #f0f0f0;
  margin-right: 32px;
}

.user-details {
  flex: 1;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.user-name {
  font-size: 24px;
  font-weight: 700;
  color: #333333;
}

.member-badge {
  background: linear-gradient(135deg, #43A047 0%, #4CAF50 100%);
  color: #ffffff;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
}

.user-meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666666;
  margin-bottom: 10px;
}

.user-meta-item i {
  width: 16px;
  text-align: center;
  color: #4CAF50;
}

.user-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 16px;
}

.edit-profile-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #43A047 0%, #4CAF50 100%);
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.edit-profile-btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.last-login {
  font-size: 13px;
  color: #999999;
}

/* 功能入口区 */
.function-grid-section {
  padding: 32px 0;
  flex: 1;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.function-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 32px;
  height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.function-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
}

.function-icon-wrapper {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #43A047 0%, #4CAF50 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  position: relative;
}

.function-icon-wrapper i {
  font-size: 24px;
  color: #ffffff;
}

.cart-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 20px;
  height: 20px;
  background: #ff4d4f;
  color: #ffffff;
  font-size: 11px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.function-name {
  font-size: 16px;
  font-weight: 500;
  color: #333333;
}

/* 页脚区 */
.footer {
  padding: 32px 0 16px;
  background: #ffffff;
  margin-top: auto;
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 32px;
}

.footer-link {
  font-size: 14px;
  color: #666666;
  text-decoration: none;
  transition: all 0.2s ease;
  cursor: pointer;
}

.footer-link:hover {
  color: #4CAF50;
}

.copyright {
  font-size: 13px;
  color: #999999;
}

/* 编辑弹窗样式 */
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.preview-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

/* 响应式适配 */
@media (max-width: 1240px) {
  .container,
  .header-container {
    width: 100%;
    padding: 0 16px;
  }
}

@media (max-width: 768px) {
  .function-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .user-info-card {
    flex-direction: column;
    text-align: center;
  }
  
  .user-avatar {
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  .user-actions {
    align-items: center;
    margin-top: 16px;
  }
  
  .nav-menu {
    gap: 16px;
  }
  
  .nav-link span {
    display: none;
  }
}

/* 农户专区样式 */
.farmer-section {
  padding: 16px 0 40px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title i {
  color: #52c41a;
}

.section-desc {
  font-size: 14px;
  color: #999;
}

.farmer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.farmer-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.farmer-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  border-color: #52c41a;
}

.farmer-card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  background: #f6ffed;
  color: #52c41a;
  flex-shrink: 0;
}

.farmer-card-icon.shop-icon {
  background: #e6f7ff;
  color: #1890ff;
}

.farmer-card-icon.publish-icon {
  background: #fff7e6;
  color: #fa8c16;
}

.farmer-card-icon.products-icon {
  background: #f9f0ff;
  color: #722ed1;
}

.farmer-card-icon.stats-icon {
  background: #fff1f0;
  color: #f5222d;
}

.farmer-card-icon.message-icon {
  background: #e6fffb;
  color: #13c2c2;
}

.farmer-card-info {
  flex: 1;
}

.farmer-card-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px;
}

.farmer-card-info p {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.farmer-card-arrow {
  color: #ccc;
  font-size: 14px;
  flex-shrink: 0;
}

.farmer-card:hover .farmer-card-arrow {
  color: #52c41a;
}

.apply-card {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  border-color: #b7eb8f;
}

.apply-card .farmer-card-icon {
  background: #52c41a;
  color: #fff;
  width: 56px;
  height: 56px;
  font-size: 26px;
}

.apply-card:hover {
  border-color: #52c41a;
  background: linear-gradient(135deg, #d9f7be 0%, #b7eb8f 100%);
}
</style>
