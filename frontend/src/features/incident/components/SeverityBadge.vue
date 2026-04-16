<script setup lang="ts">
import type { Priority } from '../types/incident';

interface Props {
  priority: Priority;
}

defineProps<Props>();
</script>

<template>
  <span class="severity-badge" :class="`severity--${priority.toLowerCase()}`">
    {{ priority }}
  </span>
</template>

<style scoped>
.severity-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-tech);
  font-size: 0.625rem;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 2px;
  letter-spacing: 0.05em;
}

.severity--p1 {
  color: #fff;
  background: var(--color-incident-crimson);
  box-shadow: 0 0 8px var(--color-incident-tint);
}

.severity--p2 {
  color: var(--color-incident-crimson);
  background: var(--color-incident-tint);
}

.severity--p3 {
  color: var(--color-approval-amber);
  background: rgba(245, 158, 11, 0.1);
}

.severity--p4 {
  color: var(--color-on-surface-variant);
  background: rgba(148, 163, 184, 0.1);
}
</style>
