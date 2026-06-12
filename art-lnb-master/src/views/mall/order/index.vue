<template>
  <div class="order-manage-page">
    <ElCard class="search-card">
      <ElForm :model="searchForm" inline>
        <ElFormItem label="订单编号">
          <ElInput v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
        </ElFormItem>
        <ElFormItem label="订单状态">
          <ElSelect v-model="searchForm.status" placeholder="请选择状态" clearable>
            <ElOption label="待付款" :value="0" />
            <ElOption label="已付款" :value="1" />
            <ElOption label="已发货" :value="2" />
            <ElOption label="已完成" :value="3" />
            <ElOption label="已取消" :value="4" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="下单时间">
          <ElDatePicker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <Icon name="ri:search-line" class="mr-1" />
            查询
          </ElButton>
          <ElButton @click="handleReset">
            <Icon name="ri:refresh-line" class="mr-1" />
            重置
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="table-card mt-4">
      <div class="table-header">
        <h3 class="table-title">订单列表</h3>
      </div>

      <ElTable v-loading="loading" :data="orderList" border stripe style="width: 100%">
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn prop="orderNo" label="订单编号" min-width="180" />
        <ElTableColumn prop="username" label="下单用户" width="120" />
        <ElTableColumn prop="totalAmount" label="订单金额" width="100" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ row.totalAmount }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="payAmount" label="实付金额" width="100" align="center">
          <template #default="{ row }">
            <span class="pay-price">¥{{ row.payAmount }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" label="订单状态" width="100" align="center">
          <template #default="{ row }">
            <ElTag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="receiverName" label="收货人" width="100" />
        <ElTableColumn prop="receiverPhone" label="联系电话" width="130" />
        <ElTableColumn prop="createTime" label="下单时间" min-width="160" />
        <ElTableColumn label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <ElButton type="primary" link @click="handleViewDetail(row)"> 详情 </ElButton>
            <ElButton
              v-if="row.status === 1"
              type="success"
              link
              @click="handleUpdateStatus(row, 2)"
            >
              发货
            </ElButton>
            <ElButton
              v-if="row.status === 0"
              type="danger"
              link
              @click="handleUpdateStatus(row, 4)"
            >
              取消
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <div class="pagination-wrapper">
        <ElPagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </ElCard>

    <!-- 订单详情弹窗 -->
    <ElDialog v-model="detailVisible" title="订单详情" width="800px" destroy-on-close>
      <div v-if="currentOrder" class="order-detail">
        <!-- 订单信息 -->
        <div class="detail-section">
          <h4 class="section-title">订单信息</h4>
          <ElDescriptions :column="2" border>
            <ElDescriptionsItem label="订单编号">{{ currentOrder.orderNo }}</ElDescriptionsItem>
            <ElDescriptionsItem label="订单状态">
              <ElTag :type="getStatusType(currentOrder.status)">
                {{ getStatusText(currentOrder.status) }}
              </ElTag>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="下单用户">{{ currentOrder.username }}</ElDescriptionsItem>
            <ElDescriptionsItem label="用户ID">{{ currentOrder.userId }}</ElDescriptionsItem>
            <ElDescriptionsItem label="订单金额"
              >¥{{ currentOrder.totalAmount }}</ElDescriptionsItem
            >
            <ElDescriptionsItem label="实付金额">¥{{ currentOrder.payAmount }}</ElDescriptionsItem>
            <ElDescriptionsItem label="下单时间" :span="2">{{
              currentOrder.createTime
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="付款时间" :span="2">{{
              currentOrder.payTime || '-'
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="发货时间" :span="2">{{
              currentOrder.shipTime || '-'
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="收货时间" :span="2">{{
              currentOrder.receiveTime || '-'
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="订单备注" :span="2">{{
              currentOrder.remark || '-'
            }}</ElDescriptionsItem>
          </ElDescriptions>
        </div>

        <!-- 收货信息 -->
        <div class="detail-section">
          <h4 class="section-title">收货信息</h4>
          <ElDescriptions :column="2" border>
            <ElDescriptionsItem label="收货人">{{ currentOrder.receiverName }}</ElDescriptionsItem>
            <ElDescriptionsItem label="联系电话">{{
              currentOrder.receiverPhone
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="收货地址" :span="2">{{
              currentOrder.receiverAddress
            }}</ElDescriptionsItem>
          </ElDescriptions>
        </div>

        <!-- 商品信息 -->
        <div class="detail-section">
          <h4 class="section-title">商品信息</h4>
          <ElTable :data="currentOrder.items" border size="small">
            <ElTableColumn label="商品图片" width="80" align="center">
              <template #default="{ row }">
                <img :src="row.productImage" class="product-image" />
              </template>
            </ElTableColumn>
            <ElTableColumn prop="productName" label="商品名称" min-width="200" />
            <ElTableColumn prop="price" label="单价" width="100" align="center">
              <template #default="{ row }">
                <span class="price">¥{{ row.price }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="quantity" label="数量" width="80" align="center" />
            <ElTableColumn prop="totalAmount" label="小计" width="100" align="center">
              <template #default="{ row }">
                <span class="price">¥{{ row.totalAmount }}</span>
              </template>
            </ElTableColumn>
          </ElTable>
        </div>

        <!-- 操作按钮 -->
        <div class="detail-actions">
          <ElButton
            v-if="currentOrder.status === 1"
            type="primary"
            @click="handleUpdateStatus(currentOrder, 2)"
          >
            确认发货
          </ElButton>
          <ElButton
            v-if="currentOrder.status === 0"
            type="danger"
            @click="handleUpdateStatus(currentOrder, 4)"
          >
            取消订单
          </ElButton>
        </div>
      </div>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { fetchGetOrderList, fetchGetOrderDetail, fetchUpdateOrderStatus } from '@/api/mall'

  defineOptions({ name: 'OrderManage' })

  // 搜索表单
  const searchForm = reactive({
    orderNo: '',
    status: undefined as number | undefined,
    startTime: '',
    endTime: ''
  })

  // 日期范围
  const dateRange = ref<string[]>([])

  // 分页
  const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
  })

  // 表格数据
  const loading = ref(false)
  const orderList = ref<Api.Order.OrderItem[]>([])

  // 详情弹窗
  const detailVisible = ref(false)
  const currentOrder = ref<Api.Order.OrderDetail | null>(null)

  // 状态映射
  const statusMap: Record<number, { text: string; type: 'warning' | 'success' | 'primary' | 'info' | 'danger' }> = {
    0: { text: '待付款', type: 'warning' },
    1: { text: '已付款', type: 'success' },
    2: { text: '已发货', type: 'primary' },
    3: { text: '已完成', type: 'success' },
    4: { text: '已取消', type: 'info' }
  }

  const getStatusText = (status: number) => statusMap[status]?.text || '未知'
  const getStatusType = (status: number): 'warning' | 'success' | 'primary' | 'info' | 'danger' => statusMap[status]?.type || 'info'

  // 获取订单列表
  const getOrderList = async () => {
    loading.value = true
    try {
      const res = await fetchGetOrderList({
        current: pagination.current,
        size: pagination.size,
        orderNo: searchForm.orderNo || undefined,
        status: searchForm.status,
        startTime: dateRange.value?.[0],
        endTime: dateRange.value?.[1]
      })
      orderList.value = res.records
      pagination.total = res.total
    } catch (error) {
      console.error('获取订单列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 搜索
  const handleSearch = () => {
    pagination.current = 1
    getOrderList()
  }

  // 重置
  const handleReset = () => {
    searchForm.orderNo = ''
    searchForm.status = undefined
    dateRange.value = []
    pagination.current = 1
    getOrderList()
  }

  // 查看详情
  const handleViewDetail = async (row: Api.Order.OrderItem) => {
    try {
      const res = await fetchGetOrderDetail(row.id)
      currentOrder.value = res
      detailVisible.value = true
    } catch (error) {
      ElMessage.error('获取订单详情失败')
    }
  }

  // 更新订单状态
  const handleUpdateStatus = async (row: Api.Order.OrderItem, status: number) => {
    const actionText = status === 2 ? '发货' : status === 4 ? '取消' : '更新'
    try {
      await ElMessageBox.confirm(`确定要${actionText}该订单吗？`, '提示', { type: 'warning' })
      await fetchUpdateOrderStatus({ id: row.id, status })
      ElMessage.success(`${actionText}成功`)
      getOrderList()
      // 如果详情弹窗打开，关闭它
      if (detailVisible.value) {
        detailVisible.value = false
      }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('操作失败')
      }
    }
  }

  // 分页变化
  const handleSizeChange = (val: number) => {
    pagination.size = val
    getOrderList()
  }

  const handleCurrentChange = (val: number) => {
    pagination.current = val
    getOrderList()
  }

  onMounted(() => {
    getOrderList()
  })
</script>

<style scoped>
  .order-manage-page {
    padding: 20px;
  }

  .search-card :deep(.el-card__body) {
    padding: 20px;
  }

  .table-card :deep(.el-card__body) {
    padding: 20px;
  }

  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .table-title {
    font-size: 16px;
    font-weight: 600;
    margin: 0;
  }

  .price {
    color: #f56c6c;
    font-weight: bold;
  }

  .pay-price {
    color: #67c23a;
    font-weight: bold;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .mr-1 {
    margin-right: 4px;
  }

  .mt-4 {
    margin-top: 16px;
  }

  /* 订单详情样式 */
  .order-detail {
    max-height: 600px;
    overflow-y: auto;
  }

  .detail-section {
    margin-bottom: 24px;
  }

  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px 0;
    padding-left: 12px;
    border-left: 4px solid var(--el-color-primary);
  }

  .product-image {
    width: 50px;
    height: 50px;
    object-fit: cover;
    border-radius: 4px;
  }

  .detail-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
  }
</style>
