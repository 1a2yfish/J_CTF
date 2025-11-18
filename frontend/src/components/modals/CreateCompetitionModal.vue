<template>
  <t-dialog
      header="创建新竞赛"
      :visible="visible"
      @close="handleClose"
      :footer="false"
      width="600px"
  >
    <t-form :data="formData" label-width="100px" @submit="createCompetition">
      <t-form-item label="竞赛名称" name="title" :rules="[{ required: true, message: '请输入竞赛名称' }]">
        <t-input v-model="formData.title" placeholder="例如：2023网络安全挑战赛" />
      </t-form-item>
      <t-form-item label="竞赛描述" name="description" :rules="[{ required: true, message: '请输入竞赛描述' }]">
        <t-textarea v-model="formData.description" :rows="3" placeholder="请输入竞赛详细描述" />
      </t-form-item>
      <t-form-item label="开始时间" name="startAt" :rules="[{ required: true, message: '请选择开始时间' }]">
        <t-date-time-picker v-model="formData.startAt" />
      </t-form-item>
      <t-form-item label="结束时间" name="endAt" :rules="[{ required: true, message: '请选择结束时间' }]">
        <t-date-time-picker v-model="formData.endAt" />
      </t-form-item>
      <t-form-item label="发布状态">
        <t-switch v-model="formData.published" />
        <span style="margin-left: 10px;">{{ formData.published ? '已发布' : '未发布' }}</span>
      </t-form-item>
      <div style="display: flex; justify-content: flex-end; margin-top: 20px;">
        <t-button theme="default" style="margin-right: 10px;" @click="handleClose">取消</t-button>
        <t-button theme="primary" type="submit" :loading="loading">创建竞赛</t-button>
      </div>
    </t-form>
  </t-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useCompetitionStore } from '@/stores/competitionStore'
import { Message } from 'tdesign-vue-next'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'competition-created'])

const competitionStore = useCompetitionStore()
const loading = ref(false)

const formData = ref({
  title: '',
  description: '',
  startAt: new Date(),
  endAt: new Date(new Date().getTime() + 24 * 60 * 60 * 1000),
  published: true
})

const handleClose = () => {
  emit('close')
}

const createCompetition = async () => {
  if (!formData.value.title || !formData.value.description) {
    Message.warning('请填写必要信息')
    return
  }

  try {
    loading.value = true
    const competition = await competitionStore.createCompetition(formData.value)
    Message.success('竞赛创建成功')
    emit('competition-created', competition)
    handleClose()
  } catch (error) {
    Message.error(error.message || '创建竞赛失败')
  } finally {
    loading.value = false
  }
}

// 重置表单
watch(() => props.visible, (newVal) => {
  if (newVal) {
    formData.value = {
      title: '',
      description: '',
      startAt: new Date(),
      endAt: new Date(new Date().getTime() + 24 * 60 * 60 * 1000),
      published: true
    }
  }
})
</script>