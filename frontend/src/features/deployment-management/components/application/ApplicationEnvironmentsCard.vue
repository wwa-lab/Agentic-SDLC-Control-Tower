<script setup lang="ts">
import type { EnvironmentRow } from '../../types/application';
import type { SectionResult } from '@/shared/types/section';
import EnvironmentChip from '../primitives/EnvironmentChip.vue';
import ReleaseVersionPill from '../primitives/ReleaseVersionPill.vue';
import DeployStateBadge from '../primitives/DeployStateBadge.vue';

defineProps<{ section: SectionResult<ReadonlyArray<EnvironmentRow>> }>();
const emit = defineEmits<{ openEnvironment: [applicationId: string, envName: string] }>();

function formatDate(iso: string | null): string {
  if (!iso) return '\u2014';
  return new Date(iso).toLocaleString();
}
</script>

<template>
  <div class="envs-card card">
    <div class="card-title">Environments</div>
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading environments...</div>
    <div v-else-if="section.data.length === 0" class="card-empty">No environments configured.</div>
    <div v-else class="env-table">
      <div class="env-row env-row-header">
        <span class="col-env">Environment</span>
        <span class="col-rev">Revision</span>
        <span class="col-state">State</span>
        <span class="col-time">Deployed</span>
        <span class="col-rb">Rollback</span>
      </div>
      <button
        v-for="row in section.data"
        :key="row.environmentName"
        class="env-row env-row-data"
        @click="emit('openEnvironment', row.environmentName, row.environmentName)"
      >
        <span class="col-env">
          <EnvironmentChip :name="row.environmentName" :kind="row.kind" />
        </span>
        <span class="col-rev">
          <ReleaseVersionPill v-if="row.currentRevision" :version="row.currentRevision" />
          <span v-else class="no-data">&mdash;</span>
        </span>
        <span class="col-state">
          <DeployStateBadge v-if="row.currentDeployState" :state="row.currentDeployState" />
          <span v-else class="no-data">&mdash;</span>
        </span>
        <span class="col-time">{{ formatDate(row.currentDeployedAt) }}</span>
        <span class="col-rb">
          <span v-if="row.isRolledBack" class="rollback-flag" title="Rolled back">
            &#x21A9; {{ row.rolledBackToReleaseVersion ?? '' }}
          </span>
          <span v-else class="no-data">&mdash;</span>
        </span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.envs-card { padding: 16px; }
.card {
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}
.card-title {
  margin-bottom: 12px;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-on-surface);
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.card-error { color: var(--color-incident-crimson); font-size: 0.85rem; }
.card-skeleton, .card-empty { color: var(--color-on-surface-variant); font-size: 0.85rem; }
.env-table { display: flex; flex-direction: column; gap: 0; }
.env-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 0.8fr;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  border-bottom: 1px solid var(--color-surface-container-high);
}
.env-row-header {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.env-row-data {
  all: unset;
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 0.8fr;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  border-bottom: 1px solid var(--color-surface-container-high);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  color: var(--color-on-surface);
}
.env-row-data:hover { background: var(--color-surface-container-high); }
.env-row-data:last-child { border-bottom: none; }
.no-data { color: var(--color-on-surface-variant); font-size: 0.75rem; }
.rollback-flag {
  font-family: var(--font-tech);
  font-size: 0.75rem;
  color: var(--color-approval-amber);
  font-weight: 600;
}
</style>
