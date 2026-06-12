import request from '@/utils/http'

/**
 * 登录
 * @param params 登录参数
 * @returns 登录响应
 */
export function fetchLogin(params: Api.Auth.LoginParams) {
  return request.post<Api.Auth.LoginResponse>({
    url: '/api/auth/login',
    params: {
      username: params.userName,
      password: params.password
    }
  })
}

/**
 * 获取用户信息
 * @returns 用户信息
 */
export function fetchGetUserInfo() {
  return request.get<Api.Auth.UserInfo>({
    url: '/api/user/info'
  })
}

/**
 * 用户注册
 * @param params 注册参数
 * @returns 注册响应
 */
export function fetchRegister(params: {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
}) {
  return request.post({
    url: '/api/auth/register',
    data: params
  })
}
