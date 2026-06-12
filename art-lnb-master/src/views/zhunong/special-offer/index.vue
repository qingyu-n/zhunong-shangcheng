<template>
  <div class="special-offer-page">
    <ArtPageWrapper>
      <ArtCard title="限时特惠商品管理">
        <div class="toolbar">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入商品名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
          <el-select
            v-model="searchForm.status"
            placeholder="活动状态"
            clearable
            style="width: 130px"
          >
            <el-option label="进行中" :value="1" />
            <el-option label="未开始" :value="0" />
            <el-option label="已结束" :value="2" />
          </el-select>
          <el-button type="primary" @click="handleSearch">
            <i class="ri-search-line"></i> 搜索
          </el-button>
          <el-button type="danger" @click="handleAdd">
            <i class="ri-add-circle-line"></i> 新增特惠
          </el-button>
        </div>

        <el-table :data="offerList" v-loading="loading" @selection-change="handleSelectionChange" stripe>
          <el-table-column type="selection" width="50" />
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column label="商品" min-width="220">
            <template #default="{ row }">
              <div class="product-cell">
                <el-image
                  :src="row.image || '/placeholder.png'"
                  style="width: 50px; height: 50px; border-radius: 6px; flex-shrink: 0"
                  fit="cover"
                />
                <span class="product-name">{{ row.name || '未命名商品' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="价格信息" width="150" align="center">
            <template #default="{ row }">
              <div class="price-info">
                <span class="orig">¥{{ row.originalPrice }}</span>
                <span class="disc">¥{{ row.discountPrice }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="优惠" width="80" align="center">
            <template #default="{ row }">
              <el-tag
                :type="getDiscountTagType(row.discountPercent)"
                size="small"
                effect="dark"
                round
              >
                {{ row.discountPercent }}折
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="活动时间" width="170">
            <template #default="{ row }">
              <div class="time-info">
                <i class="ri-time-line"></i>
                {{ formatTime(row.startTime) }}
                <br />
                <i class="ri-stop-circle-line"></i>
                {{ formatTime(row.endTime) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row)" size="small" effect="light" round>
                {{ getStatusText(row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="65" align="center" />
          <el-table-column label="操作" width="160" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEdit(row)">
                <i class="ri-edit-line"></i> 编辑
              </el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">
                <i class="ri-delete-bin-line"></i> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>

        <div class="batch-actions" v-if="selectedRows.length > 0">
          <el-alert
            :title="`已选择 ${selectedRows.length} 项`"
            type="info"
            :closable="false"
            show-icon
            style="flex: 1"
          >
            <template #default>
              <el-button type="danger" size="small" @click="handleBatchDelete">批量删除</el-button>
            </template>
          </el-alert>
        </div>
      </ArtCard>
    </ArtPageWrapper>

    <el-dialog
      v-model="dialogVisible"
      width="760px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-title">
          {{ isEdit ? '编辑特惠商品' : '新增特惠商品' }}
        </div>
      </template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="offer-form"
      >
        <!-- 基本信息 -->
        <div class="form-section-title">
          <i class="ri-information-line"></i> 基本信息
        </div>
        <el-row :gutter="24">
          <el-col :span="14">
            <el-form-item label="关联商品" prop="productId">
              <el-select
                id="productId"
                v-model="form.productId"
                filterable
                remote
                reserve-keyword
                placeholder="输入关键词搜索商品..."
                :remote-method="searchProducts"
                :loading="searchLoading"
                style="width: 100%"
                :disabled="isEdit"
                @change="onProductSelect"
              >
                <el-option
                  v-for="item in productOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                  <div style="display: flex; justify-content: space-between; align-items: center">
                    <span>{{ item.name }}</span>
                    <el-tag size="small" type="info">¥{{ item.price }}</el-tag>
                  </div>
                </el-option>
              </el-select>
              <div class="form-tip" v-if="!isEdit">选择商品后自动填充名称和原价</div>
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="特惠名称" prop="name">
              <el-input id="name" v-model="form.name" placeholder="如：限时秒杀、超值特惠" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 价格设置 -->
        <div class="form-section-title">
          <i class="ri-money-dollar-circle-line"></i> 价格设置
        </div>
        <el-row :gutter="24" align="middle">
          <el-col :span="8">
            <el-form-item label="原价 (¥)" prop="originalPrice">
              <el-input-number
                id="originalPrice"
                v-model="form.originalPrice"
                :min="0"
                :precision="2"
                :step="1"
                disabled
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="2" style="text-align: center; padding-top: 30px">
            <i class="ri-arrow-right-line" style="font-size: 18px; color: #c0c4cc"></i>
          </el-col>
          <el-col :span="8">
            <el-form-item label="特惠价 (¥)" prop="discountPrice">
              <el-input-number
                id="discountPrice"
                v-model="form.discountPrice"
                :min="0"
                :precision="2"
                :step="1"
                controls-position="right"
                style="width: 100%"
                @change="calcDiscount"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="折扣率">
              <div class="discount-preview" :class="{ 'good-deal': form.discountPercent && form.discountPercent <= 70 }">
                <span class="percent">{{ form.discountPercent || '-' }}折</span>
                <span class="save" v-if="form.originalPrice && form.discountPrice">
                  省¥{{ (form.originalPrice - form.discountPrice).toFixed(2) }}
                </span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 时间设置 -->
        <div class="form-section-title">
          <i class="ri-calendar-line"></i> 活动时间
        </div>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                id="startTime"
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                id="endTime"
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="24">
            <div class="quick-time-btns">
              <span class="quick-label">快捷设置：</span>
              <el-button size="small" @click="setQuickTime(1)">今天</el-button>
              <el-button size="small" @click="setQuickTime(3)">3天</el-button>
              <el-button size="small" @click="setQuickTime(7)">一周</el-button>
              <el-button size="small" @click="setQuickTime(30)">一个月</el-button>
            </div>
          </el-col>
        </el-row>

        <!-- 其他设置 -->
        <div class="form-section-title">
          <i class="ri-settings-3-line"></i> 其他设置
        </div>
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="活动描述">
              <el-input
                id="description"
                v-model="form.description"
                type="textarea"
                :rows="3"
                placeholder="输入活动描述，将展示在商城端..."
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="排序值">
              <el-input-number
                id="sortOrder"
                v-model="form.sortOrder"
                :min="0"
                :max="999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group id="status" v-model="form.status">
                <el-radio-button :label="1">启用</el-radio-button>
                <el-radio-button :label="0">禁用</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="submitLoading"
            @click="handleSubmit"
          >
            <i :class="isEdit ? 'ri-save-line' : 'ri-add-circle-line'" style="margin-right: 4px"></i>
            {{ isEdit ? '保存修改' : '创建特惠' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const searchLoading = ref(false)
const submitLoading = ref(false)
const offerList = ref<any[]>([])
const selectedRows = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const productOptions = ref<any[]>([])

const searchForm = reactive({
  keyword: '',
  status: undefined as number | undefined
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: undefined as number | undefined,
  productId: undefined as number | undefined,
  name: '',
  originalPrice: 0,
  discountPrice: 0,
  discountPercent: 0,
  startTime: '',
  endTime: '',
  description: '',
  sortOrder: 0,
  status: 1
})

const rules: FormRules = {
  productId: [{ required: true, message: '请选择关联商品', trigger: 'change' }],
  name: [
    { required: true, message: '请输入特惠名称', trigger: 'blur' },
    { max: 50, message: '名称不能超过50个字符', trigger: 'blur' }
  ],
  discountPrice: [
    { required: true, message: '请输入特惠价格', trigger: 'blur' },
    {
      validator: (_rule: any, value: number, callback: Function) => {
        if (!value || value <= 0) callback(new Error('特惠价必须大于0'))
        else if (value >= form.originalPrice) callback(new Error('特惠价必须小于原价'))
        else callback()
      },
      trigger: 'blur'
    }
  ],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (form.startTime && value && new Date(value) <= new Date(form.startTime)) {
          callback(new Error('结束时间必须晚于开始时间'))
        } else callback()
      },
      trigger: 'change'
    }
  ]
}

const fetchOfferList = async () => {
  loading.value = true
  try {
    const data = await adminApi.getSpecialOfferList({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status
    })
    if (data) {
      offerList.value = (data as any).list || (data as any).records || []
      pagination.total = (data as any).total || 0
    }
  } catch {
    console.error('获取限时特惠列表失败')
  } finally {
    loading.value = false
  }
}

const searchProducts = async (query: string) => {
  if (!query || query.length < 2) {
    productOptions.value = []
    return
  }
  searchLoading.value = true
  try {
    const data = await adminApi.getProductList({ current: 1, size: 20, name: query })
    productOptions.value = (data as any)?.list || (data as any)?.records || []
  } catch {
    console.error('搜索商品失败')
  } finally {
    searchLoading.value = false
  }
}

const onProductSelect = (val: number | undefined) => {
  if (!val) return
  const item = productOptions.value.find((p: any) => p.id === val)
  if (item) {
    if (!form.name) form.name = item.name + '-特惠'
    form.originalPrice = Number(item.price) || 0
    if (form.discountPrice === 0) form.discountPrice = Math.round(form.originalPrice * 0.7 * 100) / 100
    calcDiscount()
  }
}

const calcDiscount = () => {
  if (form.originalPrice > 0 && form.discountPrice > 0) {
    form.discountPercent = Math.round((form.discountPrice / form.originalPrice) * 10)
  }
}

const setQuickTime = (days: number) => {
  const now = new Date()
  form.startTime = formatDate(now)
  const end = new Date(now.getTime() + days * 24 * 60 * 60 * 1000)
  form.endTime = formatDate(end)
}

const formatDate = (d: Date): string => {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const handleSearch = () => {
  pagination.current = 1
  fetchOfferList()
}

const handleSizeChange = (val: number) => {
  pagination.size = val
  fetchOfferList()
}

const handleCurrentChange = (val: number) => {
  pagination.current = val
  fetchOfferList()
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const getDiscountTagType = (percent: number): 'info' | 'danger' | 'warning' | 'success' => {
  if (!percent) return 'info'
  if (percent <= 50) return 'danger'
  if (percent <= 70) return 'warning'
  return 'success'
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

const getStatusType = (row: any): 'info' | 'warning' | 'danger' | 'success' => {
  const now = new Date()
  const start = new Date(row.startTime)
  const end = new Date(row.endTime)
  if (row.status === 0) return 'info'
  if (now < start) return 'warning'
  if (now > end) return 'danger'
  return 'success'
}

const getStatusText = (row: any): string => {
  const now = new Date()
  const start = new Date(row.startTime)
  const end = new Date(row.endTime)
  if (row.status === 0) return '已禁用'
  if (now < start) return '未开始'
  if (now > end) return '已结束'
  return '进行中'
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  form.id = row.id
  form.productId = row.productId
  form.name = row.name || ''
  form.originalPrice = row.originalPrice || 0
  form.discountPrice = row.discountPrice || 0
  form.discountPercent = row.discountPercent || 0
  form.startTime = row.startTime ? row.startTime.replace('T', ' ') : ''
  form.endTime = row.endTime ? row.endTime.replace('T', ' ') : ''
  form.description = row.description || ''
  form.sortOrder = row.sortOrder || 0
  form.status = row.status ?? 1
  isEdit.value = true
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除"${row.name}"吗？`, '确认删除', { type: 'warning' })
    await adminApi.deleteSpecialOffer(row.id)
    ElMessage.success('删除成功')
    fetchOfferList()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error?.message || '删除失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个特惠商品吗？`,
      '批量删除',
      { type: 'warning' }
    )
    for (const row of selectedRows.value) {
      await adminApi.deleteSpecialOffer(row.id)
    }
    ElMessage.success(`成功删除 ${selectedRows.value.length} 个`)
    fetchOfferList()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error?.message || '批量删除失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const submitData: any = {
        productId: form.productId,
        name: form.name,
        originalPrice: form.originalPrice,
        discountPrice: form.discountPrice,
        discountPercent: form.discountPercent,
        startTime: form.startTime || '',
        endTime: form.endTime || '',
        description: form.description || '',
        sortOrder: form.sortOrder || 0,
        status: form.status ?? 1
      }

      if (isEdit.value && form.id) {
        await adminApi.updateSpecialOffer(form.id, submitData)
        ElMessage.success({ message: '修改成功', grouping: true })
      } else {
        await adminApi.createSpecialOffer(submitData)
        ElMessage.success({ message: '创建成功', grouping: true })
      }
      dialogVisible.value = false
      fetchOfferList()
    } catch (error: any) {
      ElMessage.error(error?.message || '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const resetForm = () => {
  form.id = undefined
  form.productId = undefined
  form.name = ''
  form.originalPrice = 0
  form.discountPrice = 0
  form.discountPercent = 0
  form.startTime = ''
  form.endTime = ''
  form.description = ''
  form.sortOrder = 0
  form.status = 1
  formRef.value?.resetFields()
}

onMounted(() => {
  fetchOfferList()
})
</script>

<style scoped lang="scss">
.special-offer-page {
  .toolbar {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    flex-wrap: wrap;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .batch-actions {
    margin-top: 16px;

    .el-alert {
      padding: 8px 16px;
    }
  }

  .product-cell {
    display: flex;
    align-items: center;
    gap: 10px;

    .product-name {
      font-weight: 500;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .price-info {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .orig {
      color: #909399;
      text-decoration: line-through;
      font-size: 12px;
    }

    .disc {
      color: #f56c6c;
      font-weight: 700;
      font-size: 14px;
    }
  }

  .time-info {
    font-size: 12px;
    line-height: 1.6;
    color: #606266;

    i {
      margin-right: 4px;
      color: #909399;
    }
  }

  .dialog-title {
    text-align: center;
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    width: 100%;
  }

  .offer-form {
    .form-section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 16px;
      padding-bottom: 8px;
      border-bottom: 2px solid #e4e7ed;
      i {
        color: #409eff;
        font-size: 18px;
      }
    }

    .form-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }

    .discount-preview {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 10px 16px;
      background: linear-gradient(135deg, #fff5f5 0%, #ffe4e4 100%);
      border-radius: 10px;
      border: 1px solid #fde2e2;

      &.good-deal {
        background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
        border-color: #e1f3d8;
      }

      .percent {
        font-size: 22px;
        font-weight: 700;
        color: #f56c6c;

        .good-deal & {
          color: #67c23a;
        }
      }

      .save {
        font-size: 12px;
        color: #67c23a;
        margin-top: 2px;
      }
    }

    .quick-time-btns {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 0;

      .quick-label {
        font-size: 13px;
        color: #909399;
        white-space: nowrap;
      }
    }

    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
  }
}
</style>
