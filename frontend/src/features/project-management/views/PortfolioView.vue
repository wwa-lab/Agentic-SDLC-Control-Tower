<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { SectionResult } from '@/shared/types/section';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import ProjectManagementCardShell from '../components/ProjectManagementCardShell.vue';
import { useProjectManagementStore } from '../stores/projectManagementStore';
import {
  PM_DEFAULT_PROJECT_ID,
  PM_DEFAULT_WORKSPACE_ID,
  PM_PROJECT_ID_PATTERN,
  type CadenceMetric,
  type DependencyBottleneck,
  type HealthIndicator,
  type PortfolioCapacity,
  type PortfolioHeatmap,
  type PortfolioRiskConcentration,
  type PortfolioSummary,
  type ProjectManagementPortfolioCardKey,
} from '../types';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const projectManagementStore = useProjectManagementStore();

function emptySection<T>(): SectionResult<T> {
  return {
    data: null,
    error: null,
  };
}

const resolvedWorkspaceId = computed(() => {
  const value = route.query.workspaceId;
  return typeof value === 'string' && value.trim() ? value : PM_DEFAULT_WORKSPACE_ID;
});

const summarySection = computed(() => projectManagementStore.portfolio?.summary ?? emptySection<PortfolioSummary>());
const heatmapSection = computed(() => projectManagementStore.portfolio?.heatmap ?? emptySection<PortfolioHeatmap>());
const capacitySection = computed(() => projectManagementStore.portfolio?.capacity ?? emptySection<PortfolioCapacity>());
const risksSection = computed(() => projectManagementStore.portfolio?.risks ?? emptySection<PortfolioRiskConcentration>());
const bottlenecksSection = computed(() => projectManagementStore.portfolio?.bottlenecks ?? emptySection<ReadonlyArray<DependencyBottleneck>>());
const cadenceSection = computed(() => projectManagementStore.portfolio?.cadence ?? emptySection<ReadonlyArray<CadenceMetric>>());

const summaryData = computed(() => summarySection.value.data);

