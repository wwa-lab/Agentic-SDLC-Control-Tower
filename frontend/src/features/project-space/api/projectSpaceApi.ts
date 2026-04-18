import { fetchJson } from '@/shared/api/client';
import type {
  ProjectSpaceAggregate,
  ProjectSpaceCardKey,
  ProjectSpaceSectionMap,
} from '../types/aggregate';
import type { ProjectSummary } from '../types/summary';
import { aggregate as getMockAggregate } from '../mock/aggregate.mock';
import { summary as getMockSummary } from '../mock/summary.mock';
import { leadership as getMockLeadership } from '../mock/leadership.mock';
import { chain as getMockChain } from '../mock/chain.mock';
import { milestones as getMockMilestones } from '../mock/milestones.mock';
import { dependencies as getMockDependencies } from '../mock/dependencies.mock';
import { risks as getMockRisks } from '../mock/risks.mock';
import { environments as getMockEnvironments } from '../mock/environments.mock';

const USE_MOCK = import.meta.env.DEV && !import.meta.env.VITE_USE_BACKEND;

const SECTION_PATHS: Record<ProjectSpaceCardKey, string> = {
  summary: 'summary',
  leadership: 'leadership',
  chain: 'chain',
  milestones: 'milestones',
  dependencies: 'dependencies',
  risks: 'risks',
  environments: 'environments',
};

function getMockSection<K extends ProjectSpaceCardKey>(cardKey: K, projectId: string): ProjectSpaceSectionMap[K] {
  switch (cardKey) {
    case 'summary':
      return { data: getMockSummary(projectId), error: null } as ProjectSpaceSectionMap[K];
    case 'leadership':
      return { data: getMockLeadership(projectId), error: null } as ProjectSpaceSectionMap[K];
    case 'chain':
      return { data: getMockChain(projectId), error: null } as ProjectSpaceSectionMap[K];
    case 'milestones':
      return { data: getMockMilestones(projectId), error: null } as ProjectSpaceSectionMap[K];
    case 'dependencies':
      return { data: getMockDependencies(projectId), error: null } as ProjectSpaceSectionMap[K];
    case 'risks':
      return { data: getMockRisks(projectId), error: null } as ProjectSpaceSectionMap[K];
    case 'environments':
      return { data: getMockEnvironments(projectId), error: null } as ProjectSpaceSectionMap[K];
  }
}

export const projectSpaceApi = {
  async getAggregate(projectId: string): Promise<ProjectSpaceAggregate> {
    if (USE_MOCK) {
      return getMockAggregate(projectId);
    }
    return fetchJson<ProjectSpaceAggregate>(`/project-space/${projectId}`);
  },

  async getSummary(projectId: string): Promise<ProjectSummary> {
    if (USE_MOCK) {
      return getMockSummary(projectId);
    }
    return fetchJson<ProjectSummary>(`/project-space/${projectId}/summary`);
  },

  async getSection<K extends ProjectSpaceCardKey>(cardKey: K, projectId: string): Promise<ProjectSpaceSectionMap[K]> {
    if (USE_MOCK) {
      return getMockSection(cardKey, projectId);
    }
    const rawSection = await fetchJson<ProjectSpaceSectionMap[K]['data']>(`/project-space/${projectId}/${SECTION_PATHS[cardKey]}`);
    return {
      data: rawSection,
      error: null,
    } as ProjectSpaceSectionMap[K];
  },
};
