<template>
  <div class="user-favorite-page">
    <ArtPageWrapper>
      <ArtCard title="用户收藏管理">
        <div class="toolbar">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
          />
          <el-button type="primary" @click="handleSearch"
            ><i class="ri-search-line"></i> 搜索</el-button
          >
        </div>

        <el-table :data="favoriteList" v-loading="loading">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="username" label="用户" width="120" />
          <el-table-column
            prop="productName"
            label="商品名称"
            min-width="200"
            show-overflow-tooltip
          />
          <el-table-column prop="productImage" label="商品图片" width="100">
            <template #default="{ row }">
              <el-image
                :src="row.productImage"
                style="width: 60px; height: 60px; border-radius: 4px"
                fit="cover"
              />
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="收藏时间" width="160" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { adminApi } from '@/api/admin'

  const loading = ref(false)
  const favoriteList = ref<any[]>([])

  const searchForm = reactive({
    username: ''
  })

  const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
  })

  const fetchFavoriteList = async () => {
    loading.value = true
    try {
      const data = await adminApi.getFavoriteList({
        current: pagination.current,
        size: pagination.size,
        username: searchForm.username || undefined
      })
      if (data) {
        favoriteList.value = (data as any).records || []
        pagination.total = (data as any).total || 0
      }
    } catch (error) {
      console.error('获取收藏列表失败:', error)
      ElMessage.error('获取收藏列表失败')
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    pagination.current = 1
    fetchFavoriteList()
  }

  const handleSizeChange = (val: number) => {
    pagination.size = val
    fetchFavoriteList()
  }

  const handleCurrentChange = (val: number) => {
    pagination.current = val
    fetchFavoriteList()
  }

  const handleDelete = async (row: any) => {
    try {
      await ElMessageBox.confirm('确定要删除该收藏吗？', '提示', { type: 'warning' })
      await adminApi.deleteFavorite(row.id)
      ElMessage.success('删除成功')
      fetchFavoriteList()
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
        ElMessage.error(error.message || '删除失败')
      }
    }
  }

  onMounted(() => {
    fetchFavoriteList()
  })
</script>

<style scoped lang="scss">
  .user-favorite-page {
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
  }
</style>
