<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import LineageBadge from '@/shared/components/LineageBadge.vue';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import CaseOutcomeSparkline from '../components/CaseOutcomeSparkline.vue';
import EmptyStatePlaceholder from '../components/EmptyStatePlaceholder.vue';
import ReqChip from '../components/ReqChip.vue';
import RunStatusBadge from '../components/RunStatusBadge.vue';
import TestingManagementCardShell from '../components/TestingManagementCardShell.vue';
import { useTestingManagement } from '../composables/useTestingManagement';
import { buildWorkspaceQuery, resolveWorkspaceIdFromQuery } from '../routing';
import { useTestingStore } from '../stores/testingStore';
import { formatDateTime, formatRelativeTime, originLabel, truncateExcerpt } from '../utils';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const testingStore = useTestingStore();
const { dataSourceLabel } = useTestingManagement();

const workspaceId = computed(() => resolveWorkspaceIdFromQuery(route.query));
const resolvedCaseId = computed(() => typeof route.params.caseId === 'string' ? route.params.caseId : 'case-auth-4201');

const detailSection = computed(() => testingStore.caseAggregate?.detail ?? { data: null, error: null });
const recentResultsSection = computed(() => testingStore.caseAggregate?.recentResults ?? { data: null, error: null });
const revisionsSection = computed(() => testingStore.caseAggregate?.revisions ?? { data: null, error: null });

const originLineage = computed(() => {
  const origin = detailSection.value.data?.origin;
  if (!origin) {
    return null;
  }
  const lineageOrigin = origin === 'AI_DRAFT' ? 'AI_SKILL' : origin === 'IMPORTED' ? 'APPLICATION' : 'WORKSPACE';
  return {
    origin: lineageOrigin,
    overridden: false,
    chain: [
      {
        origin: lineageOrigin,
        value: originLabel(origin),
        setAt: detailSection.value.data?.updatedAt,
        setBy: detailSection.value.data?.owner.displayName,
      },
    ],
  } as const;
});

