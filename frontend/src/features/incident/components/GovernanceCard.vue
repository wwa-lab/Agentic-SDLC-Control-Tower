<script setup lang="ts">
import type { Governance, SectionResult } from '../types/incident';
import IncidentCard from './IncidentCard.vue';

interface Props {
  governance: SectionResult<Governance>;
  isLoading?: boolean;
}

defineProps<Props>();

function formatTime(iso: string): string {
  return new Date(iso).toLocaleString('en-US', {
    month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false,
  });
}

const ACTION_CLASS: Record<string, string> = {
  approve: 'gov--approve',
  reject: 'gov--reject',
  escalate: 'gov--escalate',
  override: 'gov--override',
};
</script>

<template>
  <IncidentCard title="Human Governance" :is-loading="isLoading" :error="governance.error">
    <div v-if="governance.data" class="governance-content">
      <div v-if="governance.data.entries.length === 0" class="empty-state">
        No governance actions yet
      </div>
      <div v-for="(entry, i) in governance.data.entries" :key="i" class="gov-entry" :class="ACTION_CLASS[entry.actionTaken]">
        <div class="entry-header">
          <span class="gov-actor">{{ entry.actor }}</span>
          <span class="gov-time">{{ formatTime(entry.timestamp) }}</span>
        </div>
        <div class="entry-action">
          <span class="gov-action-label">{{ entry.actionTaken.toUpperCase() }}</span>
          <span v-if="entry.reason" class="gov-reason">{{ entry.reason }}</span>
        </div>
        <div v-if="entry.policyRef" class="gov-policy">{{ entry.policyRef }}</div>
      </div>
    </div>
  </IncidentCard>
</template>

<style scoped>
.governance-content { display: flex; flex-direction: column; gap: 8px; }

.empty-state {
  font-family: var(--font-ui); font-size: 0.75rem;
  color: var(--color-on-surface-variant); text-align: center; padding: 16px;
}

.gov-entry {
  padding: 10px 12px;
  background: var(--color-surface-container-low);
  border-radius: var(--radius-sm);
  border-left: 3px solid transparent;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.gov--approve { border-left-color: var(--color-health-emerald); }
.gov--reject { border-left-color: var(--color-incident-crimson); }
.gov--escalate { border-left-color: var(--color-approval-amber); }
.gov--override { border-left-color: var(--color-secondary); }

.entry-header { display: flex; justify-content: space-between; align-items: center; }

.gov-actor {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-on-surface);
}

.gov-time {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
}

.entry-action { display: flex; align-items: baseline; gap: 8px; }

.gov-action-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-on-surface-variant);
}

.gov-reason {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
  font-style: italic;
}

.gov-policy {
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  color: var(--color-on-surface-variant);
  opacity: 0.6;
}
</style>
