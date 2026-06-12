import { createRouter, createWebHistory } from 'vue-router'

// 导入所有页面组件
import LoginPage from '../views/LoginPage.vue'
import RegisterPage from '../views/RegisterPage.vue'
import ForgotPassword from '../views/ForgotPassword.vue'
import HomePage from '../views/HomePage.vue'
import CategoryPage from '../views/CategoryPage.vue'
import ProductList from '../views/ProductList.vue'
import SearchResult from '../views/SearchResult.vue'
import ProductDetail from '../views/ProductDetail.vue'
import HelpActivity from '../views/HelpActivity.vue'
import ShoppingCart from '../views/ShoppingCart.vue'
import OrderConfirm from '../views/OrderConfirm.vue'
import PaymentSuccess from '../views/PaymentSuccess.vue'
import UserCenter from '../views/UserCenter.vue'
import MyOrders from '../views/MyOrders.vue'
import OrderDetail from '../views/OrderDetail.vue'
import AddressManage from '../views/AddressManage.vue'
import AccountInfo from '../views/AccountInfo.vue'
import MyFavorites from '../views/MyFavorites.vue'
import AboutUs from '../views/AboutUs.vue'
import SpecialOffer from '../views/SpecialOffer.vue'

// 农户功能页面
import ApplyFarmer from '../views/ApplyFarmer.vue'
import PublishProduct from '../views/PublishProduct.vue'
import MyProducts from '../views/MyProducts.vue'
import ShopPage from '../views/ShopPage.vue'
import MessageCenter from '../views/MessageCenter.vue'
import MyShop from '../views/MyShop.vue'
import FarmerStatistics from '../views/FarmerStatistics.vue'
import FarmerOrders from '../views/FarmerOrders.vue'

// 路由配置
const routes = [
  // 默认首页
  {
    path: '/',
    name: 'Home',
    component: HomePage,
    meta: { title: '商城首页' }
  },
  // 认证相关页面
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
    meta: { title: '登录', guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterPage,
    meta: { title: '注册', guestOnly: true }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPassword,
    meta: { title: '忘记密码', guestOnly: true }
  },
  // 商品相关页面
  {
    path: '/category',
    name: 'Category',
    component: CategoryPage,
    meta: { title: '农产品分类' }
  },
  {
    path: '/products',
    name: 'ProductList',
    component: ProductList,
    meta: { title: '商品列表' }
  },
  {
    path: '/search',
    name: 'SearchResult',
    component: SearchResult,
    meta: { title: '搜索结果' }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: ProductDetail,
    meta: { title: '商品详情' }
  },
  // 助农活动
  {
    path: '/help',
    name: 'HelpActivity',
    component: HelpActivity,
    meta: { title: '助农活动' }
  },
  // 限时特惠
  {
    path: '/special-offer',
    name: 'SpecialOffer',
    component: SpecialOffer,
    meta: { title: '限时特惠' }
  },
  // 关于我们
  {
    path: '/about',
    name: 'AboutUs',
    component: AboutUs,
    meta: { title: '关于我们' }
  },
  // 购物车（需要登录）
  {
    path: '/cart',
    name: 'ShoppingCart',
    component: ShoppingCart,
    meta: { title: '购物车', requiresAuth: true }
  },
  // 订单相关（需要登录）
  {
    path: '/order/confirm',
    name: 'OrderConfirm',
    component: OrderConfirm,
    meta: { title: '订单确认', requiresAuth: true }
  },
  {
    path: '/order/success',
    name: 'PaymentSuccess',
    component: PaymentSuccess,
    meta: { title: '支付成功', requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'MyOrders',
    component: MyOrders,
    meta: { title: '我的订单', requiresAuth: true }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: OrderDetail,
    meta: { title: '订单详情', requiresAuth: true }
  },
  // 用户中心（需要登录）
  {
    path: '/user',
    name: 'UserCenter',
    component: UserCenter,
    meta: { title: '个人中心', requiresAuth: true }
  },
  // 用户相关页面（需要登录）
  {
    path: '/address',
    name: 'AddressManage',
    component: AddressManage,
    meta: { title: '收货地址管理', requiresAuth: true }
  },
  {
    path: '/account',
    name: 'AccountInfo',
    component: AccountInfo,
    meta: { title: '账号信息', requiresAuth: true }
  },
  {
    path: '/favorites',
    name: 'MyFavorites',
    component: MyFavorites,
    meta: { title: '我的收藏', requiresAuth: true }
  },
  // 农户功能页面（需要登录）
  {
    path: '/apply-farmer',
    name: 'ApplyFarmer',
    component: ApplyFarmer,
    meta: { title: '申请成为农户', requiresAuth: true }
  },
  {
    path: '/publish',
    name: 'PublishProduct',
    component: PublishProduct,
    meta: { title: '发布商品', requiresAuth: true, requiresFarmer: true }
  },
  {
    path: '/publish/:id?',
    name: 'EditProduct',
    component: PublishProduct,
    meta: { title: '编辑商品', requiresAuth: true, requiresFarmer: true }
  },
  {
    path: '/my-products',
    name: 'MyProducts',
    component: MyProducts,
    meta: { title: '我的商品', requiresAuth: true, requiresFarmer: true }
  },
  {
    path: '/shop/:id',
    name: 'ShopPage',
    component: ShopPage,
    meta: { title: '店铺主页' }
  },
  {
    path: '/messages',
    name: 'MessageCenter',
    component: MessageCenter,
    meta: { title: '消息中心', requiresAuth: true }
  },
  {
    path: '/my-shop',
    name: 'MyShop',
    component: MyShop,
    meta: { title: '我的店铺', requiresAuth: true, requiresFarmer: true }
  },
  {
    path: '/statistics',
    name: 'FarmerStatistics',
    component: FarmerStatistics,
    meta: { title: '数据统计', requiresAuth: true, requiresFarmer: true }
  },
  {
    path: '/farmer-orders',
    name: 'FarmerOrders',
    component: FarmerOrders,
    meta: { title: '订单发货', requiresAuth: true, requiresFarmer: true }
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 从 localStorage 获取登录状态
const isAuthenticated = () => {
  return !!localStorage.getItem('token')
}

// 路由守卫
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const guestOnly = to.matched.some(record => record.meta.guestOnly)
  const isLoggedIn = isAuthenticated()

  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title + ' - 农鲜生'
  }

  // 需要登录但未登录，跳转到登录页
  if (requiresAuth && !isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 已登录用户访问登录/注册/忘记密码页，跳转到首页
  if (guestOnly && isLoggedIn) {
    next('/')
    return
  }

  next()
})

export default router
