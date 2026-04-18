<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { DeployHeader } from '../../types/deploy';
import type { EnvironmentKind } from '../../types/enums';
import EnvironmentChip from '../primitives/EnvironmentChip.vue';
import ReleaseVersionPill from '../primitives/ReleaseVersionPill.vue';
import DeployStateBadge from '../primitives/DeployStateBadge.vue';
import DeployTriggerChip from '../primitives/DeployTriggerChip.vue';
import JenkinsLinkOut from '../primitives/JenkinsLinkOut.vue';
import DurationPill from '../primitives/DurationPill.vue';

defineProps<{ section: SectionResult<DeployHeader> }>();
const emit = defineEmits<{ openRelease: [releaseId: string] }>();

function inferEnvironmentKind(envName: string): EnvironmentKind {
  const lower = envName.toLowerCase();
  if (lower.includes('prod')) return 'PROD';
  if (lower.includes('staging')) return 'STAGING';
  if (lower.includes('test') || lower.includes('qa')) return 'TEST';
  if (lower.includes('dev')) return 'DEV';
  return 'CUSTOM';
}
</script>

<template>
  <div class="deploy-header-card card">
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading deploy header...</div>
    <template v-else>
      <div class="top-row">
        <div class="identity">
          <span class="deploy-id">{{ section.data.deployId }}</span>
          <span class="app-name">{{ section.data.applicationId }}</span>
        </div>
        <div class="badges">
          <DeployStateBadge :state="section.data.state" />
          <EnvironmentChip
            :name="section.data.environmentName"
            :kind="inferEnvironmentKind(section.data.environmentName)"
          />
        </div>
      </div>

      <div class="meta-row">
        <div class="meta-item">
          <span class="meta-label">Release</span>
          <button class="release-link" @click="emit('openRelease', section.data.releaseId)">
            <ReleaseVersionPill :version="section.data.releaseVersion" />
          </button>
        </div>
        <div class="meta-item">
          <span class="meta-label">Trigger</span>
          <DeployTriggerChip :trigger="section.data.trigger" :actor="section.data.actor" />
        </div>
        <div class="meta-item">
          <span class="meta-label">Duration</span>
          <DurationPill :seconds="section.data.durationSec" :started-at="section.data.startedAt" />
        </div>
        <div class="meta-item">
          <span class="meta-label">Build</span>
          <JenkinsLinkOut
            :url="section.data.jenkinsBuildUrl"
            :label="`#${section.data.jenkinsBuildNumber}`"
          />
        </div>
      </div>

      <div v-if="section.data.isRollback || section.data.unresolvedFlag" class="flags-row">
        <span v-if="section.data.isRollback" class="flag flag--rollback">Rollback</span>
        <span v-if="section.data.unresolvedFlag" class="flag flag--warning">Unresolved issues detected</span>
      </div>
    </template>
  </div>
</template>

<style scoped>
.deploy-header-card { padding: 16px; }
.card {
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}
.card-error { color: var(--color-incident-crimson); font-size: 0.85rem; }
.card-skeleton { color: var(--color-on-surface-variant); font-size: 0.85rem; }

.top-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}
.identity { display: flex; flex-direction: column; gap: 4px; }
.deploy-id {
  font-family: var(--font-tech);
  font-size: 0.75rem;
  color: var(--color-secondary);
  letter-spacing: 0.04em;
}
.app-name {
  font-family: var(--font-ui);
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-on-surface);
}
.badges { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }

.meta-row {
  display: flex;
  gap: 24px;
  margin-top: 12px;
  padding: 12px;
  background: var(--color-surface-container-low);
  border-radius: var(--radius-sm);
  flex-wrap: wrap;
}
.meta-item { display: flex; flex-direction: column; gap: 2px; }
.meta-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}

.release-link {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
}
.release-link:hover { opacity: 0.8; }

.flags-row { display: flex; gap: 8px; margin-top: 10px; flex-wrap: wrap; }
.flag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.flag--rollback {
  border: 1px solid var(--dp-trigger-rollback);
  color: var(--dp-trigger-rollback);
}
.flag--warning {
  border: 1px solid var(--color-approval-amber);
  color: var(--color-approval-amber);
}
</style>
