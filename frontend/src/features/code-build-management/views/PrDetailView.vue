<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import LineageBadge from '@/shared/components/LineageBadge.vue';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import AiNoteCountsBar from '../components/AiNoteCountsBar.vue';
import AiSeverityChip from '../components/AiSeverityChip.vue';
import BranchPill from '../components/BranchPill.vue';
import CodeBuildCardShell from '../components/CodeBuildCardShell.vue';
import ContextLens from '../components/ContextLens.vue';
import DurationLabel from '../components/DurationLabel.vue';
import PrStateBadge from '../components/PrStateBadge.vue';
import ShaPill from '../components/ShaPill.vue';
import StoryLinkStatusChip from '../components/StoryLinkStatusChip.vue';
import { buildContextQuery, resolveViewerContextFromQuery, updateContextQuery } from '../routing';
import { useCodeBuildStore } from '../stores/codeBuildStore';
import { autonomyMeetsMinimum } from '../utils';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const codeBuildStore = useCodeBuildStore();
const regenerateReason = ref('');

const viewerContext = computed(() => resolveViewerContextFromQuery(route.query));
const resolvedPrId = computed(() => typeof route.params.prId === 'string' ? route.params.prId : 'pr-42');
const headerSection = computed(() => codeBuildStore.pr?.header ?? { data: null, error: null });
const checksSection = computed(() => codeBuildStore.pr?.checks ?? { data: null, error: null });
const reviewsSection = computed(() => codeBuildStore.pr?.reviews ?? { data: null, error: null });
const commitsSection = computed(() => codeBuildStore.pr?.commits ?? { data: null, error: null });
const aiReviewSection = computed(() => codeBuildStore.pr?.aiReview ?? { data: null, error: null });

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

watch(
  () => [viewerContext.value, resolvedPrId.value],
  ([context, nextPrId]) => {
    codeBuildStore.setViewerContext(context as typeof viewerContext.value);
    void codeBuildStore.openPr(nextPrId as string);
  },
  { immediate: true, deep: true },
);

