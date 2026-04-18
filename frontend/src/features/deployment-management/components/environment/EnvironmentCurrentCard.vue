<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { EnvironmentRevisions } from '../../types/environment';
import ReleaseVersionPill from '../primitives/ReleaseVersionPill.vue';

defineProps<{ section: SectionResult<EnvironmentRevisions> }>();
const emit = defineEmits<{ openDeploy: [deployId: string] }>();

interface RevisionSlot {
  readonly label: string;
  readonly revision: string | null;
  readonly deployId: string | null;
}

function slots(data: EnvironmentRevisions): ReadonlyArray<RevisionSlot> {
  return [
    { label: 'Current', revision: data.currentRevision, deployId: data.currentDeployId },
    { label: 'Prior', revision: data.priorRevision, deployId: data.priorDeployId },
    { label: 'Last Good', revision: data.lastGoodRevision, deployId: data.lastGoodDeployId },
    { label: 'Last Failed', revision: data.lastFailedRevision, deployId: data.lastFailedDeployId },
  ];
}
</script>

<template>
  <div class="current-card card">
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading current revisions...</div>
    <template v-else>
      <h3 class="card-title">Current State</h3>
      <div class="revision-grid">
        <div v-for="slot in slots(section.data)" :key="slot.label" class="revision-slot">
          <span class="slot-label">{{ slot.label }}</span>
          <button
            v-if="slot.revision && slot.deployId"
            class="slot-link"
            @click="emit('openDeploy', slot.deployId)"
          >
            <ReleaseVersionPill :version="slot.revision" />
          </button>
          <span v-else class="slot-empty">&mdash;</span>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.card {
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: 16px;
}
.card-error { color: var(--color-incident-crimson); font-size: 0.85rem; }
.card-skeleton { color: var(--color-on-surface-variant); font-size: 0.85rem; }
.card-title {
  margin: 0 0 12px;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.revision-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.revision-slot {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.slot-label {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.slot-link {
  all: unset;
  cursor: pointer;
  display: inline-flex;
}
.slot-link:hover {
  opacity: 0.8;
}
.slot-empty {
  font-family: var(--font-tech);
  font-size: 0.85rem;
  color: var(--color-on-surface-variant);
}
</style>
