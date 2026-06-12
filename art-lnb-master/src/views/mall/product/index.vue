<template>
  <div class="product-manage-page">
    <ElCard class="search-card">
      <ElForm :model="searchForm" inline>
        <ElFormItem label="商品名称">
          <ElInput v-model="searchForm.name" placeholder="请输入商品名称" clearable />
        </ElFormItem>
        <ElFormItem label="商品分类">
          <ElSelect v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
            <ElOption
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchForm.status" placeholder="请选择状态" clearable>
            <ElOption label="上架" :value="1" />
            <ElOption label="下架" :value="0" />
          </ElSelect>
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
        <h3 class="table-title">商品列表</h3>
        <ElButton type="primary" @click="handleAdd">
          <Icon name="ri:add-line" class="mr-1" />
          新增商品
        </ElButton>
      </div>

      <ElTable v-loading="loading" :data="productList" border stripe style="width: 100%">
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn label="商品图片" width="100" align="center">
          <template #default="{ row }">
            <img :src="row.mainImage" class="product-image" />
          </template>
        </ElTableColumn>
        <ElTableColumn prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
        <ElTableColumn prop="categoryName" label="分类" width="120" />
        <ElTableColumn prop="price" label="售价" width="100" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="originalPrice" label="原价" width="100" align="center">
          <template #default="{ row }">
            <span class="original-price">¥{{ row.originalPrice }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="stock" label="库存" width="100" align="center" />
        <ElTableColumn prop="sales" label="销量" width="100" align="center" />
        <ElTableColumn label="标签" width="120" align="center">
          <template #default="{ row }">
            <ElTag v-if="row.isHot === 1" type="danger" size="small" class="mr-1">热卖</ElTag>
            <ElTag v-if="row.isNew === 1" type="success" size="small">新品</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createTime" label="创建时间" min-width="160" />
        <ElTableColumn label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <ElButton type="primary" link @click="handleEdit(row)"> 编辑 </ElButton>
            <ElButton type="danger" link @click="handleDelete(row)"> 删除 </ElButton>
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

    <!-- 新增/编辑弹窗 -->
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElFormItem label="商品名称" prop="name">
              <ElInput v-model="formData.name" placeholder="请输入商品名称" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="商品分类" prop="categoryId">
              <ElSelect v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
                <ElOption
                  v-for="item in categoryList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElFormItem label="售价" prop="price">
              <ElInputNumber v-model="formData.price" :min="0" :precision="2" style="width: 100%" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="原价" prop="originalPrice">
              <ElInputNumber
                v-model="formData.originalPrice"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElFormItem label="库存" prop="stock">
              <ElInputNumber v-model="formData.stock" :min="0" :precision="0" style="width: 100%" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="排序" prop="sort">
              <ElInputNumber v-model="formData.sort" :min="0" :max="999" style="width: 100%" />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElFormItem label="商品主图" prop="mainImage">
          <div class="upload-wrapper">
            <ElUpload
              class="avatar-uploader"
              :action="uploadAction"
              :data="{ token: qiniuToken }"
              :show-file-list="false"
              :on-success="(res: any) => handleMainImageSuccess(res)"
              :before-upload="beforeUpload"
            >
              <img v-if="formData.mainImage" :src="formData.mainImage" class="avatar" />
              <ElIcon v-else class="avatar-uploader-icon"><Plus /></ElIcon>
            </ElUpload>
            <span class="upload-tip">点击上传主图，建议尺寸 400x400</span>
          </div>
        </ElFormItem>

        <ElFormItem label="商品详情图">
          <ElUpload
            :action="uploadAction"
            :data="{ token: qiniuToken }"
            list-type="picture-card"
            :file-list="detailImageList"
            :on-success="handleDetailImageSuccess"
            :on-remove="handleDetailImageRemove"
            :before-upload="beforeUpload"
          >
            <ElIcon><Plus /></ElIcon>
          </ElUpload>
        </ElFormItem>

        <ElFormItem label="商品描述" prop="description">
          <ElInput
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </ElFormItem>

        <ElRow :gutter="20">
          <ElCol :span="8">
            <ElFormItem label="是否上架" prop="status">
              <ElSwitch v-model="formData.status" :active-value="1" :inactive-value="0" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="8">
            <ElFormItem label="是否热卖" prop="isHot">
              <ElSwitch v-model="formData.isHot" :active-value="1" :inactive-value="0" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="8">
            <ElFormItem label="是否新品" prop="isNew">
              <ElSwitch v-model="formData.isNew" :active-value="1" :inactive-value="0" />
            </ElFormItem>
          </ElCol>
        </ElRow>
      </ElForm>

      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit"> 确定 </ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { Plus } from '@element-plus/icons-vue'
  import type { UploadFile } from 'element-plus'
  import {
    fetchGetProductList,
    fetchCreateProduct,
    fetchUpdateProduct,
    fetchDeleteProduct,
    fetchGetCategoryList,
    fetchGetQiniuToken
  } from '@/api/mall'

  defineOptions({ name: 'ProductManage' })

  // 搜索表单
  const searchForm = reactive({
    name: '',
    categoryId: undefined as number | undefined,
    status: undefined as number | undefined
  })

  // 分页
  const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
  })

  // 表格数据
  const loading = ref(false)
  const productList = ref<Api.Product.ProductItem[]>([])
  const categoryList = ref<Api.Category.CategoryItem[]>([])

  // 弹窗
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const dialogTitle = computed(() => (dialogType.value === 'add' ? '新增商品' : '编辑商品'))
  const submitLoading = ref(false)

  // 表单
  const formRef = ref()
  const formData = reactive<Api.Product.ProductForm>({
    name: '',
    categoryId: undefined as number | undefined,
    price: 0,
    originalPrice: 0,
    stock: 0,
    mainImage: '',
    images: [],
    description: '',
    status: 1,
    isHot: 0,
    isNew: 0,
    sort: 0
  })

  const formRules = {
    name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
    categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
    price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
    stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
    mainImage: [{ required: true, message: '请上传商品主图', trigger: 'change' }]
  }

  // 七牛云上传配置
  const uploadAction = ref('https://upload.qiniup.com')
  const qiniuToken = ref('')
  const qiniuDomain = ref('')
  const detailImageList = ref<UploadFile[]>([])

  // 获取七牛云token
  const getQiniuToken = async () => {
    try {
      const res = await fetchGetQiniuToken()
      qiniuToken.value = res.token
      qiniuDomain.value = res.domain
    } catch (error) {
      console.error('获取七牛云token失败:', error)
    }
  }

  // 获取分类列表
  const getCategoryList = async () => {
    try {
      const res = await fetchGetCategoryList()
      categoryList.value = res.filter((item) => item.status === 1)
    } catch (error) {
      console.error('获取分类列表失败:', error)
    }
  }

  // 获取商品列表
  const getProductList = async () => {
    loading.value = true
    try {
      const res = await fetchGetProductList({
        current: pagination.current,
        size: pagination.size,
        name: searchForm.name || undefined,
        categoryId: searchForm.categoryId,
        status: searchForm.status
      })
      productList.value = res.records
      pagination.total = res.total
    } catch (error) {
      console.error('获取商品列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 搜索
  const handleSearch = () => {
    pagination.current = 1
    getProductList()
  }

  // 重置
  const handleReset = () => {
    searchForm.name = ''
    searchForm.categoryId = undefined
    searchForm.status = undefined
    pagination.current = 1
    getProductList()
  }

  // 新增
  const handleAdd = () => {
    dialogType.value = 'add'
    resetForm()
    dialogVisible.value = true
  }

  // 编辑
  const handleEdit = (row: Api.Product.ProductItem) => {
    dialogType.value = 'edit'
    resetForm()
    formData.id = row.id
    formData.name = row.name
    formData.categoryId = row.categoryId
    formData.price = row.price
    formData.originalPrice = row.originalPrice
    formData.stock = row.stock
    formData.mainImage = row.mainImage
    formData.images = row.images || []
    formData.description = row.description
    formData.status = row.status
    formData.isHot = row.isHot
    formData.isNew = row.isNew
    formData.sort = row.sort

    // 设置详情图列表
    detailImageList.value = (row.images || []).map((url, index) => ({
      name: `image-${index}`,
      url
    })) as UploadFile[]

    dialogVisible.value = true
  }

  // 删除
  const handleDelete = async (row: Api.Product.ProductItem) => {
    try {
      await ElMessageBox.confirm('确定要删除该商品吗？删除后无法恢复！', '提示', {
        type: 'warning'
      })
      await fetchDeleteProduct(row.id)
      ElMessage.success('删除成功')
      getProductList()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  // 重置表单
  const resetForm = () => {
    formData.id = undefined
    formData.name = ''
    formData.categoryId = undefined
    formData.price = 0
    formData.originalPrice = 0
    formData.stock = 0
    formData.mainImage = ''
    formData.images = []
    formData.description = ''
    formData.status = 1
    formData.isHot = 0
    formData.isNew = 0
    formData.sort = 0
    detailImageList.value = []
  }

  // 上传前处理
  const beforeUpload = async (file: File) => {
    const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
    if (!isJpgOrPng) {
      ElMessage.error('只支持 JPG/PNG 格式的图片！')
      return false
    }
    const isLt5M = file.size / 1024 / 1024 < 5
    if (!isLt5M) {
      ElMessage.error('图片大小不能超过 5MB！')
      return false
    }

    if (!qiniuToken.value) {
      await getQiniuToken()
    }
    return true
  }

  // 主图上传成功
  const handleMainImageSuccess = (res: any) => {
    if (res.key) {
      formData.mainImage = `${qiniuDomain.value}/${res.key}`
      ElMessage.success('上传成功')
    } else {
      ElMessage.error('上传失败')
    }
  }

  // 详情图上传成功
  const handleDetailImageSuccess = (res: any) => {
    if (res.key) {
      const url = `${qiniuDomain.value}/${res.key}`
      formData.images.push(url)
      ElMessage.success('上传成功')
    }
  }

  // 详情图删除
  const handleDetailImageRemove = (file: UploadFile) => {
    const index = formData.images.findIndex((url) => url === file.url)
    if (index > -1) {
      formData.images.splice(index, 1)
    }
  }

  // 提交
  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()
      submitLoading.value = true

      if (dialogType.value === 'add') {
        await fetchCreateProduct(formData)
        ElMessage.success('新增成功')
      } else {
        await fetchUpdateProduct(formData)
        ElMessage.success('更新成功')
      }

      dialogVisible.value = false
      getProductList()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitLoading.value = false
    }
  }

  // 分页变化
  const handleSizeChange = (val: number) => {
    pagination.size = val
    getProductList()
  }

  const handleCurrentChange = (val: number) => {
    pagination.current = val
    getProductList()
  }

  onMounted(() => {
    getProductList()
    getCategoryList()
    getQiniuToken()
  })
</script>

<style scoped>
  .product-manage-page {
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

  .product-image {
    width: 60px;
    height: 60px;
    object-fit: cover;
    border-radius: 4px;
  }

  .price {
    color: #f56c6c;
    font-weight: bold;
  }

  .original-price {
    color: #909399;
    text-decoration: line-through;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .upload-wrapper {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .avatar-uploader :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
  }

  .avatar-uploader :deep(.el-upload:hover) {
    border-color: var(--el-color-primary);
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

  .upload-tip {
    font-size: 12px;
    color: #909399;
  }

  .mr-1 {
    margin-right: 4px;
  }

  .mt-4 {
    margin-top: 16px;
  }
</style>
