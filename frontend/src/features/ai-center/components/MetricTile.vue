<script setup lang="ts">
import { computed } from 'vue';
import { TrendingDown, TrendingUp, Minus, AlertCircle } from 'lucide-vue-next';
import type { MetricValue, SectionResult } from '../types';
import { formatDelta, formatMetricValue } from '../utils';

interface Props {
  label: string;
  value: SectionResult<MetricValue>;
}

const props = defineProps<Props>();

defineEmits<{
  retry: [];
}>();

const trendIcon = computed(() => {
  const trend = props.value.data?.trend;
  if (trend === 'up') return TrendingUp;
  if (trend === 'down') return TrendingDown;
  return Minus;
});
</script>

<template>
  <article class="metric-tile">
    <div class="metric-tile__header">
      <span class="text-label">{{ label }}</span>
      <button
        v-if="value.error"
        class="metric-tile__retry"
        type="button"
        :aria-label="`Retry ${label}`"
        @click="$emit('retry')"
      >
        <AlertCircle :size="14" />
      </button>
    </div>

    <div v-if="value.error" class="metric-tile__error">
      <strong>Unavailable</strong>
      <p class="text-body-sm">{{ value.error }}</p>
    </div>

    <div v-else-if="value.data" class="metric-tile__body">
      <strong class="metric-tile__value">{{ formatMetricValue(value.data.value, value.data.unit) }}</strong>
      <div class="metric-tile__trend" :class="{ 'metric-tile__trend--negative': !value.data.isPositive && value.data.trend !== 'flat' }">
        <component :is="trendIcon" :size="14" />
        <span>{{ formatDelta(value.data.delta, value.data.unit) }}</span>
      </div>
    </div>

    <div v-else class="metric-tile__empty">
      <strong>Not enough data</strong>
      <p class="text-body-sm">Run a few skills to unlock this metric.</p>
    </div>
  </article>
</template>

<style scoped>
.metric-tile {
  min-height: 132px;
  padding: 16px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.03), rgba(137, 206, 255, 0.02));
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.metric-tile__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.metric-tile__retry {
  border: none;
  background: transparent;
  color: var(--color-incident-crimson);
  cursor: pointer;
  display: inline-flex;
}

.metric-tile__body,
.metric-tile__error,
.metric-tile__empty {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metric-tile__value {
  font-size: 1.8rem;
  line-height: 1;
}

.metric-tile__trend {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--color-health-emerald);
  font-size: 0.875rem;
}

.metric-tile__trend--negative {
  color: var(--color-incident-crimson);
}

.metric-tile__error {
  color: var(--color-incident-crimson);
}
</style>
