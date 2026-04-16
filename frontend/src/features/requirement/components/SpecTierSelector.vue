<script setup lang="ts">
import type { OrchestratorResult, PipelineProfile } from '../types/requirement';

interface Props {
  profile: PipelineProfile;
  orchestratorResult: OrchestratorResult | null;
}

defineProps<Props>();
</script>

<template>
  <div v-if="profile.specTiering" class="tier-badge">
    <span class="tier-label">Tier</span>
    <span v-if="orchestratorResult?.determinedTier" class="tier-value">{{ orchestratorResult.determinedTier }}</span>
    <span v-else class="tier-placeholder">Tier will be determined by orchestrator</span>
  </div>
</template>

<style scoped>
.tier-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 8px;
  border-radius: 2px;
  background: rgba(167, 139, 250, 0.1);
  border: 1px solid rgba(167, 139, 250, 0.2);
}

.tier-label {
  font-family: var(--font-ui);
  font-size: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}

.tier-value {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  font-weight: 700;
  color: #a78bfa;
}

.tier-placeholder {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
  opacity: 0.7;
}
</style>
