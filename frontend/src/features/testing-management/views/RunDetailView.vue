<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import EmptyStatePlaceholder from '../components/EmptyStatePlaceholder.vue';
import ReqChip from '../components/ReqChip.vue';
import RunStatusBadge from '../components/RunStatusBadge.vue';
import TestingManagementCardShell from '../components/TestingManagementCardShell.vue';
import { useTestingManagement } from '../composables/useTestingManagement';
import { buildWorkspaceQuery, resolveWorkspaceIdFromQuery } from '../routing';
import { useTestingStore } from '../stores/testingStore';
import { formatDateTime, formatDuration, formatRelativeTime, truncateExcerpt } from '../utils';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const testingStore = useTestingStore();
const { dataSourceLabel } = useTestingManagement();

const workspaceId = computed(() => resolveWorkspaceIdFromQuery(route.query));
const resolvedRunId = computed(() => typeof route.params.runId === 'string' ? route.params.runId : 'run-auth-001');

const headerSection = computed(() => testingStore.run?.header ?? { data: null, error: null });
const caseResultsSection = computed(() => testingStore.run?.caseResults ?? { data: null, error: null });
const coverageSection = computed(() => testingStore.run?.coverage ?? { data: null, error: null });

function openPlan() {
  if (!headerSection.value.data) {
    return;
  }
  void router.push({
    name: 'testing-management-plan',
    params: { planId: headerSection.value.data.planId },
    query: buildWorkspaceQuery(route, workspaceId.value),
  });
}

function openCase(caseId: string) {
  void router.push({
    name: 'testing-management-case',
    params: { caseId },
    query: buildWorkspaceQuery(route, workspaceId.value),
  });
}

watch(
  () => [workspaceId.value, resolvedRunId.value],
  ([nextWorkspaceId, nextRunId]) => {
    testingStore.setWorkspaceId(nextWorkspaceId as string);
    void testingStore.openRun(nextRunId as string);
  },
  { immediate: true },
);

