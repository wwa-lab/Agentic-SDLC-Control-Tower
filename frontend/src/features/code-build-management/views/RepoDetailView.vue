<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import LineageBadge from '@/shared/components/LineageBadge.vue';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import BranchPill from '../components/BranchPill.vue';
import BuildHealthSparkline from '../components/BuildHealthSparkline.vue';
import BuildStatusBadge from '../components/BuildStatusBadge.vue';
import CodeBuildCardShell from '../components/CodeBuildCardShell.vue';
import ContextLens from '../components/ContextLens.vue';
import DurationLabel from '../components/DurationLabel.vue';
import RepoNavChip from '../components/RepoNavChip.vue';
import ShaPill from '../components/ShaPill.vue';
import StoryLinkStatusChip from '../components/StoryLinkStatusChip.vue';
import WebhookStaleBanner from '../components/WebhookStaleBanner.vue';
import { buildContextQuery, resolveViewerContextFromQuery, updateContextQuery } from '../routing';
import { useCodeBuildStore } from '../stores/codeBuildStore';
import { autonomyMeetsMinimum, formatRelativeTime, isStaleSync } from '../utils';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const codeBuildStore = useCodeBuildStore();
const repoRefreshReason = ref('');

const viewerContext = computed(() => resolveViewerContextFromQuery(route.query));
const resolvedRepoId = computed(() => typeof route.params.repoId === 'string' ? route.params.repoId : 'repo-1');
const headerSection = computed(() => codeBuildStore.repo?.header ?? { data: null, error: null });
const runsSection = computed(() => codeBuildStore.repo?.recentRuns ?? { data: null, error: null });
const prsSection = computed(() => codeBuildStore.repo?.recentPrs ?? { data: null, error: null });
const commitsSection = computed(() => codeBuildStore.repo?.recentCommits ?? { data: null, error: null });
const branchesSection = computed(() => codeBuildStore.repo?.branches ?? { data: null, error: null });
const healthSection = computed(() => codeBuildStore.repo?.healthSummary ?? { data: null, error: null });
const aiSummarySection = computed(() => codeBuildStore.repo?.aiSummary ?? { data: null, error: null });

const showStaleBanner = computed(() => {
  const latestRun = runsSection.value.data?.[0];
  const header = headerSection.value.data;
  return !!header && !!latestRun && ['RUNNING', 'QUEUED'].includes(latestRun.status) && isStaleSync(header.lastSyncedAt);
});

function patchContext(next: Partial<typeof viewerContext.value>) {
  void updateContextQuery(router, route, {
    ...viewerContext.value,
    ...next,
  });
}

