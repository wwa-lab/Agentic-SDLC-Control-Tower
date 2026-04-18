<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { DeployStageRow } from '../../types/deploy';
import StageConclusionChip from '../primitives/StageConclusionChip.vue';
import DurationPill from '../primitives/DurationPill.vue';

defineProps<{ section: SectionResult<ReadonlyArray<DeployStageRow>> }>();
</script>

<template>
  <div class="deploy-stages-card card">
    <div class="card-title">Pipeline Stages</div>
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading stages...</div>
    <template v-else>
      <div v-if="section.data.length === 0" class="card-empty">No stages recorded.</div>
      <ol v-else class="timeline">
        <li
          v-for="stage in section.data"
          :key="stage.stageId"
          class="stage-row"
          :class="{ 'stage-row--active': stage.state === 'IN_PROGRESS' }"
        >
          <span class="stage-indicator">
            <span class="dot" :class="{ animating: stage.state === 'IN_PROGRESS' }" />
            <span class="connector" />
          </span>
          <div class="stage-body">
            <div class="stage-header">
              <span class="stage-name">{{ stage.name }}</span>
              <StageConclusionChip :state="stage.state" />
            </div>
            <DurationPill :seconds="stage.durationSec" :started-at="stage.startedAt" />
          </div>
        </li>
      </ol>
    </template>
  </div>
</template>

<style scoped>
.deploy-stages-card { padding: 16px; }
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
.card-skeleton { color: var(--color-on-surface-variant); font-size: 0.85rem; }
.card-empty { color: var(--color-on-surface-variant); font-size: 0.85rem; }

.timeline {
  list-style: none;
  margin: 0;
  padding: 0;
}

.stage-row {
  display: flex;
  gap: 12px;
  position: relative;
}

.stage-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 16px;
  padding-top: 4px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-on-surface-variant);
  flex-shrink: 0;
}
.stage-row--active .dot { background: var(--dp-state-in-progress); }
.animating {
  animation: pulse-dot 1.4s ease-in-out infinite;
}
@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(1.3); }
}

.connector {
  flex: 1;
  width: 1px;
  background: var(--color-outline-variant);
}
.stage-row:last-child .connector { display: none; }

.stage-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-bottom: 14px;
}
.stage-row:last-child .stage-body { padding-bottom: 0; }

.stage-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.stage-name {
  font-family: var(--font-ui);
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-on-surface);
}
</style>
