<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  current: number;
  total: number;
}

const props = defineProps<Props>();

const percentage = computed(() =>
  props.total > 0 ? Math.round((props.current / props.total) * 100) : 0
);
</script>

<template>
  <div class="batch-progress">
    <div class="progress-track">
      <div class="progress-fill" :style="{ width: `${percentage}%` }"></div>
    </div>
    <span class="progress-text">{{ current }} of {{ total }} normalized...</span>
  </div>
</template>

<style scoped>
.batch-progress {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 0;
}

.progress-track {
  height: 6px;
  background: var(--color-surface-container-low);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-secondary), #a78bfa);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
  text-align: center;
}
</style>
