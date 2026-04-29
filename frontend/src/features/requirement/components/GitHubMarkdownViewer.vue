<script setup lang="ts">
import type { SddDocumentContent } from '../types/requirement';

defineProps<{
  content: SddDocumentContent | null;
  isLoading?: boolean;
  error?: string | null;
}>();
</script>

<template>
  <div class="markdown-viewer">
    <div v-if="isLoading" class="viewer-state">
      <div class="loading-spinner"></div>
      <span>Loading Markdown...</span>
    </div>
    <div v-else-if="error" class="viewer-state viewer-state--error">
      {{ error }}
    </div>
    <div v-else-if="!content" class="empty">Select a document to view Markdown</div>
    <template v-else>
      <div class="viewer-meta">
        <strong>{{ content.document.title }}</strong>
        <span>{{ content.document.stageLabel }}</span>
        <span>{{ content.commitSha }}</span>
        <span>{{ content.blobSha }}</span>
        <a :href="content.githubUrl" target="_blank" rel="noreferrer">GitHub</a>
      </div>
      <pre>{{ content.markdown }}</pre>
    </template>
  </div>
</template>

<style scoped>
.markdown-viewer {
  min-height: 180px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  overflow: hidden;
}
.viewer-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
  padding: 8px 10px;
  border-bottom: var(--border-ghost);
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  color: var(--color-on-surface-variant);
}
.viewer-meta strong {
  font-family: var(--font-ui);
  color: var(--color-on-surface);
}
.viewer-meta a { color: var(--color-secondary); text-decoration: none; }
pre {
  margin: 0;
  max-height: 320px;
  overflow: auto;
  padding: 12px;
  white-space: pre-wrap;
  font-family: var(--font-tech);
  font-size: 0.6875rem;
  line-height: 1.55;
  color: var(--color-on-surface);
}
.empty,
.viewer-state {
  display: flex;
  min-height: 180px;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 30px 12px;
  text-align: center;
  color: var(--color-on-surface-variant);
  font-size: 0.75rem;
}
.viewer-state--error { color: var(--color-incident-crimson); }
.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-surface-container-highest);
  border-top-color: var(--color-secondary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
