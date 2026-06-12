<template>
  <div class="product-reviews">
    <div class="reviews-header">
      <h3>商品评价 ({{ total }})</h3>
      <div class="rating-summary">
        <span class="average-rating">{{ averageRating }}</span>
        <el-rate v-model="averageRating" disabled />
      </div>
    </div>

    <!-- 评价统计 -->
    <div class="rating-stats" v-if="showStats">
      <div
        v-for="star in 5"
        :key="star"
        class="rating-bar-item"
      >
        <span class="star-label">{{ 6 - star }}星</span>
        <el-progress
          :percentage="getRatingPercentage(6 - star)"
          :color="'#faad14'"
          :stroke-width="8"
        />
        <span class="star-count">{{ getRatingCount(6 - star) }}</span>
      </div>
    </div>

    <!-- 写评价按钮 -->
    <div class="write-review-btn" v-if="canReview && !isFarmer">
      <el-button type="primary" @click="showReviewDialog = true">
        <el-icon><EditPen /></el-icon>
        写评价
      </el-button>
    </div>

    <!-- 评价列表 -->
    <div class="review-list" v-loading="loading">
      <div v-if="reviews.length === 0 && !loading" class="empty-reviews">
        <el-empty description="暂无评价" />
      </div>

      <div v-for="review in reviews" :key="review.id" class="review-item">
        <div class="review-header">
          <el-avatar :size="40" :src="review.userAvatar || undefined">
            {{ review.userName?.charAt(0) }}
          </el-avatar>
          <div class="user-info">
            <div class="username">{{ review.userName || '匿名用户' }}</div>
            <el-rate v-model="review.rating" disabled size="small" />
          </div>
          <div class="review-time">{{ formatTime(review.createTime) }}</div>
        </div>

        <div class="review-content" v-if="review.content">
          {{ review.content }}
        </div>

        <!-- 评价图片 -->
        <div class="review-images" v-if="review.images && review.images.length > 0">
          <el-image
            v-for="(img, index) in review.images"
            :key="index"
            :src="img"
            :preview-src-list="review.images"
            fit="cover"
            class="review-image"
          />
        </div>

        <!-- 农户回复 -->
        <div class="farmer-reply" v-if="review.replyContent">
          <div class="reply-header">
            <el-tag type="success" size="small">卖家回复</el-tag>
            <span class="reply-time">{{ formatTime(review.replyTime) }}</span>
          </div>
          <div class="reply-content">{{ review.replyContent }}</div>
        </div>

        <!-- 回复按钮(农户可见) -->
        <div class="reply-btn" v-if="isFarmer && !review.replyContent">
          <el-button type="primary" link size="small" @click="openReplyDialog(review)">
            回复评价
          </el-button>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchReviews"
          @current-change="fetchReviews"
        />
      </div>
    </div>

    <!-- 写评价弹窗 -->
    <el-dialog v-model="showReviewDialog" title="写评价" width="600px" destroy-on-close>
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="80px">
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="reviewForm.rating" show-text :texts="['很差', '较差', '一般', '推荐', '非常推荐']" />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="5"
            placeholder="分享您的使用体验，帮助其他买家做出选择"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="上传图片">
          <el-upload
            action="#"
            :http-request="handleUploadImage"
            list-type="picture-card"
            :limit="5"
            :file-list="imageList"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传5张图片，每张不超过5MB</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submitting">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- 回复评价弹窗 -->
    <el-dialog v-model="showReplyDialog" title="回复评价" width="500px" destroy-on-close>
      <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="80px">
        <el-form-item label="回复内容" prop="content">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容，感谢用户的评价"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReplyDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replying">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, Plus } from '@element-plus/icons-vue'
import { reviewApi } from '@/api'

const props = defineProps({
  productId: {
    type: [Number, String],
    required: true
  },
  canReview: {
    type: Boolean,
    default: false
  },
  isFarmer: {
    type: Boolean,
    default: false
  },
  showStats: {
    type: Boolean,
    default: true
  }
})