watch(
  () => testingStore.run,
  aggregate => {
    const header = aggregate?.header.data;
    if (!header) {
      return;
    }

    workspaceStore.setRouteContext({
      workspace: workspaceId.value,
      application: 'Testing Management',
      snowGroup: workspaceStore.context.snowGroup,
      project: header.planName,
      environment: header.environmentName,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Testing', path: '/testing' },
      { label: header.planName, path: `/testing/plans/${header.planId}` },
      { label: header.runId },
    ]);

    shellUiStore.setAiPanelContent({
      summary: `Reviewing ${header.runId} for run status, per-case outcomes, and requirement coverage rollup.`,
      reasoning: [
        { text: `${caseResultsSection.value.data?.length ?? 0} case results loaded`, status: 'ok' },
        {
          text: `${coverageSection.value.data?.coveredRequirementCount ?? 0} requirements are represented in the run`,
          status: 'ok',
        },
        {
          text: `Run state is ${header.state}`,
          status: header.state === 'FAILED' ? 'warning' : header.state === 'RUNNING' ? 'warning' : 'ok',
        },
      ],
      evidence: JSON.stringify(
        {
          route: route.fullPath,
          runId: header.runId,
          planId: header.planId,
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
  <div class="tm-page">
    <section class="tm-hero">
      <div>
        <p class="text-label">Run Detail</p>
        <h2>{{ headerSection.data?.runId ?? resolvedRunId }}</h2>
        <p class="tm-hero__copy">
          Inspect execution metadata, per-case outcomes, and the requirement footprint of a single run.
        </p>
      </div>
      <div class="tm-hero__actions">
        <span class="tm-mode-pill">{{ dataSourceLabel }}</span>
        <button class="btn-machined" @click="openPlan">Back to Plan</button>
      </div>
    </section>

    <div v-if="testingStore.runError && !testingStore.run" class="tm-page-error">
      <p class="text-label">Run Unavailable</p>
      <p class="text-body-sm">{{ testingStore.runError }}</p>
      <button class="btn-machined" @click="testingStore.openRun(resolvedRunId)">Retry</button>
    </div>

    <template v-else>
      <div class="tm-grid">
        <TestingManagementCardShell
          class="tm-span-5"
          title="Run Header"
          subtitle="Execution identity, trigger, actor, environment, and state"
          :loading="testingStore.runLoading || testingStore.runLoadingCards.header"
          :error="headerSection.error"
          @retry="testingStore.refreshRunCard('header')"
        >
          <div v-if="headerSection.data" class="tm-detail-grid">
            <div class="tm-detail-cell">
              <span class="text-label">Plan</span>
              <strong>{{ headerSection.data.planName }}</strong>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Environment</span>
              <strong>{{ headerSection.data.environmentName }}</strong>
              <span class="tm-muted">{{ headerSection.data.environmentKind }}</span>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Trigger</span>
              <strong>{{ headerSection.data.triggerSource }}</strong>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Actor</span>
              <strong>{{ headerSection.data.actor.displayName }}</strong>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">State</span>
              <RunStatusBadge :status="headerSection.data.state" />
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Duration</span>
              <strong>{{ formatDuration(headerSection.data.durationSec) }}</strong>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Started</span>
              <strong>{{ formatDateTime(headerSection.data.startedAt) }}</strong>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Completed</span>
              <strong>{{ headerSection.data.completedAt ? formatDateTime(headerSection.data.completedAt) : 'Still running' }}</strong>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">External Run</span>
              <strong>{{ headerSection.data.externalRunId ?? 'Not linked' }}</strong>
            </div>
          </div>
        </TestingManagementCardShell>

        <TestingManagementCardShell
          class="tm-span-7"
          title="Case Results"
          subtitle="Case-level outcomes with truncated failure excerpts"
          :loading="testingStore.runLoading || testingStore.runLoadingCards.caseResults"
          :error="caseResultsSection.error"
          @retry="testingStore.refreshRunCard('caseResults')"
        >
          <EmptyStatePlaceholder
            v-if="(caseResultsSection.data?.length ?? 0) === 0"
            title="No Case Results"
            message="This run has not produced case-level outcomes yet."
          />

          <div v-else class="tm-list">
            <article v-for="result in caseResultsSection.data" :key="result.resultId" class="tm-list-row">
              <div class="tm-inline-row">
                <div class="tm-inline-stack">
                  <button class="tm-link-button" @click="openCase(result.caseId)">{{ result.title }}</button>
                  <span class="tm-muted">{{ result.caseId }}</span>
                </div>
                <RunStatusBadge :status="result.outcome" />
              </div>
              <div class="tm-plan-tile__meta">
                <span>{{ formatDuration(result.durationSec) }}</span>
                <span>{{ result.lastPassedAt ? `Last pass ${formatRelativeTime(result.lastPassedAt)}` : 'No prior pass' }}</span>
                <span>{{ formatDateTime(result.createdAt) }}</span>
              </div>
              <p v-if="result.failureExcerpt" class="tm-excerpt">{{ truncateExcerpt(result.failureExcerpt, 260) }}</p>
            </article>
          </div>
        </TestingManagementCardShell>

        <TestingManagementCardShell
          class="tm-span-12"
          title="Requirement Coverage"
          subtitle="Union of requirement links represented by the cases that participated in the run"
          :loading="testingStore.runLoading || testingStore.runLoadingCards.coverage"
          :error="coverageSection.error"
          @retry="testingStore.refreshRunCard('coverage')"
        >
          <div v-if="coverageSection.data" class="tm-list">
            <div class="tm-inline-row">
              <div>
                <p class="text-label">Covered Requirement Count</p>
                <strong>{{ coverageSection.data.coveredRequirementCount }}</strong>
              </div>
              <span class="tm-meta-pill">{{ headerSection.data?.environmentName ?? 'Run Coverage' }}</span>
            </div>
            <div class="tm-chip-row">
              <ReqChip v-for="chip in coverageSection.data.coveredRequirements" :key="chip.reqId" :chip="chip" />
            </div>
          </div>
        </TestingManagementCardShell>
      </div>
    </template>
  </div>
</template>
