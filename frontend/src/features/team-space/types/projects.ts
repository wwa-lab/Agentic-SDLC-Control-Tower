import type { LifecycleStage, ProjectHealthStratum } from './enums';

export interface ProjectCardDto {
  readonly id: string;
  readonly name: string;
  readonly lifecycleStage: LifecycleStage;
  readonly healthStratum: ProjectHealthStratum;
  readonly primaryRisk: string | null;
  readonly activeSpecCount: number;
  readonly openIncidentCount: number;
  readonly projectSpaceUrl: string;
}

export interface ProjectDistribution {
  readonly groups: Record<ProjectHealthStratum, ReadonlyArray<ProjectCardDto>>;
  readonly totals: Record<ProjectHealthStratum, number>;
}
