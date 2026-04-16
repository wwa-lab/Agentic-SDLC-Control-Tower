import type { HealthAggregate } from './enums';

export interface ResponsibilityBoundary {
  readonly applications: ReadonlyArray<string>;
  readonly snowGroups: ReadonlyArray<string>;
  readonly projectCount: number;
}

export interface WorkspaceSummary {
  readonly id: string;
  readonly name: string;
  readonly applicationId: string;
  readonly applicationName: string;
  readonly snowGroupId: string | null;
  readonly snowGroupName: string | null;
  readonly activeProjectCount: number;
  readonly activeEnvironmentCount: number;
  readonly healthAggregate: HealthAggregate;
  readonly ownerId: string;
  readonly ownerDisplayName: string;
  readonly compatibilityMode: boolean;
  readonly responsibilityBoundary: ResponsibilityBoundary;
}
