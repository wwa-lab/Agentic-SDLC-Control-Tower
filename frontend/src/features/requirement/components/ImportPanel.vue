<script setup lang="ts">
import { computed } from 'vue';
import { useRequirementStore } from '../stores/requirementStore';
import ImportSourceTabs from './ImportSourceTabs.vue';
import ImportTextInput from './ImportTextInput.vue';
import ImportDropZone from './ImportDropZone.vue';
import NormalizationResultCard from './NormalizationResultCard.vue';
import BatchPreviewTable from './BatchPreviewTable.vue';
import BatchProgressBar from './BatchProgressBar.vue';
import type { ImportSourceType, RequirementDraft } from '../types/requirement';

const store = useRequirementStore();
const importState = computed(() => store.importState);

function handleClose() {
  store.closeImport();
}

function handleSourceSelect(source: ImportSourceType) {
  store.setImportSource(source);
}

function handleTextSubmit(text: string) {
  store.setRawInput(text);
  store.triggerNormalization();
}

function handleFilesSelected(files: File[]) {
  store.handleFileImport(files);
}

function handleConfirmDraft(draft: RequirementDraft) {
  store.confirmDraft(draft);
}

function handleDiscard() {
  store.discardDraft();
}

function handleBatchNormalize(indices: number[]) {
  store.normalizeSelected(indices);
}

function handleUpdateMapping(column: string, target: string) {
  store.updateColumnMapping(column, target);
}
</script>

<template>
  <Teleport to="body">
    <div v-if="importState.isOpen" class="import-overlay" @click.self="handleClose">
      <div class="import-panel">
        <div class="panel-header">
          <h2 class="panel-title">Import Requirement</h2>
          <button class="close-btn" @click="handleClose">&times;</button>
        </div>

        <div v-if="importState.error" class="panel-error">{{ importState.error }}</div>

        <!-- Source Selection Step -->
        <template v-if="importState.step === 'source'">
          <ImportSourceTabs
            :active-source="importState.sourceType"
            @select="handleSourceSelect"
          />

          <template v-if="importState.sourceType === 'paste' || importState.sourceType === 'email' || importState.sourceType === 'meeting'">
            <ImportTextInput @submit="handleTextSubmit" />
          </template>

          <template v-if="importState.sourceType === 'file'">
            <ImportDropZone @files-selected="handleFilesSelected" />
          </template>
        </template>

        <!-- Normalizing Step -->
        <template v-if="importState.step === 'normalizing' || importState.step === 'processing'">
          <div class="normalizing-state">
            <div class="normalizing-spinner"></div>
            <span>
              {{ importState.step === 'processing'
                ? (importState.importMessage || 'Uploading files to the knowledge base and waiting for indexing...')
                : 'AI is analyzing your input...' }}
            </span>
            <div v-if="importState.step === 'processing'" class="import-progress-card">
              <div class="import-progress-row">
                <span>Knowledge Base</span>
                <strong>{{ importState.kbName }}</strong>
              </div>
              <div v-if="importState.importId" class="import-progress-row">
                <span>Import ID</span>
                <strong>{{ importState.importId }}</strong>
              </div>
              <div v-if="importState.importDatasetId" class="import-progress-row">
                <span>Dataset ID</span>
                <strong>{{ importState.importDatasetId }}</strong>
              </div>
              <div class="import-progress-row">
                <span>Files Accepted</span>
                <strong>{{ importState.importSuccessCount }}</strong>
              </div>
              <div class="import-progress-row">
                <span>Files Failed</span>
                <strong>{{ importState.importFailureCount }}</strong>
              </div>
              <div v-if="importState.unsupportedFileTypes.length > 0" class="import-chip-group">
                <span class="chip-label">Unsupported</span>
                <span
                  v-for="fileType in importState.unsupportedFileTypes"
                  :key="fileType"
                  class="import-chip import-chip--warning"
                >
                  {{ fileType }}
                </span>
              </div>
              <div v-if="importState.importFiles.length > 0" class="import-file-list">
                <div
                  v-for="file in importState.importFiles"
                  :key="`${file.displayName}-${file.providerDocumentId ?? file.sourceFileName}`"
                  class="import-file-row"
                >
                  <div class="import-file-meta">
                    <span class="import-file-name">{{ file.displayName }}</span>
                    <span class="import-file-kind">{{ file.sourceKind }} · {{ file.fileExtension || 'unknown' }}</span>
                  </div>
                  <span class="import-file-status" :class="`status--${file.providerStatus.toLowerCase()}`">
                    {{ file.providerStatus }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- Review Step (Single) -->
        <template v-if="importState.step === 'review' && importState.draft">
          <NormalizationResultCard
            :draft="importState.draft"
            @confirm="handleConfirmDraft"
            @discard="handleDiscard"
          />
        </template>

        <!-- Batch Preview Step -->
        <template v-if="importState.step === 'batch-preview'">
          <BatchPreviewTable
            :rows="importState.batchRows"
            :column-mapping="importState.columnMapping"
            @normalize-selected="handleBatchNormalize"
            @update-mapping="handleUpdateMapping"
          />
        </template>

        <!-- Batch Normalizing Step -->
        <template v-if="importState.step === 'batch-normalizing'">
          <BatchProgressBar
            :current="importState.batchProgress"
            :total="importState.batchTotal"
          />
        </template>

        <!-- Batch Review Step -->
        <template v-if="importState.step === 'batch-review'">
          <div class="batch-results">
            <h3 class="batch-results-title">{{ importState.batchDrafts.length }} drafts ready</h3>
            <div
              v-for="(draft, idx) in importState.batchDrafts"
              :key="idx"
              class="batch-draft-row"
            >
              <span class="draft-title">{{ draft.title }}</span>
              <span class="draft-priority" :class="`pri--${draft.priority.toLowerCase()}`">{{ draft.priority }}</span>
              <span class="draft-category">{{ draft.category }}</span>
            </div>
            <div class="batch-actions">
              <button class="btn-discard" @click="handleDiscard">Discard All</button>
              <button class="btn-confirm-all" @click="store.confirmAllDrafts()">
                Confirm All ({{ importState.batchDrafts.length }})
              </button>
            </div>
          </div>
        </template>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.import-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(6, 14, 32, 0.7);
  backdrop-filter: blur(8px);
}

