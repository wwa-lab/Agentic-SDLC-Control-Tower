<script setup lang="ts">
defineProps<{ text: string }>();
</script>

<template>
  <div class="log-excerpt">
    <div class="watermark">REDACTED</div>
    <pre class="content">{{ text }}</pre>
  </div>
</template>

<style scoped>
.log-excerpt {
  position: relative;
  padding: 12px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-lowest);
  border: var(--border-ghost);
  overflow: auto;
  max-height: 240px;
}
.watermark {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(-30deg);
  font-size: 3rem;
  font-weight: 800;
  color: var(--color-on-surface-variant);
  opacity: 0.06;
  pointer-events: none;
  user-select: none;
}
.content {
  margin: 0;
  font-family: var(--font-tech);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
