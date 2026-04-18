<script setup lang="ts">
import type { BuildHealthPoint } from '../types';

interface Props {
  points: ReadonlyArray<BuildHealthPoint>;
}

defineProps<Props>();

defineEmits<{
  openRun: [runId: string];
}>();
</script>

<template>
  <div class="sparkline" aria-label="Recent build history">
    <button
      v-for="point in points"
      :key="`${point.runId}-${point.label}`"
      class="sparkline__point"
      :class="`sparkline__point--${point.status.toLowerCase()}`"
      :title="`${point.label}: ${point.status}`"
      type="button"
      @click="$emit('openRun', point.runId)"
    />
  </div>
</template>

<style scoped>
.sparkline {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(10px, 1fr));
  gap: 4px;
}

.sparkline__point {
  height: 18px;
  border-radius: 3px;
  border: none;
  cursor: pointer;
  opacity: 0.9;
}

.sparkline__point--success {
  background: var(--cb-status-success);
}

.sparkline__point--failure {
  background: var(--cb-status-failure);
}

.sparkline__point--running,
.sparkline__point--queued {
  background: var(--cb-status-running);
}

.sparkline__point--cancelled {
  background: var(--cb-status-cancelled);
}

.sparkline__point--neutral {
  background: var(--cb-status-neutral);
}
</style>

