<script setup lang="ts">
import { computed } from 'vue';
import type { SectionResult } from '@/shared/types/section';
import type { ReleaseDeployRow } from '../../types/release';
import DeployStateBadge from '../primitives/DeployStateBadge.vue';
import DurationPill from '../primitives/DurationPill.vue';

const props = defineProps<{
  section: SectionResult<ReadonlyArray<ReleaseDeployRow>>;
}>();
const emit = defineEmits<{ openDeploy: [deployId: string] }>();

interface EnvGroup {
  readonly environmentName: string;
  readonly deploys: ReadonlyArray<ReleaseDeployRow>;
}

const grouped = computed<ReadonlyArray<EnvGroup>>(() => {
  const rows = props.section.data;
  if (!rows) return [];

  const map = new Map<string, ReleaseDeployRow[]>();
  for (const row of rows) {
    const existing = map.get(row.environmentName);
    if (existing) {
      map.set(row.environmentName, [...existing, row]);
    } else {
      map.set(row.environmentName, [row]);
    }
  }

  return Array.from(map.entries()).map(([environmentName, deploys]) => ({
    environmentName,
    deploys,
  }));
});
</script>

<template>
  <div class="deploys-card card">
    <div class="card-title">Deploys</div>

    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading deploys...</div>
    <div v-else-if="section.data.length === 0" class="card-empty">No deploys recorded for this release.</div>
    <template v-else>
      <div v-for="group in grouped" :key="group.environmentName" class="env-group">
        <div class="env-header">{{ group.environmentName }}</div>
        <div class="deploy-rows">
          <button
            v-for="deploy in group.deploys"
            :key="deploy.deployId"
            class="deploy-row"
            @click="emit('openDeploy', deploy.deployId)"
          >
            <DeployStateBadge :state="deploy.state" />
            <DurationPill :seconds="deploy.durationSec" :started-at="deploy.startedAt" />
            <span v-if="deploy.approverDisplayName" class="approver">
              {{ deploy.approverDisplayName }}
            </span>
            <span v-if="deploy.isCurrentRevision" class="current-badge">current</span>
            <span v-if="deploy.isRollback" class="rollback-badge">rollback</span>
          </button>
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

.env-group { margin-bottom: 12px; }
.env-group:last-child { margin-bottom: 0; }
.env-header {
  font-family: var(--font-tech);
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 6px;
}

.deploy-rows { display: flex; flex-direction: column; gap: 4px; }
.deploy-row {
  all: unset;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px;
  border-radius: var(--radius-sm);
  border: var(--border-ghost);
  background: var(--color-surface-container-high);
  transition: box-shadow 0.15s;
}
.deploy-row:hover { box-shadow: var(--shadow-card-hover); }

.approver {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
  margin-left: auto;
}
.current-badge,
.rollback-badge {
  font-family: var(--font-tech);
  font-size: 0.6rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 1px 6px;
  border-radius: var(--radius-sm);
}
.current-badge {
  color: var(--dp-state-succeeded);
  border: 1px solid var(--dp-state-succeeded);
}
.rollback-badge {
  color: var(--dp-state-rolled-back);
  border: 1px solid var(--dp-state-rolled-back);
}
</style>