watch(
  () => codeBuildStore.pr,
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
      environment: header.baseBranch,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Code & Build', path: '/code-build-management' },
      { label: `PR #${header.number}` },
    ]);

    shellUiStore.setAiPanelContent({
      summary: aggregate?.aiReview.data?.summary ?? 'Pull request detail is focused on checks, reviews, and AI guidance.',
      reasoning: [
        { text: `${aggregate?.checks.data?.length ?? 0} checks tied to the current head`, status: 'ok' },
        {
          text: `${aggregate?.aiReview.data?.noteCounts.blocker ?? 0} blocker notes currently visible by count`,
          status: (aggregate?.aiReview.data?.noteCounts.blocker ?? 0) > 0 ? 'warning' : 'ok',
        },
        {
          text: codeBuildStore.canSeeBlockerBodies ? 'Blocker bodies are visible for this role' : 'Blocker bodies are redacted for this role',
          status: codeBuildStore.canSeeBlockerBodies ? 'ok' : 'warning',
        },
      ],
      evidence: JSON.stringify(
        {
          route: route.fullPath,
          prId: resolvedPrId.value,
          headSha: aggregate?.header.data?.headSha,
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
        <p class="text-label">Pull Request Detail</p>
        <h2>{{ headerSection.data ? `PR #${headerSection.data.number}` : resolvedPrId }}</h2>
        <p class="cb-hero__copy">
          Pair checks, human reviews, commit lineage, and AI review output in one place so triage stays anchored to the current head SHA.
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

    <div v-if="codeBuildStore.prError && !codeBuildStore.pr" class="cb-page-error">
      <p class="text-label">
        {{ codeBuildStore.isPrForbidden ? 'Access Denied' : codeBuildStore.isPrNotFound ? 'PR Not Found' : 'PR Unavailable' }}
      </p>
      <p class="text-body-sm">{{ codeBuildStore.prError }}</p>
      <button class="btn-machined" @click="codeBuildStore.openPr(resolvedPrId)">Retry</button>
    </div>

    <template v-else>
      <div class="cb-grid">
        <CodeBuildCardShell
          title="PR Header"
          subtitle="Identity, branches, SHA fence, and linked story"
          :loading="codeBuildStore.prLoading || codeBuildStore.prLoadingCards.header"
          :error="headerSection.error"
          @retry="codeBuildStore.refreshPrCard('header')"
        >
          <div v-if="headerSection.data" class="header-stack">
            <div class="header-stack__top">
              <div>
                <PrStateBadge :state="headerSection.data.state" />
                <h3>{{ headerSection.data.title }}</h3>
              </div>
              <a class="external-link" :href="headerSection.data.githubUrl" rel="noopener noreferrer" target="_blank">Open in GitHub ↗</a>
            </div>
            <div class="header-meta">
              <span>{{ headerSection.data.author }}</span>
              <BranchPill :branch="headerSection.data.baseBranch" :is-default="true" />
              <span>→</span>
              <BranchPill :branch="headerSection.data.headBranch" />
              <ShaPill :sha="headerSection.data.headSha" />
              <StoryLinkStatusChip :status="headerSection.data.storyLinkStatus" />
            </div>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Checks"
          subtitle="Check runs tied to the head SHA"
          :loading="codeBuildStore.prLoading || codeBuildStore.prLoadingCards.checks"
          :error="checksSection.error"
          :empty="!!checksSection.data && checksSection.data.length === 0"
          empty-message="No check runs were observed for this PR head."
          @retry="codeBuildStore.refreshPrCard('checks')"
        >
          <div v-if="checksSection.data" class="stacked-list">
            <button v-for="entry in checksSection.data" :key="entry.id" class="list-row" @click="openRun(entry.runId)">
              <div class="list-row__main">
                <strong>{{ entry.checkName }}</strong>
                <span>{{ entry.conclusion }}</span>
              </div>
              <div class="list-row__meta">
                <DurationLabel :duration-seconds="entry.durationSeconds" :timestamp="entry.completedAt" />
                <span>{{ entry.status }}</span>
              </div>
            </button>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Human Reviews"
          subtitle="Approvals, requested changes, and comments"
          :loading="codeBuildStore.prLoading || codeBuildStore.prLoadingCards.reviews"
          :error="reviewsSection.error"
          :empty="!!reviewsSection.data && reviewsSection.data.length === 0"
          empty-message="No human reviews are attached to this PR."
          @retry="codeBuildStore.refreshPrCard('reviews')"
        >
          <div v-if="reviewsSection.data" class="stacked-list">
            <div v-for="review in reviewsSection.data" :key="review.id" class="list-row list-row--static">
              <div class="list-row__main">
                <strong>{{ review.reviewer }}</strong>
                <span>{{ review.state }}</span>
              </div>
              <div class="review-copy">
                <p class="text-body-sm">{{ review.summary }}</p>
                <p v-if="review.body" class="text-body-sm review-copy__body">{{ review.body }}</p>
              </div>
            </div>
          </div>
        </CodeBuildCardShell>

        <CodeBuildCardShell
          title="Commits"
          subtitle="Commit-level lineage inside this pull request"
          :loading="codeBuildStore.prLoading || codeBuildStore.prLoadingCards.commits"
          :error="commitsSection.error"
          :empty="!!commitsSection.data && commitsSection.data.length === 0"
          empty-message="No commits are attached to this PR."
          @retry="codeBuildStore.refreshPrCard('commits')"
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
          title="AI Review"
          subtitle="Counts are visible to everyone; blocker bodies are role-gated"
          :loading="codeBuildStore.prLoading || codeBuildStore.prLoadingCards.aiReview"
          :error="aiReviewSection.error"
          @retry="codeBuildStore.refreshPrCard('aiReview')"
        >
          <template #actions>
            <input
              v-if="codeBuildStore.canManage"
              v-model="regenerateReason"
              class="reason-input"
              type="text"
              placeholder="Optional simulation token"
            />
            <button
              v-if="codeBuildStore.canManage"
              class="btn-machined"
              :disabled="!autonomyMeetsMinimum(viewerContext.autonomyLevel, 'SUPERVISED')"
              :title="autonomyMeetsMinimum(viewerContext.autonomyLevel, 'SUPERVISED') ? 'Regenerate AI review' : 'Workspace autonomy must be SUPERVISED or above.'"
              @click="codeBuildStore.regenerateAiPrReview(resolvedPrId, { prevHeadSha: codeBuildStore.currentPrHeadSha ?? '', reason: regenerateReason })"
            >
              Regenerate
            </button>
          </template>

          <div v-if="aiReviewSection.data" class="ai-review">
            <AiNoteCountsBar :counts="aiReviewSection.data.noteCounts" />
            <p class="text-body-sm">{{ aiReviewSection.data.summary }}</p>

            <div v-if="codeBuildStore.mutationMessage" class="feedback feedback--ok">
              {{ codeBuildStore.mutationMessage }}
            </div>
            <div v-if="codeBuildStore.mutationError" class="feedback feedback--error">
              {{ codeBuildStore.mutationError }}
            </div>

            <template v-if="aiReviewSection.data.status === 'PENDING'">
              <p class="text-body-sm">AI review is pending for this PR head.</p>
            </template>
            <template v-else-if="aiReviewSection.data.status === 'FAILED'">
              <p class="text-body-sm">AI review failed with {{ aiReviewSection.data.failureCode }}.</p>
            </template>
            <template v-else>
              <div class="notes-list">
                <article v-for="note in aiReviewSection.data.notes" :key="note.id" class="note-card">
                  <div class="note-card__header">
                    <AiSeverityChip :severity="note.severity" />
                    <strong>{{ note.title }}</strong>
                  </div>
                  <p class="text-body-sm">{{ note.body }}</p>
                  <p class="text-body-sm note-card__meta">{{ note.filePath ?? 'cross-cutting' }} · {{ note.evidence }}</p>
                </article>
              </div>
            </template>

            <LineageBadge v-if="aiReviewSection.data.lineage" :lineage="aiReviewSection.data.lineage" />
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
.ai-review,
.notes-list {
  display: flex;
}

.header-stack,
.stacked-list,
.ai-review,
.notes-list {
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
  text-align: left;
}

.list-row__meta {
  justify-content: flex-end;
  color: var(--color-on-surface-variant);
  font-size: 0.78rem;
}

.review-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.review-copy__body,
.note-card__meta {
  color: var(--color-on-surface-variant);
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

.feedback--error {
  background: rgba(255, 180, 171, 0.1);
  color: var(--cb-status-failure);
}

.note-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
  background: rgba(255, 255, 255, 0.02);
}

.note-card__header {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
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

