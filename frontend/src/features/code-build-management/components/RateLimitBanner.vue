<script setup lang="ts">
import { formatTimestamp } from '../utils';

interface Props {
  resetAt: string;
}

const props = defineProps<Props>();
</script>

<template>
  <div class="rate-limit-banner">
    GitHub rerun rate limit is active until {{ formatTimestamp(props.resetAt) }}.
  </div>
</template>

<style scoped>
.rate-limit-banner {
  padding: 10px 12px;
  border-radius: var(--radius-md);
  background: rgba(245, 158, 11, 0.12);
  color: var(--cb-severity-minor);
  border: 1px solid rgba(245, 158, 11, 0.24);
  font-size: 0.82rem;
}
</style>

