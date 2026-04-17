<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import DesignManagementCardShell from '../components/DesignManagementCardShell.vue';
import { useDesignManagementStore } from '../stores/designManagementStore';
import { DM_DEFAULT_WORKSPACE_ID } from '../types';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const designManagementStore = useDesignManagementStore();

const resolvedWorkspaceId = computed(() => {
  const value = route.query.workspaceId;
  return typeof value === 'string' && value.trim() ? value : DM_DEFAULT_WORKSPACE_ID;
});

const matrixSection = computed(() => designManagementStore.traceability?.matrix);
const summarySection = computed(() => designManagementStore.traceability?.summary);
const gapsSection = computed(() => designManagementStore.traceability?.gaps);

function openArtifact(path: string) {
  void router.push(path);
}

function openRequirement(path: string) {
  void router.push(path);
}

watch(
  () => resolvedWorkspaceId.value,
  workspaceId => {
    void designManagementStore.initTraceability(workspaceId);
  },
  { immediate: true },
);

watch(
  () => designManagementStore.traceability,
  aggregate => {
    workspaceStore.setRouteContext({
      workspace: resolvedWorkspaceId.value,
      application: 'Design Management Traceability',
      snowGroup: workspaceStore.context.snowGroup,
      project: null,
      environment: null,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Design Management', path: '/design-management' },
      { label: 'Traceability' },
    ]);

    shellUiStore.setAiPanelContent({
      summary: `${aggregate?.summary.data?.specCount ?? 0} specs in scope, ${aggregate?.gaps.data?.length ?? 0} without linked design artifacts.`,
      reasoning: [
        { text: `${aggregate?.summary.data?.artifactCount ?? 0} artifacts considered`, status: 'ok' },
        { text: `${aggregate?.gaps.data?.length ?? 0} gaps require design linkage`, status: (aggregate?.gaps.data?.length ?? 0) > 0 ? 'warning' : 'ok' },
      ],
      evidence: JSON.stringify(
        {
          workspaceId: resolvedWorkspaceId.value,
          matrixRows: aggregate?.matrix.data?.length ?? 0,
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
    <div class="dm-grid">
      <DesignManagementCardShell
        title="Coverage Summary"
        :loading="designManagementStore.traceabilityLoading"
        :error="summarySection?.error ?? null"
        @retry="designManagementStore.initTraceability(resolvedWorkspaceId)"
      >
        <div v-if="summarySection?.data" class="dm-bucket-list">
          <div v-for="bucket in summarySection.data.buckets" :key="bucket.status" class="dm-bucket">
            <strong>{{ bucket.status }}</strong>
            <p class="text-body-sm">{{ bucket.count }} specs · {{ Math.round(bucket.percentage) }}%</p>
          </div>
        </div>
      </DesignManagementCardShell>

      <DesignManagementCardShell
        title="Spec Gaps"
        :loading="designManagementStore.traceabilityLoading"
        :error="gapsSection?.error ?? null"
        @retry="designManagementStore.initTraceability(resolvedWorkspaceId)"
      >
        <div v-if="gapsSection?.data" class="dm-list">
          <button
            v-for="gap in gapsSection.data"
            :key="gap.specId"
            class="dm-row dm-row--compact"
            @click="openRequirement(gap.requirementRoute)"
          >
            <div>
              <strong>{{ gap.specId }} · {{ gap.specTitle }}</strong>
              <p class="text-body-sm">{{ gap.projectName }} · latest revision {{ gap.latestRevision }}</p>
            </div>
            <span class="dm-pill">Gap</span>
          </button>
        </div>
      </DesignManagementCardShell>

      <DesignManagementCardShell
        title="Traceability Matrix"
        :loading="designManagementStore.traceabilityLoading"
        :error="matrixSection?.error ?? null"
        :full-width="true"
        @retry="designManagementStore.initTraceability(resolvedWorkspaceId)"
      >
        <div v-if="matrixSection?.data" class="dm-list">
          <div v-for="row in matrixSection.data" :key="row.specId" class="dm-matrix-row">
            <button class="dm-matrix-row__spec" @click="openRequirement(row.requirementRoute)">
              <strong>{{ row.specId }}</strong>
              <p class="text-body-sm">{{ row.specTitle }}</p>
            </button>
            <div class="dm-chip-row">
              <button
                v-for="cell in row.cells"
                :key="`${row.specId}:${cell.artifactId}`"
                class="dm-pill dm-pill--action"
                @click="openArtifact(cell.viewerRoute)"
              >
                {{ cell.artifactTitle }} · {{ cell.coverageStatus }}
              </button>
              <span v-if="row.cells.length === 0" class="dm-pill">No linked artifact</span>
            </div>
          </div>
        </div>
      </DesignManagementCardShell>
    </div>
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

.dm-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 18px;
}

.dm-grid > *:not(.dm-card--full) {
  grid-column: span 6;
}

.dm-bucket-list,
.dm-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dm-bucket,
.dm-row,
.dm-matrix-row {
  padding: 12px 14px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(137, 206, 255, 0.12);
  background: rgba(255, 255, 255, 0.02);
}

.dm-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: inherit;
  text-align: left;
}

.dm-row--compact,
.dm-matrix-row__spec,
.dm-pill--action {
  cursor: pointer;
}

.dm-matrix-row {
  display: grid;
  grid-template-columns: minmax(220px, 280px) 1fr;
  gap: 14px;
  align-items: center;
}

.dm-matrix-row__spec {
  color: inherit;
  text-align: left;
  background: transparent;
  border: 0;
}

.dm-chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.dm-pill {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(137, 206, 255, 0.18);
  background: rgba(137, 206, 255, 0.08);
  font-size: 0.75rem;
}

.dm-pill--action {
  color: inherit;
}

@media (max-width: 1024px) {
  .dm-grid {
    grid-template-columns: 1fr;
  }

  .dm-grid > *:not(.dm-card--full) {
    grid-column: auto;
  }

  .dm-matrix-row {
    grid-template-columns: 1fr;
  }
}
</style>
