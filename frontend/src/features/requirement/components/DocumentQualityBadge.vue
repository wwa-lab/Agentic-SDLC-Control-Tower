<script setup lang="ts">
import type { DocumentQualityGate } from '../types/requirement';

defineProps<{
  gate?: DocumentQualityGate | null;
}>();

function qualityLabel(gate: DocumentQualityGate) {
  if (gate.label) return gate.label;
  if (gate.stale) return 'Stale';
  if (gate.band === 'EXCELLENT') return 'Excellent';
  if (gate.band === 'GOOD') return 'Good';
  return 'Blocked';
}
</script>

<template>
  <span
    v-if="gate"
    class="quality-badge"
    :class="[`quality-badge--${gate.band.toLowerCase()}`, { 'quality-badge--stale': gate.stale }]"
    :title="gate.summary"
  >
    {{ gate.score }} {{ qualityLabel(gate) }}
  </span>
</template>

<style scoped>
.quality-badge {
  display: inline-flex;
  align-items: center;
  min-height: 20px;
  padding: 2px 7px;
  border-radius: var(--radius-sm);
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.quality-badge--excellent {
  background: rgba(78, 222, 163, 0.12);
  color: var(--color-health-emerald);
}

.quality-badge--good {
  background: rgba(137, 206, 255, 0.12);
  color: var(--color-secondary);
}

.quality-badge--blocked {
  background: rgba(239, 68, 68, 0.12);
  color: var(--color-incident-crimson);
}

.quality-badge--stale {
  background: rgba(245, 158, 11, 0.12);
  color: var(--color-approval-amber);
}
</style>
