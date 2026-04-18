<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import CoverageIndicator from '../components/CoverageIndicator.vue';
import EmptyStatePlaceholder from '../components/EmptyStatePlaceholder.vue';
import TestingManagementCardShell from '../components/TestingManagementCardShell.vue';
import { useTestingManagement } from '../composables/useTestingManagement';
import { buildWorkspaceQuery, resolveWorkspaceIdFromQuery, updateWorkspaceQuery } from '../routing';
import { useTestingStore } from '../stores/testingStore';
import { formatDuration, formatPercent, formatRelativeTime } from '../utils';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const testingStore = useTestingStore();
const { dataSourceLabel } = useTestingManagement();
const coverageStatuses = ['GREEN', 'AMBER', 'RED', 'GREY'] as const;

const workspaceId = computed(() => resolveWorkspaceIdFromQuery(route.query));
const summarySection = computed(() => testingStore.catalog?.summary ?? { data: null, error: null });
const filtersSection = computed(() => testingStore.catalog?.filters ?? { data: null, error: null });
const gridSection = computed(() => testingStore.catalog?.grid ?? { data: null, error: null });

const groupedPlans = computed(() => {
  const groups = new Map<string, { projectId: string; projectName: string; plans: typeof gridSection.value.data }>();
  (gridSection.value.data ?? []).forEach(plan => {
    const current = groups.get(plan.projectId);
    if (current) {
      current.plans = [...(current.plans ?? []), plan];
      return;
    }
    groups.set(plan.projectId, {
      projectId: plan.projectId,
      projectName: plan.projectName,
      plans: [plan],
    });
  });
  return [...groups.values()];
});

function patchWorkspace(nextWorkspaceId: string) {
  void updateWorkspaceQuery(router, route, nextWorkspaceId);
}

