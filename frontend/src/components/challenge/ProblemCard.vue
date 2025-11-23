<template>
  <div class="problem-card" :class="{ 'solved': problem.solved }">
    <div class="problem-header">
      <div class="problem-title-section">
        <div class="problem-title">{{ problem.title }}</div>
        <div class="problem-meta">
          <span class="problem-category" v-if="problem.category && problem.category !== '未分类'">
            {{ problem.category }}
          </span>
          <span 
            class="problem-difficulty" 
            :class="`difficulty-${(problem.difficulty || 'Easy').toLowerCase()}`"
          >
            {{ getDifficultyLabel(problem.difficulty) }}
          </span>
        </div>
      </div>
      <div class="problem-score">{{ problem.score }}分</div>
    </div>
    <div class="problem-description">
      {{ problem.description }}
    </div>

    <div class="problem-actions">
      <t-button 
        variant="outline" 
        size="small" 
        @click="handleViewHints"
        style="margin-right: 8px;"
      >
        <t-icon name="help-circle" />
        查看提示
      </t-button>
      <div v-if="problem.solved" class="solved-badge">
        <t-icon name="check-circle" />
        <span>已解决</span>
      </div>
    </div>

    <div v-if="!problem.solved" class="flag-section">
      <FlagSubmission
          @submit-flag="handleSubmit"
          :disabled="!canSubmit"
      />
      <div v-if="!canSubmit" class="submit-disabled-tip">
        <t-icon name="info-circle" size="14" />
        <span>竞赛未开始或已结束，无法提交Flag</span>
      </div>
    </div>

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
import FlagSubmission from './FlagSubmission.vue'

const props = defineProps({
  problem: {
    type: Object,
    required: true
  },
  competitionStatus: {
    type: String,
    default: 'ongoing'
  },
  canSubmit: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['submit-flag', 'view-hints'])

const handleSubmit = (flag) => {
  if (!props.canSubmit) {
    return
  }
  emit('submit-flag', props.problem.id || props.problem.challengeID, flag)
}

const handleViewHints = () => {
  emit('view-hints', props.problem.id || props.problem.challengeID)
}

const getDifficultyLabel = (difficulty) => {
  const labels = {
    'Easy': '简单',
    'Medium': '中等',
    'Hard': '困难',
    'easy': '简单',
    'medium': '中等',
    'hard': '困难'
  }
  return labels[difficulty] || difficulty || '未知'
}
</script>

<style scoped>
.problem-card {
  background: #1a1f3a;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  margin-bottom: 16px;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
  position: relative;
  border: 1px solid #2a3458;
}

.problem-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
  border-color: #3b82f6;
}

.problem-card.solved {
  border-left-color: #10b981;
  background: linear-gradient(90deg, #064e3b 0%, #1a1f3a 100%);
  border-color: #10b981;
}

.problem-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.problem-title-section {
  flex: 1;
}

.problem-title {
  font-size: 20px;
  font-weight: 600;
  color: #e0e6ed;
  margin-bottom: 8px;
}

.problem-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.problem-category {
  padding: 4px 10px;
  background: #0f1629;
  border-radius: 6px;
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
  border: 1px solid #2a3458;
}

.problem-difficulty {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.difficulty-easy {
  background: #064e3b;
  color: #10b981;
  border: 1px solid #10b981;
}

.difficulty-medium {
  background: #78350f;
  color: #f59e0b;
  border: 1px solid #f59e0b;
}

.difficulty-hard {
  background: #7f1d1d;
  color: #ef4444;
  border: 1px solid #ef4444;
}

.problem-score {
  font-size: 24px;
  font-weight: 700;
  color: #3b82f6;
  white-space: nowrap;
}

.problem-description {
  font-size: 14px;
  color: #d1d5db;
  line-height: 1.8;
  margin-bottom: 16px;
  white-space: pre-wrap;
  word-break: break-word;
  min-height: 40px;
}

.problem-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.solved-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #10b981;
  font-size: 14px;
  font-weight: 600;
  padding: 6px 12px;
  background: #064e3b;
  border-radius: 6px;
  border: 1px solid #10b981;
}

.flag-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #2a3458;
}

.submit-disabled-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 6px;
  color: #ef4444;
  font-size: 12px;
}
</style>
