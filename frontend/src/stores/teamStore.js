// File: frontend/src/stores/teamStore.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { teamService } from '@/services/teamService'

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

  async function createTeam(payload) {
    loading.value = true
    error.value = null
    try {
      const team = await teamService.createTeam(payload)
      currentTeam.value = team
      return team
    } catch (e) {
      error.value = e
      throw e
    } finally {
      loading.value = false
    }
  }

  async function hasTeamForCompetition(competitionId) {
    try {
      return await teamService.hasTeamForCompetition(competitionId)
    } catch (e) {
      console.error(e)
      return false
    }
  }

  async function getCurrentTeam(competitionId) {
    try {
      const team = await teamService.getMyTeam(competitionId)
      currentTeam.value = team
      return team
    } catch (e) {
      console.error(e)
      return null
    }
  }

  async function joinTeam(teamId, remark) {
    loading.value = true
    error.value = null
    try {
      const res = await teamService.joinTeam(teamId, remark)
      // after joining, refresh current team
      return res
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
      const res = await teamService.leaveTeam(currentTeam.value.teamID)
      currentTeam.value = null
      return res
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
    createTeam,
    hasTeamForCompetition,
    getCurrentTeam,
    joinTeam,
    leaveTeam
  }
})