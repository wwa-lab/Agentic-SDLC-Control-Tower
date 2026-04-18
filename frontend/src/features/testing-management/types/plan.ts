import type { SectionResult, MemberRef, RequirementChip } from './common';
import type { CoverageStatus, DraftOrigin, RunTriggerSource, TestCasePriority, TestCaseState, TestCaseType, TestPlanState, TestResultOutcome, TestRunState } from './enums';

export interface PlanHeader {
  readonly planId: string;
  readonly projectId: string;
  readonly projectName: string;
  readonly workspaceId: string;
  readonly workspaceName: string;
  readonly name: string;
  readonly description: string;
  readonly releaseTarget: string;
  readonly owner: MemberRef;
  readonly state: TestPlanState;
  readonly createdAt: string;
  readonly updatedAt: string;
}

export interface PlanCaseRow {
  readonly caseId: string;
  readonly planId: string;
  readonly title: string;
  readonly type: TestCaseType;
  readonly priority: TestCasePriority;
  readonly state: TestCaseState;
  readonly linkedReqs: ReadonlyArray<RequirementChip>;
  readonly lastRunStatus: TestResultOutcome | null;
  readonly lastRunAt: string | null;
}

export interface PlanCoverageRow {
  readonly reqId: string;
  readonly reqTitle: string;
  readonly linkedCaseCount: number;
  readonly aggregateStatus: CoverageStatus;
  readonly mostRecentAt: string | null;
}

export interface PlanRunRow {
  readonly runId: string;
  readonly environmentId: string;
  readonly environmentName: string;
  readonly triggerSource: RunTriggerSource;
  readonly state: TestRunState;
  readonly durationSec: number | null;
  readonly passCount: number;
  readonly failCount: number;
  readonly skipCount: number;
  readonly actor: MemberRef;
  readonly startedAt: string;
  readonly completedAt: string | null;
}

export interface AiDraftRow {
  readonly caseId: string;
  readonly title: string;
  readonly sourceReqId: string;
  readonly skillVersion: string;
  readonly draftedAt: string;
  readonly draft: boolean;
}

export interface AiInsights {
  readonly autonomyLevel: string;
  readonly pendingDrafts: number;
  readonly activeCases: number;
  readonly passRateLast7d: number;
  readonly narrative: string;
}

export interface PlanDetailAggregate {
  readonly header: SectionResult<PlanHeader>;
  readonly cases: SectionResult<ReadonlyArray<PlanCaseRow>>;
  readonly coverage: SectionResult<ReadonlyArray<PlanCoverageRow>>;
  readonly recentRuns: SectionResult<ReadonlyArray<PlanRunRow>>;
  readonly draftInbox: SectionResult<ReadonlyArray<AiDraftRow>>;
  readonly aiInsights: SectionResult<AiInsights>;
}
