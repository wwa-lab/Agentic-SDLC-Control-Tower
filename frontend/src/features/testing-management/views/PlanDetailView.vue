<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import LineageBadge from '@/shared/components/LineageBadge.vue';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import CoverageIndicator from '../components/CoverageIndicator.vue';
import EmptyStatePlaceholder from '../components/EmptyStatePlaceholder.vue';
import ReqChip from '../components/ReqChip.vue';
import RunStatusBadge from '../components/RunStatusBadge.vue';
import TestingManagementCardShell from '../components/TestingManagementCardShell.vue';
import { useTestingManagement } from '../composables/useTestingManagement';
import { buildWorkspaceQuery, resolveWorkspaceIdFromQuery } from '../routing';
import { useTestingStore } from '../stores/testingStore';
import { formatDateTime, formatDuration, formatPercent, formatRelativeTime } from '../utils';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const testingStore = useTestingStore();
const { dataSourceLabel } = useTestingManagement();

const workspaceId = computed(() => resolveWorkspaceIdFromQuery(route.query));
const resolvedPlanId = computed(() => typeof route.params.planId === 'string' ? route.params.planId : 'plan-auth-001');

const headerSection = computed(() => testingStore.plan?.header ?? { data: null, error: null });
const casesSection = computed(() => testingStore.plan?.cases ?? { data: null, error: null });
const coverageSection = computed(() => testingStore.plan?.coverage ?? { data: null, error: null });
const recentRunsSection = computed(() => testingStore.plan?.recentRuns ?? { data: null, error: null });
const draftInboxSection = computed(() => testingStore.plan?.draftInbox ?? { data: null, error: null });
const aiInsightsSection = computed(() => testingStore.plan?.aiInsights ?? { data: null, error: null });

