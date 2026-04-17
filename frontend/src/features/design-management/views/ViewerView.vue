<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import DesignManagementCardShell from '../components/DesignManagementCardShell.vue';
import { useDesignManagementStore } from '../stores/designManagementStore';
import { DM_ARTIFACT_ID_PATTERN } from '../types';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const designManagementStore = useDesignManagementStore();

const resolvedArtifactId = computed(() => {
  const paramValue = route.params.artifactId;
  return typeof paramValue === 'string' ? paramValue : '';
});

const selectedVersion = computed(() => {
  const value = route.query.version;
  return typeof value === 'string' && value.trim() ? value : null;
});

const headerSection = computed(() => designManagementStore.viewer?.header);
const versionsSection = computed(() => designManagementStore.viewer?.versions);
const linkedSpecsSection = computed(() => designManagementStore.viewer?.linkedSpecs);
const aiSummarySection = computed(() => designManagementStore.viewer?.aiSummary);
const changeLogSection = computed(() => designManagementStore.viewer?.changeLog);

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

function selectVersion(versionId: string) {
  void router.replace({
    name: 'design-management-artifact',
    params: { artifactId: resolvedArtifactId.value },
    query: {
      ...route.query,
      version: versionId,
    },
  });
}

function openRequirement(path: string | null) {
  if (!path) {
    return;
  }
  void router.push(path);
}

watch(
  () => [resolvedArtifactId.value, selectedVersion.value],
  ([artifactId, versionId]) => {
    if (typeof artifactId !== 'string' || !artifactId || !DM_ARTIFACT_ID_PATTERN.test(artifactId)) {
      return;
    }
    void designManagementStore.openArtifact(artifactId, versionId ?? undefined);
  },
  { immediate: true },
);

