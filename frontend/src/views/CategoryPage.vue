<template>
  <div class="category-page">
    <!-- 导航栏 -->
    <header class="header">
      <div class="header-container">
        <div class="logo-section">
          <div class="logo-icon-wrapper" @click="navigateTo('home')">
            <i class="fas fa-apple-alt logo-icon"></i>
          </div>
          <h1 class="page-title">农产品分类页</h1>
        </div>
        <div class="header-actions">
          <div class="search-box">
            <input 
              type="text" 
              v-model="searchKeyword"
              placeholder="搜索农产品..." 
              class="search-input"
              @keyup.enter="handleSearch"
            >
            <i class="fas fa-search search-icon" @click="handleSearch"></i>
          </div>
          <a href="#" class="icon-btn" @click.prevent="goToUserCenter">
            <i class="fas fa-user"></i>
          </a>
          <a href="#" class="icon-btn cart-btn" @click.prevent="goToCart">
            <i class="fas fa-shopping-cart"></i>
            <span v-if="cartStore.totalCount > 0" class="cart-badge">{{ cartStore.totalCount > 99 ? '99+' : cartStore.totalCount }}</span>
          </a>
        </div>
      </div>
    </header>

    <!-- 类别筛选区 -->
    <div class="category-filter">
      <div class="container">
        <div class="filter-content">
          <div class="category-tags">
            <a href="#" 
               class="category-tag" 
               :class="{ active: currentCategory === 'all' }"
               @click.prevent="selectCategory('all')">
              全部
            </a>
            <a 
              v-for="cat in categories" 
              :key="cat.id" 
              href="#" 
              class="category-tag" 
              :class="{ active: currentCategory === cat.id }"
              @click.prevent="selectCategory(cat.id)">
              {{ cat.name }}
            </a>
          </div>
          <div class="filter-actions">
            <div class="filter-dropdown-wrapper">
              <button class="filter-btn" @click="showFilterPanel = !showFilterPanel">
                <i class="fas fa-filter"></i>
                <span>筛选</span>
                <i class="fas fa-chevron-down" :class="{ 'rotate': showFilterPanel }" style="margin-left: 2px; font-size: 10px;"></i>
              </button>
              <div class="filter-dropdown-panel" v-show="showFilterPanel">
                <div class="filter-group">
                  <div class="filter-group-title">价格区间</div>
                  <div class="filter-group-options">
                    <button class="filter-option" :class="{ active: currentPriceRange === '' }" @click="setPriceRange('')">不限</button>
                    <button class="filter-option" :class="{ active: currentPriceRange === '0-50' }" @click="setPriceRange('0-50')">0-50元</button>
                    <button class="filter-option" :class="{ active: currentPriceRange === '50-100' }" @click="setPriceRange('50-100')">50-100元</button>
                    <button class="filter-option" :class="{ active: currentPriceRange === '100-200' }" @click="setPriceRange('100-200')">100-200元</button>
                    <button class="filter-option" :class="{ active: currentPriceRange === '200+' }" @click="setPriceRange('200+')">200元以上</button>
                  </div>
                </div>
                <div class="filter-group">
                  <div class="filter-group-title">自定义价格</div>
                  <div class="custom-price-row">
                    <input type="number" v-model="customMinPrice" placeholder="最低价" class="price-input">
                    <span class="price-dash">-</span>
                    <input type="number" v-model="customMaxPrice" placeholder="最高价" class="price-input">
                    <button class="price-apply-btn" @click="applyCustomPrice">确定</button>
                  </div>
                </div>
              </div>
            </div>
            <button class="filter-btn" :class="{ active: currentSort !== 'default' }" @click="togglePriceSort">
              <span>价格</span>
              <i :class="currentSort === 'price-asc' ? 'fas fa-sort-amount-up' : currentSort === 'price-desc' ? 'fas fa-sort-amount-down' : 'fas fa-sort'"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品列表区 -->
    <main class="product-list" v-loading="loading">
      <div class="container">
        <div class="product-grid">
          <div class="product-card" v-for="product in products" :key="product.id" @click="goToProductDetail(product.id)">
            <div class="product-image-wrapper">
              <img :src="product.mainImage || product.image" :alt="product.name" class="product-image">
              <button class="favorite-btn" @click.stop="toggleFavorite(product)">
                <i :class="['far fa-heart', product.isFavorite ? 'fas favorite-active' : '']"></i>
              </button>
              <button class="add-cart-btn" @click.stop="addToCart(product)">
                <i class="fas fa-shopping-cart"></i>
              </button>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-rating">
                <i v-for="n in 5" :key="n" :class="getStarClass(n, product.rating || 4)"></i>
                <span class="rating-count">({{ product.reviewCount || product.sales || 0 }}条评价)</span>
              </div>
              <p class="product-price">¥{{ product.price }}</p>
              <p class="product-origin">
                <i class="fas fa-map-marker-alt"></i>
                {{ product.origin || product.description || '产地直供' }}
              </p>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && products.length === 0" class="empty-state">
          <i class="fas fa-inbox empty-icon"></i>
          <p>暂无商品</p>
        </div>
      </div>
    </main>

    <!-- 分页控件 -->
    <div class="pagination-section">
      <div class="container">
        <div class="pagination">
          <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">
            <i class="fas fa-chevron-left"></i>
          </button>
          <button 
            v-for="page in displayedPages" 
            :key="page"
            class="page-btn"
            :class="{ active: currentPage === page }"
            @click="currentPage = page">
            {{ page }}
          </button>
          <span v-if="showEllipsis" class="ellipsis">...</span>
          <button 
            v-if="totalPages > 5" 
            class="page-btn"
            :class="{ active: currentPage === totalPages }"
            @click="currentPage = totalPages">
            {{ totalPages }}
          </button>
          <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">
            <i class="fas fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-top">
          <div class="footer-brand">
            <div class="footer-logo">
              <i class="fas fa-leaf"></i>
              <span>农鲜生</span>
            </div>
            <p class="footer-slogan">新鲜农产品，从田间到餐桌的直达</p>
          </div>
          <div class="footer-links">
            <a href="#" @click.prevent="goToHome">首页</a>
            <a href="#" @click.prevent="goToCategory">分类</a>
            <a href="#" @click.prevent="goToHelp">助农</a>
            <a href="#" @click.prevent="goToUserCenter">我的</a>
          </div>
        </div>
        <div class="footer-bottom">
          <p>© 2026 农鲜生农产品商城 版权所有</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { categoryApi, productApi, favoriteApi } from '../api'
