<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import LineageBadge from '@/shared/components/LineageBadge.vue';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import AiTriageRowCard from '../components/AiTriageRowCard.vue';
import BranchPill from '../components/BranchPill.vue';
import BuildStatusBadge from '../components/BuildStatusBadge.vue';
import CodeBuildCardShell from '../components/CodeBuildCardShell.vue';
import ContextLens from '../components/ContextLens.vue';
import DurationLabel from '../components/DurationLabel.vue';
import LogExcerptBlockView from '../components/LogExcerptBlock.vue';
import RateLimitBanner from '../components/RateLimitBanner.vue';
import ShaPill from '../components/ShaPill.vue';
import StoryLinkStatusChip from '../components/StoryLinkStatusChip.vue';
import WebhookStaleBanner from '../components/WebhookStaleBanner.vue';
import { buildContextQuery, resolveViewerContextFromQuery, updateContextQuery } from '../routing';
import { useCodeBuildStore } from '../stores/codeBuildStore';
import { autonomyMeetsMinimum, isStaleSync } from '../utils';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const codeBuildStore = useCodeBuildStore();
const triageReason = ref('');
const rerunReason = ref('');

const viewerContext = computed(() => resolveViewerContextFromQuery(route.query));
const resolvedRunId = computed(() => typeof route.params.runId === 'string' ? route.params.runId : 'run-991');
const headerSection = computed(() => codeBuildStore.run?.header ?? { data: null, error: null });
const jobsSection = computed(() => codeBuildStore.run?.jobs ?? { data: null, error: null });
const stepsSection = computed(() => codeBuildStore.run?.steps ?? { data: null, error: null });
const logsSection = computed(() => codeBuildStore.run?.logs ?? { data: null, error: null });
const triageSection = computed(() => codeBuildStore.run?.aiTriage ?? { data: null, error: null });
const rerunSection = computed(() => codeBuildStore.run?.rerun ?? { data: null, error: null });

