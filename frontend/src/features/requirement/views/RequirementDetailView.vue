<script setup lang="ts">
import { onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useRequirementStore } from '../stores/requirementStore';
import RequirementHeaderCard from '../components/RequirementHeaderCard.vue';
import LinkedStoriesCard from '../components/LinkedStoriesCard.vue';
import ProfileSelector from '../components/ProfileSelector.vue';
import EntryPathSelector from '../components/EntryPathSelector.vue';
import SpecTierSelector from '../components/SpecTierSelector.vue';
import SourceReferencesPanel from '../components/SourceReferencesPanel.vue';
import SddDocumentsPanel from '../components/SddDocumentsPanel.vue';
import BusinessReviewPanel from '../components/BusinessReviewPanel.vue';
import RequirementTraceabilityPanel from '../components/RequirementTraceabilityPanel.vue';
import { ArrowLeft, Workflow } from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();
const store = useRequirementStore();

const requirementId = computed(() => route.params.requirementId as string);

const NULL_SECTION = { data: null, error: null };

onMounted(() => {
  store.loadActiveProfile();
  store.fetchRequirementDetail(requirementId.value);
});

onUnmounted(() => {
  store.clearDetail();
});

function goBack() {
  router.push({ name: 'requirements' });
}

function handleChainNavigate(routePath: string) {
  router.push(routePath);
}

function openSkillFlow() {
  router.push({ name: 'requirement-skill-flow' });
}

function handleRetryControlPlane() {
  store.fetchControlPlane(requirementId.value);
}
</script>

<template>
  <div class="detail-view">
    <!-- Back Navigation -->
    <button class="back-btn" @click="goBack">
      <ArrowLeft :size="14" />
      <span>Back to Requirements</span>
    </button>

    <!-- Loading -->
    <div v-if="store.detailLoading" class="detail-loading">
      <div class="loading-spinner"></div>
      <span>Loading requirement...</span>
    </div>

    <!-- Error -->
    <div v-else-if="store.detailError" class="detail-error">
      <span class="error-text">{{ store.detailError }}</span>
      <button class="retry-btn" @click="store.fetchRequirementDetail(requirementId)">Retry</button>
    </div>

    <!-- Detail Grid -->
    <div v-else-if="store.detail" class="detail-grid">
      <!-- Row 1: Header (full width) -->
      <RequirementHeaderCard
        :header="store.detail.header"
        :is-loading="store.detailLoading"
      />

      <div class="profile-strip">
        <div class="profile-strip-main">
          <ProfileSelector
            :profiles="store.availableProfiles"
            :model-value="store.activeProfile.id"
            @update:model-value="store.setActiveProfile"
          />
          <span class="profile-strip-text">{{ store.activeProfile.description }}</span>
        </div>
        <div class="profile-strip-actions">
          <button class="skill-flow-link" type="button" @click="openSkillFlow">
            <Workflow :size="14" />
            <span>Skill & Doc Flow</span>
          </button>
          <EntryPathSelector
            :profile="store.activeProfile"
            :orchestrator-result="store.orchestratorResult"
          />
          <SpecTierSelector
            :profile="store.activeProfile"
            :orchestrator-result="store.orchestratorResult"
          />
        </div>
        <p v-if="store.skillMessage" class="profile-skill-message">{{ store.skillMessage }}</p>
      </div>

      <SourceReferencesPanel
        class="grid-control-wide"
        :sources="store.sourceReferences"
        :is-loading="store.controlPlaneLoading"
        :error="store.controlPlaneError"
        @refresh="store.refreshSourceReference"
        @retry="handleRetryControlPlane"
      />

      <SddDocumentsPanel
        class="grid-control-wide"
        :documents="store.sddDocuments"
        :selected-document-id="store.selectedDocumentId"
        :selected-document="store.selectedDocument"
        :document-loading="store.selectedDocumentLoading"
        :document-error="store.selectedDocumentError"
        :is-loading="store.controlPlaneLoading"
        :error="store.controlPlaneError"
        :sync-loading="store.githubSyncLoading"
        :sync-error="store.githubSyncError"
        @open-document="store.openSddDocument"
        @refresh-documents="store.refreshGitHubDocuments([requirementId])"
        @retry="handleRetryControlPlane"
      />

      <BusinessReviewPanel
        :selected-document="store.selectedDocument"
        :selected-document-id="store.selectedDocumentId"
        :documents="store.sddDocuments"
        :reviews="store.documentReviews"
        :is-loading="store.controlPlaneLoading"
        @review="store.createReview"
      />

      <RequirementTraceabilityPanel
        :traceability="store.traceability"
        :is-loading="store.controlPlaneLoading"
      />

      <!-- Row 2-3: Stories + Specs (right stack) -->
      <div class="grid-right-stack">
        <LinkedStoriesCard
          :requirement-id="requirementId"
          :linked-stories="store.detail.linkedStories"
          :jira-sources="store.sourceReferences.filter(source => source.sourceType === 'JIRA')"
          :is-loading="store.detailLoading"
          :is-refreshing="store.controlPlaneLoading"
          @navigate="handleChainNavigate"
          @refresh-jira="store.refreshSourceReference"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 24px 24px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: var(--color-secondary);
  font-family: var(--font-ui);
  font-size: 0.8125rem;
  cursor: pointer;
  padding: 4px 0;
  align-self: flex-start;
  transition: opacity 0.2s ease;
}

