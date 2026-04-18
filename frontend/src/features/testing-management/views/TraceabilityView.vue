<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import CoverageIndicator from '../components/CoverageIndicator.vue';
import EmptyStatePlaceholder from '../components/EmptyStatePlaceholder.vue';
import TestingManagementCardShell from '../components/TestingManagementCardShell.vue';
import { useTestingManagement } from '../composables/useTestingManagement';
import { buildWorkspaceQuery, resolveWorkspaceIdFromQuery } from '../routing';
import { useTestingStore } from '../stores/testingStore';
import { formatRelativeTime } from '../utils';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const testingStore = useTestingStore();
const { dataSourceLabel } = useTestingManagement();

const workspaceId = computed(() => resolveWorkspaceIdFromQuery(route.query));
const summarySection = computed(() => testingStore.traceability?.summary ?? { data: null, error: null });
const reqRowsSection = computed(() => testingStore.traceability?.reqRows ?? { data: null, error: null });

function openCatalog() {
  void router.push({
    name: 'testing-management',
    query: buildWorkspaceQuery(route, workspaceId.value),
  });
}

function openPlan(planId: string) {
  void router.push({
    name: 'testing-management-plan',
    params: { planId },
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
  () => workspaceId.value,
  nextWorkspaceId => {
    testingStore.setWorkspaceId(nextWorkspaceId);
    void testingStore.initTraceability();
  },
  { immediate: true },
);

watch(
  () => testingStore.traceability,
  aggregate => {
    workspaceStore.setRouteContext({
      workspace: workspaceId.value,
      application: 'Testing Management',
      snowGroup: workspaceStore.context.snowGroup,
      project: null,
      environment: null,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Testing', path: '/testing' },
      { label: 'Traceability' },
    ]);

    shellUiStore.setAiPanelContent({
      summary: `Coverage traceability spans ${summarySection.value.data?.totalRequirements ?? 0} requirements in this workspace.`,
      reasoning: [
        { text: `${summarySection.value.data?.buckets.GREEN ?? 0} requirements are healthy`, status: 'ok' },
        {
          text: `${summarySection.value.data?.buckets.RED ?? 0} requirements are red`,
          status: (summarySection.value.data?.buckets.RED ?? 0) > 0 ? 'warning' : 'ok',
        },
        {
          text: `${testingStore.filteredTraceabilityRows.length} rows remain after filtering`,
          status: testingStore.filteredTraceabilityRows.length > 0 ? 'ok' : 'warning',
        },
      ],
      evidence: JSON.stringify(
        {
          route: route.fullPath,
          workspaceId: workspaceId.value,
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
  <div class="tm-page">
    <section class="tm-hero">
      <div>
        <p class="text-label">Traceability</p>
        <h2>Requirement Coverage Matrix</h2>
        <p class="tm-hero__copy">
          Inverse view from requirements into plans, cases, and latest observed outcomes, ready to deepen later with the Requirement slice tab integration.
        </p>
      </div>
      <div class="tm-hero__actions">
        <span class="tm-mode-pill">{{ dataSourceLabel }}</span>
        <button class="btn-machined" @click="openCatalog">Back to Catalog</button>
      </div>
    </section>

    <div v-if="testingStore.traceabilityError && !testingStore.traceability" class="tm-page-error">
      <p class="text-label">Traceability Unavailable</p>
      <p class="text-body-sm">{{ testingStore.traceabilityError }}</p>
      <button class="btn-machined" @click="testingStore.initTraceability()">Retry</button>
    </div>

    <template v-else>
      <div class="tm-grid">
        <TestingManagementCardShell
          class="tm-span-4"
          title="Coverage Summary"
          subtitle="Workspace-wide bucket counts across requirement links"
          :loading="testingStore.traceabilityLoading || testingStore.traceabilityLoadingCards.summary"
          :error="summarySection.error"
          @retry="testingStore.refreshTraceabilityCard('summary')"
        >
          <div v-if="summarySection.data" class="tm-stats-grid">
            <div class="tm-stat">
              <span class="text-label">Requirements</span>
              <strong>{{ summarySection.data.totalRequirements }}</strong>
            </div>
            <div class="tm-stat">
              <span class="text-label">Workspace</span>
              <strong>{{ summarySection.data.workspaceId }}</strong>
            </div>
            <div class="tm-stat">
              <span class="text-label">Coverage Buckets</span>
              <div class="tm-bucket-row">
                <CoverageIndicator v-for="status in ['GREEN', 'AMBER', 'RED', 'GREY']" :key="status" :status="status as any" compact />
              </div>
            </div>
          </div>
        </TestingManagementCardShell>

        <TestingManagementCardShell
          class="tm-span-8"
          title="Requirement Rows"
          subtitle="Deep links from each requirement to plans, cases, and latest observed coverage posture"
          :loading="testingStore.traceabilityLoading || testingStore.traceabilityLoadingCards.reqRows"
          :error="reqRowsSection.error"
          @retry="testingStore.refreshTraceabilityCard('reqRows')"
        >
          <div class="tm-filter-grid">
            <label class="tm-filter-field">
              <span class="text-label">Search</span>
              <input
                :value="testingStore.traceabilitySearch"
                type="text"
                placeholder="REQ, title, story, project"
                @input="testingStore.setTraceabilitySearch(($event.target as HTMLInputElement).value)"
              />
            </label>
          </div>

          <EmptyStatePlaceholder
            v-if="testingStore.filteredTraceabilityRows.length === 0"
            title="No Requirement Rows"
            message="No requirements match the current traceability filter."
          />

          <div v-else class="tm-list">
            <article v-for="row in testingStore.filteredTraceabilityRows" :key="row.reqId" class="tm-list-row">
              <div class="tm-inline-row">
                <div class="tm-inline-stack">
                  <button class="tm-link-button" @click="router.push(`/requirements/${row.reqId}`)">{{ row.reqId }}</button>
                  <strong>{{ row.reqTitle }}</strong>
                  <span class="tm-muted">{{ row.projectName }} · {{ row.storyId ?? 'No story link' }}</span>
                </div>
                <CoverageIndicator :status="row.coverageStatus" compact />
              </div>

              <div class="tm-plan-tile__meta">
                <span>{{ row.linkedCaseCount }} cases</span>
                <span>{{ row.linkedPlanCount }} plans</span>
                <span>{{ row.latestRunAt ? formatRelativeTime(row.latestRunAt) : 'Not run yet' }}</span>
              </div>

              <div class="tm-trace-case-list">
                <article v-for="caseRef in row.cases" :key="caseRef.caseId" class="tm-trace-case">
                  <div class="tm-inline-row">
                    <button class="tm-link-button" @click="openCase(caseRef.caseId)">{{ caseRef.title }}</button>
                    <button class="tm-link-button" @click="openPlan(caseRef.planId)">{{ caseRef.planName }}</button>
                  </div>
                  <div class="tm-plan-tile__meta">
                    <span>{{ caseRef.caseId }}</span>
                    <span>{{ caseRef.lastRunStatus ?? 'No run' }}</span>
                    <span>{{ caseRef.lastRunAt ? formatRelativeTime(caseRef.lastRunAt) : 'No run yet' }}</span>
                  </div>
                </article>
              </div>
            </article>
          </div>
        </TestingManagementCardShell>
      </div>
    </template>
  </div>
</template>
