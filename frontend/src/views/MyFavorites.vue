<template>
  <div class="my-favorites-page">
    <!-- 顶部导航栏 -->
    <nav class="top-nav">
      <div class="nav-container">
        <div class="nav-left">
          <a href="#" class="back-btn" @click.prevent="goBack">
            <i class="fas fa-arrow-left"></i>
          </a>
          <h1 class="page-title">我的收藏</h1>
        </div>
        <div class="nav-right">
          <button class="more-btn" @click="showMore">
            <i class="fas fa-ellipsis-v"></i>
          </button>
        </div>
      </div>
    </nav>

    <!-- 筛选/排序控件 -->
    <div class="filter-section">
      <div class="filter-container">
        <div class="filter-left">
          <button class="filter-btn active" @click="showFilter">
            <i class="fas fa-filter"></i>
            <span>筛选</span>
          </button>
          <div class="dropdown-wrapper">
            <button class="dropdown-btn" @click="toggleCategory">
              <span>分类</span>
              <i class="fas fa-chevron-down"></i>
            </button>
          </div>
          <div class="dropdown-wrapper">
            <button class="dropdown-btn" @click="togglePrice">
              <span>价格</span>
              <i class="fas fa-chevron-down"></i>
            </button>
          </div>
        </div>
        <div class="filter-right">
          <div class="dropdown-wrapper">
            <button class="dropdown-btn" @click="toggleSort">
              <span>默认排序</span>
              <i class="fas fa-chevron-down"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 收藏列表区域 -->
    <main class="main-content" v-loading="loading">
      <div class="content-container">
        <!-- 收藏商品卡片网格 -->
        <div class="favorites-grid">
          <div
            v-for="product in favoriteProducts"
            :key="product.id"
            class="favorite-card"
          >
            <!-- 商品图片 -->
            <div class="card-image-wrapper" @click="viewDetail(product.id)">
              <img :src="product.mainImage || product.image" :alt="product.name" class="card-image">
              <span v-if="product.isHot === 1 || product.tagType === 'hot'" class="card-tag hot">热销</span>
              <span v-if="product.isNew === 1 || product.tagType === 'new'" class="card-tag new">新品</span>
              <button class="remove-btn" :class="{ 'is-removing': removingIds.has(product.productId || product.id) }" @click.stop="removeFavorite(product)">
              <i class="fas fa-heart"></i>
            </button>
            </div>

            <!-- 商品信息 -->
            <div class="card-info">
              <h3 class="card-title">{{ product.name }}</h3>
              <p class="card-origin" v-if="product.origin">
                <i class="fas fa-map-marker-alt"></i>
                {{ product.origin }}
              </p>
              <div class="card-rating">
                <div class="rating-stars">
                  <i v-for="n in 5" :key="n" :class="getStarClass(4, n)"></i>
                </div>
                <span class="rating-count">({{ product.sales || 0 }})</span>
              </div>
              <div class="card-bottom">
                <span class="card-price">¥{{ product.price }}</span>
                <button class="view-detail-btn" @click="viewDetail(product.id)">查看详情</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && favoriteProducts.length === 0" class="empty-state">
          <i class="fas fa-heart empty-icon"></i>
          <p>暂无收藏商品</p>
          <button class="go-shopping-btn" @click="goHome()">
            去逛逛
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { favoriteApi, cartApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const removingIds = ref(new Set())
const favoriteProducts = ref([])

const fetchFavorites = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    favoriteProducts.value = []
    return
  }

  loading.value = true
  try {
    const res = await favoriteApi.getFavoriteList({ page: 1, size: 100 })

    let items = []
    if (res && res.list && Array.isArray(res.list)) {
      items = res.list
    } else if (res && res.records && Array.isArray(res.records)) {
      items = res.records
    } else if (Array.isArray(res)) {
      items = res
    } else {
      favoriteProducts.value = []
      return
    }

    favoriteProducts.value = items.map(item => {
      const vo = item
      const productId = vo.productId || (vo.product && vo.product.id) || vo.id
      return {
        id: productId,
        productId: productId,
        favoriteId: vo.id,
        name: vo.name || vo.productName || (vo.product && vo.product.name) || '未知商品',
        price: vo.price || (vo.product && vo.product.price) || 0,
        mainImage: vo.mainImage || vo.image || (vo.product && vo.product.mainImage) || '',
        origin: vo.origin || (vo.product && vo.product.origin) || '',
        isFavorite: true
      }
    })
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    ElMessage.error('获取收藏列表失败: ' + (error.message || '网络错误'))
    favoriteProducts.value = []
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (product) => {
  const targetId = product.productId || product.id
  if (!targetId) {
    ElMessage.error('商品信息异常，无法取消收藏')
    return
  }

  if (removingIds.value.has(targetId)) {
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要取消收藏 "${product.name}" 吗？`,
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
  } catch (action) {
    return
  }

  removingIds.value.add(targetId)

  const backup = [...favoriteProducts.value]

  favoriteProducts.value = favoriteProducts.value.filter(
    p => (p.productId || p.id) !== targetId
  )

  try {
    await favoriteApi.removeFavorite(targetId)
    ElMessage.success('已取消收藏')
  } catch (error) {
    favoriteProducts.value = backup

    if (error.message?.includes('401')) {
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else if (error.message?.includes('不存在') || error.message?.includes('404')) {
      favoriteProducts.value = favoriteProducts.value.filter(
        p => (p.productId || p.id) !== targetId
      )
      ElMessage.success('收藏记录已移除')
    } else {
      ElMessage.error('取消收藏失败: ' + (error.message || '网络错误'))
    }
  } finally {
    removingIds.value.delete(targetId)
  }
}

const getStarClass = (rating, index) => {
  if (index <= Math.floor(rating)) {
    return 'fas fa-star'
  } else if (index === Math.ceil(rating) && rating % 1 !== 0) {
    return 'fas fa-star-half-alt'
  } else {
    return 'far fa-star'
  }
}

const goBack = () => {
  router.back()
}

const showMore = () => {}

const showFilter = () => {}

const toggleCategory = () => {}

const togglePrice = () => {}

const toggleSort = () => {}

const viewDetail = (productId) => {
  router.push('/product/' + productId)
}

const addToCart = async (product) => {
  try {
    await cartApi.addToCart({
      productId: product.id,
      quantity: 1
    })
    ElMessage.success(`已将 "${product.name}" 加入购物车`)
  } catch (error) {
    if (error.message?.includes('401') || error.message?.includes('未登录')) {
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error('添加购物车失败: ' + (error.message || '网络错误'))
    }
  }
}

const goHome = () => {
  router.push('/')
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped>
.my-favorites-page {
  min-height: 100vh;
  background-color: #ffffff;
}

/* 顶部导航栏 */
.top-nav {
  width: 100%;
  height: 64px;
  background-color: #ffffff;
  border-bottom: 1px solid #f2f3f5;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1d2129;
  text-decoration: none;
  font-size: 18px;
  transition: color 0.2s;
}

.back-btn:hover {
  color: #4a6cf7;
}

.page-title {
  font-size: 24px;
  font-weight: 500;
  color: #1d2129;
  margin: 0;
}

.more-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  color: #1d2129;
  font-size: 18px;
  cursor: pointer;
  transition: color 0.2s;
}

.more-btn:hover {
  color: #4a6cf7;
}

/* 筛选区域 */
.filter-section {
  width: 100%;
  padding: 96px 0 24px;
}

.filter-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background-color: #4a6cf7;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.filter-btn:hover {
  background-color: #3d5ce0;
}

.filter-btn i {
  font-size: 12px;
}

.dropdown-wrapper {
  position: relative;
}

.dropdown-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background-color: #ffffff;
  border: 1px solid #f2f3f5;
  border-radius: 8px;
  font-size: 14px;
  color: #1d2129;
  cursor: pointer;
  transition: all 0.2s;
}

.dropdown-btn:hover {
  border-color: #4a6cf7;
}

.dropdown-btn i {
  font-size: 10px;
  color: #86909c;
}

/* 主内容区 */
.main-content {
  flex: 1;
  overflow: auto;
  padding: 24px 0;
}

/* 收藏商品卡片网格 */
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 0 24px;
}

@media (max-width: 1200px) {
  .favorites-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .favorites-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .favorites-grid {
    grid-template-columns: 1fr;
  }
}

.favorite-card {
  background: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s;
}

.favorite-card:hover {
  transform: translateY(-4px);
}

.card-image-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  background: #fff;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff5252;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

.remove-btn:hover {
  transform: scale(1.15);
  box-shadow: 0 3px 10px rgba(255, 82, 82, 0.3);
}

.remove-btn.is-removing {
  pointer-events: none;
  opacity: 0.5;
  animation: pulse 0.8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(0.9); }
}

.card-info {
  padding: 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-origin {
  font-size: 12px;
  color: #999;
  margin: 0 0 6px 0;
}

.card-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
}

.rating-stars {
  display: flex;
  gap: 1px;
  color: #ffc107;
  font-size: 12px;
}

.rating-count {
  font-size: 12px;
  color: #999;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-price {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.view-detail-btn {
  padding: 6px 16px;
  background-color: #ff5252;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.view-detail-btn:hover {
  background-color: #e04848;
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

.go-shopping-btn {
  display: inline-block;
  padding: 10px 30px;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.go-shopping-btn:hover {
  background-color: var(--hover-color);
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}

.product-card {
  background-color: #f0f2f5;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.product-card:hover {
  transform: scale(1.02);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  background-color: #f5f5f5;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.favorite-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  background-color: #ffffff;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff5252;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

.favorite-btn:hover {
  transform: scale(1.1);
}

.favorite-btn.active {
  color: #ff5252;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  color: #1d2129;
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.stars {
  display: flex;
  gap: 2px;
  color: #ffc107;
  font-size: 12px;
}

.review-count {
  font-size: 12px;
  color: #86909c;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 18px;
  font-weight: bold;
  color: #1d2129;
}

.view-detail-btn {
  padding: 8px 16px;
  background-color: #ff5252;
  color: #ffffff;
  border-radius: 8px;
  font-size: 14px;
  text-decoration: none;
  transition: background-color 0.2s;
}

.view-detail-btn:hover {
  background-color: #e04848;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 200px);
  text-align: center;
}

.empty-icon {
  width: 120px;
  height: 120px;
  background-color: #f0f2f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.empty-icon i {
  font-size: 48px;
  color: #86909c;
}

.empty-title {
  font-size: 24px;
  font-weight: 500;
  color: #1d2129;
  margin: 0 0 12px 0;
}

.empty-desc {
  font-size: 16px;
  color: #86909c;
  margin: 0 0 24px 0;
}

.go-home-btn {
  padding: 10px 24px;
  background-color: #4a6cf7;
  color: #ffffff;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: background-color 0.2s;
}

.go-home-btn:hover {
  background-color: #3d5ce0;
}
</style>
