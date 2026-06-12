/**
 * API 接口统一管理
 */
import { get, post, put, del } from '../utils/request'

// ==================== 认证相关 ====================
export const authApi = {
  // 登录
  login: (data) => post('/auth/login', data),
  // 注册
  register: (data) => post('/auth/register', data),
  // 获取当前用户信息
  getUserInfo: () => get('/user/info'),
  // 更新用户信息
  updateUserInfo: (data) => put('/user/info', data),
  // 上传头像
  uploadAvatar: (data) => post('/user/avatar', data),
  // 修改密码
  changePassword: (data) => put('/user/password', data)
}

// ==================== 首页相关 ====================
export const homeApi = {
  // 获取轮播图列表
  getBanners: () => get('/home/banners'),
  // 获取热销商品
  getHotProducts: (limit = 8) => get('/home/hot-products', { limit }),
  // 获取推荐商品
  getRecommendProducts: (limit = 12) => get('/home/recommend-products', { limit }),
  // 获取新品上架
  getNewProducts: (limit = 8) => get('/home/new-products', { limit })
}

// ==================== 分类相关 ====================
export const categoryApi = {
  // 获取所有分类
  getCategories: () => get('/category/list'),
  // 获取分类详情
  getCategoryById: (id) => get(`/category/${id}`),
  // 别名，兼容旧代码
  getList: () => get('/category/list')
}

// ==================== 商品相关 ====================
export const productApi = {
  // 获取商品列表
  getProductList: (params) => get('/product/list', params),
  // 获取商品详情
  getProductDetail: (id) => get(`/product/${id}`),
  // 搜索商品
  searchProducts: (params) => get('/product/search', params),
  // 获取分类下的商品
  getProductsByCategory: (categoryId, params) => get(`/product/category/${categoryId}`, params),
  // 获取热销商品
  getHotProducts: (params) => get('/product/hot', params),
  // 获取助农优选商品
  getPremiumProducts: (params) => get('/product/premium', params),
  // 获取限时特惠商品
  getSpecialOffers: () => get('/product/special-offers'),
  // 获取商品详情信息
  getProductDetailInfo: (id) => get(`/product/detail/product/${id}`)
}

// ==================== 购物车相关 ====================
export const cartApi = {
  // 获取购物车列表
  getCartList: () => get('/cart/list'),
  // 添加商品到购物车
  addToCart: (data) => post('/cart/add', data),
  // 更新购物车商品数量
  updateCartItem: (id, data) => put(`/cart/${id}`, data),
  // 删除购物车商品
  deleteCartItem: (id) => del(`/cart/${id}`),
  // 清空购物车
  clearCart: () => del('/cart/clear'),
  // 批量删除
  batchDelete: (ids) => post('/cart/batch-delete', { ids })
}

// ==================== 订单相关 ====================
export const orderApi = {
  // 创建订单
  createOrder: (data) => post('/order/create', data),
  // 获取订单列表
  getOrderList: (params) => get('/order/list', params),
  // 获取订单详情
  getOrderDetail: (id) => get(`/order/${id}`),
  // 取消订单
  cancelOrder: (id) => put(`/order/${id}/cancel`),
  // 确认收货
  confirmReceive: (id) => put(`/order/${id}/confirm`),
  // 删除订单
  deleteOrder: (id) => del(`/order/${id}`),
  // 获取订单统计
  getOrderStats: () => get('/order/stats')
}

// ==================== 收货地址相关 ====================
export const addressApi = {
  // 获取地址列表
  getAddressList: () => get('/address/list'),
  // 获取默认地址
  getDefaultAddress: () => get('/address/default'),
  // 获取地址详情
  getAddressById: (id) => get(`/address/${id}`),
  // 添加地址
  addAddress: (data) => post('/address', data),
  // 更新地址
  updateAddress: (id, data) => put(`/address/${id}`, data),
  // 删除地址
  deleteAddress: (id) => del(`/address/${id}`),
  // 设置默认地址
  setDefaultAddress: (id) => put(`/address/${id}/default`)
}

