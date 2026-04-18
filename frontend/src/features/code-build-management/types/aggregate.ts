import type { SectionResult } from '@/shared/types/section';
import type { CatalogFilter, CatalogFilterOptions, CatalogSection, CatalogSummary } from './catalog';
import type { AiAutonomyLevel, ProjectRole, StoryState, StoryLinkStatus } from './enums';
import type { PullRequestDetail } from './pr';
import type { RepoAiSummary, RepoHealthSummary, RepoHeader, BranchRow, RecentCommitRow, RecentPrRow, RecentRunRow } from './repo';
import type { AiTriagePayload, LogExcerptBlock, RunHeader, RunJobRow, RunRerunState } from './run';
import type {
  TraceabilityFilter,
  TraceabilityNoStoryIdRow,
  TraceabilityStoryRow,
  TraceabilitySummary,
  TraceabilityUnknownStoryRow,
} from './traceability';
import type { AiPrReviewPayload, PrCheckRow, PrCommitRow, PrHeader, PrReviewRow } from './pr';

export const CB_DEFAULT_WORKSPACE_ID = 'ws-default-001';
export const CB_DEFAULT_REPO_ID = 'repo-1';
export const CB_DEFAULT_PR_ID = 'pr-42';
export const CB_DEFAULT_RUN_ID = 'run-991';
export const CB_DEFAULT_ROLE: ProjectRole = 'DEVELOPER';
export const CB_DEFAULT_AUTONOMY: AiAutonomyLevel = 'SUPERVISED';

export interface CodeBuildViewerContext {
  readonly workspaceId: string;
  readonly role: ProjectRole;
  readonly autonomyLevel: AiAutonomyLevel;
}

export const CODE_BUILD_CATALOG_CARD_KEYS = ['summary', 'filters', 'grid'] as const;
export const CODE_BUILD_REPO_CARD_KEYS = [
  'header',
  'recentRuns',
  'recentPrs',
  'recentCommits',
  'branches',
  'healthSummary',
  'aiSummary',
] as const;
export const CODE_BUILD_PR_CARD_KEYS = ['header', 'checks', 'reviews', 'commits', 'aiReview'] as const;
export const CODE_BUILD_RUN_CARD_KEYS = ['header', 'jobs', 'steps', 'logs', 'aiTriage', 'rerun'] as const;
export const CODE_BUILD_TRACEABILITY_CARD_KEYS = ['summary', 'storyRows', 'unknownStory', 'noStoryId'] as const;

export type CatalogCardKey = (typeof CODE_BUILD_CATALOG_CARD_KEYS)[number];
export type RepoCardKey = (typeof CODE_BUILD_REPO_CARD_KEYS)[number];
export type PrCardKey = (typeof CODE_BUILD_PR_CARD_KEYS)[number];
export type RunCardKey = (typeof CODE_BUILD_RUN_CARD_KEYS)[number];
export type TraceabilityCardKey = (typeof CODE_BUILD_TRACEABILITY_CARD_KEYS)[number];

export interface CatalogAggregate {
  readonly summary: SectionResult<CatalogSummary>;
  readonly filters: SectionResult<CatalogFilterOptions>;
  readonly grid: SectionResult<ReadonlyArray<CatalogSection>>;
}

export interface RepoDetailAggregate {
  readonly header: SectionResult<RepoHeader>;
  readonly recentRuns: SectionResult<ReadonlyArray<RecentRunRow>>;
  readonly recentPrs: SectionResult<ReadonlyArray<RecentPrRow>>;
  readonly recentCommits: SectionResult<ReadonlyArray<RecentCommitRow>>;
  readonly branches: SectionResult<ReadonlyArray<BranchRow>>;
  readonly healthSummary: SectionResult<RepoHealthSummary>;
  readonly aiSummary: SectionResult<RepoAiSummary>;
}

export interface PrDetailAggregate {
  readonly header: SectionResult<PrHeader>;
  readonly checks: SectionResult<ReadonlyArray<PrCheckRow>>;
  readonly reviews: SectionResult<ReadonlyArray<PrReviewRow>>;
  readonly commits: SectionResult<ReadonlyArray<PrCommitRow>>;
  readonly aiReview: SectionResult<AiPrReviewPayload>;
}

export interface RunDetailAggregate {
  readonly header: SectionResult<RunHeader>;
  readonly jobs: SectionResult<ReadonlyArray<RunJobRow>>;
  readonly steps: SectionResult<ReadonlyArray<RunJobRow>>;
  readonly logs: SectionResult<LogExcerptBlock>;
  readonly aiTriage: SectionResult<AiTriagePayload>;
  readonly rerun: SectionResult<RunRerunState>;
}

export interface TraceabilityAggregate {
  readonly summary: SectionResult<TraceabilitySummary>;
  readonly storyRows: SectionResult<ReadonlyArray<TraceabilityStoryRow>>;
  readonly unknownStory: SectionResult<ReadonlyArray<TraceabilityUnknownStoryRow>>;
  readonly noStoryId: SectionResult<ReadonlyArray<TraceabilityNoStoryIdRow>>;
}

export interface CodeBuildManagementState {
  readonly viewerContext: CodeBuildViewerContext;
  readonly catalogFilters: CatalogFilter;
  readonly traceabilityFilters: TraceabilityFilter;
  readonly catalog: CatalogAggregate | null;
  readonly repo: RepoDetailAggregate | null;
  readonly pr: PrDetailAggregate | null;
  readonly run: RunDetailAggregate | null;
  readonly traceability: TraceabilityAggregate | null;
}

export const CODE_BUILD_WORKSPACE_OPTIONS = [
  { id: 'ws-default-001', label: 'Global SDLC Tower' },
  { id: 'ws-edge-002', label: 'Edge Release Lab' },
] as const;

export const CODE_BUILD_ROLE_OPTIONS: ReadonlyArray<ProjectRole> = ['PM', 'TECH_LEAD', 'DEVELOPER', 'VIEWER'];
export const CODE_BUILD_AUTONOMY_OPTIONS: ReadonlyArray<AiAutonomyLevel> = [
  'DISABLED',
  'OBSERVATION',
  'SUPERVISED',
  'AUTONOMOUS',
] as const;

export const CB_DEFAULT_CATALOG_FILTER: CatalogFilter = {
  search: '',
  buildStatus: 'ALL',
  visibility: 'ALL',
  projectId: 'ALL',
  sort: 'RECENT_ACTIVITY',
};

export const CB_DEFAULT_TRACEABILITY_FILTER: TraceabilityFilter = {
  projectId: 'ALL',
  storyState: 'ALL',
  linkStatus: 'ALL',
  rangeDays: 30,
};

export const TRACEABILITY_STORY_STATE_OPTIONS: ReadonlyArray<StoryState | 'ALL'> = [
  'ALL',
  'DISCOVERY',
  'IN_PROGRESS',
  'READY_FOR_QA',
  'DONE',
  'BLOCKED',
] as const;

export const TRACEABILITY_LINK_STATUS_OPTIONS: ReadonlyArray<StoryLinkStatus | 'ALL'> = [
  'ALL',
  'KNOWN',
  'UNKNOWN_STORY',
  'NO_STORY_ID',
  'AMBIGUOUS',
] as const;

