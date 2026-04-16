<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { SectionResult } from '@/shared/types/section';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useTeamSpaceStore } from './stores/teamSpaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import type { TeamSpaceCardKey } from './types/aggregate';
import type { WorkspaceSummary } from './types/workspace';
import type { TeamOperatingModel } from './types/operatingModel';
import type { MemberMatrix } from './types/members';
import type { TeamDefaultTemplates } from './types/templates';
import type { RequirementPipeline } from './types/pipeline';
import type { TeamMetrics } from './types/metrics';
import type { TeamRiskRadar } from './types/risks';
import type { ProjectDistribution } from './types/projects';
import SdlcChainStrip from './components/SdlcChainStrip.vue';
import WorkspaceSummaryCard from './components/WorkspaceSummaryCard.vue';
import TeamOperatingModelCard from './components/TeamOperatingModelCard.vue';
import MemberMatrixCard from './components/MemberMatrixCard.vue';
import TeamTemplatesCard from './components/TeamTemplatesCard.vue';
import PipelineCard from './components/PipelineCard.vue';
import MetricsCard from './components/MetricsCard.vue';
import RiskRadarCard from './components/RiskRadarCard.vue';
import ProjectDistributionCard from './components/ProjectDistributionCard.vue';

const DEFAULT_WORKSPACE_ID = 'ws-default-001';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const teamSpaceStore = useTeamSpaceStore();
const shellUiStore = useShellUiStore();

function emptySection<T>(): SectionResult<T> {
  return {
    data: null,
    error: null,
  };
}

const summarySection = computed(() => teamSpaceStore.aggregate?.summary ?? emptySection<WorkspaceSummary>());
const operatingSection = computed(() => teamSpaceStore.aggregate?.operatingModel ?? emptySection<TeamOperatingModel>());
const membersSection = computed(() => teamSpaceStore.aggregate?.members ?? emptySection<MemberMatrix>());
const templatesSection = computed(() => teamSpaceStore.aggregate?.templates ?? emptySection<TeamDefaultTemplates>());
const pipelineSection = computed(() => teamSpaceStore.aggregate?.pipeline ?? emptySection<RequirementPipeline>());
const metricsSection = computed(() => teamSpaceStore.aggregate?.metrics ?? emptySection<TeamMetrics>());
const risksSection = computed(() => teamSpaceStore.aggregate?.risks ?? emptySection<TeamRiskRadar>());
const projectsSection = computed(() => teamSpaceStore.aggregate?.projects ?? emptySection<ProjectDistribution>());

const resolvedWorkspaceId = computed(() => {
  const queryValue = route.query.workspaceId;
  return typeof queryValue === 'string' && queryValue.trim() ? queryValue : DEFAULT_WORKSPACE_ID;
});

async function loadWorkspace(nextWorkspaceId: string) {
  if (route.query.workspaceId !== nextWorkspaceId) {
    await router.replace({
      path: '/team',
      query: {
        ...route.query,
        workspaceId: nextWorkspaceId,
      },
    });
  }
  await teamSpaceStore.switchWorkspace(nextWorkspaceId);
}

function navigate(url: string) {
  void router.push(url);
}

function retryCard(cardKey: TeamSpaceCardKey) {
  void teamSpaceStore.retryCard(cardKey);
}

function navigateRequirementFilter(filter: string) {
  void router.push({
    path: '/requirements',
    query: {
      workspaceId: resolvedWorkspaceId.value,
      filter,
    },
  });
}

watch(
  () => resolvedWorkspaceId.value,
  workspaceId => {
    void loadWorkspace(workspaceId);
  },
  { immediate: true },
);

watch(
  () => teamSpaceStore.aggregate,
  aggregate => {
    const summary = aggregate?.summary.data;
    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Team Space', path: '/team' },
      { label: summary?.name ?? resolvedWorkspaceId.value },
    ]);

    shellUiStore.setAiPanelContent({
      summary: summary
        ? `${summary.name} is in ${summary.healthAggregate} health with ${summary.activeProjectCount} active projects and ${summary.activeEnvironmentCount} active environments.`
        : 'Team Space is loading workspace context and waiting on section projections.',
      reasoning: [
        { text: `Workspace scope: ${resolvedWorkspaceId.value}`, status: 'ok' },
        {
          text: teamSpaceStore.aggregate?.pipeline.error
            ? 'Pipeline projection degraded'
            : 'Aggregate first paint active',
          status: teamSpaceStore.aggregate?.pipeline.error ? 'warning' : 'ok',
        },
        {
          text: teamSpaceStore.aggregate?.risks.data?.total
            ? `${teamSpaceStore.aggregate.risks.data.total} active risks visible`
            : 'Risk radar currently clear or still loading',
          status: teamSpaceStore.aggregate?.risks.data?.total ? 'warning' : 'ok',
        },
      ],
      evidence: JSON.stringify(
        {
          workspaceId: resolvedWorkspaceId.value,
          route: route.fullPath,
          shellWorkspace: workspaceStore.context.workspace,
          loadedCards: teamSpaceStore.aggregate
            ? Object.entries(teamSpaceStore.aggregate)
                .filter(([key, value]) => key !== 'workspaceId' && value && typeof value === 'object' && 'data' in value && value.data)
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
    { label: 'Team Space', path: '/team' },
    { label: resolvedWorkspaceId.value },
  ]);
});

