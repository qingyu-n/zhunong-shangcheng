import { AppRouteRecord } from '@/types/router'
import { dashboardRoutes } from './dashboard'
import { zhunongRoutes } from './zhunong'

/**
 * 导出所有模块化路由
 */
export const routeModules: AppRouteRecord[] = [dashboardRoutes, zhunongRoutes]
