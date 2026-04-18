export type ProjectLifecycleStage =
  | 'DISCOVERY'
  | 'DELIVERY'
  | 'STEADY_STATE'
  | 'RETIRING';

export type HealthAggregate = 'GREEN' | 'YELLOW' | 'RED' | 'UNKNOWN';

export type MilestoneStatus =
  | 'NOT_STARTED'
  | 'IN_PROGRESS'
  | 'AT_RISK'
  | 'COMPLETED'
  | 'SLIPPED';

export type DependencyDirection = 'UPSTREAM' | 'DOWNSTREAM';

export type DependencyRelationship =
  | 'API'
  | 'DATA'
  | 'SCHEDULE'
  | 'SLA';

export type EnvironmentKind = 'DEV' | 'STAGING' | 'PROD' | 'CUSTOM';

export type EnvironmentGateStatus =
  | 'AUTO'
  | 'APPROVAL_REQUIRED'
  | 'BLOCKED';

export type VersionDriftBand = 'NONE' | 'MINOR' | 'MAJOR';

export type RiskSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export type RiskCategory =
  | 'TECHNICAL'
  | 'SECURITY'
  | 'DELIVERY'
  | 'DEPENDENCY'
  | 'GOVERNANCE';

export type AccountableRole =
  | 'PM'
  | 'ARCHITECT'
  | 'TECH_LEAD'
  | 'QA_LEAD'
  | 'SRE'
  | 'AI_ADOPTION';

export type OncallStatus = 'ON' | 'OFF' | 'UPCOMING' | 'NONE';

export type SdlcNodeKey =
  | 'REQUIREMENT'
  | 'USER_STORY'
  | 'SPEC'
  | 'ARCHITECTURE'
  | 'DESIGN'
  | 'TASKS'
  | 'CODE'
  | 'TEST'
  | 'DEPLOY'
  | 'INCIDENT'
  | 'LEARNING';

export type HealthFactorSeverity = 'INFO' | 'WARN' | 'CRIT';

export const DEFAULT_PROJECT_ID = 'proj-42';

export const PROJECT_ID_PATTERN = /^proj-[a-z0-9-]+$/;

export const ACCOUNTABLE_ROLE_ORDER: ReadonlyArray<AccountableRole> = [
  'PM',
  'ARCHITECT',
  'TECH_LEAD',
  'QA_LEAD',
  'SRE',
  'AI_ADOPTION',
];

export const SDLC_NODE_ORDER: ReadonlyArray<SdlcNodeKey> = [
  'REQUIREMENT',
  'USER_STORY',
  'SPEC',
  'ARCHITECTURE',
  'DESIGN',
  'TASKS',
  'CODE',
  'TEST',
  'DEPLOY',
  'INCIDENT',
  'LEARNING',
];
