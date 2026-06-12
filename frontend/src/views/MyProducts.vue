<template>
  <div class="my-products-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <h2>我的商品</h2>
          <el-button type="primary" @click="$router.push('/publish')">
            <el-icon><Plus /></el-icon> 发布新商品
          </el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-input v-model="searchForm.keyword" placeholder="搜索商品名称" clearable style="width: 200px; margin-right: 16px;" @keyup.enter="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="searchForm.auditStatus" placeholder="审核状态" clearable style="width: 140px; margin-right: 16px;">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="商品状态" clearable style="width: 140px; margin-right: 16px;">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 搜索</el-button>
        <el-button @click="handleReset"><el-icon><Refresh /></el-icon> 重置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="商品" min-width="250">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image :src="row.mainImage" style="width: 60px; height: 60px; border-radius: 4px;" fit="cover" />
              <div class="product-info">
                <div class="name">{{ row.name }}</div>
                <div class="price">¥{{ row.price }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="stock" label="库存" width="80" align="center" />

        <el-table-column prop="sales" label="销量" width="80" align="center" />

        <el-table-column label="审核状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getAuditStatusType(row.auditStatus)" size="small">
              {{ getAuditStatusText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />

        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'" 
              link 
              size="small" 
              @click="handleToggleStatus(row)"
              :disabled="row.auditStatus !== 1"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-popconfirm title="确定删除该商品吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无商品数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { farmerApi } from '@/api'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  auditStatus: undefined,
  status: undefined
})

const pagination = reactive({
  current: 1,
  size: 10
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await farmerApi.getMyProducts(pagination.current, pagination.size, searchForm.keyword, searchForm.auditStatus, searchForm.status)
    // 后端返回PageResult，字段为list和total
    const data = res || {}
    tableData.value = data.list || data.records || []
    total.value = data.total || 0
  } catch (error) {
    console.error('获取数据失败:', error)
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.auditStatus = undefined
  searchForm.status = undefined
  handleSearch()
}

const getAuditStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getAuditStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

const handleEdit = (row) => {
  router.push(`/publish/${row.id}`)
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await farmerApi.updateProductStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '上架成功' : '下架成功')
    fetchData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await farmerApi.deleteProduct(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.my-products-page {
  margin: 20px auto;
  max-width: 1200px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h2 {
      margin: 0;
      font-size: 20px;
      color: #333;
    }
  }

  .toolbar {
    margin-bottom: 16px;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .product-cell {
    display: flex;
    align-items: center;
    gap: 12px;

    .product-info {
      flex: 1;

      .name {
        font-weight: 500;
        color: #333;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .price {
        color: #ff4d4f;
        font-weight: bold;
        font-size: 14px;
      }
    }
  }

  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
