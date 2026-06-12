<template>
  <div class="product-detail-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <div class="container">
        <div class="breadcrumb-content">
          <a href="#" class="breadcrumb-link" @click.prevent="goToHome">首页</a>
          <i class="fas fa-chevron-right breadcrumb-separator"></i>
          <a href="#" class="breadcrumb-link" @click.prevent="goToCategory">{{ categoryName }}</a>
          <i class="fas fa-chevron-right breadcrumb-separator"></i>
          <span class="breadcrumb-current">{{ product.name || '商品详情' }}</span>
        </div>
      </div>
    </div>

    <!-- 商品详情主体 -->
    <div class="product-main" v-loading="loading">
      <div class="container">
        <div class="product-content">
          <!-- 左侧图片区 -->
          <div class="product-gallery">
            <!-- 主图 -->
            <div class="main-image-wrapper" v-if="currentImage">
              <img :src="currentImage" alt="商品主图" class="main-image">
              <button class="nav-btn prev-btn" @click="prevImage" v-if="productImages.length > 1">
                <i class="fas fa-chevron-left"></i>
              </button>
              <button class="nav-btn next-btn" @click="nextImage" v-if="productImages.length > 1">
                <i class="fas fa-chevron-right"></i>
              </button>
            </div>

            <!-- 缩略图 -->
            <div class="thumbnail-list" v-if="productImages.length > 0">
              <div
                v-for="(img, index) in productImages"
                :key="index"
                class="thumbnail-item"
                :class="{ active: currentImageIndex === index }"
                @click="selectImage(index)"
              >
                <img :src="img" :alt="`缩略图${index + 1}`">
              </div>
            </div>

            <!-- 助农故事 -->
            <div class="farmer-story" v-if="productDetail && productDetail.farmerStory">
              <h3 class="story-title">助农故事</h3>
              <div class="story-content">
                <img :src="productDetail.farmerImage || product.mainImage" alt="助农故事" class="story-image">
                <div class="story-text">
                  <p>{{ productDetail.farmerStory }}</p>
                  <a href="#" class="story-link" @click.prevent="viewFullStory">
                    查看完整故事
                  </a>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧信息区 -->
          <div class="product-info">
            <!-- 商品标题 -->
            <div class="product-header">
              <h1 class="product-title">{{ product.name || '云南高山有机生态苹果 新鲜采摘当季水果' }}</h1>
              <p class="product-subtitle">{{ product.subtitle || '产地直供 | 自然成熟 | 清甜多汁' }}</p>
            </div>

            <!-- 价格和促销 -->
            <div class="product-price-section">
              <span class="product-price">¥{{ product.price || '59.90' }}</span>
              <span class="promo-tag">限时优惠</span>
            </div>

            <!-- 评分 -->
            <div class="product-rating">
              <div class="rating-stars">
                <i v-for="n in 5" :key="n" :class="n <= Math.round(product.rating || 4.7) ? 'fas fa-star' : 'far fa-star'"></i>
              </div>
              <span class="rating-text">{{ product.rating || '4.7' }} ({{ product.reviewCount || 126 }}人评价)</span>
            </div>

            <!-- 规格选择 -->
            <div class="spec-section">
              <h3 class="section-label">
                <i class="fas fa-cubes"></i>
                选择规格
              </h3>
              <div class="spec-options">
                <button
                  v-for="(spec, index) in specifications"
                  :key="index"
                  class="spec-btn"
                  :class="{ active: selectedSpec === index }"
                  @click="selectedSpec = index"
                >
                  {{ spec.name }}
                </button>
              </div>
            </div>

            <!-- 数量调整 -->
            <div class="quantity-section">
              <h3 class="section-label">购买数量</h3>
              <div class="quantity-control">
                <button class="quantity-btn minus" @click="decreaseQuantity">
                  <i class="fas fa-minus"></i>
                </button>
                <input type="number" v-model.number="quantity" class="quantity-input" min="1" :max="product.stock || 999">
                <button class="quantity-btn plus" @click="increaseQuantity">
                  <i class="fas fa-plus"></i>
                </button>
              </div>
            </div>

            <!-- 库存状态 -->
            <div class="stock-status">
              <i class="fas fa-check-circle"></i>
              库存{{ (product.stock || 238) > 0 ? '充足' : '不足' }} (剩余{{ product.stock || 238 }}件)
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <button class="btn-add-cart" @click="addToCart">
                <i class="fas fa-shopping-cart"></i>
                加入购物车
              </button>
              <button class="btn-buy-now" @click="buyNow">
                <i class="fas fa-credit-card"></i>
                立即购买
              </button>
            </div>

            <!-- 商品参数 -->
            <div class="product-params">
              <div class="params-header" @click="toggleParams">
                <h3 class="section-label">商品参数</h3>
                <i class="fas fa-chevron-down" :class="{ 'rotate': showParams }"></i>
              </div>
              <div class="params-table" v-show="showParams">
                <table>
                  <tbody>
                    <tr v-for="(param, index) in productParams" :key="index">
                      <td class="param-label">{{ param.label }}</td>
                      <td class="param-value">{{ param.value }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- 发布者信息（农户商品） -->
            <div class="seller-info" v-if="product.farmerId || product.productType === 1">
              <div class="seller-header">
                <h3 class="section-label">
                  <i class="fas fa-store"></i>
                  卖家信息
                </h3>
                <el-tag type="success" size="small" v-if="product.productType === 1">农户直供</el-tag>
              </div>
              <div class="seller-content">
                <div class="seller-avatar">
                  <el-avatar :size="50" :src="sellerInfo.avatar || undefined">
                    {{ sellerInfo.name?.charAt(0) || '农' }}
                  </el-avatar>
                </div>
                <div class="seller-detail">
                  <div class="seller-name">{{ sellerInfo.name || '农户店铺' }}</div>
                  <div class="shop-name" v-if="sellerInfo.shopName">
                    <i class="fas fa-shop"></i> {{ sellerInfo.shopName }}
                  </div>
                  <div class="seller-location" v-if="sellerInfo.location">
                    <i class="fas fa-map-marker-alt"></i> {{ sellerInfo.location }}
                  </div>
                </div>
                <div class="seller-actions">
                  <el-button type="primary" link size="small" @click="goToShop">
                    <i class="fas fa-external-link-alt"></i> 进店看看
                  </el-button>
                  <el-button link size="small" @click="contactSeller">
                    <i class="fas fa-comment-dots"></i> 联系卖家
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 举报按钮 -->
            <div class="report-section">
              <el-button link type="info" size="small" @click="showReportDialog = true">
                <i class="fas fa-flag"></i> 举报商品
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品评价 -->
    <div class="container" style="margin-top: 20px;">
      <ProductReviews
        :productId="productId"
        :canReview="userStore.isLoggedIn && hasPurchased"
        :isFarmer="userStore.isFarmer && product.farmerId === userStore.userInfo.id"
      />
    </div>

    <!-- 举报弹窗 -->
    <ReportProduct
      v-model="showReportDialog"
      :productId="productId"
      :productName="product.name"
      @success="handleReportSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { productApi, cartApi, favoriteApi } from '../api'
import { ElMessage } from 'element-plus'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'
import ProductReviews from './ProductReviews.vue'
import ReportProduct from './ReportProduct.vue'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()

// 加载状态
const loading = ref(false)

// 商品详情数据
const product = ref({})
const productDetail = ref({})
const productId = computed(() => route.params.id)
const categoryName = computed(() => product.value.categoryName || '新鲜水果')

// 商品图片
const productImages = ref([])
const currentImageIndex = ref(0)
const currentImage = computed(() => productImages.value[currentImageIndex.value] || '')

// 规格选项
const specifications = ref([
  { name: '5斤装 (约8-10个)', price: 59.90 },
  { name: '10斤装 (约16-20个)', price: 108.00 },
  { name: '精品礼盒装 (8个装)', price: 128.00 }
])
const selectedSpec = ref(0)

// 数量
const quantity = ref(1)

// 参数展开状态
const showParams = ref(true)

// 发布者信息
const sellerInfo = ref({
  name: '',
  avatar: '',
  shopName: '',
  location: ''
})

// 是否已购买(用于判断是否可以评价)
const hasPurchased = ref(false)

// 举报弹窗
const showReportDialog = ref(false)

// 商品参数
const productParams = ref([
  { label: '商品名称', value: '云南高山有机生态苹果' },
  { label: '产地', value: '云南昭通' },
  { label: '保质期', value: '15天' },
  { label: '储存方式', value: '阴凉干燥处存放，冷藏更佳' },
  { label: '发货时间', value: '下单后48小时内' }
])

// 获取商品详情
const fetchProductDetail = async () => {
  loading.value = true
  try {
    // 获取基础商品信息
    const res = await productApi.getProductDetail(productId.value)
    product.value = res || {}

    // 设置商品图片（最多5张）
    if (res.mainImage) {
      productImages.value = [res.mainImage]
      if (res.images && res.images.length > 0) {
        productImages.value = [...productImages.value, ...res.images].slice(0, 5)
      }
    } else {
      // 默认图片
      productImages.value = [
        'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600',
        'https://images.unsplash.com/photo-1570913149827-d2ac84ab3f9a?w=600',
        'https://images.unsplash.com/photo-1568702846914-96b305d2ebb2?w=600',
        'https://images.unsplash.com/photo-1584306670957-acf935f5033c?w=600',
        'https://images.unsplash.com/photo-1619546813926-a78fa6372cd2?w=600'
      ]
    }

    // 尝试获取商品详情信息
    try {
      const detailRes = await productApi.getProductDetailInfo(productId.value)
      productDetail.value = detailRes || {}
      
      // 用详情中的图片替换（最多5张）
      if (detailRes.images && detailRes.images.length > 0) {
        productImages.value = detailRes.images.slice(0, 5)
      }
      
      // 如果有详情参数，更新商品参数
      if (detailRes.params && detailRes.params.length > 0) {
        productParams.value = detailRes.params
      }
      
      // 如果有助农故事
      if (detailRes.farmerStory) {
        productDetail.value.farmerStory = detailRes.farmerStory
      }
    } catch (e) {
      // 使用默认数据
      productDetail.value = {
        farmerStory: '李大叔是云南昭通的苹果种植户，种植苹果已有20年。今年受疫情影响，苹果销路受阻，大量果实面临滞销。通过我们的助农平台，李大叔的苹果被更多人了解和购买，解决了销售难题，保障了家庭收入。每购买一份苹果，都是对果农们辛勤劳动的支持。感谢您的爱心助农！',
        farmerImage: productImages.value[0]
      }
    }

    // 设置规格
    if (res.specifications && res.specifications.length > 0) {
      specifications.value = res.specifications.map((s, i) => ({
        name: s,
        price: res.price
      }))
    }

    // 设置发布者信息（农户商品）
    if (res.farmerId || res.productType === 1) {
      sellerInfo.value = {
        name: res.farmerName || '农户',
        avatar: res.farmerAvatar || '',
        shopName: res.shopName || '',
        location: [res.province, res.city, res.district].filter(Boolean).join(' '),
        contactPhone: res.contactPhone || ''
      }
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
    // 使用默认数据
    product.value = {
      name: '云南高山有机生态苹果 新鲜采摘当季水果',
      subtitle: '产地直供 | 自然成熟 | 清甜多汁',
      price: 59.90,
      rating: 4.7,
      reviewCount: 126,
      stock: 238
    }
    productImages.value = [
      'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600',
      'https://images.unsplash.com/photo-1570913149827-d2ac84ab3f9a?w=600',
      'https://images.unsplash.com/photo-1568702846914-96b305d2ebb2?w=600',
      'https://images.unsplash.com/photo-1584306670957-acf935f5033c?w=600',
      'https://images.unsplash.com/photo-1619546813926-a78fa6372cd2?w=600'
    ]
  } finally {
    loading.value = false
  }
}

// 选择图片
const selectImage = (index) => {
  currentImageIndex.value = index
}

// 上一张图片
const prevImage = () => {
  const newIndex = currentImageIndex.value === 0 
    ? productImages.value.length - 1 
    : currentImageIndex.value - 1
  selectImage(newIndex)
}

// 下一张图片
const nextImage = () => {
  const newIndex = currentImageIndex.value === productImages.value.length - 1 
    ? 0 
    : currentImageIndex.value + 1
  selectImage(newIndex)
}

// 减少数量
const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

// 增加数量
const increaseQuantity = () => {
  if (quantity.value < (product.value.stock || 999)) {
    quantity.value++
  }
}

// 切换参数显示
const toggleParams = () => {
  showParams.value = !showParams.value
}

// 加入购物车
const addToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await cartApi.addToCart({
      productId: product.value.id,
      quantity: quantity.value,
      spec: specifications.value[selectedSpec.value]?.name
    })
    ElMessage.success('已加入购物车')
    cartStore.fetchCartList()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

// 立即购买
const buyNow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await cartApi.addToCart({
      productId: product.value.id,
      quantity: quantity.value,
      spec: specifications.value[selectedSpec.value]?.name
    })
    router.push('/order/confirm')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 查看完整故事
const viewFullStory = () => {
  ElMessage.info('查看完整助农故事')
}

// 进入店铺
const goToShop = () => {
  if (product.value.farmerId) {
    router.push(`/shop/${product.value.farmerId}`)
  } else {
    ElMessage.info('暂无店铺信息')
  }
}

// 联系卖家
const contactSeller = () => {
  ElMessage.info('请联系卖家: ' + (sellerInfo.value.contactPhone || '暂无联系方式'))
}

// 举报成功回调
const handleReportSuccess = () => {
  ElMessage.success('举报已提交，感谢您的反馈')
}

// 页面跳转
const goToHome = () => {
  router.push('/')
}

const goToCategory = () => {
  router.push('/category')
}

// 页面加载时获取商品详情
onMounted(() => {
  fetchProductDetail()
})
</script>

<style scoped>
/* 基础样式 */
.product-detail-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 面包屑导航 */
.breadcrumb {
  padding: 16px 0;
  background: #f5f5f5;
}

.breadcrumb-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.breadcrumb-link {
  color: #666666;
  text-decoration: none;
  transition: all 0.3s ease;
}

.breadcrumb-link:hover {
  color: #4CAF50;
}

.breadcrumb-separator {
  font-size: 12px;
  color: #999999;
}

.breadcrumb-current {
  color: #333333;
}

/* 商品详情主体 */
.product-main {
  padding: 0 0 40px;
}

.product-content {
  display: flex;
  gap: 40px;
  background: #ffffff;
  border-radius: 8px;
  padding: 24px;
}

/* 左侧图片区 */
.product-gallery {
  width: 45%;
  flex-shrink: 0;
}

.main-image-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666666;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.nav-btn:hover {
  background: #ffffff;
  color: #4CAF50;
}

.prev-btn {
  left: 12px;
}

.next-btn {
  right: 12px;
}

.thumbnail-list {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.thumbnail-item {
  width: 80px;
  height: 80px;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.thumbnail-item:hover {
  border-color: #4CAF50;
}

.thumbnail-item.active {
  border-color: #4CAF50;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 助农故事 */
.farmer-story {
  background: #E8F5E9;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.story-title {
  font-size: 16px;
  font-weight: 700;
  color: #2E7D32;
  margin-bottom: 12px;
}

.story-content {
  display: flex;
  gap: 12px;
}

.story-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.story-text {
  flex: 1;
}

.story-text p {
  font-size: 13px;
  color: #333333;
  line-height: 1.6;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.story-link {
  display: inline-block;
  font-size: 13px;
  color: #4CAF50;
  text-decoration: none;
  font-weight: 500;
}

.story-link:hover {
  text-decoration: underline;
}

/* 右侧信息区 */
.product-info {
  flex: 1;
  padding-left: 20px;
}

.product-header {
  margin-bottom: 16px;
}

.product-title {
  font-size: 20px;
  font-weight: 700;
  color: #333333;
  margin-bottom: 8px;
  line-height: 1.4;
}

.product-subtitle {
  font-size: 14px;
  color: #666666;
}

.product-price-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.product-price {
  font-size: 32px;
  font-weight: 700;
  color: #4CAF50;
}

.promo-tag {
  padding: 4px 12px;
  background: #FF9800;
  color: #ffffff;
  font-size: 12px;
  border-radius: 4px;
  font-weight: 500;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}

.rating-stars {
  display: flex;
  gap: 2px;
  color: #ffc107;
  font-size: 14px;
}

.rating-text {
  font-size: 14px;
  color: #666666;
}

/* 规格选择 */
.spec-section {
  margin-bottom: 20px;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 12px;
}

.section-label i {
  color: #4CAF50;
}

.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.spec-btn {
  padding: 10px 20px;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 14px;
  color: #666666;
  cursor: pointer;
  transition: all 0.3s ease;
}

.spec-btn:hover {
  border-color: #4CAF50;
  color: #4CAF50;
}

.spec-btn.active {
  background: #E8F5E9;
  border-color: #4CAF50;
  color: #4CAF50;
}

/* 数量调整 */
.quantity-section {
  margin-bottom: 20px;
}

.quantity-control {
  display: flex;
  align-items: center;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  width: fit-content;
}

.quantity-btn {
  width: 40px;
  height: 40px;
  background: #ffffff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666666;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 12px;
}

.quantity-btn:hover {
  background: #f5f5f5;
  color: #4CAF50;
}

.quantity-btn.minus {
  border-right: 1px solid #e0e0e0;
}

.quantity-btn.plus {
  border-left: 1px solid #e0e0e0;
}

.quantity-input {
  width: 60px;
  height: 40px;
  border: none;
  text-align: center;
  font-size: 14px;
  color: #333333;
  outline: none;
}

/* 库存状态 */
.stock-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666666;
  margin-bottom: 24px;
}

.stock-status i {
  color: #4CAF50;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.btn-add-cart,
.btn-buy-now {
  flex: 1;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-add-cart {
  background: #ffffff;
  border: 1px solid #4CAF50;
  color: #4CAF50;
}

.btn-add-cart:hover {
  background: #E8F5E9;
}

.btn-buy-now {
  background: #4CAF50;
  border: 1px solid #4CAF50;
  color: #ffffff;
}

.btn-buy-now:hover {
  background: #43a047;
}

/* 商品参数 */
.product-params {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.params-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.params-header:hover {
  background: #f5f5f5;
}

.params-header i {
  color: #999999;
  transition: transform 0.3s ease;
}

.params-header i.rotate {
  transform: rotate(180deg);
}

.params-table {
  border-top: 1px solid #e0e0e0;
}

.params-table table {
  width: 100%;
  border-collapse: collapse;
}

.params-table tr {
  border-bottom: 1px solid #e0e0e0;
}

.params-table tr:last-child {
  border-bottom: none;
}

.param-label {
  width: 30%;
  padding: 12px 16px;
  background: #fafafa;
  font-size: 14px;
  color: #666666;
  text-align: left;
}

.param-value {
  padding: 12px 16px;
  font-size: 14px;
  color: #333333;
}

/* 响应式适配 */
@media (max-width: 1240px) {
  .container {
    width: 100%;
    padding: 0 16px;
  }
}

@media (max-width: 1024px) {
  .product-content {
    flex-direction: column;
  }
  
  .product-gallery {
    width: 100%;
  }
  
  .main-image-wrapper {
    max-width: 500px;
    margin: 0 auto 16px;
  }
  
  .thumbnail-list {
    justify-content: center;
  }
  
  .product-info {
    padding-left: 0;
  }
}

@media (max-width: 768px) {
  .action-buttons {
    flex-direction: column;
  }
  
  .product-title {
    font-size: 18px;
  }
  
  .story-content {
    flex-direction: column;
  }
  
  .story-image {
    width: 100%;
    height: 160px;
  }
  
  .thumbnail-item {
    width: 60px;
    height: 60px;
  }
}

@media (max-width: 480px) {
  .product-content {
    padding: 16px;
  }
  
  .product-price {
    font-size: 24px;
  }
  
  .spec-btn {
    padding: 8px 12px;
    font-size: 12px;
  }
  
  .thumbnail-item {
    width: 50px;
    height: 50px;
  }
}

/* 发布者信息 */
.seller-info {
  margin-top: 20px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.seller-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.seller-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.seller-detail {
  flex: 1;
}

.seller-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.shop-name {
  font-size: 13px;
  color: #666;
  margin-bottom: 2px;
}

.shop-name i {
  margin-right: 4px;
}

.seller-location {
  font-size: 12px;
  color: #999;
}

.seller-location i {
  margin-right: 4px;
}

.seller-actions {
  display: flex;
  gap: 8px;
}

.report-section {
  text-align: right;
  margin-top: 12px;
}
</style>
