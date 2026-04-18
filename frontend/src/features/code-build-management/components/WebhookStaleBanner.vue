<script setup lang="ts">
defineEmits<{
  refresh: [];
}>();
</script>

<template>
  <div class="stale-banner">
    <span>Webhook sync looks stale for a non-terminal entity.</span>
    <button class="btn-machined" type="button" @click="$emit('refresh')">Refresh</button>
  </div>
</template>

<style scoped>
.stale-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  background: rgba(255, 180, 171, 0.12);
  border: 1px solid rgba(255, 180, 171, 0.2);
  font-size: 0.82rem;
}
</style>

