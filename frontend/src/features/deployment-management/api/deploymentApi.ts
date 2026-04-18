import { fetchJson, postJson } from '@/shared/api/client';
import type { CatalogAggregate, CatalogFilters } from '../types/catalog';
import type { ApplicationDetailAggregate } from '../types/application';
import type { ReleaseDetailAggregate } from '../types/release';
import type { DeployDetailAggregate } from '../types/deploy';
import type { EnvironmentDetailAggregate } from '../types/environment';
import type { TraceabilityAggregate } from '../types/traceability';
import type { RegenerateReleaseNotesResponse, RegenerateDeploySummaryResponse, RegenerateWorkspaceSummaryResponse } from '../types/requests';

import { MOCK_CATALOG_SECTIONS } from '../mock/catalog.mock';
import { MOCK_CATALOG_SUMMARY, MOCK_AI_WORKSPACE_SUMMARY } from '../mock/catalogSummary.mock';
import { MOCK_APPLICATION_MAP } from '../mock/applicationDetail.mock';
import { MOCK_RELEASE_MAP } from '../mock/releaseDetail.mock';
import { MOCK_DEPLOY_MAP } from '../mock/deployDetail.mock';
import { MOCK_ENVIRONMENT_MAP } from '../mock/environmentDetail.mock';
import { buildTraceability } from '../mock/traceability.mock';

const USE_BACKEND = import.meta.env.VITE_USE_BACKEND === 'true';
const BASE = '/deployment-management';

function delay(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms));
}

// ---------- Live client ----------

const liveClient = {
  async loadCatalog(filters: CatalogFilters): Promise<CatalogAggregate> {
    const qs = new URLSearchParams();
    if (filters.window) qs.set('window', filters.window);
    if (filters.search) qs.set('search', filters.search);
    if (filters.environmentKind) qs.set('environmentKind', filters.environmentKind);
    if (filters.deployStatus) qs.set('deployStatus', filters.deployStatus);
    if (filters.projectIds?.length) qs.set('projectIds', filters.projectIds.join(','));
    return fetchJson<CatalogAggregate>(`${BASE}/catalog?${qs}`);
  },

  async loadApplicationDetail(applicationId: string): Promise<ApplicationDetailAggregate> {
    return fetchJson<ApplicationDetailAggregate>(`${BASE}/applications/${applicationId}`);
  },

  async loadReleaseDetail(releaseId: string): Promise<ReleaseDetailAggregate> {
    return fetchJson<ReleaseDetailAggregate>(`${BASE}/releases/${releaseId}`);
  },

  async loadDeployDetail(deployId: string): Promise<DeployDetailAggregate> {
    return fetchJson<DeployDetailAggregate>(`${BASE}/deploys/${deployId}`);
  },

  async loadEnvironmentDetail(applicationId: string, environmentName: string): Promise<EnvironmentDetailAggregate> {
    return fetchJson<EnvironmentDetailAggregate>(`${BASE}/applications/${applicationId}/environments/${environmentName}`);
  },

  async lookupStory(storyId: string): Promise<TraceabilityAggregate> {
    return fetchJson<TraceabilityAggregate>(`${BASE}/traceability?storyId=${encodeURIComponent(storyId)}`);
  },

  async regenerateReleaseNotes(releaseId: string): Promise<RegenerateReleaseNotesResponse> {
    return postJson<RegenerateReleaseNotesResponse>(`${BASE}/releases/${releaseId}/ai-notes/regenerate`);
  },

  async regenerateDeploySummary(deployId: string): Promise<RegenerateDeploySummaryResponse> {
    return postJson<RegenerateDeploySummaryResponse>(`${BASE}/deploys/${deployId}/ai-summary/regenerate`);
  },

  async regenerateWorkspaceSummary(workspaceId: string): Promise<RegenerateWorkspaceSummaryResponse> {
    return postJson<RegenerateWorkspaceSummaryResponse>(`${BASE}/workspaces/${workspaceId}/ai-summary/regenerate`);
  },
};

// ---------- Mock client ----------

const mockClient = {
  async loadCatalog(filters: CatalogFilters): Promise<CatalogAggregate> {
    await delay(300);
    let sections = [...MOCK_CATALOG_SECTIONS];
    if (filters.search) {
      const q = filters.search.toLowerCase();
      sections = sections.map(s => ({
        ...s,
        applications: s.applications.filter(a => a.name.toLowerCase().includes(q)),
      })).filter(s => s.applications.length > 0);
    }
    return {
      summary: { data: MOCK_CATALOG_SUMMARY, error: null },
      grid: { data: sections, error: null },
      aiSummary: { data: MOCK_AI_WORKSPACE_SUMMARY, error: null },
      filtersEcho: filters,
    };
  },

  async loadApplicationDetail(applicationId: string): Promise<ApplicationDetailAggregate> {
    await delay(250);
    const agg = MOCK_APPLICATION_MAP[applicationId];
    if (!agg) throw new Error(`Application not found: ${applicationId}`);
    return agg;
  },

  async loadReleaseDetail(releaseId: string): Promise<ReleaseDetailAggregate> {
    await delay(250);
    const agg = MOCK_RELEASE_MAP[releaseId];
    if (!agg) throw new Error(`Release not found: ${releaseId}`);
    return agg;
  },

  async loadDeployDetail(deployId: string): Promise<DeployDetailAggregate> {
    await delay(250);
    const agg = MOCK_DEPLOY_MAP[deployId];
    if (!agg) throw new Error(`Deploy not found: ${deployId}`);
    return agg;
  },

  async loadEnvironmentDetail(applicationId: string, environmentName: string): Promise<EnvironmentDetailAggregate> {
    await delay(200);
    const key = `${applicationId}:${environmentName}`;
    const agg = MOCK_ENVIRONMENT_MAP[key];
    if (!agg) throw new Error(`Environment not found: ${key}`);
    return agg;
  },

  async lookupStory(storyId: string): Promise<TraceabilityAggregate> {
    await delay(200);
    return buildTraceability(storyId);
  },

  async regenerateReleaseNotes(releaseId: string): Promise<RegenerateReleaseNotesResponse> {
    await delay(800);
    return { releaseId, status: 'PENDING', generatedAt: new Date().toISOString() };
  },

  async regenerateDeploySummary(deployId: string): Promise<RegenerateDeploySummaryResponse> {
    await delay(800);
    return { deployId, status: 'PENDING', generatedAt: new Date().toISOString() };
  },

  async regenerateWorkspaceSummary(workspaceId: string): Promise<RegenerateWorkspaceSummaryResponse> {
    await delay(800);
    return { workspaceId, status: 'PENDING', generatedAt: new Date().toISOString() };
  },
};

export const deploymentApi = USE_BACKEND ? liveClient : mockClient;
