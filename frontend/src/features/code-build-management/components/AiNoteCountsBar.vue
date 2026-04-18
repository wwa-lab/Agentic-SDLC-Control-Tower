<script setup lang="ts">
import type { AiPrReviewNoteCounts } from '../types';

interface Props {
  counts: AiPrReviewNoteCounts;
}

defineProps<Props>();
</script>

<template>
  <div class="counts-bar">
    <span class="counts-bar__item counts-bar__item--blocker">B {{ counts.blocker }}</span>
    <span class="counts-bar__item counts-bar__item--major">M {{ counts.major }}</span>
    <span class="counts-bar__item counts-bar__item--minor">m {{ counts.minor }}</span>
    <span class="counts-bar__item counts-bar__item--info">i {{ counts.info }}</span>
  </div>
</template>

<style scoped>
.counts-bar {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.counts-bar__item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 38px;
  padding: 5px 8px;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
}

.counts-bar__item--blocker {
  background: rgba(255, 180, 171, 0.18);
  color: var(--cb-severity-blocker);
}

.counts-bar__item--major {
  background: rgba(255, 180, 171, 0.12);
  color: var(--cb-severity-major);
}

.counts-bar__item--minor {
  background: rgba(245, 158, 11, 0.12);
  color: var(--cb-severity-minor);
}

.counts-bar__item--info {
  background: rgba(137, 206, 255, 0.12);
  color: var(--cb-severity-info);
}
</style>

