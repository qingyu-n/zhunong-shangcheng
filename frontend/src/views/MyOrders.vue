<template>
  <div class="my-orders-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-left">
        <a href="#" class="back-btn" @click.prevent="goBack">
          <i class="fas fa-arrow-left"></i>
        </a>
      </div>
      <h1 class="header-title">我的订单页</h1>
      <div class="header-right">
        <button class="search-btn" @click="showSearch">
          <i class="fas fa-search"></i>
        </button>
      </div>
    </header>

    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <div class="filter-container">
        <div class="filter-tags">
          <div
            v-for="tag in filterTags"
            :key="tag.value"
            class="filter-tag"
            :class="{ active: currentFilter === tag.value }"
            @click="selectFilter(tag.value)"
          >
            {{ tag.label }}
          </div>
        </div>
        <div class="search-box">
          <i class="fas fa-search search-icon"></i>
          <input
            type="text"
            placeholder="搜索订单号或商品名称"
            v-model="searchKeyword"
            @keyup.enter="handleSearch"
          />
        </div>
      </div>
    </div>

    <!-- 订单列表区域 -->
    <main class="main-content" v-loading="loading">
      <div class="content-container">
        <!-- 订单列表表头 -->
        <div class="order-header">
          <div class="col-info">订单信息</div>
          <div class="col-product">商品</div>
          <div class="col-amount">金额</div>
          <div class="col-status">状态</div>
          <div class="col-action">操作</div>
        </div>

        <!-- 订单卡片列表 -->
        <div class="order-list">
          <div
            v-for="order in orderList"
            :key="order.id"
            class="order-card"
          >
            <div class="order-card-content">
              <div class="col-info">
                <div class="order-no">订单号: {{ order.orderNo }}</div>
                <div class="order-time">下单时间: {{ order.createTime || order.orderTime }}</div>
              </div>
              <div class="col-product">
                <div class="product-name">{{ order.productName || '订单商品' }}</div>
                <div class="product-quantity">x{{ order.quantity || 1 }}</div>
              </div>
              <div class="col-amount">
                <div class="order-amount">¥{{ order.amount || order.totalAmount || '0.00' }}</div>
              </div>
              <div class="col-status">
                <div class="status-tag" :class="getStatusClass(order.status)">
                  <span class="status-dot"></span>
                  {{ getStatusText(order.status) }}
                </div>
              </div>
              <div class="col-action">
                <a href="#" class="detail-link" @click.prevent="viewDetail(order.id)">
                  详情
                  <i class="fas fa-angle-right"></i>
                </a>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && orderList.length === 0" class="empty-state">
          <i class="fas fa-file-alt empty-icon"></i>
          <p>暂无订单记录</p>
        </div>
      </div>
    </main>

    <!-- 底部分页 -->
    <footer class="pagination-footer">
      <div class="pagination">
        <a href="#" class="page-btn prev" @click.prevent="prevPage">上一页</a>
        <a
          v-for="page in visiblePages"
          :key="page"
          href="#"
          class="page-btn"
          :class="{ active: currentPage === page, ellipsis: page === '...' }"
          @click.prevent="page === '...' ? null : goToPage(page)"
        >
          {{ page }}
        </a>
        <a href="#" class="page-btn next" @click.prevent="nextPage">下一页</a>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 筛选标签
const filterTags = [
  { label: '全部订单', value: 'all' },
  { label: '待付款', value: 'pending' },
  { label: '待发货', value: 'unshipped' },
  { label: '待收货', value: 'shipped' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' }
]

// 当前筛选
const currentFilter = ref('all')

// 搜索关键词
const searchKeyword = ref('')

// 当前页码
const currentPage = ref(1)
const pageSize = 10

// 加载状态
const loading = ref(false)

// 订单列表数据 - 从API获取
const orderList = ref([])
const total = ref(0)

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize,
      status: currentFilter.value !== 'all' ? currentFilter.value : undefined,
      keyword: searchKeyword.value || undefined
    }

    const res = await orderApi.getOrderList(params)
    orderList.value = res.records || res.list || []
    total.value = res.total || orderList.value.length
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    'pending': 'warning',
    'unshipped': 'warning',
    'shipped': 'warning',
    'completed': 'success',
    'cancelled': 'danger'
  }
  return classMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'pending': '待付款',
    'unshipped': '待发货',
    'shipped': '待收货',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return textMap[status] || status
}

// 可见页码
const visiblePages = computed(() => {
  return [1, 2, 3, '...']
})

