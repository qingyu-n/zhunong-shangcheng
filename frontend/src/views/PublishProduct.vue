<template>
  <div class="publish-page">
    <el-card class="publish-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h2>{{ isEdit ? '编辑商品' : '发布商品' }}</h2>
          <p class="subtitle">{{ isEdit ? '修改后将重新进入审核流程' : '发布后需管理员审核通过才能上架' }}</p>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="publish-form">
        
        <el-card class="section-card" shadow="never">
          <template #header><span>基本信息</span></template>
          
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="100" show-word-limit />
          </el-form-item>

          <el-form-item label="商品分类" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="售价" prop="price">
                <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="原价">
                <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="库存" prop="stock">
                <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="计量单位" prop="unit">
                <el-select v-model="form.unit" placeholder="请选择" style="width: 100%">
                  <el-option label="斤" value="斤" />
                  <el-option label="公斤" value="公斤" />
                  <el-option label="个" value="个" />
                  <el-option label="箱" value="箱" />
                  <el-option label="袋" value="袋" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="重量(kg)">
                <el-input-number v-model="form.weight" :min="0" :precision="1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="产地">
                <el-input v-model="form.origin" placeholder="如：山东寿光" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <el-card class="section-card" shadow="never" style="margin-top: 16px;">
          <template #header><span>商品图片</span></template>
          
          <el-form-item label="主图" prop="mainImage">
            <el-upload
              action="#"
              :http-request="handleUploadMainImage"
              :show-file-list="false"
              accept="image/*"
              list-type="picture-card"
            >
              <img v-if="form.mainImage" :src="form.mainImage" class="uploaded-image" />
              <el-icon v-else><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">主图建议尺寸800x800像素，第一张为主图</div>
          </el-form-item>

          <el-form-item label="详情图">
            <el-upload
              action="#"
              :http-request="handleUploadImages"
              :file-list="imageList"
              :on-remove="handleRemoveImage"
              accept="image/*"
              list-type="picture-card"
              :limit="6"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">最多上传6张图片，单张不超过5MB</div>
          </el-form-item>
        </el-card>

        <el-card class="section-card" shadow="never" style="margin-top: 16px;">
          <template #header><span>商品描述</span></template>
          
          <el-form-item label="简短描述">
            <el-input v-model="form.description" placeholder="一句话介绍商品特点" maxlength="100" show-word-limit />
          </el-form-item>

          <el-form-item label="详细介绍">
            <el-input v-model="form.detail" type="textarea" :rows="6" placeholder="详细描述商品的产地、品质、食用方法等..." />
          </el-form-item>
        </el-card>

        <div class="action-buttons">
          <el-button @click="handleSaveDraft">保存草稿</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ isEdit ? '保存修改' : '提交审核' }}</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { farmerApi, qiniuApi, categoryApi } from '@/api'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const submitting = ref(false)
const isEdit = ref(false)
const categories = ref([])
const imageList = ref([])

const form = reactive({
  name: '',
  categoryId: null,
  price: null,
  originalPrice: null,
  stock: 0,
  unit: '斤',
  weight: null,
  origin: '',
  description: '',
  detail: '',
  mainImage: '',
  images: []
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const uploadImage = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  const res = await request.post('/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return res.url
}

const handleUploadMainImage = async (options) => {
  try {
    const url = await uploadImage(options.file)
    form.mainImage = url
    ElMessage.success('主图上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const handleUploadImages = async (options) => {
  try {
    const url = await uploadImage(options.file)
    form.images.push(url)
    imageList.value.push({ name: options.file.name, url })
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const handleRemoveImage = (file) => {
  const index = imageList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    imageList.value.splice(index, 1)
    form.images.splice(index, 1)
  }
}

const handleSaveDraft = () => {
  ElMessage.info('草稿保存功能开发中...')
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true

  try {
    const submitData = {
      ...form,
      images: form.images && form.images.length > 0 ? JSON.stringify(form.images) : null
    }
    if (isEdit.value) {
      await farmerApi.updateProduct(route.params.id, submitData)
      ElMessage.success('修改成功，将重新进入审核')
    } else {
      await farmerApi.publishProduct(submitData)
      ElMessage.success('商品发布成功，等待审核')
    }
    router.push('/my-products')
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const catRes = await categoryApi.getCategories()
    categories.value = Array.isArray(catRes) ? catRes : []
  } catch (e) {
    // ignore
  }

  if (route.params.id) {
    isEdit.value = true
    try {
      const productRes = await farmerApi.getMyProductDetail(route.params.id)
      if (productRes) {
        Object.assign(form, productRes)
      }
    } catch (e) {
      // ignore
    }
  }
})
</script>

<style scoped lang="scss">
.publish-page {
  max-width: 900px;
  margin: 20px auto;

  .card-header {
    h2 {
      margin: 0;
      font-size: 20px;
      color: #333;
    }

    .subtitle {
      margin: 8px 0 0;
      color: #999;
      font-size: 14px;
    }
  }

  .section-card {
    border-radius: 8px;

    :deep(.el-card__header) {
      background-color: #fafafa;
      font-weight: bold;
    }
  }

  .upload-tip {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
  }

  .uploaded-image {
    width: 148px;
    height: 148px;
    object-fit: cover;
  }

  .action-buttons {
    text-align: center;
    padding: 30px 0;

    .el-button {
      min-width: 120px;
      margin: 0 10px;
    }
  }
}
</style>
