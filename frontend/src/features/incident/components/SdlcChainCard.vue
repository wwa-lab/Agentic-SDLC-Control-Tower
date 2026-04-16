<script setup lang="ts">
import type { SdlcChain, SectionResult } from '../types/incident';
import IncidentCard from './IncidentCard.vue';

interface Props {
  chain: SectionResult<SdlcChain>;
  isLoading?: boolean;
}

defineProps<Props>();
const emit = defineEmits<{ navigate: [routePath: string] }>();

const TYPE_LABELS: Record<string, string> = {
  requirement: 'REQ',
  spec: 'SPEC',
  design: 'DSN',
  code: 'CODE',
  test: 'TEST',
  deploy: 'DEP',
};
</script>

<template>
  <IncidentCard title="Related SDLC Chain" :is-loading="isLoading" :error="chain.error">
    <div v-if="chain.data" class="chain-content">
      <div v-if="chain.data.links.length === 0" class="empty-state">
        No linked artifacts
      </div>
      <div v-else class="chain-links">
        <div
          v-for="(link, i) in chain.data.links"
          :key="i"
          class="chain-link"
          :class="{ 'chain-link--spec': link.artifactType === 'spec' }"
          @click="emit('navigate', link.routePath)"
        >
          <span class="link-type">{{ TYPE_LABELS[link.artifactType] ?? link.artifactType }}</span>
          <span class="link-id">{{ link.artifactId }}</span>
          <span class="link-title">{{ link.artifactTitle }}</span>
        </div>
      </div>
    </div>
  </IncidentCard>
</template>

<style scoped>
.chain-content { display: flex; flex-direction: column; gap: 8px; }

.empty-state {
  font-family: var(--font-ui); font-size: 0.75rem;
  color: var(--color-on-surface-variant); text-align: center; padding: 16px;
}

.chain-links { display: flex; gap: 8px; flex-wrap: wrap; }

.chain-link {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 12px;
  background: var(--color-surface-container-low);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.2s ease;
  min-width: 120px;
}

.chain-link:hover { background: var(--color-surface-container-highest); }

.chain-link--spec {
  border: 1px solid var(--color-secondary-subtle);
  background: var(--color-secondary-tint);
}

.link-type {
  font-family: var(--font-ui);
  font-size: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-on-surface-variant);
}

.chain-link--spec .link-type { color: var(--color-secondary); }

.link-id {
  font-family: var(--font-tech);
  font-size: 0.6875rem;
  color: var(--color-secondary);
}

.link-title {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}
</style>
