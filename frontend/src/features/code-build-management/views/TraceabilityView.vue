<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import BuildStatusBadge from '../components/BuildStatusBadge.vue';
import CodeBuildCardShell from '../components/CodeBuildCardShell.vue';
import ContextLens from '../components/ContextLens.vue';
import ShaPill from '../components/ShaPill.vue';
import StoryLinkStatusChip from '../components/StoryLinkStatusChip.vue';
import {
  TRACEABILITY_LINK_STATUS_OPTIONS,
  TRACEABILITY_STORY_STATE_OPTIONS,
} from '../types';
import { buildContextQuery, resolveViewerContextFromQuery, updateContextQuery } from '../routing';
import { useCodeBuildStore } from '../stores/codeBuildStore';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const codeBuildStore = useCodeBuildStore();

const viewerContext = computed(() => resolveViewerContextFromQuery(route.query));
const summarySection = computed(() => codeBuildStore.traceability?.summary ?? { data: null, error: null });
const storyRowsSection = computed(() => codeBuildStore.traceability?.storyRows ?? { data: null, error: null });
const unknownStorySection = computed(() => codeBuildStore.traceability?.unknownStory ?? { data: null, error: null });
const noStoryIdSection = computed(() => codeBuildStore.traceability?.noStoryId ?? { data: null, error: null });
const projectOptions = computed(() => (codeBuildStore.catalog?.filters.data?.projects ?? []));

function patchContext(next: Partial<typeof viewerContext.value>) {
  void updateContextQuery(router, route, {
    ...viewerContext.value,
    ...next,
  });
}

watch(
  () => viewerContext.value,
  context => {
    codeBuildStore.setViewerContext(context);
    void codeBuildStore.initCatalog();
    void codeBuildStore.initTraceability();
  },
  { immediate: true, deep: true },
);

