<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { EnvironmentMetrics } from '../../types/environment';

defineProps<{ section: SectionResult<EnvironmentMetrics> }>();

function formatMttr(seconds: number | null): string {
  if (seconds == null) return '\u2014';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return m > 0 ? `${m}m ${s}s` : `${s}s`;
}

function pct(v: number): string {
  return `${(v * 100).toFixed(1)}%`;
}
</script>

<template>
  <div class="stability-card card">
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading stability metrics...</div>
    <template v-else>
      <h3 class="card-title">Stability (30d)</h3>
      <div class="metrics-grid">
        <div class="metric">
          <span class="metric-value">{{ pct(section.data.changeFailureRate30d) }}</span>
          <span class="metric-label">CFR</span>
        </div>
        <div class="metric">
          <span class="metric-value">{{ formatMttr(section.data.mttrSec30d) }}</span>
          <span class="metric-label">MTTR</span>
        </div>
        <div class="metric">
          <span class="metric-value">{{ section.data.deployCount30d }}</span>
          <span class="metric-label">Deploys</span>
        </div>
        <div class="metric">
          <span class="metric-value">{{ section.data.rollbackCount30d }}</span>
          <span class="metric-label">Rollbacks</span>
        </div>
        <div class="metric">
          <span class="metric-value">{{ section.data.deploymentFrequency30d.toFixed(1) }}/d</span>
          <span class="metric-label">Deploy Freq</span>
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
.card-error { color: var(--color-incident-crimson); font-size: 0.85rem; }
.card-skeleton { color: var(--color-on-surface-variant); font-size: 0.85rem; }
.card-title {
  margin: 0 0 12px;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.metrics-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}
.metric {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.metric-value {
  font-family: var(--font-tech);
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--color-on-surface);
}
.metric-label {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
</style>
