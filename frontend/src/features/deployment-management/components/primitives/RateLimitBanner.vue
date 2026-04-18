<script setup lang="ts">
defineProps<{ message?: string }>();
</script>

<template>
  <div class="rate-limit-banner">
    {{ message ?? 'Rate limit exceeded — please wait before retrying.' }}
  </div>
</template>

<style scoped>
.rate-limit-banner {
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  background: rgba(245, 158, 11, 0.1);
  color: var(--color-approval-amber);
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 500;
  text-align: center;
  position: sticky;
  top: 0;
  z-index: 5;
}
</style>
