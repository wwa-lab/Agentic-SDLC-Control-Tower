import { fetchJson } from '@/shared/api/client';
import type { TeamSpaceAggregate, TeamSpaceCardKey, TeamSpaceSectionMap } from '../types/aggregate';
import type { WorkspaceSummary } from '../types/workspace';
import { aggregate as getMockAggregate } from '../mock/aggregate.mock';
import { summary as getMockSummary } from '../mock/summary.mock';
import { operatingModel as getMockOperatingModel } from '../mock/operatingModel.mock';
import { members as getMockMembers } from '../mock/members.mock';
import { templates as getMockTemplates } from '../mock/templates.mock';
import { pipeline as getMockPipeline } from '../mock/pipeline.mock';
import { metrics as getMockMetrics } from '../mock/metrics.mock';
import { risks as getMockRisks } from '../mock/risks.mock';
import { projects as getMockProjects } from '../mock/projects.mock';

const USE_MOCK = import.meta.env.DEV && !import.meta.env.VITE_USE_BACKEND;

const SECTION_PATHS: Record<TeamSpaceCardKey, string> = {
  summary: 'summary',
  operatingModel: 'operating-model',
  members: 'members',
  templates: 'templates',
  pipeline: 'pipeline',
  metrics: 'metrics',
  risks: 'risks',
  projects: 'projects',
};

function getMockSection<K extends TeamSpaceCardKey>(cardKey: K, workspaceId: string): TeamSpaceSectionMap[K] {
  switch (cardKey) {
    case 'summary':
      return { data: getMockSummary(workspaceId), error: null } as TeamSpaceSectionMap[K];
    case 'operatingModel':
      return { data: getMockOperatingModel(workspaceId), error: null } as TeamSpaceSectionMap[K];
    case 'members':
      return { data: getMockMembers(workspaceId), error: null } as TeamSpaceSectionMap[K];
    case 'templates':
      return { data: getMockTemplates(workspaceId), error: null } as TeamSpaceSectionMap[K];
    case 'pipeline':
      return { data: getMockPipeline(workspaceId), error: null } as TeamSpaceSectionMap[K];
    case 'metrics':
      return { data: getMockMetrics(workspaceId), error: null } as TeamSpaceSectionMap[K];
    case 'risks':
      return { data: getMockRisks(workspaceId), error: null } as TeamSpaceSectionMap[K];
    case 'projects':
      return { data: getMockProjects(workspaceId), error: null } as TeamSpaceSectionMap[K];
  }
}

export const teamSpaceApi = {
  async getAggregate(workspaceId: string): Promise<TeamSpaceAggregate> {
    if (USE_MOCK) {
      return getMockAggregate(workspaceId);
    }
    return fetchJson<TeamSpaceAggregate>(`/team-space/${workspaceId}`);
  },

  async getSummary(workspaceId: string): Promise<WorkspaceSummary> {
    if (USE_MOCK) {
      return getMockSummary(workspaceId);
    }
    return fetchJson<WorkspaceSummary>(`/team-space/${workspaceId}/summary`);
  },

  async getSection<K extends TeamSpaceCardKey>(cardKey: K, workspaceId: string): Promise<TeamSpaceSectionMap[K]> {
    if (USE_MOCK) {
      return getMockSection(cardKey, workspaceId);
    }
    const rawSection = await fetchJson<TeamSpaceSectionMap[K]['data']>(`/team-space/${workspaceId}/${SECTION_PATHS[cardKey]}`);
    return {
      data: rawSection,
      error: null,
    } as TeamSpaceSectionMap[K];
  },
};
