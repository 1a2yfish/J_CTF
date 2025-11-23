<template>
  <div class="competition-card" @click="$emit('click')">
    <div class="competition-header">
      <div class="competition-title">{{ competition.title || competition.Title || '未命名竞赛' }}</div>
      <div
          class="competition-status"
          :class="statusClass"
      >
        {{ statusLabel }}
      </div>
    </div>
    <div class="competition-description">
      {{ competition.description || competition.introduction || competition.Introduction || '暂无描述' }}
    </div>
    <div class="competition-meta">
      <div class="meta-item">
        <t-icon name="time" /> {{ formatTime(competition.startTime || competition.startAt || competition.StartTime) }} - {{ formatTime(competition.endTime || competition.endAt || competition.EndTime) }}
      </div>
      <div class="meta-item">
        <t-icon name="user" /> {{ competition.participants || competition.participantCount || 0 }}人
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

// 根据时间计算竞赛状态
const computedStatus = computed(() => {
  const startTime = props.competition.startTime || props.competition.startAt || props.competition.StartTime
  const endTime = props.competition.endTime || props.competition.endAt || props.competition.EndTime
  
  if (!startTime || !endTime) {
    // 如果没有时间信息，使用后端返回的状态
    const status = props.competition.status || props.competition.Status || ''
    return status.toLowerCase()
  }
  
  try {
    const now = new Date()
    // 确保正确解析时间字符串（处理各种格式）
    let start = new Date(startTime)
    let end = new Date(endTime)
    
    // 如果解析失败，尝试其他格式
    if (isNaN(start.getTime())) {
      // 尝试处理带时区的时间字符串
      start = new Date(startTime.replace(' ', 'T'))
    }
    if (isNaN(end.getTime())) {
      end = new Date(endTime.replace(' ', 'T'))
    }
    
    // 如果还是无效，使用后端状态
    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
      const status = props.competition.status || props.competition.Status || ''
      return status.toLowerCase()
    }
    
    // 优先根据时间判断状态，而不是后端状态
    // 使用 getTime() 进行精确比较
    const nowTime = now.getTime()
    const startTime_ms = start.getTime()
    const endTime_ms = end.getTime()
    
    if (nowTime < startTime_ms) {
      return 'upcoming'
    } else if (nowTime >= startTime_ms && nowTime <= endTime_ms) {
      return 'ongoing'
    } else {
      return 'finished'
    }
  } catch (e) {
    console.error('计算竞赛状态失败:', e, 'startTime:', startTime, 'endTime:', endTime)
    const status = props.competition.status || props.competition.Status || ''
    return status.toLowerCase()
  }
})

const statusClass = computed(() => {
  const status = computedStatus.value
  switch (status) {
    case 'active':
    case 'ongoing':
      return 'status-active'
    case 'upcoming':
    case 'published':
      return 'status-upcoming'
    case 'ended':
    case 'finished':
      return 'status-ended'
    default: return ''
  }
})

const statusLabel = computed(() => {
  const status = computedStatus.value
  switch (status) {
    case 'active':
    case 'ongoing':
      return '进行中'
    case 'upcoming':
    case 'published':
      return '即将开始'
    case 'ended':
    case 'finished':
      return '已结束'
    default: return '未知状态'
  }
})

const formatTime = (time) => {
  if (!time) return ''
  if (typeof time === 'string') {
    // 如果是ISO格式字符串，转换为本地时间
    try {
      const date = new Date(time)
      return date.toLocaleString('zh-CN', { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    } catch (e) {
      return time
    }
  }
  return time
}
</script>

<style scoped>
.competition-card {
  background: linear-gradient(135deg, #1a1f3a 0%, #1e2542 100%);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(59, 130, 246, 0.1);
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.competition-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #3b82f6, #60a5fa, #3b82f6);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.competition-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.4);
  border-color: rgba(59, 130, 246, 0.5);
}

.competition-card:hover::before {
  opacity: 1;
}

.competition-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 12px;
}

.competition-title {
  font-size: 20px;
  font-weight: 600;
  color: #e0e6ed;
  flex: 1;
  line-height: 1.4;
  word-break: break-word;
}

.competition-status {
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.status-active {
  background: #064e3b;
  color: #10b981;
  border: 1px solid #10b981;
}

.status-upcoming {
  background: #78350f;
  color: #f59e0b;
  border: 1px solid #f59e0b;
}

.status-ended {
  background: #1f2937;
  color: #9ca3af;
  border: 1px solid #374151;
}

.competition-description {
  font-size: 14px;
  color: #d1d5db;
  line-height: 1.6;
  margin-bottom: 20px;
  min-height: 44px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.competition-meta {
  display: flex;
  gap: 20px;
  color: #9ca3af;
  font-size: 13px;
  flex-wrap: wrap;
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid rgba(42, 52, 88, 0.5);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-item .t-icon {
  color: #3b82f6;
}
</style>