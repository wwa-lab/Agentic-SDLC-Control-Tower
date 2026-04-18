import type { Lineage } from '@/shared/types/lineage';
import type { AiNoteSeverity, PrState, RunStatus, StoryLinkStatus } from './enums';

export type HumanReviewState = 'APPROVED' | 'CHANGES_REQUESTED' | 'COMMENTED' | 'DISMISSED';
export type AiReviewStatus = 'SUCCESS' | 'PENDING' | 'FAILED' | 'STALE';

export interface AiPrReviewNoteCounts {
  readonly blocker: number;
  readonly major: number;
  readonly minor: number;
  readonly info: number;
}

export interface AiPrReviewNote {
  readonly id: string;
  readonly severity: AiNoteSeverity;
  readonly title: string;
  readonly body: string;
  readonly filePath: string | null;
  readonly line: number | null;
  readonly evidence: string;
  readonly bodyRestricted?: boolean;
}

export interface AiPrReviewPayload {
  readonly status: AiReviewStatus;
  readonly summary: string;
  readonly headSha: string;
  readonly noteCounts: AiPrReviewNoteCounts;
  readonly notes: ReadonlyArray<AiPrReviewNote>;
  readonly generatedAt: string | null;
  readonly skillVersion: string | null;
  readonly lineage: Lineage | null;
  readonly failureCode: string | null;
  readonly stale: boolean;
}

export interface PrHeader {
  readonly prId: string;
  readonly number: number;
  readonly title: string;
  readonly state: PrState;
  readonly author: string;
  readonly baseBranch: string;
  readonly headBranch: string;
  readonly headSha: string;
  readonly createdAt: string;
  readonly updatedAt: string;
  readonly githubUrl: string;
  readonly storyLinkStatus: StoryLinkStatus;
  readonly storyId: string | null;
  readonly repoId: string;
  readonly repoLabel: string;
  readonly workspaceId: string;
  readonly projectId: string;
}

export interface PrCheckRow {
  readonly id: string;
  readonly checkName: string;
  readonly status: RunStatus;
  readonly conclusion: string;
  readonly durationSeconds: number | null;
  readonly startedAt: string;
  readonly completedAt: string | null;
  readonly runId: string;
  readonly githubUrl: string;
}

export interface PrReviewRow {
  readonly id: string;
  readonly reviewer: string;
  readonly state: HumanReviewState;
  readonly summary: string;
  readonly body: string | null;
  readonly submittedAt: string;
}

export interface PrCommitRow {
  readonly commitId: string;
  readonly sha: string;
  readonly message: string;
  readonly author: string;
  readonly committedAt: string;
  readonly storyLinkStatus: StoryLinkStatus;
  readonly githubUrl: string;
}

export interface PullRequestDetail {
  readonly header: PrHeader;
  readonly checks: ReadonlyArray<PrCheckRow>;
  readonly reviews: ReadonlyArray<PrReviewRow>;
  readonly commits: ReadonlyArray<PrCommitRow>;
  readonly aiReview: AiPrReviewPayload;
}

