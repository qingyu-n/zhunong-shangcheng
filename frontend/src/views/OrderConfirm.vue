<template>
  <div class="order-confirm-page">
    <div class="container">
      <!-- 面包屑导航 -->
      <div class="breadcrumb-section">
        <div class="breadcrumb">
          <a href="#" class="breadcrumb-item" @click.prevent="goHome">
            <i class="fas fa-home"></i>
          </a>
          <i class="fas fa-angle-right breadcrumb-separator"></i>
          <a href="#" class="breadcrumb-item" @click.prevent="goCart">购物车</a>
          <i class="fas fa-angle-right breadcrumb-separator"></i>
          <span class="breadcrumb-item active">订单确认</span>
        </div>
        <h1 class="page-title">订单确认</h1>
      </div>

      <div class="content-wrapper">
        <!-- 左侧内容区 -->
        <div class="left-content">
          <!-- 商品信息区 -->
          <div class="section-card">
            <h2 class="section-title">商品信息</h2>
            <div class="product-list">
              <div v-for="item in orderItems" :key="item.id" class="product-item">
                <div class="product-image-wrapper">
                  <img :src="item.image" :alt="item.name" class="product-image" />
                </div>
                <div class="product-info">
                  <h3 class="product-name">{{ item.name }}</h3>
                  <p class="product-origin">产地：{{ item.origin }}</p>
                </div>
                <div class="product-price-info">
                  <p class="product-price">¥{{ item.price }}</p>
                  <p class="product-quantity">x{{ item.quantity }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 收货地址区 -->
          <div class="section-card">
            <div class="section-header">
              <h2 class="section-title">收货地址</h2>
              <a href="#" class="edit-link" @click.prevent="editAddress">
                <i class="fas fa-edit"></i> 修改
              </a>
            </div>
            <div class="address-list">
              <div
                v-for="address in addresses"
                :key="address.id"
                class="address-item"
                :class="{ active: address.id === selectedAddressId }"
                @click="selectAddress(address.id)"
              >
                <span v-if="address.isDefault" class="default-tag">默认</span>
                <div class="address-user">{{ address.name }} {{ address.phone }}</div>
                <div class="address-detail">{{ address.detail || (address.province + address.city + address.district + address.address) }}</div>
                <div class="address-zip" v-if="address.zipCode">邮政编码: {{ address.zipCode }}</div>
              </div>
            </div>
            <button class="add-address-btn" @click="addAddress">
              <i class="fas fa-plus"></i> 新增地址
            </button>
          </div>

          <!-- 支付方式区 -->
          <div class="section-card">
            <h2 class="section-title">支付方式</h2>
            <div class="payment-list">
              <label
                v-for="payment in paymentMethods"
                :key="payment.id"
                class="payment-item"
                :class="{ active: selectedPayment === payment.id }"
              >
                <input
                  type="radio"
                  name="payment"
                  :value="payment.id"
                  v-model="selectedPayment"
                  class="payment-radio"
                />
                <i :class="payment.icon" :style="{ color: payment.color }"></i>
                <span class="payment-name">{{ payment.name }}</span>
              </label>
            </div>
          </div>

          <!-- 优惠券区 -->
          <div class="section-card">
            <div class="coupon-header">
              <h2 class="section-title">优惠券</h2>
              <div class="coupon-select-wrapper">
                <select v-model="selectedCoupon" class="coupon-select">
                  <option value="">请选择优惠券</option>
                  <option v-for="coupon in coupons" :key="coupon.id" :value="coupon.id">
                    {{ coupon.name }}
                  </option>
                </select>
                <i class="fas fa-chevron-down coupon-arrow"></i>
              </div>
            </div>
            <div v-if="discountAmount > 0" class="discount-info">
              <i class="fas fa-check-circle"></i>
              <span>已优惠 ¥{{ discountAmount }}</span>
            </div>
          </div>
        </div>

        <!-- 右侧订单金额区 -->
        <div class="right-sidebar">
          <div class="amount-card">
            <h2 class="section-title">订单金额</h2>
            <div class="amount-list">
              <div class="amount-item">
                <span class="amount-label">商品总价</span>
                <span class="amount-value">¥{{ totalPrice }}</span>
              </div>
              <div class="amount-item">
                <span class="amount-label">运费</span>
                <span class="amount-value">¥{{ shippingFee }}</span>
              </div>
              <div class="amount-item">
                <span class="amount-label">优惠</span>
                <span class="amount-value discount">-¥{{ discountAmount }}</span>
              </div>
              <div class="amount-divider"></div>
              <div class="amount-item total">
                <span class="amount-label">实付款</span>
                <span class="amount-value total-price">¥{{ finalPrice }}</span>
              </div>
            </div>
            <div class="order-tip">
              <i class="fas fa-info-circle"></i>
              <span>订单提交后将为您保留15分钟，请及时付款</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部提交订单按钮 -->
    <div class="bottom-bar">
      <div class="bottom-container">
        <button class="submit-order-btn" @click="submitOrder" :disabled="loading">
          <i class="fas fa-check-circle"></i> {{ loading ? '提交中...' : '提交订单' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { addressApi, orderApi } from '../api'
import { ElMessage } from 'element-plus'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()

// 加载状态
const loading = ref(false)

// 订单商品 - 从购物车获取或路由参数
const orderItems = ref([])

// 收货地址 - 从API获取
const addresses = ref([])
const selectedAddressId = ref(null)

// 支付方式
const paymentMethods = ref([
  { id: 'wechat', name: '微信支付', icon: 'fab fa-weixin', color: '#52c41a' },
  { id: 'alipay', name: '支付宝', icon: 'fab fa-alipay', color: '#4096ff' },
  { id: 'card', name: '银行卡支付', icon: 'fas fa-credit-card', color: '#666666' }
])

const selectedPayment = ref('wechat')

// 优惠券（暂时使用静态数据）
const coupons = ref([
  { id: 1, name: '满100减10元优惠券', amount: 10 },
  { id: 2, name: '满200减25元优惠券', amount: 25 },
  { id: 3, name: '满300减40元优惠券', amount: 40 }
])

const selectedCoupon = ref('')

// 获取收货地址列表
const fetchAddresses = async () => {
  try {
    const res = await addressApi.getAddressList()
    addresses.value = res || []
    // 如果有默认地址，自动选中
    const defaultAddr = addresses.value.find(addr => addr.isDefault)
    if (defaultAddr) {
      selectedAddressId.value = defaultAddr.id
    } else if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].id
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
    ElMessage.error('获取地址失败')
  }
}

// 初始化订单商品
const initOrderItems = () => {
  // 从购物车获取选中的商品
  const selectedItems = cartStore.getSelectedItems()
  if (selectedItems.length > 0) {
    orderItems.value = selectedItems.map(item => ({
      id: item.id,
      productId: item.productId,
      name: item.name,
      origin: item.origin || '',
      price: item.price,
      quantity: item.quantity,
      image: item.image || item.mainImage
    }))
  }

  // 如果没有从购物车获取到，尝试从路由参数获取
  if (orderItems.value.length === 0 && route.query.productId) {
    // 单个商品购买模式
    const productData = JSON.parse(route.query.productData || '{}')
    if (productData.id) {
      orderItems.value = [{
        id: Date.now(),
        productId: productData.id,
        name: productData.name,
        origin: '',
        price: productData.price,
        quantity: parseInt(route.query.quantity) || 1,
        image: productData.mainImage || productData.image
      }]
    }
  }
}

// 计算金额
const totalPrice = computed(() => {
  return orderItems.value.reduce((sum, item) => {
    return sum + (parseFloat(item.price) * item.quantity)
  }, 0).toFixed(2)
})

const shippingFee = ref('8.00')

const discountAmount = computed(() => {
  if (!selectedCoupon.value) return '0.00'
  const coupon = coupons.value.find(c => c.id === selectedCoupon.value)
  return coupon ? coupon.amount.toFixed(2) : '0.00'
})

const finalPrice = computed(() => {
  const total = parseFloat(totalPrice.value)
  const shipping = parseFloat(shippingFee.value)
  const discount = parseFloat(discountAmount.value)
  return (total + shipping - discount).toFixed(2)
})

// 导航方法
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

const goHome = () => {
  router.push('/')
}

const goCart = () => {
  router.push('/cart')
}

const editAddress = () => {
  router.push('/address')
}

const goToAddressManage = () => {
  router.push('/address')
}

const selectAddress = (id) => {
  selectedAddressId.value = id
}

const addAddress = () => {
  router.push('/address')
}

const submitOrder = async () => {
  // 验证
  if (orderItems.value.length === 0) {
    ElMessage.warning('订单中没有商品')
    return
  }

  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  if (!selectedPayment.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  loading.value = true
  try {
    const selectedAddr = addresses.value.find(addr => addr.id === selectedAddressId.value)
    
    const orderData = {
      addressId: selectedAddressId.value,
      paymentMethod: selectedPayment.value,
      couponId: selectedCoupon.value || null,
      items: orderItems.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      })),
      remark: ''
    }

    const res = await orderApi.createOrder(orderData)
    ElMessage.success('订单创建成功')

    // 只删除购物车中已下单的商品，保留未下单的商品
    const orderedCartItemIds = orderItems.value.map(item => item.id)
    if (orderedCartItemIds.length > 0) {
      await cartStore.batchRemove(orderedCartItemIds)
    }

    // 跳转到支付成功页或订单详情页
    if (res.orderNo || res.id) {
      const params = new URLSearchParams()
      if (res.orderNo) params.set('orderNo', res.orderNo)
      if (res.id) params.set('orderId', res.id)
      router.push(`/order/success?${params.toString()}`)
    } else {
      router.push('/order/success')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error(error.message || '创建订单失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时初始化数据
onMounted(() => {
  initOrderItems()
  fetchAddresses()
})
</script>

<style scoped>
.order-confirm-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 80px;
}

.container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px;
}

/* 面包屑导航 */
.breadcrumb-section {
  margin-bottom: 24px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.breadcrumb-item {
  color: #666666;
  text-decoration: none;
  font-size: 14px;
}

.breadcrumb-item.active {
  color: #333333;
}

.breadcrumb-separator {
  color: #666666;
  font-size: 12px;
}

.page-title {
  font-size: 24px;
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
  flex: 7;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.right-sidebar {
  flex: 3;
}

/* 卡片样式 */
.section-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333333;
  margin: 0 0 16px 0;
}

.section-header .section-title {
  margin-bottom: 0;
}

/* 商品列表 */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.product-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
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
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  color: #333333;
  margin: 0 0 8px 0;
}

.product-origin {
  font-size: 14px;
  color: #666666;
  margin: 0;
}

.product-price-info {
  text-align: right;
}

.product-price {
  font-size: 16px;
  font-weight: 500;
  color: #333333;
  margin: 0 0 4px 0;
}

.product-quantity {
  font-size: 14px;
  color: #666666;
  margin: 0;
}

/* 编辑链接 */
.edit-link {
  color: #4096ff;
  font-size: 14px;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 4px;
}

.edit-link:hover {
  color: #1677ff;
}

/* 地址列表 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.address-item {
  position: relative;
  padding: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.address-item:hover {
  border-color: #4096ff;
}

.address-item.active {
  border-color: #4096ff;
  background-color: rgba(64, 150, 255, 0.05);
}

.default-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background-color: #4096ff;
  color: #ffffff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.address-user {
  font-size: 16px;
  font-weight: bold;
  color: #333333;
  margin-bottom: 8px;
}

.address-detail {
  font-size: 14px;
  color: #666666;
  margin-bottom: 4px;
}

.address-zip {
  font-size: 14px;
  color: #666666;
}

/* 新增地址按钮 */
.add-address-btn {
  width: 100%;
  padding: 12px;
  border: 1px dashed #666666;
  border-radius: 8px;
  background-color: transparent;
  color: #666666;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}

.add-address-btn:hover {
  border-color: #4096ff;
  color: #4096ff;
}

/* 支付方式 */
.payment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.payment-item:hover {
  border-color: #4096ff;
}

.payment-item.active {
  border-color: #4096ff;
  background-color: rgba(64, 150, 255, 0.05);
}

.payment-radio {
  margin-right: 12px;
  accent-color: #4096ff;
}

.payment-item i {
  font-size: 20px;
  margin-right: 12px;
}

.payment-name {
  font-size: 14px;
  color: #333333;
}

/* 优惠券 */
.coupon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.coupon-header .section-title {
  margin-bottom: 0;
}

.coupon-select-wrapper {
  position: relative;
  width: 260px;
}

.coupon-select {
  width: 100%;
  padding: 10px 36px 10px 12px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  font-size: 14px;
  color: #333333;
  background-color: #ffffff;
  appearance: none;
  cursor: pointer;
  outline: none;
}

.coupon-select:focus {
  border-color: #4096ff;
}

.coupon-arrow {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #666666;
  font-size: 12px;
  pointer-events: none;
}

.discount-info {
  margin-top: 12px;
  color: #52c41a;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 订单金额卡片 */
.amount-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 24px;
}

.amount-list {
  margin-bottom: 24px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.amount-label {
  font-size: 14px;
  color: #666666;
}

.amount-value {
  font-size: 14px;
  color: #333333;
}

.amount-value.discount {
  color: #52c41a;
}

.amount-divider {
  border-top: 1px solid #e8e8e8;
  margin: 16px 0;
}

.amount-item.total {
  margin-bottom: 0;
}

.amount-item.total .amount-label {
  font-weight: bold;
  color: #333333;
}

.amount-value.total-price {
  font-size: 18px;
  font-weight: bold;
  color: #333333;
}

.order-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
  color: #666666;
}

.order-tip i {
  color: #4096ff;
  margin-top: 2px;
}

/* 底部提交栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  border-top: 1px solid #e8e8e8;
  padding: 16px 0;
  z-index: 100;
}

.bottom-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 24px;
}

.submit-order-btn {
  width: 100%;
  padding: 14px;
  background-color: #4096ff;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background-color 0.2s;
}

.submit-order-btn:hover {
  background-color: #1677ff;
}

/* 响应式 */
@media (max-width: 1024px) {
  .content-wrapper {
    flex-direction: column;
  }
  
  .right-sidebar {
    order: -1;
  }
  
  .amount-card {
    position: static;
  }
}
</style>
