<script setup lang="ts">
import { computed } from 'vue';
import type { TrendDirection } from '../types/dashboard';

interface Props {
  label: string;
  value: string;
  trend?: TrendDirection;
  trendIsPositive?: boolean;
  size?: 'sm' | 'md';
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  trend: 'stable',
  trendIsPositive: true
});

const trendIcon = computed(() => {
  if (props.trend === 'up') return '↑';
  if (props.trend === 'down') return '↓';
  return '→';
});

const trendClass = computed(() => {
  if (props.trend === 'stable') return 'trend--stable';
  return props.trendIsPositive ? 'trend--positive' : 'trend--negative';
});
</script>

<template>
  <div class="metric-card" :class="[`metric-card--${size}`]">
    <div class="metric-label">{{ label }}</div>
    <div class="metric-value-container">
      <span class="metric-value">{{ value }}</span>
      <span v-if="trend !== 'stable'" class="metric-trend" :class="trendClass">
        {{ trendIcon }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.metric-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.metric-label {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-on-surface-variant);
}

.metric-value-container {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.metric-value {
  font-family: var(--font-tech);
  font-size: 1.5rem;
  color: var(--color-secondary);
  line-height: 1.2;
}

.metric-card--sm .metric-value {
  font-size: 1.125rem;
}

.metric-trend {
  font-size: 0.75rem;
  font-weight: 700;
}

.trend--positive {
  color: var(--color-health-emerald);
}

.trend--negative {
  color: var(--color-incident-crimson);
}

.trend--stable {
  color: var(--color-on-surface-variant);
}
</style>