function formatTimestamp(value: string | null | undefined) {
  if (!value) {
    return 'n/a';
  }
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function healthClass(status: HealthIndicator) {
  return `pm-health--${status.toLowerCase()}`;
}

function trendGlyph(trend: string) {
  return trend === 'UP' ? '▲' : trend === 'DOWN' ? '▼' : '■';
}

function openPlan(projectId: string) {
  void router.push({
    path: `/project-management/plan/${projectId}`,
    query: {
      workspaceId: resolvedWorkspaceId.value,
    },
  });
}

function retryCard(cardKey: ProjectManagementPortfolioCardKey) {
  void projectManagementStore.refreshPortfolioCard(cardKey);
}

watch(
  () => resolvedWorkspaceId.value,
  workspaceId => {
    void projectManagementStore.initPortfolio(workspaceId);
  },
  { immediate: true },
);

watch(
  () => route.query.projectId,
  projectId => {
    if (typeof projectId !== 'string' || !PM_PROJECT_ID_PATTERN.test(projectId)) {
      return;
    }
    const nextQuery = {
      ...route.query,
    };
    delete nextQuery.projectId;
    void router.replace({
      path: `/project-management/plan/${projectId}`,
      query: nextQuery,
    });
  },
  { immediate: true },
);

watch(
  () => projectManagementStore.portfolio,
  aggregate => {
    const summary = aggregate?.summary.data;
    workspaceStore.setRouteContext({
      workspace: summary?.workspaceId === 'ws-default-001' ? 'Global SDLC Tower' : summary?.workspaceId ?? workspaceStore.context.workspace,
      application: 'Project Management Portfolio',
      snowGroup: workspaceStore.context.snowGroup,
      project: null,
      environment: null,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Project Management', path: '/project-management' },
      { label: resolvedWorkspaceId.value },
    ]);

    shellUiStore.setAiPanelContent({
      summary: summary
        ? `${summary.activeProjects} active projects are in scope for ${resolvedWorkspaceId.value}, with ${summary.redProjects} red projects and ${summary.pendingApprovals} approvals still open.`
        : 'Project Management portfolio is loading cross-project heat, capacity, risk, and dependency signals.',
      reasoning: [
        {
          text: `Workspace scope: ${resolvedWorkspaceId.value}`,
          status: 'ok',
        },
        {
          text: summary?.redProjects ? `${summary.redProjects} projects require executive attention` : 'No red projects in the current workspace scope',
          status: summary?.redProjects ? 'warning' : 'ok',
        },
        {
          text: summary?.aiPendingReview ? `${summary.aiPendingReview} AI suggestions await review` : 'AI review queue is clear',
          status: summary?.aiPendingReview ? 'warning' : 'ok',
        },
      ],
      evidence: JSON.stringify(
        {
          workspaceId: resolvedWorkspaceId.value,
          route: route.fullPath,
          loadedCards: aggregate
            ? Object.entries(aggregate)
                .filter(([, value]) => value.data)
                .map(([key]) => key)
            : [],
        },
        null,
        2,
      ),
    });
  },
  { immediate: true, deep: true },
);

onMounted(() => {
  shellUiStore.setBreadcrumbs([
    { label: 'Dashboard', path: '/' },
    { label: 'Project Management', path: '/project-management' },
    { label: resolvedWorkspaceId.value },
  ]);
});

onBeforeUnmount(() => {
  workspaceStore.clearRouteContext();
  shellUiStore.clearBreadcrumbs();
  shellUiStore.clearAiPanelContent();
  projectManagementStore.clearMutationFeedback();
  projectManagementStore.reset();
});
</script>

<template>
  <div class="pm-page">
    <div class="pm-hero">
      <div>
        <p class="text-label">Portfolio Command</p>
        <h2>Project Management Portfolio</h2>
        <p class="pm-hero__copy">
          Workspace {{ resolvedWorkspaceId }} across heat, capacity, risk concentration, dependency drag, and delivery cadence.
        </p>
      </div>
      <button class="btn-machined" @click="openPlan(PM_DEFAULT_PROJECT_ID)">Open Default Plan</button>
    </div>

    <div v-if="projectManagementStore.portfolioError && !projectManagementStore.portfolio" class="pm-page-error">
      <p class="text-label">
        {{ projectManagementStore.isPortfolioForbidden ? 'Access Denied' : 'Project Management Unavailable' }}
      </p>
      <p class="text-body-sm">{{ projectManagementStore.portfolioError }}</p>
      <button class="btn-machined" @click="projectManagementStore.initPortfolio(resolvedWorkspaceId)">Retry</button>
    </div>

    <template v-else>
      <ProjectManagementCardShell
        title="Portfolio Summary"
        :loading="projectManagementStore.portfolioLoading || projectManagementStore.portfolioLoadingCards.summary"
        :error="summarySection.error"
        :full-width="true"
        @retry="retryCard('summary')"
      >
        <template #subtitle>
          <p class="pm-card-subtitle">Operational digest and approval pressure</p>
        </template>

        <div v-if="summaryData" class="pm-summary-grid">
          <div class="pm-summary-metric">
            <span class="text-label">Active Projects</span>
            <strong>{{ summaryData.activeProjects }}</strong>
          </div>
          <div class="pm-summary-metric">
            <span class="text-label">Red Projects</span>
            <strong class="pm-text-red">{{ summaryData.redProjects }}</strong>
          </div>
          <div class="pm-summary-metric">
            <span class="text-label">At Risk / Slipped</span>
            <strong class="pm-text-amber">{{ summaryData.atRiskOrSlippedMilestones }}</strong>
          </div>
          <div class="pm-summary-metric">
            <span class="text-label">Critical Risks</span>
            <strong class="pm-text-red">{{ summaryData.criticalRisks }}</strong>
          </div>
          <div class="pm-summary-metric">
            <span class="text-label">Blocked Dependencies</span>
            <strong>{{ summaryData.blockedDependencies }}</strong>
          </div>
          <div class="pm-summary-metric">
            <span class="text-label">Pending Approvals</span>
            <strong>{{ summaryData.pendingApprovals }}</strong>
          </div>
          <div class="pm-summary-metric">
            <span class="text-label">AI Pending Review</span>
            <strong>{{ summaryData.aiPendingReview }}</strong>
          </div>
          <div class="pm-summary-metric">
            <span class="text-label">Refreshed</span>
            <strong>{{ formatTimestamp(summaryData.lastRefreshedAt) }}</strong>
          </div>
        </div>
      </ProjectManagementCardShell>

      <div class="pm-grid">
        <ProjectManagementCardShell
          title="Milestone Heatmap"
          :loading="projectManagementStore.portfolioLoading || projectManagementStore.portfolioLoadingCards.heatmap"
          :error="heatmapSection.error"
          :empty="!heatmapSection.data?.rows.length"
          empty-message="No portfolio heatmap data available."
          @retry="retryCard('heatmap')"
        >
          <template #subtitle>
            <p class="pm-card-subtitle">Project x week dominant status</p>
          </template>

          <div v-if="heatmapSection.data" class="pm-heatmap">
            <div class="pm-heatmap__header">
              <span></span>
              <span v-for="column in heatmapSection.data.columns" :key="column" class="text-label">{{ column }}</span>
            </div>
            <div v-for="row in heatmapSection.data.rows" :key="row.projectId" class="pm-heatmap__row">
              <button class="pm-link-button" @click="openPlan(row.projectId)">{{ row.projectName }}</button>
              <button
                v-for="cell in row.cells"
                :key="`${row.projectId}-${cell.windowLabel}`"
                class="pm-heatmap__cell"
                :class="healthClass(cell.dominantStatus)"
                :title="`${row.projectName} / ${cell.windowLabel} / ${cell.dominantStatus}`"
                @click="openPlan(row.projectId)"
              >
                <span>{{ cell.dominantStatus }}</span>
              </button>
            </div>
          </div>
        </ProjectManagementCardShell>

        <ProjectManagementCardShell
          title="Capacity Allocation"
          :loading="projectManagementStore.portfolioLoading || projectManagementStore.portfolioLoadingCards.capacity"
          :error="capacitySection.error"
          :empty="!capacitySection.data?.rows.length"
          empty-message="No portfolio capacity rows available."
          @retry="retryCard('capacity')"
        >
          <template #subtitle>
            <p class="pm-card-subtitle">Person-level load across visible projects</p>
          </template>

          <div v-if="capacitySection.data" class="pm-capacity-list">
            <div v-for="row in capacitySection.data.rows" :key="row.memberId" class="pm-capacity-row">
              <div class="pm-capacity-row__meta">
                <div>
                  <strong>{{ row.displayName }}</strong>
                  <p class="text-body-sm">{{ row.totalPercent }}% total allocation</p>
                </div>
                <span class="pm-pill" :class="row.flag === 'OVER' ? 'pm-pill--danger' : row.flag === 'FOCUS' ? 'pm-pill--warning' : 'pm-pill--ok'">
                  {{ row.flag }}
                </span>
              </div>

              <div class="pm-capacity-row__bar">
                <div
                  v-for="(cell, index) in row.cells"
                  :key="`${row.memberId}-${cell.projectId}`"
                  class="pm-capacity-row__segment"
                  :style="{
                    width: `${Math.max(6, (cell.percent / Math.max(100, row.totalPercent || 1)) * 100)}%`,
                    '--segment-index': String(index),
                  }"
                >
                  <span>{{ cell.percent }}%</span>
                </div>
              </div>

              <div class="pm-capacity-row__legend">
                <span
                  v-for="project in capacitySection.data.projects"
                  :key="project.projectId"
                  class="pm-capacity-row__legend-item"
                >
                  {{ project.projectName }}
                </span>
              </div>
            </div>
          </div>
        </ProjectManagementCardShell>

        <ProjectManagementCardShell
          title="Risk Concentration"
          :loading="projectManagementStore.portfolioLoading || projectManagementStore.portfolioLoadingCards.risks"
          :error="risksSection.error"
          :empty="!risksSection.data?.topRisks.length"
          empty-message="No risks registered in this workspace."
          @retry="retryCard('risks')"
        >
          <template #subtitle>
            <p class="pm-card-subtitle">Weighted risk stack across the workspace</p>
          </template>

          <div v-if="risksSection.data" class="pm-risk-list">
            <div v-for="risk in risksSection.data.topRisks" :key="risk.id" class="pm-risk-item">
              <div class="pm-risk-item__header">
                <strong>{{ risk.title }}</strong>
                <span class="pm-pill" :class="`pm-pill--${risk.severity.toLowerCase()}`">{{ risk.severity }}</span>
              </div>
              <p class="text-body-sm">{{ risk.projectId }} · {{ risk.category }} · {{ risk.state }}</p>
            </div>

            <div class="pm-risk-heat">
              <span
                v-for="bucket in risksSection.data.severityCategoryHeatmap"
                :key="`${bucket.severity}-${bucket.category}`"
                class="pm-risk-heat__item"
              >
                {{ bucket.severity }} / {{ bucket.category }}: {{ bucket.count }}
              </span>
            </div>
          </div>
        </ProjectManagementCardShell>

        <ProjectManagementCardShell
          title="Dependency Bottlenecks"
          :loading="projectManagementStore.portfolioLoading || projectManagementStore.portfolioLoadingCards.bottlenecks"
          :error="bottlenecksSection.error"
          :empty="!bottlenecksSection.data?.length"
          empty-message="No active bottlenecks in the current workspace scope."
          @retry="retryCard('bottlenecks')"
        >
          <template #subtitle>
            <p class="pm-card-subtitle">Negotiating or blocked edges ordered by drag</p>
          </template>

          <div v-if="bottlenecksSection.data" class="pm-bottleneck-list">
            <button
              v-for="dependency in bottlenecksSection.data"
              :key="dependency.dependencyId"
              class="pm-bottleneck-item"
              @click="openPlan(dependency.sourceProjectId)"
            >
              <div>
                <strong>{{ dependency.sourceProjectName }}</strong>
                <p class="text-body-sm">{{ dependency.targetDescriptor }} · {{ dependency.relationship }}</p>
              </div>
              <div class="pm-bottleneck-item__meta">
                <span class="pm-pill" :class="dependency.daysBlocked > 5 ? 'pm-pill--danger' : 'pm-pill--warning'">
                  {{ dependency.daysBlocked }}d
                </span>
                <span class="text-body-sm">{{ dependency.ownerTeam }}</span>
              </div>
            </button>
          </div>
        </ProjectManagementCardShell>

        <ProjectManagementCardShell
          title="Cadence Metrics"
          :loading="projectManagementStore.portfolioLoading || projectManagementStore.portfolioLoadingCards.cadence"
          :error="cadenceSection.error"
          :empty="!cadenceSection.data?.length"
          empty-message="Cadence data is not available yet."
          @retry="retryCard('cadence')"
        >
          <template #subtitle>
            <p class="pm-card-subtitle">Throughput, cycle time, and WIP posture</p>
          </template>

          <div v-if="cadenceSection.data" class="pm-cadence-grid">
            <div v-for="metric in cadenceSection.data" :key="metric.key" class="pm-cadence-card">
              <span class="text-label">{{ metric.key }}</span>
              <strong>{{ metric.value }}</strong>
              <p class="text-body-sm">
                {{ trendGlyph(metric.trend) }} {{ metric.deltaAbs }} · {{ metric.window }}
              </p>
            </div>
          </div>
        </ProjectManagementCardShell>
      </div>
    </template>
  </div>
