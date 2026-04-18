<script setup lang="ts">
import { computed } from 'vue';
import type { CaseRunOutcome } from '../types';
import { formatDateTime, outcomeTone } from '../utils';

interface Props {
  outcomes: ReadonlyArray<CaseRunOutcome>;
}

const props = defineProps<Props>();

const bars = computed(() =>
  props.outcomes.map(outcome => ({
    id: outcome.resultId,
    outcome: outcome.outcome,
    tone: outcomeTone(outcome.outcome),
    title: `${outcome.outcome} • ${formatDateTime(outcome.createdAt)}`,
  })),
);
</script>

<template>
  <div class="tm-sparkline" role="img" :aria-label="`Recent outcomes: ${bars.map(bar => bar.outcome).join(', ')}`">
    <span
      v-for="bar in bars"
      :key="bar.id"
      class="tm-sparkline__bar"
      :class="`is-${bar.tone}`"
      :title="bar.title"
    ></span>
  </div>
</template>

<style scoped>
.tm-sparkline {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: 1fr;
  gap: 6px;
  align-items: end;
  min-height: 48px;
}

.tm-sparkline__bar {
  min-width: 10px;
  height: 42px;
  border-radius: 999px;
  opacity: 0.85;
}

.tm-sparkline__bar.is-success {
  background: var(--tm-coverage-green);
}

.tm-sparkline__bar.is-running {
  background: var(--tm-coverage-amber);
}

.tm-sparkline__bar.is-danger {
  background: var(--tm-coverage-red);
}

.tm-sparkline__bar.is-muted {
  background: var(--tm-coverage-grey);
}
</style>
