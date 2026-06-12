import { AppRouteRecord } from '@/types/router'

export const mallRoutes: AppRouteRecord = {
  name: 'Mall',
  path: '/mall',
  component: '/index/index',
  meta: {
    title: '商城管理',
    icon: 'ri:shopping-cart-2-line',
    roles: ['ADMIN']
  },
  children: [
    {
      path: 'category',
      name: 'Category',
      component: '/mall/category',
      meta: {
        title: '分类管理',
        icon: 'ri:folder-5-line',
        keepAlive: false
      }
    },
    {
      path: 'product',
      name: 'Product',
      component: '/mall/product',
      meta: {
        title: '商品管理',
        icon: 'ri:gift-line',
        keepAlive: false
      }
    },
    {
      path: 'order',
      name: 'Order',
      component: '/mall/order',
      meta: {
        title: '订单管理',
        icon: 'ri:file-list-3-line',
        keepAlive: false
      }
    },
    {
      path: 'user',
      name: 'MallUser',
      component: '/mall/user',
      meta: {
        title: '用户管理',
        icon: 'ri:user-3-line',
        keepAlive: false
      }
    }
  ]
}