// 选择筛选标签
const selectFilter = (value) => {
  currentFilter.value = value
  currentPage.value = 1
  fetchOrders()
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

// 显示搜索
const showSearch = () => {
  // 搜索框已显示，无需额外操作
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 查看详情
const viewDetail = (id) => {
  router.push('/order/' + id)
}

// 导航跳转
const navigateTo = (page) => {
  const routes = {
    home: '/',
    category: '/category',
    help: '/help',
    user: '/user',
    cart: '/cart'
  }
  const path = routes[page]
  if (path) {
    router.push(path)
  }
}

// 上一页
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

// 下一页
const nextPage = () => {
  currentPage.value++
}

// 跳转到指定页
const goToPage = (page) => {
  currentPage.value = page
  fetchOrders()
}

// 页面加载时获取订单列表
onMounted(() => {
  fetchOrders()
})

// 监听页码变化
watch(currentPage, () => {
  fetchOrders()
})
</script>

<style scoped>
.my-orders-page {
  min-height: 100vh;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.header {
  width: 100%;
  height: 64px;
  border-bottom: 1px solid #e8e8e8;
  background-color: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-left {
  position: absolute;
  left: 16px;
}

.back-btn {
  color: #666666;
  font-size: 18px;
  text-decoration: none;
  transition: color 0.2s;
}

.back-btn:hover {
  color: #4096ff;
}

.header-title {
  font-size: 24px;
  font-weight: 500;
  color: #333333;
  margin: 0;
}

.header-right {
  position: absolute;
  right: 16px;
}

.search-btn {
  background: none;
  border: none;
  color: #666666;
  font-size: 18px;
  cursor: pointer;
  transition: color 0.2s;
}

.search-btn:hover {
  color: #4096ff;
}

/* 筛选工具栏 */
.filter-toolbar {
  width: 100%;
  background-color: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  padding: 16px 0;
}

.filter-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tag {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: #f5f5f5;
  color: #666666;
}

.filter-tag:hover {
  background-color: rgba(64, 150, 255, 0.1);
}

.filter-tag.active {
  background-color: #4096ff;
  color: #ffffff;
}

.search-box {
  position: relative;
  width: 280px;
}

.search-box input {
  width: 100%;
  height: 40px;
  padding: 0 16px 0 40px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  background-color: #ffffff;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
}

.search-box input:focus {
  border-color: #4096ff;
  box-shadow: 0 0 0 3px rgba(64, 150, 255, 0.1);
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #666666;
  font-size: 14px;
}

/* 主内容区 */
.main-content {
  flex: 1;
  overflow: auto;
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

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 订单列表表头 */
.order-header {
  display: grid;
  grid-template-columns: 4fr 3fr 2fr 2fr 1fr;
  gap: 16px;
  padding: 12px 0;
  margin-bottom: 16px;
  font-size: 14px;
  color: #666666;
  font-weight: 500;
  border-bottom: 1px solid #f0f0f0;
}

/* 订单卡片列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background-color: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.3s;
}

.order-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.order-card-content {
  display: grid;
  grid-template-columns: 4fr 3fr 2fr 2fr 1fr;
  gap: 16px;
  padding: 20px 24px;
  align-items: center;
}

.col-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.order-no {
  font-size: 14px;
  color: #333333;
}

.order-time {
  font-size: 14px;
  color: #666666;
}

.col-product {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-name {
  font-size: 14px;
  color: #333333;
}

.product-quantity {
  font-size: 14px;
  color: #666666;
}

.col-amount {
  display: flex;
  align-items: center;
}

.order-amount {
  font-size: 16px;
  font-weight: bold;
  color: #333333;
}

.col-status {
  display: flex;
  align-items: center;
}

.status-tag {
  height: 28px;
  padding: 0 16px;
  border-radius: 14px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  gap: 6px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
}

.status-tag.warning {
  background-color: #faad14;
}

.status-tag.success {
  background-color: #52c41a;
}

.status-tag.danger {
  background-color: #ff4d4f;
}

.col-action {
  display: flex;
  align-items: center;
}

.detail-link {
  color: #4096ff;
  font-size: 14px;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
}

.detail-link:hover {
  color: #1677ff;
}

/* 底部分页 */
.pagination-footer {
  width: 100%;
  background-color: #ffffff;
  border-top: 1px solid #e8e8e8;
  padding: 24px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.page-btn {
  min-width: 40px;
  height: 40px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #666666;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s;
  background-color: #ffffff;
  padding: 0 12px;
}

.page-btn:hover:not(.active):not(.ellipsis) {
  background-color: #f5f5f5;
}

.page-btn.active {
  background-color: #4096ff;
  border-color: #4096ff;
  color: #ffffff;
}

.page-btn.ellipsis {
  border: none;
  cursor: default;
}

.page-btn.prev,
.page-btn.next {
  min-width: 80px;
}
</style>
