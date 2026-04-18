<script setup lang="ts">
import { ArrowUpRight, ShieldAlert } from 'lucide-vue-next';
import type { RiskItem as ProjectRiskItem } from '../types/risks';

interface Props {
  risk: ProjectRiskItem;
}

defineProps<Props>();

defineEmits<{
  open: [url: string];
}>();
</script>

<template>
  <div class="risk-row" :class="{ 'risk-row--critical': risk.severity === 'CRITICAL' }">
    <div class="risk-row__header">
      <div>
        <p class="text-label">{{ risk.category }} / {{ risk.ageDays }}d old</p>
        <strong>{{ risk.title }}</strong>
      </div>
      <span class="severity-chip" :class="`severity-chip--${risk.severity.toLowerCase()}`">{{ risk.severity }}</span>
    </div>

    <p class="text-body-sm">{{ risk.latestNote ?? 'No mitigation note recorded yet.' }}</p>

    <div class="risk-row__footer">
      <span class="text-tech">{{ risk.owner?.displayName ?? 'Owner TBD' }}</span>
      <button class="risk-row__action" @click="$emit('open', risk.primaryAction.url)">
        <ShieldAlert :size="14" />
        <span>{{ risk.primaryAction.label }}</span>
        <ArrowUpRight :size="14" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.risk-row {
  padding: 12px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(255, 255, 255, 0.03);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.risk-row--critical {
  border-color: rgba(255, 180, 171, 0.28);
  background: rgba(255, 180, 171, 0.08);
}

.risk-row__header,
.risk-row__footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.severity-chip {
  padding: 4px 8px;
  border-radius: 999px;
  font-family: var(--font-tech);
  font-size: 0.625rem;
}

.severity-chip--critical { background: rgba(255, 180, 171, 0.12); color: var(--color-incident-crimson); }
.severity-chip--high { background: rgba(245, 158, 11, 0.12); color: var(--color-approval-amber); }
.severity-chip--medium { background: rgba(137, 206, 255, 0.12); color: var(--color-secondary); }
.severity-chip--low { background: rgba(148, 163, 184, 0.12); color: var(--color-on-surface-variant); }

.risk-row__action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  color: var(--color-secondary);
  cursor: pointer;
}

@media (max-width: 1200px) {
  .risk-row__header,
  .risk-row__footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
