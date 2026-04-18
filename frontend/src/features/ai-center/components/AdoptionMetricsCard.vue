<script setup lang="ts">
import { computed } from 'vue';
import { Gauge } from 'lucide-vue-next';
import MetricTile from './MetricTile.vue';
import { METRIC_TITLES } from '../constants';
import type { MetricsSummary } from '../types';

interface Props {
  metrics: MetricsSummary | null;
  loading: boolean;
  error: string | null;
}

const props = defineProps<Props>();

defineEmits<{
  retry: [];
}>();

const allEmpty = computed(() => {
  if (!props.metrics) {
    return false;
  }
  return METRIC_TITLES.every(entry => props.metrics?.[entry.key].data == null && props.metrics?.[entry.key].error == null);
});
</script>

<template>
  <section class="metrics-card section-high">
    <header class="metrics-card__header">
      <div>
        <p class="text-label">Metrics</p>
        <h3>Adoption Metrics</h3>
      </div>
      <Gauge :size="18" class="metrics-card__icon" />
    </header>

    <div v-if="loading" class="metrics-card__grid" data-state="loading">
      <div v-for="index in 5" :key="index" class="metrics-card__skeleton"></div>
    </div>

    <div v-else-if="error" class="metrics-card__error" data-state="error">
      <p class="text-label">Metrics unavailable</p>
      <p class="text-body-sm">{{ error }}</p>
      <button class="btn-machined" type="button" @click="$emit('retry')">Retry</button>
    </div>

    <div v-else-if="allEmpty" class="metrics-card__empty" data-state="empty">
      <p class="text-label">Not enough data yet</p>
      <p class="text-body-sm">Run some AI skills to see adoption and time-saved trends.</p>
    </div>

    <div v-else-if="metrics" class="metrics-card__grid" data-state="normal">
      <MetricTile
        v-for="entry in METRIC_TITLES"
        :key="entry.key"
        :label="entry.label"
        :value="metrics[entry.key]"
        @retry="$emit('retry')"
      />
    </div>
  </section>
</template>

<style scoped>
.metrics-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
}

.metrics-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.metrics-card__grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.metrics-card__skeleton {
  min-height: 132px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.03), rgba(137, 206, 255, 0.08), rgba(255, 255, 255, 0.03));
  background-size: 220% 100%;
  animation: metrics-pulse 1.2s ease-in-out infinite;
}

.metrics-card__error,
.metrics-card__empty {
  padding: 16px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
  background: rgba(255, 255, 255, 0.03);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.metrics-card__error {
  color: var(--color-incident-crimson);
}

@keyframes metrics-pulse {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}
</style>
