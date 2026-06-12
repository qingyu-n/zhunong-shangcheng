<template>
  <div class="special-offer-page">
    <header class="offer-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="container">
          <div class="header-text">
            <h1 class="main-title">
              <i class="fas fa-leaf leaf-icon"></i>
              限时特惠
            </h1>
            <p class="sub-title">助农优选 · 新鲜直达</p>
          </div>
          <div class="countdown-wrapper" v-if="latestEndTime">
            <span class="countdown-label">距结束还剩</span>
            <div class="countdown-box">
              <span class="countdown-item">{{ padZero(countdown.days) }}</span>
              <span class="countdown-separator">:</span>
              <span class="countdown-item">{{ padZero(countdown.hours) }}</span>
              <span class="countdown-separator">:</span>
              <span class="countdown-item">{{ padZero(countdown.minutes) }}</span>
              <span class="countdown-separator">:</span>
              <span class="countdown-item">{{ padZero(countdown.seconds) }}</span>
            </div>
            <div class="countdown-unit">
              <span>天</span><span>时</span><span>分</span><span>秒</span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <section class="products-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">
            <i class="fas fa-seedling title-icon"></i>
            特惠商品
          </h2>
          <span class="product-count" v-if="specialProducts.length > 0">
            共 {{ specialProducts.length }} 件
          </span>
        </div>

        <div class="products-grid" v-loading="loading">
          <div
            v-for="item in specialProducts"
            :key="item.id"
            class="product-card"
            :class="{ 'is-expired': isExpired(item) }"
            @click="goToProductDetail(item.productId || item.id)"
          >
            <div class="product-image-wrapper">
              <img :src="item.image || item.mainImage" :alt="item.name" class="product-image" />
              <div v-if="item.discountPercent" class="discount-badge">
                {{ item.discountPercent }}折
              </div>
              <div v-if="item.endTime && !isExpired(item)" class="item-countdown">
                <i class="fas fa-clock"></i>
                <span>{{ getItemCountdown(item.endTime) }}</span>
              </div>
              <div v-if="isExpired(item)" class="expired-overlay">
                <span>已结束</span>
              </div>
            </div>

            <div class="product-info">
              <h3 class="product-name">{{ item.name }}</h3>
              <div class="price-area">
                <span class="original-price">¥{{ item.originalPrice }}</span>
                <span class="special-price">¥{{ item.discountPrice }}</span>
              </div>
              <div class="save-tag" v-if="item.originalPrice && item.discountPrice">
                省¥{{ (item.originalPrice - item.discountPrice).toFixed(2) }}
              </div>
              <button
                class="buy-btn"
                @click="handleBuyNow(item)"
                :disabled="isExpired(item)"
              >
                <i class="fas fa-shopping-cart"></i>
                {{ isExpired(item) ? '已结束' : '立即抢购' }}
              </button>
            </div>
          </div>

          <div v-if="!loading && specialProducts.length === 0" class="empty-state">
            <i class="fas fa-leaf empty-icon"></i>
            <p class="empty-text">暂无特惠活动，敬请期待~</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { productApi, cartApi } from '@/api/index'

const router = useRouter()

const loading = ref(false)
const specialProducts = ref([])
let countdownTimer = null

const countdown = reactive({
  days: 0,
  hours: 0,
  minutes: 0,
  seconds: 0
})

const latestEndTime = computed(() => {
  if (specialProducts.value.length === 0) return null
  const activeItems = specialProducts.value.filter(item => !isExpired(item) && item.endTime)
  if (activeItems.length === 0) return null
  const endTimes = activeItems.map(item => new Date(item.endTime).getTime())
  return Math.min(...endTimes)
})

const padZero = (n) => String(n).padStart(2, '0')

const fetchSpecialOffers = async () => {
  loading.value = true
  try {
    const res = await productApi.getSpecialOffers()
    if (Array.isArray(res)) {
      specialProducts.value = res
    } else if (res) {
      specialProducts.value = res.list || res.records || []
    } else {
      specialProducts.value = []
    }
  } catch (error) {
    console.error('获取特惠商品失败:', error)
    specialProducts.value = []
  } finally {
    loading.value = false
  }
}

const calculateCountdown = () => {
  if (!latestEndTime.value) return
  const now = new Date().getTime()
  const distance = latestEndTime.value - now

  if (distance > 0) {
    countdown.days = Math.floor(distance / (1000 * 60 * 60 * 24))
    countdown.hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    countdown.minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60))
    countdown.seconds = Math.floor((distance % (1000 * 60)) / 1000)
  } else {
    countdown.days = 0
    countdown.hours = 0
    countdown.minutes = 0
    countdown.seconds = 0
  }
}

const getItemCountdown = (endTimeStr) => {
  if (!endTimeStr) return ''
  const end = new Date(endTimeStr).getTime()
  const now = new Date().getTime()
  const distance = end - now

  if (distance <= 0) return '已结束'

  const hours = Math.floor(distance / (1000 * 60 * 60))
  const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((distance % (1000 * 60)) / 1000)

  if (hours > 0) return `${hours}时${minutes}分`
  if (minutes > 0) return `${minutes}分${seconds}秒`
  return `${seconds}秒`
}

const isExpired = (item) => {
  if (!item.endTime) return false
  return new Date(item.endTime).getTime() <= new Date().getTime()
}

const handleBuyNow = async (item) => {
  try {
    await cartApi.addToCart({ productId: item.id, quantity: 1 })
    alert(`已将 "${item.name}" 加入购物车`)
  } catch (error) {
    console.error('加入购物车失败:', error)
    alert('操作失败，请重试')
  }
}

const startCountdown = () => {
  calculateCountdown()
  countdownTimer = setInterval(calculateCountdown, 1000)
}

const stopCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const goToProductDetail = (productId) => {
  router.push('/product/' + productId)
}

onMounted(() => {
  fetchSpecialOffers()
  startCountdown()
})

onUnmounted(() => {
  stopCountdown()
})
</script>

<style scoped>
.special-offer-page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #f8faf8 0%, #f0f7f0 100%);
}

.container {
  width: 1200px;
  max-width: 100%;
  margin: 0 auto;
  padding: 0 16px;
}

.offer-header {
  position: relative;
  width: 100%;
  height: 280px;
  overflow: hidden;
}

.header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 40%, #95de64 100%);
}

.header-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 15% 85%, rgba(255, 255, 255, 0.15) 0%, transparent 45%),
    radial-gradient(circle at 85% 15%, rgba(255, 255, 255, 0.1) 0%, transparent 35%);
}

.header-bg::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: linear-gradient(180deg, transparent 0%, rgba(248, 250, 248, 0.8) 100%);
}

.header-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}

.header-text {
  text-align: center;
  margin-bottom: 24px;
}

.main-title {
  font-size: 44px;
  font-weight: 800;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  text-shadow: 0 3px 10px rgba(82, 196, 26, 0.3);
}

.leaf-icon {
  font-size: 40px;
  animation: sway 2s ease-in-out infinite;
}

@keyframes sway {
  0%, 100% { transform: rotate(-5deg); }
  50% { transform: rotate(5deg); }
}

.sub-title {
  font-size: 18px;
  font-weight: 500;
  opacity: 0.95;
  letter-spacing: 8px;
}

.countdown-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.countdown-label {
  font-size: 14px;
  opacity: 0.9;
  letter-spacing: 2px;
}

.countdown-box {
  display: flex;
  align-items: center;
  gap: 6px;
}

.countdown-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 52px;
  height: 58px;
  padding: 0 10px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  border-radius: 10px;
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
  box-shadow: 0 3px 10px rgba(82, 196, 26, 0.2);
}

.countdown-separator {
  font-size: 28px;
  font-weight: 700;
  opacity: 0.8;
}

.countdown-unit {
  display: flex;
  gap: 18px;
  font-size: 12px;
  opacity: 0.85;
}

.products-section {
  padding: 40px 0 60px;
  min-height: 400px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #333333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  color: #52c41a;
  font-size: 20px;
}

.product-count {
  font-size: 14px;
  color: #999999;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(82, 196, 26, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid rgba(82, 196, 26, 0.1);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(82, 196, 26, 0.15);
}

.product-card.is-expired {
  opacity: 0.65;
}

.product-card.is-expired:hover {
  transform: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  background: #f5f5f5;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.06);
}

.discount-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  background: linear-gradient(135deg, #52c41a, #73d13d);
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(82, 196, 26, 0.35);
  z-index: 2;
}

.item-countdown {
  position: absolute;
  bottom: 10px;
  right: 10px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(6px);
  border-radius: 16px;
  color: #ffffff;
  font-size: 12px;
  font-weight: 500;
  z-index: 2;
}

.item-countdown i {
  color: #95de64;
  font-size: 12px;
}

.expired-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}

.expired-overlay span {
  padding: 6px 20px;
  background: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  border-radius: 4px;
  letter-spacing: 4px;
}

.product-info {
  padding: 14px 16px 16px;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 10px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.price-area {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 8px;
}

.original-price {
  font-size: 13px;
  color: #bbbbbb;
  text-decoration: line-through;
}

.special-price {
  font-size: 24px;
  font-weight: 800;
  color: #52c41a;
}

.save-tag {
  display: inline-block;
  padding: 2px 8px;
  background: #f6ffed;
  color: #52c41a;
  font-size: 12px;
  font-weight: 500;
  border-radius: 4px;
  margin-bottom: 12px;
  border: 1px solid #b7eb8f;
}

.buy-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #52c41a, #73d13d);
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.buy-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #389e0d, #52c41a);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.35);
}

.buy-btn:disabled {
  background: #cccccc;
  cursor: not-allowed;
  transform: none;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  color: #95de64;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #999;
}

@media (max-width: 1240px) {
  .products-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .main-title {
    font-size: 38px;
  }
  .countdown-item {
    min-width: 46px;
    height: 52px;
    font-size: 26px;
  }
}

@media (max-width: 992px) {
  .products-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .offer-header {
    height: 240px;
  }
  .main-title {
    font-size: 34px;
  }
  .sub-title {
    font-size: 16px;
  }
  .countdown-item {
    min-width: 40px;
    height: 46px;
    font-size: 22px;
  }
}

@media (max-width: 768px) {
  .offer-header {
    height: 220px;
  }
  .header-content {
    flex-direction: column;
    gap: 16px;
  }
  .main-title {
    font-size: 28px;
    gap: 8px;
    margin-bottom: 6px;
  }
  .leaf-icon {
    font-size: 28px;
  }
  .sub-title {
    font-size: 14px;
    letter-spacing: 4px;
  }
  .countdown-item {
    min-width: 34px;
    height: 40px;
    font-size: 18px;
    padding: 0 6px;
  }
  .countdown-separator {
    font-size: 20px;
  }
  .products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .special-price {
    font-size: 20px;
  }
  .product-image-wrapper {
    height: 160px;
  }
}

@media (max-width: 480px) {
  .offer-header {
    height: 200px;
  }
  .main-title {
    font-size: 24px;
  }
  .sub-title {
    font-size: 12px;
  }
  .countdown-item {
    min-width: 30px;
    height: 36px;
    font-size: 16px;
  }
  .countdown-unit {
    gap: 10px;
    font-size: 10px;
  }
  .products-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .product-image-wrapper {
    height: 200px;
  }
}
</style>
