<script setup lang="ts">
import type { StabilityMetrics, SectionResult } from '../types/dashboard';
import DashboardCard from './DashboardCard.vue';
import MetricCard from './MetricCard.vue';

interface Props {
  section: SectionResult<StabilityMetrics>;
  isLoading?: boolean;
}

defineProps<Props>();
const emit = defineEmits(['navigate-incidents']);
</script>

<template>
  <DashboardCard title="Stability & Incidents" :is-loading="isLoading" :error="section.error">
    <div v-if="section.data" class="stability-content">
      <div class="incidents-overview" @click="emit('navigate-incidents')">
        <div class="active-badge" :class="{ 'badge--critical': section.data.criticalIncidents > 0 }">
          <span class="badge-count">{{ section.data.activeIncidents }}</span>
          <span class="badge-label">Active Incidents</span>
        </div>
        <div v-if="section.data.criticalIncidents > 0" class="critical-flag">
          {{ section.data.criticalIncidents }} CRITICAL
        </div>
      </div>
      
      <div class="metrics-row">
        <MetricCard v-bind="section.data.changeFailureRate" size="sm" />
        <MetricCard v-bind="section.data.mttr" size="sm" />
        <MetricCard v-bind="section.data.rollbackRate" size="sm" />
      </div>
    </div>
  </DashboardCard>
</template>

<style scoped>
.stability-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.incidents-overview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--color-surface-container-low);
  padding: 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.2s ease;
}

.incidents-overview:hover {
  background: var(--color-surface-container-highest);
}

.active-badge {
  display: flex;
  flex-direction: column;
}

.badge-count {
  font-family: var(--font-tech);
  font-size: 1.5rem;
  color: var(--color-health-emerald);
  line-height: 1;
}

.badge--critical .badge-count {
  color: var(--color-incident-crimson);
}

.badge-label {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  color: var(--color-on-surface-variant);
  letter-spacing: 0.05em;
}

.critical-flag {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 700;
  color: #fff;
  background: var(--color-incident-crimson);
  padding: 2px 6px;
  border-radius: 2px;
  box-shadow: 0 0 8px var(--color-incident-tint);
}

.metrics-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
</style>
