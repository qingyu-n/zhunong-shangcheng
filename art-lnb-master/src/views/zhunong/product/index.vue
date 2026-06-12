<template>
  <div class="product-page">
    <ArtPageWrapper>
      <ArtCard title="商品管理">
        <div class="toolbar">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入商品名称"
            clearable
            style="width: 200px"
          />
          <el-select
            v-model="searchForm.categoryId"
            placeholder="商品分类"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          <el-select
            v-model="searchForm.status"
            placeholder="商品状态"
            clearable
            style="width: 120px"
          >
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
          <el-button type="primary" @click="handleSearch"
            ><i class="ri-search-line"></i> 搜索</el-button
          >
          <el-button type="primary" @click="handleAdd"
            ><i class="ri-add-line"></i> 新增商品</el-button
          >
        </div>

        <el-table :data="processedProductList" v-loading="loading">
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
          <el-table-column prop="categoryName" label="分类" width="120" />
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="80" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '上架' : '下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="700px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称">
              <el-input v-model="form.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品分类">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="item in categories"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品价格">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品库存">
              <el-input-number v-model="form.stock" :min="0" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品图片">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :http-request="handleCustomUpload"
            :before-upload="beforeUpload"
            accept="image/jpeg,image/png"
          >
            <img v-if="form.mainImage" :src="processedFormMainImage" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="商品状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted, computed } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { Plus } from '@element-plus/icons-vue'
  import { adminApi } from '@/api/admin'

  const loading = ref(false)
  const productList = ref([])
  const categories = ref<any[]>([])
  const dialogVisible = ref(false)
  const isEdit = ref(false)

  const searchForm = reactive({
    keyword: '',
    categoryId: undefined as number | undefined,
    status: undefined as number | undefined
  })

  const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
  })

  const form = reactive({
    id: undefined as number | undefined,
    name: '',
    categoryId: undefined as number | undefined,
    price: 0,
    stock: 0,
    mainImage: '',
    description: '',
    status: 1
  })

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

  const processedProductList = computed(() => {
    return productList.value.map((item: any) => ({
      ...item,
      mainImage: processImageUrl(item.mainImage)
    }))
  })

  const processedFormMainImage = computed(() => {
    return processImageUrl(form.mainImage)
  })

  const fetchProductList = async () => {
    loading.value = true
    try {
      const data = await adminApi.getProductList({
        current: pagination.current,
        size: pagination.size,
        keyword: searchForm.keyword || undefined,
        categoryId: searchForm.categoryId,
        status: searchForm.status
      })
      if (data) {
        const rawList = (data as any).list || (data as any).records || []
        productList.value = rawList.map((item: any) => ({
          ...item,
          categoryName: getCategoryName(item.categoryId)
        }))
        pagination.total = (data as any).total || 0
      }
    } catch {
      console.error('获取商品列表失败')
    } finally {
      loading.value = false
    }
  }

  const getCategoryName = (categoryId: number | string | undefined): string => {
    if (!categoryId || categories.value.length === 0) return '-'
    const category = categories.value.find((c: any) => c.id === Number(categoryId))
    return category ? category.name : '-'
  }

  const fetchCategories = async () => {
    try {
      const data = await adminApi.getCategoryList()
      if (data) {
        categories.value = Array.isArray(data) ? data : ((data as any)?.list || (data as any)?.records || [])
      }
    } catch {
      console.error('获取分类列表失败')
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

  const handleAdd = () => {
    form.id = undefined
    form.name = ''
    form.categoryId = undefined
    form.price = 0
    form.stock = 0
    form.mainImage = ''
    form.description = ''
    form.status = 1
    isEdit.value = false
    dialogVisible.value = true
  }

  const handleEdit = (row: any) => {
    Object.assign(form, row)
    isEdit.value = true
    dialogVisible.value = true
  }

  const handleDelete = async (row: any) => {
    try {
      await ElMessageBox.confirm('确定要删除该商品吗？', '提示', { type: 'warning' })
      await adminApi.deleteProduct(row.id)
      ElMessage.success('删除成功')
      fetchProductList()
    } catch (error: any) {
      if (error !== 'cancel' && error !== 'close') {
        console.error('删除商品失败:', error)
        ElMessage.error(error?.message || '删除失败')
      }
    }
  }

  const handleSubmit = async () => {
    if (!form.name || !form.categoryId) {
      ElMessage.warning('请填写完整信息')
      return
    }
    try {
      const submitData: any = {
        name: form.name,
        categoryId: form.categoryId,
        price: form.price,
        stock: form.stock,
        mainImage: form.mainImage || '',
        description: form.description || '',
        status: form.status ?? 1
      }
      let result
      if (isEdit.value && form.id) {
        result = await adminApi.updateProduct(form.id, submitData)
        ElMessage.success('编辑成功')
      } else {
        result = await adminApi.createProduct(submitData)
        ElMessage.success('新增成功')
      }
      if (result) {
        dialogVisible.value = false
        fetchProductList()
      }
    } catch (error: any) {
      console.error('提交失败:', error)
      ElMessage.error(error?.message || '操作失败')
    }
  }

  const handleCustomUpload = async (options: any) => {
    const file = options.file
    const isImage = file.type.startsWith('image/')
    const isLt5M = file.size / 1024 / 1024 < 5

    if (!isImage) {
      ElMessage.error('只支持图片文件!')
      options.onError(new Error('不支持此格式'))
      return
    }
    if (!isLt5M) {
      ElMessage.error('图片大小不能超过 5MB!')
      options.onError(new Error('文件过大'))
      return
    }

    try {
      const formData = new FormData()
      formData.append('file', file)

      const response = await fetch('/api/common/upload', {
        method: 'POST',
        body: formData
      })

      const result = await response.json()

      if (result.code === 200 && result.data?.url) {
        form.mainImage = result.data.url
        ElMessage.success('上传成功')
        options.onSuccess(result)
      } else {
        throw new Error(result.message || '上传失败')
      }
    } catch (error: any) {
      console.error('上传失败:', error)
      ElMessage.error(error.message || '上传失败')
      options.onError(error)
    }
  }

  const beforeUpload = (file: File) => {
    const isImage = file.type.startsWith('image/')
    const isLt5M = file.size / 1024 / 1024 < 5

    if (!isImage) {
      ElMessage.error('只支持图片文件!')
      return false
    }
    if (!isLt5M) {
      ElMessage.error('图片大小不能超过 5MB!')
      return false
    }
    return true
  }

  onMounted(async () => {
    await fetchCategories()
    fetchProductList()
  })
</script>

<style scoped lang="scss">
  .product-page {
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

    .avatar-uploader {
      :deep(.el-upload) {
        border: 1px dashed var(--el-border-color);
        border-radius: 6px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        transition: var(--el-transition-duration-fast);

        &:hover {
          border-color: var(--el-color-primary);
        }
      }
    }

    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 120px;
      height: 120px;
      text-align: center;
      line-height: 120px;
    }

    .avatar {
      width: 120px;
      height: 120px;
      display: block;
      object-fit: cover;
    }
  }
</style>
