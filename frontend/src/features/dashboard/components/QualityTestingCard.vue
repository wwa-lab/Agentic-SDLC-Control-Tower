<script setup lang="ts">
import type { QualityMetrics, SectionResult } from '../types/dashboard';
import DashboardCard from './DashboardCard.vue';
import MetricCard from './MetricCard.vue';

interface Props {
  section: SectionResult<QualityMetrics>;
  isLoading?: boolean;
}

defineProps<Props>();
</script>

<template>
  <DashboardCard title="Quality & Testing" :is-loading="isLoading" :error="section.error">
    <div v-if="section.data" class="quality-content">
      <div class="metrics-grid">
        <MetricCard v-bind="section.data.buildSuccessRate" />
        <MetricCard v-bind="section.data.testPassRate" />
        <MetricCard v-bind="section.data.defectDensity" />
        <MetricCard v-bind="section.data.specCoverage" />
      </div>
    </div>
  </DashboardCard>
</template>

<style scoped>
.quality-content {
  display: flex;
  flex-direction: column;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
</style>
