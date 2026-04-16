<script setup lang="ts">
import type { PipelineProfile } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';

interface Props {
  profile: PipelineProfile;
  isLoading?: boolean;
}

defineProps<Props>();
</script>

<template>
  <RequirementCard title="Pipeline Chain" :is-loading="isLoading">
    <div class="profile-chain">
      <div
        v-for="(node, idx) in profile.chainNodes"
        :key="node.id"
        class="chain-node"
        :class="{ 'chain-node--hub': node.isExecutionHub }"
      >
        <span class="node-label">{{ node.label }}</span>
        <span v-if="idx < profile.chainNodes.length - 1" class="chain-arrow">→</span>
      </div>
    </div>
  </RequirementCard>
</template>

<style scoped>
.profile-chain {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-wrap: wrap;
}

.chain-node {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
}

.chain-node--hub {
  border-color: var(--color-secondary);
  box-shadow: 0 0 8px var(--color-secondary-glow);
}

.chain-node--hub .node-label {
  color: var(--color-secondary);
  font-weight: 600;
}

.node-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-on-surface-variant);
}

.chain-arrow {
  color: var(--color-on-surface-variant);
  opacity: 0.4;
  font-size: 0.75rem;
  margin: 0 2px;
}
</style>