function openCatalog() {
  void router.push({
    name: 'code-build-management',
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

function openPr(prId: string) {
  void router.push({
    name: 'code-build-management-pr',
    params: { prId },
    query: buildContextQuery(route, viewerContext.value),
  });
}

watch(
  () => [viewerContext.value, resolvedRepoId.value],
  ([context, nextRepoId]) => {
    codeBuildStore.setViewerContext(context as typeof viewerContext.value);
    void codeBuildStore.openRepo(nextRepoId as string);
  },
  { immediate: true, deep: true },
);

watch(
  () => codeBuildStore.repo,
  aggregate => {
    const header = aggregate?.header.data;
    if (!header) {
      return;
    }
    workspaceStore.setRouteContext({
      workspace: header.workspaceName,
      application: 'Code & Build Management',
      snowGroup: workspaceStore.context.snowGroup,
      project: header.projectName,
      environment: header.defaultBranch,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Code & Build', path: '/code-build-management' },
      { label: `${header.owner}/${header.name}` },
    ]);

    shellUiStore.setAiPanelContent({
      summary: aggregate?.aiSummary.data?.summary ?? 'Inspecting repository health, branch protection, and recent execution flow.',
      reasoning: [
        { text: `${aggregate?.recentRuns.data?.length ?? 0} recent runs loaded`, status: 'ok' },
        {
          text: aggregate?.healthSummary.data?.advisory ?? 'Health summary pending',
          status: aggregate?.healthSummary.data?.successRate && aggregate.healthSummary.data.successRate < 80 ? 'warning' : 'ok',
        },
        {
          text: showStaleBanner.value ? 'Webhook data may be stale for a non-terminal run' : 'Webhook freshness looks healthy',
          status: showStaleBanner.value ? 'warning' : 'ok',
        },
      ],
      evidence: JSON.stringify(
        {
          route: route.fullPath,
          repoId: resolvedRepoId.value,
          workspaceId: viewerContext.value.workspaceId,
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
        <p class="text-label">Repository Detail</p>
        <h2>{{ headerSection.data ? `${headerSection.data.owner}/${headerSection.data.name}` : resolvedRepoId }}</h2>
        <p class="cb-hero__copy">
          Review branches, recent PRs, default-branch commits, and the workspace-level AI summary without leaving the drilldown flow.
        </p>
      </div>
      <div class="cb-hero__actions">
        <button class="btn-machined" @click="openCatalog">Back to Catalog</button>
      </div>
    </div>

    <ContextLens
      :context="viewerContext"
      @update-workspace="patchContext({ workspaceId: $event })"
      @update-role="patchContext({ role: $event })"
      @update-autonomy="patchContext({ autonomyLevel: $event })"
    />

    <div v-if="codeBuildStore.repoError && !codeBuildStore.repo" class="cb-page-error">
      <p class="text-label">
        {{ codeBuildStore.isRepoForbidden ? 'Access Denied' : codeBuildStore.isRepoNotFound ? 'Repository Not Found' : 'Repository Unavailable' }}
      </p>
      <p class="text-body-sm">{{ codeBuildStore.repoError }}</p>
      <button class="btn-machined" @click="codeBuildStore.openRepo(resolvedRepoId)">Retry</button>
    </div>

    <template v-else>
      <div class="cb-grid">
        <CodeBuildCardShell
          title="Repo Header"
          subtitle="Identity, workspace ownership, and sync freshness"
          :loading="codeBuildStore.repoLoading || codeBuildStore.repoLoadingCards.header"
          :error="headerSection.error"
          @retry="codeBuildStore.refreshRepoCard('header')"
        >
          <div v-if="headerSection.data" class="header-stack">
            <div class="header-stack__top">
              <RepoNavChip :label="`${headerSection.data.owner}/${headerSection.data.name}`" :href="headerSection.data.githubUrl" />
              <div class="header-meta">
                <span class="repo-visibility">{{ headerSection.data.visibility }}</span>
                <BranchPill :branch="headerSection.data.defaultBranch" :is-default="true" />
              </div>
            </div>

            <div class="header-meta-grid">
              <div>
                <p class="text-label">Workspace</p>
                <strong>{{ headerSection.data.workspaceName }}</strong>
              </div>
              <div>
                <p class="text-label">Project</p>
                <strong>{{ headerSection.data.projectName }}</strong>
              </div>
              <div>
                <p class="text-label">Last Sync</p>
                <strong>{{ formatRelativeTime(headerSection.data.lastSyncedAt) }}</strong>
              </div>
              <div>
                <p class="text-label">Protection</p>
                <strong>{{ headerSection.data.defaultBranchProtection }}</strong>
              </div>
            </div>

            <WebhookStaleBanner v-if="showStaleBanner" @refresh="codeBuildStore.refreshRepoCard('header')" />
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Recent Runs"
          subtitle="Workflow-level execution on this repository"
          :loading="codeBuildStore.repoLoading || codeBuildStore.repoLoadingCards.recentRuns"
          :error="runsSection.error"
          :empty="!!runsSection.data && runsSection.data.length === 0"
          empty-message="No runs have been observed for this repository."
          @retry="codeBuildStore.refreshRepoCard('recentRuns')"
        >
          <div v-if="runsSection.data" class="stacked-list">
            <button v-for="entry in runsSection.data" :key="entry.runId" class="list-row" @click="openRun(entry.runId)">
              <div class="list-row__main">
                <BuildStatusBadge :status="entry.status" />
                <strong>{{ entry.workflowName }}</strong>
                <BranchPill :branch="entry.branch" />
                <ShaPill :sha="entry.sha" />
              </div>
              <div class="list-row__meta">
                <span>{{ entry.trigger }}</span>
                <DurationLabel :duration-seconds="entry.durationSeconds" :timestamp="entry.updatedAt" />
                <span>{{ entry.actor }}</span>
              </div>
            </button>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Recent PRs"
          subtitle="Open and recently closed pull requests with AI note counts"
          :loading="codeBuildStore.repoLoading || codeBuildStore.repoLoadingCards.recentPrs"
          :error="prsSection.error"
          :empty="!!prsSection.data && prsSection.data.length === 0"
          empty-message="No pull requests are currently linked to this repository."
          @retry="codeBuildStore.refreshRepoCard('recentPrs')"
        >
          <div v-if="prsSection.data" class="stacked-list">
            <button v-for="entry in prsSection.data" :key="entry.prId" class="list-row" @click="openPr(entry.prId)">
              <div class="list-row__main">
                <strong>#{{ entry.number }}</strong>
                <span>{{ entry.title }}</span>
              </div>
              <div class="list-row__meta">
                <span>{{ entry.author }}</span>
                <span>{{ entry.baseBranch }} → {{ entry.headBranch }}</span>
                <span>B {{ entry.aiCounts.blocker }} / M {{ entry.aiCounts.major }} / m {{ entry.aiCounts.minor }}</span>
              </div>
            </button>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Recent Commits"
          subtitle="Default-branch commit lineage and story-link status"
          :loading="codeBuildStore.repoLoading || codeBuildStore.repoLoadingCards.recentCommits"
          :error="commitsSection.error"
          :empty="!!commitsSection.data && commitsSection.data.length === 0"
          empty-message="No commits are available for the default branch history."
          @retry="codeBuildStore.refreshRepoCard('recentCommits')"
        >
          <div v-if="commitsSection.data" class="stacked-list">
            <div v-for="entry in commitsSection.data" :key="entry.commitId" class="list-row list-row--static">
              <div class="list-row__main">
                <ShaPill :sha="entry.sha" :href="entry.githubUrl" />
                <span>{{ entry.message }}</span>
              </div>
              <div class="list-row__meta">
                <span>{{ entry.author }}</span>
                <StoryLinkStatusChip :status="entry.storyLinkStatus" />
              </div>
            </div>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Branches"
          subtitle="Protected branch inventory and observed head states"
          :loading="codeBuildStore.repoLoading || codeBuildStore.repoLoadingCards.branches"
          :error="branchesSection.error"
          @retry="codeBuildStore.refreshRepoCard('branches')"
        >
          <div v-if="branchesSection.data" class="stacked-list">
            <div v-for="branch in branchesSection.data" :key="branch.branch" class="list-row list-row--static">
              <div class="list-row__main">
                <BranchPill :branch="branch.branch" :is-default="branch.isDefault" />
                <ShaPill :sha="branch.headSha" />
              </div>
              <div class="list-row__meta">
                <BuildStatusBadge :status="branch.lastRunStatus" />
                <span>{{ branch.protected ? 'Protected' : 'Open' }}</span>
              </div>
            </div>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Health Summary"
          subtitle="Trailing 14-run reliability and failure concentration"
          :loading="codeBuildStore.repoLoading || codeBuildStore.repoLoadingCards.healthSummary"
          :error="healthSection.error"
          @retry="codeBuildStore.refreshRepoCard('healthSummary')"
        >
          <div v-if="healthSection.data" class="health-grid">
            <div class="summary-metric">
              <span class="text-label">Success Rate</span>
              <strong>{{ healthSection.data.successRate }}%</strong>
            </div>
            <div class="summary-metric">
              <span class="text-label">Median Duration</span>
              <strong>{{ healthSection.data.medianDurationSeconds }}s</strong>
            </div>
            <div class="summary-metric summary-metric--wide">
              <span class="text-label">Sparkline</span>
              <BuildHealthSparkline :points="healthSection.data.last14Runs" @open-run="openRun" />
            </div>
            <div class="summary-metric summary-metric--wide">
              <span class="text-label">Failing Workflows</span>
              <div class="failing-workflows">
                <span v-for="workflow in healthSection.data.failingWorkflows" :key="workflow.workflowName">
                  {{ workflow.workflowName }} ({{ workflow.failures }})
                </span>
              </div>
            </div>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="AI Summary"
          subtitle="Workspace-level summary scoped to this repository"
          :loading="codeBuildStore.repoLoading || codeBuildStore.repoLoadingCards.aiSummary"
          :error="aiSummarySection.error"
          @retry="codeBuildStore.refreshRepoCard('aiSummary')"
        >
          <template #actions>
            <input
              v-if="codeBuildStore.canManage"
              v-model="repoRefreshReason"
              class="reason-input"
              type="text"
              placeholder="Optional note"
            />
            <button
              v-if="codeBuildStore.canManage"
              class="btn-machined"
              :disabled="!autonomyMeetsMinimum(viewerContext.autonomyLevel, 'SUPERVISED')"
              :title="autonomyMeetsMinimum(viewerContext.autonomyLevel, 'SUPERVISED') ? 'Refresh summary card' : 'Workspace autonomy must be SUPERVISED or above.'"
              @click="codeBuildStore.refreshRepoCard('aiSummary')"
            >
              Refresh Summary
            </button>
          </template>

          <div v-if="aiSummarySection.data" class="ai-summary">
            <p class="text-body-sm">{{ aiSummarySection.data.summary }}</p>
            <p class="text-body-sm ai-summary__delta">{{ aiSummarySection.data.deltaSummary }}</p>
            <LineageBadge :lineage="aiSummarySection.data.lineage" />
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
  grid-column: span 6;
}

.cb-grid > :first-child,
.cb-grid > :last-child {
  grid-column: span 12;
}

.header-stack,
.ai-summary {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.header-stack__top,
.header-meta,
.header-meta-grid,
.list-row,
.list-row__main,
.list-row__meta,
.failing-workflows {
  display: flex;
}

.header-stack__top,
.list-row {
  justify-content: space-between;
  gap: 12px;
}

.header-meta,
.list-row__main,
.list-row__meta,
.failing-workflows {
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.header-meta-grid {
  flex-wrap: wrap;
  gap: 14px;
}

.repo-visibility {
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.05);
  font-size: 0.75rem;
}

.stacked-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.list-row {
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.02);
  padding: 12px;
  text-align: left;
}

.list-row--static {
  cursor: default;
}

.list-row__meta {
  color: var(--color-on-surface-variant);
  font-size: 0.78rem;
  justify-content: flex-end;
}

.summary-metric {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.03);
}

.summary-metric--wide {
  gap: 10px;
}

.health-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.failing-workflows {
  flex-direction: column;
  align-items: flex-start;
}

.ai-summary__delta {
  color: var(--color-secondary);
}

.reason-input {
  min-width: 180px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  color: var(--color-on-surface);
  padding: 8px 10px;
}

.cb-page-error {
  padding: 18px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(255, 180, 171, 0.18);
  background: rgba(255, 180, 171, 0.08);
}

@media (max-width: 1100px) {
  .cb-grid > * {
    grid-column: span 12;
  }

  .cb-hero {
    flex-direction: column;
  }
}
</style>
