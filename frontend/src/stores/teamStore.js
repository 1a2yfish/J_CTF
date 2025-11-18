// File: frontend/src/stores/teamStore.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useTeamStore = defineStore('team', () => {
  const teams = ref([])
  const currentTeam = ref(null)
  const loading = ref(false)
  const error = ref(null)

  const isMember = computed(() => !!currentTeam.value)

  function setTeams(list) {
    teams.value = Array.isArray(list) ? list : []
  }

  function setCurrent(team) {
    currentTeam.value = team || null
  }

  async function joinTeam(teamId) {
    loading.value = true
    error.value = null
    try {
      // 示例：调用后端接口加入队伍。根据项目实际 API 调整 URL/方法。
      const res = await fetch(`/api/teams/${encodeURIComponent(teamId)}/join`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      })
      if (!res.ok) throw new Error(`Join failed: ${res.status}`)
      const data = await res.json()
      // 假设后端返回当前队伍信息
      setCurrent(data)
      return data
    } catch (e) {
      error.value = e
      throw e
    } finally {
      loading.value = false
    }
  }

  async function leaveTeam() {
    if (!currentTeam.value) return
    loading.value = true
    error.value = null
    try {
      const res = await fetch(`/api/teams/${encodeURIComponent(currentTeam.value.id)}/leave`, {
        method: 'POST'
      })
      if (!res.ok) throw new Error(`Leave failed: ${res.status}`)
      setCurrent(null)
    } catch (e) {
      error.value = e
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    teams,
    currentTeam,
    loading,
    error,
    isMember,
    setTeams,
    setCurrent,
    joinTeam,
    leaveTeam
  }
})