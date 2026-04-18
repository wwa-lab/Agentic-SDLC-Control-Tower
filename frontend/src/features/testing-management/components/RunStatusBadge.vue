<script setup lang="ts">
import { computed } from 'vue';
import type { TestResultOutcome, TestRunState } from '../types';
import { outcomeTone } from '../utils';

interface Props {
  status: TestRunState | TestResultOutcome;
}

const props = defineProps<Props>();
const tone = computed(() => outcomeTone(props.status));
</script>

<template>
  <span class="tm-status" :class="`is-${tone}`">
    <span class="tm-status__dot"></span>
    <span>{{ status }}</span>
  </span>
</template>

<style scoped>
.tm-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.tm-status__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.tm-status.is-success {
  color: var(--tm-coverage-green);
  background: var(--tm-coverage-green-soft);
}

.tm-status.is-running {
  color: var(--tm-coverage-amber);
  background: var(--tm-coverage-amber-soft);
}

.tm-status.is-danger {
  color: var(--tm-coverage-red);
  background: var(--tm-coverage-red-soft);
}

.tm-status.is-muted {
  color: var(--tm-coverage-grey);
  background: var(--tm-coverage-grey-soft);
}
</style>
