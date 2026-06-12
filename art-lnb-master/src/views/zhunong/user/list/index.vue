<template>
  <div class="user-list-page">
    <ArtPageWrapper>
      <ArtCard title="用户列表">
        <div class="search-bar">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
          <el-select
            v-model="searchForm.status"
            placeholder="用户状态"
            clearable
            style="width: 120px"
          >
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button type="primary" @click="handleSearch">
            <i class="ri-search-line"></i> 搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">
            <i class="ri-add-line"></i> 新增用户
          </el-button>
        </div>

        <el-table :data="userList" v-loading="loading" style="width: 100%">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="id" label="用户ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="nickname" label="昵称" width="120" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="email" label="邮箱" min-width="180" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="160" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
              <el-button
                :type="row.status === 1 ? 'danger' : 'success'"
                link
                size="small"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </ArtCard>

      <el-dialog
        v-model="dialogVisible"
        title="新增用户"
        width="500px"
        @close="dialogVisible = false"
      >
        <el-form :model="userForm" label-width="80px" label-position="right">
          <el-form-item label="用户名" required>
            <el-input v-model="userForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="昵称" required>
            <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="手机号" required>
            <el-input v-model="userForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" required>
            <el-input v-model="userForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="密码" required>
            <el-input
              v-model="userForm.password"
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

      <el-dialog
        v-model="detailDialogVisible"
        title="用户详情"
        width="550px"
        destroy-on-close
      >
        <el-descriptions v-if="currentUser" :column="2" border>
          <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentUser.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱" :span="2">{{ currentUser.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="头像" :span="2">
            <el-image
              v-if="currentUser.avatar"
              :src="currentUser.avatar"
              style="width: 60px; height: 60px; border-radius: 50%"
              fit="cover"
              :preview-src-list="[currentUser.avatar]"
            />
            <span v-else>未设置</span>
          </el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ currentUser.gender === 1 ? '男' : currentUser.gender === 2 ? '女' : '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentUser.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="是否农户">
            <el-tag :type="currentUser.isFarmer === 1 ? 'warning' : 'info'" size="small">
              {{ currentUser.isFarmer === 1 ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ currentUser.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="最后登录" :span="2">{{ currentUser.lastLoginTime || '-' }}</el-descriptions-item>
        </el-descriptions>
        <template #footer>
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </ArtPageWrapper>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { fetchGetUserList, fetchDeleteUser } from '@/api/system-manage'
  import { adminApi } from '@/api/admin'

  const loading = ref(false)
  const userList = ref<any[]>([])

  const dialogVisible = ref(false)
  const detailDialogVisible = ref(false)
  const currentUser = ref<any>(null)
  const userForm = reactive({
    username: '',
    nickname: '',
    phone: '',
    email: '',
    password: ''
  })

  const searchForm = reactive({
    username: '',
    status: undefined as string | undefined
  })

  const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
  })

  const fetchUserList = async () => {
    loading.value = true
    try {
      const data = await fetchGetUserList({
        current: pagination.current,
        size: pagination.size,
        userName: searchForm.username || undefined,
        status: searchForm.status
      })
      if (data) {
        userList.value = (data as any).list || (data as any).records || (Array.isArray(data) ? data : [])
        pagination.total = (data as any).total || (data as any)?.length || 0
      }
    } catch (error) {
      console.error('获取用户列表失败:', error)
      ElMessage.error('获取用户列表失败')
    } finally {
      loading.value = false
    }
  }

  const handleAdd = () => {
    userForm.username = ''
    userForm.nickname = ''
    userForm.phone = ''
    userForm.email = ''
    userForm.password = ''
    dialogVisible.value = true
  }

  const handleSubmit = async () => {
    if (!userForm.username || !userForm.password) {
      ElMessage.warning('请填写必填项')
      return
    }
    try {
      // 调用注册接口创建普通用户（写入sys_user表）
      await adminApi.createUser(userForm)
      ElMessage.success('新增用户成功')
      dialogVisible.value = false
      fetchUserList()
    } catch (error: any) {
      console.error('新增用户失败:', error)
      ElMessage.error(error.message || '新增用户失败')
    }
  }

  const handleSearch = () => {
    pagination.current = 1
    fetchUserList()
  }

  const handleReset = () => {
    searchForm.username = ''
    searchForm.status = undefined
    pagination.current = 1
    fetchUserList()
  }

  const handleSizeChange = (val: number) => {
    pagination.size = val
    fetchUserList()
  }

  const handleCurrentChange = (val: number) => {
    pagination.current = val
    fetchUserList()
  }

  const handleView = (row: any) => {
    currentUser.value = { ...row }
    detailDialogVisible.value = true
  }

  const handleToggleStatus = async (row: any) => {
    const action = row.status === 1 ? '禁用' : '启用'
    const newStatus = row.status === 1 ? 0 : 1
    try {
      await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
        type: 'warning'
      })
      // 调用后端API更新用户状态
      await adminApi.updateUserStatus({ id: row.id, status: newStatus })
      ElMessage.success(`${action}成功`)
      fetchUserList()
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('更新状态失败:', error)
        ElMessage.error(error.message || '更新状态失败')
      }
    }
  }

  const handleDelete = async (row: any) => {
    try {
      await ElMessageBox.confirm('确定要删除该用户吗？删除后不可恢复！', '警告', {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      })
      await fetchDeleteUser(row.id)
      ElMessage.success('删除成功')
      fetchUserList()
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('删除用户失败:', error)
        ElMessage.error(error.message || '删除用户失败')
      }
    }
  }

  onMounted(() => {
    fetchUserList()
  })
</script>

<style scoped lang="scss">
  .user-list-page {
    .search-bar {
      display: flex;
      gap: 12px;
      margin-bottom: 20px;
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }
  }
</style>
