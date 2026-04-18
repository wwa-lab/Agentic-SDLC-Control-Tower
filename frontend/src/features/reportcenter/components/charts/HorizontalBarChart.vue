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
  <div class="horizontal-bars">
    <div v-for="(point, index) in series" :key="point.groupKey" class="horizontal-bars__row">
      <span class="horizontal-bars__label">{{ point.x }}</span>
      <div class="horizontal-bars__track">
        <div
          class="horizontal-bars__fill"
          :style="{ width: `${(point.y / max) * 100}%`, background: REPORT_CHART_COLORS[index % REPORT_CHART_COLORS.length] }"
        />
      </div>
      <span class="horizontal-bars__value">{{ point.y.toFixed(1) }}%</span>
    </div>
  </div>
</template>

<style scoped>
.horizontal-bars {
  display: grid;
  gap: 12px;
}

.horizontal-bars__row {
  display: grid;
  grid-template-columns: 140px 1fr 70px;
  gap: 12px;
  align-items: center;
}

.horizontal-bars__track {
  min-height: 14px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--color-surface-container-high) 90%, transparent);
  overflow: hidden;
}

.horizontal-bars__fill {
  height: 100%;
  border-radius: 999px;
}

.horizontal-bars__label,
.horizontal-bars__value {
  font-size: 0.875rem;
}
</style>
