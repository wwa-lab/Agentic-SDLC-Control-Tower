<script setup lang="ts">
import { computed } from 'vue';
import type { SeriesPoint } from '../../types';
import { REPORT_CHART_COLORS } from '../../chartTheme';

const props = defineProps<{
  series: SeriesPoint[];
}>();

const groups = computed(() => [...new Set(props.series.map(point => point.x))]);
const grouped = computed(() =>
  groups.value.map(group => ({
    group,
    items: props.series.filter(point => point.x === group),
    total: props.series.filter(point => point.x === group).reduce((sum, point) => sum + point.y, 0),
  })),
);
const max = computed(() => Math.max(...grouped.value.map(group => group.total), 1));
</script>

<template>
  <svg viewBox="0 0 640 260" class="chart">
    <g v-for="(group, groupIndex) in grouped" :key="String(group.group)">
      <g v-for="(point, pointIndex) in group.items" :key="point.groupKey">
        <rect
          :x="90 + groupIndex * 140"
          :y="200 - ((group.items.slice(0, pointIndex + 1).reduce((sum, item) => sum + item.y, 0)) / max) * 140"
          width="64"
          :height="Math.max(6, (point.y / max) * 140)"
          :fill="REPORT_CHART_COLORS[pointIndex % REPORT_CHART_COLORS.length]"
          rx="6"
        />
      </g>
      <text :x="122 + groupIndex * 140" y="226" text-anchor="middle" class="label">{{ group.group }}</text>
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
