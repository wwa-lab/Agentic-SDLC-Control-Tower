<script setup lang="ts">
import { computed } from 'vue';
import type { PrState } from '../types';

interface Props {
  state: PrState;
}

const props = defineProps<Props>();

const stateClass = computed(() => `pr-state--${props.state.toLowerCase()}`);
</script>

<template>
  <span class="pr-state" :class="stateClass">{{ state }}</span>
</template>

<style scoped>
.pr-state {
  display: inline-flex;
  align-items: center;
  padding: 4px 9px;
  border-radius: 999px;
  border: var(--border-ghost);
  font-size: 0.75rem;
  font-weight: 700;
}

.pr-state--open {
  color: var(--cb-link-ok);
}

.pr-state--merged {
  color: var(--cb-status-success);
}

.pr-state--closed {
  color: var(--cb-status-cancelled);
}

.pr-state--draft {
  color: var(--cb-status-neutral);
}
</style>

