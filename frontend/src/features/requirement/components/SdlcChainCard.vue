<script setup lang="ts">
import { ref } from 'vue';
import type { SectionResult } from '../types/requirement';
import type { SdlcChain, SdlcArtifactType } from '@/shared/types/sdlc-chain';
import RequirementCard from './RequirementCard.vue';

interface Props {
  chain: SectionResult<SdlcChain>;
  isLoading?: boolean;
}

defineProps<Props>();
const emit = defineEmits<{ navigate: [path: string] }>();
const expanded = ref(false);

const ARTIFACT_LABELS: Record<SdlcArtifactType, string> = {
  requirement: 'REQ',
  'user-story': 'STORY',
  spec: 'SPEC',
  architecture: 'ARCH',
  design: 'DESIGN',
  tasks: 'TASKS',
  code: 'CODE',
  test: 'TEST',
  deploy: 'DEPLOY',
};
</script>

<template>
  <RequirementCard
    title="SDLC Chain"
    :is-loading="isLoading"
    :error="chain.error"
  >
    <div v-if="chain.data" class="chain-content">
      <div v-if="chain.data.links.length === 0" class="empty">
        No downstream artifacts yet
      </div>

      <div v-else class="chain-flow">
        <div
          v-for="(link, idx) in chain.data.links"
          :key="link.artifactId"
          class="chain-node"
          :class="{ 'chain-node--spec': link.artifactType === 'spec' }"
          @click="emit('navigate', link.routePath)"
        >
          <span class="node-type">{{ ARTIFACT_LABELS[link.artifactType] }}</span>
          <span class="node-id">{{ link.artifactId }}</span>
          <span v-if="expanded" class="node-title">{{ link.artifactTitle }}</span>
          <span v-if="idx < chain.data!.links.length - 1" class="chain-arrow">→</span>
        </div>
      </div>

      <button
        v-if="chain.data.links.length > 0"
        class="expand-btn"
        @click="expanded = !expanded"
      >
        {{ expanded ? 'Collapse' : 'Expand' }}
      </button>
    </div>
  </RequirementCard>
</template>

<style scoped>
.chain-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chain-flow {
  display: flex;
  align-items: center;
  gap: 4px;
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
  cursor: pointer;
  transition: all 0.15s ease;
}

.chain-node:hover { background: var(--nav-hover-bg); }

.chain-node--spec {
  border-color: var(--color-secondary);
  box-shadow: 0 0 8px var(--color-secondary-glow);
}

.node-type {
  font-family: var(--font-ui);
  font-size: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
  padding: 1px 4px;
  background: var(--color-surface-container-highest);
  border-radius: 2px;
}

.chain-node--spec .node-type {
  color: var(--color-secondary);
  background: var(--color-secondary-tint);
}

.node-id {
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  color: var(--color-secondary);
}

.node-title {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  color: var(--color-on-surface);
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chain-arrow {
  color: var(--color-on-surface-variant);
  opacity: 0.4;
  font-size: 0.75rem;
  margin: 0 2px;
}

.expand-btn {
  background: none;
  border: none;
  color: var(--color-secondary);
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  cursor: pointer;
  padding: 2px 0;
  align-self: flex-start;
  opacity: 0.7;
  transition: opacity 0.2s ease;
}

.expand-btn:hover { opacity: 1; }

.empty {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
  opacity: 0.5;
  text-align: center;
  padding: 16px 0;
}
</style>
