<template>
  <div class="help-activity-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <div class="header-left">
          <button class="back-btn" @click="goBack">
            <i class="fas fa-arrow-left"></i>
          </button>
          <h1 class="page-title">助农活动专题页</h1>
        </div>
        <div class="header-right">
          <div class="search-box">
            <input type="text" placeholder="搜索助农产品..." class="search-input">
            <i class="fas fa-search search-icon"></i>
          </div>
          <button class="cart-btn" @click="goToCart">
            <i class="fas fa-shopping-cart"></i>
            <span class="cart-badge">0</span>
          </button>
        </div>
      </div>
    </header>

    <!-- 活动轮播图 -->
    <section class="banner-section">
      <div class="banner-bg"></div>
      <div class="banner-overlay">
        <div class="container">
          <div class="banner-content">
            <h2 class="banner-title">2026春季助农行动</h2>
            <p class="banner-subtitle">直采新鲜农产品，助力乡村振兴</p>
            <button class="banner-btn" @click="scrollToProducts">立即选购</button>
          </div>
        </div>
      </div>
    </section>

    <!-- 活动介绍区 -->
    <section class="intro-section">
      <div class="container">
        <h2 class="intro-title">乡村振兴·助农行动</h2>
        <p class="intro-text">为积极响应国家乡村振兴战略，我们特别推出"乡村振兴·助农行动"专题活动，旨在搭建农产品直达消费者的桥梁，减少中间环节，让优质农产品走出大山，走进城市。</p>
        <p class="intro-text">本次活动汇集了来自全国各地贫困地区的特色农产品，所有产品均由当地农户直供，保证新鲜、优质、原生态。您的每一次购买，都是对乡村振兴事业的一份支持，让我们共同助力农民增收致富，共享美好生活。</p>
      </div>
    </section>

    <!-- 助农优选商品区 -->
    <section class="products-section" id="products">
      <div class="container">
        <h2 class="section-title">助农优选商品</h2>
        <div class="products-grid" v-loading="loading">
          <!-- 动态商品卡片 -->
          <div
            v-for="item in premiumProducts"
            :key="item.id"
            class="product-card"
            @click="goToProductDetail(item.id)"
          >
            <div class="product-image-wrapper">
              <img :src="item.mainImage || item.image" :alt="item.name" class="product-image">
              <span class="product-tag">助农价</span>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ item.name }}</h3>
              <div class="product-price-row">
                <span class="original-price">¥{{ item.originalPrice || item.price }}</span>
                <span class="sale-price">¥{{ item.salePrice || (item.price * 0.8).toFixed(1) }}</span>
              </div>
              <div class="product-action">
                <div class="product-rating">
                  <i class="fas fa-star"></i>
                  <i class="fas fa-star"></i>
                  <i class="fas fa-star"></i>
                  <i class="fas fa-star"></i>
                  <i class="fas fa-star-half-alt"></i>
                  <span class="rating-count">({{ item.salesCount || Math.floor(Math.random() * 200) }})</span>
                </div>
                <button class="add-cart-btn" @click.stop="addToCart(item)">
                  <i class="fas fa-shopping-cart"></i>加入购物车
                </button>
              </div>
            </div>
          </div>

          <!-- 空状态提示 -->
          <div v-if="!loading && premiumProducts.length === 0" class="empty-state">
            <i class="fas fa-box-open empty-icon"></i>
            <p class="empty-text">暂无助农优选商品，敬请期待~</p>
          </div>
        </div>
        <div class="view-more" v-if="premiumProducts.length > 0">
          <button class="view-more-btn" @click="navigateTo('products')">
            查看更多助农产品 <i class="fas fa-angle-right"></i>
          </button>
        </div>
      </div>
    </section>

    <!-- 活动规则区 -->
    <section class="rules-section">
      <div class="container">
        <h2 class="section-title">活动规则</h2>
        <div class="rules-grid">
          <!-- 规则卡片1 -->
          <div class="rule-card">
            <div class="rule-icon-wrapper">
              <i class="fas fa-clock"></i>
            </div>
            <h3 class="rule-title">活动时间</h3>
            <p class="rule-text">2026年3月20日 - 2026年4月20日</p>
            <p class="rule-text">每日9:00-21:00开放抢购</p>
          </div>

          <!-- 规则卡片2 -->
          <div class="rule-card">
            <div class="rule-icon-wrapper">
              <i class="fas fa-shopping-basket"></i>
            </div>
            <h3 class="rule-title">购买限制</h3>
            <p class="rule-text">每位用户每种商品限购3份</p>
            <p class="rule-text">每日累计订单不超过5单</p>
          </div>

          <!-- 规则卡片3 -->
          <div class="rule-card">
            <div class="rule-icon-wrapper">
              <i class="fas fa-truck"></i>
            </div>
            <h3 class="rule-title">配送说明</h3>
            <p class="rule-text">全场满99元包邮</p>
            <p class="rule-text">生鲜产品48小时内发货</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-column">
            <h3 class="footer-column-title">快速导航</h3>
            <ul class="footer-links">
              <li><a href="#" @click.prevent="goToHome">商城首页</a></li>
              <li><a href="#" @click.prevent="goToHelp">助农活动</a></li>
              <li><a href="#" @click.prevent="goToCategory">商品分类</a></li>
              <li><a href="#" @click.prevent="goToUserCenter">个人中心</a></li>
            </ul>
          </div>
          <div class="footer-column">
            <h3 class="footer-column-title">客户服务</h3>
            <ul class="footer-links">
              <li><a href="#" @click.prevent="goToHelpCenter">帮助中心</a></li>
              <li><a href="#" @click.prevent="contactUs">联系我们</a></li>
              <li><a href="#" @click.prevent="deliveryInfo">配送说明</a></li>
              <li><a href="#" @click.prevent="returnPolicy">退换货政策</a></li>
            </ul>
          </div>
          <div class="footer-column footer-contact">
            <h3 class="footer-column-title">关注我们</h3>
            <div class="social-links">
              <a href="#" class="social-link"><i class="fab fa-weixin"></i></a>
              <a href="#" class="social-link"><i class="fab fa-weibo"></i></a>
              <a href="#" class="social-link"><i class="fab fa-qq"></i></a>
            </div>
            <p class="contact-info">客服热线: 400-123-4567</p>
            <p class="contact-info">工作时间: 9:00-18:00</p>
          </div>
        </div>
        <div class="footer-bottom">
          <p class="copyright">&copy; 2026 助农商城 版权所有 | 沪ICP备12345678号</p>
          <p class="footer-note">本平台所有商品均来自贫困地区农户直供，感谢您的支持</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productApi, cartApi } from '@/api/index'

