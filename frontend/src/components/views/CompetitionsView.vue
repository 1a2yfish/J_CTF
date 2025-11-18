<template>
  <div>
    <div class="page-title">竞赛列表</div>

    <div class="card">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <t-input placeholder="搜索竞赛..." size="large" style="width: 300px;">
          <template #prefix-icon>
            <t-icon name="search" />
          </template>
        </t-input>
        <t-button theme="primary" size="large" @click="showCreateCompetition = true">
          <t-icon name="add" /> 创建新竞赛
        </t-button>
      </div>

      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px;">
        <CompetitionCard
            v-for="(competition, index) in competitions"
            :key="index"
            :competition="competition"
            @click="navigateToCompetition(competition.id)"
        />
      </div>
    </div>

    <CreateCompetitionModal
        :visible="showCreateCompetition"
        @close="showCreateCompetition = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import CompetitionCard from '../common/CompetitionCard.vue'
import CreateCompetitionModal from '../modals/CreateCompetitionModal.vue'
import { useCompetitionStore } from '../../stores/competitionStore'

const router = useRouter()
const competitionStore = useCompetitionStore()
const competitions = ref([])
const showCreateCompetition = ref(false)

onMounted(async () => {
  try {
    competitions.value = await competitionStore.getPublishedCompetitions()
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
  }
})

const navigateToCompetition = (id) => {
  router.push(`/competitions/${id}`)
}
</script>