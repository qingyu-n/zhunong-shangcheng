<template>
  <div class="user-manage-page">
    <ElCard class="search-card">
      <ElForm :model="searchForm" inline>
        <ElFormItem label="用户名">
          <ElInput v-model="searchForm.keyword" placeholder="请输入用户名" clearable />
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
        <h3 class="table-title">用户列表</h3>
      </div>

      <ElTable v-loading="loading" :data="userList" border stripe style="width: 100%">
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn label="头像" width="80" align="center">
          <template #default="{ row }">
            <ElAvatar :size="40" :src="row.avatar || defaultAvatar" />
          </template>
        </ElTableColumn>
        <ElTableColumn prop="username" label="用户名" min-width="120" />
        <ElTableColumn prop="nickname" label="昵称" min-width="120" />
        <ElTableColumn prop="phone" label="手机号" min-width="130" />
        <ElTableColumn prop="email" label="邮箱" min-width="180" />
        <ElTableColumn prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            <ElTag v-if="row.gender === 1" type="primary">男</ElTag>
            <ElTag v-else-if="row.gender === 2" type="danger">女</ElTag>
            <ElTag v-else type="info">保密</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <ElSwitch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val: string | number | boolean) => handleStatusChange(row, val as number)"
            />
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createTime" label="注册时间" min-width="160" />
        <ElTableColumn label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <ElButton type="primary" link @click="handleViewDetail(row)"> 查看 </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <div class="pagination-wrapper">
        <ElPagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </ElCard>

    <!-- 用户详情弹窗 -->
    <ElDialog v-model="detailVisible" title="用户详情" width="600px" destroy-on-close>
      <ElDescriptions :column="2" border v-if="currentUser">
        <ElDescriptionsItem label="用户ID">{{ currentUser.id }}</ElDescriptionsItem>
        <ElDescriptionsItem label="用户名">{{ currentUser.username }}</ElDescriptionsItem>
        <ElDescriptionsItem label="昵称">{{ currentUser.nickname }}</ElDescriptionsItem>
        <ElDescriptionsItem label="手机号">{{ currentUser.phone }}</ElDescriptionsItem>
        <ElDescriptionsItem label="邮箱">{{ currentUser.email }}</ElDescriptionsItem>
        <ElDescriptionsItem label="性别">
          {{ currentUser.gender === 1 ? '男' : currentUser.gender === 2 ? '女' : '保密' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          <ElTag :type="currentUser.status === 1 ? 'success' : 'danger'">
            {{ currentUser.status === 1 ? '启用' : '禁用' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="角色">
          {{ currentUser.role === 'ADMIN' ? '管理员' : '普通用户' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="注册时间" :span="2">{{
          currentUser.createTime
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="更新时间" :span="2">{{
          currentUser.updateTime
        }}</ElDescriptionsItem>
      </ElDescriptions>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { fetchGetUserList, fetchUpdateUserStatus } from '@/api/mall'

  defineOptions({ name: 'UserManage' })

  const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

  // 搜索表单
  const searchForm = reactive({
    keyword: '',
    status: undefined as number | undefined
  })

  // 分页
  const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
  })

  // 表格数据
  const loading = ref(false)
  const userList = ref<Api.MallUser.UserItem[]>([])

  // 详情弹窗
  const detailVisible = ref(false)
  const currentUser = ref<Api.MallUser.UserItem | null>(null)

  // 获取用户列表
  const getUserList = async () => {
    loading.value = true
    try {
      const res = await fetchGetUserList({
        current: pagination.current,
        size: pagination.size,
        keyword: searchForm.keyword || undefined,
        status: searchForm.status
      })
      userList.value = res.records
      pagination.total = res.total
    } catch (error) {
      console.error('获取用户列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 搜索
  const handleSearch = () => {
    pagination.current = 1
    getUserList()
  }

  // 重置
  const handleReset = () => {
    searchForm.keyword = ''
    searchForm.status = undefined
    pagination.current = 1
    getUserList()
  }

  // 状态变更
  const handleStatusChange = async (row: Api.MallUser.UserItem, val: number) => {
    try {
      await ElMessageBox.confirm(`确定要${val === 1 ? '启用' : '禁用'}该用户吗？`, '提示', {
        type: 'warning'
      })
      await fetchUpdateUserStatus({ id: row.id, status: val })
      ElMessage.success(`${val === 1 ? '启用' : '禁用'}成功`)
    } catch (error) {
      row.status = val === 1 ? 0 : 1
      if (error !== 'cancel') {
        ElMessage.error('操作失败')
      }
    }
  }

  // 查看详情
  const handleViewDetail = (row: Api.MallUser.UserItem) => {
    currentUser.value = row
    detailVisible.value = true
  }

  // 分页变化
  const handleSizeChange = (val: number) => {
    pagination.size = val
    getUserList()
  }

  const handleCurrentChange = (val: number) => {
    pagination.current = val
    getUserList()
  }

  onMounted(() => {
    getUserList()
  })
</script>

<style scoped>
  .user-manage-page {
    padding: 20px;
  }

  .search-card {
    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .table-card {
    :deep(.el-card__body) {
      padding: 20px;
    }
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

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .mr-1 {
    margin-right: 4px;
  }

  .mt-4 {
    margin-top: 16px;
  }
</style>