const loading = ref(false)
const submitting = ref(false)
const replying = ref(false)
const reviews = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const averageRating = ref(0)

const showReviewDialog = ref(false)
const showReplyDialog = ref(false)
const currentReview = ref(null)
const imageList = ref([])

const reviewFormRef = ref()
const replyFormRef = ref()

const reviewForm = reactive({
  rating: 5,
  content: '',
  images: []
})

const replyForm = reactive({
  content: ''
})

const reviewRules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
}

const replyRules = {
  content: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

const fetchReviews = async () => {
  loading.value = true
  try {
    const res = await reviewApi.getReviews(props.productId, {
      page: currentPage.value,
      size: pageSize.value
    })
    reviews.value = res.records || []
    total.value = res.total || 0
    if (res.averageRating !== undefined) {
      averageRating.value = res.averageRating
    }
  } catch (error) {
    console.error('获取评价列表失败:', error)
    ElMessage.error('获取评价列表失败')
  } finally {
    loading.value = false
  }
}

const getRatingPercentage = (star) => {
  const count = getRatingCount(star)
  const totalRatings = reviews.value.length
  return totalRatings > 0 ? Math.round((count / totalRatings) * 100) : 0
}

const getRatingCount = (star) => {
  return reviews.value.filter(r => r.rating === star).length
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 2592000000) return `${Math.floor(diff / 86400000)}天前`

  return time.split('T')[0]
}

const handleUploadImage = async (options) => {
  try {
    // 这里应该调用七牛云上传,暂时使用本地URL
    const url = URL.createObjectURL(options.file)
    reviewForm.images.push(url)
    imageList.value.push({ name: options.file.name, url })
  } catch (error) {
    ElMessage.error('图片上传失败')
  }
}

const submitReview = async () => {
  await reviewFormRef.value.validate()
  submitting.value = true
  try {
    await reviewApi.createReview(props.productId, reviewForm)
    ElMessage.success('评价提交成功')
    showReviewDialog.value = false
    Object.assign(reviewForm, { rating: 5, content: '', images: [] })
    imageList.value = []
    fetchReviews()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const openReplyDialog = (review) => {
  currentReview.value = review
  replyForm.content = ''
  showReplyDialog.value = true
}

const submitReply = async () => {
  await replyFormRef.value.validate()
  replying.value = true
  try {
    await reviewApi.replyReview(currentReview.value.id, replyForm)
    ElMessage.success('回复成功')
    showReplyDialog.value = false
    fetchReviews()
  } catch (error) {
    ElMessage.error(error.message || '回复失败')
  } finally {
    replying.value = false
  }
}

onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.product-reviews {
  margin-top: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.reviews-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.rating-summary {
  display: flex;
  align-items: center;
  gap: 10px;
}

.average-rating {
  font-size: 24px;
  font-weight: bold;
  color: #faad14;
}

.rating-stats {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.rating-bar-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.star-label {
  width: 40px;
  font-size: 13px;
  color: #666;
}

.star-count {
  width: 40px;
  text-align: right;
  font-size: 13px;
  color: #999;
}

.write-review-btn {
  text-align: right;
  padding: 15px 0;
}

.review-list {
  min-height: 200px;
}

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  flex: 1;
  margin-left: 12px;
}

.username {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.review-time {
  font-size: 12px;
  color: #999;
}

.review-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
  padding-left: 52px;
}

.review-images {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  padding-left: 52px;
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.farmer-reply {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 4px;
  padding: 12px;
  margin-top: 12px;
  margin-left: 52px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.reply-time {
  font-size: 12px;
  color: #999;
}

.reply-content {
  font-size: 13px;
  color: #555;
  line-height: 1.6;
}

.reply-btn {
  text-align: right;
  margin-top: 8px;
  padding-left: 52px;
}

.pagination-wrapper {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.upload-tip {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
}
</style>