import { ElMessage } from 'element-plus'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 当前排序
const currentSort = ref('default')

const currentPriceRange = ref('')

const showFilterPanel = ref(false)
const customMinPrice = ref('')
const customMaxPrice = ref('')

// 搜索关键词
const searchKeyword = ref('')

// 当前分类（支持 'all' 或数字ID）
const currentCategory = ref('all')

// 当前页码
const currentPage = ref(1)
const totalPages = ref(1)
const total = ref(0)

// 加载状态
const loading = ref(false)

// 每页商品数
const pageSize = 10

// 商品数据 - 从API获取
const products = ref([])

// 分类列表
const categories = ref([])

// 计算属性：显示的页码
const displayedPages = computed(() => {
  const pages = []
  const maxDisplay = 3
  
  if (totalPages.value <= maxDisplay + 2) {
    for (let i = 1; i <= totalPages.value; i++) {
      pages.push(i)
    }
  } else {
    if (currentPage.value <= 2) {
      for (let i = 1; i <= maxDisplay; i++) {
        pages.push(i)
      }
    } else if (currentPage.value >= totalPages.value - 1) {
      for (let i = totalPages.value - maxDisplay + 1; i <= totalPages.value; i++) {
        pages.push(i)
      }
    } else {
      for (let i = currentPage.value - 1; i <= currentPage.value + 1; i++) {
        pages.push(i)
      }
    }
  }
  
  return pages
})

