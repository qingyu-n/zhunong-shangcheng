<template>
  <div class="product-list-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <div class="header-left">
          <div class="logo" @click="navigateTo('home')">
            <i class="fas fa-leaf logo-icon"></i>
            <span class="logo-text">农鲜优选</span>
          </div>
          <nav class="nav-menu">
            <a class="nav-link" @click="navigateTo('home')">首页</a>
            <a class="nav-link active" @click="navigateTo('products')">全部商品</a>
            <a class="nav-link" @click="navigateTo('category')">商品分类</a>
            <a class="nav-link" @click="navigateTo('help')">助农专区</a>
            <a class="nav-link" @click="navigateTo('sale')">限时特惠</a>
            <a class="nav-link" @click="navigateTo('fresh')">新鲜直达</a>
            <a class="nav-link" @click="navigateTo('about')">关于我们</a>
          </nav>
        </div>
        <div class="header-icons">
          <button class="icon-btn">
            <i class="fas fa-search"></i>
          </button>
          <button class="icon-btn" @click="navigateTo('user')">
            <i class="fas fa-user"></i>
          </button>
          <button class="icon-btn cart-btn" @click="navigateTo('cart')">
            <i class="fas fa-shopping-cart"></i>
            <span v-if="cartStore.totalCount > 0" class="cart-badge">{{ cartStore.totalCount }}</span>
          </button>
        </div>
      </div>
    </header>

    <!-- 搜索区 -->
    <section class="search-section">
      <div class="container">
        <div class="search-box">
          <i class="fas fa-search search-icon"></i>
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索商品..." 
            class="search-input"
            @keyup.enter="handleSearch"
          >
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
      </div>
    </section>

    <!-- 筛选与排序区 -->
    <section class="filter-section">
      <div class="container">
        <div class="filter-left">
          <!-- 分类筛选 -->
          <div class="filter-dropdown">
            <button class="dropdown-btn" @click="toggleCategoryDropdown">
              <span>全部分类</span>
              <i class="fas fa-chevron-down" :class="{ 'rotate': showCategoryDropdown }"></i>
            </button>
            <div class="dropdown-menu" v-show="showCategoryDropdown">
              <a href="#" class="dropdown-item" @click.prevent="selectCategory('')">全部分类</a>
              <a
                v-for="cat in categories.filter(c => c.level === 1)"
                :key="cat.id"
                href="#"
                class="dropdown-item"
                @click.prevent="selectCategory(String(cat.id))"
              >{{ cat.name }}</a>
            </div>
          </div>

          <!-- 分类标签 -->
          <div class="filter-tags">
            <button
              class="tag-btn"
              :class="{ active: currentCategory === '' }"
              @click="currentCategory = ''"
            >全部</button>
            <button
              v-for="cat in categories.filter(c => c.level === 1)"
              :key="cat.id"
              class="tag-btn"
              :class="{ active: currentCategory === String(cat.id) }"
              @click="currentCategory = String(cat.id)"
            >{{ cat.name }}</button>
          </div>

          <!-- 价格区间 -->
          <div class="filter-dropdown">
            <button class="dropdown-btn" @click="togglePriceDropdown">
              <span>价格区间</span>
              <i class="fas fa-chevron-down" :class="{ 'rotate': showPriceDropdown }"></i>
            </button>
            <div class="dropdown-menu" v-show="showPriceDropdown">
              <a href="#" class="dropdown-item" @click.prevent="selectPriceRange('')">全部价格</a>
              <a href="#" class="dropdown-item" @click.prevent="selectPriceRange('0-50')">0-50元</a>
              <a href="#" class="dropdown-item" @click.prevent="selectPriceRange('50-100')">50-100元</a>
              <a href="#" class="dropdown-item" @click.prevent="selectPriceRange('100+')">100元以上</a>
            </div>
          </div>

          <!-- 价格标签 -->
          <div class="filter-tags">
            <button 
              class="tag-btn outline" 
              :class="{ active: currentPriceRange === '0-50' }"
              @click="currentPriceRange = '0-50'"
            >0-50元</button>
            <button 
              class="tag-btn outline" 
              :class="{ active: currentPriceRange === '50-100' }"
              @click="currentPriceRange = '50-100'"
            >50-100元</button>
            <button 
              class="tag-btn outline" 
              :class="{ active: currentPriceRange === '100+' }"
              @click="currentPriceRange = '100+'"
            >100元以上</button>
          </div>

          <button class="more-filter-btn" @click="showMoreFilter = !showMoreFilter">
            <i class="fas fa-sliders-h"></i>
            <span>{{ showMoreFilter ? '收起筛选' : '更多筛选' }}</span>
            <i class="fas fa-chevron-down" :class="{ 'rotate': showMoreFilter }" style="margin-left: 4px; font-size: 10px;"></i>
          </button>
        </div>

        <div class="filter-right">
          <!-- 排序 -->
          <div class="filter-dropdown">
            <button class="dropdown-btn" @click="toggleSortDropdown">
              <span>{{ currentSortLabel }}</span>
              <i class="fas fa-chevron-down" :class="{ 'rotate': showSortDropdown }"></i>
            </button>
            <div class="dropdown-menu" v-show="showSortDropdown">
              <a href="#" class="dropdown-item" @click.prevent="selectSort('default')">综合排序</a>
              <a href="#" class="dropdown-item" @click.prevent="selectSort('price-asc')">价格从低到高</a>
              <a href="#" class="dropdown-item" @click.prevent="selectSort('price-desc')">价格从高到低</a>
              <a href="#" class="dropdown-item" @click.prevent="selectSort('sales')">销量优先</a>
            </div>
          </div>

          <!-- 视图切换 -->
          <div class="view-toggle">
            <button class="view-btn active" @click="viewMode = 'list'">
              <i class="fas fa-list"></i>
            </button>
            <button class="view-btn" @click="viewMode = 'grid'">
              <i class="fas fa-th-large"></i>
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 更多筛选面板 -->
    <section class="more-filter-panel" v-show="showMoreFilter">
      <div class="container">
        <div class="filter-row">
          <span class="filter-label">价格区间：</span>
          <div class="filter-options">
            <button class="filter-option-btn" :class="{ active: customPriceMin === '' && customPriceMax === '' }" @click="customPriceMin = ''; customPriceMax = ''">不限</button>
            <button class="filter-option-btn" :class="{ active: currentPriceRange === '0-50' }" @click="selectPriceRange('0-50')">0-50元</button>
            <button class="filter-option-btn" :class="{ active: currentPriceRange === '50-100' }" @click="selectPriceRange('50-100')">50-100元</button>
            <button class="filter-option-btn" :class="{ active: currentPriceRange === '100+' }" @click="selectPriceRange('100+')">100元以上</button>
            <div class="custom-price">
              <input type="number" v-model="customPriceMin" placeholder="最低价" class="price-input" @change="applyCustomPrice">
              <span class="price-separator">-</span>
              <input type="number" v-model="customPriceMax" placeholder="最高价" class="price-input" @change="applyCustomPrice">
              <button class="price-confirm-btn" @click="applyCustomPrice">确定</button>
            </div>
          </div>
        </div>
        <div class="filter-row">
          <span class="filter-label">排序方式：</span>
          <div class="filter-options">
            <button class="filter-option-btn" :class="{ active: currentSort === 'default' }" @click="selectSort('default')">综合排序</button>
            <button class="filter-option-btn" :class="{ active: currentSort === 'price-asc' }" @click="selectSort('price-asc')">价格从低到高</button>
            <button class="filter-option-btn" :class="{ active: currentSort === 'price-desc' }" @click="selectSort('price-desc')">价格从高到低</button>
            <button class="filter-option-btn" :class="{ active: currentSort === 'sales' }" @click="selectSort('sales')">销量优先</button>
          </div>
        </div>
      </div>
    </section>

    <!-- 商品展示区 -->
    <main class="product-display" v-loading="loading">
      <div class="container">
        <div class="product-grid" :class="viewMode">
          <div
            class="product-card"
            v-for="product in products"
            :key="product.id"
            @click="goToProductDetail(product.id)"
          >
            <div class="product-image-wrapper">
              <img :src="product.mainImage || product.image" :alt="product.name" class="product-image">
              <span v-if="product.isHot === 1" class="product-tag hot">热销</span>
              <span v-if="product.isNew === 1" class="product-tag new">新品</span>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-rating">
                <i v-for="n in 5" :key="n"
                   :class="getStarClass(n, product.rating || 4)"
                ></i>
                <span class="rating-count">({{ product.reviewCount || product.sales || 0 }}条评价)</span>
              </div>
              <div class="product-meta">
                <span class="product-price">¥{{ product.price }}</span>
                <div class="favorite-btn" @click.stop="toggleFavorite(product)">
                  <i :class="['far fa-heart', product.isFavorite ? 'fas favorite-active' : '']"></i>
                </div>
              </div>
              <p class="product-desc">{{ product.description || '优质农产品' }}</p>
              <button class="add-cart-btn" @click.stop="addToCart(product)">加入购物车</button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && products.length === 0" class="empty-state">
          <i class="fas fa-inbox empty-icon"></i>
          <p>暂无符合条件的商品</p>
        </div>
      </div>
    </main>

    <!-- 分页区 -->
    <section class="pagination-section">
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
            @click="currentPage = page"
          >{{ page }}</button>
          <span v-if="showEllipsis" class="ellipsis">...</span>
          <button 
            v-if="totalPages > 5" 
            class="page-btn"
            :class="{ active: currentPage === totalPages }"
            @click="currentPage = totalPages"
          >{{ totalPages }}</button>
          <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">
            <i class="fas fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-links">
            <a class="footer-link" @click="navigateTo('home')">首页</a>
            <a class="footer-link" @click="navigateTo('category')">商品分类</a>
            <a class="footer-link" @click="navigateTo('help')">助农专区</a>
          </div>
          <div class="social-links">
            <a href="#" class="social-link"><i class="fab fa-weixin"></i></a>
            <a href="#" class="social-link"><i class="fab fa-weibo"></i></a>
            <a href="#" class="social-link"><i class="fas fa-phone"></i></a>
          </div>
          <div class="copyright">© 2026 绿色农产品商城 版权所有 | 联系电话：400-123-4567</div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { productApi, categoryApi, favoriteApi } from '../api'