const router = useRouter()

// 助农优选商品数据
const loading = ref(false)
const premiumProducts = ref([])

// 获取助农优选商品列表
const fetchPremiumProducts = async () => {
  loading.value = true
  try {
    const res = await productApi.getPremiumProducts({ page: 1, size: 8 })
    const data = res?.data || res
    // 兼容不同的API响应格式
    premiumProducts.value = data?.list || data?.records || data || []
  } catch (error) {
    console.error('获取助农优选商品失败:', error)
    premiumProducts.value = []
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchPremiumProducts()
})

// 统一的页面跳转函数
const navigateTo = (page) => {
  const routes = {
    home: '/',
    category: '/category',
    help: '/help',
    cart: '/cart',
    user: '/user',
    login: '/login'
  }
  const path = routes[page]
  if (path) {
    router.push(path)
  }
}

// 跳转到商品详情页
const goToProductDetail = (productId) => {
  router.push('/product/' + productId)
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 跳转到购物车
const goToCart = () => {
  navigateTo('cart')
}

// 滚动到商品区
const scrollToProducts = () => {
  const productsSection = document.getElementById('products')
  if (productsSection) {
    productsSection.scrollIntoView({ behavior: 'smooth' })
  }
}

// 加入购物车（调用真实API）
const addToCart = async (item) => {
  try {
    await cartApi.addToCart({ productId: item.id, quantity: 1 })
    alert(`已将 "${item.name}" 加入购物车`)
  } catch (error) {
    console.error('加入购物车失败:', error)
    alert('加入购物车失败，请重试')
  }
}

// 查看更多
const viewMore = () => {
  navigateTo('category')
}

// 页面跳转
const goToHome = () => {
  navigateTo('home')
}

const goToHelp = () => {
  navigateTo('help')
}

const goToCategory = () => {
  navigateTo('category')
}

const goToUserCenter = () => {
  navigateTo('user')
}

const goToHelpCenter = () => {
  navigateTo('help')
}

const contactUs = () => {
  alert('联系我们')
}

const deliveryInfo = () => {
  alert('配送说明')
}

const returnPolicy = () => {
  alert('退换货政策')
}
</script>

<style scoped>
/* 基础样式 */
.help-activity-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 顶部导航栏 */
.header {
  width: 100%;
  height: 60px;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-container {
  width: 1200px;
  height: 100%;
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  font-size: 20px;
  color: #333333;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn:hover {
  color: #4CAF50;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #333333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.search-box {
  position: relative;
  width: 256px;
}

.search-input {
  width: 100%;
  height: 36px;
  padding: 0 12px 0 36px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
}

.search-input:focus {
  border-color: #4CAF50;
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
}

.search-input::placeholder {
  color: #999999;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #666666;
  font-size: 14px;
}

.cart-btn {
  position: relative;
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  font-size: 20px;
  color: #333333;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cart-btn:hover {
  color: #4CAF50;
}

.cart-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 18px;
  height: 18px;
  background: #FF9800;
  border-radius: 50%;
  font-size: 10px;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 活动轮播图 */
.banner-section {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.banner-bg {
  position: absolute;
  inset: 0;
  background-image: url('https://assets.mockplus.cn/ai/newImages/pexels/1176.jpg');
  background-size: cover;
  background-position: center;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to right, rgba(76, 175, 80, 0.8), rgba(76, 175, 80, 0.4));
  display: flex;
  align-items: center;
}

.banner-content {
  max-width: 500px;
  color: #ffffff;
}

.banner-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
}

.banner-subtitle {
  font-size: 18px;
  margin-bottom: 20px;
  opacity: 0.95;
}

.banner-btn {
  padding: 10px 24px;
  background: #FF9800;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.banner-btn:hover {
  background: #f57c00;
}

/* 活动介绍区 */
.intro-section {
  background: #ffffff;
  padding: 48px 0;
  text-align: center;
}

.intro-title {
  font-size: 24px;
  font-weight: 700;
  color: #333333;
  margin-bottom: 24px;
}

.intro-text {
  font-size: 15px;
  color: #666666;
  line-height: 1.8;
  max-width: 800px;
  margin: 0 auto 12px;
}

.intro-text:last-child {
  margin-bottom: 0;
}

/* 助农优选商品区 */
.products-section {
  padding: 48px 0;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #333333;
  margin-bottom: 24px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.product-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  height: 192px;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 8px;
  background: #FF9800;
  color: #ffffff;
  font-size: 12px;
  border-radius: 4px;
}

.product-info {
  padding: 0 4px;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  color: #333333;
  margin-bottom: 8px;
}

.product-price-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.original-price {
  font-size: 13px;
  color: #999999;
  text-decoration: line-through;
}

.sale-price {
  font-size: 18px;
  font-weight: 700;
  color: #FF9800;
}

.product-action {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 2px;
  color: #ffc107;
  font-size: 12px;
}

.rating-count {
  color: #666666;
  margin-left: 4px;
}

.add-cart-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: #4CAF50;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-cart-btn:hover {
  background: #43a047;
}

