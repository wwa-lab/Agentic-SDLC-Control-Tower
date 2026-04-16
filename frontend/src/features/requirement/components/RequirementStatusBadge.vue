<script setup lang="ts">
import type { RequirementStatus } from '../types/requirement';

interface Props {
  status: RequirementStatus;
}

const props = defineProps<Props>();

const ACTIVE_STATUSES = new Set<RequirementStatus>(['In Review', 'In Progress']);

const STATUS_COLORS: Record<RequirementStatus, string> = {
  'Draft': 'led-muted',
  'In Review': 'led-amber',
  'Approved': 'led-green',
  'In Progress': 'led-cyan',
  'Delivered': 'led-green',
  'Archived': 'led-muted',
};
</script>

<template>
  <span class="status-badge">
    <span
      class="led"
      :class="[STATUS_COLORS[props.status], { 'led-pulse': ACTIVE_STATUSES.has(props.status) }]"
    ></span>
    <span class="status-text">{{ props.status }}</span>
  </span>
</template>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.led {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.led-green { background: var(--color-health-emerald); }
.led-cyan { background: var(--color-secondary); }
.led-amber { background: var(--color-approval-amber); }
.led-muted { background: var(--color-on-surface-variant); opacity: 0.4; }

.led-pulse {
  animation: pulse-led 2s ease-in-out infinite;
}

.status-text {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-on-surface-variant);
}

@keyframes pulse-led {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