// 计算属性：是否显示省略号
const showEllipsis = computed(() => {
  return totalPages.value > 5 && !displayedPages.value.includes(totalPages.value)
})

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await categoryApi.getCategories()
    // 确保返回的是数组格式
    categories.value = Array.isArray(res) ? res : (res?.list || res?.records || [])
  } catch (error) {
    console.error('获取分类失败:', error)
    ElMessage.error('获取分类失败')
  }
}

// 选择分类
const selectCategory = (categoryId) => {
  currentCategory.value = categoryId
  // 重置搜索关键词
  searchKeyword.value = ''
  currentPage.value = 1
  fetchProducts()
}

// 搜索处理
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  // 重置分类为"全部"
  currentCategory.value = 'all'
  currentPage.value = 1
  fetchProducts()
}

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize
    }

    // 如果选择了分类，添加分类ID（数字类型）
    if (currentCategory.value && currentCategory.value !== 'all') {
      params.categoryId = currentCategory.value
    }

    // 如果有搜索关键词，添加搜索参数
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }

    // 排序参数
    if (currentSort.value !== 'default') {
      params.sort = currentSort.value
    }

    // 价格区间筛选
    if (currentPriceRange.value) {
      switch (currentPriceRange.value) {
        case '0-50':
          params.minPrice = 0
          params.maxPrice = 50
          break
        case '50-100':
          params.minPrice = 50
          params.maxPrice = 100
          break
        case '100-200':
          params.minPrice = 100
          params.maxPrice = 200
          break
        case '200+':
          params.minPrice = 200
          break
      }
    } else if (customMinPrice.value || customMaxPrice.value) {
      if (customMinPrice.value) params.minPrice = Number(customMinPrice.value)
      if (customMaxPrice.value) params.maxPrice = Number(customMaxPrice.value)
    }

    let res
    if (searchKeyword.value.trim()) {
      // 使用搜索接口
      res = await productApi.searchProducts(params)
    } else {
      // 使用普通列表接口
      res = await productApi.getProductList(params)
    }

    console.log('CategoryPage API返回数据:', JSON.stringify(res).substring(0, 500))

    // 兼容后端返回格式：可能是分页对象或数组
    if (res && typeof res === 'object') {
      // 优先使用 records（MyBatis-Plus分页格式），其次 list，最后尝试数组
      let rawProducts = res.records || res.list || (Array.isArray(res) ? res : [])

      // 确保每个商品都有必要的字段，进行数据标准化
      products.value = rawProducts.map(item => ({
        id: item.id,
        name: item.name || item.productName || '未命名商品',
        price: item.price || 0,
        mainImage: item.mainImage || item.image || item.imageUrl || '',
        origin: item.origin || item.description || '',
        categoryId: item.categoryId || item.category?.id,
        ...item  // 保留其他原始字段
      }))

      total.value = res.total || products.value.length
      console.log(`解析完成：共 ${products.value.length} 个商品，总计 ${total.value} 条`)
    } else if (Array.isArray(res)) {
      products.value = res.map(item => ({
        id: item.id,
        name: item.name || '未命名商品',
        price: item.price || 0,
        mainImage: item.mainImage || item.image || '',
        origin: item.origin || '',
        ...item
      }))
      total.value = res.length
    } else {
      console.warn('CategoryPage: 无法解析的商品数据格式:', typeof res)
      products.value = []
      total.value = 0
    }
    
    totalPages.value = Math.ceil(total.value / pageSize) || 1

    // 如果用户已登录，同步收藏状态
    if (userStore.isLoggedIn && products.value.length > 0) {
      try {
        const favRes = await favoriteApi.getFavoriteList({ page: 1, size: 1000 })
        const favList = Array.isArray(favRes) ? favRes : (favRes?.records || favRes?.list || [])
        const favProductIds = new Set(favList.map(fav => fav.productId || fav.id))
        products.value.forEach(product => {
          if (favProductIds.has(product.id)) {
            product.isFavorite = true
          }
        })
      } catch (e) {
        console.error('获取收藏列表失败:', e)
      }
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 方法：加入购物车
const addToCart = async (product) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await cartStore.addToCart({
      productId: product.id,
      quantity: 1
    })
    ElMessage.success(`已将 "${product.name}" 加入购物车`)
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

// 方法：切换收藏
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
      ElMessage.success('已取消收藏')
    } else {
      await favoriteApi.addFavorite(product.id)
      product.isFavorite = true
      ElMessage.success('已添加到收藏')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    if (error.message?.includes('401')) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      router.push('/login')
    } else {
      ElMessage.error('操作失败: ' + (error.message || '网络错误'))
    }
  }
}

