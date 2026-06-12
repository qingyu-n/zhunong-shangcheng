<template>
  <div class="order-detail-page">
    <div class="container">
      <!-- 返回按钮 + 页面标题 -->
      <div class="page-header">
        <a href="#" class="back-btn" @click.prevent="goBack">
          <i class="fas fa-arrow-left"></i>
        </a>
        <h1 class="page-title">订单详情页</h1>
      </div>

      <div class="content-wrapper">
        <!-- 左侧内容区域 -->
        <div class="left-content">
          <!-- 订单基本信息 -->
          <div class="section-card">
            <div class="card-header">
              <h2 class="card-title">订单基本信息</h2>
              <div class="status-tag">{{ getStatusText(orderInfo.status) }}</div>
            </div>
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">订单编号</span>
                <span class="info-value">{{ orderInfo.orderNo || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">下单时间</span>
                <span class="info-value">{{ orderInfo.createTime || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">买家信息</span>
                <span class="info-value">{{ orderInfo.receiverName || '-' }} ({{ orderInfo.receiverPhone || '-' }})</span>
              </div>
              <div class="info-item">
                <span class="info-label">收货地址</span>
                <span class="info-value">{{ orderInfo.receiverAddress || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- 商品明细 -->
          <div class="section-card">
            <h2 class="card-title">商品明细</h2>
            <div class="product-list">
              <div v-for="item in orderItems" :key="item.id" class="product-item">
                <div class="product-image-wrapper">
                  <img :src="item.image" :alt="item.name" class="product-image" />
                </div>
                <div class="product-info">
                  <div class="product-name">{{ item.name }}</div>
                  <div class="product-spec">{{ item.spec }}</div>
                  <div class="product-footer">
                    <div class="product-price">¥{{ item.price }}</div>
                    <div class="product-quantity">
                      <span class="quantity-label">数量:</span>
                      <span class="quantity-value">{{ item.quantity }}</span>
                    </div>
                    <div class="product-subtotal">¥{{ item.subtotal }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 物流信息 -->
          <div class="section-card" v-if="orderInfo.status >= 2">
            <h2 class="card-title">物流信息</h2>
            <div class="logistics-header">
              <div class="logistics-item">
                <div class="logistics-label">
                  <i class="fas fa-truck logistics-icon warning"></i>
                  <span>物流公司</span>
                </div>
                <span class="logistics-value">{{ orderInfo.logisticsCompany || '暂无' }}</span>
              </div>
              <div class="logistics-item">
                <div class="logistics-label">
                  <i class="fas fa-barcode logistics-icon"></i>
                  <span>运单编号</span>
                </div>
                <span class="logistics-value">{{ orderInfo.trackingNumber || '暂无' }}</span>
              </div>
            </div>
            
            <div class="timeline">
              <div
                v-for="(node, index) in logisticsNodes"
                :key="index"
                class="timeline-item"
                :class="node.status"
              >
                <div class="timeline-dot">
                  <i v-if="node.status === 'completed'" class="fas fa-check"></i>
                  <i v-else-if="node.status === 'processing'" class="fas fa-spinner fa-spin"></i>
                  <div v-else class="dot-inner"></div>
                </div>
                <div class="timeline-content">
                  <div class="timeline-title">{{ node.title }}</div>
                  <div class="timeline-time">{{ node.time }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧内容区域 -->
        <div class="right-sidebar">
          <!-- 价格汇总 -->
          <div class="section-card">
            <h2 class="card-title">价格汇总</h2>
            <div class="price-list">
              <div class="price-item">
                <span class="price-label">商品小计</span>
                <span class="price-value">¥{{ formatPrice(orderInfo.totalAmount) }}</span>
              </div>
              <div class="price-item">
                <span class="price-label">运费</span>
                <span class="price-value">¥{{ formatPrice(orderInfo.freightAmount) }}</span>
              </div>
              <div class="price-item">
                <span class="price-label">优惠</span>
                <span class="price-value discount">-¥{{ formatPrice(orderInfo.discountAmount) }}</span>
              </div>
              <div class="price-divider"></div>
              <div class="price-item total">
                <span class="price-label">总计</span>
                <span class="price-value total-price">¥{{ formatPrice(orderInfo.payAmount) }}</span>
              </div>
            </div>
          </div>

          <!-- 支付信息 -->
          <div class="section-card">
            <h2 class="card-title">支付信息</h2>
            <div class="payment-info">
              <div class="payment-method">
                <div class="payment-icon">
                  <i class="fab fa-weixin"></i>
                </div>
                <div class="payment-detail">
                  <div class="payment-name">{{ getPaymentMethodName(orderInfo.payType) }}</div>
                  <div class="payment-time">支付时间: {{ orderInfo.payTime || orderInfo.updateTime || '-' }}</div>
                </div>
              </div>
              <div class="payment-amount">
                <span class="amount-label">支付金额</span>
                <span class="amount-value">¥{{ formatPrice(orderInfo.payAmount) }}</span>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="section-card action-card">
            <button class="action-btn refund" @click="applyRefund">
              <i class="fas fa-undo-alt"></i> 申请退款
            </button>
            <div class="action-row">
              <button class="action-btn secondary" @click="contactService">
                <i class="fas fa-headphones-alt"></i> 联系客服
              </button>
              <button class="action-btn secondary" @click="shareOrder">
                <i class="fas fa-share-alt"></i> 分享订单
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { orderApi } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 加载状态
const loading = ref(false)

// 订单信息
const orderInfo = ref({})

// 订单商品 - 从API获取
const orderItems = ref([])

// 物流节点 - 从API获取
const logisticsNodes = ref([])

// 获取订单详情
const fetchOrderDetail = async () => {
  const orderId = route.params.id
  if (!orderId) {
    ElMessage.error('订单ID不存在')
    return
  }

  loading.value = true
  try {
    console.log('获取订单详情，ID:', orderId)
    const res = await orderApi.getOrderDetail(orderId)
    console.log('订单详情数据:', res)

    // 设置订单基本信息
    orderInfo.value = res || {}

    // 设置订单商品
    if (res && res.items) {
      orderItems.value = res.items.map(item => ({
        id: item.id,
        name: item.productName || item.name,
        spec: item.spec || `规格：${item.quantity || 1}${item.unit || '件'}`,
        price: String(item.price || item.unitPrice || '0.00'),
        quantity: item.quantity || 1,
        subtotal: String((item.price || 0) * (item.quantity || 1)),
        image: item.image || item.mainImage || 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=200&h=200&fit=crop'
      }))
    }

    // 设置物流信息
    if (res && res.logistics) {
      logisticsNodes.value = res.logistics
    } else {
      // 默认物流节点（基于订单状态）
      const statusMap = {
        0: [{ title: '订单已创建，等待付款', time: res.createTime || new Date().toLocaleString(), status: 'processing' }],
        1: [{ title: '付款成功，商家正在准备发货', time: res.updateTime || new Date().toLocaleString(), status: 'processing' }],
        2: [
          { title: '商品已发货', time: res.updateTime || new Date().toLocaleString(), status: 'completed' },
          { title: '快递员正在派送中', time: '', status: 'processing' }
        ],
        3: [{ title: '订单已完成', time: res.updateTime || new Date().toLocaleString(), status: 'completed' }],
        4: [{ title: '订单已取消', time: res.updateTime || new Date().toLocaleString(), status: 'cancelled' }]
      }
      logisticsNodes.value = statusMap[res.status] || statusMap[0]
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('❌ 获取订单详情失败')
  } finally {
    loading.value = false
  }
}

// 导航方法
const navigateTo = (page) => {
  const routes = {
    home: '/',
    category: '/category',
    help: '/help',
    user: '/user',
    orders: '/orders',
    cart: '/cart'
  }
  const path = routes[page]
  if (path) {
    router.push(path)
  }
}

const goBack = () => {
  router.back()
}

const getStatusText = (status) => {
  const map = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消' }
  return map[status] || '未知状态'
}

const formatPrice = (value) => {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toFixed(2)
}

const getPaymentMethodName = (method) => {
  const map = { 1: '微信支付', 2: '支付宝', 3: '余额支付' }
  return map[method] || '在线支付'
}

const applyRefund = () => {
  console.log('申请退款')
}

const contactService = () => {
  console.log('联系客服')
}

const shareOrder = () => {
  console.log('分享订单')
}

// 页面加载时获取订单详情
onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 48px;
}

.container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px;
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.back-btn {
  width: 40px;
  height: 40px;
  background-color: #ffffff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333333;
  text-decoration: none;
  transition: background-color 0.2s;
}

.back-btn:hover {
  background-color: #f5f5f5;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  color: #333333;
  margin: 0;
}

/* 内容布局 */
.content-wrapper {
  display: flex;
  gap: 24px;
}

.left-content {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.right-sidebar {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 卡片样式 */
.section-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8e8e8;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #333333;
  margin: 0 0 16px 0;
}

.card-header .card-title {
  margin-bottom: 0;
}

.status-tag {
  width: 128px;
  height: 32px;
  background-color: rgba(64, 150, 255, 0.1);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4096ff;
  font-size: 14px;
  font-weight: 500;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 14px;
  color: #666666;
}

.info-value {
  font-size: 14px;
  color: #333333;
}

/* 商品列表 */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.product-item {
  display: flex;
  gap: 16px;
}

.product-image-wrapper {
  width: 96px;
  height: 96px;
  background-color: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #333333;
  margin-bottom: 8px;
}

.product-spec {
  font-size: 12px;
  color: #666666;
  margin-bottom: 12px;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 14px;
  color: #4096ff;
  font-weight: 500;
}

.product-quantity {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quantity-label {
  font-size: 14px;
  color: #666666;
}

.quantity-value {
  width: 32px;
  height: 20px;
  background-color: #f5f5f5;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #333333;
}

.product-subtotal {
  font-size: 14px;
  color: #333333;
  font-weight: 500;
}

/* 物流信息 */
.logistics-header {
  margin-bottom: 24px;
}

.logistics-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.logistics-item:last-child {
  margin-bottom: 0;
}

.logistics-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333333;
}

.logistics-icon {
  font-size: 14px;
  color: #666666;
}

.logistics-icon.warning {
  color: #faad14;
}

.logistics-value {
  font-size: 14px;
  color: #333333;
}

/* 时间线 */
.timeline {
  position: relative;
  padding-left: 32px;
  margin-left: 12px;
  border-left: 2px solid #e8e8e8;
}

.timeline-item {
  position: relative;
  padding-bottom: 32px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -43px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.timeline-item.completed .timeline-dot {
  background-color: #52c41a;
  color: #ffffff;
  font-size: 10px;
}

.timeline-item.processing .timeline-dot {
  background-color: #faad14;
  color: #ffffff;
  font-size: 10px;
}

.timeline-item.pending .timeline-dot {
  background-color: #e8e8e8;
}

.dot-inner {
  width: 8px;
  height: 8px;
  background-color: #666666;
  border-radius: 50%;
}

.timeline-content {
  margin-top: -4px;
}

.timeline-title {
  font-size: 14px;
  color: #333333;
  margin-bottom: 4px;
}

.timeline-item.pending .timeline-title {
  color: #666666;
}

.timeline-time {
  font-size: 12px;
  color: #666666;
}

/* 价格汇总 */
.price-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-label {
  font-size: 14px;
  color: #666666;
}

.price-value {
  font-size: 14px;
  color: #333333;
}

.price-value.discount {
  color: #f5222d;
}

.price-divider {
  border-top: 1px solid #e8e8e8;
  margin: 8px 0;
  padding-top: 12px;
}

.price-item.total {
  font-weight: bold;
}

.price-item.total .price-label {
  color: #333333;
}

.price-value.total-price {
  font-size: 18px;
  color: #4096ff;
}

/* 支付信息 */
.payment-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 12px;
}

.payment-icon {
  width: 40px;
  height: 40px;
  background-color: #f5f5f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.payment-icon i {
  font-size: 20px;
  color: #52c41a;
}

.payment-detail {
  flex: 1;
}

.payment-name {
  font-size: 14px;
  font-weight: 500;
  color: #333333;
  margin-bottom: 4px;
}

.payment-time {
  font-size: 12px;
  color: #666666;
}

.payment-amount {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #e8e8e8;
}

.amount-label {
  font-size: 14px;
  color: #666666;
}

.amount-value {
  font-size: 18px;
  font-weight: bold;
  color: #4096ff;
}

/* 操作按钮 */
.action-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.action-btn {
  width: 100%;
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
  border: none;
}

.action-btn.refund {
  background-color: #f5222d;
  color: #ffffff;
}

.action-btn.refund:hover {
  background-color: #ff4d4f;
}

.action-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-btn.secondary {
  background-color: #ffffff;
  border: 1px solid #e8e8e8;
  color: #333333;
}

.action-btn.secondary:hover {
  background-color: #f5f5f5;
}

/* 响应式 */
@media (max-width: 1024px) {
  .content-wrapper {
    flex-direction: column;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
