<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  markdown: string;
  evidence?: ReadonlyArray<{ kind: string; id: string; label?: string }>;
}>();

const sanitized = computed(() => {
  return props.markdown
    .replace(/<script[\s\S]*?<\/script>/gi, '')
    .replace(/<style[\s\S]*?<\/style>/gi, '')
    .replace(/on\w+="[^"]*"/gi, '');
});
</script>

<template>
  <div class="release-notes">
    <div class="body" v-html="sanitized" />
    <div v-if="evidence?.length" class="evidence-footer">
      <span class="evidence-label">Evidence:</span>
      <span v-for="e in evidence" :key="e.id" class="evidence-item">
        {{ e.kind }}:{{ e.label ?? e.id }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.release-notes {
  font-family: var(--font-ui);
  font-size: 0.85rem;
  color: var(--color-on-surface);
  line-height: 1.6;
}
.body :deep(h2) { font-size: 1rem; margin: 12px 0 6px; }
.body :deep(ul) { padding-left: 20px; }
.body :deep(li) { margin-bottom: 4px; }
.body :deep(strong) { color: var(--color-primary); }
.body :deep(code) { font-family: var(--font-tech); font-size: 0.8rem; background: var(--color-surface-container-high); padding: 1px 4px; border-radius: 2px; }
.evidence-footer {
  margin-top: 12px;
  padding-top: 8px;
  border-top: var(--border-ghost);
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 0.7rem;
  color: var(--color-on-surface-variant);
}
.evidence-label { font-weight: 600; }
.evidence-item {
  padding: 1px 6px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  font-family: var(--font-tech);
}
</style>
