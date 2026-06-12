<template>
  <div class="search-result-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <a href="#" class="logo" @click.prevent="goToHome">
          <i class="fas fa-apple-alt logo-icon"></i>
          <span class="logo-text">农鲜生</span>
        </a>
        
        <div class="search-box">
          <div class="search-input-wrapper">
            <i class="fas fa-search search-icon"></i>
            <input type="text" v-model="searchKeyword" placeholder="搜索农产品、生鲜、水果..." class="search-input" @keyup.enter="handleSearch">
            <button class="search-btn" @click="handleSearch">搜索</button>
          </div>
        </div>
        
        <div class="header-actions">
          <a href="#" class="action-btn" @click.prevent="goToUserCenter">
            <i class="fas fa-user"></i>
          </a>
          <a href="#" class="action-btn cart-btn" @click.prevent="goToCart">
            <i class="fas fa-shopping-cart"></i>
            <span class="cart-badge">3</span>
          </a>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <main class="main-content">
      <div class="container">
        <div class="content-wrapper">
          <!-- 左侧筛选栏 -->
          <aside class="filter-sidebar">
            <!-- 商品分类 -->
            <div class="filter-section">
              <h3 class="filter-title">商品分类</h3>
              <div class="filter-options">
                <label class="filter-option" v-for="cat in categories" :key="cat.id">
                  <input type="checkbox" v-model="selectedCategories" :value="String(cat.id)" @change="handleCategoryChange">
                  <span>{{ cat.name }}</span>
                </label>
              </div>
            </div>

            <!-- 价格区间 -->
            <div class="filter-section">
              <h3 class="filter-title">价格区间</h3>
              <div class="price-inputs">
                <input type="number" v-model.number="minPriceInput" placeholder="¥" class="price-input">
                <span class="price-separator">-</span>
                <input type="number" v-model.number="maxPriceInput" placeholder="¥" class="price-input">
              </div>
              <button class="confirm-btn" @click="handlePriceConfirm">确定</button>
              <div class="price-radios">
                <label class="filter-option">
                  <input type="radio" v-model="priceRange" value="0-50" @change="handlePriceRangeChange">
                  <span>¥0 - ¥50</span>
                </label>
                <label class="filter-option">
                  <input type="radio" v-model="priceRange" value="50-100" @change="handlePriceRangeChange">
                  <span>¥50 - ¥100</span>
                </label>
                <label class="filter-option">
                  <input type="radio" v-model="priceRange" value="100-200" @change="handlePriceRangeChange">
                  <span>¥100 - ¥200</span>
                </label>
                <label class="filter-option">
                  <input type="radio" v-model="priceRange" value="200+" @change="handlePriceRangeChange">
                  <span>¥200以上</span>
                </label>
              </div>
            </div>
          </aside>

          <!-- 右侧结果区 -->
          <div class="result-content">
            <!-- 结果头部 -->
            <div class="result-header">
              <div class="result-count">
                找到 <span class="count-highlight">{{ total }}</span> 件相关商品
              </div>
              <div class="sort-dropdown">
                <select v-model="sortBy" class="sort-select">
                  <option value="default">默认排序</option>
                  <option value="price-asc">价格从低到高</option>
                  <option value="price-desc">价格从高到低</option>
                  <option value="sales">销量优先</option>
                  <option value="rating">评分优先</option>
                </select>
                <i class="fas fa-chevron-down sort-icon"></i>
              </div>
            </div>

            <!-- 商品网格 -->
            <div class="products-grid">
              <div class="product-card" v-for="product in products" :key="product.id" @click="goToProductDetail(product.id)">
                <div class="product-image-wrapper">
                  <img :src="product.image" :alt="product.name" class="product-image">
                </div>
                <div class="product-info">
                  <h4 class="product-name">{{ product.name }}</h4>
                  <div class="product-rating">
                    <div class="rating-stars">
                      <i v-for="n in 5" :key="n" :class="getStarClass(n, product.rating)"></i>
                    </div>
                    <span class="rating-count">({{ product.reviewCount }})</span>
                  </div>
                  <div class="product-price">¥{{ product.price }}</div>
                  <button class="add-cart-btn" @click.stop="addToCart(product)">
                    <i class="fas fa-shopping-cart"></i>
                    加入购物车
                  </button>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div class="pagination">
              <button class="page-btn" :disabled="currentPage === 1" @click="prevPage">
                <i class="fas fa-chevron-left"></i>
                <span>上一页</span>
              </button>
              <button 
                v-for="page in displayedPages" 
                :key="page"
                class="page-btn"
                :class="{ active: currentPage === page }"
                @click="changePage(page)"
              >
                {{ page }}
              </button>
              <button class="page-btn" :disabled="currentPage === totalPages" @click="nextPage">
                <span>下一页</span>
                <i class="fas fa-chevron-right"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { productApi, categoryApi, cartApi } from '../api'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 搜索关键词
