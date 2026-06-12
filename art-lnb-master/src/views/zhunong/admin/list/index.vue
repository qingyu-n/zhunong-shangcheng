<template>
  <div class="admin-list-page">
    <ArtPageWrapper>
      <ArtCard title="管理员列表">
        <div class="toolbar">
          <el-button type="primary" @click="handleAdd"
            ><i class="ri-add-line"></i> 新增管理员</el-button
          >
        </div>

        <el-table :data="adminList" v-loading="loading">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="nickname" label="昵称" width="120" />
          <el-table-column label="角色" width="120">
            <template #default="{ row }">
              {{ getRoleName(row.role) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-dialog
          v-model="dialogVisible"
          title="新增管理员"
          width="500px"
          @close="dialogVisible = false"
        >
          <el-form :model="adminForm" label-width="80px" label-position="right">
            <el-form-item label="用户名" required>
              <el-input v-model="adminForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="昵称" required>
              <el-input v-model="adminForm.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="角色" required>
              <el-select v-model="adminForm.role" placeholder="请选择角色" style="width: 100%">
                <el-option label="超级管理员" value="super_admin" />
                <el-option label="普通管理员" value="admin" />
                <el-option label="运营人员" value="operator" />
              </el-select>
            </el-form-item>
            <el-form-item label="密码" required>
              <el-input
                v-model="adminForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit">确定</el-button>
          </template>
        </el-dialog>
      </ArtCard>
    </ArtPageWrapper>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { adminApi } from '@/api/admin'

  const loading = ref(false)
  const adminList = ref<any[]>([])

  const dialogVisible = ref(false)
  const isEdit = ref(false)

  const getRoleName = (role: string): string => {
    const roleMap: Record<string, string> = {
      super_admin: '超级管理员',
      admin: '普通管理员',
      operator: '运营人员'
    }
    return roleMap[role] || role || '普通管理员'
  }
  const adminForm = reactive({
    id: undefined as number | undefined,
    username: '',
    nickname: '',
    role: '',
    password: '',
    status: 1
  })

  const fetchAdminList = async () => {
    loading.value = true
    try {
      const res: any = await adminApi.getAdminList()
      if (res) {
        adminList.value =
          res.list || res.records || (Array.isArray(res) ? res : [])
      }
    } catch (error) {
      console.error('获取管理员列表失败:', error)
      ElMessage.error('获取管理员列表失败')
    } finally {
      loading.value = false
    }
  }

  const handleAdd = () => {
    adminForm.id = undefined
    adminForm.username = ''
    adminForm.nickname = ''
    adminForm.role = ''
    adminForm.password = ''
    isEdit.value = false
    dialogVisible.value = true
  }

  const handleEdit = (row: any) => {
    adminForm.id = row.id
    adminForm.username = row.username
    adminForm.nickname = row.nickname
    adminForm.role = row.role
    adminForm.password = ''
    isEdit.value = true
    dialogVisible.value = true
  }

  const handleSubmit = async () => {
    if (!adminForm.username || !adminForm.role || (!isEdit.value && !adminForm.password)) {
      ElMessage.warning('请填写必填项')
      return
    }
    try {
      const submitData = { ...adminForm }
      if (!isEdit.value) {
        // 新增时补全默认值，避免字段不匹配导致400/500错误
        submitData.status = submitData.status ?? 1
        submitData.nickname = submitData.nickname || submitData.username
      }
      if (isEdit.value) {
        await adminApi.updateAdmin(adminForm.id!, submitData)
        ElMessage.success('编辑成功')
      } else {
        await adminApi.createAdmin(submitData)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchAdminList()
    } catch (error: any) {
      console.error('操作失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  }

  const handleDelete = async (row: any) => {
    try {
      await ElMessageBox.confirm('确定要删除该管理员吗？', '警告', {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      })
      await adminApi.deleteAdmin(row.id)
      ElMessage.success('删除成功')
      fetchAdminList()
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
        ElMessage.error(error.message || '删除失败')
      }
    }
  }

  onMounted(() => {
    fetchAdminList()
  })
</script>

<style scoped lang="scss">
  .admin-list-page {
    .toolbar {
      margin-bottom: 20px;
    }
  }
</style>
