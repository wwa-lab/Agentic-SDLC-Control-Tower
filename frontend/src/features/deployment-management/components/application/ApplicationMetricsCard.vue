<script setup lang="ts">
import type { TraceSummaryRow } from '../../types/application';
import type { SectionResult } from '@/shared/types/section';

defineProps<{ section: SectionResult<ReadonlyArray<TraceSummaryRow>> }>();
</script>

<template>
  <div class="metrics-card card">
    <div class="card-title">Metrics (30d)</div>
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading metrics...</div>
    <div v-else-if="section.data.length === 0" class="card-empty">No metrics available.</div>
    <div v-else class="metrics-table">
      <div class="metrics-row metrics-row-header">
        <span class="col-env">Environment</span>
        <span class="col-stories">Stories</span>
        <span class="col-deploys">Deploys</span>
      </div>
      <div
        v-for="row in section.data"
        :key="row.environmentName"
        class="metrics-row metrics-row-data"
      >
        <span class="col-env env-name">{{ row.environmentName }}</span>
        <span class="col-stories metric-value">{{ row.storiesLast30d }}</span>
        <span class="col-deploys metric-value">{{ row.deploysLast30d }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.metrics-card { padding: 16px; }
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
.metrics-table { display: flex; flex-direction: column; }
.metrics-row {
  display: grid;
  grid-template-columns: 1fr 0.6fr 0.6fr;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  border-bottom: 1px solid var(--color-surface-container-high);
}
.metrics-row:last-child { border-bottom: none; }
.metrics-row-header {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.env-name {
  font-family: var(--font-tech);
  font-size: 0.8rem;
  color: var(--color-on-surface);
  text-transform: uppercase;
}
.metric-value {
  font-family: var(--font-tech);
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--color-on-surface);
  text-align: right;
}
</style>
