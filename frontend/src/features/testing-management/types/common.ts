import type { SectionResult } from '@/shared/types/section';
import type { CoverageStatus, DraftOrigin, ReqLinkStatus, TestCasePriority, TestCaseState, TestCaseType, TestResultOutcome } from './enums';

export type { SectionResult };

export interface MemberRef {
  readonly memberId: string;
  readonly displayName: string;
}

export interface RequirementChip {
  readonly reqId: string;
  readonly storyId: string | null;
  readonly title: string;
  readonly projectId: string | null;
  readonly linkStatus: ReqLinkStatus;
  readonly chipColor: CoverageStatus;
  readonly routePath: string;
}

export interface IncidentChip {
  readonly incidentId: string;
  readonly title: string;
  readonly severity: string;
  readonly routePath: string;
}

export interface PlanCaseRef {
  readonly caseId: string;
  readonly title: string;
  readonly planId: string;
  readonly planName: string;
  readonly lastRunStatus: TestResultOutcome | null;
  readonly lastRunAt: string | null;
}

export interface CaseCardDescriptor {
  readonly caseId: string;
  readonly title: string;
  readonly type: TestCaseType;
  readonly priority: TestCasePriority;
  readonly state: TestCaseState;
}

export interface SectionFallbacks {
  readonly data: null;
  readonly error: string | null;
}