const searchKeyword = ref('')

// 筛选状态
const selectedCategories = ref([])
const priceRange = ref('')
const minPriceInput = ref(null)
const maxPriceInput = ref(null)
const minPrice = ref(undefined)
const maxPrice = ref(undefined)
const sortBy = ref('default')
const currentPage = ref(1)
const pageSize = 12
const total = ref(0)
const loading = ref(false)

// 商品数据 - 从API获取
const products = ref([])

// 分类数据 - 从API获取
const categories = ref([])

// 计算属性：总页数
const totalPages = computed(() => Math.ceil(total.value / pageSize) || 1)

// 计算属性：显示的页码
const displayedPages = computed(() => {
  const pages = []
  const start = Math.max(1, Math.min(currentPage.value - 2, totalPages.value - 4))
  const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await categoryApi.getCategories()
    categories.value = Array.isArray(res) ? res : (res?.list || res?.records || [])
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
      sort: sortBy.value !== 'default' ? sortBy.value : undefined
    }

    // 分类筛选：取第一个选中的分类
    if (selectedCategories.value.length > 0) {
      params.categoryId = selectedCategories.value[0]
    }

    // 价格区间筛选
    if (minPrice.value !== undefined && minPrice.value !== null) {
      params.minPrice = minPrice.value
    }
    if (maxPrice.value !== undefined && maxPrice.value !== null) {
      params.maxPrice = maxPrice.value
    }

    const res = await productApi.getProductList(params)

    if (res) {
      const list = res.list || res.records || (Array.isArray(res) ? res : [])
      const totalCount = res.total || list.length
      products.value = list.map(item => ({
        id: item.id,
        name: item.name,
        image: item.mainImage || item.image,
        price: String(item.price),
        originalPrice: item.originalPrice ? String(item.originalPrice) : undefined,
        rating: item.rating || 4.5,
        reviewCount: item.reviewCount || item.sales || 0,
        sales: item.sales || 0,
        isHot: item.isHot || 0,
        isNew: item.isNew || 0
      }))
      total.value = totalCount
    }
  } catch (error) {
    console.error('获取搜索结果失败:', error)
    ElMessage.error('获取商品列表失败')
    products.value = []
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

// 分类变更处理
const handleCategoryChange = () => {
  currentPage.value = 1
  fetchProducts()
}

// 价格区间快捷选择
const handlePriceRangeChange = () => {
  minPriceInput.value = null
  maxPriceInput.value = null
  if (priceRange.value === '0-50') {
    minPrice.value = 0
    maxPrice.value = 50
  } else if (priceRange.value === '50-100') {
    minPrice.value = 50
    maxPrice.value = 100
  } else if (priceRange.value === '100-200') {
    minPrice.value = 100
    maxPrice.value = 200
  } else if (priceRange.value === '200+') {
    minPrice.value = 200
    maxPrice.value = undefined
  } else {
    minPrice.value = undefined
    maxPrice.value = undefined
  }
  currentPage.value = 1
  fetchProducts()
}

// 自定义价格区间确认
const handlePriceConfirm = () => {
  minPrice.value = minPriceInput.value !== null && minPriceInput.value !== '' ? minPriceInput.value : undefined
  maxPrice.value = maxPriceInput.value !== null && maxPriceInput.value !== '' ? maxPriceInput.value : undefined
  priceRange.value = ''
  currentPage.value = 1
  fetchProducts()
}

// 获取星星类名
const getStarClass = (n, rating) => {
  if (n <= Math.floor(rating)) {
    return 'fas fa-star star-filled'
  } else if (n === Math.ceil(rating) && rating % 1 !== 0) {
    return 'fas fa-star-half-alt star-filled'
  } else {
    return 'far fa-star'
  }
}

// 方法 - 添加到购物车
const addToCart = async (product) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await cartApi.addToCart({
      productId: product.id,
      quantity: 1
    })
    ElMessage.success(`已将 "${product.name}" 加入购物车`)
  } catch (error) {
    console.error('添加购物车失败:', error)
    if (error.message?.includes('401') || error.message?.includes('未登录')) {
      ElMessage.error('请先登录')
      router.push('/login')
    } else {
      ElMessage.error('添加失败: ' + (error.message || '网络错误'))
    }
  }
}

const goToHome = () => {
  router.push('/')
}

const goToUserCenter = () => {
  router.push('/user')
}

const goToCart = () => {
  router.push('/cart')
}

const goToProductDetail = (productId) => {
  router.push('/product/' + productId)
}

