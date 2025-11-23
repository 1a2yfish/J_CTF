/**
 * 统一的消息提示工具
 * 封装 TDesign MessagePlugin，提供统一的接口
 */
import { MessagePlugin } from 'tdesign-vue-next'

/**
 * 成功提示
 * @param {string} content - 提示内容
 * @param {number} duration - 显示时长（毫秒），默认3000
 */
export const showSuccess = (content, duration = 3000) => {
  MessagePlugin.success({
    content,
    duration
  })
}

/**
 * 错误提示
 * @param {string} content - 提示内容
 * @param {number} duration - 显示时长（毫秒），默认3000
 */
export const showError = (content, duration = 3000) => {
  MessagePlugin.error({
    content,
    duration
  })
}

/**
 * 警告提示
 * @param {string} content - 提示内容
 * @param {number} duration - 显示时长（毫秒），默认3000
 */
export const showWarning = (content, duration = 3000) => {
  MessagePlugin.warning({
    content,
    duration
  })
}

/**
 * 信息提示
 * @param {string} content - 提示内容
 * @param {number} duration - 显示时长（毫秒），默认3000
 */
export const showInfo = (content, duration = 3000) => {
  MessagePlugin.info({
    content,
    duration
  })
}

/**
 * 处理API错误并显示提示
 * @param {Error} error - 错误对象
 * @param {string} defaultMessage - 默认错误消息
 * @returns {string} 错误消息
 */
export const handleApiError = (error, defaultMessage = '操作失败') => {
  let message = defaultMessage
  
  if (error?.response?.data?.message) {
    message = error.response.data.message
  } else if (error?.message) {
    message = error.message
  }
  
  showError(message)
  return message
}

