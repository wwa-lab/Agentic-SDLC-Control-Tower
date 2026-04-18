<script setup lang="ts">
import type { RecentDeployRow } from '../../types/application';
import type { SectionResult } from '@/shared/types/section';
import DeployStateBadge from '../primitives/DeployStateBadge.vue';
import DurationPill from '../primitives/DurationPill.vue';
import ReleaseVersionPill from '../primitives/ReleaseVersionPill.vue';

defineProps<{ section: SectionResult<ReadonlyArray<RecentDeployRow>> }>();
const emit = defineEmits<{ openDeploy: [deployId: string] }>();
</script>

<template>
  <div class="deploys-card card">
    <div class="card-title">Recent Deploys</div>
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading recent deploys...</div>
    <div v-else-if="section.data.length === 0" class="card-empty">No deploys found.</div>
    <div v-else class="deploy-list">
      <div class="deploy-row deploy-row-header">
        <span class="col-id">Deploy</span>
        <span class="col-ver">Version</span>
        <span class="col-env">Env</span>
        <span class="col-state">State</span>
        <span class="col-dur">Duration</span>
        <span class="col-rb">Rollback</span>
      </div>
      <button
        v-for="row in section.data"
        :key="row.deployId"
        class="deploy-row deploy-row-data"
        @click="emit('openDeploy', row.deployId)"
      >
        <span class="col-id deploy-id">{{ row.deployId }}</span>
        <span class="col-ver">
          <ReleaseVersionPill :version="row.releaseVersion" />
        </span>
        <span class="col-env env-name">{{ row.environmentName }}</span>
        <span class="col-state">
          <DeployStateBadge :state="row.state" />
        </span>
        <span class="col-dur">
          <DurationPill :seconds="row.durationSec" :started-at="row.startedAt" />
        </span>
        <span class="col-rb">
          <span v-if="row.isRollback" class="rollback-flag">&#x21A9; Rollback</span>
          <span v-else class="no-data">&mdash;</span>
        </span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.deploys-card { padding: 16px; }
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
.deploy-list { display: flex; flex-direction: column; }
.deploy-row {
  display: grid;
  grid-template-columns: 1.2fr 0.9fr 0.7fr 1fr 0.7fr 0.7fr;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  border-bottom: 1px solid var(--color-surface-container-high);
}
.deploy-row-header {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.deploy-row-data {
  all: unset;
  display: grid;
  grid-template-columns: 1.2fr 0.9fr 0.7fr 1fr 0.7fr 0.7fr;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  border-bottom: 1px solid var(--color-surface-container-high);
  cursor: pointer;
}
.deploy-row-data:hover { background: var(--color-surface-container-high); }
.deploy-row-data:last-child { border-bottom: none; }
.deploy-id {
  font-family: var(--font-tech);
  font-size: 0.75rem;
  color: var(--color-on-surface);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.env-name {
  font-family: var(--font-tech);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
}
.no-data { color: var(--color-on-surface-variant); font-size: 0.75rem; }
.rollback-flag {
  font-family: var(--font-tech);
  font-size: 0.75rem;
  color: var(--color-approval-amber);
  font-weight: 600;
}
</style>
