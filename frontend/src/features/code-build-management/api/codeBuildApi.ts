import { fetchJson, postJson } from '@/shared/api/client';
import type {
  CatalogAggregate,
  CatalogCardKey,
  CatalogFilter,
  CodeBuildViewerContext,
  PrCardKey,
  PrDetailAggregate,
  RegenerateAiPrReviewRequest,
  RegenerateAiPrReviewResponse,
  RegenerateAiTriageRequest,
  RegenerateAiTriageResponse,
  RepoCardKey,
  RepoDetailAggregate,
  RerunRunRequest,
  RerunRunResponse,
  RunCardKey,
  RunDetailAggregate,
  TraceabilityAggregate,
  TraceabilityCardKey,
  TraceabilityFilter,
} from '../types';
import {
  readCatalogAggregate,
  readCatalogSection,
  readPrAggregate,
  readPrSection,
  readRepoAggregate,
  readRepoSection,
  readRunAggregate,
  readRunSection,
  readTraceabilityAggregate,
  readTraceabilitySection,
  regenerateAiPrReview as regenerateAiPrReviewMock,
  regenerateAiTriage as regenerateAiTriageMock,
  rerunRun as rerunRunMock,
  resetMockState,
  simulateWebhookRoundTrip,
} from '../mock/commandLoop';

const USE_MOCK = import.meta.env.DEV && !import.meta.env.VITE_USE_BACKEND;
const MOCK_LATENCY = import.meta.env.MODE === 'test' ? 0 : 90;

function wait(ms: number) {
  return new Promise(resolve => {
    window.setTimeout(resolve, ms);
  });
}

function buildSearchParams(entries: Record<string, string | number | null | undefined>) {
  const params = new URLSearchParams();
  Object.entries(entries).forEach(([key, value]) => {
    if (value == null || value === '') {
      return;
    }
    params.set(key, String(value));
  });
  const serialized = params.toString();
  return serialized ? `?${serialized}` : '';
}

async function withMockLatency<T>(work: () => Promise<T>) {
  if (MOCK_LATENCY > 0) {
    await wait(MOCK_LATENCY);
  }
  return work();
}