function openCatalog() {
  void router.push({
    name: 'testing-management',
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

function openRun(runId: string) {
  void router.push({
    name: 'testing-management-run',
    params: { runId },
    query: buildWorkspaceQuery(route, workspaceId.value),
  });
}

function openTraceability() {
  void router.push({
    name: 'testing-management-traceability',
    query: buildWorkspaceQuery(route, workspaceId.value),
  });
}

watch(
  () => [workspaceId.value, resolvedPlanId.value],
  ([nextWorkspaceId, nextPlanId]) => {
    testingStore.setWorkspaceId(nextWorkspaceId as string);
    void testingStore.openPlan(nextPlanId as string);
  },
  { immediate: true },
);

watch(
  () => testingStore.plan,
  aggregate => {
    const header = aggregate?.header.data;
    if (!header) {
      return;
    }

    workspaceStore.setRouteContext({
      workspace: header.workspaceName,
      application: 'Testing Management',
      snowGroup: workspaceStore.context.snowGroup,
      project: header.projectName,
      environment: header.releaseTarget,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Testing', path: '/testing' },
      { label: header.name },
    ]);

    shellUiStore.setAiPanelContent({
      summary: aiInsightsSection.value.data?.narrative ?? 'Inspecting plan health, run history, and AI draft readiness.',
      reasoning: [
        { text: `${casesSection.value.data?.length ?? 0} cases are linked to this plan`, status: 'ok' },
        {
          text: `${draftInboxSection.value.data?.length ?? 0} AI drafts are waiting in the inbox`,
          status: (draftInboxSection.value.data?.length ?? 0) > 0 ? 'warning' : 'ok',
        },
        {
          text: `Latest run is ${recentRunsSection.value.data?.[0]?.state ?? 'not available'}`,
          status: recentRunsSection.value.data?.[0]?.state === 'FAILED' ? 'warning' : 'ok',
        },
      ],
      evidence: JSON.stringify(
        {
          route: route.fullPath,
          planId: resolvedPlanId.value,
          workspaceId: workspaceId.value,
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
        <p class="text-label">Plan Detail</p>
        <h2>{{ headerSection.data?.name ?? resolvedPlanId }}</h2>
        <p class="tm-hero__copy">
          Review plan identity, linked cases, requirement coverage, recent runs, and AI-drafted candidates without losing workspace context.
        </p>
      </div>
      <div class="tm-hero__actions">
        <span class="tm-mode-pill">{{ dataSourceLabel }}</span>
        <button class="btn-machined" @click="openCatalog">Back to Catalog</button>
        <button class="btn-machined" @click="openTraceability">Traceability</button>
      </div>
    </section>

    <div v-if="testingStore.planError && !testingStore.plan" class="tm-page-error">
      <p class="text-label">Plan Unavailable</p>
      <p class="text-body-sm">{{ testingStore.planError }}</p>
      <button class="btn-machined" @click="testingStore.openPlan(resolvedPlanId)">Retry</button>
    </div>

    <template v-else>
      <div class="tm-grid">
        <TestingManagementCardShell
          class="tm-span-5"
          title="Plan Header"
          subtitle="Workspace ownership, release target, and lifecycle state"
          :loading="testingStore.planLoading || testingStore.planLoadingCards.header"
          :error="headerSection.error"
          @retry="testingStore.refreshPlanCard('header')"
        >
          <div v-if="headerSection.data" class="tm-detail-grid">
            <div class="tm-detail-cell">
              <span class="text-label">Project</span>
              <strong>{{ headerSection.data.projectName }}</strong>
              <span class="tm-muted">{{ headerSection.data.projectId }}</span>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Owner</span>
              <strong>{{ headerSection.data.owner.displayName }}</strong>
              <span class="tm-muted">{{ headerSection.data.owner.memberId }}</span>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Release</span>
              <strong>{{ headerSection.data.releaseTarget }}</strong>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">State</span>
              <span class="tm-state-pill">{{ headerSection.data.state }}</span>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Created</span>
              <strong>{{ formatDateTime(headerSection.data.createdAt) }}</strong>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Updated</span>
              <strong>{{ formatRelativeTime(headerSection.data.updatedAt) }}</strong>
            </div>
            <div class="tm-detail-cell tm-span-12">
              <span class="text-label">Description</span>
              <p class="text-body-sm">{{ headerSection.data.description }}</p>
            </div>
          </div>
        </TestingManagementCardShell>

        <TestingManagementCardShell
          class="tm-span-7"
          title="Plan Cases"
          subtitle="Case inventory, requirement chips, and latest outcome per case"
          :loading="testingStore.planLoading || testingStore.planLoadingCards.cases"
          :error="casesSection.error"
          @retry="testingStore.refreshPlanCard('cases')"
        >
          <EmptyStatePlaceholder
            v-if="(casesSection.data?.length ?? 0) === 0"
            title="No Cases"
            message="This plan does not have any linked test cases yet."
          />

          <table v-else class="tm-table tm-table--wide">
            <thead>
              <tr>
                <th>Case</th>
                <th>State</th>
                <th>Linked Reqs</th>
                <th>Last Outcome</th>
                <th>Last Run</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="entry in casesSection.data" :key="entry.caseId">
                <td class="tm-table__cell">
                  <button class="tm-link-button" @click="openCase(entry.caseId)">{{ entry.title }}</button>
                  <span class="tm-muted">{{ entry.type }} · {{ entry.priority }}</span>
                </td>
                <td class="tm-table__cell">
                  <span class="tm-state-pill">{{ entry.state }}</span>
                </td>
                <td class="tm-table__cell">
                  <div class="tm-chip-row">
                    <ReqChip v-for="chip in entry.linkedReqs" :key="chip.reqId" :chip="chip" />
                  </div>
                </td>
                <td class="tm-table__cell">
                  <RunStatusBadge v-if="entry.lastRunStatus" :status="entry.lastRunStatus" />
                  <span v-else class="tm-muted">No run</span>
                </td>
                <td class="tm-table__cell">
                  <span>{{ entry.lastRunAt ? formatRelativeTime(entry.lastRunAt) : 'Not run yet' }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </TestingManagementCardShell>

        <TestingManagementCardShell
          class="tm-span-5"
          title="Coverage Rollup"
          subtitle="Requirement posture aggregated at the plan level"
          :loading="testingStore.planLoading || testingStore.planLoadingCards.coverage"
          :error="coverageSection.error"
          @retry="testingStore.refreshPlanCard('coverage')"
        >
          <EmptyStatePlaceholder
            v-if="(coverageSection.data?.length ?? 0) === 0"
            title="No Requirement Links"
            message="Coverage will appear after cases link back to requirements."
          />

          <div v-else class="tm-list">
            <article v-for="row in coverageSection.data" :key="row.reqId" class="tm-list-row">
              <div class="tm-inline-row">
                <div>
                  <button class="tm-link-button" @click="router.push(row.reqId.startsWith('REQ') ? `/requirements/${row.reqId}` : '/requirements')">{{ row.reqId }}</button>
                  <p class="text-body-sm">{{ row.reqTitle }}</p>
                </div>
                <CoverageIndicator :status="row.aggregateStatus" compact />
              </div>
              <div class="tm-plan-tile__meta">
                <span>{{ row.linkedCaseCount }} cases</span>
                <span>{{ row.mostRecentAt ? formatRelativeTime(row.mostRecentAt) : 'No runs yet' }}</span>
              </div>
            </article>
          </div>
        </TestingManagementCardShell>

        <TestingManagementCardShell
          class="tm-span-7"
          title="Recent Runs"
          subtitle="Latest execution history with counts, actor, and status"
          :loading="testingStore.planLoading || testingStore.planLoadingCards.recentRuns"
          :error="recentRunsSection.error"
          @retry="testingStore.refreshPlanCard('recentRuns')"
        >
          <EmptyStatePlaceholder
            v-if="(recentRunsSection.data?.length ?? 0) === 0"
            title="No Runs Yet"
            message="This plan has not recorded any executions yet."
          />

          <div v-else class="tm-list">
            <button v-for="run in recentRunsSection.data" :key="run.runId" class="tm-list-row" @click="openRun(run.runId)">
              <div class="tm-list-row__main">
                <RunStatusBadge :status="run.state" />
                <strong>{{ run.environmentName }}</strong>
                <span class="tm-muted">{{ run.triggerSource }}</span>
              </div>
              <div class="tm-list-row__meta">
                <span>{{ run.actor.displayName }}</span>
                <span>{{ run.passCount }}P / {{ run.failCount }}F / {{ run.skipCount }}S</span>
                <span>{{ formatDuration(run.durationSec) }}</span>
                <span>{{ formatRelativeTime(run.startedAt) }}</span>
              </div>
            </button>
          </div>
        </TestingManagementCardShell>

        <TestingManagementCardShell
          class="tm-span-5"
          title="AI Draft Inbox"
          subtitle="Mocked Phase 5 preview, already backed by the shared section envelope"
          :loading="testingStore.planLoading || testingStore.planLoadingCards.draftInbox"
          :error="draftInboxSection.error"
          @retry="testingStore.refreshPlanCard('draftInbox')"
        >
          <EmptyStatePlaceholder
            v-if="(draftInboxSection.data?.length ?? 0) === 0"
            title="No Drafts"
            message="No AI-generated candidate cases are waiting for review."
          />

          <div v-else class="tm-list">
            <article v-for="draft in draftInboxSection.data" :key="draft.caseId" class="tm-list-row">
              <div class="tm-inline-row">
                <button class="tm-link-button" @click="openCase(draft.caseId)">{{ draft.title }}</button>
                <LineageBadge
                  :lineage="{ origin: 'AI_SKILL', overridden: false, chain: [{ origin: 'AI_SKILL', value: draft.skillVersion, setAt: draft.draftedAt, setBy: 'tm-drafter' }] }"
                />
              </div>
              <div class="tm-plan-tile__meta">
                <span>{{ draft.sourceReqId }}</span>
                <span>{{ formatDateTime(draft.draftedAt) }}</span>
                <span>{{ draft.skillVersion }}</span>
              </div>
            </article>
          </div>
        </TestingManagementCardShell>

        <TestingManagementCardShell
          class="tm-span-7"
          title="AI Insights"
          subtitle="Autonomy posture and narrative summary for the current plan"
          :loading="testingStore.planLoading || testingStore.planLoadingCards.aiInsights"
          :error="aiInsightsSection.error"
          @retry="testingStore.refreshPlanCard('aiInsights')"
        >
          <div v-if="aiInsightsSection.data" class="tm-summary-grid">
            <div class="tm-stat">
              <span class="text-label">Autonomy</span>
              <strong>{{ aiInsightsSection.data.autonomyLevel }}</strong>
            </div>
            <div class="tm-stat">
              <span class="text-label">Pending Drafts</span>
              <strong>{{ aiInsightsSection.data.pendingDrafts }}</strong>
            </div>
            <div class="tm-stat">
              <span class="text-label">Active Cases</span>
              <strong>{{ aiInsightsSection.data.activeCases }}</strong>
            </div>
            <div class="tm-stat">
              <span class="text-label">Pass Rate</span>
              <strong>{{ formatPercent(aiInsightsSection.data.passRateLast7d) }}</strong>
            </div>
            <div class="tm-rich-panel">
              <span class="text-label">Narrative</span>
              <p class="text-body-sm">{{ aiInsightsSection.data.narrative }}</p>
            </div>
          </div>
        </TestingManagementCardShell>
      </div>
    </template>
  </div>
</template>
