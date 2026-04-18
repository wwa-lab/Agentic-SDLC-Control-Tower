<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import CodeBuildCardShell from '../components/CodeBuildCardShell.vue';
import ContextLens from '../components/ContextLens.vue';
import BuildStatusBadge from '../components/BuildStatusBadge.vue';
import BuildHealthSparkline from '../components/BuildHealthSparkline.vue';
import RepoNavChip from '../components/RepoNavChip.vue';
import { buildContextQuery, resolveViewerContextFromQuery, updateContextQuery } from '../routing';
import { useCodeBuildStore } from '../stores/codeBuildStore';
import { formatRelativeTime } from '../utils';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const codeBuildStore = useCodeBuildStore();

const viewerContext = computed(() => resolveViewerContextFromQuery(route.query));
const summarySection = computed(() => codeBuildStore.catalog?.summary ?? { data: null, error: null });
const filtersSection = computed(() => codeBuildStore.catalog?.filters ?? { data: null, error: null });
const gridSection = computed(() => codeBuildStore.catalog?.grid ?? { data: null, error: null });

const emptyGridMessage = computed(() => {
  if (codeBuildStore.catalogFilters.search.trim() || codeBuildStore.catalogFilters.buildStatus !== 'ALL' || codeBuildStore.catalogFilters.projectId !== 'ALL' || codeBuildStore.catalogFilters.visibility !== 'ALL') {
    return 'No repos match your filters.';
  }
  return 'No repos in this workspace yet.';
});

function patchContext(next: Partial<typeof viewerContext.value>) {
  void updateContextQuery(router, route, {
    ...viewerContext.value,
    ...next,
  });
}

function openRepo(repoId: string) {
  void router.push({
    name: 'code-build-management-repo',
    params: { repoId },
    query: buildContextQuery(route, viewerContext.value),
  });
}

function openRun(runId: string) {
  void router.push({
    name: 'code-build-management-run',
    params: { runId },
    query: buildContextQuery(route, viewerContext.value),
  });
}

function openTraceability() {
  void router.push({
    name: 'code-build-management-traceability',
    query: buildContextQuery(route, viewerContext.value),
  });
}

watch(
  () => viewerContext.value,
  context => {
    codeBuildStore.setViewerContext(context);
    void codeBuildStore.initCatalog();
  },
  { immediate: true, deep: true },
);

watch(
  () => codeBuildStore.catalog,
  aggregate => {
    workspaceStore.setRouteContext({
      workspace: summarySection.value.data?.workspaceId === 'ws-edge-002' ? 'Edge Release Lab' : 'Global SDLC Tower',
      application: 'Code & Build Management',
      snowGroup: workspaceStore.context.snowGroup,
      project: null,
      environment: null,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Code & Build', path: '/code-build-management' },
      { label: viewerContext.value.workspaceId },
    ]);

    shellUiStore.setAiPanelContent({
      summary: summarySection.value.data?.advisory ?? 'Cataloging repository health, PR readiness, and build activity across the current workspace.',
      reasoning: [
        { text: `${summarySection.value.data?.totalRepos ?? 0} repos visible in ${viewerContext.value.workspaceId}`, status: 'ok' },
        {
          text: `${summarySection.value.data?.failureRepos ?? 0} repos currently failing on the default branch`,
          status: (summarySection.value.data?.failureRepos ?? 0) > 0 ? 'warning' : 'ok',
        },
        {
          text: `${summarySection.value.data?.reposWithoutRuns ?? 0} repos still have no build history`,
          status: (summarySection.value.data?.reposWithoutRuns ?? 0) > 0 ? 'warning' : 'ok',
        },
      ],
      evidence: JSON.stringify(
        {
          route: route.fullPath,
          workspaceId: viewerContext.value.workspaceId,
          cards: aggregate ? Object.keys(aggregate) : [],
        },
        null,
        2,
      ),
    });
  },
  { immediate: true, deep: true },
);

onBeforeUnmount(() => {
  workspaceStore.clearRouteContext();
  shellUiStore.clearBreadcrumbs();
  shellUiStore.clearAiPanelContent();
});
</script>

