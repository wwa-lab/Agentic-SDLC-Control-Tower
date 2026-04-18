<script setup lang="ts">
import type { ReportScope } from '../../types';

defineProps<{
  modelValue: ReportScope;
  supportedScopes: ReportScope[];
}>();

defineEmits<{
  'update:modelValue': [value: ReportScope];
}>();
</script>

<template>
  <label class="field">
    <span class="text-label">Scope</span>
    <select class="field__control" :value="modelValue" @change="$emit('update:modelValue', ($event.target as HTMLSelectElement).value as ReportScope)">
      <option v-for="scope in supportedScopes" :key="scope" :value="scope">
        {{ scope }}
      </option>
    </select>
  </label>
</template>

<style scoped>
.field {
  display: grid;
  gap: 8px;
}

.field__control {
  border: var(--border-ghost);
  border-radius: 10px;
  background: var(--color-surface-container-high);
  color: var(--color-on-surface);
  min-height: 40px;
  padding: 0 12px;
}
</style>
