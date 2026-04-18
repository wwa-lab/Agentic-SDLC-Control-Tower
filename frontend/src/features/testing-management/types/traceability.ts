import type { SectionResult, PlanCaseRef } from './common';
import type { CoverageStatus } from './enums';

export type TraceabilityCoverageStatus = CoverageStatus;

export interface TraceabilityReqRow {
  readonly reqId: string;
  readonly reqTitle: string;
  readonly storyId: string | null;
  readonly projectId: string;
  readonly projectName: string;
  readonly linkedCaseCount: number;
  readonly linkedPlanCount: number;
  readonly coverageStatus: TraceabilityCoverageStatus;
  readonly latestRunAt: string | null;
  readonly cases: ReadonlyArray<PlanCaseRef>;
}

export interface TraceabilitySummary {
  readonly workspaceId: string;
  readonly totalRequirements: number;
  readonly buckets: Record<CoverageStatus, number>;
}

export interface TraceabilityAggregate {
  readonly summary: SectionResult<TraceabilitySummary>;
  readonly reqRows: SectionResult<ReadonlyArray<TraceabilityReqRow>>;
}
