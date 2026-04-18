<script setup lang="ts">
import type { DeployState } from '../../types/enums';

const props = defineProps<{ state: DeployState }>();

const CONFIG: Record<DeployState, { label: string; cssVar: string; shape: string }> = {
  PENDING: { label: 'Pending', cssVar: 'var(--dp-state-pending)', shape: 'circle' },
  IN_PROGRESS: { label: 'In Progress', cssVar: 'var(--dp-state-in-progress)', shape: 'pulse' },
  SUCCEEDED: { label: 'Succeeded', cssVar: 'var(--dp-state-succeeded)', shape: 'check' },
  FAILED: { label: 'Failed', cssVar: 'var(--dp-state-failed)', shape: 'cross' },
  CANCELLED: { label: 'Cancelled', cssVar: 'var(--dp-state-cancelled)', shape: 'dash' },
  ROLLED_BACK: { label: 'Rolled Back', cssVar: 'var(--dp-state-rolled-back)', shape: 'rewind' },
};

const cfg = CONFIG[props.state];
</script>

<template>
  <span
    class="deploy-state-badge"
    :class="[`shape-${cfg.shape}`, { animating: state === 'IN_PROGRESS' }]"
    :style="{ '--badge-color': cfg.cssVar }"
    :aria-label="`Deploy state: ${cfg.label}`"
  >
    <span class="dot" />
    <span class="label">{{ cfg.label }}</span>
  </span>
</template>

<style scoped>
.deploy-state-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  color: var(--badge-color);
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--badge-color);
  flex-shrink: 0;
}
.animating .dot {
  animation: pulse-dot 1.4s ease-in-out infinite;
}
@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(1.3); }
}
</style>
