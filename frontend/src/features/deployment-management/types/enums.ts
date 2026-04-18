export type DeployState =
  | 'PENDING' | 'IN_PROGRESS'
  | 'SUCCEEDED' | 'FAILED' | 'CANCELLED' | 'ROLLED_BACK';

export type DeployTrigger =
  | 'PUSH_TO_MAIN' | 'MANUAL' | 'SCHEDULED'
  | 'PROMOTE_FROM_DEV' | 'PROMOTE_FROM_TEST' | 'PROMOTE_FROM_STAGING'
  | 'ROLLBACK';

export type DeployStageState =
  | 'SUCCESS' | 'FAILURE' | 'ABORTED' | 'UNSTABLE'
  | 'SKIPPED' | 'IN_PROGRESS' | 'NOT_STARTED';

export type ReleaseState = 'PREPARED' | 'DEPLOYED' | 'SUPERSEDED' | 'ABANDONED';

export type EnvironmentKind = 'DEV' | 'TEST' | 'STAGING' | 'PROD' | 'CUSTOM';

export type ApprovalDecision = 'APPROVED' | 'REJECTED' | 'TIMED_OUT';

export type ApprovalState = 'PROMPTED' | 'APPROVED' | 'REJECTED' | 'TIMED_OUT';

export type AiRowStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'STALE' | 'SUPERSEDED' | 'EVIDENCE_MISMATCH' | 'SKIPPED';

export type HealthLed = 'GREEN' | 'AMBER' | 'RED' | 'UNKNOWN';

export type ChangeLogEntryType =
  | 'APPLICATION_REGISTERED' | 'DEPLOY_INGESTED' | 'RELEASE_CREATED'
  | 'APPROVAL_RECORDED' | 'ROLLBACK_DETECTED'
  | 'RELEASE_NOTES_GENERATED' | 'SUMMARY_GENERATED'
  | 'INSTANCE_BACKFILLED' | 'RESYNC_DRIFT_HEALED';

export type RollbackDetectionSignal = 'trigger=rollback' | 'version-older-than-prior';

export type ProjectRole = 'PM' | 'TECH_LEAD' | 'DEVELOPER' | 'VIEWER';
export type AiAutonomyLevel = 'DISABLED' | 'OBSERVATION' | 'SUPERVISED' | 'AUTONOMOUS';

export type FreshnessTier = 'FRESH' | 'DEGRADED' | 'STALE';
