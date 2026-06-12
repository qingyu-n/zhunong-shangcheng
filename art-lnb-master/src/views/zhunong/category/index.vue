<template>
  <div class="category-page">
    <ArtPageWrapper>
      <ArtCard title="商品分类管理">
        <div class="toolbar">
          <el-button type="primary" @click="handleAdd">
            <i class="ri-add-line"></i> 新增分类
          </el-button>
        </div>

        <el-table :data="categoryList" v-loading="loading" row-key="id" default-expand-all>
          <el-table-column prop="name" label="分类名称" min-width="150" />
          <el-table-column prop="icon" label="图标" width="100">
            <template #default="{ row }">
              <i :class="row.icon" v-if="row.icon" style="font-size: 20px"></i>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '正常' : '禁用' }}
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
      </ArtCard>
    </ArtPageWrapper>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-select
            v-model="form.parentId"
            placeholder="请选择父分类"
            clearable
            style="width: 100%"
          >
            <el-option label="无（一级分类）" :value="0" />
            <el-option
              v-for="item in parentCategories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="请输入图标类名" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
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
  import { ref, reactive, computed, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { adminApi } from '@/api/admin'

  const loading = ref(false)
  const categoryList = ref<any[]>([])
  const dialogVisible = ref(false)
  const isEdit = ref(false)

  const form = reactive({
    id: undefined as number | undefined,
    name: '',
    parentId: 0,
    icon: '',
    sortOrder: 0,
    status: 1
  })

  const parentCategories = computed(() => {
    return categoryList.value.filter((item: any) => item.level === 0)
  })

  const fetchCategoryList = async () => {
    loading.value = true
    try {
      const data = await adminApi.getCategoryList()
      if (data) {
        categoryList.value = Array.isArray(data)
          ? data
          : ((data as any)?.list || (data as any)?.records || [])
      }
    } catch (error) {
      console.error('获取分类列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  const resetForm = () => {
    form.id = undefined
    form.name = ''
    form.parentId = 0
    form.icon = ''
    form.sortOrder = 0
    form.status = 1
  }

  const handleAdd = () => {
    resetForm()
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
      await ElMessageBox.confirm(
        '确定要删除该分类吗？关联的商品将变为未分类状态',
        '提示',
        { type: 'warning' }
      )
      await adminApi.deleteCategory(row.id)
      ElMessage.success('删除成功')
      fetchCategoryList()
    } catch (error: any) {
      if (error !== 'cancel' && error !== 'close') {
        console.error('删除分类失败:', error)
        ElMessage.error(error?.message || '删除失败，请重试')
      }
    }
  }

  const handleSubmit = async () => {
    if (!form.name) {
      ElMessage.warning('请输入分类名称')
      return
    }
    try {
      const submitData: any = {
        name: form.name,
        parentId: form.parentId || 0,
        icon: form.icon || '',
        sortOrder: form.sortOrder || 0,
        status: form.status ?? 1
      }
      if (!isEdit.value) {
        submitData.level = (form.parentId || 0) > 0 ? 1 : 0
      }

      if (isEdit.value && form.id) {
        await adminApi.updateCategory(form.id, submitData)
      } else {
        await adminApi.createCategory(submitData)
      }
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      fetchCategoryList()
    } catch (error: any) {
      console.error('提交失败详情:', error)
      ElMessage.error(error?.message || '操作失败，请重试')
    }
  }

  onMounted(() => {
    fetchCategoryList()
  })
</script>

<style scoped lang="scss">
  .category-page {
    .toolbar {
      margin-bottom: 20px;
    }
  }
</style>
