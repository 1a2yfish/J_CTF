/**
 * 日期格式化工具函数
 */

/**
 * 格式化日期为本地字符串
 * @param {Date|string} date - 日期对象或字符串
 * @param {string} format - 格式类型：'date' | 'datetime' | 'time'
 * @returns {string} 格式化后的日期字符串
 */
export const formatDate = (date, format = 'datetime') => {
  if (!date) return '-'
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return '-'
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  switch (format) {
    case 'date':
      return `${year}-${month}-${day}`
    case 'time':
      return `${hours}:${minutes}:${seconds}`
    case 'datetime':
    default:
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  }
}

/**
 * 格式化相对时间（如：3分钟前）
 * @param {Date|string} date - 日期对象或字符串
 * @returns {string} 相对时间字符串
 */
export const formatRelativeTime = (date) => {
  if (!date) return '-'
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return '-'
  
  const now = new Date()
  const diff = now - d
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  if (minutes > 0) return `${minutes}分钟前`
  return '刚刚'
}

/**
 * 将日期转换为ISO字符串（用于API请求）
 * @param {Date|string} date - 日期对象或字符串
 * @returns {string} ISO格式字符串
 */
export const toISOString = (date) => {
  if (!date) return null
  
  if (date instanceof Date) {
    return date.toISOString()
  }
  
  if (typeof date === 'string') {
    return new Date(date).toISOString()
  }
  
  return null
}

/**
 * 检查日期是否在指定范围内
 * @param {Date|string} date - 要检查的日期
 * @param {Date|string} startDate - 开始日期
 * @param {Date|string} endDate - 结束日期
 * @returns {boolean} 是否在范围内
 */
export const isDateInRange = (date, startDate, endDate) => {
  if (!date || !startDate || !endDate) return false
  
  const d = new Date(date)
  const start = new Date(startDate)
  const end = new Date(endDate)
  
  return d >= start && d <= end
}

