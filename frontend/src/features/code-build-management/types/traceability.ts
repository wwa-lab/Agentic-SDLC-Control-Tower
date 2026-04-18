import type { RunStatus, StoryLinkStatus, StoryState } from './enums';

export interface TraceabilityBuildRef {
  readonly runId: string;
  readonly runNumber: number;
  readonly status: RunStatus;
  readonly workflowName: string;
  readonly finishedAt: string;
}

export interface TraceabilityCommitRef {
  readonly commitId: string;
  readonly sha: string;
  readonly message: string;
  readonly author: string;
  readonly repoId: string;
  readonly repoLabel: string;
  readonly committedAt: string;
  readonly storyLinkStatus: StoryLinkStatus;
  readonly githubUrl: string;
  readonly builds: ReadonlyArray<TraceabilityBuildRef>;
}

export interface TraceabilityStoryRow {
  readonly storyId: string;
  readonly title: string;
  readonly projectId: string;
  readonly projectName: string;
  readonly state: StoryState;
  readonly commitCount: number;
  readonly buildCount: number;
  readonly worstStatus: StoryLinkStatus;
  readonly requirementUrl: string;
  readonly commits: ReadonlyArray<TraceabilityCommitRef>;
}

export interface TraceabilityUnknownStoryRow {
  readonly storyId: string;
  readonly repoId: string;
  readonly repoLabel: string;
  readonly commitId: string;
  readonly sha: string;
  readonly message: string;
  readonly ageDays: number;
  readonly requirementSearchUrl: string;
  readonly committedAt: string;
}

export interface TraceabilityNoStoryIdRow {
  readonly repoId: string;
  readonly repoLabel: string;
  readonly commitId: string;
  readonly sha: string;
  readonly message: string;
  readonly branch: string;
  readonly author: string;
  readonly committedAt: string;
  readonly githubUrl: string;
}

export interface TraceabilitySummary {
  readonly known: number;
  readonly unknownStory: number;
  readonly noStoryId: number;
  readonly ambiguous: number;
  readonly advisory: string;
}

export interface TraceabilityFilter {
  readonly projectId: string | 'ALL';
  readonly storyState: StoryState | 'ALL';
  readonly linkStatus: StoryLinkStatus | 'ALL';
  readonly rangeDays: number;
}

export interface TraceabilityStatus {
  readonly status: StoryLinkStatus;
  readonly label: string;
  readonly description: string;
}

