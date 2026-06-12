<template>
  <div class="product-audit-page">
    <ArtPageWrapper>
      <ArtCard title="商品审核">
        <template #headerExtra>
          <el-button @click="handleRefresh"><i class="ri-refresh-line"></i> 刷新</el-button>
        </template>

        <!-- 工具栏 -->
        <div class="toolbar">
          <el-input v-model="searchForm.keyword" placeholder="商品名称/农户昵称" clearable style="width: 220px" />
          <el-select v-model="searchForm.auditStatus" placeholder="审核状态" clearable style="width: 130px; margin-left: 12px;">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
          <el-button type="primary" style="margin-left: 12px;" @click="handleSearch"><i class="ri-search-line"></i> 搜索</el-button>
        </div>

        <!-- 统计卡片 -->
        <el-row :gutter="16" class="stats-row" style="margin-bottom: 20px;">
          <el-col :span="8">
            <el-card shadow="hover" class="stat-card pending">
              <div class="stat-number">{{ stats.pending }}</div>
              <div class="stat-label">待审核</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="stat-card approved">
              <div class="stat-number">{{ stats.approvedToday }}</div>
              <div class="stat-label">今日通过</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="stat-card rejected">
              <div class="stat-number">{{ stats.rejectedToday }}</div>
              <div class="stat-label">今日拒绝</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 数据表格 -->
        <el-table 
          ref="tableRef"
          :data="tableData" 
          v-loading="loading" 
          @selection-change="handleSelectionChange"
          stripe
          style="width: 100%"
        >
          <el-table-column type="selection" width="50" align="center" />
          
          <el-table-column type="index" label="#" width="60" align="center" />

          <el-table-column label="商品图片" width="100" align="center">
            <template #default="{ row }">
              <el-image 
                v-if="row.productInfo?.mainImage"
                :src="row.productInfo.mainImage" 
                style="width: 70px; height: 70px; border-radius: 6px;" 
                fit="cover" 
                :preview-src-list="[row.productInfo.mainImage]"
              />
              </template>
          </el-table-column>

          <el-table-column prop="productInfo.name" label="商品名称" min-width="180" show-overflow-tooltip />

          <el-table-column prop="farmerName" label="发布农户" width="120" show-overflow-tooltip />

          <el-table-column prop="productInfo.categoryName" label="分类" width="100" align="center" />

          <el-table-column label="价格" width="90" align="center">
            <template #default="{ row }">
              ¥{{ row.productInfo?.price || '-' }}
            </template>
          </el-table-column>

          <el-table-column label="审核状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getAuditStatusType(row.auditStatus)" size="small" effect="dark">
                {{ getAuditStatusText(row.auditStatus) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="createTime" label="提交时间" width="165" align="center" />

          <el-table-column label="操作" width="280" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
              <el-button type="success" link size="small" @click="handleApprove(row)" :disabled="row.auditStatus !== 0">通过</el-button>
              <el-button type="danger" link size="small" @click="handleReject(row)" :disabled="row.auditStatus !== 0">拒绝</el-button>
              <el-button type="danger" link size="small" @click="handleDeleteProduct(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchData"
            @current-change="fetchData"
          />
        </div>

        <!-- 批量操作栏 -->
        <div class="batch-actions" v-if="selectedRows.length > 0">
          <span>已选 {{ selectedRows.length }} 项</span>
          <el-button type="success" size="small" @click="batchApprove">批量通过</el-button>
          <el-button type="danger" size="small" @click="batchReject">批量拒绝</el-button>
        </div>
      </ArtCard>
    </ArtPageWrapper>

    <!-- 商品详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="商品审核详情" width="750px" destroy-on-close>
      <div v-if="currentRow" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品名称">{{ currentRow.productInfo?.name }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentRow.productInfo?.categoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="售价">¥{{ currentRow.productInfo?.price || '-' }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ currentRow.productInfo?.stock || '-' }}</el-descriptions-item>
          <el-descriptions-item label="产地">{{ currentRow.productInfo?.origin || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发布农户">{{ currentRow.farmerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">{{ formatTime(currentRow.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <div class="section-title">商品主图</div>
        <el-image 
          v-if="currentRow.productInfo?.mainImage"
          :src="currentRow.productInfo.mainImage"
          fit="contain"
          style="max-width: 400px; max-height: 300px;"
          :preview-src-list="[currentRow.productInfo.mainImage]"
        />

        <div class="section-title" style="margin-top: 16px;">商品描述</div>
        <p class="description-text">{{ currentRow.productInfo?.description || '暂无描述' }}</p>
      </div>
    </el-dialog>

    <!-- 拒绝原因弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝审核" width="480px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input 
            v-model="rejectForm.reason" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入拒绝原因（必填）" 
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchGetAuditProductList, fetchApproveProduct, fetchRejectProduct, fetchDeleteAuditProduct } from '@/api/mall'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const tableData = ref<any[]>([])
const selectedRows = ref<any[]>([])
const detailDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const currentRow = ref<any>(null)
const categories = ref<any[]>([])
const farmerMap = ref<Record<number, string>>({})

const searchForm = reactive({
  keyword: '',
  auditStatus: undefined as number | undefined
})

const rejectForm = reactive({
  reason: ''
})

const pagination = reactive({
  current: 1,
  size: 10
})

const total = ref(0)

const stats = reactive({
  pending: 0,
  approvedToday: 0,
  rejectedToday: 0
})

const fetchCategories = async () => {
  try {
    const data = await adminApi.getCategoryList()
    if (data) {
      categories.value = Array.isArray(data) ? data : ((data as any)?.list || (data as any)?.records || [])
    }
  } catch {
    // ignore
  }
}

const getCategoryName = (categoryId: number | string | undefined): string => {
  if (!categoryId || categories.value.length === 0) return '-'
  const category = categories.value.find((c: any) => c.id === Number(categoryId))
  return category ? category.name : '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await fetchGetAuditProductList({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      auditStatus: searchForm.auditStatus
    })
    const rawList = res?.list || res?.records || []
    // 将后端返回的扁平Product结构映射为前端需要的结构
    tableData.value = rawList.map((item: any) => ({
      ...item,
      productInfo: {
        name: item.name,
        mainImage: item.mainImage,
        categoryName: getCategoryName(item.categoryId),
        price: item.price,
        stock: item.stock,
        origin: item.origin,
        description: item.description
      },
      farmerName: item.farmerId ? `农户${item.farmerId}` : '-'
    }))
    total.value = res?.total || 0

    // 统计待审核数量
    stats.pending = rawList.filter((item: any) => item.auditStatus === 0).length
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleRefresh = () => {
  fetchData()
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const getAuditStatusType = (status: number): 'warning' | 'success' | 'danger' | 'info' => {
  const map: Record<number, 'warning' | 'success' | 'danger' | 'info'> = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getAuditStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return '-'
  return new Date(timeStr).toLocaleString('zh-CN')
}

const handleView = (row: any) => {
  currentRow.value = row
  detailDialogVisible.value = true
}

const handleApprove = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认通过该商品的审核？', '提示', { type: 'warning' })
    await fetchApproveProduct(row.id, { remark: '' })
    ElMessage.success('审核通过')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

const handleReject = (row: any) => {
  currentRow.value = row
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  try {
    await fetchRejectProduct(currentRow.value.id, { reason: rejectForm.reason })
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDeleteProduct = (row: any) => {
  ElMessageBox.confirm('确定要删除该商品吗？删除后不可恢复！', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await fetchDeleteAuditProduct(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error: any) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

const batchApprove = async () => {
  try {
    await ElMessageBox.confirm(`确认批量通过选中的 ${selectedRows.value.length} 条记录？`, '批量操作', { type: 'warning' })
    
    for (const row of selectedRows.value) {
      if (row.auditStatus === 0) {
        await fetchApproveProduct(row.id, { remark: '' })
      }
    }
    
    ElMessage.success(`成功通过 ${selectedRows.value.length} 条记录`)
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '批量操作失败')
  }
}

const batchReject = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因（将应用于所有选中项）：', '批量拒绝', {
      confirmButtonText: '确认',
      inputType: 'textarea',
      inputValidator: (val: string) => !val?.trim() ? '请输入拒绝原因' : true
    })

    for (const row of selectedRows.value) {
      if (row.auditStatus === 0) {
        await fetchRejectProduct(row.id, { reason })
      }
    }

    ElMessage.success(`成功拒绝 ${selectedRows.value.length} 条记录`)
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '批量操作失败')
  }
}

onMounted(async () => {
  await fetchCategories()
  fetchData()
})
</script>

<style scoped lang="scss">
.product-audit-page {
  .toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 16px;
  }

  .stats-row {
    .stat-card {
      text-align: center;
      padding: 16px;

      &.pending {
        background-color: #fff7e6;
        border-color: #ffd591;
        
        .stat-number { color: #fa8c16; }
      }

      &.approved {
        background-color: #f6ffed;
        border-color: #b7eb8f;
        
        .stat-number { color: #52c41a; }
      }

      &.rejected {
        background-color: #fff2f0;
        border-color: #ffccc7;
        
        .stat-number { color: #ff4d4f; }
      }

      .stat-number {
        font-size: 28px;
        font-weight: bold;
      }

      .stat-label {
        margin-top: 4px;
        color: #666;
        font-size: 13px;
      }
    }
  }

  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .batch-actions {
    position: fixed;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%);
    padding: 12px 24px;
    background: white;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    border-radius: 8px;
    display: flex;
    align-items: center;
    gap: 12px;
    z-index: 100;

    span {
      font-weight: 500;
      color: #333;
    }
  }

  .detail-content {
    .section-title {
      font-weight: 600;
      font-size: 14px;
      color: #333;
      margin: 20px 0 10px;
      padding-left: 10px;
      border-left: 3px solid #1890ff;
    }

    .description-text {
      line-height: 1.8;
      color: #666;
    }
  }
}
</style>
