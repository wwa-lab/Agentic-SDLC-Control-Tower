<script setup lang="ts">
import { ArrowDownRight, ArrowRightLeft, ArrowUpRight, History } from 'lucide-vue-next';
import type { TeamMetricItem } from '../types/metrics';

interface Props {
  item: TeamMetricItem;
}

defineProps<Props>();

defineEmits<{
  navigateHistory: [url: string];
}>();

function formatValue(item: TeamMetricItem): string {
  if (item.unit === 'PERCENT') {
    return `${item.currentValue}%`;
  }
  return `${item.currentValue}${item.unit === 'DAYS' ? 'd' : item.unit === 'HOURS' ? 'h' : ''}`;
}

function trendIcon(trend: TeamMetricItem['trend']) {
  switch (trend) {
    case 'UP':
      return ArrowUpRight;
    case 'DOWN':
      return ArrowDownRight;
    default:
      return ArrowRightLeft;
  }
}
</script>

<template>
  <div class="metric-item" :title="item.tooltip">
    <div class="metric-item__main">
      <div>
        <span class="text-label">{{ item.label }}</span>
        <strong class="metric-item__value">{{ formatValue(item) }}</strong>
      </div>
      <component :is="trendIcon(item.trend)" :size="16" />
    </div>
    <div class="metric-item__footer">
      <span class="text-body-sm">Prev: {{ item.previousValue }}</span>
      <button class="history-link" @click="$emit('navigateHistory', item.historyLink)">
        <History :size="14" />
        History
      </button>
    </div>
  </div>
</template>

<style scoped>
.metric-item {
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.03);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.metric-item__main,
.metric-item__footer {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.metric-item__value {
  display: block;
  margin-top: 6px;
  font-size: 1.2rem;
}

.history-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  color: var(--color-secondary);
  cursor: pointer;
}
</style>