onBeforeUnmount(() => {
  shellUiStore.clearBreadcrumbs();
  shellUiStore.clearAiPanelContent();
  teamSpaceStore.reset();
});
</script>

<template>
  <div class="team-space-view">
    <div v-if="teamSpaceStore.error && !teamSpaceStore.aggregate" class="team-space-page-error section-high">
      <p class="text-label">{{ teamSpaceStore.isForbidden ? 'Access Denied' : 'Team Space Unavailable' }}</p>
      <p class="text-body-sm">{{ teamSpaceStore.error }}</p>
      <button class="btn-machined" @click="loadWorkspace(resolvedWorkspaceId)">Retry</button>
    </div>

    <template v-else>
      <div class="team-space-strip section-high">
        <div class="team-space-strip__header">
          <span class="text-label">SDLC Chain Overview</span>
          <span class="text-tech">Workspace {{ resolvedWorkspaceId }}</span>
        </div>
        <SdlcChainStrip :nodes="pipelineSection.data?.chain ?? []" />
      </div>

      <div class="team-space-grid">
        <WorkspaceSummaryCard
          :section="summarySection"
          :is-loading="teamSpaceStore.isLoading || teamSpaceStore.loadingCards.summary"
          @retry="retryCard('summary')"
        />
        <TeamOperatingModelCard
          :section="operatingSection"
          :is-loading="teamSpaceStore.isLoading || teamSpaceStore.loadingCards.operatingModel"
          @retry="retryCard('operatingModel')"
          @navigate-platform="navigate"
        />
        <MemberMatrixCard
          :section="membersSection"
          :is-loading="teamSpaceStore.isLoading || teamSpaceStore.loadingCards.members"
          @retry="retryCard('members')"
          @navigate-access="navigate"
        />
        <TeamTemplatesCard
          :section="templatesSection"
          :is-loading="teamSpaceStore.isLoading || teamSpaceStore.loadingCards.templates"
          @retry="retryCard('templates')"
        />

        <div class="team-space-grid__full">
          <PipelineCard
            :section="pipelineSection"
            :is-loading="teamSpaceStore.isLoading || teamSpaceStore.loadingCards.pipeline"
            @retry="retryCard('pipeline')"
            @navigate-filter="navigateRequirementFilter"
            @navigate-link="navigate"
          />
        </div>

        <MetricsCard
          :section="metricsSection"
          :is-loading="teamSpaceStore.isLoading || teamSpaceStore.loadingCards.metrics"
          @retry="retryCard('metrics')"
          @navigate-history="navigate"
        />
        <RiskRadarCard
          :section="risksSection"
          :is-loading="teamSpaceStore.isLoading || teamSpaceStore.loadingCards.risks"
          @retry="retryCard('risks')"
          @navigate="navigate"
        />

        <div class="team-space-grid__full">
          <ProjectDistributionCard
            :section="projectsSection"
            :is-loading="teamSpaceStore.isLoading || teamSpaceStore.loadingCards.projects"
            @retry="retryCard('projects')"
            @open-project="navigate"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.team-space-view {
  padding: 0 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.team-space-page-error,
.team-space-strip {
  border-radius: var(--radius-sm);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.team-space-strip__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.team-space-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 20px;
}

.team-space-grid > :nth-child(1),
.team-space-grid > :nth-child(2),
.team-space-grid > :nth-child(3),
.team-space-grid > :nth-child(4) {
  grid-column: span 6;
}

.team-space-grid > :nth-child(6) {
  grid-column: span 4;
}

.team-space-grid > :nth-child(5) {
  grid-column: span 8;
}

.team-space-grid__full {
  grid-column: 1 / -1;
}

@media (max-width: 1400px) {
  .team-space-grid {
    grid-template-columns: repeat(8, minmax(0, 1fr));
  }

  .team-space-grid > :nth-child(1),
  .team-space-grid > :nth-child(2),
  .team-space-grid > :nth-child(3),
  .team-space-grid > :nth-child(4),
  .team-space-grid > :nth-child(5),
  .team-space-grid > :nth-child(6) {
    grid-column: span 8;
  }
}

@media (max-width: 900px) {
  .team-space-view {
    padding: 0 12px 16px;
  }

  .team-space-grid {
    grid-template-columns: 1fr;
  }

  .team-space-grid > * {
    grid-column: auto;
  }
}
</style>
