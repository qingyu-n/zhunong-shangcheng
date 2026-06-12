<template>
  <div class="farmer-manage-page">
    <ArtPageWrapper>
      <ArtCard title="农户管理">
        <template #headerExtra>
          <el-button @click="handleRefresh"><i class="ri-refresh-line"></i> 刷新</el-button>
        </template>

        <!-- 工具栏 -->
        <div class="toolbar">
          <el-input
            v-model="searchForm.keyword"
            placeholder="店铺名称/联系人"
            clearable
            style="width: 220px"
          />
          <el-select
            v-model="searchForm.status"
            placeholder="审核状态"
            clearable
            style="width: 130px; margin-left: 12px"
          >
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
          <el-button type="primary" style="margin-left: 12px" @click="handleSearch"
            ><i class="ri-search-line"></i> 搜索</el-button
          >
        </div>

        <!-- 数据表格 -->
    <el-table ref="tableRef" :data="processedTableData" v-loading="loading" stripe style="width: 100%">
          <el-table-column type="index" label="#" width="60" align="center" />

          <el-table-column prop="userId" label="用户ID" width="80" align="center" />

          <el-table-column prop="shopName" label="店铺名称" min-width="150" show-overflow-tooltip />

          <el-table-column prop="contactName" label="联系人" width="100" align="center" />

          <el-table-column prop="contactPhone" label="联系电话" width="130" align="center">
            <template #default="{ row }">
              {{ maskPhone(row.contactPhone) }}
            </template>
          </el-table-column>

          <el-table-column
            prop="contactAddress"
            label="联系地址"
            min-width="180"
            show-overflow-tooltip
          />

          <el-table-column label="审核状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small" effect="dark">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="createTime" label="申请时间" width="165" align="center" />

          <el-table-column label="操作" width="260" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleView(row)"
                >查看详情</el-button
              >
              <el-button
                v-if="row.status === 0"
                type="success"
                link
                size="small"
                @click="handleApprove(row)"
                >通过</el-button
              >
              <el-button
                v-if="row.status === 0"
                type="danger"
                link
                size="small"
                @click="handleReject(row)"
                >拒绝</el-button
              >
              <el-button
                v-if="row.status === 1 || row.status === 2"
                type="danger"
                link
                size="small"
                @click="handleDeleteFarmer(row)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchData"
            @current-change="fetchData"
          />
        </div>
      </ArtCard>
    </ArtPageWrapper>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="农户申请详情" width="650px" destroy-on-close>
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="用户ID">{{ currentRow.userId }}</el-descriptions-item>
        <el-descriptions-item label="店铺名称">{{ currentRow.shopName }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{
          currentRow.contactName || currentRow.realName || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{
          currentRow.contactPhone || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="联系地址" :span="2">{{
          currentRow.contactAddress || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="店铺简介" :span="2">{{
          currentRow.description || '暂无'
        }}</el-descriptions-item>
        <el-descriptions-item label="营业执照" :span="2">
          <el-image
            v-if="currentRowLicenseUrl"
            :src="currentRowLicenseUrl"
            fit="contain"
            style="max-height: 200px"
            :preview-src-list="[currentRowLicenseUrl]"
          />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间" :span="2">{{
          formatTime(currentRow.createTime)
        }}</el-descriptions-item>
      </el-descriptions>

      <div
        v-if="currentRow?.auditRemark"
        style="margin-top: 16px; padding: 10px; background: #fff7e6; border-radius: 4px"
      >
        <strong>审核备注：</strong>{{ currentRow.auditRemark }}
      </div>
    </el-dialog>

    <!-- 拒绝原因弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝农户申请" width="480px">
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
  import { ref, reactive, onMounted, computed } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    fetchGetFarmerList,
    fetchApproveFarmer,
    fetchRejectFarmer,
    fetchDeleteFarmer
  } from '@/api/mall'

  const loading = ref(false)
  const tableData = ref<any[]>([])
  const detailDialogVisible = ref(false)
  const rejectDialogVisible = ref(false)
  const currentRow = ref<any>(null)

  // 图片URL处理函数
  const processImageUrl = (url: string) => {
    if (!url) return ''
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url
    }
    if (url.startsWith('/')) {
      const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
      return `${baseUrl}${url}`
    }
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    return `${baseUrl}/${url}`
  }

  // 处理表格数据图片URL的计算属性
  const processedTableData = computed(() => {
    return tableData.value.map(item => ({
      ...item,
      businessLicense: processImageUrl(item.businessLicense)
    }))
  })

  // 处理当前行营业执照图片
  const currentRowLicenseUrl = computed(() => {
    return currentRow.value ? processImageUrl(currentRow.value.businessLicense) : ''
  })

  const searchForm = reactive({
    keyword: '',
    status: undefined as number | undefined
  })

  const rejectForm = reactive({
    reason: ''
  })

  const pagination = reactive({
    current: 1,
    size: 10
  })

  const total = ref(0)

  const fetchData = async () => {
    loading.value = true
    try {
      const res: any = await fetchGetFarmerList({
        current: pagination.current,
        size: pagination.size,
        keyword: searchForm.keyword || undefined,
        status: searchForm.status
      })
      tableData.value = res?.list || res?.records || []
      total.value = res?.total || 0
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    pagination.current = 1
    fetchData()
  }

  const handleRefresh = () => fetchData()

  const getStatusType = (status: number): 'warning' | 'success' | 'danger' | 'info' => {
    const map: Record<number, 'warning' | 'success' | 'danger' | 'info'> = {
      0: 'warning',
      1: 'success',
      2: 'danger'
    }
    return map[status] || 'info'
  }

  const getStatusText = (status: number) => {
    const map: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
    return map[status] || '未知'
  }

  const formatTime = (timeStr: string) =>
    timeStr ? new Date(timeStr).toLocaleString('zh-CN') : '-'

  const maskPhone = (phone?: string) =>
    phone ? phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '-'

  const handleView = (row: any) => {
    currentRow.value = row
    detailDialogVisible.value = true
  }

  const handleApprove = async (row: any) => {
    try {
      await ElMessageBox.confirm('确认通过该用户的农户申请？', '提示', { type: 'warning' })
      await fetchApproveFarmer(row.id, { remark: '' })
      ElMessage.success('审核通过，已授予农户身份')
      await fetchData()
    } catch (e: any) {
      if (e !== 'cancel') ElMessage.error(e.message || '审核操作失败，请重试')
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
      await fetchRejectFarmer(currentRow.value.id, { reason: rejectForm.reason })
      ElMessage.success('已拒绝该农户申请')
      rejectDialogVisible.value = false
      await fetchData()
    } catch (error: any) {
      ElMessage.error(error.message || '拒绝操作失败，请重试')
    }
  }

  const handleDeleteFarmer = (row: any) => {
    ElMessageBox.confirm('确定要删除该农户吗？删除后将撤销农户身份！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        try {
          await fetchDeleteFarmer(row.id)
          ElMessage.success('删除成功，已撤销农户身份')
          await fetchData()
        } catch (error: any) {
          ElMessage.error(error.message || '删除失败，请重试')
        }
      })
      .catch(() => {})
  }

  onMounted(() => fetchData())
</script>

<style scoped lang="scss">
  .farmer-manage-page {
    .toolbar {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 16px;
    }

    .pagination-wrapper {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
</style>
