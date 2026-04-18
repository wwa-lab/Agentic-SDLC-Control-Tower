<script setup lang="ts">
import { computed } from 'vue';
import type { SeriesPoint } from '../../types';
import { REPORT_CHART_COLORS } from '../../chartTheme';

const props = defineProps<{
  series: SeriesPoint[];
}>();

const categories = computed(() => [...new Set(props.series.map(point => point.x))]);
const groups = computed(() => [...new Set(props.series.map(point => point.groupKey))]);
const max = computed(() => Math.max(...props.series.map(point => point.y), 1));
</script>

<template>
  <svg viewBox="0 0 700 260" class="chart">
    <g v-for="(category, categoryIndex) in categories" :key="String(category)">
      <g
        v-for="(groupKey, groupIndex) in groups"
        :key="`${String(category)}-${groupKey}`"
      >
        <rect
          :x="90 + categoryIndex * 150 + groupIndex * 30"
          :y="200 - ((series.find(point => point.x === category && point.groupKey === groupKey)?.y ?? 0) / max) * 140"
          width="22"
          :height="Math.max(6, ((series.find(point => point.x === category && point.groupKey === groupKey)?.y ?? 0) / max) * 140)"
          :fill="REPORT_CHART_COLORS[groupIndex % REPORT_CHART_COLORS.length]"
          rx="6"
        />
      </g>
      <text :x="122 + categoryIndex * 150" y="224" text-anchor="middle" class="label">{{ category }}</text>
    </g>
  </svg>
</template>

<style scoped>
.chart {
  width: 100%;
  height: 100%;
}

.label {
  fill: var(--color-on-surface-variant);
  font-size: 11px;
}
</style>
