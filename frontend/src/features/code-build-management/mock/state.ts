import type { PullRequestDetail, PipelineRunDetail, RepoDetail, CatalogSection, TraceabilityNoStoryIdRow, TraceabilityStoryRow, TraceabilityUnknownStoryRow } from '../types';
import { aiPrReviewByPrId } from './aiPrReview.mock';
import { aiTriageByRunId } from './aiTriage.mock';
import { catalogSectionsMock } from './catalog.mock';
import { prDetailsById } from './prDetail.mock';
import { repoDetailsById } from './repoDetail.mock';
import { runDetailsById } from './runDetail.mock';
import { traceabilityNoStoryIdRowsMock, traceabilityStoryRowsMock, traceabilityUnknownStoryRowsMock } from './traceability.mock';

export function cloneValue<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}

function createMockState() {
  const prDetails = cloneValue<Record<string, PullRequestDetail>>(prDetailsById) as any;
  for (const [prId, payload] of Object.entries(aiPrReviewByPrId)) {
    prDetails[prId].aiReview = cloneValue(payload);
  }

  const runDetails = cloneValue<Record<string, PipelineRunDetail>>(runDetailsById) as any;
  for (const [runId, payload] of Object.entries(aiTriageByRunId)) {
    runDetails[runId].aiTriage = cloneValue(payload);
  }

  return {
    catalogSections: cloneValue<ReadonlyArray<CatalogSection>>(catalogSectionsMock),
    repoDetails: cloneValue<Record<string, RepoDetail>>(repoDetailsById),
    prDetails,
    runDetails,
    traceabilityStoryRows: cloneValue<ReadonlyArray<TraceabilityStoryRow>>(traceabilityStoryRowsMock),
    traceabilityUnknownStoryRows: cloneValue<ReadonlyArray<TraceabilityUnknownStoryRow>>(traceabilityUnknownStoryRowsMock),
    traceabilityNoStoryIdRows: cloneValue<ReadonlyArray<TraceabilityNoStoryIdRow>>(traceabilityNoStoryIdRowsMock),
    counters: {
      prReview: 10,
      triage: 20,
      rerun: 1200,
      clock: 0,
    },
  };
}

let mockState = createMockState() as any;

export function getMockState() {
  return mockState;
}

export function resetMockState() {
  mockState = createMockState() as any;
}
