export type RepoVisibility = 'PUBLIC' | 'PRIVATE';
export type PrState = 'OPEN' | 'MERGED' | 'CLOSED' | 'DRAFT';
export type RunStatus = 'SUCCESS' | 'FAILURE' | 'RUNNING' | 'QUEUED' | 'CANCELLED' | 'NEUTRAL';
export type RunTrigger = 'PUSH' | 'PULL_REQUEST' | 'MANUAL' | 'SCHEDULE' | 'RERUN';
export type StepConclusion = 'SUCCESS' | 'FAILURE' | 'SKIPPED' | 'RUNNING' | 'CANCELLED';
export type StoryLinkStatus = 'KNOWN' | 'UNKNOWN_STORY' | 'NO_STORY_ID' | 'AMBIGUOUS';
export type AiNoteSeverity = 'BLOCKER' | 'MAJOR' | 'MINOR' | 'INFO';
export type AiRowStatus = 'SUCCESS' | 'PENDING' | 'FAILED_EVIDENCE';
export type ChangeLogEntryType =
  | 'AI_PR_REVIEW_REGENERATED'
  | 'AI_TRIAGE_REGENERATED'
  | 'RUN_RERUN_REQUESTED'
  | 'WEBHOOK_INGESTED'
  | 'HEAD_ADVANCED';

export type ProjectRole = 'PM' | 'TECH_LEAD' | 'DEVELOPER' | 'VIEWER';
export type AiAutonomyLevel = 'DISABLED' | 'OBSERVATION' | 'SUPERVISED' | 'AUTONOMOUS';
export type BuildSort = 'RECENT_ACTIVITY' | 'ALPHABETICAL' | 'HEALTH_WORST_FIRST';
export type BuildStatusFilter = RunStatus | 'NO_RUNS' | 'ALL';
export type VisibilityFilter = RepoVisibility | 'ALL';
export type StoryState = 'DISCOVERY' | 'IN_PROGRESS' | 'READY_FOR_QA' | 'DONE' | 'BLOCKED';

