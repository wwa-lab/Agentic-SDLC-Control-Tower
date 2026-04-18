import type { Lineage } from '@/shared/types/lineage';
import type {
  AiRowStatus,
  RunStatus,
  RunTrigger,
  StepConclusion,
  StoryLinkStatus,
} from './enums';

export interface LogLine {
  readonly lineNumber: number;
  readonly text: string;
  readonly redacted: boolean;
}

export interface LogExcerptBlock {
  readonly stepId: string;
  readonly byteCount: number;
  readonly githubUrl: string;
  readonly lines: ReadonlyArray<LogLine>;
  readonly truncated: boolean;
}

export interface RunStepRow {
  readonly stepId: string;
  readonly name: string;
  readonly conclusion: StepConclusion;
  readonly startedAt: string | null;
  readonly completedAt: string | null;
  readonly durationSeconds: number | null;
  readonly githubUrl: string;
  readonly logExcerpt: LogExcerptBlock | null;
}

export interface RunJobRow {
  readonly jobId: string;
  readonly jobName: string;
  readonly status: RunStatus;
  readonly startedAt: string | null;
  readonly completedAt: string | null;
  readonly durationSeconds: number | null;
  readonly githubUrl: string;
  readonly steps: ReadonlyArray<RunStepRow>;
}

export interface AiEvidenceTrail {
  readonly runId: string;
  readonly jobId: string;
  readonly stepId: string;
}

export interface AiTriageRow {
  readonly id: string;
  readonly status: AiRowStatus;
  readonly title: string;
  readonly summary: string;
  readonly probableCause: string;
  readonly evidence: AiEvidenceTrail;
  readonly confidence: number;
  readonly retryable: boolean;
  readonly errorCode: string | null;
  readonly generatedAt: string | null;
}

export interface AiTriagePayload {
  readonly status: 'SUCCESS' | 'PENDING' | 'FAILED';
  readonly summary: string;
  readonly rows: ReadonlyArray<AiTriageRow>;
  readonly generatedAt: string | null;
  readonly skillVersion: string | null;
  readonly lineage: Lineage | null;
}

export interface RunHeader {
  readonly runId: string;
  readonly runNumber: number;
  readonly workflowName: string;
  readonly status: RunStatus;
  readonly trigger: RunTrigger;
  readonly actor: string;
  readonly branch: string;
  readonly sha: string;
  readonly createdAt: string;
  readonly updatedAt: string;
  readonly durationSeconds: number | null;
  readonly storyLinkStatus: StoryLinkStatus;
  readonly storyId: string | null;
  readonly repoId: string;
  readonly repoLabel: string;
  readonly workspaceId: string;
  readonly projectId: string;
  readonly githubUrl: string;
}

export interface RunRerunState {
  readonly canRerun: boolean;
  readonly lastAttemptAt: string | null;
  readonly lastResultCode: string | null;
  readonly lastResultMessage: string | null;
  readonly rateLimitResetAt: string | null;
  readonly requestedRunId: string | null;
}

export interface PipelineRunDetail {
  readonly header: RunHeader;
  readonly jobs: ReadonlyArray<RunJobRow>;
  readonly steps: ReadonlyArray<RunJobRow>;
  readonly logs: LogExcerptBlock | null;
  readonly aiTriage: AiTriagePayload;
  readonly rerun: RunRerunState;
}

