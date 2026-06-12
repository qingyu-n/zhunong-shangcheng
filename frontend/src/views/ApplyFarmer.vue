<template>
  <div class="apply-farmer-page">
    <el-card class="apply-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h2>申请成为认证农户</h2>
          <p class="subtitle">填写以下信息，提交后等待管理员审核</p>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="apply-form">
        
        <el-divider content-position="left">店铺信息</el-divider>
        
        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="form.shopName" placeholder="请输入店铺名称，2-50个字符" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="店铺简介" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请简要描述您的农产品特色..." :rows="3" maxlength="200" show-word-limit />
        </el-form-item>

        <el-divider content-position="left">个人信息</el-divider>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入18位身份证号" maxlength="18" />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入11位手机号" maxlength="11" />
        </el-form-item>

        <el-form-item label="联系地址" prop="region">
          <el-cascader
            v-model="addressArray"
            :options="regionOptions"
            placeholder="请选择省/市/区"
            style="width: 100%"
            @change="handleRegionChange"
            filterable
          />
        </el-form-item>

        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="form.detailAddress" placeholder="请输入详细地址（街道/门牌号）" />
        </el-form-item>

        <el-divider content-position="left">资质证明</el-divider>

        <el-form-item label="营业执照" prop="businessLicense">
          <el-upload
            action="#"
            :http-request="handleUploadLicense"
            :show-file-list="true"
            :file-list="licenseFileList"
            accept="image/*"
            list-type="picture-card"
            :limit="1"
            :on-preview="handlePreview"
            :on-remove="handleRemoveLicense"
            :before-upload="beforeUpload"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="upload-tip">支持jpg/png格式，大小不超过5MB</div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 图片预览对话框 -->
        <el-dialog v-model="previewVisible" title="图片预览" width="600px">
          <img :src="previewImageUrl" alt="预览图片" style="width: 100%" />
        </el-dialog>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
            提交申请
          </el-button>
          <el-button @click="$router.back()" size="large">取消</el-button>
        </el-form-item>
      </el-form>

      <div v-if="currentStatus !== null && currentStatus === 0" class="status-info warning">
        <el-alert title="您的申请正在审核中，请耐心等待..." type="warning" show-icon :closable="false" />
      </div>
      <div v-if="currentStatus === 2" class="status-info error">
        <el-alert :title="'申请被拒绝' + (rejectReason ? '：' + rejectReason : '')" type="error" show-icon :closable="false" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { farmerApi, qiniuApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { processImageUrl } from '@/utils/image'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const submitting = ref(false)
const currentStatus = ref(null)
const rejectReason = ref('')
const addressArray = ref([])
const licenseFileList = ref([])
const previewVisible = ref(false)
const previewImageUrl = ref('')

const form = reactive({
  shopName: '',
  description: '',
  realName: '',
  idCard: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  businessLicense: ''
})

const rules = {
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入店铺简介', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择联系地址', trigger: 'change', validator: (rule, value, callback) => {
      if (!addressArray.value || addressArray.value.length === 0) {
        callback(new Error('请选择省/市/区'))
      } else {
        callback()
      }
    }}
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 省市区数据（转换为el-cascader格式）
const regionOptions = [
  {
    value: '北京市',
    label: '北京市',
    children: [
      {
        value: '北京市',
        label: '北京市',
        children: [
          { value: '东城区', label: '东城区' },
          { value: '西城区', label: '西城区' },
          { value: '朝阳区', label: '朝阳区' },
          { value: '海淀区', label: '海淀区' },
          { value: '丰台区', label: '丰台区' },
          { value: '石景山区', label: '石景山区' },
          { value: '门头沟区', label: '门头沟区' },
          { value: '房山区', label: '房山区' }
        ]
      }
    ]
  },
  {
    value: '上海市',
    label: '上海市',
    children: [
      {
        value: '上海市',
        label: '上海市',
        children: [
          { value: '黄浦区', label: '黄浦区' },
          { value: '徐汇区', label: '徐汇区' },
          { value: '浦东新区', label: '浦东新区' },
          { value: '静安区', label: '静安区' },
          { value: '闵行区', label: '闵行区' },
          { value: '宝山区', label: '宝山区' },
          { value: '嘉定区', label: '嘉定区' }
        ]
      }
    ]
  },
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '天河区', label: '天河区' },
          { value: '越秀区', label: '越秀区' },
          { value: '海珠区', label: '海珠区' },
          { value: '番禺区', label: '番禺区' },
          { value: '白云区', label: '白云区' }
        ]
      },
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '南山区', label: '南山区' },
          { value: '福田区', label: '福田区' },
          { value: '罗湖区', label: '罗湖区' },
          { value: '宝安区', label: '宝安区' },
          { value: '龙岗区', label: '龙岗区' }
        ]
      },
      {
        value: '东莞市',
        label: '东莞市',
        children: [
          { value: '莞城区', label: '莞城区' },
          { value: '南城区', label: '南城区' },
          { value: '东城区', label: '东城区' }
        ]
      },
      {
        value: '佛山市',
        label: '佛山市',
        children: [
          { value: '禅城区', label: '禅城区' },
          { value: '南海区', label: '南海区' },
          { value: '顺德区', label: '顺德区' }
        ]
      }
    ]
  },
  {
    value: '浙江省',
    label: '浙江省',
    children: [
      {
        value: '杭州市',
        label: '杭州市',
        children: [
          { value: '西湖区', label: '西湖区' },
          { value: '上城区', label: '上城区' },
          { value: '余杭区', label: '余杭区' },
          { value: '萧山区', label: '萧山区' }
        ]
      },
      {
        value: '宁波市',
        label: '宁波市',
        children: [
          { value: '海曙区', label: '海曙区' },
          { value: '江北区', label: '江北区' },
          { value: '鄞州区', label: '鄞州区' }
        ]
      },
      {
        value: '温州市',
        label: '温州市',
        children: [
          { value: '鹿城区', label: '鹿城区' },
          { value: '龙湾区', label: '龙湾区' },
          { value: '瓯海区', label: '瓯海区' }
        ]
      }
    ]
  },
  {
    value: '江苏省',
    label: '江苏省',
    children: [
      {
        value: '南京市',
        label: '南京市',
        children: [
          { value: '玄武区', label: '玄武区' },
          { value: '秦淮区', label: '秦淮区' },
          { value: '鼓楼区', label: '鼓楼区' },
          { value: '建邺区', label: '建邺区' }
        ]
      },
      {
        value: '苏州市',
        label: '苏州市',
        children: [
          { value: '姑苏区', label: '姑苏区' },
          { value: '虎丘区', label: '虎丘区' },
          { value: '吴中区', label: '吴中区' }
        ]
      },
      {
        value: '无锡市',
        label: '无锡市',
        children: [
          { value: '梁溪区', label: '梁溪区' },
          { value: '滨湖区', label: '滨湖区' },
          { value: '新吴区', label: '新吴区' }
        ]
      }
    ]
  },
  {
    value: '四川省',
    label: '四川省',
    children: [
      {
        value: '成都市',
        label: '成都市',
        children: [
          { value: '锦江区', label: '锦江区' },
          { value: '青羊区', label: '青羊区' },
          { value: '武侯区', label: '武侯区' },
          { value: '高新区', label: '高新区' }
        ]
      },
      {
        value: '绵阳市',
        label: '绵阳市',
        children: [
          { value: '涪城区', label: '涪城区' },
          { value: '游仙区', label: '游仙区' }
        ]
      }
    ]
  },
  {
    value: '湖北省',
    label: '湖北省',
    children: [
      {
        value: '武汉市',
        label: '武汉市',
        children: [
          { value: '江岸区', label: '江岸区' },
          { value: '江汉区', label: '江汉区' },
          { value: '武昌区', label: '武昌区' },
          { value: '洪山区', label: '洪山区' }
        ]
      }
    ]
  },
  {
    value: '湖南省',
    label: '湖南省',
    children: [
      {
        value: '长沙市',
        label: '长沙市',
        children: [
          { value: '芙蓉区', label: '芙蓉区' },
          { value: '天心区', label: '天心区' },
          { value: '岳麓区', label: '岳麓区' }
        ]
      }
    ]
  },
  {
    value: '河南省',
    label: '河南省',
    children: [
      {
        value: '郑州市',
        label: '郑州市',
        children: [
          { value: '中原区', label: '中原区' },
          { value: '二七区', label: '二七区' },
          { value: '金水区', label: '金水区' }
        ]
      }
    ]
  },
  {
    value: '山东省',
    label: '山东省',
    children: [
      {
        value: '济南市',
        label: '济南市',
        children: [
          { value: '历下区', label: '历下区' },
          { value: '市中区', label: '市中区' },
          { value: '槐荫区', label: '槐荫区' }
        ]
      },
      {
        value: '青岛市',
        label: '青岛市',
        children: [
          { value: '市南区', label: '市南区' },
          { value: '市北区', label: '市北区' },
          { value: '崂山区', label: '崂山区' }
        ]
      }
    ]
  }
]

