<template>
  <div class="address-manage-page">
    <!-- 顶部导航栏 -->
    <nav class="top-nav">
      <div class="nav-container">
        <a href="#" class="logo" @click.prevent="goHome">
          <i class="fas fa-apple-alt logo-icon"></i>
          <span class="logo-text">农鲜达</span>
        </a>
        <div class="nav-links">
          <a href="#" class="nav-link" @click.prevent="goHome">首页</a>
          <a href="#" class="nav-link" @click.prevent="goProducts">商品</a>
          <a href="#" class="nav-link" @click.prevent="goHelp">助农</a>
          <div class="user-menu">
            <i class="fas fa-user-circle user-icon" @click="goUserCenter"></i>
          </div>
        </div>
      </div>
    </nav>

    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-container">
        <h1 class="page-title">收货地址管理</h1>
        <button class="add-address-btn" @click="showAddModal">
          <i class="fas fa-plus"></i>
          <span>新增收货地址</span>
        </button>
      </div>
    </div>

    <!-- 地址列表区 -->
    <div class="address-section" v-loading="loading">
      <div class="section-container">
        <h2 class="section-title">我的收货地址</h2>
        <div class="address-grid" v-if="addressList.length > 0">
          <div
            v-for="address in addressList"
            :key="address.id"
            class="address-card"
          >
            <div class="card-header">
              <h3 class="address-tag">{{ address.tag }}</h3>
              <span v-if="address.isDefault" class="default-badge">
                <i class="fas fa-check-circle"></i>
                默认
              </span>
            </div>
            <div class="address-info">
              <p class="user-info">{{ address.name }} {{ address.phone }}</p>
              <p class="address-line1">{{ address.addressLine1 }}</p>
              <p class="address-line2">{{ address.detail || (address.province + address.city + address.district + address.address) }} 邮编:{{ address.zipCode }}</p>
            </div>
            <div class="card-actions">
              <button class="action-btn edit" @click="editAddress(address)">
                <i class="fas fa-pencil-alt"></i>
              </button>
              <button class="action-btn delete" @click="confirmDelete(address)">
                <i class="fas fa-trash-alt"></i>
              </button>
              <button
                v-if="!address.isDefault"
                class="set-default-btn"
                @click="setDefault(address)"
              >
                设为默认
              </button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && addressList.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="fas fa-map-marker-alt"></i>
          </div>
          <h3 class="empty-title">暂无收货地址</h3>
          <p class="empty-desc">您还没有保存任何收货地址，请添加一个收货地址以便顺利完成订单</p>
          <button class="add-address-btn-large" @click="showAddModal">
            <i class="fas fa-plus"></i>
            <span>新增收货地址</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 地址表单弹窗 -->
    <div class="modal-overlay" v-if="showModal" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3 class="modal-title">{{ isEditing ? '编辑收货地址' : '新增收货地址' }}</h3>
          <button class="close-btn" @click="closeModal">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">收货人</label>
            <input
              type="text"
              v-model="form.name"
              class="form-input"
              placeholder="请输入收货人姓名"
            />
          </div>
          <div class="form-group">
            <label class="form-label">联系电话</label>
            <input
              type="tel"
              v-model="form.phone"
              class="form-input"
              placeholder="请输入联系电话"
            />
          </div>
          <div class="form-group">
            <label class="form-label">所在地区</label>
            <div class="region-selects">
              <select v-model="selectedProvince" class="region-select-input" @change="handleProvinceChange">
                <option value="">请选择省份</option>
                <option v-for="p in provinces" :key="p.value" :value="p.value">{{ p.label }}</option>
              </select>
              <select v-model="selectedCity" class="region-select-input" @change="handleCityChange" :disabled="!selectedProvince">
                <option value="">请选择城市</option>
                <option v-for="c in cities" :key="c.value" :value="c.value">{{ c.label }}</option>
              </select>
              <select v-model="selectedDistrict" class="region-select-input" @change="updateRegionText" :disabled="!selectedCity">
                <option value="">请选择区县</option>
                <option v-for="d in districts" :key="d.value" :value="d.value">{{ d.label }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">详细地址</label>
            <input
              type="text"
              v-model="form.detail"
              class="form-input"
              placeholder="请输入详细地址信息"
            />
          </div>
          <div class="form-group">
            <label class="form-label">邮政编码</label>
            <input
              type="text"
              v-model="form.zipCode"
              class="form-input"
              placeholder="请输入邮政编码（选填）"
            />
          </div>
          <div class="form-group checkbox-group">
            <input
              type="checkbox"
              id="default-address"
              v-model="form.isDefault"
              class="form-checkbox"
            />
            <label for="default-address" class="checkbox-label">设为默认收货地址</label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeModal">取消</button>
          <button class="save-btn" @click="saveAddress">保存地址</button>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div class="modal-overlay" v-if="showDeleteModal" @click.self="closeDeleteModal">
      <div class="modal-content delete-modal">
        <div class="modal-body text-center">
          <h3 class="modal-title">确认删除</h3>
          <p class="delete-desc">您确定要删除这个收货地址吗？删除后将无法恢复。</p>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeDeleteModal">取消</button>
          <button class="delete-confirm-btn" @click="confirmDeleteAction">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { addressApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 地址列表 - 从API获取
const addressList = ref([])

// 获取地址列表
const fetchAddresses = async () => {
  // 检查是否已登录
  const token = localStorage.getItem('token')
  if (!token) {
    console.warn('⚠️ 未登录，无法获取地址列表')
    addressList.value = []
    return
  }

  loading.value = true
  try {
    console.log('获取地址列表，Token:', token.substring(0, 30) + '...')
    const res = await addressApi.getAddressList()
    console.log('地址列表数据:', res)
    addressList.value = Array.isArray(res) ? res : (res?.list || res?.records || [])
  } catch (error) {
    console.error('获取地址列表失败:', error)
    if (error.message?.includes('401') || error.message?.includes('未登录')) {
      ElMessage.warning('请先登录后再查看地址')
      router.push('/login')
    } else {
      ElMessage.error('获取地址失败: ' + (error.message || '网络错误'))
    }
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchAddresses()
})

// 弹窗状态
const showModal = ref(false)
const showDeleteModal = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const deleteId = ref(null)

// 表单数据
const form = reactive({
  name: '',
  phone: '',
  region: '',
  detail: '',
  zipCode: '',
  isDefault: false
})

// 显示新增弹窗
const showAddModal = () => {
  isEditing.value = false
  editingId.value = null
  resetForm()
  showModal.value = true
}

// 编辑地址
const editAddress = (address) => {
  isEditing.value = true
  editingId.value = address.id
  // 预填表单
  form.name = address.name || ''
  form.phone = address.phone || ''
  // 设置地区选择器
  if (address.province) {
    selectedProvince.value = address.province
    handleProvinceChange(true)
    if (address.city) {
      selectedCity.value = address.city
      handleCityChange(true)
      if (address.district) {
        selectedDistrict.value = address.district
        updateRegionText()
      }
    }
  } else {
    form.region = (address.province || '') + ' ' + (address.city || '') + ' ' + (address.district || '')
  }
  form.detail = address.detailAddress || address.detail || ''
  form.zipCode = address.zipCode || ''
  form.isDefault = address.isDefault === 1 || address.isDefault === true
  showModal.value = true
}
// 确认删除（显示删除确认弹窗）
const confirmDelete = (address) => {
  deleteId.value = address.id
  showDeleteModal.value = true
}

// 执行删除操作
const confirmDeleteAction = async () => {
  try {
    await addressApi.deleteAddress(deleteId.value)
    ElMessage.success('删除成功')
    showDeleteModal.value = false
    await fetchAddresses()
  } catch (error) {
    console.error('删除地址失败:', error)
    ElMessage.error('删除失败: ' + (error.message || '网络错误'))
  }
}

// 设置默认地址
const setDefault = async (address) => {
  try {
    await addressApi.updateAddress({
      ...address,
      isDefault: true
    })
    // 更新本地状态：将所有地址设为非默认，当前地址设为默认
    addressList.value.forEach(addr => {
      addr.isDefault = addr.id === address.id
    })
    ElMessage.success('已设为默认地址')
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置失败')
  }
}

// 重置表单
const resetForm = () => {
  form.name = ''
  form.phone = ''
  form.region = ''
  form.detail = ''
  form.zipCode = ''
  form.isDefault = false
  // 重置地区选择器
  selectedProvince.value = ''
  selectedCity.value = ''
  selectedDistrict.value = ''
  cities.value = []
  districts.value = []
}

// 关闭弹窗
const closeModal = () => {
  showModal.value = false
  resetForm()
}

// 地区选择器数据
const selectedProvince = ref('')
const selectedCity = ref('')
const selectedDistrict = ref('')

// 省市区数据（精简版常用地区）
const provinces = [
  { value: '北京市', label: '北京市' },
  { value: '上海市', label: '上海市' },
  { value: '广东省', label: '广东省' },
  { value: '浙江省', label: '浙江省' },
  { value: '江苏省', label: '江苏省' },
  { value: '四川省', label: '四川省' },
  { value: '湖北省', label: '湖北省' },
  { value: '湖南省', label: '湖南省' },
  { value: '河南省', label: '河南省' },
  { value: '山东省', label: '山东省' }
]

const cityData = {
  '北京市': [
    { value: '北京市', label: '北京市' }
  ],
  '上海市': [
    { value: '上海市', label: '上海市' }
  ],
  '广东省': [
    { value: '广州市', label: '广州市' },
    { value: '深圳市', label: '深圳市' },
    { value: '东莞市', label: '东莞市' },
    { value: '佛山市', label: '佛山市' },
    { value: '珠海市', label: '珠海市' }
  ],
  '浙江省': [
    { value: '杭州市', label: '杭州市' },
    { value: '宁波市', label: '宁波市' },
    { value: '温州市', label: '温州市' },
    { value: '绍兴市', label: '绍兴市' }
  ],
  '江苏省': [
    { value: '南京市', label: '南京市' },
    { value: '苏州市', label: '苏州市' },
    { value: '无锡市', label: '无锡市' },
    { value: '常州市', label: '常州市' }
  ],
  '四川省': [
    { value: '成都市', label: '成都市' },
    { value: '绵阳市', label: '绵阳市' },
    { value: '德阳市', label: '德阳市' }
  ],
  '湖北省': [
    { value: '武汉市', label: '武汉市' },
    { value: '宜昌市', label: '宜昌市' },
    { value: '襄阳市', label: '襄阳市' }
  ],
  '湖南省': [
    { value: '长沙市', label: '长沙市' },
    { value: '株洲市', label: '株洲市' },
    { value: '湘潭市', label: '湘潭市' }
  ],
  '河南省': [
    { value: '郑州市', label: '郑州市' },
    { value: '洛阳市', label: '洛阳市' },
    { value: '开封市', label: '开封市' }
  ],
  '山东省': [
    { value: '济南市', label: '济南市' },
    { value: '青岛市', label: '青岛市' },
    { value: '烟台市', label: '烟台市' }
  ]
}

const districtData = {
  '北京市': [
    { value: '东城区', label: '东城区' },
    { value: '西城区', label: '西城区' },
    { value: '朝阳区', label: '朝阳区' },
    { value: '海淀区', label: '海淀区' },
    { value: '丰台区', label: '丰台区' }
  ],
  '上海市': [
    { value: '黄浦区', label: '黄浦区' },
    { value: '徐汇区', label: '徐汇区' },
    { value: '浦东新区', label: '浦东新区' },
    { value: '静安区', label: '静安区' },
    { value: '闵行区', label: '闵行区' }
  ],
  '广州市': [
    { value: '天河区', label: '天河区' },
    { value: '越秀区', label: '越秀区' },
    { value: '海珠区', label: '海珠区' },
    { value: '番禺区', label: '番禺区' }
  ],
  '深圳市': [
    { value: '南山区', label: '南山区' },
    { value: '福田区', label: '福田区' },
    { value: '罗湖区', label: '罗湖区' },
    { value: '宝安区', label: '宝安区' }
  ],
  '东莞市': [
    { value: '莞城区', label: '莞城区' },
    { value: '南城区', label: '南城区' },
    { value: '东城区', label: '东城区' }
  ],
  '佛山市': [
    { value: '禅城区', label: '禅城区' },
    { value: '南海区', label: '南海区' },
    { value: '顺德区', label: '顺德区' }
  ],
  '珠海市': [
    { value: '香洲区', label: '香洲区' },
    { value: '斗门区', label: '斗门区' }
  ],
  '杭州市': [
    { value: '西湖区', label: '西湖区' },
    { value: '上城区', label: '上城区' },
    { value: '余杭区', label: '余杭区' },
    { value: '萧山区', label: '萧山区' }
  ],
  '宁波市': [
    { value: '海曙区', label: '海曙区' },
    { value: '江北区', label: '江北区' },
    { value: '鄞州区', label: '鄞州区' }
  ],
  '温州市': [
    { value: '鹿城区', label: '鹿城区' },
    { value: '龙湾区', label: '龙湾区' },
    { value: '瓯海区', label: '瓯海区' }
  ],
  '绍兴市': [
    { value: '越城区', label: '越城区' },
    { value: '柯桥区', label: '柯桥区' }
  ],
  '南京市': [
    { value: '玄武区', label: '玄武区' },
    { value: '秦淮区', label: '秦淮区' },
    { value: '鼓楼区', label: '鼓楼区' },
    { value: '建邺区', label: '建邺区' }
  ],
  '苏州市': [
    { value: '姑苏区', label: '姑苏区' },
    { value: '虎丘区', label: '虎丘区' },
    { value: '吴中区', label: '吴中区' }
  ],
  '无锡市': [
    { value: '梁溪区', label: '梁溪区' },
    { value: '滨湖区', label: '滨湖区' },
    { value: '新吴区', label: '新吴区' }
  ],
  '常州市': [
    { value: '天宁区', label: '天宁区' },
    { value: '钟楼区', label: '钟楼区' }
  ],
  '成都市': [
    { value: '锦江区', label: '锦江区' },
    { value: '青羊区', label: '青羊区' },
    { value: '武侯区', label: '武侯区' },
    { value: '高新区', label: '高新区' }
  ],
  '绵阳市': [
    { value: '涪城区', label: '涪城区' },
    { value: '游仙区', label: '游仙区' }
  ],
  '德阳市': [
    { value: '旌阳区', label: '旌阳区' }
  ],
  '武汉市': [
    { value: '江岸区', label: '江岸区' },
    { value: '江汉区', label: '江汉区' },
    { value: '武昌区', label: '武昌区' },
    { value: '洪山区', label: '洪山区' }
  ],
  '宜昌市': [
    { value: '西陵区', label: '西陵区' },
    { value: '伍家岗区', label: '伍家岗区' }
  ],
  '襄阳市': [
    { value: '襄城区', label: '襄城区' },
    { value: '樊城区', label: '樊城区' }
  ],
  '长沙市': [
    { value: '芙蓉区', label: '芙蓉区' },
    { value: '天心区', label: '天心区' },
    { value: '岳麓区', label: '岳麓区' }
  ],
  '株洲市': [
    { value: '荷塘区', label: '荷塘区' },
    { value: '芦淞区', label: '芦淞区' }
  ],
  '湘潭市': [
    { value: '雨湖区', label: '雨湖区' }
  ],
  '郑州市': [
    { value: '中原区', label: '中原区' },
    { value: '二七区', label: '二七区' },
    { value: '金水区', label: '金水区' }
  ],
  '洛阳市': [
    { value: '西工区', label: '西工区' },
    { value: '涧西区', label: '涧西区' }
  ],
  '开封市': [
    { value: '鼓楼区', label: '鼓楼区' },
    { value: '龙亭区', label: '龙亭区' }
  ],
  '济南市': [
    { value: '历下区', label: '历下区' },
    { value: '市中区', label: '市中区' },
    { value: '槐荫区', label: '槐荫区' }
  ],
  '青岛市': [
    { value: '市南区', label: '市南区' },
    { value: '市北区', label: '市北区' },
    { value: '崂山区', label: '崂山区' }
  ],
  '烟台市': [
    { value: '芝罘区', label: '芝罘区' },
    { value: '福山区', label: '福山区' }
  ]
}

const cities = ref([])
const districts = ref([])

// 处理省份选择变化
const handleProvinceChange = (isInit = false) => {
  selectedCity.value = ''
  selectedDistrict.value = ''
  cities.value = cityData[selectedProvince.value] || []
  districts.value = []
  if (!isInit) {
    updateRegionText()
  }
}

// 处理城市选择变化
const handleCityChange = (isInit = false) => {
  selectedDistrict.value = ''
  districts.value = districtData[selectedCity.value] || []
  if (!isInit) {
    updateRegionText()
  }
}

// 更新地区文本
const updateRegionText = () => {
  const parts = []
  if (selectedProvince.value) parts.push(selectedProvince.value)
  if (selectedCity.value) parts.push(selectedCity.value)
  if (selectedDistrict.value) parts.push(selectedDistrict.value)
  form.region = parts.join(' ')
}

// 保存地址（真实API调用）
const saveAddress = async () => {
  if (!form.name || !form.phone || !form.detail) {
    ElMessage.warning('请填写必填项')
    return
  }

  try {
    console.log('正在保存地址:', form)
    ElMessage.info('正在保存...')

    const addressData = {
      name: form.name,
      phone: form.phone,
      province: selectedProvince.value || '',
      city: selectedCity.value || '',
      district: selectedDistrict.value || '',
      detailAddress: form.detail,
      zipCode: form.zipCode || '',
      isDefault: form.isDefault ? 1 : 0
    }

    if (isEditing.value && editingId.value) {
      // 更新地址
      await addressApi.updateAddress({
        id: editingId.value,
        ...addressData
      })
      ElMessage.success('✅ 地址更新成功')
    } else {
      // 新增地址
      await addressApi.addAddress(addressData)
      ElMessage.success('✅ 地址添加成功')
    }

    closeModal()
    // 重新获取地址列表以刷新UI
    await fetchAddresses()

  } catch (error) {
    console.error('保存地址失败:', error)
    ElMessage.error('❌ 保存失败: ' + (error.message || '网络错误'))
  }
}

// 导航方法
const goHome = () => {
  router.push('/')
}

const goProducts = () => {
  router.push('/category')
}

const goHelp = () => {
  router.push('/help')
}

const goUserCenter = () => {
  router.push('/user')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.address-manage-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 顶部导航栏 */
.top-nav {
  width: 100%;
  background-color: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.logo-icon {
  color: #409eff;
  font-size: 24px;
}

.logo-text {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-link {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.nav-link:hover {
  color: #409eff;
}

.user-icon {
  font-size: 24px;
  color: #606266;
  cursor: pointer;
  transition: color 0.2s;
}

.user-icon:hover {
  color: #409eff;
}

/* 页面标题区 */
.page-header {
  width: 100%;
  padding: 32px 0;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.add-address-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background-color: #409eff;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.add-address-btn:hover {
  background-color: #66b1ff;
}

/* 地址列表区 */
.address-section {
  width: 100%;
  padding-bottom: 48px;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 24px 0;
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

@media (max-width: 768px) {
  .address-grid {
    grid-template-columns: 1fr;
  }
}

.address-card {
  background-color: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.3s;
}

.address-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.address-tag {
  font-size: 16px;
  font-weight: 500;
  color: #606266;
  margin: 0;
}

.default-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: #67c23a;
  color: #ffffff;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 4px;
}

.address-info {
  margin-bottom: 24px;
}

.address-info p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
}

.address-info p:last-child {
  margin-bottom: 0;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.action-btn {
  width: 64px;
  height: 32px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #ffffff;
  color: #606266;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.action-btn:hover {
  background-color: #f5f7fa;
}

.set-default-btn {
  width: 96px;
  height: 32px;
  border: 1px solid #409eff;
  border-radius: 4px;
  background-color: #ffffff;
  color: #409eff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.set-default-btn:hover {
  background-color: rgba(64, 158, 255, 0.05);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 24px;
  text-align: center;
}

.empty-icon {
  width: 120px;
  height: 120px;
  background-color: #f5f7fa;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.empty-icon i {
  font-size: 64px;
  margin-bottom: 16px;
  color: #dddddd;
}

.empty-title {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px 0;
}

.empty-desc {
  font-size: 14px;
  color: #606266;
  margin: 0 0 32px 0;
  max-width: 400px;
}

.add-address-btn-large {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  background-color: #409eff;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.add-address-btn-large:hover {
  background-color: #66b1ff;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: #ffffff;
  border-radius: 8px;
  width: 100%;
  max-width: 480px;
  margin: 0 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e4e7ed;
}

.modal-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: #606266;
  font-size: 18px;
  cursor: pointer;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #303133;
}

.modal-body {
  padding: 24px;
}

.text-center {
  text-align: center;
}

.delete-desc {
  font-size: 14px;
  color: #606266;
  margin: 16px 0 0 0;
}

.form-group {
  margin-bottom: 16px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  font-size: 14px;
  color: #303133;
  outline: none;
  transition: all 0.2s;
}

.form-input:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.form-input::placeholder {
  color: #c0c4cc;
}

.region-selects {
  display: flex;
  gap: 8px;
}

.region-select-input {
  flex: 1;
  height: 40px;
  padding: 0 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  font-size: 14px;
  color: #303133;
  outline: none;
  transition: all 0.2s;
  background-color: #ffffff;
  cursor: pointer;
}

.region-select-input:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.region-select-input:disabled {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-checkbox {
  width: 18px;
  height: 18px;
  accent-color: #409eff;
  cursor: pointer;
}

.checkbox-label {
  font-size: 14px;
  color: #606266;
  cursor: pointer;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #e4e7ed;
}

.cancel-btn {
  width: 80px;
  height: 40px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #ffffff;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn:hover {
  background-color: #f5f7fa;
}

.save-btn {
  width: 96px;
  height: 40px;
  border: none;
  border-radius: 4px;
  background-color: #409eff;
  color: #ffffff;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.save-btn:hover {
  background-color: #66b1ff;
}

.delete-confirm-btn {
  width: 96px;
  height: 40px;
  border: none;
  border-radius: 4px;
  background-color: #f56c6c;
  color: #ffffff;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.delete-confirm-btn:hover {
  background-color: #f78989;
}

.delete-modal {
  max-width: 400px;
}
</style>
