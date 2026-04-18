import type { SectionResult } from '@/shared/types/section';
import type { DeployState, EnvironmentKind } from './enums';

export interface EnvironmentHeader {
  readonly applicationId: string;
  readonly environmentName: string;
  readonly kind: EnvironmentKind;
}

export interface EnvironmentRevisions {
  readonly currentRevision: string | null;
  readonly currentDeployId: string | null;
  readonly priorRevision: string | null;
  readonly priorDeployId: string | null;
  readonly lastGoodRevision: string | null;
  readonly lastGoodDeployId: string | null;
  readonly lastFailedRevision: string | null;
  readonly lastFailedDeployId: string | null;
}

export interface EnvironmentTimelineEntry {
  readonly deployId: string;
  readonly releaseVersion: string;
  readonly state: DeployState;
  readonly startedAt: string;
  readonly durationSec?: number;
  readonly isRollback: boolean;
}

export interface EnvironmentMetrics {
  readonly changeFailureRate30d: number;
  readonly mttrSec30d: number | null;
  readonly deployCount30d: number;
  readonly rollbackCount30d: number;
  readonly deploymentFrequency30d: number;
}

export interface EnvironmentDetailAggregate {
  readonly header: SectionResult<EnvironmentHeader>;
  readonly revisions: SectionResult<EnvironmentRevisions>;
  readonly timeline: SectionResult<ReadonlyArray<EnvironmentTimelineEntry>>;
  readonly metrics: SectionResult<EnvironmentMetrics>;
}
