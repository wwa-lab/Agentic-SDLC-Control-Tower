<script setup lang="ts">
import type { StageCoverage } from '../types';

interface Props {
  coverage: StageCoverage;
}

defineProps<Props>();
</script>

<template>
  <div class="coverage-chain">
    <template v-for="(entry, index) in coverage" :key="entry.stageKey">
      <div
        class="coverage-node"
        :class="{ 'coverage-node--covered': entry.covered }"
        :title="`${entry.stageLabel}: ${entry.activeSkillCount} active skills`"
      >
        <span class="coverage-node__label">{{ entry.stageLabel }}</span>
        <strong>{{ entry.activeSkillCount }}</strong>
      </div>
      <div v-if="index < coverage.length - 1" class="coverage-link" :class="{ 'coverage-link--active': entry.covered && coverage[index + 1].covered }"></div>
    </template>
  </div>
</template>

<style scoped>
.coverage-chain {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.coverage-node {
  min-width: 108px;
  padding: 12px 10px;
  border-radius: 999px;
  border: var(--border-ghost);
  background: rgba(255, 255, 255, 0.03);
  display: flex;
  flex-direction: column;
  gap: 6px;
  text-align: center;
  color: var(--color-on-surface-variant);
}

.coverage-node--covered {
  color: var(--color-on-surface);
  background: rgba(78, 222, 163, 0.12);
  box-shadow: inset 0 0 0 1px rgba(78, 222, 163, 0.22);
}

.coverage-node__label {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.coverage-link {
  height: 2px;
  min-width: 18px;
  background: rgba(255, 255, 255, 0.12);
}

.coverage-link--active {
  background: rgba(78, 222, 163, 0.5);
}
</style>