// 切换页码
const changePage = (page) => {
  currentPage.value = page
  fetchProducts()
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    fetchProducts()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    fetchProducts()
  }
}

// 监听排序变化
watch(sortBy, () => {
  currentPage.value = 1
  fetchProducts()
})

// 监听页码变化
watch(currentPage, () => {
  fetchProducts()
})

// 页面加载时获取数据
onMounted(async () => {
  // 从路由参数获取搜索关键词
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword
  }
  await fetchCategories()
  fetchProducts()
})

// 监听路由参数变化
watch(() => route.query.keyword, (newKeyword) => {
  if (newKeyword !== undefined) {
    searchKeyword.value = newKeyword || ''
    currentPage.value = 1
    fetchProducts()
  }
})
</script>

<style scoped>
/* 基础样式 */
.search-result-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 顶部导航栏 */
.header {
  width: 100%;
  height: 80px;
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

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.logo-icon {
  font-size: 28px;
  color: #4a90e2;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #333333;
}

.search-box {
  flex: 1;
  max-width: 600px;
  margin: 0 32px;
}

.search-input-wrapper {
  position: relative;
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
  border-color: #4a90e2;
}

.search-btn {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  padding: 6px 16px;
  background: #4a90e2;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: #357abd;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 24px;
}

.action-btn {
  font-size: 20px;
  color: #333333;
  text-decoration: none;
  transition: all 0.3s ease;
}

.action-btn:hover {
  color: #4a90e2;
}

.cart-btn {
  position: relative;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 18px;
  height: 18px;
  background: #5cb85c;
  border-radius: 50%;
  font-size: 10px;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 主体内容 */
.main-content {
  padding: 24px 0;
}

.content-wrapper {
  display: flex;
  gap: 24px;
}

/* 左侧筛选栏 */
.filter-sidebar {
  width: 240px;
  flex-shrink: 0;
}

.filter-section {
  background: #f0f7ff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.filter-title {
  font-size: 14px;
  font-weight: 700;
  color: #333333;
  margin-bottom: 16px;
}

.filter-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-option {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333333;
  cursor: pointer;
}

.filter-option input[type="checkbox"],
.filter-option input[type="radio"] {
  width: 16px;
  height: 16px;
  accent-color: #4a90e2;
}

.price-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.price-input {
  width: 80px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
  outline: none;
}

.price-separator {
  color: #999999;
}

.confirm-btn {
  width: 100%;
  height: 36px;
  background: #4a90e2;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.confirm-btn:hover {
  background: #357abd;
}

.price-radios {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.more-btn {
  width: 100%;
  height: 36px;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
  color: #333333;
  cursor: pointer;
  margin-top: 12px;
  transition: all 0.3s ease;
}

.more-btn:hover {
  border-color: #4a90e2;
  color: #4a90e2;
}

/* 右侧结果区 */
.result-content {
  flex: 1;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.result-count {
  font-size: 14px;
  color: #333333;
}

.count-highlight {
  color: #4a90e2;
  font-weight: 700;
}

.sort-dropdown {
  position: relative;
}

.sort-select {
  width: 160px;
  height: 36px;
  padding: 0 32px 0 12px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 14px;
  color: #333333;
  background: #ffffff;
  appearance: none;
  cursor: pointer;
  outline: none;
}

.sort-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #999999;
  font-size: 12px;
  pointer-events: none;
}

/* 商品网格 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.product-card {
  background: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
}

.product-image-wrapper {
  width: 100%;
  height: 180px;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #333333;
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
  gap: 6px;
  margin-bottom: 8px;
}

.rating-stars {
  display: flex;
  gap: 2px;
  font-size: 12px;
}

.rating-stars .star-filled {
  color: #ffc107;
}

.rating-stars .fa-star:not(.star-filled) {
  color: #e0e0e0;
}

.rating-count {
  font-size: 12px;
  color: #999999;
}

.product-price {
  font-size: 18px;
  font-weight: 700;
  color: #4a90e2;
  margin-bottom: 12px;
}

.add-cart-btn {
  width: 100%;
  height: 36px;
  background: #5cb85c;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-cart-btn:hover {
  background: #4cae4c;
}

/* 分页 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.page-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 12px;
  height: 36px;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 14px;
  color: #333333;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: #f5f5f5;
}

.page-btn.active {
  background: #4a90e2;
  border-color: #4a90e2;
  color: #ffffff;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn i {
  font-size: 12px;
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
  
  .search-box {
    margin: 0 16px;
  }
}

@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
  
  .filter-sidebar {
    width: 100%;
  }
  
  .products-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .header {
    height: 60px;
  }
  
  .logo-text {
    display: none;
  }
}
</style>
