<script setup lang="ts">
import type { DeliveryMetrics, SectionResult } from '../types/dashboard';
import DashboardCard from './DashboardCard.vue';
import MetricCard from './MetricCard.vue';

interface Props {
  section: SectionResult<DeliveryMetrics>;
  isLoading?: boolean;
}

defineProps<Props>();
</script>

<template>
  <DashboardCard title="Delivery Rhythm" :is-loading="isLoading" :error="section.error">
    <div v-if="section.data" class="delivery-content">
      <div class="metrics-grid">
        <MetricCard v-bind="section.data.leadTime" />
        <MetricCard v-bind="section.data.deployFrequency" />
        <MetricCard v-bind="section.data.iterationCompletion" />
      </div>
      
      <div v-if="section.data.bottleneckStage" class="bottleneck-info">
        <span class="bottleneck-label">Current Bottleneck:</span>
        <span class="bottleneck-value">{{ section.data.bottleneckStage }}</span>
      </div>
    </div>
  </DashboardCard>
</template>

<style scoped>
.delivery-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: space-between;
  gap: 16px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.bottleneck-info {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--color-incident-tint);
  padding: 8px 12px;
  border-radius: var(--radius-sm);
}

.bottleneck-label {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  color: var(--color-on-surface-variant);
  letter-spacing: 0.05em;
}

.bottleneck-value {
  font-family: var(--font-tech);
  font-size: 0.75rem;
  color: var(--color-incident-crimson);
  text-transform: uppercase;
  font-weight: 700;
}
</style>
