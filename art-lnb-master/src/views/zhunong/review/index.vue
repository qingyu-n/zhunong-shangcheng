<template>
  <div class="review-page">
    <ArtPageWrapper>
      <ArtCard title="商品评价管理">
        <div class="toolbar">
          <el-input
            v-model="searchForm.keyword"
            placeholder="商品名称/用户"
            clearable
            style="width: 200px"
          />
          <el-select v-model="searchForm.rating" placeholder="评分" clearable style="width: 100px">
            <el-option label="5星" :value="5" />
            <el-option label="4星" :value="4" />
            <el-option label="3星" :value="3" />
            <el-option label="2星" :value="2" />
            <el-option label="1星" :value="1" />
          </el-select>
          <el-button type="primary" @click="handleSearch"
            ><i class="ri-search-line"></i> 搜索</el-button
          >
        </div>

        <el-table :data="reviewList" v-loading="loading">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="productName" label="商品名称" min-width="200" />
          <el-table-column prop="username" label="用户" width="120" />
          <el-table-column prop="rating" label="评分" width="100">
            <template #default="{ row }">
              <el-rate v-model="row.rating" disabled />
            </template>
          </el-table-column>
          <el-table-column prop="content" label="评价内容" min-width="250" show-overflow-tooltip />
          <el-table-column prop="createTime" label="评价时间" width="160" />
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
  const reviewList = ref<any[]>([])

  const searchForm = reactive({
    keyword: '',
    rating: undefined as number | undefined
  })

  const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
  })

  const fetchReviewList = async () => {
    loading.value = true
    try {
      const data = await adminApi.getReviewList({
        current: pagination.current,
        size: pagination.size,
        keyword: searchForm.keyword || undefined,
        rating: searchForm.rating
      })
      if (data) {
        reviewList.value = (data as any).list || (data as any).records || []
        pagination.total = (data as any).total || 0
      }
    } catch (error) {
      console.error('获取评价列表失败:', error)
      ElMessage.error('获取评价列表失败')
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    pagination.current = 1
    fetchReviewList()
  }

  const handleSizeChange = (val: number) => {
    pagination.size = val
    fetchReviewList()
  }

  const handleCurrentChange = (val: number) => {
    pagination.current = val
    fetchReviewList()
  }

  const handleDelete = async (row: any) => {
    try {
      await ElMessageBox.confirm('确定要删除该评价吗？', '提示', {
        type: 'warning'
      })
      await adminApi.deleteReview(row.id)
      ElMessage.success('删除成功')
      fetchReviewList()
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
        ElMessage.error(error.message || '删除失败')
      }
    }
  }

  onMounted(() => {
    fetchReviewList()
  })
</script>

<style scoped lang="scss">
  .review-page {
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
