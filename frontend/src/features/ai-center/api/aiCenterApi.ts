import { fetchJson } from '@/shared/api/client';
import type {
  ApiResponse,
  MetricsSummary,
  Page,
  Run,
  RunDetail,
  RunDetailResponse,
  RunQuery,
  Skill,
  SkillDetail,
  SkillDetailResponse,
  StageCoverage,
  StageCoverageResponse,
  AiCenterWindow,
} from '../types';
import {
  mockGetMetrics,
  mockGetRunDetail,
  mockGetRuns,
  mockGetSkillDetail,
  mockGetSkills,
  mockGetStageCoverage,
} from './mocks';

const USE_MOCK_API = import.meta.env.VITE_USE_MOCK_API !== 'false';

function headers(workspaceId: string): HeadersInit {
  return {
    'X-Workspace-Id': workspaceId,
  };
}

function unwrapData<T>(response: ApiResponse<T>): T {
  if (response.error || !response.data) {
    throw new Error(response.error ?? 'No data');
  }
  return response.data;
}

function buildRunQueryString(query: RunQuery = {}): string {
  const params = new URLSearchParams();
  (query.skillKey ?? []).forEach(value => params.append('skillKey', value));
  (query.status ?? []).forEach(value => params.append('status', value));
  if (query.triggerSourcePage) params.set('triggerSourcePage', query.triggerSourcePage);
  if (query.startedAfter) params.set('startedAfter', query.startedAfter);
  if (query.startedBefore) params.set('startedBefore', query.startedBefore);
  if (query.triggeredByType) params.set('triggeredByType', query.triggeredByType);
  params.set('page', String(query.page ?? 1));
  params.set('size', String(query.size ?? 50));
  return params.toString();
}

export const aiCenterApi = {
  async getMetrics(workspaceId: string, window: AiCenterWindow = '30d'): Promise<MetricsSummary> {
    if (USE_MOCK_API) {
      return unwrapData(await mockGetMetrics(workspaceId));
    }
    return fetchJson<MetricsSummary>(`/ai-center/metrics?window=${window}`, {
      headers: headers(workspaceId),
    });
  },

  async getStageCoverage(workspaceId: string): Promise<StageCoverage> {
    if (USE_MOCK_API) {
      return unwrapData(await mockGetStageCoverage(workspaceId)).entries;
    }
    const response = await fetchJson<StageCoverageResponse>('/ai-center/stage-coverage', {
      headers: headers(workspaceId),
    });
    return response.entries;
  },

  async getSkills(workspaceId: string): Promise<Skill[]> {
    if (USE_MOCK_API) {
      return unwrapData(await mockGetSkills(workspaceId));
    }
    return fetchJson<Skill[]>('/ai-center/skills', {
      headers: headers(workspaceId),
    });
  },

  async getSkillDetail(workspaceId: string, skillKey: string): Promise<SkillDetail> {
    if (USE_MOCK_API) {
      const response = unwrapData(await mockGetSkillDetail(workspaceId, skillKey));
      return {
        ...response.skill,
        inputContract: response.inputContract,
        outputContract: response.outputContract,
        policy: response.policy,
        recentRuns: response.recentRuns,
        aggregateMetrics: response.aggregateMetrics,
      };
    }

    const response = await fetchJson<SkillDetailResponse>(`/ai-center/skills/${encodeURIComponent(skillKey)}`, {
      headers: headers(workspaceId),
    });
    return {
      ...response.skill,
      inputContract: response.inputContract,
      outputContract: response.outputContract,
      policy: response.policy,
      recentRuns: response.recentRuns,
      aggregateMetrics: response.aggregateMetrics,
    };
  },

  async getRuns(workspaceId: string, query: RunQuery = {}): Promise<Page<Run>> {
    if (USE_MOCK_API) {
      return unwrapData(await mockGetRuns(workspaceId, query));
    }
    const suffix = buildRunQueryString(query);
    return fetchJson<Page<Run>>(`/ai-center/runs?${suffix}`, {
      headers: headers(workspaceId),
    });
  },

  async getRunDetail(workspaceId: string, executionId: string): Promise<RunDetail> {
    if (USE_MOCK_API) {
      const response = unwrapData(await mockGetRunDetail(workspaceId, executionId));
      return {
        ...response.run,
        inputSummary: response.inputSummary,
        outputSummary: response.outputSummary,
        stepBreakdown: response.stepBreakdown,
        policyTrail: response.policyTrail,
        evidenceLinks: response.evidenceLinks,
        autonomyLevel: response.autonomyLevel,
        timeSavedMinutes: response.timeSavedMinutes,
      };
    }

    const response = await fetchJson<RunDetailResponse>(`/ai-center/runs/${encodeURIComponent(executionId)}`, {
      headers: headers(workspaceId),
    });
    return {
      ...response.run,
      inputSummary: response.inputSummary,
      outputSummary: response.outputSummary,
      stepBreakdown: response.stepBreakdown,
      policyTrail: response.policyTrail,
      evidenceLinks: response.evidenceLinks,
      autonomyLevel: response.autonomyLevel,
      timeSavedMinutes: response.timeSavedMinutes,
    };
  },
};