function openPlan() {
  if (!detailSection.value.data) {
    return;
  }
  void router.push({
    name: 'testing-management-plan',
    params: { planId: detailSection.value.data.planId },
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

watch(
  () => [workspaceId.value, resolvedCaseId.value],
  ([nextWorkspaceId, nextCaseId]) => {
    testingStore.setWorkspaceId(nextWorkspaceId as string);
    void testingStore.openCase(nextCaseId as string);
  },
  { immediate: true },
);

watch(
  () => testingStore.caseAggregate,
  aggregate => {
    const detail = aggregate?.detail.data;
    if (!detail) {
      return;
    }

    workspaceStore.setRouteContext({
      workspace: workspaceId.value,
      application: 'Testing Management',
      snowGroup: workspaceStore.context.snowGroup,
      project: detail.planName,
      environment: null,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Testing', path: '/testing' },
      { label: detail.planName, path: `/testing/plans/${detail.planId}` },
      { label: detail.title },
    ]);

    shellUiStore.setAiPanelContent({
      summary: `Inspecting ${detail.title} for ownership, requirement traceability, and recent run outcomes.`,
      reasoning: [
        { text: `${detail.linkedReqs.length} linked requirements loaded`, status: 'ok' },
        {
          text: `${recentResultsSection.value.data?.length ?? 0} recent outcomes are available`,
          status: (recentResultsSection.value.data?.length ?? 0) > 0 ? 'ok' : 'warning',
        },
        {
          text: `Origin is ${originLabel(detail.origin)}`,
          status: detail.origin === 'AI_DRAFT' ? 'warning' : 'ok',
        },
      ],
      evidence: JSON.stringify(
        {
          route: route.fullPath,
          caseId: resolvedCaseId.value,
          planId: detail.planId,
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
        <p class="text-label">Case Detail</p>
        <h2>{{ detailSection.data?.title ?? resolvedCaseId }}</h2>
        <p class="tm-hero__copy">
          Review the authored case body, its requirement links, and the recent execution trail without leaving the plan drilldown.
        </p>
      </div>
      <div class="tm-hero__actions">
        <span class="tm-mode-pill">{{ dataSourceLabel }}</span>
        <button class="btn-machined" @click="openPlan">Back to Plan</button>
      </div>
    </section>

    <div v-if="testingStore.caseError && !testingStore.caseAggregate" class="tm-page-error">
      <p class="text-label">Case Unavailable</p>
      <p class="text-body-sm">{{ testingStore.caseError }}</p>
      <button class="btn-machined" @click="testingStore.openCase(resolvedCaseId)">Retry</button>
    </div>

    <template v-else>
      <div class="tm-grid">
        <TestingManagementCardShell
          class="tm-span-5"
          title="Case Identity"
          subtitle="Ownership, state, origin, and linked evidence"
          :loading="testingStore.caseLoading || testingStore.caseLoadingCards.detail"
          :error="detailSection.error"
          @retry="testingStore.refreshCaseCard('detail')"
        >
          <div v-if="detailSection.data" class="tm-detail-grid">
            <div class="tm-detail-cell">
              <span class="text-label">Type</span>
              <strong>{{ detailSection.data.type }}</strong>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Priority</span>
              <strong>{{ detailSection.data.priority }}</strong>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">State</span>
              <span class="tm-state-pill">{{ detailSection.data.state }}</span>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Owner</span>
              <strong>{{ detailSection.data.owner.displayName }}</strong>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Origin</span>
              <LineageBadge v-if="originLineage" :lineage="originLineage" :compact="false" />
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Updated</span>
              <strong>{{ formatRelativeTime(detailSection.data.updatedAt) }}</strong>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Requirements</span>
              <div class="tm-chip-row">
                <ReqChip v-for="chip in detailSection.data.linkedReqs" :key="chip.reqId" :chip="chip" />
              </div>
            </div>
            <div class="tm-detail-cell">
              <span class="text-label">Incidents</span>
              <div v-if="detailSection.data.linkedIncidents.length" class="tm-chip-row">
                <RouterLink
                  v-for="incident in detailSection.data.linkedIncidents"
                  :key="incident.incidentId"
                  :to="incident.routePath"
                  class="tm-meta-pill"
                >
                  {{ incident.incidentId }} · {{ incident.severity }}
                </RouterLink>
              </div>
              <span v-else class="tm-muted">No incidents linked.</span>
            </div>
          </div>
        </TestingManagementCardShell>

        <TestingManagementCardShell
          class="tm-span-7"
          title="Case Body"
          subtitle="Safe markdown-style rendering for preconditions, steps, and expected result"
          :loading="testingStore.caseLoading || testingStore.caseLoadingCards.detail"
          :error="detailSection.error"
          @retry="testingStore.refreshCaseCard('detail')"
        >
          <div v-if="detailSection.data" class="tm-rich-grid">
            <section class="tm-rich-panel">
              <p class="text-label">Preconditions</p>
              <div class="tm-markdown">{{ detailSection.data.preconditions }}</div>
            </section>
            <section class="tm-rich-panel">
              <p class="text-label">Steps</p>
              <div class="tm-markdown">{{ detailSection.data.steps }}</div>
            </section>
            <section class="tm-rich-panel">
              <p class="text-label">Expected Result</p>
              <div class="tm-markdown">{{ detailSection.data.expectedResult }}</div>
            </section>
          </div>
        </TestingManagementCardShell>

        <TestingManagementCardShell
          class="tm-span-8"
          title="Recent Results"
          subtitle="Sparkline and latest execution table for the case"
          :loading="testingStore.caseLoading || testingStore.caseLoadingCards.recentResults"
          :error="recentResultsSection.error"
          @retry="testingStore.refreshCaseCard('recentResults')"
        >
          <EmptyStatePlaceholder
            v-if="(recentResultsSection.data?.length ?? 0) === 0"
            title="No Results"
            message="This case has not been executed yet."
          />

          <div v-else class="tm-list">
            <CaseOutcomeSparkline :outcomes="recentResultsSection.data ?? []" />

            <div class="tm-list">
              <button
                v-for="result in recentResultsSection.data"
                :key="result.resultId"
                class="tm-list-row"
                @click="openRun(result.runId)"
              >
                <div class="tm-list-row__main">
                  <RunStatusBadge :status="result.outcome" />
                  <strong>{{ result.environmentName }}</strong>
                  <span class="tm-muted">{{ result.runId }}</span>
                </div>
                <div class="tm-list-row__meta">
                  <span>{{ formatDateTime(result.createdAt) }}</span>
                  <span>{{ result.lastPassedAt ? `Last pass ${formatDateTime(result.lastPassedAt)}` : 'No prior pass' }}</span>
                </div>
                <p v-if="result.failureExcerpt" class="tm-excerpt">{{ truncateExcerpt(result.failureExcerpt, 220) }}</p>
              </button>
            </div>
          </div>
        </TestingManagementCardShell>

        <TestingManagementCardShell
          class="tm-span-4"
          title="Revisions"
          subtitle="Field-level edits captured for authored cases"
          :loading="testingStore.caseLoading || testingStore.caseLoadingCards.revisions"
          :error="revisionsSection.error"
          @retry="testingStore.refreshCaseCard('revisions')"
        >
          <EmptyStatePlaceholder
            v-if="(revisionsSection.data?.length ?? 0) === 0"
            title="No Revisions"
            message="No field-level revisions are recorded for this case yet."
          />

          <div v-else class="tm-list">
            <article v-for="revision in revisionsSection.data" :key="revision.revisionId" class="tm-list-row">
              <div class="tm-inline-row">
                <strong>{{ revision.actor.displayName }}</strong>
                <span class="tm-muted">{{ formatDateTime(revision.timestamp) }}</span>
              </div>
              <div class="tm-list">
                <div v-for="(diff, fieldName) in revision.fieldDiff" :key="fieldName" class="tm-inline-panel">
                  <p class="text-label">{{ fieldName }}</p>
                  <p class="text-body-sm">{{ diff }}</p>
                </div>
              </div>
            </article>
          </div>
        </TestingManagementCardShell>
      </div>
    </template>
  </div>
</template>
