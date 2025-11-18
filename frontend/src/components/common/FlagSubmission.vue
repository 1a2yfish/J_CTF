<template>
  <div class="flag-submission">
    <t-input
        v-model="flag"
        class="flag-input"
        placeholder="请输入Flag"
        @keyup.enter="submitFlag"
    />
    <t-button
        theme="primary"
        @click="submitFlag"
        :loading="loading"
    >
      提交
    </t-button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Message } from 'tdesign-vue-next'

const flag = ref('')
const loading = ref(false)

const emit = defineEmits(['submit-flag'])

const submitFlag = () => {
  if (!flag.value.trim()) {
    Message.warning('请输入Flag')
    return
  }

  loading.value = true
  emit('submit-flag', flag.value)
  flag.value = ''
  loading.value = false
}
</script>