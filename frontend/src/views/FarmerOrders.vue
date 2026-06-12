<template>
  <div class="farmer-orders-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <h2>订单发货</h2>
          <el-button @click="fetchOrders"><i class="fas fa-sync-alt"></i> 刷新</el-button>
        </div>
      </template>

      <div class="filter-bar">
        <el-select v-model="filterStatus" placeholder="订单状态" clearable style="width: 140px" @change="handleFilter">
          <el-option label="待发货" :value="1" />
          <el-option label="已发货" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
      </div>

      <el-table :data="orders" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="180" show-overflow-tooltip />
        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div v-if="row.items && row.items.length > 0">
              <div v-for="item in row.items.slice(0, 2)" :key="item.id" class="order-product">
                <span class="product-name">{{ item.productName }}</span>
                <span class="product-qty">x{{ item.quantity }}</span>
              </div>
              <div v-if="row.items.length > 2" class="more-items">...等{{ row.items.length }}件商品</div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" label="订单金额" width="110" align="center">
          <template #default="{ row }">¥{{ formatPrice(row.payAmount) }}</template>
        </el-table-column>
        <el-table-column label="收货人" width="100" align="center">
          <template #default="{ row }">{{ row.receiverName || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="165" align="center" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 1"
              type="success"
              link
              size="small"
              @click="handleShip(row)"
            >发货</el-button>
            <el-button
              v-if="row.status === 2"
              type="warning"
              link
              size="small"
              @click="handleCancelShip(row)"
            >取消发货</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchOrders"
          @current-change="fetchOrders"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="700px" destroy-on-close>
      <el-descriptions v-if="currentOrder" :column="2" border>
        <el-descriptions-item label="订单编号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收货人">{{ currentOrder.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="商品小计">¥{{ formatPrice(currentOrder.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="运费">¥{{ formatPrice(currentOrder.freightAmount) }}</el-descriptions-item>
        <el-descriptions-item label="优惠">-¥{{ formatPrice(currentOrder.discountAmount) }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ formatPrice(currentOrder.payAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.logisticsCompany" label="物流公司">{{ currentOrder.logisticsCompany }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.trackingNumber" label="运单号">{{ currentOrder.trackingNumber }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="currentOrder && currentOrder.items && currentOrder.items.length" style="margin-top: 16px;">
        <h4 style="margin-bottom: 10px;">商品明细</h4>
        <el-table :data="currentOrder.items" stripe size="small">
          <el-table-column prop="productName" label="商品名称" show-overflow-tooltip />
          <el-table-column prop="price" label="单价" width="100" align="center">
            <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column label="小计" width="100" align="center">
            <template #default="{ row }">¥{{ formatPrice(row.totalAmount) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog v-model="shipDialogVisible" title="确认发货" width="480px" destroy-on-close>
      <el-form :model="shipForm" :rules="shipRules" ref="shipFormRef" label-width="90px">
        <el-form-item label="物流公司" prop="logisticsCompany">
          <el-select v-model="shipForm.logisticsCompany" placeholder="请选择物流公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="京东物流" value="京东物流" />
            <el-option label="极兔速递" value="极兔速递" />
            <el-option label="邮政EMS" value="邮政EMS" />
          </el-select>
        </el-form-item>
        <el-form-item label="运单号" prop="trackingNumber">
          <el-input v-model="shipForm.trackingNumber" placeholder="请输入运单号" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip" :loading="shipping">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { farmerApi } from '@/api'

const loading = ref(false)
const shipping = ref(false)
const orders = ref([])
const total = ref(0)
const filterStatus = ref(undefined)
const detailVisible = ref(false)
const shipDialogVisible = ref(false)
const currentOrder = ref(null)
const shipFormRef = ref()

const pagination = reactive({
  current: 1,
  size: 10
})

const shipForm = reactive({
  orderId: null,
  logisticsCompany: '',
  trackingNumber: ''
})

const shipRules = {
  logisticsCompany: [{ required: true, message: '请选择物流公司', trigger: 'change' }],
  trackingNumber: [{ required: true, message: '请输入运单号', trigger: 'blur' }]
}

let refreshTimer = null

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await farmerApi.getFarmerOrders(pagination.current, pagination.size, filterStatus.value)
    orders.value = res?.list || res?.records || []
    total.value = res?.total || 0
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  pagination.current = 1
  fetchOrders()
}

const getStatusText = (status) => {
  const map = { 0: '待付款', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: '', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
}

const formatPrice = (value) => {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toFixed(2)
}

const handleViewDetail = (row) => {
  currentOrder.value = row
  detailVisible.value = true
}

const handleShip = (row) => {
  currentOrder.value = row
  shipForm.orderId = row.id
  shipForm.logisticsCompany = ''
  shipForm.trackingNumber = ''
  shipDialogVisible.value = true
}

const confirmShip = async () => {
  await shipFormRef.value.validate()
  shipping.value = true
  try {
    await farmerApi.shipOrder(shipForm.orderId, {
      logisticsCompany: shipForm.logisticsCompany,
      trackingNumber: shipForm.trackingNumber
    })
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    fetchOrders()
  } catch (error) {
    ElMessage.error(error.message || '发货失败')
  } finally {
    shipping.value = false
  }
}

const handleCancelShip = async (row) => {
  try {
    await ElMessageBox.confirm('确认取消发货？订单将恢复为待发货状态', '提示', { type: 'warning' })
    await farmerApi.cancelShip(row.id)
    ElMessage.success('已取消发货')
    fetchOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

onMounted(() => {
  fetchOrders()
  refreshTimer = setInterval(fetchOrders, 60000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped>
.farmer-orders-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.filter-bar {
  margin-bottom: 16px;
}

.order-product {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2px 0;
}

.product-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 8px;
}

.product-qty {
  color: #999;
  font-size: 12px;
  flex-shrink: 0;
}

.more-items {
  color: #999;
  font-size: 12px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
