<script setup lang="ts">
defineProps<{
  message: string;
}>();

const emit = defineEmits<{
  retry: [];
}>();
</script>

<template>
  <div class="error-panel">
    <p>{{ message }}</p>
    <button class="btn-machined" @click="emit('retry')">Retry</button>
  </div>
</template>

<style scoped>
.error-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 24px;
  color: var(--color-critical, #f44336);
  font-size: 14px;
}
</style>
