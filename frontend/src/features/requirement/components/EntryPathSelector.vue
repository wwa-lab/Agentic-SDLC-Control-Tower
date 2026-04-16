<script setup lang="ts">
import type { OrchestratorResult, PipelineProfile } from '../types/requirement';

interface Props {
  profile: PipelineProfile;
  orchestratorResult: OrchestratorResult | null;
}

defineProps<Props>();
</script>

<template>
  <!-- Hidden for single-path profiles -->
  <div v-if="profile.entryPaths.length > 1 && orchestratorResult" class="entry-path-badge">
    <span class="path-label">Path</span>
    <span class="path-value">
      {{ profile.entryPaths.find(p => p.id === orchestratorResult!.determinedPathId)?.label ?? orchestratorResult!.determinedPathId }}
    </span>
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
</style>
