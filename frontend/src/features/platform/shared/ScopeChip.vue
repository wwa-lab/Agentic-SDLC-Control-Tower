<script setup lang="ts">
import type { ScopeType } from './types';

defineProps<{
  scopeType: ScopeType;
  scopeId: string;
}>();
</script>

<template>
  <span class="scope-chip" :title="`${scopeType}:${scopeId}`">
    {{ scopeType }}:{{ scopeId === '*' ? '∗' : scopeId }}
  </span>
</template>

<style scoped>
.scope-chip {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.02em;
  font-family: var(--font-mono, monospace);
  border-radius: 3px;
  background: var(--color-surface-hover, rgba(255,255,255,0.06));
  color: var(--color-on-surface-variant, #aaa);
}
</style>
