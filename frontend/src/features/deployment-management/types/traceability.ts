import type { SectionResult } from '@/shared/types/section';
import type { DeployState, EnvironmentKind, ReleaseState } from './enums';

export interface StoryChip {
  readonly storyId: string;
  readonly status: 'VERIFIED' | 'UNVERIFIED' | 'UNKNOWN_STORY';
  readonly title?: string;
  readonly projectId?: string;
}

export interface TraceabilityReleaseRow {
  readonly releaseId: string;
  readonly releaseVersion: string;
  readonly applicationId: string;
  readonly applicationName: string;
  readonly createdAt: string;
  readonly state: ReleaseState;
}

export interface TraceabilityDeployGroup {
  readonly environmentName: string;
  readonly kind: EnvironmentKind;
  readonly deploys: ReadonlyArray<{
    readonly deployId: string;
    readonly releaseVersion: string;
    readonly state: DeployState;
    readonly startedAt: string;
    readonly isCurrentRevision: boolean;
    readonly isRollback: boolean;
  }>;
}

export interface TraceabilityAggregate {
  readonly storyChip: StoryChip;
  readonly releases: SectionResult<ReadonlyArray<TraceabilityReleaseRow>>;
  readonly deploysByEnvironment: SectionResult<ReadonlyArray<TraceabilityDeployGroup>>;
  readonly upstreamAvailable: boolean;
}
