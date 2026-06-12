<template>
  <div class="statistics-page">
    <el-card class="stats-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h2>数据统计</h2>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </div>
      </template>

      <!-- 数据概览 -->
      <el-row :gutter="24" class="overview-row">
        <el-col :span="6">
          <div class="overview-card sales">
            <div class="overview-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="overview-content">
              <div class="overview-value">¥{{ overview.totalSales || '0.00' }}</div>
              <div class="overview-label">总销售额</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card orders">
            <div class="overview-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="overview-content">
              <div class="overview-value">{{ overview.totalOrders || 0 }}</div>
              <div class="overview-label">订单数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card products">
            <div class="overview-icon">
              <el-icon><Goods /></el-icon>
            </div>
            <div class="overview-content">
              <div class="overview-value">{{ overview.totalProducts || 0 }}</div>
              <div class="overview-label">商品数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card views">
            <div class="overview-icon">
              <el-icon><View /></el-icon>
            </div>
            <div class="overview-content">
              <div class="overview-value">{{ overview.totalViews || 0 }}</div>
              <div class="overview-label">访问量</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 销售趋势 -->
      <el-card class="chart-card" shadow="never">
        <template #header><span>销售趋势</span></template>
        <div class="chart-container" ref="salesChartRef">
          <!-- 这里可以集成ECharts,暂时使用简单表格展示 -->
          <el-table :data="salesTrend" stripe style="width: 100%">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="sales" label="销售额" width="120">
              <template #default="{ row }">¥{{ row.sales }}</template>
            </el-table-column>
            <el-table-column prop="orders" label="订单数" width="100" />
            <el-table-column prop="products" label="商品数" width="100" />
            <el-table-column label="趋势">
              <template #default="{ row }">
                <el-tag :type="row.growth >= 0 ? 'success' : 'danger'" size="small">
                  {{ row.growth >= 0 ? '+' : '' }}{{ row.growth }}%
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <!-- 商品统计 -->
      <el-row :gutter="24" class="stats-row">
        <el-col :span="12">
          <el-card class="chart-card" shadow="never">
            <template #header><span>热销商品TOP10</span></template>
            <el-table :data="topProducts" stripe max-height="400">
              <el-table-column type="index" label="#" width="50" align="center" />
              <el-table-column prop="name" label="商品名称" show-overflow-tooltip />
              <el-table-column prop="salesCount" label="销量" width="80" align="center" />
              <el-table-column prop="salesAmount" label="销售额" width="100" align="center">
                <template #default="{ row }">¥{{ row.salesAmount }}</template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card" shadow="never">
            <template #header><span>订单状态分布</span></template>
            <div class="order-stats">
              <div
                v-for="item in orderStats"
                :key="item.status"
                class="stat-bar-item"
              >
                <div class="stat-bar-label">{{ item.label }}</div>
                <el-progress
                  :percentage="item.percentage"
                  :color="item.color"
                  :stroke-width="20"
                  :text-inside="true"
                />
                <div class="stat-bar-count">{{ item.count }} 单</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Money, Document, Goods, View } from '@element-plus/icons-vue'
import { statisticsApi } from '@/api'

const dateRange = ref([])
const salesChartRef = ref(null)
let refreshTimer = null

const overview = reactive({
  totalSales: 0,
  totalOrders: 0,
  totalProducts: 0,
  totalViews: 0
})

const salesTrend = ref([])
const topProducts = ref([])

const orderStats = ref([
  { status: 0, label: '待付款', count: 0, percentage: 0, color: '#909399' },
  { status: 1, label: '待发货', count: 0, percentage: 0, color: '#E6A23C' },
  { status: 2, label: '已发货', count: 0, percentage: 0, color: '#409EFF' },
  { status: 3, label: '已完成', count: 0, percentage: 0, color: '#67C23A' },
  { status: 4, label: '已取消', count: 0, percentage: 0, color: '#F56C6C' }
])

const fetchOverview = async () => {
  try {
    const res = await statisticsApi.getOverview()
    Object.assign(overview, res)
  } catch (error) {
    console.error('获取数据概览失败:', error)
  }
}

const fetchSalesTrend = async () => {
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await statisticsApi.getSalesTrend(params)
    salesTrend.value = res.list || []
  } catch (error) {
    console.error('获取销售趋势失败:', error)
  }
}

const fetchProductStats = async () => {
  try {
    const res = await statisticsApi.getProductStats()
    topProducts.value = res.topProducts || []
    if (res.orderStats) {
      const total = Object.values(res.orderStats).reduce((sum, count) => sum + count, 0)
      orderStats.value = orderStats.value.map(item => ({
        ...item,
        count: res.orderStats[item.status] || 0,
        percentage: total > 0 ? Math.round((res.orderStats[item.status] || 0) / total * 100) : 0
      }))
    }
  } catch (error) {
    console.error('获取商品统计失败:', error)
  }
}

const refreshAll = () => {
  fetchOverview()
  fetchSalesTrend()
  fetchProductStats()
}

const handleDateChange = () => {
  fetchSalesTrend()
}

onMounted(() => {
  refreshAll()
  refreshTimer = setInterval(refreshAll, 60000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped>
.statistics-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.overview-row {
  margin-bottom: 24px;
}

.overview-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  color: white;
  margin-bottom: 16px;
}

.overview-card.sales {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.overview-card.orders {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
}

.overview-card.products {
  background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
}

.overview-card.views {
  background: linear-gradient(135deg, #eb2f96 0%, #c41d7f 100%);
}

.overview-icon {
  font-size: 48px;
  margin-right: 20px;
  opacity: 0.9;
}

.overview-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 4px;
}

.overview-label {
  font-size: 14px;
  opacity: 0.9;
}

.chart-card {
  margin-bottom: 24px;
}

.chart-card .el-card__header span {
  font-weight: 600;
  color: #333;
}

.chart-container {
  min-height: 300px;
}

.stats-row {
  margin-top: 24px;
}

.order-stats {
  padding: 10px 0;
}

.stat-bar-item {
  margin-bottom: 20px;
}

.stat-bar-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-bar-count {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  text-align: right;
}
</style>
