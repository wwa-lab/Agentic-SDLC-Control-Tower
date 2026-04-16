<script setup lang="ts">
import { ShieldAlert } from 'lucide-vue-next';
import type { CoverageGap } from '../types/members';

interface Props {
  gaps: ReadonlyArray<CoverageGap>;
}

defineProps<Props>();
</script>

<template>
  <div v-if="gaps.length" class="coverage-gap-banner">
    <div class="coverage-gap-banner__header">
      <ShieldAlert :size="14" />
      <span class="text-label">Coverage Gaps</span>
    </div>
    <div class="coverage-gap-banner__items">
      <div v-for="gap in gaps" :key="`${gap.kind}-${gap.description}`" class="coverage-gap-banner__item">
        <strong>{{ gap.kind }}</strong>
        <span>{{ gap.description }}</span>
        <span v-if="gap.window" class="window">{{ gap.window }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.coverage-gap-banner {
  border-radius: var(--radius-sm);
  border: 1px solid rgba(244, 166, 102, 0.3);
  background: rgba(244, 166, 102, 0.08);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.coverage-gap-banner__header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-approval-amber);
}

.coverage-gap-banner__items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.coverage-gap-banner__item {
  display: grid;
  grid-template-columns: minmax(96px, auto) 1fr auto;
  gap: 10px;
  font-size: 0.75rem;
  color: var(--color-on-surface);
}

.window {
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
}

@media (max-width: 1200px) {
  .coverage-gap-banner__item {
    grid-template-columns: 1fr;
  }
}
</style>
