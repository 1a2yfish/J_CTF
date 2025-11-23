<template>
  <t-dialog
      :header="hasTeam ? '管理团队' : '加入/创建团队'"
      :visible="visible"
      @close="handleClose"
      :footer="false"
      width="500px"
  >
    <div v-if="!hasTeam">
      <t-form :data="formData" label-width="90px" @submit="createTeam">
        <t-form-item label="团队名称" name="teamName" :rules="[{ required: true, message: '请输入团队名称' }]">
          <t-input v-model="formData.teamName" placeholder="请输入团队名称" />
        </t-form-item>
        <t-form-item label="邀请码" name="inviteCode">
          <t-input v-model="formData.inviteCode" placeholder="如果有邀请码请填写" />
        </t-form-item>
        <t-alert theme="info" style="margin: 15px 0;">
          创建团队后，您可以邀请其他用户加入（最多5人）
        </t-alert>
        <div style="display: flex; justify-content: flex-end; margin-top: 20px;">
          <t-button theme="default" style="margin-right: 10px;" @click="handleClose">取消</t-button>
          <t-button theme="primary" type="submit" :loading="loading">创建团队</t-button>
        </div>
      </t-form>
    </div>
    <div v-else>
      <div style="text-align: center; padding: 20px 0;">
        <div style="font-size: 1.5rem; margin-bottom: 10px;">🛡️</div>
        <h3 style="margin-bottom: 20px;">{{ currentTeam.teamName || currentTeam.name }}</h3>
        <div style="display: flex; justify-content: center; margin-bottom: 20px;">
          <div
              class="member-avatar"
              v-for="(member, idx) in currentTeam.members"
              :key="idx"
              style="margin-right: 8px;"
          >
            {{ member.name.charAt(0) }}
          </div>
        </div>
        <t-button theme="default" style="margin-right: 10px;">邀请成员</t-button>
        <t-button theme="primary">团队设置</t-button>
      </div>
    </div>
  </t-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useTeamStore } from '@/stores/teamStore'
import { Message } from 'tdesign-vue-next'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  competitionId: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits(['close', 'team-joined'])

const teamStore = useTeamStore()
const loading = ref(false)
const hasTeam = ref(false)
const currentTeam = ref(null)

const formData = ref({
  teamName: '',
  inviteCode: ''
})

const handleClose = () => {
  emit('close')
}

const createTeam = async () => {
  if (!formData.value.teamName) {
    Message.warning('请输入团队名称')
    return
  }

  try {
    loading.value = true
    await teamStore.createTeam({
      teamName: formData.value.teamName,
      competitionID: props.competitionId,
      competitionId: props.competitionId,
      inviteCode: formData.value.inviteCode
    })

    Message.success('团队创建成功')
    emit('team-joined')
    handleClose()
  } catch (error) {
    Message.error(error.message || '创建团队失败')
  } finally {
    loading.value = false
  }
}

// 检查用户是否已加入团队
watch(() => props.visible, async (newVal) => {
  if (newVal) {
    try {
      hasTeam.value = await teamStore.hasTeamForCompetition(props.competitionId)
      if (hasTeam.value) {
        currentTeam.value = await teamStore.getCurrentTeam(props.competitionId)
      }
    } catch (error) {
      console.error('检查团队状态失败:', error)
      Message.error('无法获取团队信息')
    }
  }
})
</script>