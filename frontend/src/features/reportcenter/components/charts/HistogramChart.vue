<script setup lang="ts">
import { computed } from 'vue';
import type { SeriesPoint } from '../../types';
import { REPORT_CHART_COLORS } from '../../chartTheme';

const props = defineProps<{
  series: SeriesPoint[];
}>();

const max = computed(() => Math.max(...props.series.map(point => point.y), 1));
</script>

<template>
  <svg viewBox="0 0 640 240" class="chart">
    <line x1="52" y1="200" x2="610" y2="200" class="axis" />
    <g v-for="(point, index) in series" :key="`${point.groupKey}-${point.x}`">
      <rect
        :x="70 + index * 96"
        :y="200 - (point.y / max) * 140"
        width="52"
        :height="Math.max(8, (point.y / max) * 140)"
        :fill="REPORT_CHART_COLORS[index % REPORT_CHART_COLORS.length]"
        rx="8"
      />
      <text :x="96 + index * 96" y="220" text-anchor="middle" class="label">{{ point.x }}</text>
    </g>
  </svg>
</template>

<style scoped>
.chart {
  width: 100%;
  height: 100%;
}

.axis {
  stroke: var(--color-on-surface-variant);
  stroke-opacity: 0.4;
}

.label {
  fill: var(--color-on-surface-variant);
  font-size: 10px;
}
</style>
