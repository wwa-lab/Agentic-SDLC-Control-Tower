<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { SectionResult } from '@/shared/types/section';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import { useProjectSpaceStore } from './stores/projectSpaceStore';
import { DEFAULT_PROJECT_ID, PROJECT_ID_PATTERN } from './types/enums';
import type { ProjectSummary } from './types/summary';
import type { LeadershipOwnership } from './types/leadership';
import type { SdlcChainState } from './types/chain';
import type { MilestoneHub } from './types/milestones';
import type { DependencyMap } from './types/dependencies';
import type { RiskRegistry } from './types/risks';
import type { EnvironmentMatrix } from './types/environments';
import ProjectSummaryBar from './components/ProjectSummaryBar.vue';
import LeadershipOwnershipCard from './components/LeadershipOwnershipCard.vue';
import SdlcDeepLinksCard from './components/SdlcDeepLinksCard.vue';
import MilestoneExecutionHubCard from './components/MilestoneExecutionHubCard.vue';
import OperationalDependencyMapCard from './components/OperationalDependencyMapCard.vue';
import RiskRegistryCard from './components/RiskRegistryCard.vue';
import EnvironmentMatrixCard from './components/EnvironmentMatrixCard.vue';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const projectSpaceStore = useProjectSpaceStore();

function emptySection<T>(): SectionResult<T> {
  return {
    data: null,
    error: null,
  };
}

const resolvedProjectId = computed(() => {
  const paramValue = route.params.projectId;
  return typeof paramValue === 'string' && paramValue.trim() ? paramValue : DEFAULT_PROJECT_ID;
});

const currentWorkspaceId = computed(() => {
  const queryValue = route.query.workspaceId;
  return typeof queryValue === 'string' && queryValue.trim()
    ? queryValue
    : projectSpaceStore.workspaceId ?? projectSpaceStore.aggregate?.workspaceId ?? null;
});

const isInvalidProjectId = computed(() => !PROJECT_ID_PATTERN.test(resolvedProjectId.value));

const summarySection = computed(() => projectSpaceStore.aggregate?.summary ?? emptySection<ProjectSummary>());
const leadershipSection = computed(() => projectSpaceStore.aggregate?.leadership ?? emptySection<LeadershipOwnership>());
const chainSection = computed(() => projectSpaceStore.aggregate?.chain ?? emptySection<SdlcChainState>());
const milestonesSection = computed(() => projectSpaceStore.aggregate?.milestones ?? emptySection<MilestoneHub>());
const dependenciesSection = computed(() => projectSpaceStore.aggregate?.dependencies ?? emptySection<DependencyMap>());
const risksSection = computed(() => projectSpaceStore.aggregate?.risks ?? emptySection<RiskRegistry>());
const environmentsSection = computed(() => projectSpaceStore.aggregate?.environments ?? emptySection<EnvironmentMatrix>());

async function ensureProjectRoute() {
  if (typeof route.params.projectId === 'string' && route.params.projectId.trim()) {
    return;
  }
  await router.replace({
    path: `/project-space/${DEFAULT_PROJECT_ID}`,
    query: route.query,
  });
}

function withWorkspaceContext(url: string): string {
  if (!currentWorkspaceId.value || !url.startsWith('/')) {
    return url;
  }
  const [path, rawQuery = ''] = url.split('?');
  const params = new URLSearchParams(rawQuery);
  if (!params.has('workspaceId')) {
    params.set('workspaceId', currentWorkspaceId.value);
  }
  const query = params.toString();
  return query ? `${path}?${query}` : path;
}

async function loadProject(nextProjectId: string) {
  if (!PROJECT_ID_PATTERN.test(nextProjectId)) {
    projectSpaceStore.reset();
    return;
  }
  await projectSpaceStore.switchProject(nextProjectId);
}

function navigate(url: string) {
  void router.push(withWorkspaceContext(url));
}

function retryCard(cardKey: Parameters<typeof projectSpaceStore.retryCard>[0]) {
  void projectSpaceStore.retryCard(cardKey);
}

watch(
  () => resolvedProjectId.value,
  projectId => {
    void loadProject(projectId);
  },
  { immediate: true },
);

watch(
  () => projectSpaceStore.aggregate,
  aggregate => {
    const summary = aggregate?.summary.data;
    workspaceStore.setRouteContext({
      workspace: summary?.workspaceName ?? workspaceStore.context.workspace,
      application: summary?.applicationName ?? workspaceStore.context.application,
      snowGroup: workspaceStore.context.snowGroup ?? null,
      project: summary?.name ?? resolvedProjectId.value,
      environment: null,
    });
    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      {
        label: `Team Space${currentWorkspaceId.value ? ` (${currentWorkspaceId.value})` : ''}`,
        path: summary?.teamSpaceLink ?? `/team?workspaceId=${currentWorkspaceId.value ?? ''}`,
      },
      { label: summary?.name ?? resolvedProjectId.value },
    ]);

    shellUiStore.setAiPanelContent({
      summary: summary
        ? `${summary.name} is ${summary.healthAggregate} with ${summary.counters.activeSpecs} active specs, ${summary.counters.openIncidents} open incidents, and ${summary.counters.criticalHighRisks} elevated risks.`
        : 'Project Space is loading project scope, milestone posture, and environment drift.',
      reasoning: [
        {
          text: `Project scope: ${resolvedProjectId.value}`,
          status: 'ok',
        },
        {
          text: summary?.activeMilestone
            ? `Active milestone: ${summary.activeMilestone.label} (${summary.activeMilestone.targetDate})`
            : 'No active milestone recorded',
          status: summary?.activeMilestone ? 'ok' : 'warning',
        },
        {
          text: risksSection.value.data?.total
            ? `${risksSection.value.data.total} project-scoped risks require review`
            : 'Risk registry clear or still loading',
          status: risksSection.value.data?.total ? 'warning' : 'ok',
        },
      ],
      evidence: JSON.stringify(
        {
          projectId: resolvedProjectId.value,
          workspaceId: projectSpaceStore.workspaceId,
          route: route.fullPath,
          loadedCards: aggregate
            ? Object.entries(aggregate)
                .filter(
                  ([key, value]) =>
                    key !== 'projectId' &&
                    key !== 'workspaceId' &&
                    typeof value === 'object' &&
                    value !== null &&
                    'data' in value &&
                    Boolean(value.data),
                )
                .map(([key]) => key)
            : [],
          healthFactors: summary?.healthFactors ?? [],
        },
        null,
        2,
      ),
    });
  },
  { immediate: true, deep: true },
);

