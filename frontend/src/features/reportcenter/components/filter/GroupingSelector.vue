<script setup lang="ts">
defineProps<{
  modelValue: string;
  supportedGroupings: string[];
}>();

defineEmits<{
  'update:modelValue': [value: string];
}>();
</script>

<template>
  <label class="field">
    <span class="text-label">Grouping</span>
    <select class="field__control" :value="modelValue" @change="$emit('update:modelValue', ($event.target as HTMLSelectElement).value)">
      <option v-for="grouping in supportedGroupings" :key="grouping" :value="grouping">
        {{ grouping }}
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