watch(
  () => designManagementStore.viewer,
  viewer => {
    const header = viewer?.header.data;
    workspaceStore.setRouteContext({
      workspace: header?.workspaceId ?? workspaceStore.context.workspace,
      application: 'Design Management',
      snowGroup: workspaceStore.context.snowGroup,
      project: header?.projectName ?? null,
      environment: null,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Design Management', path: '/design-management' },
      { label: header?.title ?? resolvedArtifactId.value },
    ]);

    shellUiStore.setAiPanelContent({
      summary: viewer?.aiSummary.data?.summaryText ?? 'Previewing artifact context, linked specs, and AI design summary.',
      reasoning: [
        { text: `${viewer?.linkedSpecs.data?.length ?? 0} linked specs loaded`, status: 'ok' },
        { text: `AI summary status: ${viewer?.aiSummary.data?.status ?? 'PENDING'}`, status: viewer?.aiSummary.data?.status === 'FAILED' ? 'warning' : 'ok' },
        { text: `Selected version: ${viewer?.header.data?.currentVersionId ?? selectedVersion.value ?? 'n/a'}`, status: 'ok' },
      ],
      evidence: JSON.stringify(
        {
          artifactId: resolvedArtifactId.value,
          version: selectedVersion.value,
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
    <div v-if="designManagementStore.viewerError && !designManagementStore.viewer" class="dm-page-error">
      <p class="text-label">
        {{ designManagementStore.isViewerNotFound ? 'Artifact Not Found' : designManagementStore.isViewerForbidden ? 'Access Denied' : 'Viewer Unavailable' }}
      </p>
      <p class="text-body-sm">{{ designManagementStore.viewerError }}</p>
      <button class="btn-machined" @click="designManagementStore.openArtifact(resolvedArtifactId, selectedVersion)">Retry</button>
    </div>

    <template v-else>
      <div class="dm-grid">
        <DesignManagementCardShell
          title="Artifact Header"
          :loading="designManagementStore.viewerLoading"
          :error="headerSection?.error ?? null"
          :full-width="true"
          @retry="designManagementStore.openArtifact(resolvedArtifactId, selectedVersion)"
        >
          <div v-if="headerSection?.data" class="dm-header">
            <div>
              <p class="text-label">{{ headerSection.data.projectId }} · {{ headerSection.data.format }}</p>
              <h2>{{ headerSection.data.title }}</h2>
              <p class="text-body-sm">
                {{ headerSection.data.lifecycle }} · registered by {{ headerSection.data.registeredBy.displayName }} · updated {{ formatTimestamp(headerSection.data.lastUpdatedAt) }}
              </p>
            </div>
            <div class="dm-chip-row">
              <span class="dm-pill">{{ headerSection.data.currentVersionId }}</span>
              <button class="btn-machined" @click="router.push({ name: 'design-management-traceability', query: { workspaceId: headerSection.data.workspaceId } })">Traceability</button>
            </div>
          </div>
        </DesignManagementCardShell>

        <DesignManagementCardShell
          title="Preview"
          :loading="designManagementStore.viewerLoading"
          :error="headerSection?.error ?? null"
          :full-width="true"
          @retry="designManagementStore.openArtifact(resolvedArtifactId, selectedVersion)"
        >
          <iframe
            v-if="headerSection?.data"
            :src="headerSection.data.rawUrl"
            class="dm-preview"
            title="Design artifact preview"
          />
        </DesignManagementCardShell>

        <DesignManagementCardShell
          title="Versions"
          :loading="designManagementStore.viewerLoading"
          :error="versionsSection?.error ?? null"
          @retry="designManagementStore.openArtifact(resolvedArtifactId, selectedVersion)"
        >
          <div v-if="versionsSection?.data" class="dm-list">
            <button
              v-for="version in versionsSection.data"
              :key="version.versionId"
              class="dm-row dm-row--compact"
              :class="{ 'dm-row--active': version.current || version.versionId === selectedVersion }"
              @click="selectVersion(version.versionId)"
            >
              <div>
                <strong>{{ version.label }}</strong>
                <p class="text-body-sm">{{ version.changeLogNote || 'No note' }}</p>
              </div>
              <span class="text-body-sm">{{ Math.round(version.sizeBytes / 1024) || 1 }} KB</span>
            </button>
          </div>
        </DesignManagementCardShell>

        <DesignManagementCardShell
          title="Linked Specs"
          :loading="designManagementStore.viewerLoading"
          :error="linkedSpecsSection?.error ?? null"
          @retry="designManagementStore.openArtifact(resolvedArtifactId, selectedVersion)"
        >
          <div v-if="linkedSpecsSection?.data" class="dm-list">
            <button
              v-for="spec in linkedSpecsSection.data"
              :key="spec.linkId"
              class="dm-row dm-row--compact"
              @click="openRequirement(spec.requirementRoute)"
            >
              <div>
                <strong>{{ spec.specId }} · {{ spec.specTitle }}</strong>
                <p class="text-body-sm">{{ spec.why }}</p>
              </div>
              <span class="dm-pill dm-pill--coverage">{{ spec.coverageStatus }}</span>
            </button>
          </div>
        </DesignManagementCardShell>

        <DesignManagementCardShell
          title="AI Summary"
          :loading="designManagementStore.viewerLoading"
          :error="aiSummarySection?.error ?? null"
          @retry="designManagementStore.openArtifact(resolvedArtifactId, selectedVersion)"
        >
          <div v-if="aiSummarySection?.data" class="dm-list">
            <p class="text-body-sm">{{ aiSummarySection.data.summaryText || 'AI summary is still pending.' }}</p>
            <div class="dm-chip-row">
              <span class="dm-pill">{{ aiSummarySection.data.status }}</span>
              <span class="dm-pill">{{ aiSummarySection.data.skillVersion }}</span>
            </div>
            <ul v-if="aiSummarySection.data.keyElements.length" class="dm-bullets">
              <li v-for="item in aiSummarySection.data.keyElements" :key="item">{{ item }}</li>
            </ul>
          </div>
        </DesignManagementCardShell>

        <DesignManagementCardShell
          title="Change Log"
          :loading="designManagementStore.viewerLoading"
          :error="changeLogSection?.error ?? null"
          :full-width="true"
          @retry="designManagementStore.openArtifact(resolvedArtifactId, selectedVersion)"
        >
          <div v-if="changeLogSection?.data" class="dm-list">
            <div v-for="entry in changeLogSection.data" :key="entry.id" class="dm-log-row">
              <div>
                <strong>{{ entry.entryType }}</strong>
                <p class="text-body-sm">{{ entry.reason || 'No reason recorded' }}</p>
              </div>
              <span class="text-body-sm">{{ entry.actorDisplayName }} · {{ formatTimestamp(entry.occurredAt) }}</span>
            </div>
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

.dm-page-error {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
}

.dm-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 18px;
}

.dm-grid > *:not(.dm-card--full) {
  grid-column: span 6;
}

.dm-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.dm-preview {
  width: 100%;
  min-height: 520px;
  border: 1px solid rgba(137, 206, 255, 0.18);
  border-radius: var(--radius-md);
  background: white;
}

.dm-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dm-row,
.dm-log-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 12px 14px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(137, 206, 255, 0.12);
  background: rgba(255, 255, 255, 0.02);
  color: inherit;
  text-align: left;
}

.dm-row--compact {
  cursor: pointer;
}

.dm-row--active {
  border-color: rgba(137, 206, 255, 0.45);
  background: rgba(137, 206, 255, 0.08);
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

.dm-bullets {
  margin-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

@media (max-width: 1024px) {
  .dm-grid {
    grid-template-columns: 1fr;
  }

  .dm-grid > *:not(.dm-card--full) {
    grid-column: auto;
  }

  .dm-header,
  .dm-row,
  .dm-log-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
