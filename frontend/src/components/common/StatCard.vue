<template>
  <div class="stat-card">
    <div class="stat-title">{{ title }}</div>
    <div class="stat-value">{{ value }}</div>
    <div class="stat-description">{{ description }}</div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  description: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.stat-card {
  background: #1a1f3a;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
  border: 1px solid #2a3458;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.3);
  border-left-color: #3b82f6;
  border-color: #3b82f6;
}

.stat-title {
  font-size: 14px;
  color: #9ca3af;
  margin-bottom: 12px;
  font-weight: 500;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #e0e6ed;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-description {
  font-size: 12px;
  color: #9ca3af;
  line-height: 1.5;
}
</style>
