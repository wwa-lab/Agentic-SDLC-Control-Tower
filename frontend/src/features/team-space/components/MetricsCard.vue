<script setup lang="ts">
import { BarChart3 } from 'lucide-vue-next';
import type { SectionResult } from '@/shared/types/section';
import type { TeamMetrics } from '../types/metrics';
import TeamSpaceCardShell from './TeamSpaceCardShell.vue';
import MetricItem from './MetricItem.vue';

interface Props {
  section: SectionResult<TeamMetrics>;
  isLoading?: boolean;
}

withDefaults(defineProps<Props>(), {
  isLoading: false,
});

defineEmits<{
  retry: [];
  navigateHistory: [url: string];
}>();

type MetricGroupKey =
  | 'deliveryEfficiency'
  | 'quality'
  | 'stability'
  | 'governanceMaturity'
  | 'aiParticipation';

const groups: ReadonlyArray<{ key: MetricGroupKey; label: string }> = [
  { key: 'deliveryEfficiency', label: 'Delivery Efficiency' },
  { key: 'quality', label: 'Quality' },
  { key: 'stability', label: 'Stability' },
  { key: 'governanceMaturity', label: 'Governance Maturity' },
  { key: 'aiParticipation', label: 'AI Participation' },
];
</script>

<template>
  <TeamSpaceCardShell
    title="Team Metrics"
    :loading="isLoading"
    :error="section.error"
    @retry="$emit('retry')"
  >
    <template #icon>
      <BarChart3 :size="16" />
    </template>

    <template v-if="section.data">
      <div class="metrics-groups">
        <div v-for="group in groups" :key="group.key" class="metrics-group">
          <span class="text-label">{{ group.label }}</span>
          <div class="metric-list">
            <MetricItem
              v-for="item in section.data[group.key]"
              :key="item.key"
              :item="item"
              @navigate-history="$emit('navigateHistory', $event)"
            />
          </div>
        </div>
      </div>
      <div class="metrics-footer text-tech">
        Last refreshed {{ section.data.lastRefreshed }}
      </div>
    </template>
  </TeamSpaceCardShell>
</template>

<style scoped>
.metrics-groups {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.metrics-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metric-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.metrics-footer {
  color: var(--color-on-surface-variant);
  font-size: 0.75rem;
}

@media (max-width: 1200px) {
  .metric-list {
    grid-template-columns: 1fr;
  }
}
</style>
