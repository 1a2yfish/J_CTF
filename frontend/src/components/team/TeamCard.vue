<template>
  <div class="team-card" v-if="team">
    <div class="team-header">
      <div class="team-name">{{ team.teamName || team.name || '未知团队' }}</div>
      <t-tag v-if="isCurrentTeam" theme="primary" variant="light">我的团队</t-tag>
      <t-tag v-if="team.auditState === '0'" theme="warning" variant="light">待审核</t-tag>
      <t-tag v-else-if="team.auditState === '1'" theme="success" variant="light">已通过</t-tag>
      <t-tag v-else-if="team.auditState === '2'" theme="danger" variant="light">已拒绝</t-tag>
    </div>
    <div class="team-meta">
      <div class="meta-item" v-if="team.captainName || team.captain?.userName">
        <t-icon name="user" />
        <span>队长: {{ team.captainName || team.captain?.userName }}</span>
      </div>
      <div class="meta-item" v-if="team.members">
        <t-icon name="users" />
        <span>成员: {{ team.members.length || 0 }} 人</span>
      </div>
      <div class="meta-item" v-if="team.competitionName || team.competition?.title">
        <t-icon name="flag" />
        <span>竞赛: {{ team.competitionName || team.competition?.title }}</span>
      </div>
      <div class="meta-item" v-if="team.creationTime">
        <t-icon name="time" />
        <span>创建时间: {{ formatTime(team.creationTime) }}</span>
      </div>
    </div>
    <div class="team-description" v-if="team.description">
      {{ team.description }}
    </div>
    <div v-else class="team-description empty">
      暂无团队描述
    </div>
    
    <!-- WriteUp信息区域 -->
    <div class="team-writeup-section" v-if="isCurrentTeam && (team.competitionID || team.competition?.competitionID)">
      <div class="writeup-actions">
        <t-button
          theme="default"
          variant="text"
          size="small"
          @click="$emit('view-writeup', team)"
          style="color: #3b82f6; padding: 4px 8px;"
        >
          <t-icon name="file-text" style="margin-right: 4px;" />
          查看WriteUp
        </t-button>
      </div>
    </div>
    
    <div class="team-actions">
      <t-button
        theme="default"
        variant="outline"
        size="small"
        @click="$emit('view-members', team)"
      >
        <t-icon name="users" style="margin-right: 4px;" />
        查看成员
      </t-button>
      <t-button
        v-if="isCurrentTeam && team.competitionID"
        theme="primary"
        variant="outline"
        size="small"
        @click="$emit('submit-writeup', team)"
      >
        <t-icon name="file-text" style="margin-right: 4px;" />
        提交WriteUp
      </t-button>
      <t-button
        v-if="isCurrentTeam && (team.auditState === '1' || team.auditState === 'APPROVED')"
        theme="danger"
        variant="outline"
        size="small"
        @click="$emit('disband-team', team)"
      >
        <t-icon name="delete" style="margin-right: 4px;" />
        解散团队
      </t-button>
    </div>
  </div>
</template>

<script setup>
// defineProps and defineEmits are compiler macros, no need to import

const props = defineProps({
  team: {
    type: Object,
    required: true,
    default: null
  },
  isCurrentTeam: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['view-members', 'edit-description', 'disband-team', 'submit-writeup', 'view-writeup'])

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.team-card {
  background: #1a1f3a;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  margin-bottom: 16px;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
  border: 1px solid #2a3458;
}

.team-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
  border-left-color: #3b82f6;
  border-color: #3b82f6;
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}

.team-name {
  font-size: 20px;
  font-weight: 600;
  color: #e0e6ed;
  flex: 1;
}

.team-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
  color: #9ca3af;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-item .t-icon {
  color: #3b82f6;
}

.team-description {
  font-size: 14px;
  color: #d1d5db;
  line-height: 1.6;
  padding: 12px;
  background: #0f1629;
  border-radius: 6px;
  border: 1px solid #2a3458;
}

.team-description.empty {
  color: #6b7280;
  font-style: italic;
}

.team-writeup-section {
  margin-top: 12px;
  padding: 12px;
  background: #0f1629;
  border-radius: 6px;
  border: 1px solid #2a3458;
}

.writeup-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.team-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #2a3458;
  flex-wrap: wrap;
}
</style>
