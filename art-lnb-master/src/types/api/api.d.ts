/**
 * API 接口类型定义模块
 *
 * 提供所有后端接口的类型定义
 *
 * ## 主要功能
 *
 * - 通用类型（分页参数、响应结构等）
 * - 认证类型（登录、用户信息等）
 * - 系统管理类型（用户、角色等）
 * - 助农商城业务类型（分类、商品、订单等）
 * - 全局命名空间声明
 *
 * ## 使用场景
 *
 * - API 请求参数类型约束
 * - API 响应数据类型定义
 * - 接口文档类型同步
 *
 * ## 注意事项
 *
 * - 在 .vue 文件使用需要在 eslint.config.mjs 中配置 globals: { Api: 'readonly' }
 * - 使用全局命名空间，无需导入即可使用
 *
 * ## 使用方式
 *
 * ```typescript
 * const params: Api.Auth.LoginParams = { userName: 'admin', password: '123456' }
 * const response: Api.Auth.UserInfo = await fetchUserInfo()
 * ```
 *
 * @module types/api/api
 * @author Art Design Pro Team
 */

declare namespace Api {
  /** 通用类型 */
  namespace Common {
    /** 分页参数 */
    interface PaginationParams {
      /** 当前页码 */
      current: number
      /** 每页条数 */
      size: number
      /** 总条数 */
      total: number
    }

    /** 通用搜索参数 */
    type CommonSearchParams = Pick<PaginationParams, 'current' | 'size'>

    /** 分页响应基础结构 */
    interface PaginatedResponse<T = any> {
      records: T[]
      current: number
      size: number
      total: number
    }

    /** 启用状态 */
    type EnableStatus = '1' | '2'
  }

  /** 认证类型 */
  namespace Auth {
    /** 登录参数 */
    interface LoginParams {
      userName: string
      password: string
    }

    /** 登录响应 */
    interface LoginResponse {
      token: string
      refreshToken: string
    }

    /** 用户信息 */
    interface UserInfo {
      buttons: string[]
      roles: string[]
      userId: number
      userName: string
      email: string
      avatar?: string
    }
  }

  /** 系统管理类型 */
  namespace SystemManage {
    /** 用户列表 */
    type UserList = Api.Common.PaginatedResponse<UserListItem>

    /** 用户列表项 */
    interface UserListItem {
      id: number
      avatar: string
      status: string
      userName: string
      userGender: string
      nickName: string
      userPhone: string
      userEmail: string
      userRoles: string[]
      createBy: string
      createTime: string
      updateBy: string
      updateTime: string
    }

    /** 用户搜索参数 */
    type UserSearchParams = Partial<
      Pick<UserListItem, 'id' | 'userName' | 'userGender' | 'userPhone' | 'userEmail' | 'status'> &
        Api.Common.CommonSearchParams
    >

    /** 角色列表 */
    type RoleList = Api.Common.PaginatedResponse<RoleListItem>

    /** 角色列表项 */
    interface RoleListItem {
      roleId: number
      roleName: string
      roleCode: string
      description: string
      enabled: boolean
      createTime: string
    }

    /** 角色搜索参数 */
    type RoleSearchParams = Partial<
      Pick<RoleListItem, 'roleId' | 'roleName' | 'roleCode' | 'description' | 'enabled'> &
        Api.Common.CommonSearchParams
    >
  }

  /** 助农商城 - 用户管理 */
  namespace MallUser {
    /** 用户列表项 */
    interface UserItem {
      id: number
      username: string
      nickname: string
      email: string
      phone: string
      avatar: string
      gender: number
      birthday: string
      status: number
      role: string
      createTime: string
      updateTime: string
    }

    /** 用户列表响应 */
    type UserList = Api.Common.PaginatedResponse<UserItem>

    /** 用户搜索参数 */
    interface UserSearchParams {
      current?: number
      size?: number
      keyword?: string
      status?: number
    }

    /** 更新用户状态参数 */
    interface UpdateStatusParams {
      id: number
      status: number
    }
  }

  /** 助农商城 - 商品分类管理 */
  namespace Category {
    /** 分类项 */
    interface CategoryItem {
      id: number
      name: string
      icon: string
      sort: number
      status: number
      createTime: string
      updateTime: string
    }

    /** 分类列表响应 */
    type CategoryList = CategoryItem[]

    /** 分类搜索参数 */
    interface CategorySearchParams {
      name?: string
      status?: number
    }

    /** 创建/更新分类参数 */
    interface CategoryForm {
      id?: number
      name: string
      icon: string
      sort: number
      status: number
    }
  }

  /** 助农商城 - 商品管理 */
  namespace Product {
    /** 商品项 */
    interface ProductItem {
      id: number
      name: string
      categoryId: number
      categoryName: string
      price: number
      originalPrice: number
      stock: number
      sales: number
      mainImage: string
      images: string[]
      description: string
      status: number
      isHot: number
      isNew: number
      sort: number
      createTime: string
      updateTime: string
    }

