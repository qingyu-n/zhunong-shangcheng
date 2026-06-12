<template>
  <div class="my-shop-page">
    <el-card class="shop-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h2>我的店铺</h2>
          <div class="header-actions">
            <el-button @click="handleSyncFromFarmerProfile" type="success">🔄 同步农户申请数据</el-button>
            <el-button @click="handleDebug">调试/刷新数据</el-button>
            <el-button type="primary" @click="handlePreviewShop">
              <el-icon><View /></el-icon>
              预览店铺
            </el-button>
          </div>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="shop-form">

        <!-- 店铺基本信息 -->
        <el-card class="section-card" shadow="never">
          <template #header><span>基本信息</span></template>

          <el-form-item label="店铺名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入店铺名称" maxlength="50" show-word-limit />
          </el-form-item>

          <el-form-item label="店铺Logo">
            <el-upload
              action="#"
              :http-request="handleUploadLogo"
              :show-file-list="false"
              accept="image/*"
              list-type="picture-card"
              class="logo-uploader"
            >
              <img v-if="form.logo" :src="processedLogoUrl" class="uploaded-image" />
              <el-icon v-else><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">建议尺寸200x200像素，支持JPG、PNG格式</div>
          </el-form-item>

          <el-form-item label="店铺横幅">
            <el-upload
              action="#"
              :http-request="handleUploadBanner"
              :show-file-list="false"
              accept="image/*"
              list-type="picture-card"
              class="banner-uploader"
            >
              <img v-if="form.bannerImage" :src="processedBannerUrl" class="uploaded-image banner-image" />
              <el-icon v-else><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">建议尺寸1200x300像素，用于店铺首页顶部展示</div>
          </el-form-item>
        </el-card>

        <!-- 联系信息 -->
        <el-card class="section-card" shadow="never">
          <template #header><span>联系信息</span></template>

          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="form.contactPhone" placeholder="请输入联系电话" maxlength="20" />
          </el-form-item>

          <el-form-item label="微信号">
            <el-input v-model="form.contactWechat" placeholder="请输入微信号（选填）" maxlength="100" />
          </el-form-item>

          <el-form-item label="店铺地址">
            <el-input v-model="form.address" placeholder="请输入店铺地址" maxlength="255" />
          </el-form-item>
        </el-card>

        <!-- 店铺简介 -->
        <el-card class="section-card" shadow="never">
          <template #header><span>店铺简介</span></template>

          <el-form-item label="简介内容">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="6"
              placeholder="请输入店铺简介，介绍您的农场/果园特色、经营理念等"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>
        </el-card>

        <!-- 统计数据 -->
        <el-card class="section-card stats-card" shadow="never">
          <template #header><span>店铺数据</span></template>

          <el-row :gutter="24">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-number">{{ shopStats.viewCount || 0 }}</div>
                <div class="stat-label">访问次数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-number">{{ shopStats.favoriteCount || 0 }}</div>
                <div class="stat-label">收藏数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-number">{{ shopStats.productCount || 0 }}</div>
                <div class="stat-label">商品数</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button @click="$router.push('/my-products')">管理商品</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, View } from '@element-plus/icons-vue'
import { shopApi, farmerApi, qiniuApi, authApi } from '@/api'
import request from '@/utils/request'
import { processImageUrl } from '@/utils/image'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const saving = ref(false)

// 关键！使用 ref 而不是 reactive 来强制响应式！
const form = ref({
  name: '',
  logo: '',
  bannerImage: '',
  contactPhone: '',
  contactWechat: '',
  address: '',
  description: ''
})

const shopStats = ref({
  viewCount: 0,
  favoriteCount: 0,
  productCount: 0
})

// 处理图片URL的计算属性
const processedLogoUrl = computed(() => processImageUrl(form.value.logo))
const processedBannerUrl = computed(() => processImageUrl(form.value.bannerImage))

