<template>
  <div class="competition-card" @click="$emit('click')">
    <div class="competition-header">
      <div class="competition-title">{{ competition.title }}</div>
      <div
          class="competition-status"
          :class="statusClass"
      >
        {{ statusLabel }}
      </div>
    </div>
    <div class="competition-description">
      {{ competition.description }}
    </div>
    <div class="competition-meta">
      <div class="meta-item">
        <t-icon name="time" /> {{ competition.startAt }} - {{ competition.endAt }}
      </div>
      <div class="meta-item">
        <t-icon name="user" /> {{ competition.participants }}人
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  competition: {
    type: Object,
    required: true
  }
})

const statusClass = computed(() => {
  switch (props.competition.status) {
    case 'active': return 'status-active'
    case 'upcoming': return 'status-upcoming'
    case 'ended': return 'status-ended'
    default: return ''
  }
})

const statusLabel = computed(() => {
  switch (props.competition.status) {
    case 'active': return '进行中'
    case 'upcoming': return '即将开始'
    case 'ended': return '已结束'
    default: return '未知状态'
  }
})
</script>