<template>
  <div class="problem-card">
    <div class="problem-header">
      <div class="problem-title">{{ problem.title }}</div>
      <div class="problem-score">{{ problem.score }}分</div>
    </div>
    <div class="problem-description">
      {{ problem.description }}
    </div>

    <FlagSubmission
        v-if="!problem.solved"
        @submit-flag="handleSubmit"
    />

    <t-alert
        v-if="problem.solved"
        theme="success"
        style="margin-top: 12px;"
        :show-arrow="false"
    >
      已解决！获得 {{ problem.score }} 分
    </t-alert>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import FlagSubmission from './FlagSubmission.vue'

const props = defineProps({
  problem: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['submit-flag'])

const handleSubmit = (flag) => {
  emit('submit-flag', props.problem.id, flag)
}
</script>