<template>
  <div class="product-detail-page">
    <ArtPageWrapper>
      <ArtCard title="商品详情页管理">
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
          <el-button type="danger" @click="handleAdd">
            <i class="ri-add-circle-line"></i> 新增详情
          </el-button>
        </div>

        <el-table :data="detailList" v-loading="loading" stripe>
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <div class="product-cell">
                <el-image
                  :src="getProductImage(row)"
                  style="width: 50px; height: 50px; border-radius: 6px; flex-shrink: 0"
                  fit="cover"
                  :preview-src-list="[getProductImage(row)]"
                  :hide-on-click-modal="true"
                >
                  <template #error>
                    <div class="image-error">
                      <i class="ri-image-line"></i>
                    </div>
                  </template>
                </el-image>
                <span class="product-name">{{ getProductName(row) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="助农故事" min-width="200">
            <template #default="{ row }">
              <span class="story-preview">{{ row.farmerStory ? row.farmerStory.substring(0, 50) + '...' : '暂无' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="图片数量" width="100" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ getImageCount(row) }}张</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light" round>
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEdit(row)">
                <i class="ri-edit-line"></i> 编辑
              </el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">
                <i class="ri-delete-bin-line"></i> 删除
              </el-button>
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

    <el-dialog
      v-model="dialogVisible"
      width="900px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-title">
          {{ isEdit ? '编辑商品详情' : '新增商品详情' }}
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="detail-form"
      >
        <!-- 基本信息 -->
        <div class="form-section-title">
          <i class="ri-information-line"></i> 基本信息
        </div>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="关联商品" prop="productId">
              <el-select
                id="productId"
                v-model="form.productId"
                filterable
                remote
                reserve-keyword
                placeholder="输入关键词搜索商品..."
                :remote-method="searchProducts"
                :loading="searchLoading"
                style="width: 100%"
                :disabled="isEdit"
                @change="onProductSelect"
              >
                <el-option
                  v-for="item in productOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                  <div style="display: flex; justify-content: space-between; align-items: center">
                    <span>{{ item.name }}</span>
                    <el-tag size="small" type="info">¥{{ item.price }}</el-tag>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group id="status" v-model="form.status">
                <el-radio-button :label="1">启用</el-radio-button>
                <el-radio-button :label="0">禁用</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 助农故事 -->
        <div class="form-section-title">
          <i class="ri-heart-line"></i> 助农故事
        </div>
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="故事内容">
              <el-input
                id="farmerStory"
                v-model="form.farmerStory"
                type="textarea"
                :rows="4"
                placeholder="输入助农故事内容..."
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="故事配图">
              <el-upload
                class="image-uploader"
                action="#"
                :http-request="(options) => handleUpload(options, 'farmerImage')"
                :show-file-list="false"
                accept="image/*"
              >
                <img v-if="form.farmerImage" :src="form.farmerImage" class="uploaded-image">
                <div v-else class="upload-placeholder">
                  <i class="ri-image-add-line"></i>
                  <span>点击上传</span>
                </div>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 产品图片 -->
        <div class="form-section-title">
          <i class="ri-image-line"></i> 产品图片（最多5张）
        </div>
        <el-row :gutter="16">
          <el-col :span="4" v-for="(img, index) in imageFields" :key="index">
            <el-form-item :label="`图片${index + 1}`">
              <el-upload
                class="image-uploader small"
                action="#"
                :http-request="(options) => handleUpload(options, img.field)"
                :show-file-list="false"
                accept="image/*"
              >
                <img v-if="form[img.field]" :src="form[img.field]" class="uploaded-image">
                <div v-else class="upload-placeholder small">
                  <i class="ri-add-line"></i>
                </div>
              </el-upload>
              <el-button
                v-if="form[img.field]"
                type="danger"
                link
                size="small"
                @click="clearImage(img.field)"
                style="margin-top: 4px"
              >
                删除
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 商品参数 -->
        <div class="form-section-title">
          <i class="ri-list-settings-line"></i> 商品参数
        </div>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="商品名称">
              <el-input id="paramName" v-model="form.paramName" placeholder="如：云南高山有机生态苹果" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产地">
              <el-input id="paramOrigin" v-model="form.paramOrigin" placeholder="如：云南昭通" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="保质期">
              <el-input id="paramShelfLife" v-model="form.paramShelfLife" placeholder="如：15天" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="储存方式">
              <el-input id="paramStorage" v-model="form.paramStorage" placeholder="如：阴凉干燥处存放，冷藏更佳" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="发货时间">
              <el-input id="paramDelivery" v-model="form.paramDelivery" placeholder="如：下单后48小时内" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '创建详情' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const searchLoading = ref(false)
const submitLoading = ref(false)
const detailList = ref<any[]>([])
const productMap = ref<Map<number, any>>(new Map())
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const productOptions = ref<any[]>([])

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive<Record<string, any>>({
  id: undefined as number | undefined,
  productId: undefined as number | undefined,
  description: '',
  farmerStory: '',
  farmerImage: '',
  image1: '',
  image2: '',
  image3: '',
  image4: '',
  image5: '',
  paramName: '',
  paramOrigin: '',
  paramShelfLife: '',
  paramStorage: '',
  paramDelivery: '',
  status: 1
})

const imageFields = [
  { field: 'image1' },
  { field: 'image2' },
  { field: 'image3' },
  { field: 'image4' },
  { field: 'image5' }
]

const rules: FormRules = {
  productId: [{ required: true, message: '请选择关联商品', trigger: 'change' }]
}

const fetchDetailList = async () => {
  loading.value = true
  try {
    const data = await adminApi.getProductDetailList({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined
    })
    if (data) {
      detailList.value = (data as any).list || (data as any).records || []
      pagination.total = (data as any).total || 0
      
      // 获取关联商品信息
      const productIds = detailList.value.map(item => item.productId).filter(Boolean)
      if (productIds.length > 0) {
        await fetchProductInfo(productIds)
      }
    }
  } catch {
    console.error('获取商品详情列表失败')
  } finally {
    loading.value = false
  }
}

const fetchProductInfo = async (productIds: number[]) => {
  for (const id of productIds) {
    if (!productMap.value.has(id)) {
      try {
        const product = await adminApi.getProductById(id)
        if (product) {
          productMap.value.set(id, product)
        }
      } catch (e) {
        console.error(`获取商品${id}信息失败`, e)
      }
    }
  }
}

const getProductName = (row: any) => {
  const product = productMap.value.get(row.productId)
  return product?.name || '未知商品'
}

const getProductImage = (row: any) => {
  const product = productMap.value.get(row.productId)
  // 优先使用详情表中的图片，其次使用商品主图
  if (row.image1) return row.image1
  if (row.farmerImage) return row.farmerImage
  return product?.mainImage || ''
}

const getImageCount = (row: any) => {
  let count = 0
  for (let i = 1; i <= 5; i++) {
    if (row[`image${i}`]) count++
  }
  return count
}

const searchProducts = async (query: string) => {
  if (!query || query.length < 2) {
    productOptions.value = []
    return
  }
  searchLoading.value = true
  try {
    const data = await adminApi.getProductList({ current: 1, size: 20, name: query })
    productOptions.value = (data as any)?.list || (data as any)?.records || []
  } catch {
    console.error('搜索商品失败')
  } finally {
    searchLoading.value = false
  }
}

const onProductSelect = (val: number | undefined) => {
  if (!val) return
  const item = productOptions.value.find((p: any) => p.id === val)
  if (item && !form.paramName) {
    form.paramName = item.name
  }
}

const handleUpload = async (options: any, field: string) => {
  try {
    const file = options.file
    const formData = new FormData()
    formData.append('file', file)
    
    const res: any = await adminApi.uploadImage(formData)
    if (res && res.url) {
      (form as any)[field] = res.url
      ElMessage.success('上传成功')
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '上传失败')
  }
}

const clearImage = (field: string) => {
  (form as any)[field] = ''
}

const handleSearch = () => {
  pagination.current = 1
  fetchDetailList()
}

const handleSizeChange = (val: number) => {
  pagination.size = val
  fetchDetailList()
}

const handleCurrentChange = (val: number) => {
  pagination.current = val
  fetchDetailList()
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  form.id = row.id
  form.productId = row.productId
  form.description = row.description || ''
  form.farmerStory = row.farmerStory || ''
  form.farmerImage = row.farmerImage || ''
  form.image1 = row.image1 || ''
  form.image2 = row.image2 || ''
  form.image3 = row.image3 || ''
  form.image4 = row.image4 || ''
  form.image5 = row.image5 || ''
  form.paramName = row.paramName || ''
  form.paramOrigin = row.paramOrigin || ''
  form.paramShelfLife = row.paramShelfLife || ''
  form.paramStorage = row.paramStorage || ''
  form.paramDelivery = row.paramDelivery || ''
  form.status = row.status ?? 1
  isEdit.value = true
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品详情吗？', '确认删除', { type: 'warning' })
    await adminApi.deleteProductDetail(row.id)
    ElMessage.success('删除成功')
    fetchDetailList()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error?.message || '删除失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const submitData: any = {
        productId: form.productId,
        description: form.description,
        farmerStory: form.farmerStory,
        farmerImage: form.farmerImage,
        image1: form.image1,
        image2: form.image2,
        image3: form.image3,
        image4: form.image4,
        image5: form.image5,
        paramName: form.paramName,
        paramOrigin: form.paramOrigin,
        paramShelfLife: form.paramShelfLife,
        paramStorage: form.paramStorage,
        paramDelivery: form.paramDelivery,
        status: form.status
      }

      if (isEdit.value && form.id) {
        await adminApi.updateProductDetail(form.id, submitData)
        ElMessage.success('修改成功')
      } else {
        await adminApi.createProductDetail(submitData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchDetailList()
    } catch (error: any) {
      ElMessage.error(error?.message || '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const resetForm = () => {
  form.id = undefined
  form.productId = undefined
  form.description = ''
  form.farmerStory = ''
  form.farmerImage = ''
  form.image1 = ''
  form.image2 = ''
  form.image3 = ''
  form.image4 = ''
  form.image5 = ''
  form.paramName = ''
  form.paramOrigin = ''
  form.paramShelfLife = ''
  form.paramStorage = ''
  form.paramDelivery = ''
  form.status = 1
  formRef.value?.resetFields()
}

onMounted(() => {
  fetchDetailList()
})
</script>

<style scoped lang="scss">
.product-detail-page {
  .toolbar {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    flex-wrap: wrap;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .product-cell {
    display: flex;
    align-items: center;
    gap: 10px;

    .product-name {
      font-weight: 500;
      color: #303133;
    }

    .image-error {
      width: 50px;
      height: 50px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f7fa;
      border-radius: 6px;
      color: #c0c4cc;
      font-size: 20px;
    }
  }

  .story-preview {
    font-size: 13px;
    color: #606266;
  }

  .dialog-title {
    text-align: center;
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }

  .detail-form {
    .form-section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      margin: 24px 0 16px;
      padding-bottom: 8px;
      border-bottom: 2px solid #e4e7ed;

      &:first-child {
        margin-top: 0;
      }

      i {
        color: #409eff;
        font-size: 18px;
      }
    }

    .image-uploader {
      :deep(.el-upload) {
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        transition: border-color 0.3s;
        width: 178px;
        height: 178px;

        &:hover {
          border-color: #409eff;
        }
      }

      &.small :deep(.el-upload) {
        width: 100px;
        height: 100px;
      }

      .uploaded-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .upload-placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
        color: #8c939d;

        i {
          font-size: 28px;
          margin-bottom: 8px;
        }

        span {
          font-size: 12px;
        }

        &.small {
          i {
            font-size: 20px;
            margin-bottom: 4px;
          }
        }
      }
    }

    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
  }
}
</style>
