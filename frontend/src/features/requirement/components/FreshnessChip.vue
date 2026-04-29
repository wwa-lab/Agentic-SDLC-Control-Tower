<script setup lang="ts">
import type { FreshnessStatus } from '../types/requirement';

defineProps<{ status: FreshnessStatus | string }>();
</script>

<template>
  <span class="freshness-chip" :class="`freshness-chip--${status.toLowerCase().replaceAll('_', '-')}`">
    {{ status.replaceAll('_', ' ') }}
  </span>
</template>

<style scoped>
.freshness-chip {
  display: inline-flex;
  align-items: center;
  min-height: 18px;
  padding: 2px 7px;
  border-radius: 2px;
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  line-height: 1.2;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-on-surface-variant);
  background: rgba(148, 163, 184, 0.12);
}

.freshness-chip--fresh { color: var(--color-health-emerald); background: rgba(78, 222, 163, 0.12); }
.freshness-chip--source-changed,
.freshness-chip--document-changed-after-review { color: var(--color-approval-amber); background: rgba(245, 158, 11, 0.12); }
.freshness-chip--missing-document,
.freshness-chip--missing-source,
.freshness-chip--error { color: var(--color-incident-crimson); background: rgba(239, 68, 68, 0.12); }
</style>
