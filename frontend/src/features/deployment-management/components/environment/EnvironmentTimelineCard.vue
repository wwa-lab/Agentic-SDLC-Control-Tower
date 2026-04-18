<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { EnvironmentTimelineEntry } from '../../types/environment';
import ReleaseVersionPill from '../primitives/ReleaseVersionPill.vue';
import DeployStateBadge from '../primitives/DeployStateBadge.vue';
import DurationPill from '../primitives/DurationPill.vue';

defineProps<{ section: SectionResult<ReadonlyArray<EnvironmentTimelineEntry>> }>();
const emit = defineEmits<{ openDeploy: [deployId: string] }>();
</script>

<template>
  <div class="timeline-card card">
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading timeline...</div>
    <div v-else-if="section.data.length === 0" class="card-empty">No deploys recorded.</div>
    <template v-else>
      <h3 class="card-title">Deploy Timeline</h3>
      <ul class="timeline-list">
        <li
          v-for="entry in section.data"
          :key="entry.deployId"
          class="timeline-entry"
        >
          <button class="entry-button" @click="emit('openDeploy', entry.deployId)">
            <div class="entry-top">
              <ReleaseVersionPill :version="entry.releaseVersion" />
              <DeployStateBadge :state="entry.state" />
              <span v-if="entry.isRollback" class="rollback-flag">Rollback</span>
            </div>
            <div class="entry-bottom">
              <DurationPill :seconds="entry.durationSec" :started-at="entry.startedAt" />
              <span class="entry-time">{{ new Date(entry.startedAt).toLocaleString() }}</span>
            </div>
          </button>
        </li>
      </ul>
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
.timeline-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.timeline-entry {
  border-bottom: 1px solid var(--border-separator);
}
.timeline-entry:last-child {
  border-bottom: none;
}
.entry-button {
  all: unset;
  cursor: pointer;
  width: 100%;
  padding: 8px 4px;
  border-radius: var(--radius-sm);
  transition: background 0.15s;
}
.entry-button:hover {
  background: var(--nav-hover-bg);
}
.entry-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.rollback-flag {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--dp-state-rolled-back);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 1px 6px;
  border: 1px solid var(--dp-state-rolled-back);
  border-radius: var(--radius-sm);
}
.entry-bottom {
  display: flex;
  align-items: center;
  gap: 12px;
}
.entry-time {
  font-family: var(--font-tech);
  font-size: 0.7rem;
  color: var(--color-on-surface-variant);
}
</style>
