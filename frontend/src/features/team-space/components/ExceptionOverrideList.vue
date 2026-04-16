<script setup lang="ts">
import type { ExceptionOverride } from '../types/templates';

interface Props {
  overrides: ReadonlyArray<ExceptionOverride>;
}

defineProps<Props>();
</script>

<template>
  <div class="override-list">
    <span class="text-label">Exception Overrides</span>
    <div v-if="overrides.length" class="override-list__items">
      <div v-for="override in overrides" :key="`${override.templateId}-${override.overrideScopeId}`" class="override-row">
        <div>
          <strong>{{ override.templateName }}</strong>
          <p class="text-body-sm">{{ override.overrideScopeName }} / {{ override.overrideScopeId }}</p>
        </div>
        <span class="text-tech">{{ override.overriddenAt }}</span>
      </div>
    </div>
    <p v-else class="text-body-sm override-list__empty">No project-level template overrides are active.</p>
  </div>
</template>

<style scoped>
.override-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.override-list__items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.override-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.override-list__empty {
  color: var(--color-on-surface-variant);
}

@media (max-width: 1200px) {
  .override-row {
    flex-direction: column;
  }
}
</style>
