<template>
  <div class="home-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <!-- Logo -->
        <div class="logo" @click="navigateTo('home')">
          <i class="fas fa-leaf logo-icon"></i>
          <span class="logo-text">农鲜生</span>
        </div>
        
        <!-- 导航链接 -->
        <nav class="nav-menu">
          <a class="nav-link active" @click="navigateTo('home')">首页</a>
          <a class="nav-link" @click="navigateTo('products')">全部商品</a>
          <a class="nav-link" @click="navigateTo('category')">分类浏览</a>
          <a class="nav-link" @click="navigateTo('help')">助农专区</a>
          <a class="nav-link" @click="navigateTo('sale')">限时特惠</a>
          <a class="nav-link" @click="navigateTo('fresh')">新鲜直达</a>
          <a class="nav-link" @click="navigateTo('about')">关于我们</a>
        </nav>
        
        <!-- 搜索框 -->
        <div class="search-box">
          <i class="fas fa-search search-icon"></i>
          <input 
            type="text" 
            placeholder="搜索新鲜农产品..." 
            class="search-input"
            v-model="searchKeyword"
            @keyup.enter="handleSearch"
          >
        </div>
        
        <!-- 功能图标 -->
        <div class="header-icons">
          <div class="icon-wrapper cart-icon-wrapper">
            <a class="icon-btn" @click="navigateTo('cart')">
              <i class="fas fa-shopping-cart"></i>
            </a>
            <span class="cart-badge" v-if="cartStore.totalCount > 0">{{ cartStore.totalCount }}</span>
          </div>
          <a class="icon-btn" @click="navigateTo('user')">
            <i class="fas fa-user"></i>
          </a>
        </div>
      </div>
    </header>

    <!-- 轮播图区域 -->
    <section class="carousel-section" v-loading="loading.banners">
      <div class="carousel-slide" :style="{ backgroundImage: `url(${currentBanner.imageUrl})` }" v-if="banners.length > 0">
        <div class="carousel-overlay">
          <div class="carousel-content">
            <h2 class="carousel-title">{{ currentBanner.title }}</h2>
            <p class="carousel-subtitle">{{ currentBanner.subtitle }}</p>
            <a class="carousel-btn" @click="navigateTo('products')">立即选购</a>
          </div>
        </div>
      </div>
      <div class="carousel-dots" v-if="banners.length > 1">
        <span 
          v-for="(dot, index) in banners" 
          :key="index" 
          class="dot"
          :class="{ active: currentBannerIndex === index }"
          @click="currentBannerIndex = index"
        ></span>
      </div>
    </section>

    <!-- 分类入口 -->
    <section class="category-section">
      <div class="container">
        <div class="category-grid">
          <a class="category-card" @click="goToCategory(1)">
            <div class="category-icon-wrapper">
              <i class="fas fa-carrot category-icon"></i>
            </div>
            <span class="category-name">新鲜蔬菜</span>
          </a>
          <a class="category-card" @click="goToCategory(2)">
            <div class="category-icon-wrapper">
              <i class="fas fa-apple-alt category-icon"></i>
            </div>
            <span class="category-name">时令水果</span>
          </a>
          <a class="category-card" @click="goToCategory(3)">
            <div class="category-icon-wrapper">
              <i class="fas fa-drumstick-bite category-icon"></i>
            </div>
            <span class="category-name">禽蛋肉类</span>
          </a>
          <a class="category-card" @click="goToCategory(4)">
            <div class="category-icon-wrapper">
              <i class="fas fa-egg category-icon"></i>
            </div>
            <span class="category-name">蛋奶制品</span>
          </a>
          <a class="category-card" @click="goToCategory(5)">
            <div class="category-icon-wrapper">
              <i class="fas fa-bread-slice category-icon"></i>
            </div>
            <span class="category-name">粮油米面</span>
          </a>
          <a class="category-card" @click="goToCategory(6)">
            <div class="category-icon-wrapper">
              <i class="fas fa-seedling category-icon"></i>
            </div>
            <span class="category-name">干货特产</span>
          </a>
        </div>
      </div>
    </section>

    <!-- 热销商品 -->
    <section class="hot-products-section" v-loading="loading.products">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">热销商品</h2>
          <a class="view-all" @click="navigateTo('products')">
            查看全部 <i class="fas fa-chevron-right"></i>
          </a>
        </div>
        <div class="products-scroll">
          <div class="product-card" v-for="product in hotProducts" :key="product.id" @click="goToProductDetail(product.id)">
            <div class="product-image-wrapper">
              <img :src="product.mainImage" :alt="product.name" class="product-image">
              <span v-if="product.isHot === 1" class="product-tag hot">热销</span>
              <span v-if="product.isNew === 1" class="product-tag new">新品</span>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-rating">
                <i v-for="n in 5" :key="n" class="fas fa-star" :class="{ 'star-filled': n <= 4 }"></i>
                <span class="rating-count">({{ product.sales || 0 }})</span>
              </div>
              <div class="product-action">
                <span class="product-price">¥{{ product.price }}</span>
                <div class="favorite-btn" @click.stop="toggleFavorite(product)">
                  <i :class="['far fa-heart', product.isFavorite ? 'fas favorite-active' : '']"></i>
                </div>
              </div>
              <button class="add-cart-btn" @click.stop="addToCart(product)">
                加入购物车
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 助农专区 -->
    <section class="farmer-help-section">
      <div class="container">
        <div class="farmer-help-card">
          <div class="farmer-help-bg"></div>
          <div class="farmer-help-content">
            <div class="farmer-help-text">
              <h2 class="farmer-help-title">助农专区</h2>
              <p class="farmer-help-desc">支持乡村振兴，购买直采农产品，帮助农民增收</p>
              <a class="farmer-help-btn" @click="navigateTo('help')">立即支持</a>
            </div>
            <div class="farmer-products-grid">
              <div class="farmer-product-item" v-for="(item, index) in farmerProducts" :key="index">
                <img :src="item.image" :alt="item.name" class="farmer-product-img">
                <p class="farmer-product-name">{{ item.name }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 底部版权 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="copyright">© 2026 绿色农产品商城. 保留所有权利</div>
          <div class="footer-links">
            <a class="footer-link">关于我们</a>
            <a class="footer-link">联系方式</a>
            <a class="footer-link">隐私政策</a>
            <a class="footer-link">用户协议</a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { homeApi, categoryApi, cartApi, favoriteApi } from '../api'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 搜索关键词
const searchKeyword = ref('')

// 加载状态
const loading = ref({
  banners: false,
  categories: false,
  products: false
})

// 导航跳转函数
const navigateTo = (page) => {
  const routes = {
    home: '/',
    category: '/category',
    products: '/products',
    help: '/help',
    cart: '/cart',
    user: '/user',
    sale: '/special-offer',
    fresh: '/products?fresh=1',
    about: '/about'
  }
  if (routes[page]) {
    router.push(routes[page])
  }
}

// 跳转到商品详情页
const goToProductDetail = (productId) => {
  router.push('/product/' + productId)
}

// 跳转到分类页
const goToCategory = (categoryId) => {
  router.push('/products?category=' + categoryId)
}

// 搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push('/search?keyword=' + encodeURIComponent(searchKeyword.value.trim()))
  }
}

