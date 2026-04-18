<script setup lang="ts">
defineProps<{
  version: string;
  buildArtifactId?: string;
  jenkinsJobName?: string;
}>();
</script>

<template>
  <span
    class="version-pill"
    :title="[buildArtifactId && `Artifact: ${buildArtifactId}`, jenkinsJobName && `Job: ${jenkinsJobName}`].filter(Boolean).join('\n') || undefined"
  >
    {{ version }}
  </span>
</template>

<style scoped>
.version-pill {
  display: inline-block;
  padding: 1px 6px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  font-family: var(--font-tech);
  font-size: 0.75rem;
  color: var(--color-on-surface);
  white-space: nowrap;
}
</style>
