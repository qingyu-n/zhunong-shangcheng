<template>
  <div class="message-center-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <h2>消息中心</h2>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0">
            <el-button type="primary" link @click="markAllRead">全部已读</el-button>
          </el-badge>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部消息" name="all" />
        <el-tab-pane label="未读消息" name="unread" />
        <el-tab-pane label="系统通知" name="system" />
        <el-tab-pane label="审核通知" name="audit" />
      </el-tabs>

      <div class="message-list" v-loading="loading">
        <div 
          v-for="message in filteredMessages" 
          :key="message.id" 
          class="message-item"
          :class="{ unread: message.isRead === 0 }"
          @click="handleReadMessage(message)"
        >
          <div class="message-icon" :type="getMessageType(message.type)">
            <el-icon v-if="message.type === 2"><Warning /></el-icon>
            <el-icon v-else><Bell /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-title">{{ message.title }}</div>
            <div class="message-text">{{ message.content }}</div>
            <div class="message-time">{{ formatTime(message.createTime) }}</div>
          </div>
          <div class="message-status">
            <el-tag v-if="message.isRead === 0" size="small">未读</el-tag>
            <el-tag v-else type="info" size="small">已读</el-tag>
          </div>
        </div>

        <el-empty v-if="!loading && filteredMessages.length === 0" description="暂无消息" />

        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="total"
            layout="total, prev, pager, next"
            @size-change="fetchMessages"
            @current-change="fetchMessages"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, Bell } from '@element-plus/icons-vue'
import { messageApi } from '@/api'

const loading = ref(false)
const messages = ref([])
const unreadCount = ref(0)
const activeTab = ref('all')
const retryCount = ref(0)
const maxRetries = 3

const pagination = reactive({
  current: 1,
  size: 15
})

const total = computed(() => messages.value.length)

const filteredMessages = computed(() => {
  switch (activeTab.value) {
    case 'unread':
      return messages.value.filter(m => m.isRead === 0)
    case 'system':
      return messages.value.filter(m => m.type === 1)
    case 'audit':
      return messages.value.filter(m => m.type === 2)
    default:
      return messages.value
  }
})

const fetchMessages = async () => {
  loading.value = true
  try {
    let params = { page: pagination.current, size: pagination.size }
    
    if (activeTab.value === 'unread') params.isRead = 0
    else if (activeTab.value === 'system') params.type = 1
    else if (activeTab.value === 'audit') params.type = 2

    const res = await messageApi.getMessageList(params)
    messages.value = res?.list || res?.records || res?.data?.records || []
    retryCount.value = 0
  } catch (error) {
    console.error('获取消息失败:', error)
    if (retryCount.value < maxRetries) {
      retryCount.value++
      ElMessage.warning(`获取消息失败，正在重试(${retryCount.value}/${maxRetries})...`)
      setTimeout(fetchMessages, 2000 * retryCount.value)
    } else {
      ElMessage.error('获取消息失败，请稍后再试')
    }
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await messageApi.getUnreadCount()
    unreadCount.value = res ?? res?.data ?? 0
  } catch (e) {
    console.error('获取未读数失败:', e)
  }
}

const handleTabChange = () => {
  pagination.current = 1
  fetchMessages()
}

const handleReadMessage = async (message) => {
  if (message.isRead === 0) {
    try {
      await messageApi.markAsRead(message.id)
      message.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
}

const markAllRead = async () => {
  try {
    await messageApi.markAllRead()
    messages.value.forEach(m => m.isRead = 1)
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const getMessageType = (type) => {
  return type === 2 ? 'warning' : 'primary'
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

let refreshTimer = null

onMounted(() => {
  fetchMessages()
  fetchUnreadCount()
  refreshTimer = setInterval(() => {
    fetchMessages()
    fetchUnreadCount()
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped lang="scss">
.message-center-page {
  max-width: 900px;
  margin: 20px auto;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h2 {
      margin: 0;
      font-size: 20px;
      color: #333;
    }
  }

  .message-list {
    min-height: 300px;
  }

  .message-item {
    display: flex;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover,
    &.unread {
      background-color: #fafafa;
    }

    &.unread {
      border-left: 3px solid #1890ff;
    }

    .message-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      flex-shrink: 0;

      &[type="primary"] {
        background-color: #e6f7ff;
        color: #1890ff;
      }

      &[type="warning"] {
        background-color: #fff7e6;
        color: #faad14;
      }
    }

    .message-content {
      flex: 1;
      min-width: 0;

      .message-title {
        font-weight: 500;
        color: #333;
        margin-bottom: 4px;
      }

      .message-text {
        color: #666;
        font-size: 13px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .message-time {
        color: #999;
        font-size: 12px;
        margin-top: 6px;
      }
    }

    .message-status {
      margin-left: 16px;
      flex-shrink: 0;
    }
  }

  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style>
