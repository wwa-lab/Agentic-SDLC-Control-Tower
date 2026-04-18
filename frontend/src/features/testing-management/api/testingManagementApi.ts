import { fetchJson } from '@/shared/api/client';
import type {
  CaseCardKey,
  CaseDetailAggregate,
  CatalogAggregate,
  CatalogCardKey,
  CatalogFilter,
  PlanCardKey,
  PlanDetailAggregate,
  RunCardKey,
  RunDetailAggregate,
  TraceabilityAggregate,
  TraceabilityCardKey,
} from '../types';
import {
  readCaseAggregate,
  readCaseSection,
  readCatalogAggregate,
  readCatalogSection,
  readPlanAggregate,
  readPlanSection,
  readRunAggregate,
  readRunSection,
  readTraceabilityAggregate,
  readTraceabilitySection,
} from '../mock/testingMockApi';

export const TM_USE_MOCK = import.meta.env.DEV && !import.meta.env.VITE_USE_BACKEND;
const MOCK_LATENCY = import.meta.env.MODE === 'test' ? 0 : 90;

function wait(ms: number) {
  return new Promise(resolve => {
    window.setTimeout(resolve, ms);
  });
}

async function withMockLatency<T>(work: () => Promise<T>) {
  if (MOCK_LATENCY > 0) {
    await wait(MOCK_LATENCY);
  }
  return work();
}

function buildSearchParams(entries: Record<string, string | null | undefined>) {
  const params = new URLSearchParams();
  Object.entries(entries).forEach(([key, value]) => {
    if (!value) {
      return;
    }
    params.set(key, value);
  });
  const serialized = params.toString();
  return serialized ? `?${serialized}` : '';
}

export const testingManagementApi = {
  async getCatalogAggregate(workspaceId: string, filters: CatalogFilter): Promise<CatalogAggregate> {
    if (TM_USE_MOCK) {
      return withMockLatency(() => readCatalogAggregate(filters));
    }
    return fetchJson<CatalogAggregate>(
      `/testing/catalog${buildSearchParams({
        workspaceId,
        projectId: filters.projectId === 'ALL' ? null : filters.projectId,
        planState: filters.planState === 'ALL' ? null : filters.planState,
        coverageLed: filters.coverageStatus === 'ALL' ? null : filters.coverageStatus,
        search: filters.search || null,
      })}`,
    );
  },

  async getCatalogSection(cardKey: CatalogCardKey, workspaceId: string, filters: CatalogFilter) {
    if (TM_USE_MOCK) {
      return withMockLatency(() => readCatalogSection(cardKey, filters));
    }
    return fetchJson(
      `/testing/catalog/${cardKey}${buildSearchParams({
        workspaceId,
        projectId: filters.projectId === 'ALL' ? null : filters.projectId,
        planState: filters.planState === 'ALL' ? null : filters.planState,
        coverageLed: filters.coverageStatus === 'ALL' ? null : filters.coverageStatus,
        search: filters.search || null,
      })}`,
    );
  },

  async getPlanAggregate(planId: string): Promise<PlanDetailAggregate> {
    if (TM_USE_MOCK) {
      return withMockLatency(() => readPlanAggregate(planId));
    }
    return fetchJson<PlanDetailAggregate>(`/testing/plans/${encodeURIComponent(planId)}`);
  },

  async getPlanSection(cardKey: PlanCardKey, planId: string) {
    if (TM_USE_MOCK) {
      return withMockLatency(() => readPlanSection(cardKey, planId));
    }
    const path = cardKey === 'recentRuns' ? 'recent-runs' : cardKey === 'draftInbox' ? 'draft-inbox' : cardKey === 'aiInsights' ? 'ai-insights' : cardKey;
    return fetchJson(`/testing/plans/${encodeURIComponent(planId)}/${path}`);
  },

  async getCaseAggregate(caseId: string): Promise<CaseDetailAggregate> {
    if (TM_USE_MOCK) {
      return withMockLatency(() => readCaseAggregate(caseId));
    }
    return fetchJson<CaseDetailAggregate>(`/testing/cases/${encodeURIComponent(caseId)}`);
  },

  async getCaseSection(cardKey: CaseCardKey, caseId: string) {
    if (TM_USE_MOCK) {
      return withMockLatency(() => readCaseSection(cardKey, caseId));
    }
    const aggregate = await fetchJson<CaseDetailAggregate>(`/testing/cases/${encodeURIComponent(caseId)}`);
    return aggregate[cardKey];
  },

  async getRunAggregate(runId: string): Promise<RunDetailAggregate> {
    if (TM_USE_MOCK) {
      return withMockLatency(() => readRunAggregate(runId));
    }
    return fetchJson<RunDetailAggregate>(`/testing/runs/${encodeURIComponent(runId)}`);
  },

  async getRunSection(cardKey: RunCardKey, runId: string) {
    if (TM_USE_MOCK) {
      return withMockLatency(() => readRunSection(cardKey, runId));
    }
    const path = cardKey === 'caseResults' ? 'case-results' : cardKey;
    return fetchJson(`/testing/runs/${encodeURIComponent(runId)}/${path}`);
  },

  async getTraceabilityAggregate(workspaceId: string): Promise<TraceabilityAggregate> {
    if (TM_USE_MOCK) {
      return withMockLatency(() => readTraceabilityAggregate());
    }
    return fetchJson<TraceabilityAggregate>(`/testing/traceability${buildSearchParams({ workspaceId })}`);
  },

  async getTraceabilitySection(cardKey: TraceabilityCardKey, workspaceId: string) {
    if (TM_USE_MOCK) {
      return withMockLatency(() => readTraceabilitySection(cardKey));
    }
    if (cardKey === 'summary') {
      const aggregate = await fetchJson<TraceabilityAggregate>(`/testing/traceability${buildSearchParams({ workspaceId })}`);
      return aggregate.summary;
    }
    return fetchJson(`/testing/traceability/req-rows${buildSearchParams({ workspaceId })}`);
  },
};