// 轮播图数据 - 提供默认数据
const banners = ref([
  {
    id: 1,
    title: '新鲜直达 品质保证',
    subtitle: '精选优质农产品，从田间到餐桌',
    imageUrl: 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=1920&h=400&fit=crop'
  },
  {
    id: 2,
    title: '助农惠农 乡村振兴',
    subtitle: '支持农民增收，助力乡村发展',
    imageUrl: 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=1920&h=400&fit=crop'
  },
  {
    id: 3,
    title: '绿色有机 健康生活',
    subtitle: '天然无污染，吃得更放心',
    imageUrl: 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=1920&h=400&fit=crop'
  }
])
const currentBannerIndex = ref(0)
const currentBanner = computed(() => banners.value[currentBannerIndex.value] || {})

// 分类数据 - 提供默认数据
const categories = ref([
  { id: 1, name: '新鲜蔬菜', icon: 'https://cdn-icons-png.flaticon.com/128/2329/2329903.png' },
  { id: 2, name: '时令水果', icon: 'https://cdn-icons-png.flaticon.com/128/3194/3194591.png' },
  { id: 3, name: '肉禽蛋类', icon: 'https://cdn-icons-png.flaticon.com/128/1046/1046784.png' },
  { id: 4, name: '粮油米面', icon: 'https://cdn-icons-png.flaticon.com/128/3050/3050227.png' },
  { id: 5, name: '干货特产', icon: 'https://cdn-icons-png.flaticon.com/128/3081/3081840.png' },
  { id: 6, name: '农副加工', icon: 'https://cdn-icons-png.flaticon.com/128/994/994039.png' }
])

