<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import DesignManagementCardShell from '../components/DesignManagementCardShell.vue';
import { useDesignManagementStore } from '../stores/designManagementStore';
import { DM_DEFAULT_WORKSPACE_ID, type CatalogArtifactRow } from '../types';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const designManagementStore = useDesignManagementStore();

const resolvedWorkspaceId = computed(() => {
  const value = route.query.workspaceId;
  return typeof value === 'string' && value.trim() ? value : DM_DEFAULT_WORKSPACE_ID;
});

const summarySection = computed(() => designManagementStore.catalog?.summary);
const gridSection = computed(() => designManagementStore.catalog?.grid);
const filtersSection = computed(() => designManagementStore.catalog?.filters);

function formatTimestamp(value: string | null | undefined) {
  if (!value) {
    return 'n/a';
  }
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function openArtifact(row: CatalogArtifactRow) {
  void router.push({
    name: 'design-management-artifact',
    params: { artifactId: row.artifactId },
  });
}

function openTraceability() {
  void router.push({
    name: 'design-management-traceability',
    query: { workspaceId: resolvedWorkspaceId.value },
  });
}

watch(
  () => resolvedWorkspaceId.value,
  workspaceId => {
    void designManagementStore.initCatalog(workspaceId);
  },
  { immediate: true },
);

watch(
  () => designManagementStore.catalog,
  aggregate => {
    workspaceStore.setRouteContext({
      workspace: aggregate?.summary.data?.workspaceId ?? 'Global SDLC Tower',
      application: 'Design Management',
      snowGroup: workspaceStore.context.snowGroup,
      project: null,
      environment: null,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Design Management', path: '/design-management' },
      { label: resolvedWorkspaceId.value },
    ]);

    shellUiStore.setAiPanelContent({
      summary: aggregate?.summary.data?.advisory ?? 'Cataloging design artifacts, coverage drift, and viewer readiness.',
      reasoning: [
        { text: `${aggregate?.summary.data?.totalArtifacts ?? 0} artifacts in scope`, status: 'ok' },
        {
          text: `${aggregate?.summary.data?.coverageBuckets.STALE ?? 0} artifacts are stale against the latest specs`,
          status: (aggregate?.summary.data?.coverageBuckets.STALE ?? 0) > 0 ? 'warning' : 'ok',
        },
        {
          text: `${aggregate?.summary.data?.coverageBuckets.MISSING ?? 0} artifacts have no design linkage`,
          status: (aggregate?.summary.data?.coverageBuckets.MISSING ?? 0) > 0 ? 'warning' : 'ok',
        },
      ],
      evidence: JSON.stringify(
        {
          workspaceId: resolvedWorkspaceId.value,
          projects: aggregate?.filters.data?.projects ?? [],
          route: route.fullPath,
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
  designManagementStore.reset();
});
</script>

<template>
  <div class="dm-page">
    <div class="dm-hero">
      <div>
        <p class="text-label">Design Catalog</p>
        <h2>Design Management</h2>
        <p class="dm-hero__copy">
          Browse artifact versions, preview internal HTML mocks, and spot spec coverage drift across the workspace.
        </p>
      </div>
      <button class="btn-machined" @click="openTraceability">Open Traceability</button>
    </div>

    <div v-if="designManagementStore.catalogError && !designManagementStore.catalog" class="dm-page-error">
      <p class="text-label">Catalog Unavailable</p>
      <p class="text-body-sm">{{ designManagementStore.catalogError }}</p>
      <button class="btn-machined" @click="designManagementStore.initCatalog(resolvedWorkspaceId)">Retry</button>
    </div>

    <template v-else>
      <div class="dm-grid">
        <DesignManagementCardShell
          title="Catalog Summary"
          :loading="designManagementStore.catalogLoading"
          :error="summarySection?.error ?? null"
          :full-width="true"
          @retry="designManagementStore.initCatalog(resolvedWorkspaceId)"
        >
          <div v-if="summarySection?.data" class="dm-summary-grid">
            <div class="dm-summary-metric">
              <span class="text-label">Artifacts</span>
              <strong>{{ summarySection.data.totalArtifacts }}</strong>
            </div>
            <div class="dm-summary-metric">
              <span class="text-label">Published</span>
              <strong>{{ summarySection.data.publishedArtifacts }}</strong>
            </div>
            <div class="dm-summary-metric">
              <span class="text-label">Draft</span>
              <strong>{{ summarySection.data.draftArtifacts }}</strong>
            </div>
            <div class="dm-summary-metric">
              <span class="text-label">Retired</span>
              <strong>{{ summarySection.data.retiredArtifacts }}</strong>
            </div>
            <div class="dm-summary-metric">
              <span class="text-label">Linked</span>
              <strong>{{ summarySection.data.linkedArtifacts }}</strong>
            </div>
            <div class="dm-summary-metric dm-summary-metric--wide">
              <span class="text-label">Advisory</span>
              <strong class="dm-summary-copy">{{ summarySection.data.advisory }}</strong>
            </div>
          </div>
        </DesignManagementCardShell>

        <DesignManagementCardShell
          title="Workspace Filters"
          :loading="designManagementStore.catalogLoading"
          :error="filtersSection?.error ?? null"
          @retry="designManagementStore.initCatalog(resolvedWorkspaceId)"
        >
          <div v-if="filtersSection?.data" class="dm-chip-groups">
            <div>
              <p class="text-label">Projects</p>
              <div class="dm-chip-row">
                <span v-for="project in filtersSection.data.projects" :key="project" class="dm-chip">{{ project }}</span>
              </div>
            </div>
            <div>
              <p class="text-label">Lifecycle</p>
              <div class="dm-chip-row">
                <span v-for="lifecycle in filtersSection.data.lifecycles" :key="lifecycle" class="dm-chip">{{ lifecycle }}</span>
              </div>
            </div>
          </div>
        </DesignManagementCardShell>

        <DesignManagementCardShell
          title="Artifact Grid"
          :loading="designManagementStore.catalogLoading"
          :error="gridSection?.error ?? null"
          :full-width="true"
          @retry="designManagementStore.initCatalog(resolvedWorkspaceId)"
        >
          <div v-if="gridSection?.data" class="dm-section-list">
            <section v-for="section in gridSection.data" :key="section.projectId" class="dm-project-section">
              <div class="dm-project-header">
                <div>
                  <p class="text-label">{{ section.projectId }}</p>
                  <h3>{{ section.projectName }}</h3>
                </div>
                <span class="text-body-sm">{{ section.artifacts.length }} artifacts</span>
              </div>

              <button
                v-for="row in section.artifacts"
                :key="row.artifactId"
                class="dm-row"
                @click="openArtifact(row)"
              >
                <div class="dm-row__main">
                  <strong>{{ row.title }}</strong>
                  <p class="text-body-sm">
                    {{ row.format }} · v{{ row.currentVersionNumber }} · updated {{ formatTimestamp(row.lastUpdatedAt) }}
                  </p>
                </div>
                <div class="dm-row__meta">
                  <span class="dm-pill">{{ row.lifecycle }}</span>
                  <span class="dm-pill dm-pill--coverage">{{ row.worstCoverageStatus }}</span>
                  <span class="text-body-sm">{{ row.linkedSpecCount }} linked specs</span>
                </div>
              </button>
            </section>
          </div>
        </DesignManagementCardShell>
      </div>
    </template>
  </div>
</template>

<style scoped>
.dm-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 24px;
  height: 100%;
  overflow: auto;
}

.dm-hero,
.dm-page-error {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, rgba(137, 206, 255, 0.1), rgba(6, 14, 32, 0.4));
}

.dm-hero__copy {
  margin-top: 8px;
  max-width: 720px;
  color: var(--color-on-surface-variant);
}

.dm-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 18px;
}

.dm-grid > *:not(.dm-card--full) {
  grid-column: span 4;
}

.dm-summary-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.dm-summary-metric {
  padding: 12px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.03);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.dm-summary-metric--wide {
  grid-column: span 2;
}

.dm-summary-copy {
  font-size: 0.95rem;
  line-height: 1.5;
}

.dm-chip-groups {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.dm-chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.dm-chip,
.dm-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(137, 206, 255, 0.18);
  background: rgba(137, 206, 255, 0.08);
  font-size: 0.75rem;
}

.dm-section-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.dm-project-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dm-project-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.dm-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px;
  border: 1px solid rgba(137, 206, 255, 0.12);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.02);
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.dm-row:hover {
  border-color: rgba(137, 206, 255, 0.35);
  transform: translateY(-1px);
}

.dm-row__main,
.dm-row__meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dm-row__meta {
  align-items: flex-end;
}

@media (max-width: 1024px) {
  .dm-grid {
    grid-template-columns: 1fr;
  }

  .dm-grid > *:not(.dm-card--full) {
    grid-column: auto;
  }

  .dm-summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dm-summary-metric--wide {
    grid-column: span 2;
  }

  .dm-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .dm-row__meta {
    align-items: flex-start;
  }
}
</style>
