<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import type { ReportRunHistoryEntry } from '../types';
import { prefillQueryFromHistory } from '../utils';
import { useReportCenterStore } from '../stores/reportCenterStore';
import ReportCategoryGroup from '../components/catalog/ReportCategoryGroup.vue';
import HistoryList from '../components/catalog/HistoryList.vue';
import ExportsList from '../components/catalog/ExportsList.vue';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const store = useReportCenterStore();

const activeTab = computed(() => {
  const value = route.query.tab;
  return value === 'history' || value === 'exports' ? value : 'catalog';
});

function setTab(tab: 'catalog' | 'history' | 'exports') {
  void router.replace({
    name: 'reports',
    query: tab === 'catalog' ? {} : { tab },
  });
}

function openReport(reportKey: string) {
  void router.push({
    name: 'report-detail',
    params: { reportKey },
  });
}

function openHistoryEntry(entry: ReportRunHistoryEntry) {
  void router.push({
    name: 'report-detail',
    params: { reportKey: entry.reportKey },
    query: {
      ...prefillQueryFromHistory(entry),
    },
  });
}

watch(
  () => activeTab.value,
  tab => {
    if (!store.hasLoadedCatalog) {
      void store.fetchCatalog();
    }
    if (tab === 'history') {
      void store.fetchHistory();
    }
    if (tab === 'exports') {
      void store.fetchExportHistory();
    }
  },
  { immediate: true },
);

watch(
  () => store.catalog,
  catalog => {
    workspaceStore.setRouteContext({
      workspace: 'Global SDLC Tower',
      application: 'Report Center',
      snowGroup: workspaceStore.context.snowGroup,
      project: null,
      environment: null,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Reports', path: '/reports' },
    ]);

    shellUiStore.setAiPanelContent({
      summary: `Report Center is ready with ${catalog?.categories[0]?.reports.length ?? 0} enabled efficiency reports and historical export surfaces.`,
      reasoning: [
        { text: `${catalog?.categories.length ?? 0} categories registered`, status: 'ok' },
        { text: `${catalog?.categories[0]?.reports.length ?? 0} enabled reports in MVP`, status: 'ok' },
        { text: activeTab.value === 'catalog' ? 'Catalog tab active' : `${activeTab.value} tab active`, status: 'ok' },
      ],
      evidence: JSON.stringify({ route: route.fullPath, tab: activeTab.value }, null, 2),
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
  <div class="reports-page">
    <section class="reports-hero">
      <div>
        <p class="text-label">History Mode</p>
        <h2>Report Center</h2>
        <p class="text-body-sm">
          Browse evidence-ready reports, re-run historical slices on demand, and export the exact view you just reviewed.
        </p>
      </div>

      <div class="reports-tabs">
        <button class="reports-tab" :class="{ 'reports-tab--active': activeTab === 'catalog' }" @click="setTab('catalog')">Catalog</button>
        <button class="reports-tab" :class="{ 'reports-tab--active': activeTab === 'history' }" @click="setTab('history')">History</button>
        <button class="reports-tab" :class="{ 'reports-tab--active': activeTab === 'exports' }" @click="setTab('exports')">Exports</button>
      </div>
    </section>

    <div v-if="store.catalogError && !store.catalog" class="reports-error">
      <p class="text-label">Catalog unavailable</p>
      <p class="text-body-sm">{{ store.catalogError }}</p>
      <button class="btn-machined" @click="store.fetchCatalog(true)">Retry</button>
    </div>

    <template v-else>
      <div v-if="activeTab === 'catalog'" class="reports-grid">
        <ReportCategoryGroup
          v-for="group in store.catalog?.categories ?? []"
          :key="group.category"
          :group="group"
          @open="openReport"
        />
      </div>

      <HistoryList
        v-else-if="activeTab === 'history'"
        :entries="store.history"
        :loading="store.historyLoading"
        :error="store.historyError"
        @open="openHistoryEntry"
        @retry="store.fetchHistory()"
      />

      <ExportsList
        v-else
        :entries="store.exportsHistory"
        :loading="store.exportsLoading"
        :error="store.exportsError"
        @retry="store.fetchExportHistory()"
      />
    </template>
  </div>
</template>

<style scoped>
.reports-page {
  display: grid;
  gap: 20px;
  padding: 24px;
  height: 100%;
  overflow: auto;
}

.reports-hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 22px;
  border: var(--border-ghost);
  border-radius: 22px;
  background:
    radial-gradient(circle at top right, color-mix(in srgb, var(--accent-history) 20%, transparent), transparent 42%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-report) 92%, transparent), var(--color-surface-container-low));
}

.reports-tabs {
  display: flex;
  gap: 10px;
  align-items: start;
}

.reports-tab {
  border: var(--border-ghost);
  border-radius: 999px;
  background: transparent;
  color: var(--color-on-surface);
  padding: 10px 14px;
  cursor: pointer;
}

.reports-tab--active {
  background: var(--accent-history-bg);
  color: var(--accent-history);
}

.reports-grid {
  display: grid;
  gap: 22px;
}

.reports-error {
  display: grid;
  gap: 10px;
  padding: 18px;
  border-radius: 16px;
  border: var(--border-ghost);
  background: color-mix(in srgb, var(--color-error) 10%, transparent);
}

@media (max-width: 900px) {
  .reports-hero {
    flex-direction: column;
  }
}
</style>
