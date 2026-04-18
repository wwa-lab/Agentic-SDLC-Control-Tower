import type { SectionResult } from '@/shared/types/section';
import type { ProjectSummary } from './summary';
import type { LeadershipOwnership } from './leadership';
import type { SdlcChainState } from './chain';
import type { MilestoneHub } from './milestones';
import type { DependencyMap } from './dependencies';
import type { RiskRegistry } from './risks';
import type { EnvironmentMatrix } from './environments';

export interface ProjectSpaceAggregate {
  readonly projectId: string;
  readonly workspaceId: string;
  readonly summary: SectionResult<ProjectSummary>;
  readonly leadership: SectionResult<LeadershipOwnership>;
  readonly chain: SectionResult<SdlcChainState>;
  readonly milestones: SectionResult<MilestoneHub>;
  readonly dependencies: SectionResult<DependencyMap>;
  readonly risks: SectionResult<RiskRegistry>;
  readonly environments: SectionResult<EnvironmentMatrix>;
}

export const PROJECT_SPACE_CARD_KEYS = [
  'summary',
  'leadership',
  'chain',
  'milestones',
  'dependencies',
  'risks',
  'environments',
] as const;

export type ProjectSpaceCardKey = typeof PROJECT_SPACE_CARD_KEYS[number];

export type ProjectSpaceSectionMap = Omit<ProjectSpaceAggregate, 'projectId' | 'workspaceId'>;

export interface ProjectSpaceState {
  readonly projectId: string | null;
  readonly workspaceId: string | null;
  readonly aggregate: ProjectSpaceAggregate | null;
  readonly isLoading: boolean;
  readonly error: string | null;
  readonly errorStatus: number | null;
  readonly loadingCards: Record<ProjectSpaceCardKey, boolean>;
}

export function createLoadingCardState(): Record<ProjectSpaceCardKey, boolean> {
  return {
    summary: false,
    leadership: false,
    chain: false,
    milestones: false,
    dependencies: false,
    risks: false,
    environments: false,
  };
}