</template>

<style scoped>
.pm-page {
  padding: 0 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.pm-hero,
.pm-page-error {
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  background:
    radial-gradient(circle at top right, color-mix(in srgb, var(--color-secondary) 16%, transparent), transparent 45%),
    linear-gradient(180deg, color-mix(in srgb, var(--color-surface-container-high) 92%, transparent), var(--color-surface-container-low));
  padding: 22px 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.pm-hero h2 {
  font-size: 2rem;
}

.pm-hero__copy {
  margin-top: 8px;
  max-width: 720px;
  color: var(--color-on-surface-variant);
  line-height: 1.6;
}

.pm-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 18px;
}

.pm-grid > * {
  grid-column: span 6;
}

.pm-card-subtitle {
  margin-top: 6px;
  color: var(--color-on-surface-variant);
  font-size: 0.8rem;
}

.pm-summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.pm-summary-metric {
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 14px;
  background: color-mix(in srgb, var(--color-surface-container-highest) 72%, transparent);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pm-summary-metric strong {
  font-size: 1.4rem;
}

.pm-text-red {
  color: var(--color-incident-crimson);
}

.pm-text-amber {
  color: var(--color-approval-amber);
}

.pm-link-button,
.pm-bottleneck-item {
  border: 0;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.pm-heatmap {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pm-heatmap__header,
.pm-heatmap__row {
  display: grid;
  grid-template-columns: 1.6fr repeat(4, minmax(0, 1fr));
  gap: 8px;
  align-items: center;
}

.pm-heatmap__cell {
  min-height: 54px;
  border-radius: var(--radius-sm);
  border: var(--border-ghost);
  cursor: pointer;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--color-on-surface);
}

.pm-health--green {
  background: color-mix(in srgb, var(--color-health-emerald) 18%, transparent);
}

.pm-health--yellow {
  background: color-mix(in srgb, var(--color-approval-amber) 20%, transparent);
}

.pm-health--red {
  background: color-mix(in srgb, var(--color-incident-crimson) 20%, transparent);
}

.pm-health--unknown {
  background: color-mix(in srgb, var(--color-on-surface-variant) 14%, transparent);
}

.pm-capacity-list,
.pm-risk-list,
.pm-bottleneck-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.pm-capacity-row {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pm-capacity-row__meta,
.pm-risk-item__header,
.pm-bottleneck-item,
.pm-bottleneck-item__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.pm-capacity-row__bar {
  display: flex;
  gap: 6px;
}

.pm-capacity-row__segment {
  min-height: 28px;
  border-radius: 999px;
  background:
    linear-gradient(135deg,
      color-mix(in srgb, var(--color-secondary) calc(40% + (var(--segment-index) * 8%)), transparent),
      color-mix(in srgb, var(--color-secondary-glow) 45%, transparent));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.72rem;
  font-weight: 700;
  color: var(--color-on-surface);
}

.pm-capacity-row__legend {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pm-capacity-row__legend-item,
.pm-risk-heat__item {
  padding: 4px 8px;
  border-radius: 999px;
  border: var(--border-ghost);
  color: var(--color-on-surface-variant);
  font-size: 0.72rem;
}

.pm-risk-item,
.pm-bottleneck-item {
  padding: 12px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: color-mix(in srgb, var(--color-surface-container-highest) 70%, transparent);
}

.pm-risk-heat {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pm-cadence-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.pm-cadence-card {
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 14px;
  background: color-mix(in srgb, var(--color-surface-container-highest) 72%, transparent);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pm-cadence-card strong {
  font-size: 1.5rem;
}

.pm-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.05em;
}

.pm-pill--ok {
  background: color-mix(in srgb, var(--color-health-emerald) 20%, transparent);
}

.pm-pill--warning {
  background: color-mix(in srgb, var(--color-approval-amber) 22%, transparent);
}

.pm-pill--danger,
.pm-pill--critical,
.pm-pill--high {
  background: color-mix(in srgb, var(--color-incident-crimson) 20%, transparent);
}

.pm-pill--medium,
.pm-pill--low {
  background: color-mix(in srgb, var(--color-secondary) 18%, transparent);
}

@media (max-width: 1180px) {
  .pm-summary-grid,
  .pm-cadence-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .pm-grid > * {
    grid-column: 1 / -1;
  }

  .pm-hero,
  .pm-page-error,
  .pm-summary-grid {
    grid-template-columns: 1fr;
  }

  .pm-hero {
    flex-direction: column;
    align-items: stretch;
  }
}

@media (max-width: 720px) {
  .pm-page {
    padding: 0 16px 20px;
  }

  .pm-summary-grid,
  .pm-cadence-grid {
    grid-template-columns: 1fr;
  }

  .pm-heatmap__header,
  .pm-heatmap__row {
    grid-template-columns: 1fr;
  }
}
</style>
