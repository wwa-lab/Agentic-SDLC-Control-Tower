<script setup lang="ts">
import type { Milestone } from '../types/milestones';

interface Props {
  milestone: Milestone;
}

defineProps<Props>();

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
  }).format(new Date(value));
}
</script>

<template>
  <div
    class="milestone-row"
    :class="{
      'milestone-row--current': milestone.isCurrent,
      'milestone-row--risk': milestone.status === 'AT_RISK' || milestone.status === 'SLIPPED',
    }"
  >
    <div class="milestone-row__title">
      <div>
        <p class="text-label">{{ formatDate(milestone.targetDate) }}</p>
        <strong>{{ milestone.label }}</strong>
      </div>
      <span class="status-chip" :class="`status-chip--${milestone.status.toLowerCase()}`">
        {{ milestone.status }}
      </span>
    </div>

    <div class="milestone-row__progress">
      <div class="progress-track">
        <span class="progress-fill" :style="{ width: `${milestone.percentComplete ?? 0}%` }"></span>
      </div>
      <span class="text-tech">{{ milestone.percentComplete ?? '--' }}%</span>
    </div>

    <div class="milestone-row__meta text-body-sm">
      <span>{{ milestone.owner?.displayName ?? 'Owner TBD' }}</span>
      <span v-if="milestone.slippageReason">{{ milestone.slippageReason }}</span>
    </div>
  </div>
</template>

<style scoped>
.milestone-row {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(255, 255, 255, 0.03);
}

.milestone-row--current {
  box-shadow: inset 0 0 0 1px rgba(137, 206, 255, 0.34);
}

.milestone-row--risk {
  border-color: rgba(255, 180, 171, 0.24);
  background: rgba(255, 180, 171, 0.06);
}

.milestone-row__title,
.milestone-row__progress,
.milestone-row__meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.milestone-row__progress {
  align-items: center;
}

.progress-track {
  flex: 1;
  height: 8px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.06);
}

.progress-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--color-secondary), rgba(78, 222, 163, 0.8));
}

.status-chip {
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 0.625rem;
  font-family: var(--font-tech);
}

.status-chip--completed { background: rgba(78, 222, 163, 0.12); color: var(--color-health-emerald); }
.status-chip--in_progress { background: rgba(137, 206, 255, 0.12); color: var(--color-secondary); }
.status-chip--not_started { background: rgba(148, 163, 184, 0.12); color: var(--color-on-surface-variant); }
.status-chip--at_risk,
.status-chip--slipped { background: rgba(255, 180, 171, 0.12); color: var(--color-incident-crimson); }

@media (max-width: 1200px) {
  .milestone-row__title,
  .milestone-row__progress,
  .milestone-row__meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .progress-track {
    width: 100%;
  }
}
</style>
