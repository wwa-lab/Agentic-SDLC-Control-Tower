<script setup lang="ts">
import type { VersionDrift } from '../types/environments';

interface Props {
  drift: VersionDrift;
}

defineProps<Props>();
</script>

<template>
  <span class="drift-indicator" :class="`drift-indicator--${drift.band.toLowerCase()}`" :title="drift.description">
    {{ drift.band }} / {{ drift.commitDelta }} commits
  </span>
</template>

<style scoped>
.drift-indicator {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 0.625rem;
  font-family: var(--font-tech);
}

.drift-indicator--none {
  background: rgba(78, 222, 163, 0.12);
  color: var(--color-health-emerald);
}

.drift-indicator--minor {
  background: rgba(245, 158, 11, 0.12);
  color: var(--color-approval-amber);
}

.drift-indicator--major {
  background: rgba(255, 180, 171, 0.12);
  color: var(--color-incident-crimson);
}
</style>
