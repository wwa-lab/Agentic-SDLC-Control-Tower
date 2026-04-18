import type { SectionResult, MemberRef } from './common';
import type { CoverageStatus, TestPlanState } from './enums';

export interface CatalogFilters {
  readonly projectIds: ReadonlyArray<string>;
  readonly planStates: ReadonlyArray<TestPlanState>;
  readonly coverageStatuses: ReadonlyArray<CoverageStatus>;
}

export interface CatalogSummary {
  readonly workspaceId: string;
  readonly totalPlans: number;
  readonly totalActiveCases: number;
  readonly runsLast7d: number;
  readonly passRateLast7d: number;
  readonly meanRunDurationSec: number;
  readonly byCoverageLed: Record<CoverageStatus, number>;
}

export interface CatalogPlanRow {
  readonly planId: string;
  readonly projectId: string;
  readonly projectName: string;
  readonly workspaceId: string;
  readonly name: string;
  readonly releaseTarget: string;
  readonly owner: MemberRef;
  readonly state: TestPlanState;
  readonly linkedCaseCount: number;
  readonly coverageLed: CoverageStatus;
  readonly description: string;
  readonly createdAt: string;
  readonly updatedAt: string;
}

export interface CatalogFilter {
  readonly projectId: string;
  readonly planState: TestPlanState | 'ALL';
  readonly coverageStatus: CoverageStatus | 'ALL';
  readonly search: string;
}

export interface CatalogAggregate {
  readonly summary: SectionResult<CatalogSummary>;
  readonly grid: SectionResult<ReadonlyArray<CatalogPlanRow>>;
  readonly filters: SectionResult<CatalogFilters>;
}
