<script setup lang="ts">
import type { RequirementPriority } from '../types/requirement';

interface Props {
  priority: RequirementPriority;
}

defineProps<Props>();
</script>

<template>
  <span class="priority-badge" :class="`priority--${priority.toLowerCase()}`">
    {{ priority }}
  </span>
</template>

<style scoped>
.priority-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-tech);
  font-size: 0.625rem;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 2px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.priority--critical {
  color: #fff;
  background: var(--color-incident-crimson);
  box-shadow: 0 0 8px var(--color-incident-tint);
}

.priority--high {
  color: var(--color-approval-amber);
  background: rgba(245, 158, 11, 0.12);
}

.priority--medium {
  color: var(--color-secondary);
  background: var(--color-secondary-tint);
}

.priority--low {
  color: var(--color-on-surface-variant);
  background: rgba(148, 163, 184, 0.1);
}
</style>
