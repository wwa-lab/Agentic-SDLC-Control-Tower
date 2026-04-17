import type {
  DependencyDirection,
  DependencyRelationship,
  HealthAggregate,
} from './enums';

export interface Dependency {
  readonly id: string;
  readonly targetName: string;
  readonly targetRef: string;
  readonly targetProjectId: string | null;
  readonly external: boolean;
  readonly direction: DependencyDirection;
  readonly relationship: DependencyRelationship;
  readonly ownerTeam: string;
  readonly health: HealthAggregate;
  readonly blockerReason: string | null;
  readonly primaryAction: {
    readonly label: string;
    readonly url: string;
  } | null;
}

export interface DependencyMap {
  readonly upstream: ReadonlyArray<Dependency>;
  readonly downstream: ReadonlyArray<Dependency>;
}
