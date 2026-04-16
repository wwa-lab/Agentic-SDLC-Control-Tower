import type { SectionResult } from '@/shared/types/section';
import type { WorkspaceSummary } from './workspace';
import type { TeamOperatingModel } from './operatingModel';
import type { MemberMatrix } from './members';
import type { TeamDefaultTemplates } from './templates';
import type { RequirementPipeline } from './pipeline';
import type { TeamMetrics } from './metrics';
import type { TeamRiskRadar } from './risks';
import type { ProjectDistribution } from './projects';

export interface TeamSpaceAggregate {
  readonly workspaceId: string;
  readonly summary: SectionResult<WorkspaceSummary>;
  readonly operatingModel: SectionResult<TeamOperatingModel>;
  readonly members: SectionResult<MemberMatrix>;
  readonly templates: SectionResult<TeamDefaultTemplates>;
  readonly pipeline: SectionResult<RequirementPipeline>;
  readonly metrics: SectionResult<TeamMetrics>;
  readonly risks: SectionResult<TeamRiskRadar>;
  readonly projects: SectionResult<ProjectDistribution>;
}

export const TEAM_SPACE_CARD_KEYS = [
  'summary',
  'operatingModel',
  'members',
  'templates',
  'pipeline',
  'metrics',
  'risks',
  'projects',
] as const;

export type TeamSpaceCardKey = typeof TEAM_SPACE_CARD_KEYS[number];

export type TeamSpaceSectionMap = Omit<TeamSpaceAggregate, 'workspaceId'>;

export interface TeamSpaceState {
  readonly workspaceId: string | null;
  readonly aggregate: TeamSpaceAggregate | null;
  readonly isLoading: boolean;
  readonly error: string | null;
  readonly loadingCards: Record<TeamSpaceCardKey, boolean>;
}

export function createLoadingCardState(): Record<TeamSpaceCardKey, boolean> {
  return {
    summary: false,
    operatingModel: false,
    members: false,
    templates: false,
    pipeline: false,
    metrics: false,
    risks: false,
    projects: false,
  };
}
