import request from '@/utils/http'

// ==================== 首页数据概览 ====================

/**
 * 获取首页统计数据
 */
export function fetchGetStatistics() {
  return request.get<Api.Dashboard.Statistics>({
    url: '/api/admin/dashboard/statistics'
  })
}

/**
 * 获取销售趋势数据
 */
export function fetchGetSalesTrend(days: number = 7) {
  return request.get<Api.Dashboard.SalesTrend>({
    url: '/api/admin/dashboard/sales-trend',
    params: { days }
  })
}

/**
 * 获取热销商品排行
 */
export function fetchGetHotProducts(limit: number = 10) {
  return request.get<Api.Dashboard.HotProduct[]>({
    url: '/api/admin/dashboard/hot-products',
    params: { limit }
  })
}

/**
 * 获取最近订单
 */
export function fetchGetRecentOrders(limit: number = 10) {
  return request.get<Api.Dashboard.RecentOrder[]>({
    url: '/api/admin/dashboard/recent-orders',
    params: { limit }
  })
}

// ==================== 用户管理 ====================

/**
 * 获取用户列表
 */
export function fetchGetUserList(params: Api.MallUser.UserSearchParams) {
  return request.get<Api.MallUser.UserList>({
    url: '/api/admin/user/list',
    params
  })
}

/**
 * 获取用户详情
 */
export function fetchGetUserDetail(id: number) {
  return request.get<Api.MallUser.UserItem>({
    url: `/api/admin/user/${id}`
  })
}

/**
 * 更新用户状态
 */
export function fetchUpdateUserStatus(params: Api.MallUser.UpdateStatusParams) {
  return request.put<null>({
    url: '/api/admin/user/status',
    params
  })
}

// ==================== 商品分类管理 ====================

/**
 * 获取分类列表
 */
export function fetchGetCategoryList(params?: Api.Category.CategorySearchParams) {
  return request.get<Api.Category.CategoryList>({
    url: '/api/admin/category/list',
    params
  })
}

/**
 * 获取分类详情
 */
export function fetchGetCategoryDetail(id: number) {
  return request.get<Api.Category.CategoryItem>({
    url: `/api/admin/category/${id}`
  })
}

/**
 * 创建分类
 */
export function fetchCreateCategory(data: Api.Category.CategoryForm) {
  return request.post<Api.Category.CategoryItem>({
    url: '/api/admin/category',
    params: data
  })
}

/**
 * 更新分类
 */
export function fetchUpdateCategory(data: Api.Category.CategoryForm) {
  return request.put<Api.Category.CategoryItem>({
    url: `/api/admin/category/${data.id}`,
    params: data
  })
}

/**
 * 删除分类
 */
export function fetchDeleteCategory(id: number) {
  return request.del<null>({
    url: `/api/admin/category/${id}`
  })
}

// ==================== 商品管理 ====================

/**
 * 获取商品列表
 */
export function fetchGetProductList(params: Api.Product.ProductSearchParams) {
  return request.get<Api.Product.ProductList>({
    url: '/api/admin/product/list',
    params
  })
}

/**
 * 获取商品详情
 */
export function fetchGetProductDetail(id: number) {
  return request.get<Api.Product.ProductItem>({
    url: `/api/admin/product/${id}`
  })
}

/**
 * 创建商品
 */
export function fetchCreateProduct(data: Api.Product.ProductForm) {
  return request.post<Api.Product.ProductItem>({
    url: '/api/admin/product',
    params: data
  })
}

/**
 * 更新商品
 */
export function fetchUpdateProduct(data: Api.Product.ProductForm) {
  return request.put<Api.Product.ProductItem>({
    url: `/api/admin/product/${data.id}`,
    params: data
  })
}

/**
 * 删除商品
 */
export function fetchDeleteProduct(id: number) {
  return request.del<null>({
    url: `/api/admin/product/${id}`
  })
}

/**
 * 获取七牛云上传凭证
 */
export function fetchGetQiniuToken() {
  return request.get<Api.Product.QiniuToken>({
    url: '/api/common/qiniu-token'
  })
}

// ==================== 订单管理 ====================

/**
 * 获取订单列表
 */
export function fetchGetOrderList(params: Api.Order.OrderSearchParams) {
  return request.get<Api.Order.OrderList>({
    url: '/api/admin/order/list',
    params
  })
}

/**
 * 获取订单详情
 */
export function fetchGetOrderDetail(id: number) {
  return request.get<Api.Order.OrderDetail>({
    url: `/api/admin/order/${id}`
  })
}

/**
 * 更新订单状态
 */
export function fetchUpdateOrderStatus(params: Api.Order.UpdateStatusParams) {
  return request.put<null>({
    url: '/api/admin/order/status',
    params
  })
}

// ==================== 商品审核管理 ====================

/**
 * 获取待审核商品列表
 */
export function fetchGetAuditProductList(params: Api.Audit.ProductSearchParams) {
  return request.get<Api.Audit.ProductList>({
    url: '/api/admin/audit/products',
    params
  })
}

/**
 * 获取商品审核详情
 */
export function fetchGetAuditProductDetail(id: number) {
  return request.get<Api.Audit.ProductDetail>({
    url: `/api/admin/audit/products/${id}`
  })
}

/**
 * 通过商品审核
 */
export function fetchApproveProduct(id: number, data: { remark?: string }) {
  return request.put<null>({
    url: `/api/admin/audit/products/${id}/approve`,
    params: data
  })
}

/**
 * 拒绝商品审核
 */
export function fetchRejectProduct(id: number, data: { reason: string }) {
  return request.put<null>({
    url: `/api/admin/audit/products/${id}/reject`,
    params: data
  })
}

export function fetchDeleteAuditProduct(id: number) {
  return request.del<null>({
    url: `/api/admin/audit/products/${id}`
  })
}

// ==================== 农户管理 ====================

/**
 * 获取农户申请列表
 */
export function fetchGetFarmerList(params: Api.Farmer.FarmerSearchParams) {
  return request.get<Api.Farmer.FarmerList>({
    url: '/api/admin/audit/farmers',
    params
  })
}

/**
 * 通过农户申请
 */
export function fetchApproveFarmer(id: number, data: { remark?: string }) {
  return request.put<null>({
    url: `/api/admin/audit/farmers/${id}/approve`,
    params: data
  })
}

/**
 * 拒绝农户申请
 */
export function fetchRejectFarmer(id: number, data: { reason: string }) {
  return request.put<null>({
    url: `/api/admin/audit/farmers/${id}/reject`,
    params: data
  })
}

export function fetchDeleteFarmer(id: number) {
  return request.del<null>({
    url: `/api/admin/audit/farmers/${id}`
  })
}

// ==================== 举报管理 ====================

/**
 * 获取举报列表
 */
export function fetchGetReportList(params: Api.Report.ReportSearchParams) {
  return request.get<Api.Report.ReportList>({
    url: '/api/admin/reports',
    params
  })
}

/**
 * 处理举报
 */
export function fetchHandleReport(id: number, data: Api.Report.HandleParams) {
  return request.put<null>({
    url: `/api/admin/reports/${id}/handle`,
    params: data
  })
}

export function fetchDeleteReport(id: number) {
  return request.del<null>({
    url: `/api/admin/reports/${id}`
  })
}
