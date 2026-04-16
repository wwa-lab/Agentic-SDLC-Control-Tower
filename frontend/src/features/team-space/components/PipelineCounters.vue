<script setup lang="ts">
import type { PipelineCounters } from '../types/pipeline';

interface Props {
  counters: PipelineCounters;
}

defineProps<Props>();

defineEmits<{
  navigateFilter: [filter: string];
}>();

const counterConfig: ReadonlyArray<{ key: keyof PipelineCounters; label: string; filter: string }> = [
  { key: 'requirementsInflow7d', label: 'REQ INFLOW 7D', filter: 'recent-inflow' },
  { key: 'storiesDecomposing', label: 'STORIES DECOMPOSING', filter: 'stories-decomposing' },
  { key: 'specsGenerating', label: 'SPECS GENERATING', filter: 'specs-generating' },
  { key: 'specsInReview', label: 'SPECS IN REVIEW', filter: 'specs-in-review' },
  { key: 'specsBlocked', label: 'SPECS BLOCKED', filter: 'blocked-specs' },
  { key: 'specsApprovedAwaitingDownstream', label: 'APPROVED AWAITING', filter: 'approved-awaiting' },
];
</script>

<template>
  <div class="pipeline-counter-grid">
    <button
      v-for="counter in counterConfig"
      :key="counter.key"
      class="pipeline-counter"
      @click="$emit('navigateFilter', counter.filter)"
    >
      <span class="text-label">{{ counter.label }}</span>
      <strong>{{ counters[counter.key] }}</strong>
    </button>
  </div>
</template>

<style scoped>
.pipeline-counter-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

.pipeline-counter {
  border: 1px solid rgba(137, 206, 255, 0.1);
  background: rgba(137, 206, 255, 0.05);
  border-radius: var(--radius-sm);
  padding: 12px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  color: inherit;
  cursor: pointer;
}

.pipeline-counter strong {
  font-size: 1.25rem;
}

@media (max-width: 1400px) {
  .pipeline-counter-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .pipeline-counter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
