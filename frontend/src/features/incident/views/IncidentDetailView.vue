<script setup lang="ts">
import { onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useIncidentStore } from '../stores/incidentStore';
import IncidentHeaderCard from '../components/IncidentHeaderCard.vue';
import DiagnosisFeedCard from '../components/DiagnosisFeedCard.vue';
import SkillTimelineCard from '../components/SkillTimelineCard.vue';
import AiActionsCard from '../components/AiActionsCard.vue';
import GovernanceCard from '../components/GovernanceCard.vue';
import SdlcChainCard from '../components/SdlcChainCard.vue';
import AiLearningCard from '../components/AiLearningCard.vue';
import { ArrowLeft } from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();
const store = useIncidentStore();

const incidentId = computed(() => route.params.incidentId as string);

const RESOLVED_STATUSES = new Set(['RESOLVED', 'LEARNING', 'CLOSED']);
const isResolved = computed(() => {
  const status = store.detail?.header.data?.status;
  return status ? RESOLVED_STATUSES.has(status) : false;
});

onMounted(() => {
  store.fetchIncidentDetail(incidentId.value);
});

onUnmounted(() => {
  store.clearDetail();
});

function goBack() {
  router.push({ name: 'incidents' });
}

function handleApprove(actionId: string) {
  store.approveAction(incidentId.value, actionId);
}

function handleReject(payload: { actionId: string; reason: string }) {
  store.rejectAction(incidentId.value, payload.actionId, payload.reason);
}

function handleChainNavigate(routePath: string) {
  router.push(routePath);
}

const NULL_SECTION = { data: null, error: null };
</script>

<template>
  <div class="detail-view">
    <!-- Back Navigation -->
    <button class="back-btn" @click="goBack">
      <ArrowLeft :size="14" />
      <span>Back to Incidents</span>
    </button>

    <!-- Loading -->
    <div v-if="store.detailLoading" class="detail-loading">
      <div class="loading-spinner"></div>
      <span>Loading incident...</span>
    </div>

    <!-- Error -->
    <div v-else-if="store.detailError" class="detail-error">
      <span class="error-text">{{ store.detailError }}</span>
      <button class="retry-btn" @click="store.fetchIncidentDetail(incidentId)">Retry</button>
    </div>

    <!-- Detail Grid -->
    <div v-else-if="store.detail" class="detail-grid">
      <IncidentHeaderCard
        :header="store.detail.header"
        :is-loading="store.detailLoading"
      />

      <DiagnosisFeedCard
        class="grid-diagnosis"
        :diagnosis="store.detail.diagnosis"
        :is-loading="store.detailLoading"
      />

      <div class="grid-right-stack">
        <SkillTimelineCard
          :timeline="store.detail.skillTimeline"
          :is-loading="store.detailLoading"
        />
        <AiActionsCard
          :actions="store.detail.actions"
          :is-loading="store.detailLoading"
          @approve="handleApprove"
          @reject="handleReject"
        />
      </div>

      <GovernanceCard
        :governance="store.detail.governance"
        :is-loading="store.detailLoading"
      />

      <SdlcChainCard
        :chain="store.detail.sdlcChain"
        :is-loading="store.detailLoading"
        @navigate="handleChainNavigate"
      />

      <AiLearningCard
        :learning="store.detail.learning ?? NULL_SECTION"
        :is-loading="store.detailLoading"
        :is-resolved="isResolved"
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

/* Row 1: Header (full width) — handled by IncidentCard's fullWidth prop */

/* Row 2-3: Diagnosis (left, span 2 rows) + right stack */
.grid-diagnosis { grid-row: span 2; }

.grid-right-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Row 4: Governance + SDLC Chain — natural 2-col flow */
/* Row 5: AI Learning (full width) — handled by IncidentCard's fullWidth prop */

/* Staggered entrance */
.detail-grid > * {
  animation: card-enter 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
}
.detail-grid > *:nth-child(1) { animation-delay: 0.04s; }
.detail-grid > *:nth-child(2) { animation-delay: 0.08s; }
.detail-grid > *:nth-child(3) { animation-delay: 0.12s; }
.detail-grid > *:nth-child(4) { animation-delay: 0.16s; }
.detail-grid > *:nth-child(5) { animation-delay: 0.20s; }
.detail-grid > *:nth-child(6) { animation-delay: 0.24s; }

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
  .grid-diagnosis { grid-row: auto; }
}
</style>
