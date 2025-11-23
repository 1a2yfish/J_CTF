<template>
  <div class="flag-submission">
    <div class="flag-input-wrapper">
      <t-input
          :model-value="flag"
          @update:model-value="flag = $event"
          class="flag-input"
          placeholder="请输入Flag（格式：CTF{...}）"
          @keyup.enter="submitFlag"
          size="large"
          :disabled="disabled"
      >
        <template #prefix-icon>
          <t-icon name="flag" />
        </template>
      </t-input>
      <t-button
          theme="primary"
          size="large"
          @click="submitFlag"
          :loading="loading"
          :disabled="!flag.trim() || disabled"
      >
        提交Flag
      </t-button>
    </div>
    <div class="flag-tip">
      <t-icon name="info-circle" size="16" />
      <span>提示：Flag格式通常为 CTF{...}，请仔细检查大小写和特殊字符</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { showWarning } from '@/utils/message'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  }
})

const flag = ref('')
const loading = ref(false)

const emit = defineEmits(['submit-flag'])

const submitFlag = async () => {
  if (props.disabled) {
    MessagePlugin.warning('当前无法提交Flag')
    return
  }
  
  if (!flag.value.trim()) {
    MessagePlugin.warning('请输入Flag')
    return
  }

  loading.value = true
  try {
    await emit('submit-flag', flag.value)
    // 提交成功后清空输入框
    flag.value = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.flag-submission {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e7e7e7;
}

.flag-input-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.flag-input {
  flex: 1;
}

.flag-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #86909c;
  margin-top: 8px;
}
</style>