<script setup lang="ts">
import type { IncidentHeader, SectionResult } from '../types/incident';
import IncidentCard from './IncidentCard.vue';
import SeverityBadge from './SeverityBadge.vue';
import StatusBadge from './StatusBadge.vue';
import HandlerTypeBadge from './HandlerTypeBadge.vue';

interface Props {
  header: SectionResult<IncidentHeader>;
  isLoading?: boolean;
}

defineProps<Props>();

function formatDuration(iso: string): string {
  const match = iso.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
  if (!match) return iso;
  const h = match[1] || '0';
  const m = match[2] || '0';
  return `${h}h ${m}m`;
}

function formatTime(iso: string | null): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false });
}

const AUTONOMY_LABELS: Record<string, string> = {
  Level1_Manual: 'Level 1: Manual',
  Level2_SuggestApprove: 'Level 2: Suggest + Approve',
  Level3_AutoAudit: 'Level 3: Auto + Audit',
};
</script>

<template>
  <IncidentCard title="Incident Header" :is-loading="isLoading" :error="header.error" full-width>
    <div v-if="header.data" class="header-content">
      <div class="header-top">
        <div class="header-identity">
          <span class="incident-id">{{ header.data.id }}</span>
          <h2 class="incident-title">{{ header.data.title }}</h2>
        </div>
        <div class="header-badges">
          <SeverityBadge :priority="header.data.priority" />
          <StatusBadge :status="header.data.status" />
          <HandlerTypeBadge :handler-type="header.data.handlerType" />
        </div>
      </div>
      <div class="header-meta">
        <div class="meta-item">
          <span class="meta-label">Control Mode</span>
          <span class="meta-value">{{ header.data.controlMode }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">Autonomy</span>
          <span class="meta-value">{{ AUTONOMY_LABELS[header.data.autonomyLevel] ?? header.data.autonomyLevel }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">Detected</span>
          <span class="meta-value meta-value--tech">{{ formatTime(header.data.detectedAt) }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">Acknowledged</span>
          <span class="meta-value meta-value--tech">{{ formatTime(header.data.acknowledgedAt) }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">{{ header.data.resolvedAt ? 'Resolved' : 'Duration' }}</span>
          <span class="meta-value meta-value--tech">{{ header.data.resolvedAt ? formatTime(header.data.resolvedAt) : formatDuration(header.data.duration) }}</span>
        </div>
      </div>
    </div>
  </IncidentCard>
</template>

<style scoped>
.header-content { display: flex; flex-direction: column; gap: 16px; }

.header-top { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; flex-wrap: wrap; }

.header-identity { display: flex; flex-direction: column; gap: 4px; }

.incident-id {
  font-family: var(--font-tech);
  font-size: 0.75rem;
  color: var(--color-secondary);
  letter-spacing: 0.04em;
}

.incident-title {
  font-family: var(--font-ui);
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-on-surface);
  margin: 0;
}

.header-badges { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }

.header-meta {
  display: flex;
  gap: 24px;
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

.meta-value {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface);
}

.meta-value--tech { font-family: var(--font-tech); }
</style>
