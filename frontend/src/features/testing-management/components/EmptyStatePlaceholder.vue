<script setup lang="ts">
interface Props {
  title: string;
  message: string;
}

defineProps<Props>();
</script>

<template>
  <div class="tm-empty section-low">
    <p class="text-label">{{ title }}</p>
    <p class="text-body-sm">{{ message }}</p>
  </div>
</template>

<style scoped>
.tm-empty {
  display: grid;
  gap: 8px;
  padding: 16px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
}
</style>
