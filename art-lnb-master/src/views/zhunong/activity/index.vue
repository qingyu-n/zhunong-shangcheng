<template>
  <div class="activity-page">
    <ArtPageWrapper>
      <ArtCard title="助农活动管理">
        <div class="toolbar">
          <el-button type="primary" @click="handleAdd"
            ><i class="ri-add-line"></i> 新增活动</el-button
          >
        </div>

        <el-table :data="activityList" v-loading="loading">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="title" label="活动标题" min-width="200" />
          <el-table-column prop="startTime" label="开始时间" width="160" />
          <el-table-column prop="endTime" label="结束时间" width="160" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '进行中' : '已结束' }}
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

        <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑活动' : '新增活动'" width="600px">
          <el-form :model="form" label-width="100px">
            <el-form-item label="活动标题" required>
              <el-input v-model="form.title" placeholder="请输入活动标题" />
            </el-form-item>
            <el-form-item label="开始时间" required>
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item label="结束时间" required>
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item label="状态" required>
              <el-radio-group v-model="form.status">
                <el-radio :label="1">进行中</el-radio>
                <el-radio :label="0">已结束</el-radio>
              </el-radio-group>
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
  const activityList = ref<any[]>([])
  const dialogVisible = ref(false)
  const isEdit = ref(false)

  const form = reactive({
    id: undefined as number | undefined,
    title: '',
    startTime: '',
    endTime: '',
    status: 1
  })

  const fetchActivityList = async () => {
    loading.value = true
    try {
      const data = await adminApi.getActivityList()
      if (data) {
        activityList.value = (data as any).list || (data as any).records || data || []
      }
    } catch (error) {
      console.error('获取活动列表失败:', error)
      ElMessage.error('获取活动列表失败')
    } finally {
      loading.value = false
    }
  }

  const handleAdd = () => {
    form.id = undefined
    form.title = ''
    form.startTime = ''
    form.endTime = ''
    form.status = 1
    isEdit.value = false
    dialogVisible.value = true
  }

  const handleEdit = (row: any) => {
    form.id = row.id
    form.title = row.title
    form.startTime = row.startTime
    form.endTime = row.endTime
    form.status = row.status
    isEdit.value = true
    dialogVisible.value = true
  }

  const handleDelete = async (row: any) => {
    try {
      await ElMessageBox.confirm('确定要删除该活动吗？', '提示', {
        type: 'warning'
      })
      await adminApi.deleteActivity(row.id)
      ElMessage.success('删除成功')
      fetchActivityList()
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('删除活动失败:', error)
        ElMessage.error(error.message || '删除活动失败')
      }
    }
  }

  const handleSubmit = async () => {
    if (!form.title || !form.startTime || !form.endTime) {
      ElMessage.warning('请填写完整信息')
      return
    }

    try {
      // 清理数据，只提交必要字段，将Date转为字符串
      const submitData: any = {
        title: form.title,
        status: form.status,
        // 将Date对象转为格式化字符串
        startTime: form.startTime
          ? new Date(form.startTime).toISOString().slice(0, 19).replace('T', ' ')
          : '',
        endTime: form.endTime
          ? new Date(form.endTime).toISOString().slice(0, 19).replace('T', ' ')
          : ''
      }

      if (isEdit.value) {
        await adminApi.updateActivity(form.id!, submitData)
        ElMessage.success('编辑成功')
      } else {
        await adminApi.createActivity(submitData)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchActivityList()
    } catch (error: any) {
      console.error('提交失败:', error)
      ElMessage.error(error?.message || '操作失败')
    }
  }

  onMounted(() => {
    fetchActivityList()
  })
</script>

<style scoped lang="scss">
  .activity-page {
    .toolbar {
      margin-bottom: 20px;
    }
  }
</style>
