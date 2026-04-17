import type {
  EnvironmentGateStatus,
  EnvironmentKind,
  HealthAggregate,
  VersionDriftBand,
} from './enums';

export interface VersionDrift {
  readonly band: VersionDriftBand;
  readonly commitDelta: number;
  readonly sinceVersion: string;
  readonly description: string;
}

export interface EnvironmentApprover {
  readonly memberId: string;
  readonly displayName: string;
}

export interface Environment {
  readonly id: string;
  readonly label: string;
  readonly kind: EnvironmentKind;
  readonly versionRef: string;
  readonly buildId: string;
  readonly health: HealthAggregate;
  readonly gateStatus: EnvironmentGateStatus;
  readonly approver: EnvironmentApprover | null;
  readonly lastDeployedAt: string;
  readonly drift: VersionDrift | null;
  readonly deploymentLink: {
    readonly url: string;
    readonly enabled: boolean;
  };
}

export interface EnvironmentMatrix {
  readonly environments: ReadonlyArray<Environment>;
}
