import { AppRouteRecord } from '@/types/router'

export const zhunongRoutes: AppRouteRecord = {
  name: 'Zhunong',
  path: '/zhunong',
  component: '/index/index',
  meta: {
    title: '助农商城管理',
    icon: 'ri:store-2-line',
    roles: ['ADMIN']
  },
  children: [
    {
      path: 'user-list',
      name: 'UserList',
      component: '/zhunong/user/list',
      meta: {
        title: '用户列表',
        icon: 'ri:user-line',
        keepAlive: false
      }
    },
    {
      path: 'user-favorite-stats',
      name: 'UserFavoriteStats',
      component: '/zhunong/user/favorite-stats',
      meta: {
        title: '用户收藏统计',
        icon: 'ri:bar-chart-line',
        keepAlive: false
      }
    },
    {
      path: 'category',
      name: 'ZhunongCategory',
      component: '/zhunong/category',
      meta: {
        title: '商品分类管理',
        icon: 'ri:folder-5-line',
        keepAlive: false
      }
    },
    {
      path: 'product',
      name: 'ZhunongProduct',
      component: '/zhunong/product',
      meta: {
        title: '商品管理',
        icon: 'ri:gift-line',
        keepAlive: false
      }
    },
    {
      path: 'order-list',
      name: 'OrderList',
      component: '/zhunong/order/list',
      meta: {
        title: '订单列表',
        icon: 'ri:file-list-3-line',
        keepAlive: false
      }
    },
    {
      path: 'banner',
      name: 'Banner',
      component: '/zhunong/banner',
      meta: {
        title: '轮播图管理',
        icon: 'ri:image-line',
        keepAlive: false
      }
    },
    {
      path: 'activity',
      name: 'Activity',
      component: '/zhunong/activity',
      meta: {
        title: '助农活动管理',
        icon: 'ri:calendar-event-line',
        keepAlive: false
      }
    },
    {
      path: 'admin-list',
      name: 'AdminList',
      component: '/zhunong/admin/list',
      meta: {
        title: '管理员列表',
        icon: 'ri:admin-line',
        keepAlive: false
      }
    },
    {
      path: 'review',
      name: 'Review',
      component: '/zhunong/review',
      meta: {
        title: '商品评价管理',
        icon: 'ri:star-line',
        keepAlive: false
      }
    },
    {
      path: 'hot-product',
      name: 'HotProduct',
      component: '/zhunong/hot-product',
      meta: {
        title: '热销商品管理',
        icon: 'ri:fire-line',
        keepAlive: false
      }
    },
    {
      path: 'premium-product',
      name: 'PremiumProduct',
      component: '/zhunong/premium-product',
      meta: {
        title: '助农优选商品管理',
        icon: 'ri:award-line',
        keepAlive: false
      }
    },
    {
      path: 'special-offer',
      name: 'SpecialOffer',
      component: '/zhunong/special-offer',
      meta: {
        title: '限时特惠商品管理',
        icon: 'ri:timer-flash-line',
        keepAlive: false
      }
    },
    {
      path: 'product-detail',
      name: 'ProductDetailManage',
      component: '/zhunong/product-detail',
      meta: {
        title: '商品详情页管理',
        icon: 'ri:file-list-3-line',
        keepAlive: false
      }
    },
    {
      path: 'config',
      name: 'Config',
      component: '/zhunong/config',
      meta: {
        title: '系统配置',
        icon: 'ri:settings-3-line',
        keepAlive: false
      }
    },
    {
      path: 'product-audit',
      name: 'ProductAudit',
      component: '/zhunong/product-audit',
      meta: {
        title: '商品审核',
        icon: 'ri:shield-check-line',
        keepAlive: false
      }
    },
    {
      path: 'farmer-manage',
      name: 'FarmerManage',
      component: '/zhunong/farmer-manage',
      meta: {
        title: '农户管理',
        icon: 'ri:user-star-line',
        keepAlive: false
      }
    },
    {
      path: 'report-manage',
      name: 'ReportManage',
      component: '/zhunong/report-manage',
      meta: {
        title: '举报管理',
        icon: 'ri:alert-line',
        keepAlive: false
      }
    }
  ]
}