const showStaleBanner = computed(() => {
  const header = headerSection.value.data;
  return !!header && ['RUNNING', 'QUEUED'].includes(header.status) && isStaleSync(header.updatedAt);
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

watch(
  () => [viewerContext.value, resolvedRunId.value],
  ([context, nextRunId]) => {
    codeBuildStore.setViewerContext(context as typeof viewerContext.value);
    void codeBuildStore.openRun(nextRunId as string);
  },
  { immediate: true, deep: true },
);

watch(
  () => codeBuildStore.run,
  aggregate => {
    const header = aggregate?.header.data;
    if (!header) {
      return;
    }
    workspaceStore.setRouteContext({
      workspace: header.workspaceId,
      application: 'Code & Build Management',
      snowGroup: workspaceStore.context.snowGroup,
      project: header.repoLabel,
      environment: header.branch,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Code & Build', path: '/code-build-management' },
      { label: `Run #${header.runNumber}` },
    ]);

    shellUiStore.setAiPanelContent({
      summary: aggregate?.aiTriage.data?.summary ?? 'Run detail highlights jobs, step logs, and AI triage output.',
      reasoning: [
        { text: `${aggregate?.jobs.data?.length ?? 0} jobs observed for this run`, status: 'ok' },
        {
          text: `${aggregate?.aiTriage.data?.rows.length ?? 0} triage rows currently available`,
          status: (aggregate?.aiTriage.data?.rows.some(row => row.status === 'FAILED_EVIDENCE')) ? 'warning' : 'ok',
        },
        {
          text: showStaleBanner.value ? 'Webhook freshness warning is active' : 'Webhook freshness looks healthy',
          status: showStaleBanner.value ? 'warning' : 'ok',
        },
      ],
      evidence: JSON.stringify(
        {
          route: route.fullPath,
          runId: resolvedRunId.value,
          status: header.status,
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
        <p class="text-label">Run Detail</p>
        <h2>{{ headerSection.data ? `Run #${headerSection.data.runNumber}` : resolvedRunId }}</h2>
        <p class="cb-hero__copy">
          Inspect workflow, jobs, steps, redacted logs, and AI-assisted triage from the same execution envelope.
        </p>
      </div>
      <div class="cb-hero__actions">
        <button v-if="headerSection.data" class="btn-machined" @click="openRepo(headerSection.data.repoId)">Back to Repo</button>
      </div>
    </div>

    <ContextLens
      :context="viewerContext"
      @update-workspace="patchContext({ workspaceId: $event })"
      @update-role="patchContext({ role: $event })"
      @update-autonomy="patchContext({ autonomyLevel: $event })"
    />

    <div v-if="codeBuildStore.runError && !codeBuildStore.run" class="cb-page-error">
      <p class="text-label">
        {{ codeBuildStore.isRunForbidden ? 'Access Denied' : codeBuildStore.isRunNotFound ? 'Run Not Found' : 'Run Unavailable' }}
      </p>
      <p class="text-body-sm">{{ codeBuildStore.runError }}</p>
      <button class="btn-machined" @click="codeBuildStore.openRun(resolvedRunId)">Retry</button>
    </div>

    <template v-else>
      <div class="cb-grid">
        <CodeBuildCardShell
          title="Run Header"
          subtitle="Workflow, trigger, identity, and linked story"
          :loading="codeBuildStore.runLoading || codeBuildStore.runLoadingCards.header"
          :error="headerSection.error"
          @retry="codeBuildStore.refreshRunCard('header')"
        >
          <div v-if="headerSection.data" class="header-stack">
            <div class="header-stack__top">
              <div class="header-meta">
                <BuildStatusBadge :status="headerSection.data.status" />
                <strong>{{ headerSection.data.workflowName }}</strong>
                <BranchPill :branch="headerSection.data.branch" />
                <ShaPill :sha="headerSection.data.sha" />
                <StoryLinkStatusChip :status="headerSection.data.storyLinkStatus" />
              </div>
              <a class="external-link" :href="headerSection.data.githubUrl" rel="noopener noreferrer" target="_blank">Open in GitHub ↗</a>
            </div>
            <div class="header-meta">
              <span>{{ headerSection.data.trigger }}</span>
              <span>{{ headerSection.data.actor }}</span>
              <DurationLabel :duration-seconds="headerSection.data.durationSeconds" :timestamp="headerSection.data.updatedAt" />
            </div>
            <WebhookStaleBanner v-if="showStaleBanner" @refresh="codeBuildStore.refreshRunCard('header')" />
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Jobs"
          subtitle="Per-job execution status"
          :loading="codeBuildStore.runLoading || codeBuildStore.runLoadingCards.jobs"
          :error="jobsSection.error"
          @retry="codeBuildStore.refreshRunCard('jobs')"
        >
          <div v-if="jobsSection.data" class="stacked-list">
            <div v-for="job in jobsSection.data" :key="job.jobId" class="list-row list-row--static">
              <div class="list-row__main">
                <strong>{{ job.jobName }}</strong>
                <BuildStatusBadge :status="job.status" />
              </div>
              <div class="list-row__meta">
                <DurationLabel :duration-seconds="job.durationSeconds" :timestamp="job.completedAt" />
              </div>
            </div>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Steps"
          subtitle="Step-level breakdown; failing step logs are highlighted below"
          :loading="codeBuildStore.runLoading || codeBuildStore.runLoadingCards.steps"
          :error="stepsSection.error"
          @retry="codeBuildStore.refreshRunCard('steps')"
        >
          <div v-if="stepsSection.data" class="stacked-list">
            <details v-for="job in stepsSection.data" :key="job.jobId" class="step-group" :open="job.steps.some(step => step.conclusion === 'FAILURE')">
              <summary>{{ job.jobName }}</summary>
              <div class="step-group__rows">
                <div v-for="step in job.steps" :key="step.stepId" class="list-row list-row--static">
                  <div class="list-row__main">
                    <strong>{{ step.name }}</strong>
                    <span>{{ step.conclusion }}</span>
                  </div>
                  <div class="list-row__meta">
                    <DurationLabel :duration-seconds="step.durationSeconds" :timestamp="step.completedAt" />
                  </div>
                </div>
              </div>
            </details>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Run Logs"
          subtitle="Redacted combined tail of the run log"
          :loading="codeBuildStore.runLoading || codeBuildStore.runLoadingCards.logs"
          :error="logsSection.error"
          :empty="!logsSection.data"
          empty-message="No retained log tail is available for this run."
          @retry="codeBuildStore.refreshRunCard('logs')"
        >
          <LogExcerptBlockView v-if="logsSection.data" :block="logsSection.data" />
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="AI Triage"
          subtitle="Evidence-aware row-level triage; failed evidence rows do not block the rest"
          :loading="codeBuildStore.runLoading || codeBuildStore.runLoadingCards.aiTriage"
          :error="triageSection.error"
          @retry="codeBuildStore.refreshRunCard('aiTriage')"
        >
          <template #actions>
            <input
              v-if="codeBuildStore.canManage"
              v-model="triageReason"
              class="reason-input"
              type="text"
              placeholder="Optional simulation token"
            />
            <button
              v-if="codeBuildStore.canManage"
              class="btn-machined"
              :disabled="!autonomyMeetsMinimum(viewerContext.autonomyLevel, 'SUPERVISED')"
              :title="autonomyMeetsMinimum(viewerContext.autonomyLevel, 'SUPERVISED') ? 'Regenerate AI triage' : 'Workspace autonomy must be SUPERVISED or above.'"
              @click="codeBuildStore.regenerateAiTriage(resolvedRunId, { reason: triageReason })"
            >
              Regenerate
            </button>
          </template>

          <div v-if="triageSection.data" class="triage-stack">
            <div v-if="codeBuildStore.mutationCode === 'CB_TRIAGE_EVIDENCE_MISMATCH'" class="feedback feedback--warn">
              {{ codeBuildStore.mutationMessage }}
            </div>
            <div v-if="codeBuildStore.mutationError" class="feedback feedback--error">
              {{ codeBuildStore.mutationError }}
            </div>
            <p class="text-body-sm">{{ triageSection.data.summary }}</p>
            <div v-if="triageSection.data.status === 'PENDING'" class="feedback feedback--ok">
              Triage is waiting for this run to reach a terminal state.
            </div>
            <div v-else class="triage-stack">
              <AiTriageRowCard
                v-for="row in triageSection.data.rows"
                :key="row.id"
                :row="row"
                :can-retry="codeBuildStore.canManage"
                @retry="codeBuildStore.regenerateAiTriage(resolvedRunId, { stepIds: [$event], reason: triageReason })"
              />
            </div>
            <LineageBadge v-if="triageSection.data.lineage" :lineage="triageSection.data.lineage" />
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Rerun"
          subtitle="Workflow-level rerun only in V1"
          :loading="codeBuildStore.runLoading || codeBuildStore.runLoadingCards.rerun"
          :error="rerunSection.error"
          @retry="codeBuildStore.refreshRunCard('rerun')"
        >
          <template #actions>
            <input
              v-if="codeBuildStore.canManage"
              v-model="rerunReason"
              class="reason-input"
              type="text"
              placeholder="Optional simulation token"
            />
            <button
              v-if="codeBuildStore.canManage"
              class="btn-machined"
              @click="codeBuildStore.rerunRun(resolvedRunId, { reason: rerunReason })"
            >
              Re-run Workflow
            </button>
          </template>

          <div v-if="rerunSection.data" class="triage-stack">
            <RateLimitBanner v-if="codeBuildStore.rateLimitResetAt" :reset-at="codeBuildStore.rateLimitResetAt" />
            <div v-if="codeBuildStore.mutationMessage" class="feedback feedback--ok">
              {{ codeBuildStore.mutationMessage }}
            </div>
            <div v-if="codeBuildStore.mutationError" class="feedback feedback--error">
              {{ codeBuildStore.mutationError }}
            </div>
            <p class="text-body-sm">{{ rerunSection.data.lastResultMessage ?? 'No rerun has been requested in this session.' }}</p>
            <button v-if="rerunSection.data.requestedRunId" class="btn-machined" @click="router.push({ name: 'code-build-management-run', params: { runId: rerunSection.data.requestedRunId }, query: buildContextQuery(route, viewerContext) })">
              Open {{ rerunSection.data.requestedRunId }}
            </button>
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
.header-stack__top,
.header-meta,
.stacked-list,
.list-row,
.list-row__main,
.list-row__meta,
.triage-stack {
  display: flex;
}

.header-stack,
.stacked-list,
.triage-stack {
  flex-direction: column;
  gap: 12px;
}

.header-stack__top,
.list-row {
  justify-content: space-between;
  gap: 12px;
}

.header-meta,
.list-row__main,
.list-row__meta {
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.list-row {
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.02);
  padding: 12px;
}

.list-row__meta {
  justify-content: flex-end;
  color: var(--color-on-surface-variant);
  font-size: 0.78rem;
}

.step-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.step-group summary {
  cursor: pointer;
  font-weight: 700;
}

.step-group__rows {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 8px;
}

.reason-input {
  min-width: 180px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  color: var(--color-on-surface);
  padding: 8px 10px;
}

.feedback {
  padding: 10px 12px;
  border-radius: var(--radius-md);
  font-size: 0.82rem;
}

.feedback--ok {
  background: rgba(78, 222, 163, 0.1);
  color: var(--cb-status-success);
}

.feedback--warn {
  background: rgba(245, 158, 11, 0.12);
  color: var(--cb-severity-minor);
}

.feedback--error {
  background: rgba(255, 180, 171, 0.1);
  color: var(--cb-status-failure);
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

@media (max-width: 1100px) {
  .cb-grid > * {
    grid-column: span 12;
  }

  .cb-hero {
    flex-direction: column;
  }
}
</style>
