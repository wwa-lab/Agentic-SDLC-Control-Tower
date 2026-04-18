<script setup lang="ts">
import type { ReportScope } from '../../types';
import { REPORT_SCOPE_OPTIONS } from '../../utils';

const props = defineProps<{
  scope: ReportScope;
  modelValue: string[];
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string[]];
}>();

function toggle(id: string) {
  const next = props.modelValue.includes(id)
    ? props.modelValue.filter(value => value !== id)
    : [...props.modelValue, id];
  emit('update:modelValue', next);
}
</script>

<template>
  <fieldset class="scope-options">
    <legend class="text-label">Entities</legend>

    <label
      v-for="option in REPORT_SCOPE_OPTIONS[scope]"
      :key="option.id"
      class="scope-option"
    >
      <input
        type="checkbox"
        :checked="modelValue.includes(option.id)"
        @change="toggle(option.id)"
      />
      <span>{{ option.label }}</span>
      <span class="text-tech">{{ option.id }}</span>
    </label>
  </fieldset>
</template>

<style scoped>
.scope-options {
  display: grid;
  gap: 8px;
  border: none;
}

.scope-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border: var(--border-ghost);
  border-radius: 10px;
  background: color-mix(in srgb, var(--color-surface-container-high) 84%, transparent);
}

.scope-option input {
  accent-color: var(--color-secondary);
}
</style>
