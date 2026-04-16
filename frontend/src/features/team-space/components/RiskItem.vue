<script setup lang="ts">
import { ExternalLink } from 'lucide-vue-next';
import type { RiskItem } from '../types/risks';

interface Props {
  item: RiskItem;
}

defineProps<Props>();

defineEmits<{
  navigate: [url: string];
}>();
</script>

<template>
  <div class="risk-item" :class="{ 'risk-item--critical': item.severity === 'CRITICAL' }">
    <div class="risk-item__header">
      <div>
        <span class="text-label">{{ item.category }} / {{ item.severity }}</span>
        <strong>{{ item.title }}</strong>
      </div>
      <span class="text-tech">{{ item.ageDays }}d</span>
    </div>
    <p class="text-body-sm">{{ item.detail }}</p>
    <button class="risk-action" @click="$emit('navigate', item.primaryAction.url)">
      <ExternalLink :size="14" />
      {{ item.primaryAction.label }}
    </button>
  </div>
</template>

<style scoped>
.risk-item {
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.03);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.risk-item--critical {
  border-color: rgba(224, 63, 94, 0.35);
  background: rgba(224, 63, 94, 0.08);
}

.risk-item__header {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.risk-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  align-self: flex-start;
  border: none;
  background: transparent;
  color: var(--color-secondary);
  cursor: pointer;
}
</style>
