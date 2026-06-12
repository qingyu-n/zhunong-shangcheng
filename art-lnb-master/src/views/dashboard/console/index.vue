<template>
  <div class="console-page">
    <div class="page-header">
      <div class="header-info">
        <div class="header-icon">
          <i class="ri-dashboard-3-line"></i>
        </div>
        <div class="header-text">
          <h1 class="page-title">工作台</h1>
          <p class="page-desc">助农商城数据概览与运营监控</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button :loading="refreshing" @click="handleRefresh">
          <i class="ri-refresh-line"></i> 刷新
        </el-button>
        <el-button type="primary" @click="handleExport">
          <i class="ri-download-2-line"></i> 导出报表
        </el-button>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card" style="--c: #6366f1; --cb: rgba(99,102,241,0.08)">
        <div class="sc-icon"><i class="ri-user-line"></i></div>
        <div class="sc-body">
          <span class="sc-val">{{ statistics.totalUsers }}</span>
          <span class="sc-label">总用户数</span>
        </div>
        <div class="sc-badge indigo">
          <i class="ri-arrow-up-s-fill"></i> 活跃
        </div>
      </div>

      <div class="stat-card" style="--c: #10b981; --cb: rgba(16,185,129,0.08)">
        <div class="sc-icon"><i class="ri-shopping-bag-line"></i></div>
        <div class="sc-body">
          <span class="sc-val">{{ statistics.totalProducts }}</span>
          <span class="sc-label">商品总数</span>
        </div>
        <div class="sc-badge green">
          <i class="ri-arrow-up-s-fill"></i> 在售
        </div>
      </div>

      <div class="stat-card" style="--c: #f59e0b; --cb: rgba(245,158,11,0.08)">
        <div class="sc-icon"><i class="ri-file-list-3-line"></i></div>
        <div class="sc-body">
          <span class="sc-val">{{ statistics.totalOrders }}</span>
          <span class="sc-label">订单总数</span>
        </div>
        <div class="sc-badge amber">
          <i class="ri-time-line"></i> 累计
        </div>
      </div>

      <div class="stat-card" style="--c: #ef4444; --cb: rgba(239,68,68,0.08)">
        <div class="sc-icon"><i class="ri-money-cny-circle-line"></i></div>
        <div class="sc-body">
          <span class="sc-val">¥{{ formatMoney(statistics.totalSales) }}</span>
          <span class="sc-label">总销售额</span>
        </div>
        <div class="sc-badge red">
          <i class="ri-arrow-up-s-fill"></i> 增长
        </div>
      </div>
    </div>

    <div class="today-section">
      <div class="section-head">
        <div class="section-title-group">
          <i class="ri-calendar-line section-ico"></i>
          <span class="section-title">今日实时数据</span>
        </div>
      </div>
      <div class="today-grid">
        <div class="today-card" style="--tc: #6366f1; --tb: rgba(99,102,241,0.06)">
          <div class="today-icon"><i class="ri-shopping-cart-2-line"></i></div>
          <div class="today-info">
            <span class="today-value">{{ statistics.todayOrders }}</span>
            <span class="today-label">今日订单</span>
          </div>
        </div>
        <div class="today-card" style="--tc: #10b981; --tb: rgba(16,185,129,0.06)">
          <div class="today-icon"><i class="ri-cash-line"></i></div>
          <div class="today-info">
            <span class="today-value">¥{{ formatMoney(statistics.todaySales) }}</span>
            <span class="today-label">今日销售额</span>
          </div>
        </div>
        <div class="today-card" style="--tc: #f59e0b; --tb: rgba(245,158,11,0.06)">
          <div class="today-icon"><i class="ri-time-line"></i></div>
          <div class="today-info">
            <span class="today-value">{{ statistics.pendingOrders }}</span>
            <span class="today-label">待付款</span>
          </div>
        </div>
        <div class="today-card" style="--tc: #8b5cf6; --tb: rgba(139,92,246,0.06)">
          <div class="today-icon"><i class="ri-truck-line"></i></div>
          <div class="today-info">
            <span class="today-value">{{ statistics.pendingShipments }}</span>
            <span class="today-label">待发货</span>
          </div>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-panel trend-panel">
        <div class="panel-head">
          <div class="panel-title-group">
            <i class="ri-line-chart-line panel-ico"></i>
            <span class="panel-title">销售趋势</span>
          </div>
          <ElRadioGroup v-model="trendDays" size="small" @change="getSalesTrend">
            <ElRadioButton :value="7">近7天</ElRadioButton>
            <ElRadioButton :value="30">近30天</ElRadioButton>
          </ElRadioGroup>
        </div>
        <div ref="trendChartRef" class="chart-box"></div>
      </div>

      <div class="chart-panel hot-panel">
        <div class="panel-head">
          <div class="panel-title-group">
            <i class="ri-fire-fill panel-ico hot-ico"></i>
            <span class="panel-title">热销商品 TOP10</span>
          </div>
        </div>
        <div class="hot-list-wrap">
          <div v-for="(item, index) in hotProducts" :key="item.id" class="hot-item">
            <span class="hot-rank" :class="{ top: index < 3 }">
              <template v-if="index === 0">🥇</template>
              <template v-else-if="index === 1">🥈</template>
              <template v-else-if="index === 2">🥉</template>
              <template v-else>{{ index + 1 }}</template>
            </span>
            <span class="hot-name" :title="item.name">{{ item.name }}</span>
            <span class="hot-sales">{{ item.sales }}件</span>
          </div>
          <div v-if="hotProducts.length === 0" class="empty-hint">
            <i class="ri-inbox-2-line"></i>
            暂无数据
          </div>
        </div>
      </div>
    </div>

    <div class="recent-section">
      <div class="panel-head">
        <div class="panel-title-group">
          <i class="ri-file-list-3-line panel-ico"></i>
          <span class="panel-title">最近订单</span>
        </div>
        <ElButton type="primary" link @click="$router.push('/system/order')">查看全部 →</ElButton>
      </div>
      <ElTable :data="recentOrders" size="small" :header-cell-style="{ background: '#fafafa', color: '#666' }">
        <ElTableColumn prop="orderNo" label="订单编号" min-width="160" show-overflow-tooltip />
        <ElTableColumn prop="username" label="下单用户" width="120" />
        <ElTableColumn prop="amount" label="订单金额" width="110" align="center">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.amount }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="statusName" label="状态" width="100" align="center">
          <template #default="{ row }">
            <ElTag :type="getStatusType(row.status)" size="small" effect="light" round>
              {{ row.statusName }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createTime" label="下单时间" min-width="150" />
      </ElTable>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import {
  fetchGetStatistics,
  fetchGetSalesTrend,
  fetchGetHotProducts,
  fetchGetRecentOrders
} from '@/api/mall'

defineOptions({ name: 'Console' })

const refreshing = ref(false)
const statistics = reactive<Api.Dashboard.Statistics>({
  totalUsers: 0,
  totalProducts: 0,
  totalOrders: 0,
  totalSales: 0,
  todayOrders: 0,
  todaySales: 0,
  pendingOrders: 0,
  pendingShipments: 0
})

const trendDays = ref(7)
const trendChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
const salesTrend = reactive<Api.Dashboard.SalesTrend>({
  dates: [],
  amounts: [],
  orders: []
})

const hotProducts = ref<Api.Dashboard.HotProduct[]>([])
const recentOrders = ref<Api.Dashboard.RecentOrder[]>([])

const formatMoney = (value: number) => {
  if (!value) return '0'
  if (value >= 10000) return (value / 10000).toFixed(2) + '万'
  return value.toFixed(2)
}

const getStatusType = (status: number): 'warning' | 'success' | 'primary' | 'danger' | 'info' => {
  const map: Record<number, 'warning' | 'success' | 'primary' | 'danger' | 'info'> = {
    0: 'warning', 1: 'success', 2: 'primary', 3: 'success', 4: 'info'
  }
  return map[status] || 'info'
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#333', fontSize: 13 }
    },
    legend: {
      data: ['销售额', '订单数'],
      bottom: 0,
      textStyle: { color: '#888', fontSize: 12 },
      itemWidth: 16,
      itemHeight: 8,
      itemGap: 28
    },
    grid: { left: '3%', right: '4%', bottom: '14%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: salesTrend.dates,
      axisLine: { lineStyle: { color: '#e8e8e8' } },
      axisTick: { show: false },
      axisLabel: { color: '#888', fontSize: 11 }
    },
    yAxis: [
      {
        type: 'value',
        name: '销售额',
        position: 'left',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { color: '#f5f5f5' } },
        axisLabel: {
          formatter: (v: number) => '¥' + (v >= 1000 ? v / 1000 + 'k' : v),
          fontSize: 11,
          color: '#888'
        }
      },
      {
        type: 'value',
        name: '订单数',
        position: 'right',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { fontSize: 11, color: '#888' }
      }
    ],
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: salesTrend.amounts,
        itemStyle: { color: '#6366f1' },
        lineStyle: { width: 2.5 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(99,102,241,0.22)' },
            { offset: 1, color: 'rgba(99,102,241,0.02)' }
          ])
        },
        symbol: 'circle',
        symbolSize: 6
      },
      {
        name: '订单数',
        type: 'bar',
        yAxisIndex: 1,
        data: salesTrend.orders,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#10b981' },
            { offset: 1, color: '#34d399' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: '38%'
      }
    ]
  }
  trendChart.setOption(option)
}

