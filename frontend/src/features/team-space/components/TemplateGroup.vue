<script setup lang="ts">
import type { TemplateEntry } from '../types/templates';

interface Props {
  label: string;
  entries: ReadonlyArray<TemplateEntry>;
}

defineProps<Props>();
</script>

<template>
  <div class="template-group">
    <div class="template-group__header">
      <span class="text-label">{{ label }}</span>
      <span class="text-tech">{{ entries.length }}</span>
    </div>
    <div v-if="entries.length" class="template-group__list">
      <div v-for="entry in entries" :key="entry.id" class="template-entry">
        <div>
          <strong>{{ entry.name }}</strong>
          <p class="text-body-sm">v{{ entry.version }}</p>
        </div>
        <span class="entry-kind text-label">{{ entry.kind }}</span>
      </div>
    </div>
    <p v-else class="text-body-sm template-group__empty">No templates bound for this type.</p>
  </div>
</template>

<style scoped>
.template-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.template-group__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.template-group__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.template-entry {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.entry-kind {
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(137, 206, 255, 0.08);
}

.template-group__empty {
  color: var(--color-on-surface-variant);
}
</style>
