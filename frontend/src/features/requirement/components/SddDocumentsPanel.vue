<script setup lang="ts">
import type { SddDocumentContent, SddDocumentIndex } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';
import SddDocumentStageRow from './SddDocumentStageRow.vue';
import GitHubMarkdownViewer from './GitHubMarkdownViewer.vue';

defineProps<{
  documents: SddDocumentIndex | null;
  selectedDocumentId?: string | null;
  selectedDocument: SddDocumentContent | null;
  documentLoading?: boolean;
  documentError?: string | null;
  isLoading?: boolean;
  error?: string | null;
}>();

defineEmits<{ openDocument: [documentId: string]; retry: [] }>();
</script>

<template>
  <RequirementCard title="GitHub SDD Documents" :is-loading="isLoading" :error="error" :full-width="true">
    <div class="documents-panel">
      <div v-if="error" class="section-error">
        <span>{{ error }}</span>
        <button type="button" @click="$emit('retry')">Retry</button>
      </div>
      <template v-else-if="documents">
        <div v-if="documents.workspace" class="workspace-context">
          <div class="context-item">
            <span>Selected Application</span>
            <strong>{{ documents.workspace.applicationName }}</strong>
            <small>{{ documents.workspace.applicationId }}</small>
          </div>
          <div class="context-item">
            <span>Selected SNOW Group</span>
            <strong>{{ documents.workspace.snowGroup }}</strong>
            <small>Platform scope</small>
          </div>
          <div class="context-item">
            <span>Source Repo</span>
            <strong>{{ documents.workspace.sourceRepoFullName }}</strong>
            <small>Baseline {{ documents.workspace.baseBranch }}</small>
          </div>
          <div class="context-item context-item--primary">
            <span>Selected SDD Branch</span>
            <strong>{{ documents.workspace.sddRepoFullName }}</strong>
            <small>{{ documents.workspace.workingBranch }} from {{ documents.workspace.baseBranch }}</small>
          </div>
          <div class="context-item">
            <span>Knowledge Base</span>
            <strong>{{ documents.workspace.kbRepoFullName }}</strong>
            <small>{{ documents.workspace.kbPreviewBranch || documents.workspace.kbMainBranch }}</small>
          </div>
        </div>
        <div class="stage-list">
          <SddDocumentStageRow
            v-for="stage in documents.stages"
            :key="stage.sddType"
            :stage="stage"
            :selected="stage.id === selectedDocumentId"
            :loading="stage.id === selectedDocumentId && documentLoading"
            @open="$emit('openDocument', $event)"
          />
        </div>
        <GitHubMarkdownViewer
          :content="selectedDocument"
          :is-loading="documentLoading"
          :error="documentError"
        />
      </template>
      <div v-else class="empty">No document index available</div>
    </div>
  </RequirementCard>
</template>

<style scoped>
.documents-panel {
  display: grid;
  grid-template-columns: minmax(340px, 0.9fr) minmax(380px, 1.1fr);
  gap: 12px;
  align-items: stretch;
}
.workspace-context {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 8px;
}
.context-item {
  min-width: 0;
  padding: 9px 10px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
}
.context-item--primary { border-color: rgba(137, 206, 255, 0.32); }
.context-item span,
.context-item small {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.625rem;
}
.context-item strong {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 3px 0;
  color: var(--color-on-surface);
  font-family: var(--font-tech);
  font-size: 0.75rem;
}
.stage-list { display: flex; flex-direction: column; gap: 8px; min-width: 0; }
.empty { padding: 20px 0; text-align: center; color: var(--color-on-surface-variant); font-size: 0.75rem; }
.section-error {
  grid-column: 1 / -1;
  display: flex;
  justify-content: space-between;
  color: var(--color-incident-crimson);
  font-size: 0.75rem;
}
.section-error button { border: 1px solid var(--color-incident-crimson); border-radius: var(--radius-sm); background: transparent; color: var(--color-incident-crimson); cursor: pointer; }
@media (max-width: 1360px) {
  .documents-panel { grid-template-columns: 1fr; }
  .workspace-context { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 980px) {
  .workspace-context { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 720px) {
  .workspace-context { grid-template-columns: 1fr; }
}
</style>
