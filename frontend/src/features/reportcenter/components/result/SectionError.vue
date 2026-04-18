<script setup lang="ts">
defineProps<{
  message: string;
}>();

defineEmits<{
  retry: [];
}>();
</script>

<template>
  <div class="section-error">
    <strong>Unable to load this section</strong>
    <p class="text-body-sm">{{ message }}</p>
    <button class="btn-machined" @click="$emit('retry')">Retry</button>
  </div>
</template>

<style scoped>
.section-error {
  display: grid;
  gap: 10px;
  padding: 18px;
  border: 1px solid color-mix(in srgb, var(--color-error) 24%, transparent);
  border-radius: 14px;
  background: color-mix(in srgb, var(--color-error) 10%, transparent);
}
</style>