    /** 商品列表响应 */
    type ProductList = Api.Common.PaginatedResponse<ProductItem>

    /** 商品搜索参数 */
    interface ProductSearchParams {
      current?: number
      size?: number
      name?: string
      categoryId?: number
      status?: number
    }

    /** 创建/更新商品参数 */
    interface ProductForm {
      id?: number
      name: string
      categoryId?: number
      price: number
      originalPrice: number
      stock: number
      mainImage: string
      images: string[]
      description: string
      status: number
      isHot: number
      isNew: number
      sort: number
    }

    /** 七牛云上传凭证响应 */
    interface QiniuToken {
      token: string
      domain: string
    }
  }

  /** 助农商城 - 订单管理 */
  namespace Order {
    /** 订单项 */
    interface OrderItem {
      id: number
      orderNo: string
      userId: number
      username: string
      totalAmount: number
      payAmount: number
      status: number
      statusName: string
      receiverName: string
      receiverPhone: string
      receiverAddress: string
      remark: string
      payTime: string
      shipTime: string
      receiveTime: string
      createTime: string
    }

    /** 订单详情 */
    interface OrderDetail extends OrderItem {
      items: OrderProductItem[]
    }

    /** 订单商品项 */
    interface OrderProductItem {
      id: number
      productId: number
      productName: string
      productImage: string
      price: number
      quantity: number
      totalAmount: number
    }

    /** 订单列表响应 */
    type OrderList = Api.Common.PaginatedResponse<OrderItem>

    /** 订单搜索参数 */
    interface OrderSearchParams {
      current?: number
      size?: number
      orderNo?: string
      status?: number
      startTime?: string
      endTime?: string
    }

    /** 更新订单状态参数 */
    interface UpdateStatusParams {
      id: number
      status: number
    }
  }

  /** 助农商城 - 首页数据概览 */
  namespace Dashboard {
    /** 统计数据 */
    interface Statistics {
      totalUsers: number
      totalProducts: number
      totalOrders: number
      totalSales: number
      todayOrders: number
      todaySales: number
      pendingOrders: number
      pendingShipments: number
    }

    /** 销售趋势数据 */
    interface SalesTrend {
      dates: string[]
      amounts: number[]
      orders: number[]
    }

    /** 热销商品 */
    interface HotProduct {
      id: number
      name: string
      sales: number
      amount: number
    }

    /** 最近订单 */
    interface RecentOrder {
      id: number
      orderNo: string
      username: string
      amount: number
      status: number
      statusName: string
      createTime: string
    }
  }

  /** 助农商城 - 商品审核管理 */
  namespace Audit {
    /** 待审核商品项 */
    interface ProductItem {
      id: number
      name: string
      categoryId: number
      categoryName: string
      price: number
      stock: number
      mainImage: string
      images: string[]
      description: string
      origin: string
      unit: string
      farmerId: number
      farmerName: string
      auditStatus: number
      rejectReason?: string
      createTime: string
      updateTime: string
    }

    /** 商品审核列表响应 */
    type ProductList = Api.Common.PaginatedResponse<ProductItem>

    /** 商品审核详情 */
    interface ProductDetail extends ProductItem {
      detail: string
      weight?: number
    }

    /** 商品搜索参数 */
    interface ProductSearchParams {
      current?: number
      size?: number
      keyword?: string
      auditStatus?: number
      startTime?: string
      endTime?: string
    }
  }

  /** 助农商城 - 农户管理 */
  namespace Farmer {
    /** 农户申请项 */
    interface FarmerItem {
      id: number
      userId: number
      realName: string
      phone: string
      idCard: string
      contactPhone: string
      contactAddress: string
      shopName: string
      shopDescription: string
      businessLicense: string
      status: number
      auditRemark?: string
      auditorId?: number
      auditTime?: string
      createTime: string
      updateTime: string
    }

    /** 农户申请列表响应 */
    type FarmerList = Api.Common.PaginatedResponse<FarmerItem>

    /** 农户搜索参数 */
    interface FarmerSearchParams {
      current?: number
      size?: number
      keyword?: string
      status?: number
      startTime?: string
      endTime?: string
    }
  }

  /** 助农商城 - 举报管理 */
  namespace Report {
    /** 举报项 */
    interface ReportItem {
      id: number
      reporterId: number
      reporterName: string
      productId: number
      productName: string
      reason: string
      description: string
      evidenceImages: string[]
      status: number
      handlerId?: number
      handleRemark?: string
      handleTime?: string
      createTime: string
      updateTime: string
    }

    /** 举报列表响应 */
    type ReportList = Api.Common.PaginatedResponse<ReportItem>

    /** 举报搜索参数 */
    interface ReportSearchParams {
      current?: number
      size?: number
      status?: number
      startTime?: string
      endTime?: string
    }

    /** 处理举报参数 */
    interface HandleParams {
      status: number
      remark?: string
    }
  }
}