.import-panel {
  width: 840px;
  max-height: 85vh;
  overflow-y: auto;
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-ambient);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-title {
  font-family: var(--font-ui);
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-on-surface);
  margin: 0;
}

.panel-error {
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: rgba(239, 68, 68, 0.08);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: var(--color-incident-crimson);
  font-family: var(--font-ui);
  font-size: 0.75rem;
}

.close-btn {
  background: none;
  border: none;
  color: var(--color-on-surface-variant);
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
  transition: color 0.2s ease;
}

.close-btn:hover { color: var(--color-on-surface); }

.normalizing-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 0;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
}

.normalizing-spinner {
  width: 32px;
  height: 32px;
  border: 2px solid var(--color-surface-container-highest);
  border-top-color: var(--color-secondary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.import-progress-card {
  width: min(100%, 560px);
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 16px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
}

.import-progress-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
}

.import-progress-row span {
  color: var(--color-on-surface-variant);
}

.import-chip-group {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.chip-label {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.import-chip {
  padding: 2px 8px;
  border-radius: 999px;
  font-family: var(--font-ui);
  font-size: 0.625rem;
  background: var(--color-surface-container-highest);
  color: var(--color-on-surface);
}

.import-chip--warning {
  background: rgba(245, 158, 11, 0.12);
  color: #d97706;
}

.import-file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.import-file-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding-top: 8px;
  border-top: 1px solid var(--border-separator);
}

.import-file-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.import-file-name {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
}

.import-file-kind {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
}

.import-file-status {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-secondary);
}

.status--error,
.status--failed,
.status--unsupported {
  color: var(--color-incident-crimson);
}

.status--completed {
  color: #0f9f67;
}

/* Batch Review */
.batch-results {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.batch-results-title {
  font-family: var(--font-ui);
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-on-surface);
  margin: 0;
}

.batch-draft-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  background: var(--color-surface-container-low);
  border-radius: var(--radius-sm);
}

.draft-title {
  flex: 1;
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.draft-priority {
  font-family: var(--font-tech);
  font-size: 0.5rem;
  padding: 1px 4px;
  border-radius: 2px;
  text-transform: uppercase;
}

.pri--critical { color: #fff; background: var(--color-incident-crimson); }
.pri--high { color: var(--color-approval-amber); background: rgba(245, 158, 11, 0.12); }
.pri--medium { color: var(--color-secondary); background: var(--color-secondary-tint); }
.pri--low { color: var(--color-on-surface-variant); background: rgba(148, 163, 184, 0.1); }

.draft-category {
  font-family: var(--font-ui);
  font-size: 0.5rem;
  text-transform: uppercase;
  color: var(--color-on-surface-variant);
}

.batch-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 8px;
}

.btn-discard {
  background: none;
  border: var(--border-ghost);
  color: var(--color-on-surface-variant);
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  cursor: pointer;
}

.btn-confirm-all {
  background: linear-gradient(135deg, var(--color-secondary), #a78bfa);
  border: none;
  color: #0b1326;
  padding: 8px 20px;
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  cursor: pointer;
}

.btn-confirm-all:hover { opacity: 0.85; }

@keyframes spin { to { transform: rotate(360deg); } }
</style>
