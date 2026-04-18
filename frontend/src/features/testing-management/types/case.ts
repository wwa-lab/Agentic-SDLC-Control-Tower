import type { SectionResult, IncidentChip, MemberRef, RequirementChip } from './common';
import type { DraftOrigin, TestCasePriority, TestCaseState, TestCaseType, TestResultOutcome } from './enums';

export interface CaseDetail {
  readonly caseId: string;
  readonly planId: string;
  readonly projectId: string;
  readonly planName: string;
  readonly title: string;
  readonly type: TestCaseType;
  readonly priority: TestCasePriority;
  readonly state: TestCaseState;
  readonly origin: DraftOrigin;
  readonly owner: MemberRef;
  readonly preconditions: string;
  readonly steps: string;
  readonly expectedResult: string;
  readonly linkedReqs: ReadonlyArray<RequirementChip>;
  readonly linkedIncidents: ReadonlyArray<IncidentChip>;
  readonly createdAt: string;
  readonly updatedAt: string;
}

export interface CaseRunOutcome {
  readonly resultId: string;
  readonly runId: string;
  readonly outcome: TestResultOutcome;
  readonly failureExcerpt: string | null;
  readonly lastPassedAt: string | null;
  readonly environmentName: string;
  readonly createdAt: string;
}

export interface CaseRevision {
  readonly revisionId: string;
  readonly actor: MemberRef;
  readonly timestamp: string;
  readonly fieldDiff: Record<string, string>;
}

export type CaseSparkline = ReadonlyArray<CaseRunOutcome>;

export interface CaseDetailAggregate {
  readonly detail: SectionResult<CaseDetail>;
  readonly recentResults: SectionResult<ReadonlyArray<CaseRunOutcome>>;
  readonly revisions: SectionResult<ReadonlyArray<CaseRevision>>;
}
