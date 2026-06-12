<template>
  <el-dialog
    v-model="visible"
    title="举报商品"
    width="550px"
    :close-on-click-modal="false"
    destroy-on-close
    @close="handleClose"
  >
    <div class="report-content">
      <!-- 商品信息 -->
      <div class="product-info" v-if="productName">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>
            您正在举报商品：<strong>{{ productName }}</strong>
          </template>
        </el-alert>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="report-form">

        <!-- 举报原因类型 -->
        <el-form-item label="举报原因" prop="reasonType">
          <el-radio-group v-model="form.reasonType" @change="handleReasonChange">
            <el-radio :label="1">假冒伪劣</el-radio>
            <el-radio :label="2">价格欺诈</el-radio>
            <el-radio :label="3">虚假宣传</el-radio>
            <el-radio :label="4">其他原因</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 详细说明 -->
        <el-form-item label="详细说明" prop="reasonDetail">
          <el-input
            v-model="form.reasonDetail"
            type="textarea"
            :rows="5"
            placeholder="请详细描述您举报该商品的原因，提供具体证据和情况说明，以便我们快速处理"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 证据图片上传 -->
        <el-form-item label="证据图片">
          <el-upload
            action="#"
            :http-request="handleUploadImage"
            list-type="picture-card"
            :limit="5"
            :file-list="imageList"
            :on-remove="handleRemoveImage"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="upload-tip">最多上传5张图片作为证据（选填）</div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 提示信息 -->
        <el-form-item label="">
          <el-alert
            type="warning"
            :closable="false"
            show-icon
          >
            <template #title>
              <div class="alert-content">
                <p>1. 请确保您的举报内容真实有效，恶意举报将承担相应责任</p>
                <p>2. 我们将在24小时内处理您的举报，处理结果将通过消息通知您</p>
                <p>3. 如有疑问，请联系平台客服</p>
              </div>
            </template>
          </el-alert>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">提交举报</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { reportApi, qiniuApi } from '@/api'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  productId: {
    type: [Number, String],
    required: true
  },
  productName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const submitting = ref(false)
const formRef = ref()
const imageList = ref([])

const form = reactive({
  reasonType: null,
  reasonDetail: '',
  evidenceImages: []
})

const rules = {
  reasonType: [
    { required: true, message: '请选择举报原因', trigger: 'change' }
  ],
  reasonDetail: [
    { required: true, message: '请输入详细说明', trigger: 'blur' },
    { min: 10, max: 500, message: '说明内容在10-500个字符之间', trigger: 'blur' }
  ]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleReasonChange = () => {
  // 根据不同的举报原因类型,可以给出不同的提示或模板
}

const handleUploadImage = async (options) => {
  try {
    const file = options.file
    const res = await qiniuApi.getUploadToken()
    const token = res.token || res
    const domain = res.domain
    const formData = new FormData()
    formData.append('file', file)
    formData.append('token', token)
    const uploadRes = await fetch('https://upload.qiniup.com', { method: 'POST', body: formData })
    const data = await uploadRes.json()
    if (data.key && domain) {
      const url = `${domain}/${data.key}`
      form.evidenceImages.push(url)
      imageList.value.push({ name: file.name, url: url, uid: Date.now() + Math.random() })
    } else {
      const url = URL.createObjectURL(file)
      form.evidenceImages.push(url)
      imageList.value.push({ name: file.name, url: url, uid: Date.now() + Math.random() })
    }
  } catch (error) {
    const url = URL.createObjectURL(options.file)
    form.evidenceImages.push(url)
    imageList.value.push({ name: options.file.name, url: url, uid: Date.now() + Math.random() })
  }
}

const handleRemoveImage = (file) => {
  const index = form.evidenceImages.findIndex(img => img === file.url)
  if (index > -1) {
    form.evidenceImages.splice(index, 1)
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()

  submitting.value = true
  try {
    await reportApi.reportProduct(props.productId, {
      reasonType: form.reasonType,
      reasonDetail: form.reasonDetail,
      evidenceImages: form.evidenceImages.length > 0 ? JSON.stringify(form.evidenceImages) : ''
    })

    ElMessage.success('举报提交成功，我们将尽快处理')
    emit('success')
    handleClose()
  } catch (error) {
    ElMessage.error(error.message || '提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  visible.value = false
  formRef.value?.resetFields()
  Object.assign(form, {
    reasonType: null,
    reasonDetail: '',
    evidenceImages: []
  })
  imageList.value = []
}
</script>

<style scoped>
.report-content {
  padding: 0 20px;
}

.product-info {
  margin-bottom: 20px;
}

.report-form {
  margin-top: 20px;
}

.upload-tip {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
}

.alert-content p {
  margin: 4px 0;
  font-size: 13px;
  color: #e6a23c;
}
</style>
