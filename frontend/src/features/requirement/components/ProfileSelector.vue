<script setup lang="ts">
import type { PipelineProfile } from '../types/requirement';

interface Props {
  profiles: ReadonlyArray<PipelineProfile>;
  modelValue: string;
}

defineProps<Props>();

const emit = defineEmits<{
  'update:modelValue': [profileId: string];
}>();

function handleChange(event: Event) {
  emit('update:modelValue', (event.target as HTMLSelectElement).value);
}
</script>

<template>
  <label class="profile-selector">
    <span class="profile-label">Profile</span>
    <select class="profile-select" :value="modelValue" @change="handleChange">
      <option v-for="profile in profiles" :key="profile.id" :value="profile.id">
        {{ profile.name }}
      </option>
    </select>
  </label>
</template>

<style scoped>
.profile-selector {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.profile-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}

.profile-select {
  min-width: 128px;
  background: var(--color-surface-container-high);
  border: 1px solid rgba(137, 206, 255, 0.2);
  border-radius: var(--radius-sm);
  color: var(--color-secondary);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 600;
  line-height: 1;
  padding: 5px 28px 5px 8px;
  text-transform: uppercase;
  cursor: pointer;
}

.profile-select:focus {
  outline: 1px solid var(--color-secondary);
  outline-offset: 2px;
}
</style>
