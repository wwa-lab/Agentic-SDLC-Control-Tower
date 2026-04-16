<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useDashboardStore } from './stores/dashboardStore';

// Sub-components
import SdlcChainHealth from './components/SdlcChainHealth.vue';
import DeliveryMetricsCard from './components/DeliveryMetricsCard.vue';
import AiParticipationCard from './components/AiParticipationCard.vue';
import QualityTestingCard from './components/QualityTestingCard.vue';
import StabilityIncidentCard from './components/StabilityIncidentCard.vue';
import GovernanceTrustCard from './components/GovernanceTrustCard.vue';
import ValueStoryCard from './components/ValueStoryCard.vue';
import RecentActivityStream from './components/RecentActivityStream.vue';

const router = useRouter();
const dashboardStore = useDashboardStore();

onMounted(() => {
  dashboardStore.fetchSummary();
});

const handleStageNavigate = (stageKey: string) => {
  const stage = dashboardStore.summary?.sdlcHealth.data?.find(s => s.key === stageKey);
  if (stage?.routePath) {
    router.push(stage.routePath);
  }
};

const handleIncidentNavigate = () => {
  router.push('/incidents');
};

const handleGovernanceNavigate = () => {
  router.push('/platform');
};

const handleActivityViewAll = () => {
  router.push('/platform');
};

const handleTeamSpaceNavigate = () => {
  router.push('/team?workspaceId=ws-default-001');
};
</script>

<template>
  <div class="dashboard-view">
    <div class="dashboard-drill-in section-high">
      <div>
        <p class="text-label">Workspace Drill-In</p>
        <p class="text-body-sm">Open Team Space to inspect operating model, risk radar, metrics, and project distribution.</p>
      </div>
      <button class="btn-machined btn-ai" @click="handleTeamSpaceNavigate">Open Team Space</button>
    </div>

    <div v-if="dashboardStore.summary" class="dashboard-grid">
      <!-- Row 1: SDLC Chain Health (Full Width) -->
      <div class="full-width">
        <SdlcChainHealth 
          :stages="dashboardStore.summary.sdlcHealth.data || []" 
          @navigate="handleStageNavigate"
        />
      </div>

      <!-- Row 2: Delivery & AI -->
      <DeliveryMetricsCard 
        :section="dashboardStore.summary.deliveryMetrics" 
        :is-loading="dashboardStore.isLoading"
      />
      <AiParticipationCard 
        :section="dashboardStore.summary.aiParticipation" 
        :is-loading="dashboardStore.isLoading"
      />

      <!-- Row 3: Quality & Stability -->
      <QualityTestingCard 
        :section="dashboardStore.summary.qualityMetrics" 
        :is-loading="dashboardStore.isLoading"
      />
      <StabilityIncidentCard 
        :section="dashboardStore.summary.stabilityMetrics" 
        :is-loading="dashboardStore.isLoading"
        @navigate-incidents="handleIncidentNavigate"
      />

      <!-- Row 4: Governance & Value Story -->
      <GovernanceTrustCard 
        :section="dashboardStore.summary.governanceMetrics" 
        :is-loading="dashboardStore.isLoading"
        @navigate-governance="handleGovernanceNavigate"
      />
      <ValueStoryCard 
        :section="dashboardStore.summary.valueStory" 
        :is-loading="dashboardStore.isLoading"
      />

      <!-- Row 5: Recent Activity (Full Width) -->
      <div class="full-width">
        <RecentActivityStream 
          :section="dashboardStore.summary.recentActivity" 
          :is-loading="dashboardStore.isLoading"
          @view-all="handleActivityViewAll"
        />
      </div>
    </div>
    
    <!-- Top-level loading state -->
    <div v-else-if="dashboardStore.isLoading" class="dashboard-loading">
      <div class="loading-spinner"></div>
      <span>Initializing Tactical Control Tower...</span>
    </div>

    <!-- Top-level error state -->
    <div v-else-if="dashboardStore.error" class="dashboard-error">
      <div class="error-message">{{ dashboardStore.error }}</div>
      <button class="retry-btn" @click="dashboardStore.fetchSummary">Retry Initialization</button>
    </div>
  </div>
</template>

<style scoped>
.dashboard-view {
  padding: 0 24px 24px;
  min-height: calc(100vh - 120px);
  position: relative;
  background:
    radial-gradient(ellipse 80% 60% at 20% 10%, var(--color-secondary-tint) 0%, transparent 60%),
    radial-gradient(ellipse 60% 50% at 80% 80%, var(--color-incident-tint) 0%, transparent 50%);
}

.dashboard-drill-in {
  margin-bottom: 20px;
  border-radius: var(--radius-sm);
  padding: 16px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

/* Staggered card entrance */
.dashboard-grid > * {
  animation: card-enter 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
}
.dashboard-grid > *:nth-child(1) { animation-delay: 0.04s; }
.dashboard-grid > *:nth-child(2) { animation-delay: 0.08s; }
.dashboard-grid > *:nth-child(3) { animation-delay: 0.12s; }
.dashboard-grid > *:nth-child(4) { animation-delay: 0.16s; }
.dashboard-grid > *:nth-child(5) { animation-delay: 0.20s; }
.dashboard-grid > *:nth-child(6) { animation-delay: 0.24s; }
.dashboard-grid > *:nth-child(7) { animation-delay: 0.28s; }
.dashboard-grid > *:nth-child(8) { animation-delay: 0.32s; }
.dashboard-grid > *:nth-child(9) { animation-delay: 0.36s; }

.full-width {
  grid-column: 1 / -1;
}

.dashboard-loading, .dashboard-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  gap: 16px;
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-surface-container-highest);
  border-top-color: var(--color-secondary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.error-message {
  color: var(--color-incident-crimson);
}

.retry-btn {
  background: var(--color-surface-container-high);
  border: 1px solid var(--color-secondary);
  color: var(--color-secondary);
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  transition: all 0.2s ease;
}

.retry-btn:hover {
  background: var(--color-secondary);
  color: var(--color-on-secondary-container);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes card-enter {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-drill-in {
    flex-direction: column;
    align-items: flex-start;
  }

  .dashboard-view {
    padding: 0 12px 16px;
  }
}
</style>
