/**
 * 统一的日志工具
 * 在生产环境中可以禁用日志输出
 */

const isDevelopment = import.meta.env.DEV

/**
 * 日志级别
 */
export const LogLevel = {
  DEBUG: 0,
  INFO: 1,
  WARN: 2,
  ERROR: 3
}

let currentLogLevel = isDevelopment ? LogLevel.DEBUG : LogLevel.ERROR

/**
 * 设置日志级别
 * @param {number} level - 日志级别
 */
export const setLogLevel = (level) => {
  currentLogLevel = level
}

/**
 * 调试日志
 */
export const debug = (...args) => {
  if (currentLogLevel <= LogLevel.DEBUG) {
    console.debug('[DEBUG]', ...args)
  }
}

/**
 * 信息日志
 */
export const info = (...args) => {
  if (currentLogLevel <= LogLevel.INFO) {
    console.info('[INFO]', ...args)
  }
}

/**
 * 警告日志
 */
export const warn = (...args) => {
  if (currentLogLevel <= LogLevel.WARN) {
    console.warn('[WARN]', ...args)
  }
}

/**
 * 错误日志
 */
export const error = (...args) => {
  if (currentLogLevel <= LogLevel.ERROR) {
    console.error('[ERROR]', ...args)
  }
}

