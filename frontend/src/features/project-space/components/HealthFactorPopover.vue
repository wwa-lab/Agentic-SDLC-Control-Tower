<script setup lang="ts">
import type { HealthFactor } from '../types/summary';

interface Props {
  factors: ReadonlyArray<HealthFactor>;
}

defineProps<Props>();
</script>

<template>
  <div class="health-popover section-high" role="tooltip">
    <p class="text-label">Health Factors</p>
    <ul class="health-popover__list">
      <li v-for="factor in factors" :key="`${factor.label}-${factor.severity}`" class="health-popover__item">
        <span class="severity-chip" :class="`severity-chip--${factor.severity.toLowerCase()}`">
          {{ factor.severity }}
        </span>
        <span class="text-body-sm">{{ factor.label }}</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.health-popover {
  min-width: 240px;
  border-radius: var(--radius-sm);
  border: var(--border-ghost);
  padding: 12px;
  box-shadow: var(--shadow-card);
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: rgba(19, 27, 46, 0.96);
}

.health-popover__list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.health-popover__item {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 8px;
  align-items: center;
}

.severity-chip {
  border-radius: 999px;
  padding: 3px 8px;
  font-size: 0.625rem;
  letter-spacing: 0.08em;
  font-family: var(--font-tech);
}

.severity-chip--info {
  background: var(--color-secondary-tint);
  color: var(--color-secondary);
}

.severity-chip--warn {
  background: rgba(245, 158, 11, 0.12);
  color: var(--color-approval-amber);
}

.severity-chip--crit {
  background: var(--color-incident-tint);
  color: var(--color-incident-crimson);
}
</style>