watch(
  () => codeBuildStore.traceability,
  aggregate => {
    workspaceStore.setRouteContext({
      workspace: viewerContext.value.workspaceId,
      application: 'Code & Build Management',
      snowGroup: workspaceStore.context.snowGroup,
      project: 'Traceability',
      environment: null,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Code & Build', path: '/code-build-management' },
      { label: 'Traceability' },
    ]);

    shellUiStore.setAiPanelContent({
      summary: summarySection.value.data?.advisory ?? 'Traceability view is surfacing story, commit, and build linkage quality.',
      reasoning: [
        { text: `${summarySection.value.data?.known ?? 0} known story links currently resolved`, status: 'ok' },
        {
          text: `${summarySection.value.data?.unknownStory ?? 0} unknown story ids still need requirement follow-up`,
          status: (summarySection.value.data?.unknownStory ?? 0) > 0 ? 'warning' : 'ok',
        },
        {
          text: `${summarySection.value.data?.noStoryId ?? 0} protected-branch commits lack trailers`,
          status: (summarySection.value.data?.noStoryId ?? 0) > 0 ? 'warning' : 'ok',
        },
      ],
      evidence: JSON.stringify(
        {
          route: route.fullPath,
          filters: codeBuildStore.traceabilityFilters,
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
        <p class="text-label">Traceability</p>
        <h2>Story ↔ Commit ↔ Build</h2>
        <p class="cb-hero__copy">
          Keep resolved links, unknown story references, and commits without trailers visible in one audit-friendly workspace.
        </p>
      </div>
    </div>

    <ContextLens
      :context="viewerContext"
      @update-workspace="patchContext({ workspaceId: $event })"
      @update-role="patchContext({ role: $event })"
      @update-autonomy="patchContext({ autonomyLevel: $event })"
    />

    <CodeBuildCardShell
      title="Traceability Filters"
      subtitle="Project, story state, link status, and date range"
      :loading="codeBuildStore.traceabilityLoading"
    >
      <div class="filters-grid">
        <label class="filter-field">
          <span class="text-label">Project</span>
          <select :value="codeBuildStore.traceabilityFilters.projectId" @change="codeBuildStore.initTraceability({ projectId: ($event.target as HTMLSelectElement).value as any })">
            <option value="ALL">ALL</option>
            <option v-for="project in projectOptions" :key="project.id" :value="project.id">{{ project.name }}</option>
          </select>
        </label>

        <label class="filter-field">
          <span class="text-label">Story State</span>
          <select :value="codeBuildStore.traceabilityFilters.storyState" @change="codeBuildStore.initTraceability({ storyState: ($event.target as HTMLSelectElement).value as any })">
            <option v-for="state in TRACEABILITY_STORY_STATE_OPTIONS" :key="state" :value="state">{{ state }}</option>
          </select>
        </label>

        <label class="filter-field">
          <span class="text-label">Link Status</span>
          <select :value="codeBuildStore.traceabilityFilters.linkStatus" @change="codeBuildStore.initTraceability({ linkStatus: ($event.target as HTMLSelectElement).value as any })">
            <option v-for="status in TRACEABILITY_LINK_STATUS_OPTIONS" :key="status" :value="status">{{ status }}</option>
          </select>
        </label>

        <label class="filter-field">
          <span class="text-label">Range (days)</span>
          <select :value="codeBuildStore.traceabilityFilters.rangeDays" @change="codeBuildStore.initTraceability({ rangeDays: Number(($event.target as HTMLSelectElement).value) })">
            <option :value="7">7</option>
            <option :value="14">14</option>
            <option :value="30">30</option>
            <option :value="60">60</option>
          </select>
        </label>
      </div>
    </CodeBuildCardShell>

    <div v-if="codeBuildStore.traceabilityError && !codeBuildStore.traceability" class="cb-page-error">
      <p class="text-label">Traceability Unavailable</p>
      <p class="text-body-sm">{{ codeBuildStore.traceabilityError }}</p>
      <button class="btn-machined" @click="codeBuildStore.initTraceability()">Retry</button>
    </div>

    <template v-else>
      <div class="cb-grid">
        <CodeBuildCardShell
          title="Summary"
          subtitle="Bucket counts across visible traceability states"
          :loading="codeBuildStore.traceabilityLoading || codeBuildStore.traceabilityLoadingCards.summary"
          :error="summarySection.error"
          @retry="codeBuildStore.refreshTraceabilityCard('summary')"
        >
          <div v-if="summarySection.data" class="summary-grid">
            <div class="summary-metric">
              <span class="text-label">Known</span>
              <strong>{{ summarySection.data.known }}</strong>
            </div>
            <div class="summary-metric">
              <span class="text-label">Unknown</span>
              <strong>{{ summarySection.data.unknownStory }}</strong>
            </div>
            <div class="summary-metric">
              <span class="text-label">No Story Id</span>
              <strong>{{ summarySection.data.noStoryId }}</strong>
            </div>
            <div class="summary-metric">
              <span class="text-label">Ambiguous</span>
              <strong>{{ summarySection.data.ambiguous }}</strong>
            </div>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Story Rows"
          subtitle="Resolved stories expand into linked commits and builds"
          :loading="codeBuildStore.traceabilityLoading || codeBuildStore.traceabilityLoadingCards.storyRows"
          :error="storyRowsSection.error"
          :empty="!!storyRowsSection.data && storyRowsSection.data.length === 0"
          empty-message="No story-linked commits matched the current filters."
          @retry="codeBuildStore.refreshTraceabilityCard('storyRows')"
        >
          <div v-if="storyRowsSection.data" class="story-list">
            <details v-for="story in storyRowsSection.data" :key="story.storyId" class="story-row">
              <summary>
                <div class="story-row__summary">
                  <div>
                    <p class="text-label">{{ story.storyId }}</p>
                    <strong>{{ story.title }}</strong>
                  </div>
                  <div class="story-row__meta">
                    <span>{{ story.state }}</span>
                    <span>{{ story.commitCount }} commits</span>
                    <span>{{ story.buildCount }} builds</span>
                    <StoryLinkStatusChip :status="story.worstStatus" />
                  </div>
                </div>
              </summary>

              <div class="story-row__commits">
                <div v-for="commit in story.commits" :key="commit.commitId" class="commit-row">
                  <div class="commit-row__main">
                    <ShaPill :sha="commit.sha" :href="commit.githubUrl" />
                    <span>{{ commit.message }}</span>
                  </div>
                  <div class="commit-row__meta">
                    <span>{{ commit.author }}</span>
                    <span>{{ commit.repoLabel }}</span>
                    <StoryLinkStatusChip :status="commit.storyLinkStatus" />
                  </div>
                  <div class="build-ref-list">
                    <div v-for="build in commit.builds" :key="build.runId" class="build-ref">
                      <BuildStatusBadge :status="build.status" />
                      <span>#{{ build.runNumber }}</span>
                      <span>{{ build.workflowName }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </details>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Unknown Story IDs"
          subtitle="Commits that reference unresolved stories"
          :loading="codeBuildStore.traceabilityLoading || codeBuildStore.traceabilityLoadingCards.unknownStory"
          :error="unknownStorySection.error"
          :empty="!!unknownStorySection.data && unknownStorySection.data.length === 0"
          empty-message="No unresolved story ids matched the current filters."
          @retry="codeBuildStore.refreshTraceabilityCard('unknownStory')"
        >
          <div v-if="unknownStorySection.data" class="story-list">
            <div v-for="row in unknownStorySection.data" :key="row.commitId" class="commit-row">
              <div class="commit-row__main">
                <strong>{{ row.storyId }}</strong>
                <span>{{ row.message }}</span>
              </div>
              <div class="commit-row__meta">
                <span>{{ row.repoLabel }}</span>
                <span>{{ row.ageDays }}d old</span>
                <a :href="row.requirementSearchUrl" class="external-link">Open in Requirement slice ↗</a>
              </div>
            </div>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="No Story ID"
          subtitle="Protected-branch commits that shipped without trailers"
          :loading="codeBuildStore.traceabilityLoading || codeBuildStore.traceabilityLoadingCards.noStoryId"
          :error="noStoryIdSection.error"
          :empty="!!noStoryIdSection.data && noStoryIdSection.data.length === 0"
          empty-message="Every protected-branch commit matched by the current filters carries a story trailer."
          @retry="codeBuildStore.refreshTraceabilityCard('noStoryId')"
        >
          <div v-if="noStoryIdSection.data" class="story-list">
            <div v-for="row in noStoryIdSection.data" :key="row.commitId" class="commit-row">
              <div class="commit-row__main">
                <ShaPill :sha="row.sha" :href="row.githubUrl" />
                <span>{{ row.message }}</span>
              </div>
              <div class="commit-row__meta">
                <span>{{ row.author }}</span>
                <span>{{ row.branch }}</span>
                <span>{{ row.repoLabel }}</span>
              </div>
            </div>
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
  margin-top: 8px;
  max-width: 720px;
  color: var(--color-on-surface-variant);
  line-height: 1.6;
}

.cb-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 16px;
}

.cb-grid > * {
  grid-column: span 12;
}

.filters-grid,
.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

select {
  width: 100%;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  color: var(--color-on-surface);
  padding: 9px 10px;
}

.summary-metric,
.story-row,
.commit-row {
  padding: 12px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.02);
  border: var(--border-ghost);
}

.story-list,
.story-row__commits,
.build-ref-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.story-row__summary,
.story-row__meta,
.commit-row__main,
.commit-row__meta,
.build-ref {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.story-row__summary {
  justify-content: space-between;
}

.commit-row__meta {
  margin-top: 8px;
  color: var(--color-on-surface-variant);
  font-size: 0.78rem;
}

.external-link {
  color: var(--color-secondary);
  text-decoration: none;
}

.cb-page-error {
  padding: 18px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(255, 180, 171, 0.18);
  background: rgba(255, 180, 171, 0.08);
}

@media (max-width: 900px) {
  .cb-hero {
    flex-direction: column;
  }
}
</style>

