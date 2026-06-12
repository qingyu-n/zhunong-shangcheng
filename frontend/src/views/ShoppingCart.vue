<template>
  <div class="shopping-cart-page">
    <!-- 顶部导航栏 -->
    <nav class="top-nav">
      <div class="nav-container">
        <a href="#" class="logo" @click.prevent="goHome">
          <i class="fas fa-leaf logo-icon"></i>
          <span class="logo-text">农鲜达</span>
        </a>
        <div class="nav-links">
          <a href="#" class="nav-link" @click.prevent="goHome">
            <i class="fas fa-home"></i>
          </a>
          <a href="#" class="nav-link" @click.prevent="goCategory">
            <i class="fas fa-th-large"></i>
          </a>
          <a href="#" class="nav-link" @click.prevent="goUserCenter">
            <i class="fas fa-user"></i>
          </a>
        </div>
      </div>
    </nav>

    <!-- 页面标题 -->
    <div class="page-title-section">
      <div class="title-container">
        <h1 class="page-title">我的购物车</h1>
      </div>
    </div>

    <!-- 商品列表区域 -->
    <div class="cart-content">
      <div class="cart-container">
        <!-- 表头 -->
        <div class="cart-header">
          <div class="col-checkbox">
            <input
              type="checkbox"
              class="checkbox"
              :checked="isAllSelected"
              @change="toggleSelectAll"
            />
          </div>
          <div class="col-product">商品信息</div>
          <div class="col-price">单价</div>
          <div class="col-quantity">数量</div>
          <div class="col-subtotal">小计</div>
          <div class="col-action">操作</div>
        </div>

        <!-- 商品列表 -->
        <div class="cart-list">
          <!-- 空购物车提示 -->
          <div v-if="cartList.length === 0" class="empty-cart">
            <i class="fas fa-shopping-cart empty-icon"></i>
            <p class="empty-text">购物车是空的</p>
            <button class="go-shopping-btn" @click="goHome">去逛逛</button>
          </div>

          <div
            v-for="item in cartList"
            :key="item.id"
            class="cart-item"
          >
            <div class="col-checkbox">
              <input
                type="checkbox"
                class="checkbox"
                v-model="item.selected"
              />
            </div>
            <div class="col-product">
              <div class="product-info">
                <img :src="item.image" :alt="item.name" class="product-image" />
                <div class="product-detail">
                  <div class="product-name">{{ item.name }}</div>
                  <div class="product-origin">产地：{{ item.origin }}</div>
                </div>
              </div>
            </div>
            <div class="col-price">
              <span class="price">¥{{ item.price }}</span>
            </div>
            <div class="col-quantity">
              <div class="quantity-control">
                <button class="quantity-btn" @click="decreaseQuantity(item)">
                  <i class="fas fa-minus"></i>
                </button>
                <input
                  type="number"
                  class="quantity-input"
                  v-model.number="item.quantity"
                  min="1"
                  @change="validateQuantity(item)"
                />
                <button class="quantity-btn" @click="increaseQuantity(item)">
                  <i class="fas fa-plus"></i>
                </button>
              </div>
            </div>
            <div class="col-subtotal">
              <span class="subtotal">¥{{ calculateSubtotal(item) }}</span>
            </div>
            <div class="col-action">
              <button class="delete-btn" @click="removeItem(item.id)">
                <i class="fas fa-trash-alt"></i>
              </button>
            </div>
          </div>
        </div>

        <!-- 底部结算栏 -->
        <div class="cart-footer">
          <div class="checkout-left">
            <div class="select-all">
              <input
                type="checkbox"
                id="select-all"
                class="checkbox"
                :checked="isAllSelected"
                @change="toggleSelectAll"
              />
              <label for="select-all">全选</label>
            </div>
            <div class="selected-info">
              已选 <span class="selected-count">{{ selectedCount }}</span> 件商品
            </div>
          </div>
          <div class="checkout-right">
            <div class="total-section">
              <div class="total-label">合计：</div>
              <div class="total-price">¥{{ totalPrice }}</div>
            </div>
            <button class="checkout-btn" @click="checkout">结算</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

// 从store获取数据
const cartList = computed(() => cartStore.cartList)
const isAllSelected = computed(() => cartStore.isAllSelected)
const selectedCount = computed(() => cartStore.selectedCount)
const totalPrice = computed(() => cartStore.totalPrice.toFixed(2))

// 计算小计
const calculateSubtotal = (item) => {
  return (parseFloat(item.price) * item.quantity).toFixed(2)
}

// 全选/取消全选
const toggleSelectAll = () => {
  cartStore.toggleSelectAll()
}

// 减少数量
const decreaseQuantity = async (item) => {
  if (item.quantity > 1) {
    try {
      await cartStore.updateQuantity(item.id, item.quantity - 1)
    } catch (error) {
      console.error('更新数量失败:', error)
      ElMessage.error('更新数量失败')
      // 重新获取购物车数据以恢复正确状态
      cartStore.fetchCartList()
    }
  }
}

// 增加数量
const increaseQuantity = async (item) => {
  try {
    await cartStore.updateQuantity(item.id, item.quantity + 1)
  } catch (error) {
    console.error('更新数量失败:', error)
    ElMessage.error('更新数量失败')
    // 重新获取购物车数据以恢复正确状态
    cartStore.fetchCartList()
  }
}

