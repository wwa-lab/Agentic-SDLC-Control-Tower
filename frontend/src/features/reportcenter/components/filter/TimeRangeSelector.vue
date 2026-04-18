<script setup lang="ts">
import type { TimeRange } from '../../types';
import { REPORT_PRESET_LABELS } from '../../utils';

const props = defineProps<{
  modelValue: TimeRange;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: TimeRange];
}>();

function toDateInput(value?: string) {
  return value ? value.slice(0, 10) : '';
}

function updatePreset(preset: string) {
  emit('update:modelValue', {
    preset: preset as TimeRange['preset'],
    startAt: preset === 'custom' ? props.modelValue.startAt : undefined,
    endAt: preset === 'custom' ? props.modelValue.endAt : undefined,
  });
}

function updateDate(kind: 'startAt' | 'endAt', value: string) {
  const iso = value ? `${value}T00:00:00Z` : undefined;
  emit('update:modelValue', {
    ...props.modelValue,
    [kind]: iso,
  });
}
</script>

<template>
  <div class="time-range">
    <label class="field">
      <span class="text-label">Time range</span>
      <select class="field__control" :value="modelValue.preset" @change="updatePreset(($event.target as HTMLSelectElement).value)">
        <option v-for="(label, preset) in REPORT_PRESET_LABELS" :key="preset" :value="preset">{{ label }}</option>
      </select>
    </label>

    <template v-if="modelValue.preset === 'custom'">
      <label class="field">
        <span class="text-label">Start</span>
        <input class="field__control" type="date" :value="toDateInput(modelValue.startAt)" @input="updateDate('startAt', ($event.target as HTMLInputElement).value)" />
      </label>
      <label class="field">
        <span class="text-label">End</span>
        <input class="field__control" type="date" :value="toDateInput(modelValue.endAt)" @input="updateDate('endAt', ($event.target as HTMLInputElement).value)" />
      </label>
    </template>
  </div>
</template>

<style scoped>
.time-range {
  display: grid;
  gap: 10px;
}

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
