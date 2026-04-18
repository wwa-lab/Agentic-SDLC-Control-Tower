import type { Lineage } from '@/shared/types/lineage';
import type { AiPrReviewNoteCounts } from './pr';
import type { PrState, RepoVisibility, RunStatus, RunTrigger, StoryLinkStatus } from './enums';

export interface BuildHealthPoint {
  readonly runId: string;
  readonly status: RunStatus;
  readonly label: string;
}

export interface RepoHeader {
  readonly repoId: string;
  readonly owner: string;
  readonly name: string;
  readonly visibility: RepoVisibility;
  readonly defaultBranch: string;
  readonly workspaceId: string;
  readonly workspaceName: string;
  readonly projectId: string;
  readonly projectName: string;
  readonly lastSyncedAt: string;
  readonly defaultBranchProtection: string;
  readonly lastActivityAt: string | null;
  readonly githubUrl: string;
}

export interface BranchRow {
  readonly branch: string;
  readonly isDefault: boolean;
  readonly protected: boolean;
  readonly headSha: string;
  readonly lastRunStatus: RunStatus;
  readonly updatedAt: string;
}

export interface RecentCommitRow {
  readonly commitId: string;
  readonly sha: string;
  readonly message: string;
  readonly author: string;
  readonly committedAt: string;
  readonly storyLinkStatus: StoryLinkStatus;
  readonly storyId: string | null;
  readonly githubUrl: string;
}

export interface RecentPrRow {
  readonly prId: string;
  readonly number: number;
  readonly title: string;
  readonly state: PrState;
  readonly author: string;
  readonly baseBranch: string;
  readonly headBranch: string;
  readonly updatedAt: string;
  readonly aiCounts: AiPrReviewNoteCounts;
  readonly githubUrl: string;
}

export interface RecentRunRow {
  readonly runId: string;
  readonly runNumber: number;
  readonly workflowName: string;
  readonly status: RunStatus;
  readonly branch: string;
  readonly sha: string;
  readonly trigger: RunTrigger;
  readonly durationSeconds: number | null;
  readonly actor: string;
  readonly createdAt: string;
  readonly updatedAt: string;
  readonly githubUrl: string;
}

export interface RepoHealthSummary {
  readonly successRate: number;
  readonly medianDurationSeconds: number;
  readonly failingWorkflows: ReadonlyArray<{
    workflowName: string;
    failures: number;
  }>;
  readonly last14Runs: ReadonlyArray<BuildHealthPoint>;
  readonly advisory: string;
}

export interface RepoAiSummary {
  readonly summary: string;
  readonly deltaSummary: string;
  readonly generatedAt: string;
  readonly skillVersion: string;
  readonly lineage: Lineage;
}

export interface RepoDetail {
  readonly header: RepoHeader;
  readonly recentRuns: ReadonlyArray<RecentRunRow>;
  readonly recentPrs: ReadonlyArray<RecentPrRow>;
  readonly recentCommits: ReadonlyArray<RecentCommitRow>;
  readonly branches: ReadonlyArray<BranchRow>;
  readonly healthSummary: RepoHealthSummary;
  readonly aiSummary: RepoAiSummary;
}

