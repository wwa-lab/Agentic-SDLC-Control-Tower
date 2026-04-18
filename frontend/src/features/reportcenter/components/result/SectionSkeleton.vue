<script setup lang="ts">
defineProps<{
  height?: number;
}>();
</script>

<template>
  <div class="section-skeleton" :style="{ minHeight: `${height ?? 220}px` }" />
</template>

<style scoped>
.section-skeleton {
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  background: color-mix(in srgb, var(--color-surface-container-high) 90%, transparent);
}

.section-skeleton::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.12), transparent);
  animation: report-shimmer 1.4s linear infinite;
}

@keyframes report-shimmer {
  from {
    transform: translateX(-100%);
  }
  to {
    transform: translateX(100%);
  }
}
</style>
