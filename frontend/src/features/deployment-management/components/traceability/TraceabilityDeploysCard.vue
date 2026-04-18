<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { TraceabilityDeployGroup } from '../../types/traceability';
import EnvironmentChip from '../primitives/EnvironmentChip.vue';
import ReleaseVersionPill from '../primitives/ReleaseVersionPill.vue';
import DeployStateBadge from '../primitives/DeployStateBadge.vue';

defineProps<{
  section: SectionResult<ReadonlyArray<TraceabilityDeployGroup>>;
}>();
const emit = defineEmits<{ openDeploy: [deployId: string] }>();

function formatTimestamp(iso: string): string {
  return new Date(iso).toLocaleString();
}
</script>

<template>
  <div class="deploys-card card">
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading deploys...</div>
    <div v-else-if="section.data.length === 0" class="card-empty">No deploys found for this story.</div>
    <template v-else>
      <h3 class="card-title">Deploys by Environment</h3>
      <div v-for="group in section.data" :key="group.environmentName" class="env-group">
        <div class="group-header">
          <EnvironmentChip :name="group.environmentName" :kind="group.kind" />
        </div>
        <ul class="deploy-list">
          <li
            v-for="deploy in group.deploys"
            :key="deploy.deployId"
            class="deploy-row"
          >
            <button class="row-button" @click="emit('openDeploy', deploy.deployId)">
              <ReleaseVersionPill :version="deploy.releaseVersion" />
              <DeployStateBadge :state="deploy.state" />
              <span v-if="deploy.isCurrentRevision" class="current-tag">Current</span>
              <span v-if="deploy.isRollback" class="rollback-tag">Rollback</span>
              <span class="deploy-time">{{ formatTimestamp(deploy.startedAt) }}</span>
            </button>
          </li>
        </ul>
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
.card-skeleton, .card-empty { color: var(--color-on-surface-variant); font-size: 0.85rem; }
.card-title {
  margin: 0 0 12px;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.env-group {
  margin-bottom: 14px;
}
.env-group:last-child {
  margin-bottom: 0;
}
.group-header {
  padding-bottom: 6px;
  border-bottom: 1px solid var(--border-separator);
  margin-bottom: 6px;
}
.deploy-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.deploy-row {
  border-bottom: 1px solid var(--border-separator);
}
.deploy-row:last-child {
  border-bottom: none;
}
.row-button {
  all: unset;
  cursor: pointer;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 4px;
  border-radius: var(--radius-sm);
  transition: background 0.15s;
}
.row-button:hover {
  background: var(--nav-hover-bg);
}
.current-tag {
  font-family: var(--font-ui);
  font-size: 0.6rem;
  font-weight: 700;
  color: var(--dp-state-succeeded);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 1px 5px;
  border: 1px solid var(--dp-state-succeeded);
  border-radius: var(--radius-sm);
}
.rollback-tag {
  font-family: var(--font-ui);
  font-size: 0.6rem;
  font-weight: 700;
  color: var(--dp-state-rolled-back);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 1px 5px;
  border: 1px solid var(--dp-state-rolled-back);
  border-radius: var(--radius-sm);
}
.deploy-time {
  font-family: var(--font-tech);
  font-size: 0.7rem;
  color: var(--color-on-surface-variant);
  margin-left: auto;
  white-space: nowrap;
}
</style>
