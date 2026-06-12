/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, farmerApi, messageApi } from '../api'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const isLoggedIn = computed(() => !!token.value)

  // 农户相关状态
  const isFarmer = computed(() => userInfo.value.isFarmer === 1)
  const farmerProfile = ref(null)
  const unreadMessageCount = ref(0)

  // Actions
  // 设置token
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  // 登录
  const login = async (loginForm) => {
    console.log('login called with:', loginForm)
    const res = await authApi.login(loginForm)
    console.log('login response:', res)
    setToken(res.token)
    setUserInfo(res.userInfo)
    console.log('login - userInfo set:', res.userInfo)
    return res
  }

  // 注册
  const register = async (registerForm) => {
    const res = await authApi.register(registerForm)
    // 注册成功后不自动登录，让用户去登录页面手动登录
    return res
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    console.log('fetchUserInfo called')
    const res = await authApi.getUserInfo()
    console.log('fetchUserInfo response:', res)
    setUserInfo(res)
    return res
  }

  // 更新用户信息
  const updateUserInfo = async (data) => {
    const res = await authApi.updateUserInfo(data)
    setUserInfo({ ...userInfo.value, ...res })
    return res
  }

  // 退出登录
  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 检查登录状态
  const checkLogin = () => {
    return isLoggedIn.value
  }

  // 农户相关方法
  const fetchFarmerProfile = async () => {
    if (!isFarmer.value) return null
    try {
      const res = await farmerApi.getFarmerProfile()
      farmerProfile.value = res
      return res
    } catch (error) {
      console.error('获取农户资料失败:', error)
      return null
    }
  }

  const checkFarmerStatus = async () => {
    try {
      const res = await farmerApi.getApplyStatus()
      return res.status
    } catch (error) {
      console.error('查询农户状态失败:', error)
      return null
    }
  }

  const fetchUnreadCount = async () => {
    try {
      const res = await messageApi.getUnreadCount()
      const count = typeof res === 'number' ? res : (res?.count || res?.data || 0)
      unreadMessageCount.value = count
      return count
    } catch (error) {
      console.error('获取未读消息数失败:', error)
      return 0
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isFarmer,
    farmerProfile,
    unreadMessageCount,
    setToken,
    setUserInfo,
    login,
    register,
    fetchUserInfo,
    updateUserInfo,
    logout,
    checkLogin,
    fetchFarmerProfile,
    checkFarmerStatus,
    fetchUnreadCount
  }
})