// 热销商品数据 - 提供默认数据
const hotProducts = ref([
  {
    id: 1,
    name: '有机蔬菜礼盒 新鲜时令蔬菜 5斤装',
    price: 59.90,
    originalPrice: 79.90,
    mainImage: 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400&h=400&fit=crop',
    sales: 128,
    isHot: 1,
    isNew: 0
  },
  {
    id: 2,
    name: '陕西红富士苹果 8斤装 脆甜多汁',
    price: 79.00,
    originalPrice: 99.00,
    mainImage: 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&h=400&fit=crop',
    sales: 296,
    isHot: 1,
    isNew: 0
  },
  {
    id: 3,
    name: '五常大米 5kg 东北大米 优质粳米',
    price: 68.50,
    originalPrice: 88.00,
    mainImage: 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400&h=400&fit=crop',
    sales: 98,
    isHot: 0,
    isNew: 1
  },
  {
    id: 4,
    name: '农家散养土鸡蛋 30枚装 新鲜营养',
    price: 45.90,
    originalPrice: 55.00,
    mainImage: 'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400&h=400&fit=crop',
    sales: 324,
    isHot: 1,
    isNew: 0
  },
  {
    id: 5,
    name: '纯天然野花蜂蜜 500g 野生蜂蜜',
    price: 89.00,
    originalPrice: 109.00,
    mainImage: 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=400&h=400&fit=crop',
    sales: 156,
    isHot: 0,
    isNew: 1
  }
])

// 助农产品数据
const farmerProducts = ref([
  { name: '东北有机大米', image: 'https://assets.mockplus.cn/ai/newImages/pexels/6005.jpg' },
  { name: '高山绿茶', image: 'https://assets.mockplus.cn/ai/newImages/pexels/2393.jpg' },
  { name: '野生蜂蜜', image: 'https://assets.mockplus.cn/ai/newImages/pexels/1776.jpg' },
  { name: '陕北红枣', image: 'https://assets.mockplus.cn/ai/newImages/pexels/3589.jpg' }
])

// 自动轮播
let autoPlayTimer = null
const startAutoPlay = () => {
  stopAutoPlay()
  autoPlayTimer = setInterval(() => {
    if (banners.value.length > 1) {
      currentBannerIndex.value = (currentBannerIndex.value + 1) % banners.value.length
    }
  }, 5000)
}

const stopAutoPlay = () => {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
    autoPlayTimer = null
  }
}

// 获取轮播图
const fetchBanners = async () => {
  loading.value.banners = true
  try {
    const res = await homeApi.getBanners()
    // 只有获取到数据且不为空时才更新
    if (res && res.length > 0) {
      banners.value = res
      if (banners.value.length > 1) {
        startAutoPlay()
      }
    }
  } catch (error) {
    console.error('获取轮播图失败:', error)
    // 使用默认数据，不更新
  } finally {
    loading.value.banners = false
  }
}

// 获取分类
const fetchCategories = async () => {
  loading.value.categories = true
  try {
    const res = await categoryApi.getCategories()
    // 只有获取到数据且不为空时才更新
    if (res && res.length > 0) {
      categories.value = res
    }
  } catch (error) {
    console.error('获取分类失败:', error)
    // 使用默认数据，不更新
  } finally {
    loading.value.categories = false
  }
}

// 获取热销商品
const fetchHotProducts = async () => {
  loading.value.products = true
  try {
    const res = await homeApi.getHotProducts(8)
    // 只有获取到数据且不为空时才更新
    if (res && res.length > 0) {
      hotProducts.value = res.map(product => ({
        ...product,
        isFavorite: false
      }))
    }
  } catch (error) {
    console.error('获取热销商品失败:', error)
    // 使用默认数据，不更新
  } finally {
    loading.value.products = false
  }
}