const getStatistics = async () => {
  try {
    const res = await fetchGetStatistics()
    Object.assign(statistics, res)
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const getSalesTrend = async () => {
  try {
    const res = await fetchGetSalesTrend(trendDays.value)
    Object.assign(salesTrend, res)
    if (trendChart) {
      trendChart.setOption({
        xAxis: { data: salesTrend.dates },
        series: [{ data: salesTrend.amounts }, { data: salesTrend.orders }]
      })
    } else {
      initTrendChart()
    }
  } catch (error) {
    console.error('获取销售趋势失败:', error)
  }
}

const getHotProducts = async () => {
  try {
    const res = await fetchGetHotProducts(10)
    hotProducts.value = res
  } catch (error) {
    console.error('获取热销商品失败:', error)
  }
}

const getRecentOrders = async () => {
  try {
    const res = await fetchGetRecentOrders(10)
    recentOrders.value = res
  } catch (error) {
    console.error('获取最近订单失败:', error)
  }
}

const handleRefresh = async () => {
  refreshing.value = true
  await Promise.all([getStatistics(), getSalesTrend(), getHotProducts(), getRecentOrders()])
  refreshing.value = false
  ElMessage.success('数据已刷新')
}

const handleExport = () => {
  const rows = [
    ['指标', '数值'],
    ['总用户数', String(statistics.totalUsers)],
    ['商品总数', String(statistics.totalProducts)],
    ['订单总数', String(statistics.totalOrders)],
    ['总销售额', '¥' + statistics.totalSales],
    ['今日订单', String(statistics.todayOrders)],
    ['今日销售额', '¥' + statistics.todaySales],
    ['待付款订单', String(statistics.pendingOrders)],
    ['待发货订单', String(statistics.pendingShipments)]
  ]
  const csv = rows.map((r) => r.join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `运营报表_${new Date().toLocaleDateString()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const handleResize = () => {
  trendChart?.resize()
}

onMounted(() => {
  getStatistics()
  getSalesTrend()
  getHotProducts()
  getRecentOrders()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
})
</script>

<style scoped>
.console-page {
  padding: 24px;
  background: #f5f6fa;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.3);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
  letter-spacing: -0.5px;
}

.page-desc {
  font-size: 13px;
  color: #8c8c8c;
  margin: 3px 0 0;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-bottom: 22px;
}

.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease-out;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: -20px;
    right: -20px;
    width: 90px;
    height: 90px;
    background: var(--cb);
    border-radius: 50%;
    opacity: 0.5;
  }

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
}

.sc-icon {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  background: var(--cb);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--c);
  flex-shrink: 0;
  transition: transform 0.25s;

  .stat-card:hover & {
    transform: scale(1.08);
  }
}

.sc-body {
  display: flex;
  flex-direction: column;
  gap: 3px;
  flex: 1;
  min-width: 0;
  position: relative;
  z-index: 1;
}

.sc-val {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  letter-spacing: -0.5px;
  line-height: 1.2;
}

.sc-label {
  font-size: 13px;
  color: #8c8c8c;
  font-weight: 500;
}

.sc-badge {
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 20px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;

  &.indigo {
    background: rgba(99, 102, 241, 0.08);
    color: #6366f1;
  }

  &.green {
    background: rgba(16, 185, 129, 0.08);
    color: #10b981;
  }

  &.amber {
    background: rgba(245, 158, 11, 0.08);
    color: #f59e0b;
  }

  &.red {
    background: rgba(239, 68, 68, 0.08);
    color: #ef4444;
  }
}

.today-section {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  margin-bottom: 22px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.section-head {
  margin-bottom: 16px;
}

.section-title-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-ico {
  font-size: 17px;
  color: #6366f1;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}

.today-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.today-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: var(--tb);
  border-radius: 12px;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.today-card:hover {
  background: #f3f4f6;
  border-color: rgba(0, 0, 0, 0.04);
}

.today-icon {
  font-size: 28px;
  color: var(--tc);
  flex-shrink: 0;
}

.today-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.today-value {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  letter-spacing: -0.3px;
}

.today-label {
  font-size: 12px;
  color: #8c8c8c;
  font-weight: 500;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 18px;
  margin-bottom: 22px;
}

.chart-panel {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 22px;
  border-bottom: 1px solid #f5f5f5;
}

.panel-title-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-ico {
  font-size: 18px;
  color: #6366f1;

  &.hot-ico {
    color: #f43f5e;
  }
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}

.chart-box {
  height: 340px;
  padding: 14px;
}

.hot-list-wrap {
  padding: 8px 22px 18px;
  max-height: 340px;
  overflow-y: auto;
}

.hot-item {
  display: flex;
  align-items: center;
  padding: 11px 0;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.15s;
}

.hot-item:last-child {
  border-bottom: none;
}

.hot-item:hover {
  background: #fafafa;
  margin: 0 -22px;
  padding-left: 22px;
  padding-right: 22px;
}

.hot-rank {
  width: 30px;
  text-align: center;
  font-size: 14px;
  font-weight: 700;
  color: #8c8c8c;
  margin-right: 14px;
  flex-shrink: 0;
}

.hot-rank.top {
  font-size: 16px;
}

.hot-name {
  flex: 1;
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-sales {
  font-size: 13px;
  color: #10b981;
  font-weight: 600;
  margin-left: 12px;
  flex-shrink: 0;
}

.empty-hint {
  text-align: center;
  color: #c0c4cc;
  padding: 40px 0;
  font-size: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.empty-hint i {
  font-size: 40px;
  opacity: 0.35;
}

.recent-section {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.price-text {
  color: #ef4444;
  font-weight: 600;
}

@media (max-width: 1200px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-row, .today-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }

  .header-actions {
    flex-wrap: wrap;
  }

  .sc-val {
    font-size: 22px;
  }
}
</style>
