<script setup lang="ts">
import type { ImportSourceType } from '../types/requirement';

interface Props {
  activeSource: ImportSourceType;
}

defineProps<Props>();
const emit = defineEmits<{ select: [source: ImportSourceType] }>();

const TABS: ReadonlyArray<{ key: ImportSourceType; label: string }> = [
  { key: 'paste', label: 'Paste Text' },
  { key: 'file', label: 'Upload File' },
  { key: 'email', label: 'Email' },
  { key: 'meeting', label: 'Meeting Summary' },
];
</script>

<template>
  <div class="source-tabs">
    <button
      v-for="tab in TABS"
      :key="tab.key"
      class="tab"
      :class="{ 'tab--active': activeSource === tab.key }"
      @click="emit('select', tab.key)"
    >
      {{ tab.label }}
    </button>
  </div>
</template>

<style scoped>
.source-tabs {
  display: flex;
  gap: 0;
  background: var(--color-surface-container-low);
  border-radius: var(--radius-sm);
  overflow: hidden;
  border: var(--border-ghost);
}

.tab {
  flex: 1;
  background: none;
  border: none;
  padding: 8px 12px;
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-on-surface-variant);
  cursor: pointer;
  transition: all 0.2s ease;
  border-right: 1px solid var(--border-separator);
}

.tab:last-child { border-right: none; }

.tab--active {
  background: var(--color-surface-container-high);
  color: var(--color-secondary);
}

.tab:hover:not(.tab--active) {
  background: var(--nav-hover-bg);
}
</style>