.back-btn:hover { opacity: 0.7; }

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.profile-strip {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 16px;
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
}

.profile-strip-main {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.profile-strip-text {
  font-family: var(--font-ui);
  font-size: 0.8125rem;
  color: var(--color-on-surface-variant);
}

.profile-strip-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.skill-flow-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 32px;
  padding: 6px 10px;
  border: 1px solid rgba(137, 206, 255, 0.45);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container);
  color: var(--color-secondary);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.skill-flow-link:hover {
  background: var(--color-secondary-tint);
}

.profile-skill-message {
  margin: 0;
  font-family: var(--font-ui);
  font-size: 0.8125rem;
  color: var(--color-secondary);
}

.detail-view :deep(.requirement-card) {
  padding: 18px;
  gap: 14px;
}

.detail-view :deep(.card-title) {
  font-size: 0.75rem;
  letter-spacing: 0.06em;
}

.detail-view :deep(.req-id) {
  font-size: 0.9375rem;
}

.detail-view :deep(.req-title) {
  font-size: 1.125rem;
  line-height: 1.35;
}

.detail-view :deep(.section-label),
.detail-view :deep(.meta-label),
.detail-view :deep(.profile-label),
.detail-view :deep(.path-label),
.detail-view :deep(.tier-label) {
  font-size: 0.6875rem;
  letter-spacing: 0.05em;
}

.detail-view :deep(.section-text),
.detail-view :deep(.criterion),
.detail-view :deep(.bullet-list li),
.detail-view :deep(.meta-value),
.detail-view :deep(.source-title),
.detail-view :deep(.stage-title),
.detail-view :deep(.trace-row),
.detail-view :deep(.run-meta),
.detail-view :deep(.artifact-list a),
.detail-view :deep(.empty),
.detail-view :deep(.section-error) {
  font-size: 0.8125rem;
  line-height: 1.5;
}

.detail-view :deep(.priority-badge),
.detail-view :deep(.status-text),
.detail-view :deep(.category-badge),
.detail-view :deep(.source-badge),
.detail-view :deep(.freshness-chip),
.detail-view :deep(.criterion-id),
.detail-view :deep(.stage-path),
.detail-view :deep(.stage-meta),
.detail-view :deep(.source-type),
.detail-view :deep(.source-meta),
.detail-view :deep(.viewer-meta),
.detail-view :deep(.run-id),
.detail-view :deep(.run-status) {
  font-size: 0.6875rem;
}

.detail-view :deep(.profile-select),
.detail-view :deep(.skill-btn),
.detail-view :deep(.path-value),
.detail-view :deep(.path-placeholder),
.detail-view :deep(.tier-value),
.detail-view :deep(.tier-placeholder) {
  font-size: 0.75rem;
}

.detail-view :deep(.criterion-check) {
  width: 18px;
  height: 18px;
  font-size: 0.75rem;
}

.detail-view :deep(.markdown-viewer pre) {
  font-size: 0.8125rem;
  line-height: 1.6;
}

.grid-right-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.grid-control-wide {
  grid-column: 1 / -1;
}

/* Staggered entrance */
.detail-grid > * {
  animation: card-enter 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
}
.detail-grid > *:nth-child(1) { animation-delay: 0.04s; }
.detail-grid > *:nth-child(2) { animation-delay: 0.08s; }
.detail-grid > *:nth-child(3) { animation-delay: 0.12s; }
.detail-grid > *:nth-child(4) { animation-delay: 0.16s; }
.detail-grid > *:nth-child(5) { animation-delay: 0.20s; }

.detail-loading, .detail-error {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 12px; padding: 60px 0; color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
}

.loading-spinner {
  width: 32px; height: 32px;
  border: 2px solid var(--color-surface-container-highest);
  border-top-color: var(--color-secondary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.error-text { color: var(--color-incident-crimson); font-size: 0.8125rem; }

.retry-btn {
  background: var(--color-surface-container-high); border: 1px solid var(--color-secondary);
  color: var(--color-secondary); padding: 6px 16px; border-radius: var(--radius-sm);
  cursor: pointer; font-family: var(--font-ui); font-size: 0.75rem;
  text-transform: uppercase; letter-spacing: 0.04em;
}

@keyframes spin { to { transform: rotate(360deg); } }
@keyframes card-enter { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 1024px) {
  .detail-grid { grid-template-columns: 1fr; }
}
</style>
