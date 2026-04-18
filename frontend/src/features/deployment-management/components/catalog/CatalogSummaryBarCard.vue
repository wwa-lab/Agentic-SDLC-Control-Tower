<script setup lang="ts">
import type { CatalogSummary } from '../../types/catalog';
import type { SectionResult } from '@/shared/types/section';
import HealthLed from '../primitives/HealthLed.vue';

defineProps<{ section: SectionResult<CatalogSummary> }>();

function pct(v: number): string { return `${(v * 100).toFixed(1)}%`; }
</script>

<template>
  <div class="summary-bar card">
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading summary...</div>
    <template v-else>
      <div class="stat">
        <span class="value">{{ section.data.visibleApplications }}</span>
        <span class="label">Applications</span>
      </div>
      <div class="stat">
        <span class="value">{{ section.data.deploysLast7d }}</span>
        <span class="label">Deploys (7d)</span>
      </div>
      <div class="stat">
        <span class="value">{{ pct(section.data.deploySuccessRate7d) }}</span>
        <span class="label">Success Rate</span>
      </div>
      <div class="stat">
        <span class="value">{{ section.data.medianDeployFrequency.toFixed(1) }}/d</span>
        <span class="label">Deploy Freq</span>
      </div>
      <div class="stat">
        <span class="value">{{ pct(section.data.changeFailureRate30d) }}</span>
        <span class="label">CFR (30d)</span>
      </div>
      <div class="led-bar">
        <span v-for="(count, led) in section.data.byLed" :key="led" class="led-item">
          <HealthLed :led="led" />
          <span class="led-count">{{ count }}</span>
        </span>
      </div>
    </template>
  </div>
</template>

<style scoped>
.summary-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 16px 20px;
  flex-wrap: wrap;
}
.stat { display: flex; flex-direction: column; align-items: center; gap: 2px; }
.value { font-family: var(--font-tech); font-size: 1.2rem; font-weight: 700; color: var(--color-on-surface); }
.label { font-family: var(--font-ui); font-size: 0.65rem; color: var(--color-on-surface-variant); text-transform: uppercase; letter-spacing: 0.04em; }
.led-bar { display: flex; gap: 12px; margin-left: auto; }
.led-item { display: flex; align-items: center; gap: 4px; }
.led-count { font-family: var(--font-tech); font-size: 0.85rem; color: var(--color-on-surface); }
.card { background: var(--color-surface-container-low); border: var(--border-ghost); border-radius: var(--radius-md); box-shadow: var(--shadow-card); }
.card-error { color: var(--color-incident-crimson); padding: 12px; font-size: 0.85rem; }
.card-skeleton { color: var(--color-on-surface-variant); padding: 12px; font-size: 0.85rem; }
</style>
