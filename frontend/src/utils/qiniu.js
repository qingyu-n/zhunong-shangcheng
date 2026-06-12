/**
 * 七牛云上传工具
 */
import { qiniuApi } from '../api'

/**
 * 上传文件到七牛云
 * @param {File} file - 要上传的文件
 * @returns {Promise<string>} - 返回文件URL
 */
export const uploadToQiniu = async (file) => {
  try {
    // 1. 获取上传token
    const { token, domain } = await qiniuApi.getUploadToken()
    
    // 2. 构建表单数据
    const formData = new FormData()
    formData.append('token', token)
    formData.append('file', file)
    
    // 生成唯一文件名
    const ext = file.name.split('.').pop()
    const key = `${Date.now()}_${Math.random().toString(36).substr(2, 9)}.${ext}`
    formData.append('key', key)
    
    // 3. 上传到七牛云
    const response = await fetch('https://upload.qiniup.com', {
      method: 'POST',
      body: formData
    })
    
    if (!response.ok) {
      throw new Error('上传失败')
    }
    
    const result = await response.json()
    
    // 4. 返回完整URL
    return `${domain}/${result.key}`
  } catch (error) {
    console.error('七牛云上传失败:', error)
    throw error
  }
}

/**
 * 验证文件类型和大小
 * @param {File} file - 要验证的文件
 * @param {Object} options - 验证选项
 * @returns {Object} - 验证结果
 */
export const validateFile = (file, options = {}) => {
  const {
    maxSize = 2 * 1024 * 1024, // 默认2MB
    allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  } = options
  
  // 验证文件类型
  if (!allowedTypes.includes(file.type)) {
    return {
      valid: false,
      message: `只支持 ${allowedTypes.map(t => t.replace('image/', '')).join('/')} 格式的图片`
    }
  }
  
  // 验证文件大小
  if (file.size > maxSize) {
    const sizeMB = maxSize / 1024 / 1024
    return {
      valid: false,
      message: `图片大小不能超过 ${sizeMB}MB`
    }
  }
  
  return { valid: true }
}

/**
 * 压缩图片
 * @param {File} file - 原文件
 * @param {number} maxWidth - 最大宽度
 * @param {number} quality - 压缩质量 0-1
 * @returns {Promise<Blob>} - 压缩后的图片Blob
 */
export const compressImage = (file, maxWidth = 1200, quality = 0.8) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = (e) => {
      const img = new Image()
      img.src = e.target.result
      img.onload = () => {
        const canvas = document.createElement('canvas')
        let width = img.width
        let height = img.height
        
        // 等比例缩放
        if (width > maxWidth) {
          height = (height * maxWidth) / width
          width = maxWidth
        }
        
        canvas.width = width
        canvas.height = height
        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0, width, height)
        
        canvas.toBlob(
          (blob) => resolve(blob),
          file.type,
          quality
        )
      }
      img.onerror = reject
    }
    reader.onerror = reject
  })
}

export default {
  uploadToQiniu,
  validateFile,
  compressImage
}