// 处理地区选择变化
const handleRegionChange = (value) => {
  if (value && value.length === 3) {
    form.province = value[0]
    form.city = value[1]
    form.district = value[2]
  } else {
    form.province = ''
    form.city = ''
    form.district = ''
  }
}

// 上传前校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 图片预览
const handlePreview = (file) => {
  previewImageUrl.value = processImageUrl(file.url)
  previewVisible.value = true
}

// 移除图片
const handleRemoveLicense = () => {
  form.businessLicense = ''
  licenseFileList.value = []
}

const handleUploadLicense = async (options) => {
  try {
    // 首先创建本地URL用于立即预览！
    const localPreviewUrl = URL.createObjectURL(options.file)
    
    // 先显示本地预览，让用户立即看到效果
    licenseFileList.value = [{
      name: options.file.name,
      url: localPreviewUrl,
      uid: Date.now(),
      status: 'uploading'
    }]
    
    ElMessage.info('正在上传图片...')

    // 方案1: 尝试通过后端中转上传
    try {
      const formData = new FormData()
      formData.append('file', options.file)

      const token = localStorage.getItem('token')
      const headers = {}
      if (token) {
        headers['Authorization'] = `Bearer ${token}`
      }

      const uploadRes = await fetch('/api/common/upload', {
        method: 'POST',
        headers,
        body: formData
      })

      if (uploadRes.ok) {
        const result = await uploadRes.json()
        if (result.code === 200 && result.data) {
          const imageUrl = result.data.url || result.data
          form.businessLicense = imageUrl
          licenseFileList.value = [{
            name: options.file.name,
            url: processImageUrl(imageUrl),
            uid: Date.now(),
            status: 'success'
          }]
          // 释放本地临时URL
          URL.revokeObjectURL(localPreviewUrl)
          ElMessage.success('营业执照上传成功')
          return
        }
      }
    } catch (e) {
      console.warn('后端上传失败，尝试七牛云直传:', e)
    }

    // 方案2: 尝试七牛云直传
    const tokenRes = await qiniuApi.getUploadToken()

    if (!tokenRes || (!tokenRes.token && !tokenRes.uploadToken)) {
      throw new Error('获取上传凭证失败')
    }

    const uploadToken = tokenRes.token || tokenRes.uploadToken
    const domain = tokenRes.domain || ''

    const qiniuFormData = new FormData()
    qiniuFormData.append('file', options.file)
    qiniuFormData.append('token', uploadToken)
    const fileKey = `license/${Date.now()}_${options.file.name}`
    qiniuFormData.append('key', fileKey)

    const response = await fetch('https://upload.qbox.me/', {
      method: 'POST',
      body: qiniuFormData
    })

    if (!response.ok) {
      const errorText = await response.text()
      console.error('七牛云上传错误:', errorText)
      throw new Error('七牛云上传失败，请重试')
    }

    const result = await response.json()

    let imageUrl = ''
    if (domain) {
      imageUrl = `${domain}/${result.key}`
    } else {
      imageUrl = result.key || fileKey
    }

    form.businessLicense = imageUrl
    licenseFileList.value = [{
      name: options.file.name,
      url: processImageUrl(imageUrl),
      uid: Date.now(),
      status: 'success'
    }]
    // 释放本地临时URL
    URL.revokeObjectURL(localPreviewUrl)

    ElMessage.success('营业执照上传成功')

  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error(error.message || '上传失败，请重试')
    licenseFileList.value = []
    // 释放本地临时URL
    URL.revokeObjectURL(localPreviewUrl)
    throw error
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    // 1. 表单验证
    await formRef.value.validate()
    
    // 2. 检查是否已上传营业执照
    if (!form.businessLicense) {
      ElMessage.warning('请上传营业执照')
      return
    }

    submitting.value = true

    // 3. 构建提交数据 - 合并地区信息
    const submitData = {
      shopName: form.shopName,
      description: form.description,
      contactName: form.realName,
      contactPhone: form.contactPhone,
      contactAddress: `${form.province}${form.city}${form.district}${form.detailAddress}`,
      province: form.province,
      city: form.city,
      district: form.district,
      detailAddress: form.detailAddress,
      businessLicense: form.businessLicense
    }

    console.log('提交申请数据:', submitData)

    // 4. 调用API提交申请
    const res = await farmerApi.applyFarmer(submitData)
    
    ElMessage.success(res?.message || '申请提交成功，请耐心等待审核')
    currentStatus.value = 0

    // 更新用户信息中的farmerApplyStatus
    if (userStore.userInfo) {
      userStore.userInfo.farmerApplyStatus = 0
    }
    
    setTimeout(() => {
      router.push('/user')
    }, 2000)
    
  } catch (error) {
    console.error('提交申请失败:', error)
    
    // 区分不同类型的错误
    if (error.message?.includes('验证') || error.message?.includes('required')) {
      // 表单验证错误，不需要额外提示（el-form已经显示了）
      return
    }
    
    if (error.response?.status === 403) {
      ElMessage.error('您没有权限执行此操作，请确认已登录且账号状态正常')
    } else if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(error.message || error.response?.data?.message || '提交失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    // 检查用户是否已登录
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录后再申请')
      router.push('/login')
      return
    }

    // 获取申请状态和已有信息
    try {
      const profile = await farmerApi.getFarmerProfile()
      console.log('已有农户资料:', profile)
      
      if (profile) {
        currentStatus.value = profile.status
        
        // 填充表单
        if (profile.shopName) form.shopName = profile.shopName
        if (profile.description) form.description = profile.description
        if (profile.realName) form.realName = profile.realName
        if (profile.idCard) form.idCard = profile.idCard
        if (profile.contactPhone) form.contactPhone = profile.contactPhone
        if (profile.province) form.province = profile.province
        if (profile.city) form.city = profile.city
        if (profile.district) form.district = profile.district
        if (profile.detailAddress) form.detailAddress = profile.detailAddress
        if (profile.province && profile.city && profile.district) {
          addressArray.value = [profile.province, profile.city, profile.district]
        }
        
        // 处理营业执照图片
        if (profile.businessLicense) {
          form.businessLicense = profile.businessLicense
          licenseFileList.value = [{
            name: '营业执照',
            url: processImageUrl(profile.businessLicense),
            uid: Date.now(),
            status: 'success'
          }]
        }
        
        if (profile.auditRemark) {
          rejectReason.value = profile.auditRemark
        }
      }
    } catch (e) {
      // 如果没有已有资料，尝试获取状态
      const statusRes = await farmerApi.getApplyStatus()
      console.log('申请状态:', statusRes)
      
      if (typeof statusRes === 'number') {
        currentStatus.value = statusRes
      } else if (statusRes?.data !== undefined) {
        currentStatus.value = statusRes.data
      } else if (statusRes?.status !== undefined) {
        currentStatus.value = statusRes.status
      }
      
      if (statusRes?.rejectReason) {
        rejectReason.value = statusRes.rejectReason
      }
    }
    
  } catch (error) {
    console.error('获取申请状态失败:', error)
    
    if (error.response?.status === 403) {
      console.log('403错误，可能是首次申请用户，允许继续填写表单')
      currentStatus.value = null
    } else if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    }
  }
})
</script>

<style scoped lang="scss">
.apply-farmer-page {
  max-width: 800px;
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

  .apply-form {
    padding: 0 20px;
    
    // el-cascader 样式优化
    :deep(.el-cascader) {
      width: 100%;
    }
    
    // 上传组件样式优化
    :deep(.el-upload--picture-card) {
      width: 148px;
      height: 148px;
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: all 0.3s;
      
      &:hover {
        border-color: #409eff;
      }
      
      .el-icon {
        font-size: 28px;
        color: #8c939d;
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
      }
    }
    
    :deep(.el-upload-list--picture-card) {
      .el-upload-list__item {
        width: 148px;
        height: 148px;
        border-radius: 6px;
        
        img {
          object-fit: cover;
        }
      }
    }
  }

  .upload-tip {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
    line-height: 1.4;
  }

  .status-info {
    margin-top: 20px;
    
    &.warning,
    &.error {
      padding: 10px 20px;
      border-radius: 4px;
    }
  }
}
</style>