// 方法：获取星星类名
const getStarClass = (n, rating) => {
  if (n <= Math.floor(rating)) {
    return 'fas fa-star star-filled'
  } else if (n === Math.ceil(rating) && rating % 1 !== 0) {
    return 'fas fa-star-half-alt star-filled'
  } else {
    return 'far fa-star'
  }
}

// 方法：页面跳转
const navigateTo = (page) => {
  const routes = {
    home: '/',
    products: '/products',
    productDetail: '/product/1',
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

const goToProductDetail = (productId) => {
  router.push('/product/' + productId)
}

const setPriceRange = (range) => {
  currentPriceRange.value = range
  customMinPrice.value = ''
  customMaxPrice.value = ''
  showFilterPanel.value = false
  currentPage.value = 1
  fetchProducts()
}

const applyCustomPrice = () => {
  const min = customMinPrice.value ? Number(customMinPrice.value) : null
  const max = customMaxPrice.value ? Number(customMaxPrice.value) : null
  if (min !== null && max !== null && min > max) {
    ElMessage.warning('最低价不能大于最高价')
    return
  }
  currentPriceRange.value = ''
  currentPage.value = 1
  fetchProducts()
}

const togglePriceSort = () => {
  // 循环切换排序：默认 -> 价格升序 -> 价格降序 -> 默认
  if (currentSort.value === 'default') {
    currentSort.value = 'price-asc'
  } else if (currentSort.value === 'price-asc') {
    currentSort.value = 'price-desc'
  } else {
    currentSort.value = 'default'
  }
  currentPage.value = 1
  fetchProducts()
}

// 页面加载时获取数据
onMounted(async () => {
  await fetchCategories()
  fetchProducts()
  // 如果用户已登录，获取购物车数量
  if (userStore.isLoggedIn) {
    try {
      await cartStore.fetchCartList()
    } catch (error) {
      console.error('获取购物车失败:', error)
      // 静默失败，不影响主流程
    }
  }
})

// 监听页码变化
watch(currentPage, () => {
  fetchProducts()
})
</script>

<style scoped>
/* 基础样式 */
.category-page {
  width: 100%;
  min-height: 100vh;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
}

.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 导航栏 */
.header {
  width: 100%;
  height: 64px;
  background: #4CAF50;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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

.logo-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-icon-wrapper {
  width: 40px;
  height: 40px;
  background: #ffffff;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon {
  font-size: 20px;
  color: #4CAF50;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
}

.header-actions {
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
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  font-size: 14px;
  color: #ffffff;
  outline: none;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #ffffff;
  font-size: 14px;
}

.icon-btn {
  width: 32px;
  height: 32px;
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

.cart-btn {
  position: relative;
}

.cart-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 16px;
  height: 16px;
  background: #FF9800;
  border-radius: 50%;
  font-size: 10px;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 类别筛选区 */
.category-filter {
  background: #F5F5F5;
  padding: 16px 0;
}

.filter-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.category-tags {
  display: flex;
  align-items: center;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.category-tag {
  white-space: nowrap;
  padding: 8px 24px;
  background: #ffffff;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: #333333;
  text-decoration: none;
  transition: all 0.3s ease;
}

.category-tag:hover {
  background: #E8F5E9;
}

.category-tag.active {
  background: #4CAF50;
  color: #ffffff;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #ffffff;
  border: 1px solid #EEEEEE;
  border-radius: 4px;
  font-size: 13px;
  color: #757575;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-btn:hover {
  border-color: #4CAF50;
  color: #4CAF50;
}

.filter-btn.active {
  border-color: #4CAF50;
  color: #4CAF50;
  background: #E8F5E9;
}

.filter-dropdown-wrapper {
  position: relative;
}

.filter-dropdown-panel {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 8px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  padding: 16px;
  z-index: 100;
  min-width: 320px;
}

.filter-group {
  margin-bottom: 12px;
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-group-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.filter-group-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-option {
  padding: 5px 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-option:hover {
  border-color: #4CAF50;
  color: #4CAF50;
}

.filter-option.active {
  background: #4CAF50;
  border-color: #4CAF50;
  color: #fff;
}

.custom-price-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.price-input {
  width: 80px;
  height: 30px;
  padding: 0 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  outline: none;
}

.price-input:focus {
  border-color: #4CAF50;
}

.price-dash {
  color: #999;
}

.price-apply-btn {
  height: 30px;
  padding: 0 12px;
  background: #4CAF50;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
}

.price-apply-btn:hover {
  background: #43A047;
}

.filter-btn i {
  font-size: 12px;
}

/* 商品列表区 */
.product-list {
  flex: 1;
  padding: 32px 0;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999999;
}

.empty-state .empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #dddddd;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 24px;
}

.product-card {
  background: #ffffff;
  border: 1px solid #EEEEEE;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  height: 192px;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.add-cart-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  background: #ffffff;
  border: none;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4CAF50;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-cart-btn:hover {
  background: #4CAF50;
  color: #ffffff;
}

.favorite-btn {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 32px;
  height: 32px;
  background: #ffffff;
  border: none;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 2;
}

.favorite-btn:hover {
  background: #E8F5E9;
}

.favorite-btn i {
  color: #4CAF50;
  transition: all 0.3s ease;
}

.favorite-btn:hover i {
  color: #ff4757;
}

.favorite-active {
  color: #ff4757 !important;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 15px;
  font-weight: 500;
  color: #333333;
  margin-bottom: 8px;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-bottom: 8px;
}

.product-rating i {
  font-size: 12px;
  color: #dddddd;
}

.product-rating i.star-filled {
  color: #FFC107;
}

.rating-count {
  font-size: 12px;
  color: #999999;
  margin-left: 4px;
}

.product-price {
  font-size: 16px;
  font-weight: 700;
  color: #FF9800;
  margin-bottom: 8px;
}

.product-origin {
  font-size: 13px;
  color: #757575;
  display: flex;
  align-items: center;
  gap: 4px;
}

.product-origin i {
  font-size: 12px;
}

/* 分页控件 */
.pagination-section {
  padding: 32px 0;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.page-btn {
  width: 40px;
  height: 40px;
  background: #ffffff;
  border: 1px solid #EEEEEE;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #333333;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: #F5F5F5;
}

.page-btn.active {
  background: #4CAF50;
  border-color: #4CAF50;
  color: #ffffff;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ellipsis {
  color: #757575;
  padding: 0 4px;
}

/* 页脚 */
.footer {
  background: #333333;
  padding: 32px 0 16px;
  margin-top: 32px;
}

.footer-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.footer-brand {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
}

.footer-logo i {
  color: #FF9800;
}

.footer-slogan {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 32px;
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

.footer-bottom {
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  text-align: center;
}

.footer-bottom p {
  font-size: 13px;
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
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }
  
  .filter-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .category-tags {
    width: 100%;
  }
  
  .search-box {
    width: 180px;
  }
  
  .footer-top {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
