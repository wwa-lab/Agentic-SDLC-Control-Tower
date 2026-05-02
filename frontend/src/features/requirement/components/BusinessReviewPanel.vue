<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import type { DocumentQualityFinding, DocumentReview, SddDocumentContent, SddDocumentIndex } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';
import ReviewHistoryList from './ReviewHistoryList.vue';
import DocumentQualityBadge from './DocumentQualityBadge.vue';

const props = defineProps<{
  selectedDocument: SddDocumentContent | null;
  selectedDocumentId?: string | null;
  documents: SddDocumentIndex | null;
  reviews: ReadonlyArray<DocumentReview>;
  isLoading?: boolean;
}>();

const emit = defineEmits<{
  review: [documentId: string, decision: string, comment?: string];
  runQualityGate: [documentId: string];
}>();

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
const qualityGate = computed(() => currentDocument.value?.qualityGate ?? null);
const canApprove = computed(() => canReview.value && Boolean(qualityGate.value?.passed && !qualityGate.value.stale));
const trimmedRejectReason = computed(() => rejectReason.value.trim());
const rejectReasonError = computed(() => rejectTouched.value && !trimmedRejectReason.value ? 'Rejection reason is required.' : '');
const reviewDecisionState = computed(() => {
  if (!currentDocument.value) return 'empty';
  if (!canReview.value) return 'blocked';
  if (!qualityGate.value) return 'gate-needed';
  if (qualityGate.value.stale) return 'gate-stale';
  if (!qualityGate.value.passed) return 'gate-failed';
  return 'ready';
});
const reviewDecisionTitle = computed(() => {
  switch (reviewDecisionState.value) {
    case 'ready':
      return 'Ready for approval';
    case 'gate-failed':
      return 'Quality gate needs changes';
    case 'gate-stale':
      return 'Quality gate is stale';
    case 'gate-needed':
      return 'Run quality gate';
    case 'blocked':
      return 'Document is not reviewable';
    default:
      return 'Select a document';
  }
});
const reviewDecisionHint = computed(() => {
  switch (reviewDecisionState.value) {
    case 'ready':
      return 'Approve this version, or reject it with a reason if the content is not acceptable.';
    case 'gate-failed':
      return 'Reject with a reason or ask the delivery team to fix the findings before approval.';
    case 'gate-stale':
      return 'Run the gate again before approving the current document version.';
    case 'gate-needed':
      return 'Run the gate before making the approval decision.';
    case 'blocked':
      return 'A missing document cannot be reviewed.';
    default:
      return 'Choose an SDD document to review.';
  }
});
const markdownPreview = computed(() => {
  const raw = props.selectedDocument?.markdown?.trim();
  if (!raw) return null;
  const lines = raw.split(/\r?\n/);
  const previewLines = lines.slice(0, 34);
  return {
    text: previewLines.join('\n'),
    truncated: lines.length > previewLines.length,
  };
});

watch(() => currentDocument.value?.id, () => {
  rejectReason.value = '';
  rejectTouched.value = false;
  showRejectForm.value = false;
});

function approveCurrentVersion() {
  const doc = currentDocument.value;
  if (doc?.id && !doc.missing && canApprove.value) emit('review', doc.id, 'APPROVED');
}

