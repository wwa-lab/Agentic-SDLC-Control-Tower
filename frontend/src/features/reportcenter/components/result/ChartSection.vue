<script setup lang="ts">
import { computed } from 'vue';
import type { ChartType, SectionResult, SeriesPoint } from '../../types';
import GroupedBarChart from '../charts/GroupedBarChart.vue';
import HeatmapChart from '../charts/HeatmapChart.vue';
import HistogramChart from '../charts/HistogramChart.vue';
import HorizontalBarChart from '../charts/HorizontalBarChart.vue';
import StackedBarChart from '../charts/StackedBarChart.vue';
import SectionError from './SectionError.vue';
import SectionSkeleton from './SectionSkeleton.vue';

const props = defineProps<{
  section: SectionResult<SeriesPoint[]>;
  chartType: ChartType;
  loading: boolean;
}>();

defineEmits<{
  retry: [];
}>();

const componentMap = {
  histogram: HistogramChart,
  'stacked-bar': StackedBarChart,
  'grouped-bar': GroupedBarChart,
  heatmap: HeatmapChart,
  'horizontal-bar': HorizontalBarChart,
};

const resolvedComponent = computed(() => componentMap[props.chartType]);
</script>

<template>
  <SectionSkeleton v-if="loading" :height="280" />
  <SectionError v-else-if="section.error" :message="section.error" @retry="$emit('retry')" />
  <section v-else-if="section.data" class="chart-shell">
    <div class="chart-shell__header">
      <div>
        <p class="text-label">Chart</p>
        <h3>{{ chartType }}</h3>
      </div>
      <span class="text-body-sm">{{ section.data.length }} plotted points</span>
    </div>

    <component :is="resolvedComponent" :series="section.data" class="chart-shell__body" />
  </section>
</template>

<style scoped>
.chart-shell {
  display: grid;
  gap: 14px;
  border: var(--border-ghost);
  border-radius: 18px;
  background: linear-gradient(180deg, color-mix(in srgb, var(--surface-report) 92%, transparent), var(--color-surface-container-low));
  padding: 18px;
}

.chart-shell__header {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: end;
}

.chart-shell__body {
  min-height: 280px;
}
</style>
