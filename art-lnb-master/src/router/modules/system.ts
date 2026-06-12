import { AppRouteRecord } from '@/types/router'

export const systemRoutes: AppRouteRecord = {
  path: '/system',
  name: 'System',
  component: '/index/index',
  meta: {
    title: '系统管理',
    icon: 'ri:settings-3-line',
    roles: ['ADMIN']
  },
  children: [
    {
      path: 'user',
      name: 'User',
      component: '/mall/user/index',
      meta: {
        title: '用户管理',
        icon: 'ri:user-line',
        keepAlive: true,
        roles: ['ADMIN']
      }
    },
    {
      path: 'category',
      name: 'Category',
      component: '/mall/category/index',
      meta: {
        title: '商品分类管理',
        icon: 'ri:folder-line',
        keepAlive: true,
        roles: ['ADMIN']
      }
    },
    {
      path: 'product',
      name: 'Product',
      component: '/mall/product/index',
      meta: {
        title: '商品管理',
        icon: 'ri:shopping-bag-line',
        keepAlive: true,
        roles: ['ADMIN']
      }
    },
    {
      path: 'order',
      name: 'Order',
      component: '/mall/order/index',
      meta: {
        title: '订单管理',
        icon: 'ri:file-list-line',
        keepAlive: true,
        roles: ['ADMIN']
      }
    },
    {
      path: 'user-center',
      name: 'UserCenter',
      component: '/system/user-center',
      meta: {
        title: '个人中心',
        icon: 'ri:user-line',
        isHide: true,
        keepAlive: true,
        isHideTab: true
      }
    }
  ]
}
