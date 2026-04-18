<script setup lang="ts">
import type { HeadlineMetric, SectionResult } from '../../types';
import { formatTrend } from '../../utils';
import SectionError from './SectionError.vue';
import SectionSkeleton from './SectionSkeleton.vue';

defineProps<{
  section: SectionResult<HeadlineMetric[]>;
  loading: boolean;
}>();

defineEmits<{
  retry: [];
}>();
</script>

<template>
  <SectionSkeleton v-if="loading" :height="132" />
  <SectionError v-else-if="section.error" :message="section.error" @retry="$emit('retry')" />
  <div v-else-if="section.data" class="headline-strip">
    <article v-for="metric in section.data" :key="metric.key" class="headline-tile">
      <p class="text-label">{{ metric.label }}</p>
      <strong>{{ metric.value }}</strong>
      <p class="headline-tile__trend" :class="{ 'headline-tile__trend--positive': metric.trendIsPositive, 'headline-tile__trend--negative': metric.trendIsPositive === false }">
        <span v-if="metric.trendIsPositive === true">▲</span>
        <span v-else-if="metric.trendIsPositive === false">▼</span>
        <span v-else>•</span>
        {{ formatTrend(metric.trend) }}
      </p>
    </article>
  </div>
</template>

<style scoped>
.headline-strip {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
}

.headline-tile {
  display: grid;
  gap: 8px;
  padding: 18px;
  border: var(--border-ghost);
  border-radius: 16px;
  background: linear-gradient(180deg, color-mix(in srgb, var(--surface-report) 88%, transparent), var(--color-surface-container-low));
}

.headline-tile strong {
  font-size: 1.5rem;
}

.headline-tile__trend {
  font-size: 0.875rem;
  color: var(--color-on-surface-variant);
}

.headline-tile__trend--positive {
  color: var(--color-tertiary);
}

.headline-tile__trend--negative {
  color: var(--color-error);
}
</style>