<template>
  <div class="cb-page">
    <div class="cb-hero">
      <div>
        <p class="text-label">Repository Catalog</p>
        <h2>Code &amp; Build Management</h2>
        <p class="cb-hero__copy">
          Browse repository health, open PR pressure, running workflows, and cross-story traceability without leaving the shared shell.
        </p>
      </div>
      <div class="cb-hero__actions">
        <button class="btn-machined" @click="openTraceability">Open Traceability</button>
      </div>
    </div>

    <ContextLens
      :context="viewerContext"
      @update-workspace="patchContext({ workspaceId: $event })"
      @update-role="patchContext({ role: $event })"
      @update-autonomy="patchContext({ autonomyLevel: $event })"
    />

    <div v-if="codeBuildStore.catalogError && !codeBuildStore.catalog" class="cb-page-error">
      <p class="text-label">Catalog Unavailable</p>
      <p class="text-body-sm">{{ codeBuildStore.catalogError }}</p>
      <button class="btn-machined" @click="codeBuildStore.initCatalog()">Retry</button>
    </div>

    <template v-else>
      <div class="cb-grid">
        <CodeBuildCardShell
          title="Workspace Summary"
          subtitle="Default-branch health across visible repositories"
          :loading="codeBuildStore.catalogLoading || codeBuildStore.catalogLoadingCards.summary"
          :error="summarySection.error"
          @retry="codeBuildStore.refreshCatalogCard('summary')"
        >
          <div v-if="summarySection.data" class="summary-grid">
            <div class="summary-metric">
              <span class="text-label">Repos</span>
              <strong>{{ summarySection.data.totalRepos }}</strong>
            </div>
            <div class="summary-metric">
              <span class="text-label">Success</span>
              <strong>{{ summarySection.data.successRepos }}</strong>
            </div>
            <div class="summary-metric">
              <span class="text-label">Failure</span>
              <strong class="cb-danger">{{ summarySection.data.failureRepos }}</strong>
            </div>
            <div class="summary-metric">
              <span class="text-label">Running</span>
              <strong>{{ summarySection.data.runningRepos + summarySection.data.queuedRepos }}</strong>
            </div>
            <div class="summary-metric">
              <span class="text-label">No Runs</span>
              <strong>{{ summarySection.data.reposWithoutRuns }}</strong>
            </div>
            <div class="summary-metric summary-metric--wide">
              <span class="text-label">Advisory</span>
              <strong>{{ summarySection.data.advisory }}</strong>
            </div>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Catalog Filters"
          subtitle="Workspace-local filters persist while you drill into detail routes"
          :loading="codeBuildStore.catalogLoading || codeBuildStore.catalogLoadingCards.filters"
          :error="filtersSection.error"
          @retry="codeBuildStore.refreshCatalogCard('filters')"
        >
          <div v-if="filtersSection.data" class="filters-grid">
            <label class="filter-field">
              <span class="text-label">Search</span>
              <input
                :value="codeBuildStore.catalogFilters.search"
                type="text"
                placeholder="owner/repo or project"
                @input="codeBuildStore.initCatalog({ search: ($event.target as HTMLInputElement).value })"
              />
            </label>

            <label class="filter-field">
              <span class="text-label">Build Status</span>
              <select :value="codeBuildStore.catalogFilters.buildStatus" @change="codeBuildStore.initCatalog({ buildStatus: ($event.target as HTMLSelectElement).value as any })">
                <option v-for="status in filtersSection.data.statuses" :key="status" :value="status">{{ status }}</option>
              </select>
            </label>

            <label class="filter-field">
              <span class="text-label">Visibility</span>
              <select :value="codeBuildStore.catalogFilters.visibility" @change="codeBuildStore.initCatalog({ visibility: ($event.target as HTMLSelectElement).value as any })">
                <option v-for="visibility in filtersSection.data.visibilities" :key="visibility" :value="visibility">{{ visibility }}</option>
              </select>
            </label>

            <label class="filter-field">
              <span class="text-label">Project</span>
              <select :value="codeBuildStore.catalogFilters.projectId" @change="codeBuildStore.initCatalog({ projectId: ($event.target as HTMLSelectElement).value as any })">
                <option value="ALL">ALL</option>
                <option v-for="project in filtersSection.data.projects" :key="project.id" :value="project.id">{{ project.name }}</option>
              </select>
            </label>

            <label class="filter-field">
              <span class="text-label">Sort</span>
              <select :value="codeBuildStore.catalogFilters.sort" @change="codeBuildStore.initCatalog({ sort: ($event.target as HTMLSelectElement).value as any })">
                <option v-for="sort in filtersSection.data.sorts" :key="sort" :value="sort">{{ sort }}</option>
              </select>
            </label>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Repository Grid"
          subtitle="Grouped by project, with drilldown into repo, run, and PR detail"
          :loading="codeBuildStore.catalogLoading || codeBuildStore.catalogLoadingCards.grid"
          :error="gridSection.error"
          :empty="!!gridSection.data && gridSection.data.length === 0"
          :empty-message="emptyGridMessage"
          @retry="codeBuildStore.refreshCatalogCard('grid')"
        >
          <div v-if="gridSection.data" class="catalog-sections">
            <section v-for="section in gridSection.data" :key="section.projectId" class="project-section">
              <div class="project-section__header">
                <div>
                  <p class="text-label">{{ section.projectId }}</p>
                  <h3>{{ section.projectName }}</h3>
                </div>
                <span class="text-body-sm">{{ section.repos.length }} repos</span>
              </div>

              <div class="repo-grid">
                <article v-for="repo in section.repos" :key="repo.repoId" class="repo-tile">
                  <div class="repo-tile__top">
                    <RepoNavChip :label="`${repo.owner}/${repo.name}`" :href="repo.githubUrl" />
                    <BuildStatusBadge :status="repo.defaultBranchStatus" />
                  </div>

                  <BuildHealthSparkline :points="repo.sparkline" @open-run="repo.hasRuns ? openRun($event) : undefined" />

                  <div class="repo-tile__meta">
                    <p class="text-body-sm">Default branch: <strong>{{ repo.defaultBranch }}</strong></p>
                    <p class="text-body-sm">Last activity {{ formatRelativeTime(repo.lastActivityAt ?? repo.lastSyncedAt) }}</p>
                  </div>

                  <button class="btn-machined repo-tile__action" @click="openRepo(repo.repoId)">Open Repo</button>
                </article>
              </div>
            </section>
          </div>
        </CodeBuildCardShell>
      </div>
    </template>
  </div>
