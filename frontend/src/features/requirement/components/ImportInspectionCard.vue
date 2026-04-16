<script setup lang="ts">
import { computed } from 'vue';
import type { ImportInspection, ImportInspectionFile, ImportInspectionStatus } from '../types/requirement';

interface Props {
  inspection: ImportInspection;
}

const props = defineProps<Props>();

const statusOrder: Record<ImportInspectionStatus, number> = {
  PARSED: 0,
  MANUAL_REVIEW: 1,
  SKIPPED: 2,
};

const sortedFiles = computed(() =>
  [...props.inspection.files].sort((left, right) => {
    const order = statusOrder[left.processingStatus] - statusOrder[right.processingStatus];
    if (order !== 0) {
      return order;
    }
    return left.fileName.localeCompare(right.fileName);
  }),
);

function formatStatus(status: ImportInspectionStatus): string {
  switch (status) {
    case 'PARSED':
      return 'Parsed';
    case 'MANUAL_REVIEW':
      return 'Manual Review';
    case 'SKIPPED':
      return 'Skipped';
  }
}

function statusClass(status: ImportInspectionStatus): string {
  switch (status) {
    case 'PARSED':
      return 'status--parsed';
    case 'MANUAL_REVIEW':
      return 'status--manual';
    case 'SKIPPED':
      return 'status--skipped';
  }
}

function typeLabel(file: ImportInspectionFile): string {
  return file.fileType.replaceAll('_', ' ');
}
</script>

<template>
  <section class="inspection-card">
    <div class="inspection-header">
      <div>
        <h4 class="inspection-title">Source Inspection</h4>
        <p class="inspection-subtitle">
          {{ inspection.sourceKind === 'ZIP' ? 'ZIP package' : 'Uploaded file' }}:
          {{ inspection.sourceFileName }}
        </p>
      </div>
      <div class="inspection-stats">
        <span class="inspection-stat status--parsed">Parsed {{ inspection.parsedFiles }}</span>
        <span class="inspection-stat status--manual">Manual {{ inspection.manualReviewFiles }}</span>
        <span class="inspection-stat status--skipped">Skipped {{ inspection.skippedFiles }}</span>
      </div>
    </div>

    <div class="inspection-list">
      <article
        v-for="file in sortedFiles"
        :key="`${file.processingStatus}:${file.fileName}`"
        class="inspection-file"
      >
        <div class="file-header">
          <span class="file-name">{{ file.fileName }}</span>
          <span class="file-status" :class="statusClass(file.processingStatus)">
            {{ formatStatus(file.processingStatus) }}
          </span>
        </div>
        <div class="file-meta">
          <span>{{ typeLabel(file) }}</span>
          <span v-if="file.extractedCharacters != null">{{ file.extractedCharacters }} chars</span>
        </div>
        <p class="file-summary">{{ file.summary }}</p>
        <pre v-if="file.preview" class="file-preview">{{ file.preview }}</pre>
      </article>
    </div>
  </section>
</template>

<style scoped>
.inspection-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  border-radius: var(--radius-sm);
  border: var(--border-ghost);
  background: color-mix(in srgb, var(--color-surface-container-low) 86%, transparent);
}

.inspection-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.inspection-title {
  margin: 0;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-on-surface);
}

.inspection-subtitle {
  margin: 4px 0 0;
  font-family: var(--font-ui);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
}

.inspection-stats {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.inspection-stat,
.file-status {
  display: inline-flex;
  align-items: center;
  padding: 3px 8px;
  border-radius: 999px;
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.status--parsed {
  background: rgba(34, 197, 94, 0.14);
  color: #16a34a;
}

.status--manual {
  background: rgba(245, 158, 11, 0.14);
  color: #d97706;
}

.status--skipped {
  background: rgba(148, 163, 184, 0.18);
  color: var(--color-on-surface-variant);
}

.inspection-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.inspection-file {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: color-mix(in srgb, var(--color-surface-container-high) 82%, transparent);
  border: 1px solid color-mix(in srgb, var(--color-outline-variant) 40%, transparent);
}

.file-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.file-name {
  font-family: var(--font-tech);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
  word-break: break-word;
}

.file-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.file-summary {
  margin: 0;
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  line-height: 1.45;
  color: var(--color-on-surface);
}

.file-preview {
  margin: 0;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  background: rgba(9, 17, 30, 0.55);
  color: rgba(237, 242, 247, 0.9);
  font-family: var(--font-tech);
  font-size: 0.625rem;
  line-height: 1.45;
  white-space: pre-wrap;
}

@media (max-width: 720px) {
  .inspection-header,
  .file-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .inspection-stats {
    justify-content: flex-start;
  }
}
</style>
