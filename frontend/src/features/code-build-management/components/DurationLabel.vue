<script setup lang="ts">
import { computed } from 'vue';
import { formatDuration, formatTimestamp } from '../utils';

interface Props {
  durationSeconds: number | null;
  timestamp?: string | null;
}

const props = withDefaults(defineProps<Props>(), {
  timestamp: null,
});

const label = computed(() => formatDuration(props.durationSeconds));
const tooltip = computed(() => props.timestamp ? formatTimestamp(props.timestamp) : label.value);
</script>

<template>
  <span class="duration-label" :title="tooltip">{{ label }}</span>
</template>

<style scoped>
.duration-label {
  font-family: var(--font-tech);
  color: var(--color-on-surface-variant);
  font-size: 0.75rem;
}
</style>