// ==================== 收藏相关 ====================
export const favoriteApi = {
  // 获取收藏列表
  getFavoriteList: (params) => get('/favorite/list', params),
  // 添加收藏
  addFavorite: (productId) => post('/favorite', { productId }),
  // 取消收藏
  removeFavorite: (productId) => del('/favorite', { productId }),
  // 检查是否已收藏
  checkFavorite: (productId) => get('/favorite/check', { productId })
}

// ==================== 七牛云相关 ====================
export const qiniuApi = {
  // 获取上传token
  getUploadToken: () => get('/common/qiniu-token')
}

// ==================== 农户功能相关 ====================
export const farmerApi = {
  // 申请成为农户
  applyFarmer: (data) => post('/farmer/apply', data),
  // 获取农户资料
  getFarmerProfile: () => get('/farmer/profile'),
  // 更新农户资料
  updateFarmerProfile: (data) => put('/farmer/profile', data),
  // 查询申请状态
  getApplyStatus: () => get('/farmer/status'),

  // 商品管理
  publishProduct: (data) => post('/farmer/products', data),
  getMyProducts: (page, size, keyword, auditStatus, status) =>
    get('/farmer/products', { page, size, keyword, auditStatus, status }),
  updateProduct: (id, data) => put(`/farmer/products/${id}`, data),
  deleteProduct: (id) => del(`/farmer/products/${id}`),
  updateProductStatus: (id, status) => put(`/farmer/products/${id}/status?status=${status}`),
  getMyProductDetail: (id) => get(`/farmer/products/${id}`),

  // 订单管理
  getFarmerOrders: (page, size, status) => get('/farmer/orders', { page, size, status }),
  shipOrder: (id, data) => put(`/farmer/orders/${id}/ship`, data),
  cancelShip: (id) => put(`/farmer/orders/${id}/cancel-ship`)
}

// ==================== 店铺相关 ====================
export const shopApi = {
  getShopDetail: (id) => get(`/shops/${id}`),
  getMyShop: () => get('/shops/my'),
  updateShop: (data) => put('/shops/my', data),
  getShopProducts: (id, params) => get(`/shops/${id}/products`, params),
  favoriteShop: (id) => post(`/shops/${id}/favorite`),
  unfavoriteShop: (id) => del(`/shops/${id}/favorite`),
  getFavoriteShops: (params) => get('/shops/my-favorites', params)
}

// ==================== 消息相关 ====================
export const messageApi = {
  // 获取消息列表
  getMessageList: (params) => get('/messages', params),
  // 标记已读
  markAsRead: (id) => put(`/messages/${id}/read`),
  // 全部标记已读
  markAllRead: () => put('/messages/read-all'),
  // 获取未读数
  getUnreadCount: () => get('/messages/unread-count')
}

// ==================== 评价相关 ====================
export const reviewApi = {
  createReview: (productId, data) => post('/reviews', { ...data, productId }),
  getReviews: (productId, params) => get(`/reviews/product/${productId}`, params),
  replyReview: (reviewId, data) => post(`/reviews/${reviewId}/reply`, data)
}

// ==================== 举报相关 ====================
export const reportApi = {
  reportProduct: (productId, data) => post(`/reports/product/${productId}`, data)
}

// ==================== 统计相关 ====================
export const statisticsApi = {
  // 数据概览
  getOverview: () => get('/farmer/statistics/overview'),
  // 销售趋势
  getSalesTrend: (params) => get('/farmer/statistics/sales', params),
  // 商品统计
  getProductStats: () => get('/farmer/statistics/products')
}

// 统一导出
export default {
  auth: authApi,
  home: homeApi,
  category: categoryApi,
  product: productApi,
  cart: cartApi,
  order: orderApi,
  address: addressApi,
  favorite: favoriteApi,
  qiniu: qiniuApi,
  farmer: farmerApi,
  shop: shopApi,
  message: messageApi,
  review: reviewApi,
  report: reportApi,
  statistics: statisticsApi
}