// 添加到购物车
const addToCart = async (product) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再添加购物车')
    setTimeout(() => router.push('/login'), 1000)
    return
  }
  try {
    ElMessage.info('正在添加到购物车...')
    await cartStore.addToCart({
      productId: product.id,
      quantity: 1
    })
    ElMessage.success('✅ 已成功加入购物车')
    console.log('购物车当前商品数:', cartStore.totalCount)
  } catch (error) {
    console.error('添加购物车详细错误:', error)
    if (error.message?.includes('401') || error.message?.includes('未登录')) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      router.push('/login')
    } else {
      ElMessage.error('❌ 添加失败: ' + (error.message || '网络错误'))
    }
  }
}

// 切换收藏
const toggleFavorite = async (product) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再收藏商品')
    setTimeout(() => router.push('/login'), 1000)
    return
  }
  try {
    if (product.isFavorite) {
      await favoriteApi.removeFavorite(product.id)
      product.isFavorite = false
      ElMessage.success('✅ 已取消收藏')
    } else {
      await favoriteApi.addFavorite(product.id)
      product.isFavorite = true
      ElMessage.success('✅ 已添加到收藏')
    }
  } catch (error) {
    console.error('收藏操作详细错误:', error)
    if (error.message?.includes('401')) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      router.push('/login')
    } else {
      ElMessage.error('❌ 操作失败: ' + (error.message || '网络错误'))
    }
  }
}

onMounted(() => {
  fetchBanners()
  fetchCategories()
  fetchHotProducts()
  if (userStore.isLoggedIn) {
    cartStore.fetchCartList()
  }
})

onUnmounted(() => {
  stopAutoPlay()
})
</script>

<style scoped>
/* 基础样式 */
.home-page {
  width: 100%;
  min-height: 100vh;
  background-color: #ffffff;
}

.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 顶部导航栏 */
.header {
  width: 100%;
  height: 64px;
  background: linear-gradient(135deg, #43A047 0%, #4CAF50 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-container {
  width: 1200px;
  height: 100%;
  margin: 0 auto;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  margin-right: 40px;
  cursor: pointer;
}

.logo-icon {
  font-size: 28px;
  color: #ffffff;
  margin-right: 8px;
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 28px;
  flex: 1;
}

.nav-link {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.95);
  text-decoration: none;
  transition: all 0.3s ease;
  font-weight: 500;
  cursor: pointer;
}

.nav-link:hover,
.nav-link.active {
  color: #ffffff;
}

.search-box {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 6px 16px;
  width: 240px;
  margin-right: 24px;
}

.search-icon {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin-right: 8px;
}