onMounted(() => {
  void ensureProjectRoute();
  shellUiStore.setBreadcrumbs([
    { label: 'Dashboard', path: '/' },
    { label: 'Team Space', path: currentWorkspaceId.value ? `/team?workspaceId=${currentWorkspaceId.value}` : '/team' },
    { label: resolvedProjectId.value },
  ]);
});

onBeforeUnmount(() => {
  workspaceStore.clearRouteContext();
  shellUiStore.clearBreadcrumbs();
  shellUiStore.clearAiPanelContent();
  projectSpaceStore.reset();
});
</script>

<template>
  <div class="project-space-view">
    <div v-if="isInvalidProjectId" class="project-space-page-error section-high">
      <p class="text-label">Invalid Project ID</p>
      <p class="text-body-sm">
        Expected a project id that matches <span class="text-tech">proj-*</span>. The current route is
        <span class="text-tech">{{ resolvedProjectId }}</span>.
      </p>
      <button class="btn-machined" @click="router.push(`/project-space/${DEFAULT_PROJECT_ID}`)">Open Default Project</button>
    </div>

    <div
      v-else-if="projectSpaceStore.error && !projectSpaceStore.aggregate"
      class="project-space-page-error section-high"
    >
      <p class="text-label">
        {{
          projectSpaceStore.isForbidden
            ? 'Access Denied'
            : projectSpaceStore.isNotFound
              ? 'Project Not Found'
              : 'Project Space Unavailable'
        }}
      </p>
      <p class="text-body-sm">{{ projectSpaceStore.error }}</p>
      <div class="project-space-page-error__actions">
        <button class="btn-machined" @click="loadProject(resolvedProjectId)">Retry</button>
        <button
          class="btn-machined btn-secondary"
          @click="router.push(currentWorkspaceId ? `/team?workspaceId=${currentWorkspaceId}` : '/team')"
        >
          Back to Team Space
        </button>
      </div>
    </div>

    <template v-else>
      <ProjectSummaryBar
        :section="summarySection"
        :is-loading="projectSpaceStore.isLoading || projectSpaceStore.loadingCards.summary"
        @retry="retryCard('summary')"
        @open-link="navigate"
      />

      <div class="project-space-grid">
        <LeadershipOwnershipCard
          :section="leadershipSection"
          :is-loading="projectSpaceStore.isLoading || projectSpaceStore.loadingCards.leadership"
          @retry="retryCard('leadership')"
          @open-link="navigate"
        />
        <RiskRegistryCard
          :section="risksSection"
          :is-loading="projectSpaceStore.isLoading || projectSpaceStore.loadingCards.risks"
          @retry="retryCard('risks')"
          @open="navigate"
        />

        <div class="project-space-grid__full">
          <SdlcDeepLinksCard
            :section="chainSection"
            :is-loading="projectSpaceStore.isLoading || projectSpaceStore.loadingCards.chain"
            @retry="retryCard('chain')"
            @navigate="navigate"
          />
        </div>

        <MilestoneExecutionHubCard
          :section="milestonesSection"
          :is-loading="projectSpaceStore.isLoading || projectSpaceStore.loadingCards.milestones"
          @retry="retryCard('milestones')"
          @open-link="navigate"
        />
        <OperationalDependencyMapCard
          :section="dependenciesSection"
          :is-loading="projectSpaceStore.isLoading || projectSpaceStore.loadingCards.dependencies"
          @retry="retryCard('dependencies')"
          @open="navigate"
        />

        <div class="project-space-grid__full">
          <EnvironmentMatrixCard
            :section="environmentsSection"
            :is-loading="projectSpaceStore.isLoading || projectSpaceStore.loadingCards.environments"
            @retry="retryCard('environments')"
            @open="navigate"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.project-space-view {
  padding: 0 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.project-space-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.project-space-grid__full {
  grid-column: 1 / -1;
}

.project-space-page-error {
  border-radius: var(--radius-sm);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.project-space-page-error__actions {
  display: flex;
  gap: 10px;
}

.btn-secondary {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.14);
  color: var(--color-on-surface);
}

@media (max-width: 1200px) {
  .project-space-grid {
    grid-template-columns: 1fr;
  }

  .project-space-page-error__actions {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