const rules = {
  name: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const resetForm = () => {
  form.value = {
    name: '',
    logo: '',
    bannerImage: '',
    contactPhone: '',
    contactWechat: '',
    address: '',
    description: ''
  }
  shopStats.value = { viewCount: 0, favoriteCount: 0, productCount: 0 }
}

const fetchShopInfo = async () => {
  try {
    console.log('=== 开始获取店铺数据 ===')
    
    // 1. 重置表单
    resetForm()
    
    // 2. 强制刷新用户信息
    console.log('2. 刷新用户信息...')
    await userStore.fetchUserInfo()
    console.log('用户信息已更新:', userStore.userInfo)
    
    // 3. 获取店铺信息
    console.log('3. 获取店铺信息...')
    const shopData = await shopApi.getMyShop()
    console.log('✅ 成功获取店铺数据:', shopData)
    
    if (shopData) {
      form.value.name = shopData.name || ''
      form.value.logo = shopData.logo || ''
      form.value.bannerImage = shopData.bannerImage || ''
      form.value.contactPhone = shopData.contactPhone || ''
      form.value.contactWechat = shopData.contactWechat || ''
      form.value.address = shopData.address || ''
      form.value.description = shopData.description || ''
      
      shopStats.value.viewCount = shopData.viewCount || 0
      shopStats.value.favoriteCount = shopData.favoriteCount || 0
      shopStats.value.productCount = shopData.productCount || 0
      
      console.log('表单已更新:', form.value)
    }
    
  } catch (error) {
    console.error('❌ 获取店铺信息失败:', error)
    
    console.log('尝试获取农户档案...')
    try {
      const profile = await farmerApi.getFarmerProfile()
      console.log('获取到农户档案:', profile)
      
      if (profile) {
        form.value.name = profile.farmName || profile.shopName || ''
        form.value.logo = profile.shopLogo || ''
        form.value.contactPhone = profile.contactPhone || ''
        form.value.address = profile.address || profile.contactAddress || ''
        form.value.description = profile.description || ''
        
        console.log('从农户档案更新表单:', form.value)
      }
    } catch (e) {
      console.error('获取农户档案也失败:', e)
    }
  }
}

const uploadImage = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  const res = await request.post('/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return res.url
}

const handleUploadLogo = async (options) => {
  try {
    const url = await uploadImage(options.file)
    form.value.logo = url
    ElMessage.success('Logo上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const handleUploadBanner = async (options) => {
  try {
    const url = await uploadImage(options.file)
    form.value.bannerImage = url
    ElMessage.success('横幅图片上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const handleSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    await shopApi.updateShop(form.value)
    ElMessage.success('保存成功')
    fetchShopInfo()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleDebug = async () => {
  console.log('========== 调试模式 ==========')
  console.log('当前表单:', form.value)
  console.log('当前用户:', userStore.userInfo)
  console.log('localStorage:', {
    token: localStorage.getItem('token'),
    userInfo: localStorage.getItem('userInfo')
  })
  
  ElMessage.info('正在强制刷新数据，请查看控制台...')
  
  await fetchShopInfo()
  
  ElMessage.success('数据刷新完成！')
}

// 新增：从农户申请数据同步到店铺！
const handleSyncFromFarmerProfile = async () => {
  try {
    ElMessage.info('正在同步农户申请数据...')
    console.log('开始从农户档案同步数据到店铺...')
    
    const profile = await farmerApi.getFarmerProfile()
    console.log('✅ 获取到农户档案:', profile)
    
    if (!profile) {
      ElMessage.warning('找不到农户申请记录')
      return
    }
    
    const dataToUpdate = {
      name: profile.shopName,
      logo: profile.shopLogo,
      description: profile.description,
      contactPhone: profile.contactPhone,
      address: `${profile.province || ''}${profile.city || ''}${profile.district || ''}${profile.detailAddress || ''}`
    }
    
    console.log('准备更新到店铺的数据:', dataToUpdate)
    
    // 先更新表单
    form.value = {
      ...form.value,
      ...dataToUpdate
    }
    
    // 再调用更新接口
    await shopApi.updateShop(dataToUpdate)
    
    ElMessage.success('✅ 同步成功！店铺数据已更新！')
    
    await fetchShopInfo()
    
  } catch (e) {
    console.error('同步失败:', e)
    ElMessage.error('同步失败: ' + (e.message || '未知错误'))
  }
}

const handlePreviewShop = () => {
  router.push('/shop/my')
}

onMounted(() => {
  console.log('MyShop 组件已挂载')
  fetchShopInfo()
})
</script>

<style scoped>
.my-shop-page {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.shop-form {
  margin-top: 20px;
}

.section-card {
  margin-bottom: 24px;
}

.section-card .el-card__header span {
  font-weight: 600;
  color: #333;
}

.upload-tip {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-image {
  height: 150px;
}

.stats-card .stat-item {
  text-align: center;
  padding: 20px 0;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #52c41a;
  margin-bottom: 8px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.action-buttons {
  text-align: center;
  margin-top: 30px;
  padding: 20px 0;
}

.action-buttons .el-button {
  min-width: 120px;
  margin: 0 10px;
}
</style>