// 验证数量
const validateQuantity = async (item) => {
  if (item.quantity < 1 || isNaN(item.quantity)) {
    item.quantity = 1
  }
  try {
    await cartStore.updateQuantity(item.id, item.quantity)
  } catch (error) {
    console.error('更新数量失败:', error)
    ElMessage.error('更新数量失败')
    cartStore.fetchCartList()
  }
}

// 删除商品
const removeItem = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该商品吗？',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await cartStore.removeItem(id)
    ElMessage.success('删除成功')
  } catch (error) {
    // 用户取消删除或删除失败
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

// 结算
const checkout = () => {
  if (selectedCount.value === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  router.push('/order/confirm')
}

// 导航方法
const goHome = () => {
  router.push('/')
}

const goCategory = () => {
  router.push('/category')
}

const goUserCenter = () => {
  router.push('/user')
}

// 页面加载时获取购物车数据
onMounted(async () => {
  try {
    await cartStore.fetchCartList()
  } catch (error) {
    console.error('加载购物车失败:', error)
    // 401错误已在request.js中统一处理并跳转登录
    if (error.response?.status !== 401) {
      ElMessage.error('加载购物车数据失败，请刷新重试')
    }
  }
})
</script>

<style scoped>
.shopping-cart-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
  padding-bottom: 80px;
}

/* 顶部导航栏 */
.top-nav {
  width: 100%;
  background-color: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  text-decoration: none;
  gap: 8px;
}

.logo-icon {
  color: #4096ff;
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.nav-links {
  display: flex;
  gap: 20px;
}

.nav-link {
  color: #666;
  font-size: 20px;
  text-decoration: none;
  transition: color 0.3s;
}

.nav-link:hover {
  color: #4096ff;
}

/* 页面标题 */
.page-title-section {
  width: 100%;
  background-color: #ffffff;
  padding: 32px 0;
}

.title-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

/* 购物车内容 */
.cart-content {
  flex: 1;
  padding: 24px 0;
}

.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

/* 表头 */
.cart-header {
  display: grid;
  grid-template-columns: 60px 1fr 120px 160px 120px 80px;
  gap: 16px;
  padding: 20px 24px;
  background-color: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  font-weight: 500;
  color: #333;
}

/* 购物车列表 */
.cart-list {
  padding: 16px 0;
}

.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #999;
}

.empty-cart .empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #ddd;
}

.empty-cart .empty-text {
  font-size: 16px;
  margin-bottom: 24px;
}

.go-shopping-btn {
  padding: 10px 32px;
  background: linear-gradient(135deg, #43A047 0%, #4CAF50 100%);
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.go-shopping-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(67, 160, 71, 0.3);
}

.cart-item {
  display: grid;
  grid-template-columns: 60px 1fr 120px 160px 120px 80px;
  gap: 16px;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
}

.cart-item:last-child {
  border-bottom: none;
}

.col-checkbox {
  display: flex;
  justify-content: center;
}

.checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.product-info {
  display: flex;
  gap: 16px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.product-detail {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.product-origin {
  font-size: 14px;
  color: #999;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #ff4d4f;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.quantity-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #d9d9d9;
  background-color: #ffffff;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.quantity-btn:hover {
  border-color: #4096ff;
  color: #4096ff;
}

.quantity-input {
  width: 60px;
  height: 32px;
  text-align: center;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

.subtotal {
  font-size: 18px;
  font-weight: 600;
  color: #ff4d4f;
}

.delete-btn {
  width: 36px;
  height: 36px;
  border: none;
  background-color: transparent;
  color: #999;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-btn:hover {
  background-color: #fff1f0;
  color: #ff4d4f;
}

/* 底部结算栏 */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background-color: #ffffff;
  border-top: 1px solid #e8e8e8;
  position: sticky;
  bottom: 0;
}

.checkout-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 8px;
}

.select-all label {
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.selected-info {
  font-size: 14px;
  color: #666;
}

.selected-count {
  font-size: 16px;
  font-weight: 600;
  color: #4096ff;
}

.checkout-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.total-section {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.total-label {
  font-size: 16px;
  color: #333;
}

.total-price {
  font-size: 24px;
  font-weight: 700;
  color: #ff4d4f;
}

.checkout-btn {
  padding: 12px 40px;
  background: linear-gradient(135deg, #43A047 0%, #4CAF50 100%);
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.checkout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(67, 160, 71, 0.3);
}

/* 响应式适配 */
@media (max-width: 1240px) {
  .nav-container,
  .title-container,
  .cart-container {
    max-width: 100%;
    padding: 0 16px;
  }
}

@media (max-width: 768px) {
  .cart-header,
  .cart-item {
    grid-template-columns: 40px 1fr 100px 120px 100px 60px;
    gap: 8px;
    padding: 12px 16px;
  }

  .product-image {
    width: 60px;
    height: 60px;
  }

  .product-name {
    font-size: 14px;
  }

  .price,
  .subtotal {
    font-size: 16px;
  }

  .checkout-right {
    gap: 16px;
  }

  .total-price {
    font-size: 20px;
  }

  .checkout-btn {
    padding: 10px 24px;
    font-size: 14px;
  }
}
</style>
