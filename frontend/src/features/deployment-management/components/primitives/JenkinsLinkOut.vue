<script setup lang="ts">
defineProps<{ url: string; label?: string }>();
</script>

<template>
  <a :href="url" target="_blank" rel="noopener noreferrer" class="jenkins-link">
    <svg class="crown" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M2 20h20l-3-12-5 5-4-8-4 8-5-5z" />
    </svg>
    <span v-if="label" class="text">{{ label }}</span>
    <svg class="chevron" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M7 17l9.2-9.2M17 17V7H7" />
    </svg>
  </a>
</template>

<style scoped>
.jenkins-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--color-secondary);
  text-decoration: none;
  font-family: var(--font-tech);
  font-size: 0.75rem;
}
.jenkins-link:hover { text-decoration: underline; }
.crown { opacity: 0.7; }
.chevron { opacity: 0.5; }
</style>