/* 空状态样式 */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  color: #cccccc;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #999999;
}

.view-more {
  text-align: center;
}

.view-more-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  background: transparent;
  border: 1px solid #4CAF50;
  border-radius: 4px;
  font-size: 14px;
  color: #4CAF50;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-more-btn:hover {
  background: #4CAF50;
  color: #ffffff;
}

/* 活动规则区 */
.rules-section {
  background: #ffffff;
  padding: 48px 0;
}

.rules-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.rule-card {
  background: #e8f5e9;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.rule-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.rule-icon-wrapper {
  width: 48px;
  height: 48px;
  background: rgba(76, 175, 80, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.rule-icon-wrapper i {
  font-size: 20px;
  color: #4CAF50;
}

.rule-title {
  font-size: 18px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 12px;
}

.rule-text {
  font-size: 14px;
  color: #666666;
  margin-bottom: 4px;
}

/* 页脚 */
.footer {
  background: #333333;
  padding: 48px 0 24px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  margin-bottom: 32px;
}

.footer-column {
  flex: 1;
}

.footer-column-title {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 16px;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links li {
  margin-bottom: 8px;
}

.footer-links a {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
}

.footer-links a:hover {
  color: #FF9800;
}

.footer-contact {
  text-align: right;
}

.social-links {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.social-link {
  font-size: 20px;
  color: #ffffff;
  transition: all 0.3s ease;
}

.social-link:hover {
  color: #FF9800;
}

.contact-info {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 4px;
}

.footer-bottom {
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  text-align: center;
}

.copyright {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
}

.footer-note {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

/* 响应式适配 */
@media (max-width: 1240px) {
  .container,
  .header-container {
    width: 100%;
    padding: 0 16px;
  }
}

@media (max-width: 1024px) {
  .products-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .rules-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .products-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .rules-grid {
    grid-template-columns: 1fr;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 24px;
  }
  
  .footer-contact {
    text-align: left;
  }
  
  .social-links {
    justify-content: flex-start;
  }
  
  .banner-title {
    font-size: 24px;
  }
  
  .banner-subtitle {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .products-grid {
    grid-template-columns: 1fr;
  }
  
  .header-right {
    gap: 12px;
  }
  
  .search-box {
    width: 160px;
  }
}
</style>
