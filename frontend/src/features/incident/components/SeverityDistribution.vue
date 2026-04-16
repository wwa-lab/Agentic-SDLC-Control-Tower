<script setup lang="ts">
import type { SeverityDistribution as SeverityDistributionType } from '../types/incident';

interface Props {
  distribution: SeverityDistributionType;
}

defineProps<Props>();
</script>

<template>
  <div class="severity-strip">
    <div class="sev-item sev--p1">
      <span class="sev-count">{{ distribution.p1 }}</span>
      <span class="sev-label">P1</span>
    </div>
    <div class="sev-item sev--p2">
      <span class="sev-count">{{ distribution.p2 }}</span>
      <span class="sev-label">P2</span>
    </div>
    <div class="sev-item sev--p3">
      <span class="sev-count">{{ distribution.p3 }}</span>
      <span class="sev-label">P3</span>
    </div>
    <div class="sev-item sev--p4">
      <span class="sev-count">{{ distribution.p4 }}</span>
      <span class="sev-label">P4</span>
    </div>
  </div>
</template>

<style scoped>
.severity-strip {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
}

.sev-item { display: flex; align-items: baseline; gap: 4px; }

.sev-count {
  font-family: var(--font-tech);
  font-size: 1.125rem;
  font-weight: 700;
  line-height: 1;
}

.sev-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-on-surface-variant);
}

.sev--p1 .sev-count { color: var(--color-incident-crimson); }
.sev--p2 .sev-count { color: var(--color-incident-crimson); opacity: 0.7; }
.sev--p3 .sev-count { color: var(--color-approval-amber); }
.sev--p4 .sev-count { color: var(--color-on-surface-variant); }
</style>
