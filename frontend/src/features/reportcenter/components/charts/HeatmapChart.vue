<script setup lang="ts">
import { computed } from 'vue';
import type { SeriesPoint } from '../../types';

const props = defineProps<{
  series: SeriesPoint[];
}>();

const rows = computed(() => [...new Set(props.series.map(point => point.groupLabel))]);
const columns = computed(() => [...new Set(props.series.map(point => String(point.x).split('|')[0]))]);
const max = computed(() => Math.max(...props.series.map(point => point.y), 1));

function cellValue(row: string, column: string) {
  return props.series
    .filter(point => point.groupLabel === row && String(point.x).startsWith(`${column}|`))
    .reduce((sum, point) => sum + point.y, 0);
}

function cellOpacity(value: number) {
  return 0.2 + (value / max.value) * 0.7;
}
</script>

<template>
  <div class="heatmap">
    <div class="heatmap__corner" />
    <div v-for="column in columns" :key="column" class="heatmap__header">{{ column }}</div>

    <template v-for="row in rows" :key="row">
      <div class="heatmap__row-label">{{ row }}</div>
      <div
        v-for="column in columns"
        :key="`${row}-${column}`"
        class="heatmap__cell"
        :style="{ opacity: cellOpacity(cellValue(row, column)) }"
      >
        {{ cellValue(row, column) }}
      </div>
    </template>
  </div>
</template>

<style scoped>
.heatmap {
  display: grid;
  grid-template-columns: 140px repeat(auto-fit, minmax(96px, 1fr));
  gap: 8px;
}

.heatmap__header,
.heatmap__row-label,
.heatmap__cell,
.heatmap__corner {
  min-height: 56px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  border: var(--border-ghost);
}

.heatmap__header,
.heatmap__row-label,
.heatmap__corner {
  background: color-mix(in srgb, var(--color-surface-container-high) 80%, transparent);
}

.heatmap__cell {
  background: color-mix(in srgb, var(--color-secondary) 42%, transparent);
  color: var(--color-on-surface);
  font-weight: 600;
}
</style>
