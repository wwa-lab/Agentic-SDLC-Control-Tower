import type { SectionResult } from '@/shared/types/section';
import type { CatalogAggregate, CatalogFilters } from './catalog';
import type { ApplicationDetailAggregate } from './application';
import type { ReleaseDetailAggregate } from './release';
import type { DeployDetailAggregate } from './deploy';
import type { EnvironmentDetailAggregate } from './environment';
import type { TraceabilityAggregate } from './traceability';
import type { AiAutonomyLevel, ProjectRole } from './enums';

export const DP_DEFAULT_WORKSPACE_ID = 'ws-default-001';
export const DP_DEFAULT_ROLE: ProjectRole = 'DEVELOPER';
export const DP_DEFAULT_AUTONOMY: AiAutonomyLevel = 'SUPERVISED';

export interface DeploymentViewerContext {
  readonly workspaceId: string;
  readonly role: ProjectRole;
  readonly autonomyLevel: AiAutonomyLevel;
}

export const DP_ROLE_OPTIONS: ReadonlyArray<ProjectRole> = ['PM', 'TECH_LEAD', 'DEVELOPER', 'VIEWER'];
export const DP_AUTONOMY_OPTIONS: ReadonlyArray<AiAutonomyLevel> = [
  'DISABLED', 'OBSERVATION', 'SUPERVISED', 'AUTONOMOUS',
];

export const DP_CATALOG_CARD_KEYS = ['summary', 'grid', 'aiSummary'] as const;
export const DP_APPLICATION_CARD_KEYS = ['header', 'environments', 'recentReleases', 'recentDeploys', 'traceSummary', 'aiInsights'] as const;
export const DP_RELEASE_CARD_KEYS = ['header', 'commits', 'linkedStories', 'deploys', 'aiNotes'] as const;
export const DP_DEPLOY_CARD_KEYS = ['header', 'stageTimeline', 'approvals', 'artifactRef'] as const;
export const DP_ENVIRONMENT_CARD_KEYS = ['header', 'revisions', 'timeline', 'metrics'] as const;
export const DP_TRACEABILITY_CARD_KEYS = ['releases', 'deploysByEnvironment'] as const;

export type CatalogCardKey = (typeof DP_CATALOG_CARD_KEYS)[number];
export type ApplicationCardKey = (typeof DP_APPLICATION_CARD_KEYS)[number];
export type ReleaseCardKey = (typeof DP_RELEASE_CARD_KEYS)[number];
export type DeployCardKey = (typeof DP_DEPLOY_CARD_KEYS)[number];
export type EnvironmentCardKey = (typeof DP_ENVIRONMENT_CARD_KEYS)[number];
export type TraceabilityCardKey = (typeof DP_TRACEABILITY_CARD_KEYS)[number];

export const DP_DEFAULT_CATALOG_FILTERS: CatalogFilters = {
  window: '7d',
};

export interface DeploymentManagementState {
  readonly viewerContext: DeploymentViewerContext;
  readonly catalogFilters: CatalogFilters;
  readonly catalog: CatalogAggregate | null;
  readonly activeApplicationId: string | null;
  readonly applicationDetail: ApplicationDetailAggregate | null;
  readonly activeReleaseId: string | null;
  readonly releaseDetail: ReleaseDetailAggregate | null;
  readonly activeDeployId: string | null;
  readonly deployDetail: DeployDetailAggregate | null;
  readonly activeEnvironmentKey: string | null;
  readonly environmentDetail: EnvironmentDetailAggregate | null;
  readonly activeStoryId: string | null;
  readonly traceability: TraceabilityAggregate | null;
  readonly loading: Readonly<Record<string, boolean>>;
  readonly errors: Readonly<Record<string, { readonly code: string; readonly message: string } | null>>;
}