.search-input {
  background: transparent;
  border: none;
  outline: none;
  color: #ffffff;
  font-size: 13px;
  width: 100%;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.header-icons {
  display: flex;
  align-items: center;
  gap: 16px;
}

.icon-wrapper {
  position: relative;
}

.icon-btn {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  text-decoration: none;
  transition: all 0.3s ease;
}

.icon-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.cart-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #FF9800;
  border-radius: 9px;
  font-size: 11px;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

/* 轮播图区域 */
.carousel-section {
  width: 100%;
  height: 400px;
  position: relative;
  overflow: hidden;
}

.carousel-slide {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  transition: all 0.5s ease;
}

.carousel-overlay {
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-content {
  text-align: center;
  color: #ffffff;
  max-width: 700px;
  padding: 0 20px;
}

.carousel-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.carousel-subtitle {
  font-size: 18px;
  margin-bottom: 32px;
  opacity: 0.95;
  line-height: 1.6;
}

.carousel-btn {
  display: inline-block;
  padding: 14px 40px;
  background: #FF9800;
  color: #ffffff;
  text-decoration: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.4);
  cursor: pointer;
}

.carousel-btn:hover {
  background: #F57C00;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.5);
}

.carousel-dots {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
}

.dot.active {
  background: #ffffff;
  width: 28px;
  border-radius: 5px;
}

/* 分类入口 */
.category-section {
  padding: 48px 0;
  background: #ffffff;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 24px;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
  background: #ffffff;
  border-radius: 12px;
  text-decoration: none;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  cursor: pointer;
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border-color: #E8F5E9;
}

.category-icon-wrapper {
  width: 72px;
  height: 72px;
  background: #E8F5E9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  transition: all 0.3s ease;
  overflow: hidden;
}

.category-card:hover .category-icon-wrapper {
  background: #4CAF50;
}

.category-icon {
  font-size: 28px;
  color: #4CAF50;
  transition: all 0.3s ease;
}

.category-icon-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.category-card:hover .category-icon {
  color: #ffffff;
}

.category-name {
  font-size: 14px;
  color: #333333;
  font-weight: 500;
}

/* 热销商品 */
.hot-products-section {
  padding: 32px 0 48px;
  background: #ffffff;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #333333;
}

.view-all {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #4CAF50;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  cursor: pointer;
}

.view-all:hover {
  color: #388E3C;
}

.view-all i {
  font-size: 12px;
  margin-left: 4px;
}

.products-scroll {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  padding-bottom: 8px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.products-scroll::-webkit-scrollbar {
  display: none;
}

.product-card {
  min-width: 220px;
  max-width: 220px;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
  transition: all 0.3s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  color: #ffffff;
}

.product-tag.hot {
  background: #FF9800;
}

.product-tag.new {
  background: #4CAF50;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 14px;
  color: #333333;
  font-weight: 500;
  line-height: 1.5;
  height: 42px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 8px;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-bottom: 12px;
}

.product-rating .fa-star {
  font-size: 12px;
  color: #ddd;
}

.product-rating .fa-star.star-filled {
  color: #FFC107;
}

.rating-count {
  font-size: 12px;
  color: #999999;
  margin-left: 4px;
}

.product-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.product-price {
  font-size: 18px;
  font-weight: 700;
  color: #4CAF50;
}

.favorite-btn {
  width: 32px;
  height: 32px;
  background: #E8F5E9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.favorite-btn:hover {
  background: #4CAF50;
}

.favorite-btn i {
  font-size: 14px;
  color: #4CAF50;
  transition: all 0.3s ease;
}

.favorite-btn:hover i {
  color: #ffffff;
}

.favorite-active {
  color: #ff4757 !important;
}

.add-cart-btn {
  width: 100%;
  height: 40px;
  background: #4CAF50;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-cart-btn:hover {
  background: #43A047;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

/* 助农专区 */
.farmer-help-section {
  padding: 32px 0 48px;
  background: #ffffff;
}

.farmer-help-card {
  position: relative;
  background: #E8F5E9;
  border-radius: 16px;
  padding: 40px;
  overflow: hidden;
}

.farmer-help-bg {
  position: absolute;
  top: 0;
  right: 0;
  width: 40%;
  height: 100%;
  background-image: url('https://assets.mockplus.cn/ai/newImages/pexels/1176.jpg');
  background-size: cover;
  background-position: center;
  opacity: 0.9;
}

.farmer-help-content {
  position: relative;
  z-index: 1;
}

.farmer-help-text {
  margin-bottom: 32px;
}

.farmer-help-title {
  font-size: 32px;
  font-weight: 700;
  color: #4CAF50;
  margin-bottom: 12px;
}

.farmer-help-desc {
  font-size: 15px;
  color: #666666;
  margin-bottom: 24px;
}

.farmer-help-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 32px;
  background: #FF9800;
  color: #ffffff;
  text-decoration: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s ease;
  cursor: pointer;
}

.farmer-help-btn:hover {
  background: #F57C00;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.4);
}

.farmer-products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  max-width: 70%;
}

.farmer-product-item {
  background: #ffffff;
  border-radius: 12px;
  padding: 12px;
  text-align: center;
  transition: all 0.3s ease;
  cursor: pointer;
}

.farmer-product-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.farmer-product-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 10px;
}

.farmer-product-name {
  font-size: 14px;
  color: #333333;
  font-weight: 500;
}

/* 底部版权 */
.footer {
  background: #F5F5F5;
  padding: 24px 0;
  margin-top: auto;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.copyright {
  font-size: 13px;
  color: #999999;
}

.footer-links {
  display: flex;
  gap: 32px;
}

.footer-link {
  font-size: 13px;
  color: #666666;
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
}

.footer-link:hover {
  color: #4CAF50;
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
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .farmer-products-grid {
    grid-template-columns: repeat(2, 1fr);
    max-width: 60%;
  }
  
  .nav-menu {
    display: none;
  }
}

@media (max-width: 768px) {
  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .farmer-help-bg {
    display: none;
  }
  
  .farmer-products-grid {
    max-width: 100%;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .carousel-title {
    font-size: 28px;
  }
  
  .carousel-subtitle {
    font-size: 14px;
  }
}
</style>