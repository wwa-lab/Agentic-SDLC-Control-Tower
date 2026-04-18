import type { SectionResult } from '@/shared/types/section';
import type { AiRowStatus, DeployState, EnvironmentKind, ReleaseState } from './enums';

export interface ApplicationHeader {
  readonly applicationId: string;
  readonly name: string;
  readonly projectId: string;
  readonly workspaceId: string;
  readonly runtimeLabel: string;
  readonly jenkinsFolderPath: string;
  readonly jenkinsFolderUrl: string;
  readonly lastDeployAt: string | null;
  readonly description?: string;
}

export interface EnvironmentRow {
  readonly environmentName: string;
  readonly kind: EnvironmentKind;
  readonly currentRevision: string | null;
  readonly currentRevisionReleaseId: string | null;
  readonly currentDeployState: DeployState | null;
  readonly currentDeployedAt: string | null;
  readonly priorRevision: string | null;
  readonly lastGoodRevision: string | null;
  readonly isRolledBack: boolean;
  readonly rolledBackToReleaseVersion: string | null;
}

export interface RecentReleaseRow {
  readonly releaseId: string;
  readonly releaseVersion: string;
  readonly buildArtifactRef: { readonly sliceId: 'code-build'; readonly buildArtifactId: string };
  readonly createdAt: string;
  readonly state: ReleaseState;
  readonly storyCount: number;
}

export interface RecentDeployRow {
  readonly deployId: string;
  readonly releaseVersion: string;
  readonly environmentName: string;
  readonly state: DeployState;
  readonly startedAt: string;
  readonly durationSec?: number;
  readonly approverDisplayName?: string;
  readonly isCurrentRevision: boolean;
  readonly isRollback: boolean;
}

export interface TraceSummaryRow {
  readonly environmentName: string;
  readonly storiesLast30d: number;
  readonly deploysLast30d: number;
}

export interface ApplicationAiInsight {
  readonly status: AiRowStatus;
  readonly generatedAt?: string;
  readonly narrative?: string;
  readonly evidence?: ReadonlyArray<{ readonly kind: 'release' | 'deploy' | 'environment'; readonly id: string; readonly label: string }>;
  readonly skillVersion?: string;
  readonly error?: { readonly code: string; readonly message: string };
}

export interface ApplicationDetailAggregate {
  readonly header: SectionResult<ApplicationHeader>;
  readonly environments: SectionResult<ReadonlyArray<EnvironmentRow>>;
  readonly recentReleases: SectionResult<ReadonlyArray<RecentReleaseRow>>;
  readonly recentDeploys: SectionResult<ReadonlyArray<RecentDeployRow>>;
  readonly traceSummary: SectionResult<ReadonlyArray<TraceSummaryRow>>;
  readonly aiInsights: SectionResult<ApplicationAiInsight>;
}
