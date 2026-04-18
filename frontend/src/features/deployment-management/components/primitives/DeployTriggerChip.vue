<script setup lang="ts">
import type { DeployTrigger } from '../../types/enums';

const props = defineProps<{
  trigger: DeployTrigger;
  actor?: string;
  redacted?: boolean;
}>();

const LABELS: Record<DeployTrigger, string> = {
  PUSH_TO_MAIN: 'Push',
  MANUAL: 'Manual',
  SCHEDULED: 'Scheduled',
  PROMOTE_FROM_DEV: 'Promote (Dev)',
  PROMOTE_FROM_TEST: 'Promote (Test)',
  PROMOTE_FROM_STAGING: 'Promote (Staging)',
  ROLLBACK: 'Rollback',
};

const CSS_VARS: Record<string, string> = {
  MANUAL: 'var(--dp-trigger-manual)',
  SCHEDULED: 'var(--dp-trigger-scheduled)',
  ROLLBACK: 'var(--dp-trigger-rollback)',
};

const color = CSS_VARS[props.trigger] ?? 'var(--dp-trigger-webhook)';
const displayActor = props.redacted ? '(redacted)' : props.actor;
</script>

<template>
  <span
    class="trigger-chip"
    :style="{ '--chip-color': color }"
    :title="displayActor ? `Triggered by ${displayActor}` : undefined"
  >
    {{ LABELS[trigger] }}
  </span>
</template>

<style scoped>
.trigger-chip {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--chip-color);
  color: var(--chip-color);
  font-family: var(--font-ui);
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  white-space: nowrap;
}
</style>
