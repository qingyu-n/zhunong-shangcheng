<template>
  <div class="order-list-page">
    <ArtPageWrapper>
      <ArtCard title="订单列表">
        <div class="toolbar">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="订单号"
            clearable
            style="width: 180px"
          />
          <el-input
            v-model="searchForm.username"
            placeholder="用户名"
            clearable
            style="width: 150px"
          />
          <el-select
            v-model="searchForm.status"
            placeholder="订单状态"
            clearable
            style="width: 120px"
          >
            <el-option label="待付款" :value="0" />
            <el-option label="已付款" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
          <el-button type="primary" @click="handleSearch"
            ><i class="ri-search-line"></i> 搜索</el-button
          >
          <el-button @click="handleReset">重置</el-button>
        </div>

        <el-table :data="orderList" v-loading="loading">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column label="用户" width="120">
            <template #default="{ row }">
              {{ row.username || row.userName || row.user_name || row.nickname || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="订单金额" width="100">
            <template #default="{ row }">
              <span class="amount">¥{{ row.amount || row.totalAmount || row.total_amount || row.payAmount || '0.00' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ row.statusName || getStatusName(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
              <el-button type="success" link size="small" @click="handleUpdateStatus(row)"
                >更新状态</el-button
              >
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </ArtCard>
    </ArtPageWrapper>

    <el-dialog v-model="statusDialogVisible" title="更新订单状态" width="400px">
      <el-form label-width="100px">
        <el-form-item label="订单状态">
          <el-select v-model="statusForm.status" style="width: 100%">
            <el-option label="待付款" :value="0" />
            <el-option label="已付款" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitStatus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import { adminApi } from '@/api/admin'

  const router = useRouter()
  const loading = ref(false)
  const orderList = ref([])
  const statusDialogVisible = ref(false)
  const currentOrderId = ref<number | null>(null)

  const searchForm = reactive({
    orderNo: '',
    username: '',
    status: undefined as number | undefined
  })

  const statusForm = reactive({
    status: 0
  })

  const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
  })

  const getStatusType = (status: number): 'primary' | 'success' | 'warning' | 'info' | 'danger' => {
    const types: Record<number, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
      0: 'warning',
      1: 'success',
      2: 'primary',
      3: 'success',
      4: 'danger'
    }
    return types[status] || 'info'
  }

  const getStatusName = (status: number): string => {
    const names: Record<number, string> = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消' }
    return names[status] || '未知'
  }

  const fetchOrderList = async () => {
    loading.value = true
    try {
      const data = await adminApi.getOrderList({
        current: pagination.current,
        size: pagination.size,
        orderNo: searchForm.orderNo || undefined,
        username: searchForm.username || undefined,
        status: searchForm.status
      })
      if (data) {
        const rawList = (data as any).list || (data as any).records || []
        orderList.value = rawList.map((item: any) => ({
          ...item,
          statusName: item.statusName || getStatusName(item.status),
          username: item.username || item.userName || item.user_name || item.nickname,
          amount: item.amount || item.totalAmount || item.total_amount || 0
        }))
        pagination.total = (data as any).total || 0
      }
    } catch {
      console.error('获取订单列表失败')
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    pagination.current = 1
    fetchOrderList()
  }

  const handleReset = () => {
    searchForm.orderNo = ''
    searchForm.username = ''
    searchForm.status = undefined
    pagination.current = 1
    fetchOrderList()
  }

  const handleSizeChange = (val: number) => {
    pagination.size = val
    fetchOrderList()
  }

  const handleCurrentChange = (val: number) => {
    pagination.current = val
    fetchOrderList()
  }

  const handleView = (row: any) => {
    router.push(`/zhunong/order-detail/${row.id}`)
  }

  const handleUpdateStatus = (row: any) => {
    currentOrderId.value = row.id
    statusForm.status = row.status
    statusDialogVisible.value = true
  }

  const handleSubmitStatus = async () => {
    if (!currentOrderId.value) return
    try {
      await adminApi.updateOrderStatus({
        orderId: currentOrderId.value,
        status: statusForm.status
      })
      ElMessage.success('状态更新成功')
      statusDialogVisible.value = false
      fetchOrderList() // 重新获取列表以刷新数据
    } catch (error: any) {
      console.error('更新状态失败:', error)
      ElMessage.error(error.message || '更新状态失败')
    }
  }

  onMounted(() => {
    fetchOrderList()
  })
</script>

<style scoped lang="scss">
  .order-list-page {
    .toolbar {
      display: flex;
      gap: 12px;
      margin-bottom: 20px;
    }

    .amount {
      color: #ff6b6b;
      font-weight: 600;
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }
  }
</style>
