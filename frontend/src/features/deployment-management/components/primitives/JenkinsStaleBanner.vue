<script setup lang="ts">
defineProps<{ lastIngestedAt: string | null }>();
</script>

<template>
  <div class="stale-banner">
    Jenkins data may be stale — last ingested
    {{ lastIngestedAt ? new Date(lastIngestedAt).toLocaleTimeString() : 'never' }}.
    Non-terminal state may not reflect current Jenkins status.
  </div>
</template>

<style scoped>
.stale-banner {
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  background: var(--color-incident-tint);
  color: var(--color-incident-crimson);
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 500;
}
</style>
