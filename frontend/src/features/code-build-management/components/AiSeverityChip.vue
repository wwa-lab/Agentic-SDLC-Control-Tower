<script setup lang="ts">
import type { AiNoteSeverity } from '../types';

interface Props {
  severity: AiNoteSeverity;
}

defineProps<Props>();
</script>

<template>
  <span class="ai-severity" :class="`ai-severity--${severity.toLowerCase()}`">{{ severity }}</span>
</template>

<style scoped>
.ai-severity {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
}

.ai-severity--blocker {
  background: rgba(255, 180, 171, 0.18);
  color: var(--cb-severity-blocker);
}

.ai-severity--major {
  background: rgba(255, 180, 171, 0.12);
  color: var(--cb-severity-major);
}

.ai-severity--minor {
  background: rgba(245, 158, 11, 0.12);
  color: var(--cb-severity-minor);
}

.ai-severity--info {
  background: rgba(137, 206, 255, 0.12);
  color: var(--cb-severity-info);
}
</style>

