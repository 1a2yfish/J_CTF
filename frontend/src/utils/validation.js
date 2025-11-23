/**
 * 表单验证工具函数
 */

/**
 * 验证邮箱格式
 * @param {string} email - 邮箱地址
 * @returns {boolean} 是否有效
 */
export const isValidEmail = (email) => {
  if (!email) return false
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * 验证手机号格式（中国）
 * @param {string} phone - 手机号
 * @returns {boolean} 是否有效
 */
export const isValidPhone = (phone) => {
  if (!phone) return false
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

/**
 * 验证Flag格式（CTF{...}）
 * @param {string} flag - Flag字符串
 * @returns {boolean} 是否有效
 */
export const isValidFlag = (flag) => {
  if (!flag) return false
  return flag.trim().length > 0
}

/**
 * 验证必填字段
 * @param {any} value - 字段值
 * @param {string} fieldName - 字段名称
 * @returns {string|null} 错误消息，无错误返回null
 */
export const validateRequired = (value, fieldName) => {
  if (value === null || value === undefined || value === '') {
    return `请输入${fieldName}`
  }
  if (typeof value === 'string' && value.trim() === '') {
    return `请输入${fieldName}`
  }
  return null
}

/**
 * 验证数字范围
 * @param {number} value - 数值
 * @param {number} min - 最小值
 * @param {number} max - 最大值
 * @param {string} fieldName - 字段名称
 * @returns {string|null} 错误消息，无错误返回null
 */
export const validateNumberRange = (value, min, max, fieldName) => {
  if (value === null || value === undefined) {
    return `请输入${fieldName}`
  }
  const num = Number(value)
  if (isNaN(num)) {
    return `${fieldName}必须是数字`
  }
  if (min !== undefined && num < min) {
    return `${fieldName}不能小于${min}`
  }
  if (max !== undefined && num > max) {
    return `${fieldName}不能大于${max}`
  }
  return null
}

