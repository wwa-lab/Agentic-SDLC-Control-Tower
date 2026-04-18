<script setup lang="ts">
defineProps<{
  message: string;
  actionLabel?: string;
}>();

const emit = defineEmits<{
  action: [];
}>();
</script>

<template>
  <div class="empty-state">
    <p>{{ message }}</p>
    <button v-if="actionLabel" class="btn-machined" @click="emit('action')">{{ actionLabel }}</button>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 24px;
  color: var(--color-on-surface-variant, #999);
  font-size: 14px;
}
</style>
