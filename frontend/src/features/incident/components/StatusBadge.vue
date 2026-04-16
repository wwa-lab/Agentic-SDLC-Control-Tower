<script setup lang="ts">
import type { IncidentStatus } from '../types/incident';

interface Props {
  status: IncidentStatus;
}

const props = defineProps<Props>();

const ACTIVE_STATUSES = new Set<IncidentStatus>([
  'AI_INVESTIGATING', 'EXECUTING', 'DETECTED',
]);

const STATUS_COLORS: Record<string, string> = {
  DETECTED: 'led-crimson',
  AI_INVESTIGATING: 'led-amber',
  AI_DIAGNOSED: 'led-amber',
  ACTION_PROPOSED: 'led-amber',
  PENDING_APPROVAL: 'led-amber',
  EXECUTING: 'led-amber',
  RESOLVED: 'led-green',
  LEARNING: 'led-green',
  CLOSED: 'led-muted',
  ESCALATED: 'led-crimson',
  MANUAL_OVERRIDE: 'led-crimson',
};

const displayLabel = (s: IncidentStatus) => s.replace(/_/g, ' ');
</script>

<template>
  <span class="status-badge">
    <span class="led" :class="[STATUS_COLORS[props.status], { 'led-pulse': ACTIVE_STATUSES.has(props.status) }]"></span>
    <span class="status-text">{{ displayLabel(props.status) }}</span>
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

.led-crimson { background: var(--color-incident-crimson); }
.led-amber { background: var(--color-approval-amber); }
.led-green { background: var(--color-health-emerald); }
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