import { ElMessage } from 'element-plus'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()

// 搜索关键词
const searchKeyword = ref('')

// 是否显示特惠或新鲜商品
const showSale = ref(false)
const showFresh = ref(false)

// 下拉菜单显示状态
const showCategoryDropdown = ref(false)
const showPriceDropdown = ref(false)
const showSortDropdown = ref(false)

// 当前筛选条件
const currentCategory = ref('')
const currentPriceRange = ref('')
const currentSort = ref('default')
const viewMode = ref('list')
const currentPage = ref(1)
const showMoreFilter = ref(false)
const customPriceMin = ref('')
const customPriceMax = ref('')

// 每页商品数
const pageSize = 8

// 加载状态
const loading = ref(false)

// 商品数据 - 从API获取
const products = ref([])
const total = ref(0)

// 分类数据 - 从API获取
const categories = ref([])

// 计算属性：当前排序标签
const currentSortLabel = computed(() => {
  const labels = {
    'default': '综合排序',
    'price-asc': '价格从低到高',
    'price-desc': '价格从高到低',
    'sales': '销量优先'
  }
  return labels[currentSort.value] || '综合排序'
})

// 计算属性：总页数
const totalPages = computed(() => Math.ceil(total.value / pageSize))

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await categoryApi.getCategories()
    categories.value = Array.isArray(res) ? res : (res?.list || res?.records || [])
    console.log('获取分类列表成功:', categories.value.length, '个分类')
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize,
      keyword: searchKeyword.value || undefined,
      categoryId: currentCategory.value || undefined,
      sort: currentSort.value !== 'default' ? currentSort.value : undefined
    }

    // 价格筛选处理
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
        case '100+':
          params.minPrice = 100
          break
      }
    } else if (customPriceMin.value || customPriceMax.value) {
      if (customPriceMin.value) params.minPrice = Number(customPriceMin.value)
      if (customPriceMax.value) params.maxPrice = Number(customPriceMax.value)
    }

    const res = await productApi.getProductList(params)
    products.value = res.records || res.list || []
    total.value = res.total || products.value.length

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
  } finally {
    loading.value = false
  }
}

