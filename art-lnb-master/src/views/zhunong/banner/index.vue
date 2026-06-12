<template>
  <div class="banner-page">
    <ArtPageWrapper>
      <ArtCard title="轮播图管理">
        <div class="toolbar">
          <el-button type="primary" @click="handleAdd"
            ><i class="ri-add-line"></i> 新增轮播图</el-button
          >
        </div>

        <el-table :data="bannerList" v-loading="loading">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="图片" width="200">
            <template #default="{ row }">
              <el-image
                :src="row.imageUrl"
                style="width: 180px; height: 80px; border-radius: 4px"
                fit="cover"
              />
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="200" />
          <el-table-column prop="linkUrl" label="链接" min-width="200" show-overflow-tooltip />
          <el-table-column prop="sort" label="排序" width="80" align="center" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '显示' : '隐藏' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </ArtCard>
    </ArtPageWrapper>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑轮播图' : '新增轮播图'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="链接">
          <el-input v-model="form.linkUrl" placeholder="请输入跳转链接（可选）" />
        </el-form-item>
        <el-form-item label="图片" required>
          <el-upload
            ref="uploadRef"
            class="banner-uploader"
            action="#"
            :http-request="handleUpload"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            accept="image/*"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="uploaded-image" />
            <el-icon v-else class="uploader-icon">
              <Plus />
            </el-icon>
          </el-upload>
          <div class="upload-tip">支持 JPG/PNG 格式，大小不超过 2MB</div>
        </el-form-item>
        <el-form-item label="排序" required>
          <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" required>
          <el-radio-group v-model="form.status">
            <el-radio :label="1">显示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" :disabled="uploading">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="uploading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { adminApi } from '@/api/admin'
  import { Plus } from '@element-plus/icons-vue'

  const loading = ref(false)
  const bannerList = ref<any[]>([])
  const dialogVisible = ref(false)
  const isEdit = ref(false)
  const uploading = ref(false)
  const uploadRef = ref<any>(null)

  const form = reactive({
    id: undefined as number | undefined,
    title: '',
    imageUrl: '',
    linkUrl: '',
    sort: 0,
    status: 1
  })

  const fetchBannerList = async () => {
    loading.value = true
    try {
      const data = await adminApi.getBannerList()
      if (data) {
        bannerList.value = (data as any).records || data || []
      }
    } catch {
      console.error('获取轮播图列表失败')
    } finally {
      loading.value = false
    }
  }

  const handleAdd = () => {
    form.id = undefined
    form.title = ''
    form.imageUrl = ''
    form.linkUrl = ''
    form.sort = 0
    form.status = 1
    isEdit.value = false
    dialogVisible.value = true
  }

  const beforeUpload = (file: File) => {
    const isImage = file.type.startsWith('image/')
    const isLt5M = file.size / 1024 / 1024 < 5

    if (!isImage) {
      ElMessage.error('只能上传图片文件！')
    }
    if (!isLt5M) {
      ElMessage.error('图片大小不能超过 5MB!')
    }
    return isImage && isLt5M
  }

  const handleUpload = async (options: any) => {
    uploading.value = true
    try {
      const formData = new FormData()
      formData.append('file', options.file)

      // 通过后端中转上传
      const response = await fetch('/api/common/upload', {
        method: 'POST',
        body: formData
      })

      const result = await response.json()

      if (result.code === 200 && result.data?.url) {
        form.imageUrl = result.data.url
        ElMessage.success('图片上传成功')
        options.onSuccess(result)
      } else {
        throw new Error(result.message || '上传失败')
      }
    } catch (error) {
      console.error('上传失败:', error)
      ElMessage.error('图片上传失败')
      options.onError(error)
    } finally {
      uploading.value = false
    }
  }

  const handleUploadSuccess = () => {
    // 上传成功后刷新列表
    fetchBannerList()
  }

  const handleUploadError = () => {
    // 上传失败回调
  }

  const handleEdit = (row: any) => {
    form.id = row.id
    form.title = row.title
    form.imageUrl = row.imageUrl
    form.linkUrl = row.linkUrl
    form.sort = row.sort
    form.status = row.status
    isEdit.value = true
    dialogVisible.value = true
  }

  const handleDelete = async (row: any) => {
    try {
      await ElMessageBox.confirm('确定要删除该轮播图吗？', '提示', {
        type: 'warning'
      })
      await adminApi.deleteBanner(row.id)
      ElMessage.success('删除成功')
      fetchBannerList()
    } catch {
      console.log('取消删除')
    }
  }

  const handleSubmit = async () => {
    if (!form.title || !form.imageUrl) {
      ElMessage.error('请填写完整信息')
      return
    }

    try {
      const submitData: any = {
        title: form.title,
        imageUrl: form.imageUrl,
        linkUrl: form.linkUrl || '',
        sort: form.sort || 0,
        status: form.status ?? 1
      }
      if (isEdit.value && form.id) {
        await adminApi.updateBanner(form.id, submitData)
        ElMessage.success('编辑成功')
      } else {
        await adminApi.createBanner(submitData)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchBannerList()
    } catch (error: any) {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  }

  onMounted(() => {
    fetchBannerList()
  })
</script>

<style scoped lang="scss">
  .banner-page {
    .toolbar {
      margin-bottom: 20px;
    }
  }

  .banner-uploader {
    width: 300px;
    height: 150px;
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    display: flex;
    justify-content: center;
    align-items: center;

    &:hover {
      border-color: var(--el-color-primary);
    }

    .uploaded-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .uploader-icon {
      font-size: 48px;
      color: var(--el-color-info);
    }
  }

  .upload-tip {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 8px;
  }
</style>