</template>

<style scoped>
.cb-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.cb-hero {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 22px;
  border-radius: var(--radius-md);
  background:
    radial-gradient(circle at top right, rgba(137, 206, 255, 0.16), transparent 32%),
    linear-gradient(135deg, rgba(19, 27, 46, 0.96), rgba(34, 42, 61, 0.92));
  border: var(--border-ghost);
}

.cb-hero__copy {
  max-width: 720px;
  margin-top: 8px;
  color: var(--color-on-surface-variant);
  line-height: 1.6;
}

.cb-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 16px;
}

.cb-grid > :nth-child(1),
.cb-grid > :nth-child(3) {
  grid-column: span 12;
}

.cb-grid > :nth-child(2) {
  grid-column: span 12;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
  gap: 12px;
}

.summary-metric {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.03);
}

.summary-metric strong {
  font-size: 1.5rem;
}

.summary-metric--wide {
  grid-column: span 2;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

input,
select {
  width: 100%;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  color: var(--color-on-surface);
  padding: 9px 10px;
}

.catalog-sections {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.project-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.project-section__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.repo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px;
}

.repo-tile {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
  background: rgba(255, 255, 255, 0.02);
}

.repo-tile__top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.repo-tile__meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.repo-tile__action {
  align-self: flex-start;
}

.cb-page-error {
  padding: 18px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(255, 180, 171, 0.18);
  background: rgba(255, 180, 171, 0.08);
}

.cb-danger {
  color: var(--cb-status-failure);
}

@media (max-width: 900px) {
  .cb-hero {
    flex-direction: column;
  }

  .summary-metric--wide {
    grid-column: span 1;
  }
}
</style>
