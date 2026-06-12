import request from '@/utils/http'

export const adminApi = {
  getStatistics: () => {
    return request.get({ url: '/api/admin/dashboard/statistics' })
  },

  getSalesTrend: (days: number = 7) => {
    return request.get({ url: '/api/admin/dashboard/sales-trend', params: { days } })
  },

  getHotProducts: (limit: number = 10) => {
    return request.get({ url: '/api/admin/dashboard/hot-products', params: { limit } })
  },

  getRecentOrders: (limit: number = 10) => {
    return request.get({ url: '/api/admin/dashboard/recent-orders', params: { limit } })
  },

  // ==================== 用户管理 ====================

  createUser: (data: any) => {
    return request.post({ url: '/api/admin/user/create', data })
  },

  updateUserStatus: (data: any) => {
    return request.put({ url: '/api/user/status', data })
  },

  getUserList: (params?: any) => {
    return request.get({ url: '/api/user/list', params })
  },

  // ==================== 分类管理 ====================

  getCategoryList: (params?: any) => {
    return request.get({ url: '/api/admin/category/list', params })
  },

  createCategory: (data: any) => {
    return request.post({ url: '/api/admin/category', data })
  },

  updateCategory: (id: number, data: any) => {
    return request.put({ url: `/api/admin/category/${id}`, data })
  },

  deleteCategory: (id: number) => {
    return request.del({ url: `/api/admin/category/${id}` })
  },

  // ==================== 商品管理 ====================

  getProductList: (params?: any) => {
    return request.get({ url: '/api/admin/product/list', params })
  },

  createProduct: (data: any) => {
    return request.post({ url: '/api/admin/product', data })
  },

  updateProduct: (id: number, data: any) => {
    return request.put({ url: `/api/admin/product/${id}`, data })
  },

  deleteProduct: (id: number) => {
    return request.del({ url: `/api/admin/product/${id}` })
  },

  // ==================== 订单管理 ====================

  getOrderList: (params?: any) => {
    return request.get({ url: '/api/admin/order/list', params })
  },

  getOrderDetail: (id: number) => {
    return request.get({ url: `/api/admin/order/${id}` })
  },

  updateOrderStatus: (data: any) => {
    return request.put({ url: '/api/admin/order/status', data })
  },

  // ==================== 轮播图管理 ====================

  getBannerList: (params?: any) => {
    return request.get({ url: '/api/admin/banner/list', params })
  },

  createBanner: (data: any) => {
    return request.post({ url: '/api/admin/banner', data })
  },

  updateBanner: (id: number | string, data: any) => {
    return request.put({ url: `/api/admin/banner/${id}`, data })
  },

  deleteBanner: (id: number | string) => {
    return request.del({ url: `/api/admin/banner/${id}` })
  },

  getQiniuToken: () => {
    return request.get({ url: '/api/common/qiniu-token' })
  },

  // ==================== 活动管理 ====================

  getActivityList: (params?: any) => {
    return request.get({ url: '/api/admin/activity/list', params })
  },

  createActivity: (data: any) => {
    return request.post({ url: '/api/admin/activity', data })
  },

  updateActivity: (id: number, data: any) => {
    return request.put({ url: `/api/admin/activity/${id}`, data })
  },

  deleteActivity: (id: number) => {
    return request.del({ url: `/api/admin/activity/${id}` })
  },

  // ==================== 评价管理 ====================

  getReviewList: (params?: any) => {
    return request.get({ url: '/api/admin/review/list', params })
  },

  deleteReview: (id: number) => {
    return request.del({ url: `/api/admin/review/${id}` })
  },

  // ==================== 收藏管理 ====================

  getFavoriteList: (params?: any) => {
    return request.get({ url: '/api/admin/favorite/list', params })
  },

  deleteFavorite: (id: number) => {
    return request.del({ url: `/api/admin/favorite/${id}` })
  },

  // ==================== 统计数据 ====================

  getFavoriteStats: () => {
    return request.get({ url: '/api/admin/favorite/stats' })
  },

  // ==================== 系统配置 ====================

  getConfig: () => {
    return request.get({ url: '/api/admin/config' })
  },

  saveConfig: (data: any) => {
    return request.put({ url: '/api/admin/config', data })
  },

  // ==================== 管理员管理 ====================

  getAdminList: (params?: any) => {
    return request.get({ url: '/api/admin/list', params })
  },

  createAdmin: (data: any) => {
    return request.post({ url: '/api/admin', data })
  },

  updateAdmin: (id: number, data: any) => {
    return request.put({ url: `/api/admin/${id}`, data })
  },

  deleteAdmin: (id: number) => {
    return request.del({ url: `/api/admin/${id}` })
  },

  // ==================== 热销商品管理 ====================

  getHotProductList: (params?: any) => {
    return request.get({ url: '/api/admin/hot-product/list', params })
  },

  addHotProduct: (id: number) => {
    return request.post({ url: `/api/admin/hot-product/${id}` })
  },

  removeHotProduct: (id: number) => {
    return request.del({ url: `/api/admin/hot-product/${id}` })
  },

  updateHotProductSort: (data: any) => {
    return request.put({ url: '/api/admin/hot-product/sort', data })
  },

  // ==================== 助农优选商品管理 ====================

  getPremiumProductList: (params?: any) => {
    return request.get({ url: '/api/admin/premium-product/list', params })
  },

  addPremiumProduct: (id: number) => {
    return request.post({ url: `/api/admin/premium-product/${id}` })
  },

  removePremiumProduct: (id: number) => {
    return request.del({ url: `/api/admin/premium-product/${id}` })
  },

  // ==================== 限时特惠商品管理 ====================

  getSpecialOfferList: (params?: any) => {
    return request.get({ url: '/api/admin/special-offer/list', params })
  },

  createSpecialOffer: (data: any) => {
    return request.post({ url: '/api/admin/special-offer', data })
  },

  updateSpecialOffer: (id: number, data: any) => {
    return request.put({ url: `/api/admin/special-offer/${id}`, data })
  },

  deleteSpecialOffer: (id: number) => {
    return request.del({ url: `/api/admin/special-offer/${id}` })
  },

  // ==================== 商品详情页管理 ====================

  getProductDetailList: (params?: any) => {
    return request.get({ url: '/api/admin/product-detail/list', params })
  },

  getProductDetailById: (id: number) => {
    return request.get({ url: `/api/admin/product-detail/${id}` })
  },

  createProductDetail: (data: any) => {
    return request.post({ url: '/api/admin/product-detail', data })
  },

  updateProductDetail: (id: number, data: any) => {
    return request.put({ url: `/api/admin/product-detail/${id}`, data })
  },

  deleteProductDetail: (id: number) => {
    return request.del({ url: `/api/admin/product-detail/${id}` })
  },

  uploadImage: (data: FormData) => {
    return request.post({ 
      url: '/api/common/upload', 
      data,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  getProductById: (id: number) => {
    return request.get({ url: `/api/admin/product/${id}` })
  }
}
