<script setup lang="ts">
import type { AiTriageRow } from '../types';

interface Props {
  row: AiTriageRow;
  canRetry?: boolean;
}

withDefaults(defineProps<Props>(), {
  canRetry: false,
});

defineEmits<{
  retry: [stepId: string];
}>();
</script>

<template>
  <article class="triage-row" :class="`triage-row--${row.status.toLowerCase()}`">
    <div class="triage-row__header">
      <div>
        <p class="text-label">{{ row.status }}</p>
        <h4>{{ row.title }}</h4>
      </div>
      <button
        v-if="canRetry"
        class="btn-machined"
        type="button"
        @click="$emit('retry', row.evidence.stepId)"
      >
        Retry
      </button>
    </div>
    <p class="text-body-sm">{{ row.summary }}</p>
    <p class="text-body-sm triage-row__cause">{{ row.probableCause }}</p>
    <div class="triage-row__meta">
      <span class="text-tech">{{ row.evidence.runId }}</span>
      <span class="text-tech">{{ row.evidence.jobId }}</span>
      <span class="text-tech">{{ row.evidence.stepId }}</span>
      <span class="text-tech">c={{ row.confidence.toFixed(2) }}</span>
    </div>
  </article>
</template>

<style scoped>
.triage-row {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
  background: rgba(255, 255, 255, 0.02);
}

.triage-row__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.triage-row__cause {
  color: var(--color-on-surface-variant);
}

.triage-row__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  color: var(--color-on-surface-variant);
  font-size: 0.75rem;
}

.triage-row--failed_evidence {
  border-color: rgba(255, 180, 171, 0.25);
}
</style>

