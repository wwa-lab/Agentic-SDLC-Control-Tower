<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ seconds?: number; startedAt?: string }>();

const display = computed(() => {
  if (props.seconds == null) return '\u2014';
  const m = Math.floor(props.seconds / 60);
  const s = props.seconds % 60;
  return m > 0 ? `${m}m ${s}s` : `${s}s`;
});

const tooltip = computed(() => {
  if (!props.startedAt) return undefined;
  return `Started: ${new Date(props.startedAt).toLocaleString()}`;
});
</script>

<template>
  <span class="duration-pill" :title="tooltip">{{ display }}</span>
</template>

<style scoped>
.duration-pill {
  font-family: var(--font-tech);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
  white-space: nowrap;
}
</style>
