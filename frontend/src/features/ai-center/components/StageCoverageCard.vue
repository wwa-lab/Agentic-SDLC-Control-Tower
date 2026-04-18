<script setup lang="ts">
import { computed } from 'vue';
import { GitBranch } from 'lucide-vue-next';
import StageCoverageChain from './StageCoverageChain.vue';
import type { StageCoverage } from '../types';

interface Props {
  coverage: StageCoverage | null;
  loading: boolean;
  error: string | null;
}

const props = defineProps<Props>();

defineEmits<{
  retry: [];
}>();

const coveredCount = computed(() => props.coverage?.filter(entry => entry.covered).length ?? 0);
</script>

<template>
  <section class="coverage-card section-high">
    <header class="coverage-card__header">
      <div>
        <p class="text-label">Coverage</p>
        <h3>Stage Coverage</h3>
      </div>
      <GitBranch :size="18" />
    </header>

    <div v-if="loading" class="coverage-card__skeleton" data-state="loading"></div>

    <div v-else-if="error" class="coverage-card__error" data-state="error">
      <p class="text-label">Coverage unavailable</p>
      <p class="text-body-sm">{{ error }}</p>
      <button class="btn-machined" type="button" @click="$emit('retry')">Retry</button>
    </div>

    <template v-else-if="coverage">
      <div class="coverage-card__meta">
        <strong>{{ coveredCount }} / {{ coverage.length }}</strong>
        <span class="text-body-sm">Stages currently covered by active or beta skills.</span>
      </div>
      <StageCoverageChain :coverage="coverage" />
    </template>
  </section>
</template>

<style scoped>
.coverage-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
}

.coverage-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.coverage-card__meta {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.coverage-card__skeleton,
.coverage-card__error {
  min-height: 120px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
}

.coverage-card__skeleton {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.03), rgba(137, 206, 255, 0.08), rgba(255, 255, 255, 0.03));
  background-size: 220% 100%;
  animation: coverage-pulse 1.2s ease-in-out infinite;
}

.coverage-card__error {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: var(--color-incident-crimson);
}

@keyframes coverage-pulse {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}
</style>
