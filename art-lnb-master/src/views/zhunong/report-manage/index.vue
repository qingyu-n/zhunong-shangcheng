<template>
  <div class="report-manage-page">
    <ArtPageWrapper>
      <ArtCard title="举报管理">
        <template #headerExtra>
          <el-badge :value="pendingCount" :hidden="pendingCount === 0">
            <el-button type="warning"><i class="ri-alert-line"></i> 待处理 ({{ pendingCount }})</el-button>
          </el-badge>
        </template>

        <!-- 工具栏 -->
        <div class="toolbar">
          <el-select v-model="searchForm.status" placeholder="处理状态" clearable style="width: 140px;">
            <el-option label="待处理" :value="0" />
            <el-option label="已核实" :value="1" />
            <el-option label="已忽略" :value="2" />
            <el-option label="已处罚" :value="3" />
          </el-select>
          <el-button type="primary" style="margin-left: 12px;" @click="handleSearch"><i class="ri-search-line"></i> 搜索</el-button>
        </div>

        <!-- 数据表格 -->
        <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe style="width: 100%">
          <el-table-column type="index" label="#" width="60" align="center" />

          <el-table-column label="被举报商品" min-width="200">
            <template #default="{ row }">
              <div class="product-cell">
                <el-image v-if="row.productInfo?.mainImage" :src="row.productInfo.mainImage" style="width: 50px; height: 50px; border-radius: 4px;" fit="cover" />
                <span>{{ row.productInfo?.name || '-' }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="reporterName" label="举报人" width="100" align="center" />

          <el-table-column label="举报类型" width="110" align="center">
            <template #default="{ row }">
              <el-tag size="small">{{ getReasonTypeText(row.reasonType) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="reasonDetail" label="举报说明" min-width="200" show-overflow-tooltip />

          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="getReportStatusType(row.status)" size="small" effect="dark">
                {{ getReportStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="createTime" label="举报时间" width="165" align="center" />

          <el-table-column label="操作" width="220" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
              <el-button type="warning" link size="small" @click="handleView(row)" v-if="row.status === 0">处理</el-button>
              <el-button type="danger" link size="small" @click="handleDeleteReport(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="total"
            layout="total, prev, pager, next"
            @size-change="fetchData"
            @current-change="fetchData"
          />
        </div>
      </ArtCard>
    </ArtPageWrapper>

    <!-- 处理举报弹窗 -->
    <el-dialog v-model="handleDialogVisible" title="处理举报" width="550px" destroy-on-close>
      <div v-if="currentRow" class="report-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="商品名称">{{ currentRow.productInfo?.name }}</el-descriptions-item>
          <el-descriptions-item label="举报人">{{ currentRow.reporterName }}</el-descriptions-item>
          <el-descriptions-item label="举报类型">{{ getReasonTypeText(currentRow.reasonType) }}</el-descriptions-item>
          <el-descriptions-item label="举报说明" :span="2">{{ currentRow.reasonDetail }}</el-descriptions-item>
          <el-descriptions-item label="证据图片" :span="2">
            <div v-if="evidenceImages.length > 0" class="evidence-images">
              <el-image
                v-for="(img, index) in evidenceImages"
                :key="index"
                :src="img"
                fit="cover"
                style="width: 80px; height: 80px; margin-right: 8px;"
                :preview-src-list="evidenceImages"
              />
            </div>
            <span v-else>无</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <el-form :model="handleForm" label-width="90px">
          <el-form-item label="处理方式" required>
            <el-radio-group v-model="handleForm.status">
              <el-radio :label="1">核实无误</el-radio>
              <el-radio :label="2">忽略</el-radio>
              <el-radio :label="3">确认违规，处罚</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="处理结果">
            <el-input
              v-model="handleForm.handleResult"
              type="textarea"
              :rows="3"
              placeholder="请填写处理结果说明..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmHandle">确认处理</el-button>
      </template>
    </el-dialog>

    <!-- 举报详情弹窗（只读） -->
    <el-dialog v-model="detailDialogVisible" title="举报详情" width="600px" destroy-on-close>
      <div v-if="currentRow" class="report-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品名称" :span="2">
            <div class="product-info-cell" v-if="currentRow.productInfo">
              <el-image v-if="currentRow.productInfo.mainImage" :src="currentRow.productInfo.mainImage" style="width: 60px; height: 60px; border-radius: 4px;" fit="cover" />
              <div class="product-info-text">
                <div class="product-name">{{ currentRow.productInfo.name || '-' }}</div>
                <div class="product-price">¥{{ currentRow.productInfo.price || '0.00' }}</div>
              </div>
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="举报人">{{ currentRow.reporterName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="举报类型">{{ getReasonTypeText(currentRow.reasonType) }}</el-descriptions-item>
          <el-descriptions-item label="举报说明" :span="2">{{ currentRow.reasonDetail || '-' }}</el-descriptions-item>
          <el-descriptions-item label="证据图片" :span="2">
            <div v-if="evidenceImages.length > 0" class="evidence-images">
              <el-image
                v-for="(img, index) in evidenceImages"
                :key="index"
                :src="img"
                fit="cover"
                style="width: 80px; height: 80px; margin-right: 8px;"
                :preview-src-list="evidenceImages"
              />
            </div>
            <span v-else>无</span>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getReportStatusType(currentRow.status)" size="small" effect="dark">
              {{ getReportStatusText(currentRow.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="举报时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRow.handleResult" label="处理结果" :span="2">{{ currentRow.handleResult }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchGetReportList, fetchHandleReport, fetchDeleteReport } from '@/api/mall'

const loading = ref(false)
const tableData = ref<any[]>([])
const handleDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref<any>(null)
const pendingCount = ref(0)

const searchForm = reactive({
  status: undefined as number | undefined
})

const handleForm = reactive({
  status: 1 as number,
  handleResult: ''
})

const evidenceImages = computed(() => {
  if (!currentRow.value?.evidenceImages) return []
  try {
    return JSON.parse(currentRow.value.evidenceImages)
  } catch {
    return []
  }
})

const pagination = reactive({
  current: 1,
  size: 15
})

const total = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await fetchGetReportList({
      current: pagination.current,
      size: pagination.size,
      status: searchForm.status
    })
    const rawList = res?.list || res?.records || []
    tableData.value = rawList
    total.value = res?.total || 0
    
    // 统计待处理数量
    pendingCount.value = rawList.filter((item: any) => item.status === 0).length
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const getReasonTypeText = (type: number) => {
  const map: Record<number, string> = { 1: '假冒伪劣', 2: '价格欺诈', 3: '虚假宣传', 4: '其他' }
  return map[type] || '未知'
}

const getReportStatusType = (status: number): 'warning' | 'success' | 'info' | 'danger' => {
  const map: Record<number, 'warning' | 'success' | 'info' | 'danger'> = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }
  return map[status] || 'info'
}

const getReportStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '待处理', 1: '已核实', 2: '已忽略', 3: '已处罚' }
  return map[status] || '未知'
}

const handleView = (row: any) => {
  currentRow.value = row
  handleForm.status = 1
  handleForm.handleResult = ''
  handleDialogVisible.value = true
}

const handleViewDetail = (row: any) => {
  currentRow.value = row
  detailDialogVisible.value = true
}

const confirmHandle = async () => {
  try {
    await fetchHandleReport(currentRow.value.id, {
      status: handleForm.status,
      remark: handleForm.handleResult
    })
    ElMessage.success('处理完成')
    handleDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDeleteReport = (row: any) => {
  ElMessageBox.confirm('确定要删除该举报记录吗？删除后不可恢复！', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await fetchDeleteReport(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error: any) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => fetchData())
</script>

<style scoped lang="scss">
.report-manage-page {
  .toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 16px;
  }

  .product-cell {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .product-info-cell {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .product-info-text {
    .product-name {
      font-size: 14px;
      font-weight: 500;
      color: #333;
    }

    .product-price {
      font-size: 13px;
      color: #f56c6c;
      margin-top: 4px;
    }
  }

  .evidence-images {
    display: flex;
    flex-wrap: wrap;
  }

  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .report-detail {
    .el-descriptions {
      margin-bottom: 16px;
    }
  }
}
</style>
