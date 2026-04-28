<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import type { DocumentReview, SddDocumentContent, SddDocumentIndex } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';
import ReviewHistoryList from './ReviewHistoryList.vue';

const props = defineProps<{
  selectedDocument: SddDocumentContent | null;
  selectedDocumentId?: string | null;
  documents: SddDocumentIndex | null;
  reviews: ReadonlyArray<DocumentReview>;
  isLoading?: boolean;
}>();

const emit = defineEmits<{ review: [documentId: string, decision: string, comment?: string] }>();

const rejectReason = ref('');
const rejectTouched = ref(false);
const showRejectForm = ref(false);

const currentDocument = computed(() => {
  if (props.selectedDocument) return props.selectedDocument.document;
  if (!props.selectedDocumentId) return null;
  return props.documents?.stages.find(stage => stage.id === props.selectedDocumentId) ?? null;
});

const selectedVersion = computed(() => ({
  branch: currentDocument.value?.branchOrRef ?? 'not indexed',
  commitSha: props.selectedDocument?.commitSha ?? currentDocument.value?.latestCommitSha ?? null,
  blobSha: props.selectedDocument?.blobSha ?? currentDocument.value?.latestBlobSha ?? null,
}));

const currentDocumentReviews = computed(() => {
  const documentId = currentDocument.value?.id;
  if (!documentId) return [];
  return props.reviews.filter(review => review.documentId === documentId);
});

const canReview = computed(() => Boolean(currentDocument.value?.id && !currentDocument.value.missing));
const trimmedRejectReason = computed(() => rejectReason.value.trim());
const rejectReasonError = computed(() => rejectTouched.value && !trimmedRejectReason.value ? 'Rejection reason is required.' : '');

watch(() => currentDocument.value?.id, () => {
  rejectReason.value = '';
  rejectTouched.value = false;
  showRejectForm.value = false;
});

function approveCurrentVersion() {
  const doc = currentDocument.value;
  if (doc?.id && !doc.missing) emit('review', doc.id, 'APPROVED');
}

function openRejectForm() {
  if (canReview.value) showRejectForm.value = true;
}

function cancelReject() {
  rejectReason.value = '';
  rejectTouched.value = false;
  showRejectForm.value = false;
}

function rejectCurrentVersion() {
  rejectTouched.value = true;
  const doc = currentDocument.value;
  if (!doc?.id || doc.missing || !trimmedRejectReason.value) return;
  emit('review', doc.id, 'REJECTED', trimmedRejectReason.value);
  cancelReject();
}

function shortSha(value: string | null | undefined) {
  return value ? value.slice(0, 10) : 'not indexed';
}
</script>

<template>
  <RequirementCard title="Business Review" :is-loading="isLoading">
    <div class="review-panel">
      <div v-if="currentDocument" class="review-target">
        <span class="target-label">Selected Document</span>
        <strong>{{ currentDocument.title }}</strong>
        <span class="target-meta">{{ currentDocument.path ?? currentDocument.stageLabel }}</span>
        <div class="version-box">
          <span class="target-label">Selected Version</span>
          <div class="version-grid">
            <div>
              <small>Branch</small>
              <strong>{{ selectedVersion.branch }}</strong>
            </div>
            <div>
              <small>Commit</small>
              <strong>{{ shortSha(selectedVersion.commitSha) }}</strong>
            </div>
            <div>
              <small>Blob</small>
              <strong>{{ shortSha(selectedVersion.blobSha) }}</strong>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="review-target review-target--empty">
        Select an SDD document to choose the version for review.
      </div>

      <div class="review-actions">
        <button class="action-btn" type="button" :disabled="!canReview" @click="approveCurrentVersion">
          Approve Selected Version
        </button>
        <button class="action-btn action-btn--reject" type="button" :disabled="!canReview" @click="openRejectForm">
          Reject with Reason
        </button>
      </div>

      <div v-if="showRejectForm" class="reject-form">
        <label for="reject-reason">Reason for rejection</label>
        <textarea
          id="reject-reason"
          v-model="rejectReason"
          rows="3"
          placeholder="Tell the delivery team what needs to change before this document can be approved."
          @blur="rejectTouched = true"
        />
        <span v-if="rejectReasonError" class="field-error">{{ rejectReasonError }}</span>
        <div class="reject-actions">
          <button class="action-btn action-btn--reject" type="button" :disabled="!trimmedRejectReason" @click="rejectCurrentVersion">
            Confirm Reject
          </button>
          <button class="action-btn action-btn--ghost" type="button" @click="cancelReject">
            Cancel
          </button>
        </div>
      </div>

      <ReviewHistoryList :reviews="currentDocumentReviews" empty-label="No reviews for selected document" />
    </div>
  </RequirementCard>
</template>

<style scoped>
.review-panel { display: flex; flex-direction: column; gap: 10px; }
.review-target {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
}
.review-target--empty {
  color: var(--color-on-surface-variant);
  font-size: 0.75rem;
}
.target-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.review-target strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-on-surface);
  font-size: 0.8125rem;
}
.target-meta {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.625rem;
}
.version-box {
  display: flex;
  flex-direction: column;
  gap: 7px;
  margin-top: 6px;
  padding-top: 9px;
  border-top: var(--border-ghost);
}
.version-grid {
  display: grid;
  grid-template-columns: 1.25fr 0.9fr 0.9fr;
  gap: 8px;
}
.version-grid div {
  min-width: 0;
  padding: 8px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container);
}
.version-grid small,
.version-grid strong {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.version-grid small {
  color: var(--color-on-surface-variant);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.version-grid strong {
  margin-top: 4px;
  color: var(--color-on-surface);
  font-family: var(--font-tech);
  font-size: 0.6875rem;
}
.review-actions,
.reject-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.action-btn {
  align-self: flex-start;
  border: 1px solid var(--color-secondary);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-secondary);
  padding: 6px 10px;
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  cursor: pointer;
}
.action-btn:disabled { opacity: 0.45; cursor: not-allowed; }
.action-btn:not(:disabled):hover { background: var(--color-secondary); color: var(--color-on-secondary-container); }
.action-btn--reject {
  border-color: var(--color-incident-crimson);
  color: var(--color-incident-crimson);
}
.action-btn--reject:not(:disabled):hover {
  background: var(--color-incident-crimson);
  color: var(--color-on-primary);
}
.action-btn--ghost {
  border-color: var(--color-on-surface-variant);
  color: var(--color-on-surface-variant);
}
.reject-form {
  display: flex;
  flex-direction: column;
  gap: 7px;
  padding: 10px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
}
.reject-form label {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.reject-form textarea {
  width: 100%;
  box-sizing: border-box;
  resize: vertical;
  min-height: 76px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container);
  color: var(--color-on-surface);
  padding: 8px 10px;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  line-height: 1.45;
}
.reject-form textarea:focus {
  outline: none;
  border-color: var(--color-secondary);
}
.field-error {
  color: var(--color-incident-crimson);
  font-size: 0.6875rem;
}
@media (max-width: 720px) {
  .version-grid { grid-template-columns: 1fr; }
}
</style>
