import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { CatalogAggregate, CatalogFilters } from '../types/catalog';
import type { ApplicationDetailAggregate } from '../types/application';
import type { ReleaseDetailAggregate } from '../types/release';
import type { DeployDetailAggregate } from '../types/deploy';
import type { EnvironmentDetailAggregate } from '../types/environment';
import type { TraceabilityAggregate } from '../types/traceability';
import type { DeploymentViewerContext } from '../types/aggregate';
import { DP_DEFAULT_WORKSPACE_ID, DP_DEFAULT_ROLE, DP_DEFAULT_AUTONOMY, DP_DEFAULT_CATALOG_FILTERS } from '../types/aggregate';
import { deploymentApi } from '../api/deploymentApi';

export const useDeploymentStore = defineStore('deployment', () => {
  // --- Viewer context ---
  const viewerContext = ref<DeploymentViewerContext>({
    workspaceId: DP_DEFAULT_WORKSPACE_ID,
    role: DP_DEFAULT_ROLE,
    autonomyLevel: DP_DEFAULT_AUTONOMY,
  });

  // --- Catalog ---
  const catalogFilters = ref<CatalogFilters>({ ...DP_DEFAULT_CATALOG_FILTERS });
  const catalog = ref<CatalogAggregate | null>(null);
  const catalogLoading = ref(false);

  async function initCatalog(filters?: CatalogFilters): Promise<void> {
    if (filters) catalogFilters.value = { ...filters };
    catalogLoading.value = true;
    try {
      catalog.value = await deploymentApi.loadCatalog(catalogFilters.value);
    } catch (e: unknown) {
      catalog.value = {
        summary: { data: null, error: String(e) },
        grid: { data: null, error: String(e) },
        aiSummary: { data: null, error: String(e) },
        filtersEcho: catalogFilters.value,
      };
    } finally {
      catalogLoading.value = false;
    }
  }

  // --- Application Detail ---
  const activeApplicationId = ref<string | null>(null);
  const applicationDetail = ref<ApplicationDetailAggregate | null>(null);

  async function openApplication(applicationId: string): Promise<void> {
    activeApplicationId.value = applicationId;
    applicationDetail.value = null;
    try {
      applicationDetail.value = await deploymentApi.loadApplicationDetail(applicationId);
    } catch (e: unknown) {
      const errSection = { data: null, error: String(e) };
      applicationDetail.value = {
        header: errSection, environments: errSection,
        recentReleases: errSection, recentDeploys: errSection,
        traceSummary: errSection, aiInsights: errSection,
      };
    }
  }

  // --- Release Detail ---
  const activeReleaseId = ref<string | null>(null);
  const releaseDetail = ref<ReleaseDetailAggregate | null>(null);

  async function openRelease(releaseId: string): Promise<void> {
    activeReleaseId.value = releaseId;
    releaseDetail.value = null;
    try {
      releaseDetail.value = await deploymentApi.loadReleaseDetail(releaseId);
    } catch (e: unknown) {
      const errSection = { data: null, error: String(e) };
      releaseDetail.value = {
        header: errSection, commits: errSection,
        linkedStories: errSection, deploys: errSection, aiNotes: errSection,
      };
    }
  }

  // --- Deploy Detail ---
  const activeDeployId = ref<string | null>(null);
  const deployDetail = ref<DeployDetailAggregate | null>(null);
  let deployPollTimer: ReturnType<typeof setInterval> | undefined;

  async function openDeploy(deployId: string): Promise<void> {
    activeDeployId.value = deployId;
    deployDetail.value = null;
    stopDeployPolling();
    try {
      deployDetail.value = await deploymentApi.loadDeployDetail(deployId);
      if (deployDetail.value.header.data?.state === 'IN_PROGRESS') {
        startDeployPolling(deployId);
      }
    } catch (e: unknown) {
      const errSection = { data: null, error: String(e) };
      deployDetail.value = {
        header: errSection, stageTimeline: errSection,
        approvals: errSection, artifactRef: errSection,
        openIncidentContext: { applicationId: '', environmentName: '', deployId, releaseVersion: '', deployUrl: '' },
      };
    }
  }

  function startDeployPolling(deployId: string): void {
    stopDeployPolling();
    deployPollTimer = setInterval(async () => {
      if (activeDeployId.value !== deployId) { stopDeployPolling(); return; }
      try {
        const fresh = await deploymentApi.loadDeployDetail(deployId);
        deployDetail.value = fresh;
        if (fresh.header.data?.state !== 'IN_PROGRESS') stopDeployPolling();
      } catch { /* swallow polling errors */ }
    }, 5000);
  }

  function stopDeployPolling(): void {
    if (deployPollTimer) { clearInterval(deployPollTimer); deployPollTimer = undefined; }
  }

  // --- Environment Detail ---
  const activeEnvironmentKey = ref<string | null>(null);
  const environmentDetail = ref<EnvironmentDetailAggregate | null>(null);

  async function openEnvironment(applicationId: string, environmentName: string): Promise<void> {
    activeEnvironmentKey.value = `${applicationId}:${environmentName}`;
    environmentDetail.value = null;
    try {
      environmentDetail.value = await deploymentApi.loadEnvironmentDetail(applicationId, environmentName);
    } catch (e: unknown) {
      const errSection = { data: null, error: String(e) };
      environmentDetail.value = {
        header: errSection, revisions: errSection,
        timeline: errSection, metrics: errSection,
      };
    }
  }

  // --- Traceability ---
  const activeStoryId = ref<string | null>(null);
  const traceability = ref<TraceabilityAggregate | null>(null);

  async function lookupStory(storyId: string): Promise<void> {
    activeStoryId.value = storyId;
    traceability.value = null;
    try {
      traceability.value = await deploymentApi.lookupStory(storyId);
    } catch (e: unknown) {
      traceability.value = {
        storyChip: { storyId, status: 'UNKNOWN_STORY' },
        releases: { data: null, error: String(e) },
        deploysByEnvironment: { data: null, error: String(e) },
        upstreamAvailable: false,
      };
    }
  }

  // --- AI Regenerate ---
  async function regenerateWorkspaceSummary(): Promise<void> {
    try {
      await deploymentApi.regenerateWorkspaceSummary(viewerContext.value.workspaceId);
      await initCatalog();
    } catch { /* error surfaced via toast */ }
  }

  async function regenerateReleaseNotes(releaseId: string): Promise<void> {
    try {
      await deploymentApi.regenerateReleaseNotes(releaseId);
      if (activeReleaseId.value === releaseId) await openRelease(releaseId);
    } catch { /* error surfaced via toast */ }
  }

  async function regenerateDeploySummary(deployId: string): Promise<void> {
    try {
      await deploymentApi.regenerateDeploySummary(deployId);
      if (activeDeployId.value === deployId) await openDeploy(deployId);
    } catch { /* error surfaced via toast */ }
  }

  // --- Reset ---
  function reset(): void {
    stopDeployPolling();
    catalog.value = null;
    catalogFilters.value = { ...DP_DEFAULT_CATALOG_FILTERS };
    activeApplicationId.value = null;
    applicationDetail.value = null;
    activeReleaseId.value = null;
    releaseDetail.value = null;
    activeDeployId.value = null;
    deployDetail.value = null;
    activeEnvironmentKey.value = null;
    environmentDetail.value = null;
    activeStoryId.value = null;
    traceability.value = null;
  }

  return {
    viewerContext,
    catalogFilters,
    catalog,
    catalogLoading,
    initCatalog,
    activeApplicationId,
    applicationDetail,
    openApplication,
    activeReleaseId,
    releaseDetail,
    openRelease,
    activeDeployId,
    deployDetail,
    openDeploy,
    startDeployPolling,
    stopDeployPolling,
    activeEnvironmentKey,
    environmentDetail,
    openEnvironment,
    activeStoryId,
    traceability,
    lookupStory,
    regenerateWorkspaceSummary,
    regenerateReleaseNotes,
    regenerateDeploySummary,
    reset,
  };
});
