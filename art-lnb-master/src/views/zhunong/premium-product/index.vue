<template>
  <div class="premium-product-page">
    <ArtPageWrapper>
      <ArtCard title="助农优选商品管理">
        <div class="toolbar">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入商品名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
          <el-button type="primary" @click="handleSearch">
            <i class="ri-search-line"></i> 搜索
          </el-button>
          <el-button type="primary" @click="showAddDialog = true">
            <i class="ri-add-line"></i> 添加优选商品
          </el-button>
        </div>

        <el-table :data="productList" v-loading="loading" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" />
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="商品图片" width="100">
            <template #default="{ row }">
              <el-image
                :src="row.mainImage"
                style="width: 60px; height: 60px; border-radius: 4px"
                fit="cover"
              />
            </template>
          </el-table-column>
          <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="origin" label="产地" width="120" show-overflow-tooltip />
          <el-table-column prop="stock" label="库存" width="80" align="center" />
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '上架' : '下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="优选标签" width="100" align="center">
            <template #default>
              <el-tag type="warning" size="small" effect="dark">助农优选</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button type="warning" link size="small" @click="handleRemove(row)">移除</el-button>
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

        <div class="batch-actions" v-if="selectedRows.length > 0">
          <span>已选 {{ selectedRows.length }} 项</span>
          <el-button type="danger" size="small" @click="handleBatchRemove">批量移除</el-button>
        </div>
      </ArtCard>
    </ArtPageWrapper>

    <el-dialog v-model="showAddDialog" title="添加助农优选商品" width="600px">
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="选择商品" required>
          <el-select
            v-model="addForm.productId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入商品名称搜索"
            :remote-method="searchProducts"
            :loading="searchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in productOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <span>{{ item.name }}</span>
              <span style="float: right; color: #999; font-size: 12px">{{ item.origin || '未知产地' }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-alert
          title="添加后该商品将显示在助农活动页的优选商品区域"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
        />
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleAdd">确定添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const searchLoading = ref(false)
const submitLoading = ref(false)
const productList = ref<any[]>([])
const selectedRows = ref<any[]>([])
const showAddDialog = ref(false)
const productOptions = ref<any[]>([])

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const addForm = reactive({
  productId: undefined as number | undefined
})

const fetchProductList = async () => {
  loading.value = true
  try {
    const data = await adminApi.getPremiumProductList({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined
    })
    if (data) {
      productList.value = (data as any).list || (data as any).records || []
      pagination.total = (data as any).total || 0
    }
  } catch {
    console.error('获取助农优选商品列表失败')
  } finally {
    loading.value = false
  }
}

const searchProducts = async (query: string) => {
  if (!query || query.length < 2) {
    productOptions.value = []
    return
  }
  searchLoading.value = true
  try {
    const data = await adminApi.getProductList({ current: 1, size: 20, name: query })
    const list = (data as any)?.list || (data as any)?.records || []
    productOptions.value = list.filter((item: any) => item.isHelp !== 1)
  } catch {
    console.error('搜索商品失败')
  } finally {
    searchLoading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchProductList()
}

const handleSizeChange = (val: number) => {
  pagination.size = val
  fetchProductList()
}

const handleCurrentChange = (val: number) => {
  pagination.current = val
  fetchProductList()
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const handleAdd = async () => {
  if (!addForm.productId) {
    ElMessage.warning('请选择要添加的商品')
    return
  }
  submitLoading.value = true
  try {
    await adminApi.addPremiumProduct(addForm.productId)
    ElMessage.success('添加成功')
    showAddDialog.value = false
    addForm.productId = undefined
    fetchProductList()
  } catch (error: any) {
    ElMessage.error(error?.message || '添加失败')
  } finally {
    submitLoading.value = false
  }
}

const handleRemove = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要将该商品从助农优选列表中移除吗？', '提示', { type: 'warning' })
    await adminApi.removePremiumProduct(row.id)
    ElMessage.success('移除成功')
    fetchProductList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '操作失败')
    }
  }
}

const handleBatchRemove = async () => {
  try {
    await ElMessageBox.confirm(`确定要将选中的 ${selectedRows.value.length} 个商品从助农优选列表中移除吗？`, '批量移除', { type: 'warning' })
    for (const row of selectedRows.value) {
      await adminApi.removePremiumProduct(row.id)
    }
    ElMessage.success(`成功移除 ${selectedRows.value.length} 个商品`)
    fetchProductList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '批量移除失败')
    }
  }
}

onMounted(() => {
  fetchProductList()
})
</script>

<style scoped lang="scss">
.premium-product-page {
  .toolbar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .batch-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 16px;
    padding: 12px 16px;
    background: #f5f7fa;
    border-radius: 8px;

    span {
      font-size: 14px;
      color: #606266;
      font-weight: 500;
    }
  }
}
</style>
