import type { SectionResult } from '@/shared/types/section';
import type { DeployState, EnvironmentKind, HealthLed, AiRowStatus } from './enums';

export interface CatalogSummary {
  readonly visibleApplications: number;
  readonly releasesLast7d: number;
  readonly deploysLast7d: number;
  readonly deploySuccessRate7d: number;
  readonly medianDeployFrequency: number;
  readonly changeFailureRate30d: number;
  readonly byLed: Readonly<Record<HealthLed, number>>;
}

export interface EnvRevisionChip {
  readonly environmentName: string;
  readonly revisionReleaseVersion: string | null;
  readonly deployState: DeployState | null;
  readonly deployedAt: string | null;
  readonly isRolledBack: boolean;
}

export interface CatalogApplicationTile {
  readonly applicationId: string;
  readonly name: string;
  readonly projectId: string;
  readonly workspaceId: string;
  readonly runtimeLabel: string;
  readonly lastDeployAt: string | null;
  readonly environmentRevisions: ReadonlyArray<EnvRevisionChip>;
  readonly aggregateLed: HealthLed;
  readonly description?: string;
}

export interface CatalogSection {
  readonly projectId: string;
  readonly projectName: string;
  readonly applications: ReadonlyArray<CatalogApplicationTile>;
}

export interface CatalogFilters {
  readonly projectIds?: ReadonlyArray<string>;
  readonly environmentKind?: EnvironmentKind;
  readonly deployStatus?: HealthLed;
  readonly window?: '24h' | '7d' | '30d';
  readonly search?: string;
}

export interface AiWorkspaceDeploymentSummary {
  readonly status: AiRowStatus;
  readonly generatedAt?: string;
  readonly narrative?: string;
  readonly error?: { readonly code: string; readonly message: string };
}

export interface CatalogAggregate {
  readonly summary: SectionResult<CatalogSummary>;
  readonly grid: SectionResult<ReadonlyArray<CatalogSection>>;
  readonly aiSummary: SectionResult<AiWorkspaceDeploymentSummary>;
  readonly filtersEcho: CatalogFilters;
}
