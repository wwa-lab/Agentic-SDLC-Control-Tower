<script setup lang="ts">
import type { ReleaseState } from '../../types/enums';

defineProps<{ state: ReleaseState }>();

const CONFIG: Record<ReleaseState, { label: string; cssVar: string }> = {
  PREPARED: { label: 'Prepared', cssVar: 'var(--dp-release-prepared)' },
  DEPLOYED: { label: 'Deployed', cssVar: 'var(--dp-release-deployed)' },
  SUPERSEDED: { label: 'Superseded', cssVar: 'var(--dp-release-superseded)' },
  ABANDONED: { label: 'Abandoned', cssVar: 'var(--dp-release-abandoned)' },
};
</script>

<template>
  <span class="release-state" :style="{ color: CONFIG[state].cssVar }">
    <span class="dot" :style="{ background: CONFIG[state].cssVar }" />
    {{ CONFIG[state].label }}
  </span>
</template>

<style scoped>
.release-state {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
</style>
