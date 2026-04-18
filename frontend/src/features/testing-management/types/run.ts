import type { SectionResult, MemberRef, RequirementChip } from './common';
import type { RunTriggerSource, TestEnvironmentKind, TestResultOutcome, TestRunState } from './enums';

export interface RunHeader {
  readonly runId: string;
  readonly planId: string;
  readonly projectId: string;
  readonly planName: string;
  readonly environmentId: string;
  readonly environmentName: string;
  readonly environmentKind: TestEnvironmentKind;
  readonly triggerSource: RunTriggerSource;
  readonly actor: MemberRef;
  readonly state: TestRunState;
  readonly externalRunId: string | null;
  readonly durationSec: number | null;
  readonly startedAt: string;
  readonly completedAt: string | null;
}

export interface RunCaseResultRow {
  readonly resultId: string;
  readonly caseId: string;
  readonly title: string;
  readonly outcome: TestResultOutcome;
  readonly durationSec: number | null;
  readonly failureExcerpt: string | null;
  readonly lastPassedAt: string | null;
  readonly createdAt: string;
}

export interface RunCoverageAggregate {
  readonly coveredRequirementCount: number;
  readonly coveredRequirements: ReadonlyArray<RequirementChip>;
}

export interface RunDetailAggregate {
  readonly header: SectionResult<RunHeader>;
  readonly caseResults: SectionResult<ReadonlyArray<RunCaseResultRow>>;
  readonly coverage: SectionResult<RunCoverageAggregate>;
}
