<template>
  <div class="config-page">
    <ArtPageWrapper>
      <ArtCard title="系统配置">
        <el-form :model="configForm" label-width="150px" style="max-width: 600px">
          <el-form-item label="网站名称">
            <el-input v-model="configForm.siteName" placeholder="请输入网站名称" />
          </el-form-item>
          <el-form-item label="网站Logo">
            <el-input v-model="configForm.logo" placeholder="请输入Logo URL" />
          </el-form-item>
          <el-form-item label="客服电话">
            <el-input v-model="configForm.servicePhone" placeholder="请输入客服电话" />
          </el-form-item>
          <el-form-item label="客服邮箱">
            <el-input v-model="configForm.serviceEmail" placeholder="请输入客服邮箱" />
          </el-form-item>
          <el-form-item label="版权信息">
            <el-input
              v-model="configForm.copyright"
              type="textarea"
              :rows="3"
              placeholder="请输入版权信息"
            />
          </el-form-item>
          <el-form-item label="备案号">
            <el-input v-model="configForm.icp" placeholder="请输入备案号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSave">保存配置</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </ArtCard>
    </ArtPageWrapper>
  </div>
</template>

<script setup lang="ts">
  import { reactive, onMounted } from 'vue'
  import { ElMessage } from 'element-plus'
  import { adminApi } from '@/api/admin'

  const configForm = reactive({
    siteName: '',
    logo: '',
    servicePhone: '',
    serviceEmail: '',
    copyright: '',
    icp: ''
  })

  const fetchConfig = async () => {
    try {
      const data = await adminApi.getConfig()
      if (data) {
        Object.assign(configForm, data)
      }
    } catch (error) {
      console.error('获取配置失败:', error)
      ElMessage.error('获取系统配置失败')
    }
  }

  const handleSave = async () => {
    try {
      await adminApi.saveConfig(configForm)
      ElMessage.success('保存成功')
    } catch (error: any) {
      console.error('保存配置失败:', error)
      ElMessage.error(error.message || '保存配置失败')
    }
  }

  const handleReset = () => {
    fetchConfig()
  }

  onMounted(() => {
    fetchConfig()
  })
</script>

<style scoped lang="scss">
  .config-page {
    :deep(.el-form-item__label) {
      font-weight: 500;
    }
  }
</style>
