/**
 * 图片URL处理工具
 */

/**
 * 处理图片URL，确保图片能够正常显示
 * @param {string} url - 原始图片URL
 * @returns {string} - 处理后的图片URL
 */
export const processImageUrl = (url) => {
  if (!url) return ''
  
  // 如果已经是完整的HTTP(S) URL，或者是data URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
    return url
  }
  
  // 如果已经以 /api 开头（通过代理访问的），直接返回
  if (url.startsWith('/api')) {
    return url
  }
  
  // 如果以 / 开头，加上完整API base URL
  if (url.startsWith('/')) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    return `${baseUrl}${url}`
  }
  
  // 其他情况，加上 /api 前缀
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return `${baseUrl}/${url}`
}

/**
 * 验证图片URL是否有效
 * @param {string} url - 图片URL
 * @returns {boolean} - 是否有效
 */
export const isValidImageUrl = (url) => {
  if (!url) return false
  const imageRegex = /\.(jpeg|jpg|gif|png|webp|svg)$/i
  return imageRegex.test(url) || url.startsWith('data:image')
}

export default {
  processImageUrl,
  isValidImageUrl
}
