<script setup lang="ts">
import type { OrchestratorResult, PipelineProfile } from '../types/requirement';

interface Props {
  profile: PipelineProfile;
  orchestratorResult: OrchestratorResult | null;
}

const props = defineProps<Props>();

function resolvedPathLabel(): string {
  if (!props.orchestratorResult) {
    return '';
  }
  return props.profile.entryPaths.find(p => p.id === props.orchestratorResult?.determinedPathId)?.label
    ?? props.orchestratorResult.determinedPathId;
}
</script>

<template>
  <div v-if="profile.entryPaths.length > 1" class="entry-path-badge">
    <span class="path-label">Path</span>
    <span v-if="orchestratorResult" class="path-value">
      {{ resolvedPathLabel() }}
    </span>
    <span v-else class="path-placeholder">Path will be determined by orchestrator</span>
  </div>
</template>

<style scoped>
.entry-path-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 8px;
  border-radius: 2px;
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
}

.path-label {
  font-family: var(--font-ui);
  font-size: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}

.path-value {
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  color: var(--color-secondary);
}

.path-placeholder {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
  opacity: 0.7;
}
</style>
