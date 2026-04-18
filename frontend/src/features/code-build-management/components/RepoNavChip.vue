<script setup lang="ts">
interface Props {
  label: string;
  href: string;
}

defineProps<Props>();
</script>

<template>
  <a class="repo-chip" :href="href" rel="noopener noreferrer" target="_blank">
    <span>{{ label }}</span>
    <span aria-hidden="true">↗</span>
  </a>
</template>

<style scoped>
.repo-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 9px;
  border-radius: 999px;
  background: rgba(137, 206, 255, 0.08);
  color: var(--color-secondary);
  text-decoration: none;
  font-size: 0.82rem;
  font-weight: 600;
}
</style>

