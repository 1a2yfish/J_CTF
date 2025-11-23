/**
 * 请求工具函数
 * 统一处理API请求的通用逻辑
 */

/**
 * 处理API响应
 * @param {Promise} request - API请求Promise
 * @param {Object} options - 选项
 * @param {boolean} options.showSuccess - 是否显示成功提示
 * @param {string} options.successMessage - 成功消息
 * @param {boolean} options.showError - 是否显示错误提示
 * @returns {Promise} 处理后的Promise
 */
export const handleRequest = async (request, options = {}) => {
  const {
    showSuccess = false,
    successMessage = '操作成功',
    showError = true
  } = options
  
  try {
    const response = await request
    
    if (showSuccess) {
      const { showSuccess: showSuccessMsg } = await import('./message.js')
      showSuccessMsg(successMessage)
    }
    
    return response
  } catch (error) {
    if (showError) {
      const { handleApiError } = await import('./message.js')
      handleApiError(error, '操作失败')
    }
    throw error
  }
}

/**
 * 创建带加载状态的请求函数
 * @param {Function} requestFn - 请求函数
 * @param {Ref} loadingRef - loading状态的ref
 * @returns {Function} 包装后的请求函数
 */
export const withLoading = (requestFn, loadingRef) => {
  return async (...args) => {
    if (loadingRef) {
      loadingRef.value = true
    }
    try {
      return await requestFn(...args)
    } finally {
      if (loadingRef) {
        loadingRef.value = false
      }
    }
  }
}

