<script setup lang="ts">
import type { DeployStageState } from '../../types/enums';

defineProps<{ state: DeployStageState }>();

const CONFIG: Record<DeployStageState, { label: string; color: string }> = {
  SUCCESS: { label: 'Success', color: 'var(--dp-state-succeeded)' },
  FAILURE: { label: 'Failure', color: 'var(--dp-state-failed)' },
  ABORTED: { label: 'Aborted', color: 'var(--dp-state-cancelled)' },
  UNSTABLE: { label: 'Unstable', color: 'var(--color-approval-amber)' },
  SKIPPED: { label: 'Skipped', color: 'var(--dp-state-cancelled)' },
  IN_PROGRESS: { label: 'Running', color: 'var(--dp-state-in-progress)' },
  NOT_STARTED: { label: 'Waiting', color: 'var(--dp-state-pending)' },
};
</script>

<template>
  <span class="stage-chip" :style="{ '--chip-color': CONFIG[state].color }">
    {{ CONFIG[state].label }}
  </span>
</template>

<style scoped>
.stage-chip {
  display: inline-block;
  padding: 1px 6px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--chip-color);
  color: var(--chip-color);
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 600;
  text-transform: uppercase;
}
</style>