// 方法：搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
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
    } else if (error.message?.includes('已收藏')) {
      product.isFavorite = true
      ElMessage.success('该商品已在收藏夹中')
    } else if (error.message?.includes('未收藏') || error.message?.includes('不存在')) {
      product.isFavorite = false
      ElMessage.success('收藏记录已更新')
    } else {
      ElMessage.warning('操作失败: ' + (error.message || '网络错误'))
    }
  }
}

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

// 方法：切换下拉菜单
const toggleCategoryDropdown = () => {
  showCategoryDropdown.value = !showCategoryDropdown.value
  showPriceDropdown.value = false
  showSortDropdown.value = false
}

const togglePriceDropdown = () => {
  showPriceDropdown.value = !showPriceDropdown.value
  showCategoryDropdown.value = false
  showSortDropdown.value = false
}

const toggleSortDropdown = () => {
  showSortDropdown.value = !showSortDropdown.value
  showCategoryDropdown.value = false
  showPriceDropdown.value = false
}

// 方法：选择筛选条件
const selectCategory = (category) => {
  currentCategory.value = category
  showCategoryDropdown.value = false
}

const selectPriceRange = (range) => {
  currentPriceRange.value = range
  showPriceDropdown.value = false
  customPriceMin.value = ''
  customPriceMax.value = ''
}