export const codeBuildApi = {
  async getCatalogAggregate(context: CodeBuildViewerContext, filters: CatalogFilter): Promise<CatalogAggregate> {
    if (USE_MOCK) {
      return withMockLatency(() => readCatalogAggregate(context, filters));
    }
    return fetchJson<CatalogAggregate>(
      `/code-build-management/catalog${buildSearchParams({
        workspaceId: context.workspaceId,
        search: filters.search,
        buildStatus: filters.buildStatus,
        visibility: filters.visibility,
        projectId: filters.projectId,
        sort: filters.sort,
      })}`,
    );
  },

  async getCatalogSection(cardKey: CatalogCardKey, context: CodeBuildViewerContext, filters: CatalogFilter) {
    if (USE_MOCK) {
      return withMockLatency(() => readCatalogSection(cardKey, context, filters));
    }
    return fetchJson(`/code-build-management/catalog/${cardKey}${buildSearchParams({
      workspaceId: context.workspaceId,
      search: filters.search,
      buildStatus: filters.buildStatus,
      visibility: filters.visibility,
      projectId: filters.projectId,
      sort: filters.sort,
    })}`);
  },

  async getRepoAggregate(repoId: string, context: CodeBuildViewerContext): Promise<RepoDetailAggregate> {
    if (USE_MOCK) {
      return withMockLatency(() => readRepoAggregate(repoId, context));
    }
    return fetchJson<RepoDetailAggregate>(`/code-build-management/repos/${encodeURIComponent(repoId)}`);
  },

  async getRepoSection(cardKey: RepoCardKey, repoId: string, context: CodeBuildViewerContext) {
    if (USE_MOCK) {
      return withMockLatency(() => readRepoSection(cardKey, repoId, context));
    }
    return fetchJson(`/code-build-management/repos/${encodeURIComponent(repoId)}/${cardKey}`);
  },

  async getPrAggregate(prId: string, context: CodeBuildViewerContext): Promise<PrDetailAggregate> {
    if (USE_MOCK) {
      return withMockLatency(() => readPrAggregate(prId, context));
    }
    return fetchJson<PrDetailAggregate>(`/code-build-management/prs/${encodeURIComponent(prId)}`);
  },

  async getPrSection(cardKey: PrCardKey, prId: string, context: CodeBuildViewerContext) {
    if (USE_MOCK) {
      return withMockLatency(() => readPrSection(cardKey, prId, context));
    }
    return fetchJson(`/code-build-management/prs/${encodeURIComponent(prId)}/${cardKey}`);
  },

  async getRunAggregate(runId: string, context: CodeBuildViewerContext): Promise<RunDetailAggregate> {
    if (USE_MOCK) {
      return withMockLatency(() => readRunAggregate(runId, context));
    }
    return fetchJson<RunDetailAggregate>(`/code-build-management/runs/${encodeURIComponent(runId)}`);
  },

  async getRunSection(cardKey: RunCardKey, runId: string, context: CodeBuildViewerContext) {
    if (USE_MOCK) {
      return withMockLatency(() => readRunSection(cardKey, runId, context));
    }
    return fetchJson(`/code-build-management/runs/${encodeURIComponent(runId)}/${cardKey}`);
  },

  async getTraceabilityAggregate(
    context: CodeBuildViewerContext,
    filters: TraceabilityFilter,
  ): Promise<TraceabilityAggregate> {
    if (USE_MOCK) {
      return withMockLatency(() => readTraceabilityAggregate(context, filters));
    }
    return fetchJson<TraceabilityAggregate>(
      `/code-build-management/traceability${buildSearchParams({
        workspaceId: context.workspaceId,
        projectId: filters.projectId,
        storyState: filters.storyState,
        linkStatus: filters.linkStatus,
        rangeDays: filters.rangeDays,
      })}`,
    );
  },

  async getTraceabilitySection(cardKey: TraceabilityCardKey, context: CodeBuildViewerContext, filters: TraceabilityFilter) {
    if (USE_MOCK) {
      return withMockLatency(() => readTraceabilitySection(cardKey, context, filters));
    }
    return fetchJson(
      `/code-build-management/traceability/${cardKey}${buildSearchParams({
        workspaceId: context.workspaceId,
        projectId: filters.projectId,
        storyState: filters.storyState,
        linkStatus: filters.linkStatus,
        rangeDays: filters.rangeDays,
      })}`,
    );
  },

  async regenerateAiPrReview(
    prId: string,
    request: RegenerateAiPrReviewRequest,
    context: CodeBuildViewerContext,
  ): Promise<RegenerateAiPrReviewResponse> {
    if (USE_MOCK) {
      return withMockLatency(() => regenerateAiPrReviewMock(prId, request, context));
    }
    return postJson<RegenerateAiPrReviewResponse>(
      `/code-build-management/prs/${encodeURIComponent(prId)}/ai-review/regenerate`,
      request,
    );
  },

  async regenerateAiTriage(
    runId: string,
    request: RegenerateAiTriageRequest,
    context: CodeBuildViewerContext,
  ): Promise<RegenerateAiTriageResponse> {
    if (USE_MOCK) {
      return withMockLatency(() => regenerateAiTriageMock(runId, request, context));
    }
    return postJson<RegenerateAiTriageResponse>(
      `/code-build-management/runs/${encodeURIComponent(runId)}/ai-triage/regenerate`,
      request,
    );
  },

  async rerunRun(
    runId: string,
    request: RerunRunRequest,
    context: CodeBuildViewerContext,
  ): Promise<RerunRunResponse> {
    if (USE_MOCK) {
      return withMockLatency(() => rerunRunMock(runId, request, context));
    }
    return postJson<RerunRunResponse>(
      `/code-build-management/runs/${encodeURIComponent(runId)}/rerun`,
      request,
    );
  },
};

export const codeBuildMockApi = {
  resetMockState,
  simulateWebhookRoundTrip,
};

