<template>
  <div class="category-manage-page">
    <ElCard class="search-card">
      <ElForm :model="searchForm" inline>
        <ElFormItem label="分类名称">
          <ElInput v-model="searchForm.name" placeholder="请输入分类名称" clearable />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchForm.status" placeholder="请选择状态" clearable>
            <ElOption label="启用" :value="1" />
            <ElOption label="禁用" :value="0" />
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
        <h3 class="table-title">商品分类列表</h3>
        <ElButton type="primary" @click="handleAdd">
          <Icon name="ri:add-line" class="mr-1" />
          新增分类
        </ElButton>
      </div>

      <ElTable v-loading="loading" :data="categoryList" border stripe style="width: 100%">
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn prop="name" label="分类名称" min-width="150" />
        <ElTableColumn prop="icon" label="图标" width="100" align="center">
          <template #default="{ row }">
            <img v-if="row.icon" :src="row.icon" class="category-icon" />
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="sort" label="排序" width="100" align="center" sortable />
        <ElTableColumn prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
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
    </ElCard>

    <!-- 新增/编辑弹窗 -->
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <ElFormItem label="分类名称" prop="name">
          <ElInput v-model="formData.name" placeholder="请输入分类名称" />
        </ElFormItem>
        <ElFormItem label="分类图标" prop="icon">
          <div class="upload-wrapper">
            <ElUpload
              class="avatar-uploader"
              :action="uploadAction"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
            >
              <img v-if="formData.icon" :src="formData.icon" class="avatar" />
              <ElIcon v-else class="avatar-uploader-icon">
                <Plus />
              </ElIcon>
            </ElUpload>
            <span class="upload-tip">点击上传图标，建议尺寸 100x100</span>
          </div>
        </ElFormItem>
        <ElFormItem label="排序" prop="sort">
          <ElInputNumber v-model="formData.sort" :min="0" :max="999" />
        </ElFormItem>
        <ElFormItem label="状态" prop="status">
          <ElRadioGroup v-model="formData.status">
            <ElRadio :label="1">启用</ElRadio>
            <ElRadio :label="0">禁用</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
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
  import {
    fetchGetCategoryList,
    fetchCreateCategory,
    fetchUpdateCategory,
    fetchDeleteCategory,
    fetchGetQiniuToken
  } from '@/api/mall'

  defineOptions({ name: 'CategoryManage' })

  // 搜索表单
  const searchForm = reactive({
    name: '',
    status: undefined as number | undefined
  })

  // 表格数据
  const loading = ref(false)
  const categoryList = ref<Api.Category.CategoryItem[]>([])

  // 弹窗
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const dialogTitle = computed(() => (dialogType.value === 'add' ? '新增分类' : '编辑分类'))
  const submitLoading = ref(false)

  // 表单
  const formRef = ref()
  const formData = reactive<Api.Category.CategoryForm>({
    name: '',
    icon: '',
    sort: 0,
    status: 1
  })

  const formRules = {
    name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
    sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
  }

  // 七牛云上传配置
  const uploadAction = ref('https://upload.qiniup.com')
  const uploadHeaders = ref({})
  const qiniuToken = ref('')
  const qiniuDomain = ref('')

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
    loading.value = true
    try {
      const res = await fetchGetCategoryList({
        name: searchForm.name || undefined,
        status: searchForm.status
      })
      categoryList.value = res
    } catch (error) {
      console.error('获取分类列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 搜索
  const handleSearch = () => {
    getCategoryList()
  }

  // 重置
  const handleReset = () => {
    searchForm.name = ''
    searchForm.status = undefined
    getCategoryList()
  }

  // 新增
  const handleAdd = () => {
    dialogType.value = 'add'
    formData.id = undefined
    formData.name = ''
    formData.icon = ''
    formData.sort = 0
    formData.status = 1
    dialogVisible.value = true
  }

  // 编辑
  const handleEdit = (row: Api.Category.CategoryItem) => {
    dialogType.value = 'edit'
    formData.id = row.id
    formData.name = row.name
    formData.icon = row.icon
    formData.sort = row.sort
    formData.status = row.status
    dialogVisible.value = true
  }

  // 删除
  const handleDelete = async (row: Api.Category.CategoryItem) => {
    try {
      await ElMessageBox.confirm('确定要删除该分类吗？删除后无法恢复！', '提示', {
        type: 'warning'
      })
      await fetchDeleteCategory(row.id)
      ElMessage.success('删除成功')
      getCategoryList()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
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

  // 上传成功
  const handleUploadSuccess = (res: any) => {
    if (res.key) {
      formData.icon = `${qiniuDomain.value}/${res.key}`
      ElMessage.success('上传成功')
    } else {
      ElMessage.error('上传失败')
    }
  }

  // 提交
  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()
      submitLoading.value = true

      if (dialogType.value === 'add') {
        await fetchCreateCategory(formData)
        ElMessage.success('新增成功')
      } else {
        await fetchUpdateCategory(formData)
        ElMessage.success('更新成功')
      }

      dialogVisible.value = false
      getCategoryList()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitLoading.value = false
    }
  }

  onMounted(() => {
    getCategoryList()
    getQiniuToken()
  })
</script>

<style scoped>
  .category-manage-page {
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

  .category-icon {
    width: 40px;
    height: 40px;
    object-fit: cover;
    border-radius: 4px;
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
    width: 100px;
    height: 100px;
    text-align: center;
    line-height: 100px;
  }

  .avatar {
    width: 100px;
    height: 100px;
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
