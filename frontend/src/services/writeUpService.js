import api from './apiService'

export const writeUpService = {
    // 上传或更新WriteUp
    async uploadWriteUp(competitionId, title, content) {
        try {
            const response = await api.post('/writeups', {
                competitionId,
                title,
                content
            })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '提交WriteUp失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '提交WriteUp失败')
        }
    },

    // 获取当前用户的WriteUp列表
    async getMyWriteUps() {
        try {
            const response = await api.get('/writeups/my')
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return []
        } catch (error) {
            console.error('获取WriteUp列表失败:', error)
            return []
        }
    },

    // 获取指定竞赛的WriteUp列表
    async getWriteUpsByCompetition(competitionId) {
        try {
            const response = await api.get(`/writeups/competitions/${competitionId}`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return []
        } catch (error) {
            console.error('获取WriteUp列表失败:', error)
            return []
        }
    },

    // 获取指定用户的WriteUp列表（管理员）
    async getWriteUpsByUser(userId) {
        try {
            const response = await api.get(`/writeups/users/${userId}`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return []
        } catch (error) {
            console.error('获取WriteUp列表失败:', error)
            return []
        }
    },

    // 获取WriteUp详情
    async getWriteUpById(writeUpId) {
        try {
            const response = await api.get(`/writeups/${writeUpId}`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '获取WriteUp失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '获取WriteUp失败')
        }
    },

    // 下载WriteUp（用户或管理员）
    async downloadWriteUp(writeUpId) {
        try {
            const response = await api.get(`/writeups/${writeUpId}/download`, {
                responseType: 'blob'
            })
            
            // 检查响应是否为错误（blob响应可能包含错误信息）
            if (response.data instanceof Blob && response.data.size < 1000) {
                // 如果blob很小，可能是错误信息，尝试解析
                const text = await response.data.text()
                try {
                    const errorData = JSON.parse(text)
                    if (errorData.success === false) {
                        throw new Error(errorData.message || '下载失败')
                    }
                } catch (e) {
                    // 不是JSON，继续处理为正常文件
                }
            }
            
            // 创建下载链接
            const blob = response.data instanceof Blob 
                ? response.data 
                : new Blob([response.data], { type: 'text/plain;charset=utf-8' })
            const url = window.URL.createObjectURL(blob)
            const link = document.createElement('a')
            link.href = url
            
            // 从响应头获取文件名，如果没有则使用默认名称
            const contentDisposition = response.headers['content-disposition'] || 
                                     response.headers['Content-Disposition']
            let filename = `WriteUp_${writeUpId}.txt`
            if (contentDisposition) {
                const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/i)
                if (filenameMatch && filenameMatch[1]) {
                    filename = filenameMatch[1].replace(/['"]/g, '')
                    // 处理URL编码的文件名
                    try {
                        filename = decodeURIComponent(filename)
                    } catch (e) {
                        // 如果解码失败，使用原始文件名
                    }
                }
            }
            
            link.setAttribute('download', filename)
            document.body.appendChild(link)
            link.click()
            document.body.removeChild(link)
            window.URL.revokeObjectURL(url)
            
            return true
        } catch (error) {
            // 如果是blob响应但包含错误，尝试解析错误信息
            if (error.response && error.response.data instanceof Blob) {
                try {
                    const text = await error.response.data.text()
                    const errorData = JSON.parse(text)
                    throw new Error(errorData.message || '下载WriteUp失败')
                } catch (e) {
                    throw new Error(error.message || '下载WriteUp失败')
                }
            }
            throw new Error(error.response?.data?.message || error.message || '下载WriteUp失败')
        }
    },

    // 搜索WriteUp（管理员）
    async searchWriteUps(keyword) {
        try {
            const response = await api.get('/writeups/search', {
                params: { keyword }
            })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return []
        } catch (error) {
            console.error('搜索WriteUp失败:', error)
            return []
        }
    },

    // 删除WriteUp
    async deleteWriteUp(writeUpId) {
        try {
            const response = await api.delete(`/writeups/${writeUpId}`)
            if (response.data.success) {
                return true
            }
            throw new Error(response.data.message || '删除WriteUp失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '删除WriteUp失败')
        }
    }
}

