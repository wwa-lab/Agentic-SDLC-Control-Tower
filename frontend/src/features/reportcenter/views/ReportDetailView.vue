<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import type { ReportRunRequest } from '../types';
import { requestFromQuery, requestToQuery, validateRequest } from '../utils';
import { useReportCenterStore } from '../stores/reportCenterStore';
import FilterForm from '../components/filter/FilterForm.vue';
import HeadlineStrip from '../components/result/HeadlineStrip.vue';
import ChartSection from '../components/result/ChartSection.vue';
import DrilldownSection from '../components/result/DrilldownSection.vue';
import ExportActions from '../components/export/ExportActions.vue';
import ExportJobToast from '../components/export/ExportJobToast.vue';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const store = useReportCenterStore();

const reportKey = computed(() => (typeof route.params.reportKey === 'string' ? route.params.reportKey : ''));
const definition = computed(() => store.activeDefinition);
const request = computed(() => store.activeRequest);
const result = computed(() => store.activeResult);

async function loadDefinition(nextReportKey: string) {
  if (!nextReportKey) {
    return;
  }
  const selected = await store.selectReport(nextReportKey);
  const nextRequest = requestFromQuery(selected, route.query as Record<string, unknown>);
  store.setActiveRequest(nextRequest);
  await runWith(nextRequest);
}

async function runWith(nextRequest: ReportRunRequest) {
  if (!definition.value) {
    return;
  }
  const validation = validateRequest(definition.value, nextRequest);
  if (!validation.valid) {
    return;
  }
  await router.replace({
    name: 'report-detail',
    params: { reportKey: definition.value.reportKey },
    query: requestToQuery(nextRequest),
  });
  await store.runReport(definition.value.reportKey, nextRequest);
}

function handleRequestUpdate(nextRequest: ReportRunRequest) {
  store.setActiveRequest(nextRequest);
  if (definition.value) {
    void router.replace({
      name: 'report-detail',
      params: { reportKey: definition.value.reportKey },
      query: requestToQuery(nextRequest),
    });
  }
}

function handleReset() {
  store.resetActiveRequest();
  if (store.activeRequest && definition.value) {
    void router.replace({
      name: 'report-detail',
      params: { reportKey: definition.value.reportKey },
      query: requestToQuery(store.activeRequest),
    });
  }
}

watch(
  () => reportKey.value,
  value => {
    void loadDefinition(value);
  },
  { immediate: true },
);

watch(
  [definition, result, request],
  ([nextDefinition, nextResult, nextRequest]) => {
    workspaceStore.setRouteContext({
      workspace: 'Global SDLC Tower',
      application: 'Report Center',
      snowGroup: workspaceStore.context.snowGroup,
      project: nextDefinition?.name ?? null,
      environment: null,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Reports', path: '/reports' },
      { label: nextDefinition?.name ?? reportKey.value },
    ]);

    shellUiStore.setAiPanelContent({
      summary: nextResult
        ? `${nextDefinition?.name ?? nextResult.reportKey} rendered for ${nextResult.scope} scope with ${nextResult.scopeIds.length} selected entities.`
        : `Preparing ${nextDefinition?.name ?? reportKey.value} for filter-driven analysis.`,
      reasoning: [
        { text: nextRequest ? `Grouping: ${nextRequest.grouping}` : 'Grouping not set yet', status: 'ok' },
        { text: nextResult?.slow ? 'Latest run exceeded the slow threshold' : 'Latest run is within the target budget', status: nextResult?.slow ? 'warning' : 'ok' },
        { text: nextResult?.drilldown.data ? `${nextResult.drilldown.data.totalRows} rows in drilldown` : 'No drilldown rows yet', status: 'ok' },
      ],
      evidence: JSON.stringify({ route: route.fullPath, reportKey: reportKey.value, request: nextRequest }, null, 2),
    });
  },
  { immediate: true, deep: true },
);

onBeforeUnmount(() => {
  workspaceStore.clearRouteContext();
  shellUiStore.clearBreadcrumbs();
  shellUiStore.clearAiPanelContent();
  store.resetDetail();
});
</script>

<template>
  <div class="report-detail-page">
    <div class="report-detail-hero">
      <div>
        <p class="text-label">History Snapshot</p>
        <h2>{{ definition?.name ?? reportKey }}</h2>
        <p class="text-body-sm">{{ definition?.description ?? 'Loading report definition…' }}</p>
      </div>
      <button class="btn-machined" @click="router.push('/reports')">Back to Catalog</button>
    </div>

    <div v-if="store.runError && !result" class="report-detail-error">
      <p class="text-label">Run unavailable</p>
      <p class="text-body-sm">{{ store.runError }}</p>
      <button class="btn-machined" @click="request && runWith(request)">Retry</button>
    </div>

    <FilterForm
      v-if="definition && request"
      :definition="definition"
      :model-value="request"
      :show-mock-tools="store.useMockData"
      @update:model-value="handleRequestUpdate"
      @apply="runWith"
      @reset="handleReset"
    />

    <div v-if="result?.slow" class="slow-banner">
      <strong>Slow report</strong>
      <span class="text-body-sm">This run took longer than expected. Consider narrowing your filter before exporting.</span>
    </div>

    <HeadlineStrip
      v-if="result || store.runLoading"
      :section="result?.headline ?? { data: null, error: null }"
      :loading="store.runLoading"
      @retry="request && runWith(request)"
    />

    <ChartSection
      v-if="definition && (result || store.runLoading)"
      :section="result?.series ?? { data: null, error: null }"
      :chart-type="definition.chartType"
      :loading="store.runLoading"
      @retry="request && runWith(request)"
    />

    <DrilldownSection
      v-if="result || store.runLoading"
      :section="result?.drilldown ?? { data: null, error: null }"
      :loading="store.runLoading"
      @retry="request && runWith(request)"
    />

    <ExportActions
      v-if="definition && request"
      :request="request"
      :report-key="definition.reportKey"
      :disabled="store.runLoading"
      @exported="store.pollExport($event.exportId)"
    />

    <ExportJobToast />
  </div>
</template>

<style scoped>
.report-detail-page {
  display: grid;
  gap: 18px;
  padding: 24px;
  height: 100%;
  overflow: auto;
}

.report-detail-hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: start;
  padding: 22px;
  border-radius: 22px;
  border: var(--border-ghost);
  background:
    radial-gradient(circle at top right, color-mix(in srgb, var(--accent-history) 22%, transparent), transparent 38%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-report) 92%, transparent), var(--color-surface-container-low));
}

.report-detail-error,
.slow-banner {
  display: grid;
  gap: 8px;
  padding: 16px 18px;
  border-radius: 14px;
  border: var(--border-ghost);
}

.report-detail-error {
  background: color-mix(in srgb, var(--color-error) 10%, transparent);
}

.slow-banner {
  background: color-mix(in srgb, #f59e0b 12%, transparent);
}

@media (max-width: 900px) {
  .report-detail-hero {
    flex-direction: column;
  }
}
</style>
