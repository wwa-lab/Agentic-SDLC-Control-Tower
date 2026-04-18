<script setup lang="ts">
import type { EnvironmentKind } from '../../types/enums';

defineProps<{ name: string; kind: EnvironmentKind }>();

const CSS_MAP: Record<EnvironmentKind, string> = {
  DEV: 'var(--dp-env-dev)',
  TEST: 'var(--dp-env-qa)',
  STAGING: 'var(--dp-env-staging)',
  PROD: 'var(--dp-env-prod)',
  CUSTOM: 'var(--color-on-surface-variant)',
};
</script>

<template>
  <span
    class="env-chip"
    :class="{ 'env-prod': kind === 'PROD' }"
    :style="{ '--env-color': CSS_MAP[kind] }"
  >
    {{ name.toUpperCase() }}
  </span>
</template>

<style scoped>
.env-chip {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--env-color);
  color: var(--env-color);
  font-family: var(--font-tech);
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  white-space: nowrap;
}
.env-prod {
  border-width: 2px;
}
</style>
