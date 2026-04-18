<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter, RouterView } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import AdoptionMetricsCard from './components/AdoptionMetricsCard.vue';
import StageCoverageCard from './components/StageCoverageCard.vue';
import SkillCatalogCard from './components/SkillCatalogCard.vue';
import RunHistoryCard from './components/RunHistoryCard.vue';
import { useAiCenterStore } from './stores/aiCenterStore';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const aiCenterStore = useAiCenterStore();

const resolvedWorkspaceId = computed(() => workspaceStore.context.workspace || 'ws-default-001');
const selectedSkillKey = computed(() => route.name === 'ai-center-skill-detail' ? String(route.params.skillKey ?? '') : null);
const selectedRunId = computed(() => route.name === 'ai-center-run-detail' ? String(route.params.executionId ?? '') : null);

function retryAll() {
  void aiCenterStore.init(resolvedWorkspaceId.value);
}

watch(
  () => resolvedWorkspaceId.value,
  workspaceId => {
    if (workspaceId) {
      void aiCenterStore.init(workspaceId);
    }
  },
  { immediate: true },
);

watch(
  () => [resolvedWorkspaceId.value, aiCenterStore.metrics.data, aiCenterStore.skills.data, aiCenterStore.runs.data],
  () => {
    workspaceStore.setRouteContext({
      workspace: resolvedWorkspaceId.value,
      application: 'AI Center',
      snowGroup: workspaceStore.context.snowGroup,
      project: null,
      environment: null,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'AI Center', path: '/ai-center' },
      { label: resolvedWorkspaceId.value },
    ]);

    const skillCount = aiCenterStore.skills.data?.length ?? 0;
    const runCount = aiCenterStore.runs.data?.total ?? 0;
    const coverage = aiCenterStore.stageCoverage.data?.filter(entry => entry.covered).length ?? 0;

    shellUiStore.setAiPanelContent({
      summary: `${skillCount} registered skills and ${runCount} runs are in scope for ${resolvedWorkspaceId.value}. ${coverage} SDLC stages are currently covered.`,
      reasoning: [
        {
          text: aiCenterStore.metrics.data?.adoptionRate.data
            ? `Adoption rate is ${aiCenterStore.metrics.data.adoptionRate.data.value.toFixed(1)}% in the current 30-day window`
            : 'Adoption metric is still warming up',
          status: aiCenterStore.metrics.data?.adoptionRate.error ? 'warning' : 'ok',
        },
        {
          text: aiCenterStore.runs.error
            ? aiCenterStore.runs.error
            : aiCenterStore.runs.data?.hasMore
              ? 'Run history spans multiple pages; additional context is available on demand'
              : 'Run history is fully loaded in the current window',
          status: aiCenterStore.runs.error ? 'error' : 'ok',
        },
        {
          text: aiCenterStore.stageCoverage.data?.some(entry => !entry.covered)
            ? 'Some SDLC stages still lack active skill coverage'
            : 'All canonical SDLC stages are covered by at least one active or beta skill',
          status: aiCenterStore.stageCoverage.data?.some(entry => !entry.covered) ? 'warning' : 'ok',
        },
      ],
      evidence: JSON.stringify(
        {
          workspaceId: resolvedWorkspaceId.value,
          route: route.fullPath,
          selectedSkillKey: selectedSkillKey.value,
          selectedRunId: selectedRunId.value,
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
  aiCenterStore.reset();
});
</script>

<template>
  <div class="ai-center-page">
    <div class="ai-center-hero">
      <div>
        <p class="text-label">Capability Layer</p>
        <h2>AI Center</h2>
        <p class="ai-center-hero__copy">
          Monitor adoption, inspect skill posture, and drill into execution evidence without leaving the workspace.
        </p>
      </div>
      <button class="btn-machined" type="button" @click="retryAll">Retry All</button>
    </div>

    <div v-if="aiCenterStore.hasGlobalError" class="ai-center-banner">
      <p class="text-label">AI Center unavailable</p>
      <p class="text-body-sm">All cards failed to load. Retry the page-level requests once workspace context is stable.</p>
      <button class="btn-machined" type="button" @click="retryAll">Retry all</button>
    </div>

    <div class="ai-center-stack">
      <AdoptionMetricsCard
        :metrics="aiCenterStore.metrics.data"
        :loading="aiCenterStore.metrics.loading"
        :error="aiCenterStore.metrics.error"
        @retry="aiCenterStore.refetchMetrics"
      />

      <StageCoverageCard
        :coverage="aiCenterStore.stageCoverage.data"
        :loading="aiCenterStore.stageCoverage.loading"
        :error="aiCenterStore.stageCoverage.error"
        @retry="aiCenterStore.refetchStageCoverage"
      />

      <SkillCatalogCard
        :skills="aiCenterStore.skills.data ?? []"
        :loading="aiCenterStore.skills.loading"
        :error="aiCenterStore.skills.error"
        :filters="aiCenterStore.skillFilters"
        :selected-skill-key="selectedSkillKey"
        @retry="aiCenterStore.refetchSkills"
        @set-filters="aiCenterStore.setSkillFilters"
        @select="router.push(`/ai-center/skills/${$event}`)"
      />

      <RunHistoryCard
        :runs="aiCenterStore.runs.data"
        :loading="aiCenterStore.runs.loading"
        :error="aiCenterStore.runs.error"
        :filters="aiCenterStore.runFilters"
        :skills="aiCenterStore.skills.data ?? []"
        :selected-run-id="selectedRunId"
        @retry="aiCenterStore.refetchRuns({ resetPage: true })"
        @refresh="aiCenterStore.refetchRuns()"
        @change-filters="aiCenterStore.setRunFilters($event)"
        @load-more="aiCenterStore.loadMoreRuns"
        @select="router.push(`/ai-center/runs/${$event}`)"
      />
    </div>

    <RouterView />
  </div>
</template>

<style scoped>
.ai-center-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 24px;
  min-height: 100%;
}

.ai-center-hero,
.ai-center-banner {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
  background: linear-gradient(135deg, rgba(137, 206, 255, 0.08), rgba(255, 255, 255, 0.02));
}

.ai-center-banner {
  color: var(--color-incident-crimson);
  background: rgba(255, 180, 171, 0.08);
}

.ai-center-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ai-center-hero__copy {
  margin-top: 6px;
  max-width: 70ch;
  color: var(--color-on-surface-variant);
}
</style>
