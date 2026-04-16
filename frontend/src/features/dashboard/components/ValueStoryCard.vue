<script setup lang="ts">
import type { ValueStory, SectionResult } from '../types/dashboard';
import DashboardCard from './DashboardCard.vue';

interface Props {
  section: SectionResult<ValueStory>;
  isLoading?: boolean;
}

defineProps<Props>();
</script>

<template>
  <DashboardCard title="Value Story" :is-loading="isLoading" :error="section.error">
    <div v-if="section.data" class="value-story-content">
      <h3 class="story-headline">{{ section.data.headline }}</h3>
      
      <div class="proof-metrics">
        <div v-for="metric in section.data.metrics" :key="metric.label" class="proof-metric">
          <div class="proof-header">
            <span class="proof-value">{{ metric.value }}</span>
            <span class="proof-label">{{ metric.label }}</span>
          </div>
          <p class="proof-description">{{ metric.description }}</p>
        </div>
      </div>
    </div>
  </DashboardCard>
</template>

<style scoped>
.value-story-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.story-headline {
  font-family: var(--font-ui);
  font-size: 0.875rem;
  line-height: 1.5;
  color: var(--color-on-surface);
  margin: 0;
  font-weight: 500;
}

.proof-metrics {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.proof-metric {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.proof-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.proof-value {
  font-family: var(--font-tech);
  font-size: 1.125rem;
  color: var(--color-secondary);
}

.proof-label {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  color: var(--color-on-surface-variant);
  letter-spacing: 0.05em;
}

.proof-description {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
  margin: 0;
  line-height: 1.4;
}
</style>
