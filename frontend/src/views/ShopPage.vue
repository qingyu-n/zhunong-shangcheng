<template>
  <div class="shop-page" v-loading="loading">
    <el-card v-if="shopInfo" class="shop-info-card" shadow="never">
      <div class="shop-banner">
        <img v-if="shopInfo.bannerImage" :src="shopInfo.bannerImage" class="banner-img" />
        <div v-else class="banner-default"></div>
      </div>

      <div class="shop-header">
        <el-avatar :size="80" :src="shopInfo.logo">
          {{ shopInfo.name?.charAt(0) }}
        </el-avatar>
        <div class="header-info">
          <h1>{{ shopInfo.name }}</h1>
          <p class="description">{{ shopInfo.description }}</p>
          <div class="stats-row">
            <span><el-icon><View /></el-icon> 访问: {{ shopInfo.viewCount }}</span>
            <span><el-icon><Star /></el-icon> 收藏: {{ shopInfo.favoriteCount }}</span>
            <span><el-icon><Goods /></el-icon> 商品: {{ shopInfo.productCount }}</span>
          </div>
        </div>
        <div class="header-actions">
          <el-button 
            :type="isFavorited ? 'warning' : 'primary'" 
            @click="handleFavorite"
            :disabled="!isLoggedIn"
          >
            <el-icon><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
            {{ isFavorited ? '已收藏' : '收藏店铺' }}
          </el-button>
          <el-button type="success" @click="showContactDialog = true">
            <el-icon><Phone /></el-icon> 联系卖家
          </el-button>
        </div>
      </div>

      <el-divider />

      <div class="shop-products-section">
        <h3>全部商品</h3>
        <el-row :gutter="16" v-loading="productsLoading">
          <el-col :xs="12" :sm="8" :md="6" v-for="product in products" :key="product.id">
            <el-card shadow="hover" class="product-card" @click="$router.push(`/product/${product.id}`)">
              <el-image :src="product.mainImage" fit="cover" class="product-image" />
              <div class="product-name">{{ product.name }}</div>
              <div class="product-price">¥{{ product.price }}<span v-if="product.unit">/{{ product.unit }}</span></div>
              <div class="product-sales">已售{{ product.sales || 0 }}</div>
            </el-card>
          </el-col>
        </el-row>

        <el-empty v-if="!productsLoading && products.length === 0" description="暂无商品" />
        
        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
            v-model:current-page="page"
            :page-size="12"
            :total="total"
            layout="prev, pager, next"
            @current-change="fetchProducts"
          />
        </div>
      </div>
    </el-card>

    <el-dialog v-model="showContactDialog" title="联系卖家" width="400px">
      <div class="contact-info">
        <p><strong>联系电话：</strong>{{ maskPhone(shopInfo?.contactPhone) }}</p>
        <p v-if="shopInfo?.contactWechat"><strong>微信号：</strong>{{ shopInfo.contactWechat }}</p>
        <p v-if="shopInfo?.address"><strong>地址：</strong>{{ shopInfo.address }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Star, StarFilled, Goods, Phone } from '@element-plus/icons-vue'
import { shopApi } from '@/api'

const route = useRoute()
const loading = ref(false)
const productsLoading = ref(false)
const shopInfo = ref(null)
const products = ref([])
const total = ref(0)
const page = ref(1)
const isFavorited = ref(false)
const isLoggedIn = ref(!!localStorage.getItem('token'))
const showContactDialog = ref(false)

const fetchShopDetail = async () => {
  loading.value = true
  try {
    const res = await shopApi.getShopDetail(route.params.id)
    shopInfo.value = res.data
    if (res.data.isFavorited !== undefined) {
      isFavorited.value = res.data.isFavorited
    }
  } catch (error) {
    ElMessage.error(error.message || '获取店铺信息失败')
  } finally {
    loading.value = false
  }
}

const fetchProducts = async () => {
  productsLoading.value = true
  try {
    const res = await shopApi.getShopProducts(route.params.id, { page: page.value, size: 12 })
    products.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    // ignore
  } finally {
    productsLoading.value = false
  }
}

const handleFavorite = async () => {
  if (!isLoggedIn.value) return
  
  try {
    if (isFavorited.value) {
      await shopApi.unfavoriteShop(route.params.id)
      ElMessage.success('取消收藏成功')
      isFavorited.value = false
      if (shopInfo.value.favoriteCount > 0) {
        shopInfo.value.favoriteCount--
      }
    } else {
      await shopApi.favoriteShop(route.params.id)
      ElMessage.success('收藏成功')
      isFavorited.value = true
      shopInfo.value.favoriteCount++
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const maskPhone = (phone) => {
  if (!phone) return '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

onMounted(() => {
  fetchShopDetail()
  fetchProducts()
})
</script>

<style scoped lang="scss">
.shop-page {
  max-width: 1200px;
  margin: 20px auto;

  .shop-info-card {
    border-radius: 8px;
  }

  .shop-banner {
    height: 200px;
    overflow: hidden;

    .banner-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .banner-default {
      width: 100%;
      height: 100%;
      background: linear-gradient(135deg, #52c41a, #95de64);
    }
  }

  .shop-header {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 20px 0;

    .header-info {
      flex: 1;

      h1 {
        margin: 0 0 8px;
        font-size: 24px;
        color: #333;
      }

      .description {
        margin: 0 0 12px;
        color: #666;
        font-size: 14px;
      }

      .stats-row {
        display: flex;
        gap: 20px;
        color: #999;
        font-size: 14px;

        span {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }

    .header-actions {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
  }

  .shop-products-section {
    h3 {
      margin-bottom: 16px;
      color: #333;
    }

    .product-card {
      cursor: pointer;
      margin-bottom: 16px;
      border-radius: 8px;
      transition: transform 0.2s;

      &:hover {
        transform: translateY(-4px);
      }

      .product-image {
        width: 100%;
        height: 160px;
        border-radius: 4px;
      }

      .product-name {
        margin-top: 10px;
        font-weight: 500;
        color: #333;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .product-price {
        margin-top: 6px;
        color: #ff4d4f;
        font-weight: bold;
        font-size: 16px;

        span {
          font-size: 12px;
          color: #999;
          font-weight: normal;
        }
      }

      .product-sales {
        margin-top: 4px;
        font-size: 12px;
        color: #999;
      }
    }
  }

  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }

  .contact-info {
    p {
      margin: 12px 0;
      line-height: 1.6;
    }
  }
}
</style>
