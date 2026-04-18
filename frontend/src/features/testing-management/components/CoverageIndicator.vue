<script setup lang="ts">
import { computed } from 'vue';
import type { CoverageStatus } from '../types';
import { coverageLabel, formatPercent, toCoverageVars } from '../utils';

interface Props {
  status: CoverageStatus;
  ratio?: number | null;
  compact?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  ratio: null,
  compact: false,
});

const colors = computed(() => toCoverageVars(props.status));
const label = computed(() => coverageLabel(props.status));
const ratioLabel = computed(() => props.ratio == null ? null : formatPercent(props.ratio));
</script>

<template>
  <span
    class="tm-coverage"
    :class="{ compact: props.compact }"
    :style="{ '--tm-tone': colors.tone, '--tm-background': colors.background }"
  >
    <span class="tm-coverage__shape" aria-hidden="true"></span>
    <span>{{ label }}</span>
    <span v-if="ratioLabel" class="tm-coverage__ratio">{{ ratioLabel }}</span>
  </span>
</template>

<style scoped>
.tm-coverage {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 999px;
  background: var(--tm-background);
  color: var(--tm-tone);
  border: 1px solid color-mix(in srgb, var(--tm-tone) 28%, transparent);
  font-size: 0.75rem;
  font-weight: 600;
}

.tm-coverage.compact {
  padding: 4px 8px;
}

.tm-coverage__shape {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  background: var(--tm-tone);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--tm-tone) 16%, transparent);
}

.tm-coverage__ratio {
  color: var(--color-on-surface-variant);
}
</style>