const applyCustomPrice = () => {
  const min = customPriceMin.value ? Number(customPriceMin.value) : null
  const max = customPriceMax.value ? Number(customPriceMax.value) : null
  if (min !== null && max !== null && min > max) {
    ElMessage.warning('最低价不能大于最高价')
    return
  }
  currentPriceRange.value = ''
  currentPage.value = 1
  fetchProducts()
}

const selectSort = (sort) => {
  currentSort.value = sort
  showSortDropdown.value = false
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

// 跳转到商品详情页
const goToProductDetail = (productId) => {
  router.push('/product/' + productId)
}

// 导航跳转函数
const navigateTo = (page) => {
  const routes = {
    home: '/',
    category: '/category',
    products: '/products',
    help: '/help',
    cart: '/cart',
    user: '/user',
    about: '/about',
    sale: '/special-offer',
    fresh: '/products?fresh=1'
  }
  if (routes[page]) {
    if (page === 'sale' || page === 'fresh') {
      const url = routes[page]
      if (url.includes('?')) {
        const path = url.split('?')[0]
        const query = {}
        url.split('?')[1].split('&').forEach(param => {
          const [key, value] = param.split('=')
          query[key] = value
        })
        router.push({ path, query })
      } else {
        router.push(url)
      }
    } else {
      router.push(routes[page])
    }
  }
}

// 检查 URL 参数的函数
const checkQueryParams = () => {
  showSale.value = route.query.sale === '1'
  showFresh.value = route.query.fresh === '1'
  if (showFresh.value) {
    currentSort.value = 'sales'
  }
  if (showSale.value) {
    currentSort.value = 'sales'
  }
}

// 页面加载时检查 URL 参数并获取商品
onMounted(async () => {
  await fetchCategories()
  checkQueryParams()
  fetchProducts()
  if (userStore.isLoggedIn) {
    cartStore.fetchCartList().catch(() => {})
  }
})

// 监听筛选条件变化
watch([currentCategory, currentPriceRange, currentSort, currentPage], () => {
  fetchProducts()
})

// 监听路由参数变化
watch(() => route.query, () => {
  checkQueryParams()
  currentPage.value = 1
  fetchProducts()
}, { deep: true })
</script>

<style scoped>
/* 基础样式 */
.product-list-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 顶部导航栏 */
.header {
  width: 100%;
  height: 80px;
  background: linear-gradient(135deg, #43A047 0%, #4CAF50 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-container {
  width: 1200px;
  height: 100%;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  font-size: 28px;
  color: #ffffff;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-link {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 8px 0;
}

.nav-link:hover,
.nav-link.active {
  color: #ffffff;
}

.nav-link.active {
  position: relative;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #ffffff;
  border-radius: 1px;
}

.header-icons {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.icon-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

.cart-btn {
  position: relative;
}

.cart-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 18px;
  height: 18px;
  background: #FF9800;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 600;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 搜索区 */
.search-section {
  width: 100%;
  height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
}

.search-box {
  position: relative;
  width: 100%;
  max-width: 600px;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: #999999;
  font-size: 14px;
}

.search-input {
  width: 100%;
  height: 40px;
  padding: 0 80px 0 36px;
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

.search-btn {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  padding: 6px 20px;
  background: #4CAF50;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: #43A047;
}

/* 筛选与排序区 */
.filter-section {
  width: 100%;
  height: 50px;
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
}

.filter-section .container {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.filter-dropdown {
  position: relative;
}

.dropdown-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
  color: #333333;
  cursor: pointer;
  transition: all 0.3s ease;
}

.dropdown-btn:hover {
  border-color: #4CAF50;
  color: #4CAF50;
}

.dropdown-btn i {
  font-size: 12px;
  transition: transform 0.3s ease;
}

.dropdown-btn i.rotate {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 4px;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  min-width: 120px;
  z-index: 100;
}

.dropdown-item {
  display: block;
  padding: 10px 16px;
  font-size: 13px;
  color: #333333;
  text-decoration: none;
  transition: all 0.3s ease;
}

.dropdown-item:hover {
  background: #E8F5E9;
  color: #4CAF50;
}

.filter-tags {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-btn {
  padding: 5px 14px;
  background: #E8F5E9;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  color: #4CAF50;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tag-btn:hover {
  background: #4CAF50;
  color: #ffffff;
}

.tag-btn.outline {
  background: #ffffff;
  border: 1px solid #e0e0e0;
  color: #666666;
}

.tag-btn.outline:hover,
.tag-btn.outline.active {
  border-color: #4CAF50;
  color: #4CAF50;
  background: #E8F5E9;
}

.tag-btn.active {
  background: #4CAF50;
  color: #ffffff;
}

.more-filter-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: transparent;
  border: none;
  font-size: 13px;
  color: #666666;
  cursor: pointer;
  transition: all 0.3s ease;
}

.more-filter-btn:hover {
  color: #4CAF50;
}

.more-filter-panel {
  background: #F9F9F9;
  padding: 16px 0;
  border-bottom: 1px solid #EEEEEE;
}

.filter-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-label {
  width: 80px;
  font-size: 13px;
  color: #666;
  line-height: 32px;
  flex-shrink: 0;
}

.filter-options {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-option-btn {
  padding: 6px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-option-btn:hover {
  border-color: #4CAF50;
  color: #4CAF50;
}

.filter-option-btn.active {
  background: #4CAF50;
  border-color: #4CAF50;
  color: #fff;
}

.custom-price {
  display: flex;
  align-items: center;
  gap: 6px;
}

.price-input {
  width: 80px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  outline: none;
}

.price-input:focus {
  border-color: #4CAF50;
}

.price-separator {
  color: #999;
}

.price-confirm-btn {
  height: 32px;
  padding: 0 12px;
  background: #4CAF50;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
}

.price-confirm-btn:hover {
  background: #43A047;
}

.view-toggle {
  display: flex;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.view-btn {
  width: 36px;
  height: 32px;
  background: #ffffff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-btn:first-child {
  border-right: 1px solid #e0e0e0;
}

.view-btn:hover {
  background: #f5f5f5;
}

.view-btn.active {
  background: #E8F5E9;
  color: #4CAF50;
}

/* 商品展示区 */
.product-display {
  flex: 1;
  padding: 24px 0;
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

.empty-state p {
  font-size: 16px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.product-card {
  background: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.product-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
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
  transition: transform 0.3s ease;
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

.product-tag.help {
  background: #4CAF50;
}

.product-tag.limited {
  background: #f44336;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 15px;
  font-weight: 500;
  color: #333333;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.product-price {
  font-size: 18px;
  font-weight: 700;
  color: #FF9800;
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

.product-desc {
  font-size: 12px;
  color: #666666;
  margin-bottom: 12px;
}

.add-cart-btn {
  width: 100%;
  height: 36px;
  background: #4CAF50;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-cart-btn:hover {
  background: #43A047;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

/* 分页区 */
.pagination-section {
  padding: 24px 0;
  background: #ffffff;
  border-top: 1px solid #e8e8e8;
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
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #666666;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  border-color: #4CAF50;
  color: #4CAF50;
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
  width: 32px;
  text-align: center;
  color: #999999;
}

/* 页脚 */
.footer {
  width: 100%;
  height: 50px;
  background: #333333;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.footer .container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 24px;
}

.footer-link {
  font-size: 14px;
  color: #ffffff;
  text-decoration: none;
  transition: all 0.3s ease;
}

.footer-link:hover {
  color: #FF9800;
}

.social-links {
  display: flex;
  align-items: center;
  gap: 16px;
}

.social-link {
  font-size: 18px;
  color: #ffffff;
  text-decoration: none;
  transition: all 0.3s ease;
}

.social-link:hover {
  color: #FF9800;
}

.copyright {
  font-size: 14px;
  color: #999999;
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
    grid-template-columns: repeat(3, 1fr);
  }
  
  .filter-left {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .nav-menu {
    display: none;
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 12px;
    padding: 16px 0;
    height: auto;
  }
  
  .header {
    height: 60px;
  }
  
  .logo-text {
    font-size: 18px;
  }
}
</style>
