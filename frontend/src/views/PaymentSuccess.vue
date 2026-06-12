<template>
  <div class="payment-success-page">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="container">
        <a class="logo" @click="navigateTo('home')">
          <i class="fas fa-leaf logo-icon"></i>
          <span class="logo-text">农鲜达</span>
        </a>
        <div class="nav-links">
          <a class="nav-link" @click="navigateTo('home')">首页</a>
          <a class="nav-link" @click="navigateTo('products')">商品</a>
          <a class="nav-link" @click="navigateTo('user')">我的</a>
        </div>
      </div>
    </nav>

    <!-- 成功提示卡片区域 -->
    <main class="main-content">
      <div class="success-card">
        <!-- 成功图标 -->
        <div class="success-icon-wrapper">
          <div class="success-icon">
            <i class="fas fa-check"></i>
          </div>
        </div>
        
        <!-- 标题 -->
        <div class="success-header">
          <h1 class="success-title">支付成功</h1>
          <p class="success-subtitle">感谢您的购买，您的订单已确认</p>
        </div>
        
        <!-- 订单信息 -->
        <div class="order-info" v-loading="loading">
          <div class="info-row">
            <span class="info-label">订单编号</span>
            <span class="info-value">{{ orderInfo.orderNo || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">支付金额</span>
            <span class="info-value price">¥{{ orderInfo.payAmount || orderInfo.totalAmount || '0.00' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">支付时间</span>
            <span class="info-value time">{{ orderInfo.createTime || '-' }}</span>
          </div>
          <div class="info-row" v-if="orderInfo.receiverName">
            <span class="info-label">收货人</span>
            <span class="info-value">{{ orderInfo.receiverName }}</span>
          </div>
        </div>
        
        <!-- 按钮组 -->
        <div class="action-buttons">
          <button class="btn btn-primary" @click="navigateTo('orders')">
            查看订单
          </button>
          <button class="btn btn-outline" @click="navigateTo('home')">
            返回首页
          </button>
        </div>
      </div>
    </main>

    <!-- 底部信息栏 -->
    <footer class="footer">
      <div class="container">
        <p class="footer-text">如有任何问题，请联系客服热线：400-123-4567</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { orderApi } from '../api'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const orderInfo = reactive({
  orderNo: '',
  payAmount: '',
  totalAmount: '',
  createTime: '',
  receiverName: ''
})

const fetchOrderInfo = async () => {
  const orderNo = route.query.orderNo
  const orderId = route.query.orderId

  if (orderNo) {
    orderInfo.orderNo = orderNo
  }

  if (orderId) {
    loading.value = true
    try {
      const res = await orderApi.getOrderDetail(orderId)
      if (res) {
        Object.assign(orderInfo, {
          orderNo: res.orderNo || orderNo || '',
          payAmount: res.payAmount || res.totalAmount || '0.00',
          totalAmount: res.totalAmount || '0.00',
          createTime: res.createTime || res.payTime || '',
          receiverName: res.receiverName || ''
        })
      }
    } catch (error) {
      console.error('获取订单详情失败:', error)
    } finally {
      loading.value = false
    }
  } else if (orderNo) {
    try {
      const res = await orderApi.getOrderList({ page: 1, size: 10 })
      const list = res?.records || res?.list || (Array.isArray(res) ? res : [])
      const order = list.find(o => o.orderNo === orderNo)
      if (order) {
        Object.assign(orderInfo, {
          orderNo: order.orderNo,
          payAmount: order.payAmount || order.totalAmount,
          totalAmount: order.totalAmount,
          createTime: order.createTime || order.payTime,
          receiverName: order.receiverName || ''
        })
      }
    } catch (error) {
      console.error('获取订单列表失败:', error)
    }
  }
}

const navigateTo = (page) => {
  const routeMap = {
    home: '/',
    products: '/products',
    user: '/user',
    orders: '/orders'
  }
  const path = routeMap[page]
  if (path) {
    router.push(path)
  }
}

onMounted(() => {
  fetchOrderInfo()
})
</script>

<style scoped>
/* 基础样式 */
.payment-success-page {
  min-height: 100vh;
  background-color: #fff;
  font-family: 'Alibaba PuHuiTi', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.navbar {
  width: 100%;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fff;
}

.navbar .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
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
  color: #4CAF50;
  font-size: 24px;
}

.logo-text {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-link {
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: color 0.3s;
}

.nav-link:hover {
  color: #4CAF50;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

/* 成功卡片 */
.success-card {
  width: 100%;
  max-width: 480px;
  background-color: #f5f5f5;
  border-radius: 12px;
  padding: 48px 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 成功图标 */
.success-icon-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.success-icon {
  width: 72px;
  height: 72px;
  background-color: #4CAF50;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-icon i {
  color: #fff;
  font-size: 32px;
}

/* 成功标题 */
.success-header {
  text-align: center;
  margin-bottom: 32px;
}

.success-title {
  font-size: 26px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.success-subtitle {
  font-size: 14px;
  color: #666;
}

/* 订单信息 */
.order-info {
  background-color: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 32px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.info-row:not(:last-child) {
  border-bottom: 1px solid #f5f5f5;
}

.info-label {
  font-size: 14px;
  color: #666;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.info-value.price {
  color: #4CAF50;
  font-weight: 600;
}

.info-value.time {
  font-size: 13px;
  color: #666;
}

/* 按钮组 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.btn {
  width: 140px;
  height: 44px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}

.btn-primary {
  background-color: #4CAF50;
  color: #fff;
}

.btn-primary:hover {
  background-color: #2E7D32;
}

.btn-outline {
  background-color: #fff;
  color: #4CAF50;
  border: 1px solid #4CAF50;
}

.btn-outline:hover {
  background-color: #f5f5f5;
}

/* 页脚 */
.footer {
  padding: 20px 24px;
  border-top: 1px solid #f0f0f0;
  background-color: #fff;
}

.footer .container {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.footer-text {
  font-size: 13px;
  color: #666;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .success-card {
    padding: 36px 24px;
  }
  
  .success-title {
    font-size: 22px;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .btn {
    width: 100%;
    max-width: 200px;
  }
}
</style>
