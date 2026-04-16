<script setup lang="ts">
import { onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useRequirementStore } from '../stores/requirementStore';
import RequirementHeaderCard from '../components/RequirementHeaderCard.vue';
import DescriptionCard from '../components/DescriptionCard.vue';
import LinkedStoriesCard from '../components/LinkedStoriesCard.vue';
import LinkedSpecsCard from '../components/LinkedSpecsCard.vue';
import SdlcChainCard from '../components/SdlcChainCard.vue';
import AiAnalysisCard from '../components/AiAnalysisCard.vue';
import { ArrowLeft } from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();
const store = useRequirementStore();

const requirementId = computed(() => route.params.requirementId as string);

const NULL_SECTION = { data: null, error: null };

onMounted(() => {
  store.fetchRequirementDetail(requirementId.value);
});

onUnmounted(() => {
  store.clearDetail();
});

function goBack() {
  router.push({ name: 'requirements' });
}

function handleGenerateStories() {
  store.generateStories(requirementId.value);
}

function handleGenerateSpec(payload: { sourceType: 'requirement' | 'story'; sourceId: string }) {
  if (payload.sourceType === 'story' && payload.sourceId) {
    store.generateSpec(payload.sourceId);
    return;
  }
  // Fallback: find first story without a spec
  const stories = store.detail?.linkedStories.data?.stories ?? [];
  const candidate = stories.find(s => s.specId === null) ?? stories[0];
  if (candidate) {
    store.generateSpec(candidate.id);
  }
}

function handleRunAnalysis() {
  store.runAnalysis(requirementId.value);
}

function handleChainNavigate(routePath: string) {
  router.push(routePath);
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

      <!-- Row 2-3: Description (left, span 2 rows) -->
      <DescriptionCard
        class="grid-description"
        :description="store.detail.description"
        :is-loading="store.detailLoading"
      />

      <!-- Row 2-3: Stories + Specs (right stack) -->
      <div class="grid-right-stack">
        <LinkedStoriesCard
          :linked-stories="store.detail.linkedStories"
          :is-loading="store.detailLoading"
          @generate-stories="handleGenerateStories"
          @navigate="handleChainNavigate"
        />
        <LinkedSpecsCard
          :linked-specs="store.detail.linkedSpecs"
          :is-loading="store.detailLoading"
          @generate-spec="handleGenerateSpec"
          @navigate="handleChainNavigate"
        />
      </div>

      <!-- Row 4: SDLC Chain + AI Analysis -->
      <SdlcChainCard
        :chain="store.detail.sdlcChain ?? NULL_SECTION"
        :is-loading="store.detailLoading"
        @navigate="handleChainNavigate"
      />

      <AiAnalysisCard
        :ai-analysis="store.detail.aiAnalysis ?? NULL_SECTION"
        :is-loading="store.detailLoading"
        @run-analysis="handleRunAnalysis"
      />
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
  font-size: 0.75rem;
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

/* Row 2-3: Description (left, span 2 rows) */
.grid-description { grid-row: span 2; }

.grid-right-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.error-text { color: var(--color-incident-crimson); font-size: 0.75rem; }

.retry-btn {
  background: var(--color-surface-container-high); border: 1px solid var(--color-secondary);
  color: var(--color-secondary); padding: 6px 16px; border-radius: var(--radius-sm);
  cursor: pointer; font-family: var(--font-ui); font-size: 0.6875rem;
  text-transform: uppercase; letter-spacing: 0.04em;
}

@keyframes spin { to { transform: rotate(360deg); } }
@keyframes card-enter { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 1024px) {
  .detail-grid { grid-template-columns: 1fr; }
  .grid-description { grid-row: auto; }
}
</style>