function openPlan(planId: string) {
  void router.push({
    name: 'testing-management-plan',
    params: { planId },
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
  () => workspaceId.value,
  nextWorkspaceId => {
    testingStore.setWorkspaceId(nextWorkspaceId);
    void testingStore.initCatalog();
  },
  { immediate: true },
);

watch(
  () => testingStore.catalog,
  aggregate => {
    workspaceStore.setRouteContext({
      workspace: summarySection.value.data?.workspaceId === 'ws-default-001' ? 'Global SDLC Tower' : workspaceId.value,
      application: 'Testing Management',
      snowGroup: workspaceStore.context.snowGroup,
      project: null,
      environment: null,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Testing', path: '/testing' },
      { label: workspaceId.value },
    ]);

    shellUiStore.setAiPanelContent({
      summary: `QA catalog is ${summarySection.value.data ? 'loaded' : 'warming up'} across ${groupedPlans.value.length} project groups.`,
      reasoning: [
        { text: `${summarySection.value.data?.totalPlans ?? 0} plans visible in ${workspaceId.value}`, status: 'ok' },
        {
          text: `${summarySection.value.data?.byCoverageLed.RED ?? 0} plans are red and need investigation`,
          status: (summarySection.value.data?.byCoverageLed.RED ?? 0) > 0 ? 'warning' : 'ok',
        },
        {
          text: `${summarySection.value.data?.runsLast7d ?? 0} runs landed in the last 7 days`,
          status: (summarySection.value.data?.runsLast7d ?? 0) > 0 ? 'ok' : 'warning',
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
        <p class="text-label">Testing Catalog</p>
        <h2>Testing Management</h2>
        <p class="tm-hero__copy">
          Browse QA plans, coverage drift, and recent run health across the workspace. The view stays mock-backed in local dev and can switch cleanly to the backend contract.
        </p>
      </div>
      <div class="tm-hero__actions">
        <span class="tm-mode-pill">{{ dataSourceLabel }}</span>
        <button class="btn-machined" @click="openTraceability">Open Traceability</button>
      </div>
    </section>

    <div v-if="testingStore.catalogError && !testingStore.catalog" class="tm-page-error">
      <p class="text-label">Catalog Unavailable</p>
      <p class="text-body-sm">{{ testingStore.catalogError }}</p>
      <button class="btn-machined" @click="testingStore.initCatalog()">Retry</button>
    </div>

    <template v-else>
      <div class="tm-grid">
        <TestingManagementCardShell
          class="tm-span-4"
          title="Workspace Summary"
          subtitle="Read-only operational snapshot over plans, cases, and recent runs"
          :loading="testingStore.catalogLoading || testingStore.catalogLoadingCards.summary"
          :error="summarySection.error"
          @retry="testingStore.refreshCatalogCard('summary')"
        >
          <div v-if="summarySection.data" class="tm-stats-grid">
            <div class="tm-stat">
              <span class="text-label">Plans</span>
              <strong>{{ summarySection.data.totalPlans }}</strong>
            </div>
            <div class="tm-stat">
              <span class="text-label">Active Cases</span>
              <strong>{{ summarySection.data.totalActiveCases }}</strong>
            </div>
            <div class="tm-stat">
              <span class="text-label">Runs (7d)</span>
              <strong>{{ summarySection.data.runsLast7d }}</strong>
            </div>
            <div class="tm-stat">
              <span class="text-label">Pass Rate</span>
              <strong>{{ formatPercent(summarySection.data.passRateLast7d) }}</strong>
            </div>
            <div class="tm-stat">
              <span class="text-label">Mean Duration</span>
              <strong>{{ formatDuration(Math.round(summarySection.data.meanRunDurationSec)) }}</strong>
            </div>
            <div class="tm-stat">
              <span class="text-label">Coverage Mix</span>
              <div class="tm-bucket-row">
                <CoverageIndicator
                  v-for="status in coverageStatuses"
                  :key="status"
                  :status="status"
                  :ratio="summarySection.data.totalPlans ? summarySection.data.byCoverageLed[status] / summarySection.data.totalPlans : 0"
                  compact
                />
              </div>
            </div>
          </div>
        </TestingManagementCardShell>

        <TestingManagementCardShell
          class="tm-span-8"
          title="Catalog Filters"
          subtitle="Workspace-scoped controls that preserve deep links into plan detail"
          :loading="testingStore.catalogLoading || testingStore.catalogLoadingCards.filters"
          :error="filtersSection.error"
          @retry="testingStore.refreshCatalogCard('filters')"
        >
          <div class="tm-filter-grid">
            <label class="tm-filter-field">
              <span class="text-label">Workspace</span>
              <input :value="workspaceId" type="text" @change="patchWorkspace(($event.target as HTMLInputElement).value)" />
            </label>

            <label class="tm-filter-field">
              <span class="text-label">Search</span>
              <input
                :value="testingStore.catalogFilters.search"
                type="text"
                placeholder="plan, project, owner, release"
                @input="testingStore.initCatalog({ search: ($event.target as HTMLInputElement).value })"
              />
            </label>

            <label class="tm-filter-field">
              <span class="text-label">Project</span>
              <select
                :value="testingStore.catalogFilters.projectId"
                @change="testingStore.initCatalog({ projectId: ($event.target as HTMLSelectElement).value })"
              >
                <option value="ALL">ALL</option>
                <option v-for="projectId in filtersSection.data?.projectIds ?? []" :key="projectId" :value="projectId">
                  {{ projectId }}
                </option>
              </select>
            </label>

            <label class="tm-filter-field">
              <span class="text-label">Plan State</span>
              <select
                :value="testingStore.catalogFilters.planState"
                @change="testingStore.initCatalog({ planState: ($event.target as HTMLSelectElement).value as any })"
              >
                <option value="ALL">ALL</option>
                <option v-for="planState in filtersSection.data?.planStates ?? []" :key="planState" :value="planState">
                  {{ planState }}
                </option>
              </select>
            </label>

            <label class="tm-filter-field">
              <span class="text-label">Coverage</span>
              <select
                :value="testingStore.catalogFilters.coverageStatus"
                @change="testingStore.initCatalog({ coverageStatus: ($event.target as HTMLSelectElement).value as any })"
              >
                <option value="ALL">ALL</option>
                <option v-for="coverageStatus in filtersSection.data?.coverageStatuses ?? []" :key="coverageStatus" :value="coverageStatus">
                  {{ coverageStatus }}
                </option>
              </select>
            </label>
          </div>
        </TestingManagementCardShell>

        <TestingManagementCardShell
          class="tm-span-12"
          title="Plan Grid"
          subtitle="Grouped by project, with direct drilldown into plan details and coverage posture"
          :loading="testingStore.catalogLoading || testingStore.catalogLoadingCards.grid"
          :error="gridSection.error"
          @retry="testingStore.refreshCatalogCard('grid')"
        >
          <EmptyStatePlaceholder
            v-if="groupedPlans.length === 0"
            title="No Plans"
            message="No test plans match the current filters."
          />

          <div v-else class="tm-project-stack">
            <section v-for="group in groupedPlans" :key="group.projectId" class="tm-project-group">
              <div class="tm-project-header">
                <div>
                  <p class="text-label">{{ group.projectId }}</p>
                  <h3>{{ group.projectName }}</h3>
                </div>
                <span class="tm-meta-pill">{{ group.plans?.length ?? 0 }} plans</span>
              </div>

              <div class="tm-plan-grid">
                <article v-for="plan in group.plans" :key="plan.planId" class="tm-plan-tile">
                  <div class="tm-plan-tile__top">
                    <CoverageIndicator :status="plan.coverageLed" compact />
                    <span class="tm-state-pill">{{ plan.state }}</span>
                  </div>

                  <div>
                    <button class="tm-link-button" @click="openPlan(plan.planId)">{{ plan.name }}</button>
                    <p class="text-body-sm">{{ plan.description }}</p>
                  </div>

                  <div class="tm-plan-tile__meta">
                    <span>{{ plan.releaseTarget }}</span>
                    <span>{{ plan.owner.displayName }}</span>
                    <span>{{ plan.linkedCaseCount }} cases</span>
                    <span>{{ formatRelativeTime(plan.updatedAt) }}</span>
                  </div>
                </article>
              </div>
            </section>
          </div>
        </TestingManagementCardShell>
      </div>
    </template>
  </div>
</template>
