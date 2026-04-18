<script setup lang="ts">
import type { LogExcerptBlock as LogExcerpt } from '../types';

interface Props {
  block: LogExcerpt;
}

defineProps<Props>();
</script>

<template>
  <div class="log-block">
    <div class="log-block__header">
      <p class="text-label">Redacted Excerpt</p>
      <a :href="block.githubUrl" rel="noopener noreferrer" target="_blank">Open in GitHub ↗</a>
    </div>
    <div class="log-block__panel">
      <div v-for="line in block.lines" :key="line.lineNumber" class="log-block__line">
        <span class="log-block__number">{{ line.lineNumber }}</span>
        <span :class="{ 'log-block__redacted': line.redacted }">{{ line.text }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.log-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.log-block__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.log-block__header a {
  color: var(--color-secondary);
  text-decoration: none;
  font-size: 0.8rem;
}

.log-block__panel {
  max-height: 280px;
  overflow: auto;
  padding: 12px;
  border-radius: var(--radius-md);
  background: rgba(6, 14, 32, 0.72);
  border: var(--border-ghost);
  font-family: var(--font-tech);
  font-size: 0.77rem;
}

.log-block__line {
  display: grid;
  grid-template-columns: 54px 1fr;
  gap: 12px;
  line-height: 1.5;
}

.log-block__number {
  color: var(--color-on-surface-variant);
}

.log-block__redacted {
  color: var(--color-on-surface-variant);
}
</style>