function runGateForCurrentVersion() {
  const doc = currentDocument.value;
  if (doc?.id && !doc.missing) emit('runQualityGate', doc.id);
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

function findingText(finding: string | DocumentQualityFinding) {
  if (typeof finding === 'string') return finding;
  return `${finding.severity} · ${finding.section}: ${finding.message}`;
}
</script>

<template>
  <RequirementCard title="Business Review" :is-loading="isLoading">
    <div class="review-panel">
      <div class="review-decision" :class="`review-decision--${reviewDecisionState}`">
        <span class="target-label">Decision State</span>
        <strong>{{ reviewDecisionTitle }}</strong>
        <p>{{ reviewDecisionHint }}</p>
      </div>

      <div v-if="currentDocument" class="review-target">
        <span class="target-label">Selected Document</span>
        <strong>{{ currentDocument.title }}</strong>
        <span class="target-meta">{{ currentDocument.path ?? currentDocument.stageLabel }}</span>
        <div v-if="qualityGate" class="quality-gate" :class="{ 'quality-gate--blocked': !qualityGate.passed }">
          <div class="quality-gate-head">
            <span class="target-label">Document Quality Gate</span>
            <DocumentQualityBadge :gate="qualityGate" />
          </div>
          <p>{{ qualityGate.summary }}</p>
          <ul v-if="qualityGate.findings.length">
            <li v-for="(finding, index) in qualityGate.findings" :key="index">{{ findingText(finding) }}</li>
          </ul>
        </div>
        <div v-else class="quality-gate quality-gate--blocked">
          <div class="quality-gate-head">
            <span class="target-label">Document Quality Gate</span>
            <button class="gate-btn" type="button" :disabled="!canReview" @click="runGateForCurrentVersion">
              Run Gate
            </button>
          </div>
          <p>Run the document quality gate before approving this version.</p>
        </div>
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

      <div v-if="markdownPreview" class="review-preview">
        <div class="preview-head">
          <span class="target-label">Document Preview</span>
          <a
            v-if="selectedDocument?.githubUrl"
            :href="selectedDocument.githubUrl"
            target="_blank"
            rel="noreferrer"
          >
            GitHub
          </a>
        </div>
        <pre>{{ markdownPreview.text }}</pre>
        <small v-if="markdownPreview.truncated">Preview truncated. Open GitHub SDD Documents for the full Markdown.</small>
      </div>

      <div class="review-actions">
        <button class="action-btn" type="button" :disabled="!canApprove" @click="approveCurrentVersion">
          Approve Selected Version
        </button>
        <button class="action-btn action-btn--ghost" type="button" :disabled="!canReview" @click="runGateForCurrentVersion">
          Run Quality Gate
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
.review-decision {
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 10px;
  border: 1px solid rgba(137, 206, 255, 0.24);
  border-radius: var(--radius-sm);
  background: rgba(137, 206, 255, 0.07);
}
.review-decision--ready {
  border-color: rgba(125, 211, 166, 0.36);
  background: rgba(125, 211, 166, 0.09);
}
.review-decision--gate-failed,
.review-decision--blocked {
  border-color: rgba(239, 68, 68, 0.32);
  background: rgba(239, 68, 68, 0.08);
}
.review-decision--gate-stale,
.review-decision--gate-needed {
  border-color: rgba(255, 202, 40, 0.36);
  background: rgba(255, 202, 40, 0.08);
}
.review-decision strong {
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.875rem;
}
.review-decision p {
  margin: 0;
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.75rem;
  line-height: 1.45;
}
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
.quality-gate {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 6px;
  padding: 9px;
  border: 1px solid rgba(137, 206, 255, 0.22);
  border-radius: var(--radius-sm);
  background: rgba(137, 206, 255, 0.08);
}
.quality-gate--blocked {
  border-color: rgba(239, 68, 68, 0.28);
  background: rgba(239, 68, 68, 0.08);
}
.quality-gate-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.quality-gate p {
  margin: 0;
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.75rem;
  line-height: 1.4;
}
.quality-gate ul {
  display: flex;
  flex-direction: column;
  gap: 3px;
  margin: 0;
  padding-left: 16px;
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  line-height: 1.35;
}
.gate-btn {
  border: 1px solid var(--color-secondary);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-secondary);
  padding: 4px 7px;
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  cursor: pointer;
}
.gate-btn:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}
.gate-btn:not(:disabled):hover {
  background: var(--color-secondary);
  color: var(--color-on-secondary-container);
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
.review-preview {
  display: flex;
  flex-direction: column;
  gap: 7px;
  padding: 10px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
}
.preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.preview-head a {
  color: var(--color-secondary);
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  font-weight: 700;
  text-decoration: none;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.review-preview pre {
  margin: 0;
  max-height: 240px;
  overflow: auto;
  padding: 9px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container);
  color: var(--color-on-surface);
  font-family: var(--font-tech);
  font-size: 0.6875rem;
  line-height: 1.55;
  white-space: pre-wrap;
}
.review-preview small {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
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
