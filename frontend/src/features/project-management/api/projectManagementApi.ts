import { fetchJson, patchJson, postJson } from '@/shared/api/client';
import type {
  AiSuggestionActionResult,
  CounterSignDependencyRequest,
  PlanAggregate,
  PlanSectionMap,
  PortfolioAggregate,
  PortfolioSectionMap,
  ProjectManagementPlanCardKey,
  ProjectManagementPortfolioCardKey,
  SaveCapacityBatchRequest,
  TransitionDependencyRequest,
  TransitionMilestoneRequest,
  TransitionRiskRequest,
} from '../types';
import {
  acceptMockSuggestion,
  counterSignMockDependency,
  dismissMockSuggestion,
  getMockPlanAggregate,
  getMockPlanSection,
  getMockPortfolioAggregate,
  getMockPortfolioSection,
  saveMockCapacity,
  transitionMockDependency,
  transitionMockMilestone,
  transitionMockRisk,
} from '../mock/state';

const USE_MOCK = import.meta.env.DEV && !import.meta.env.VITE_USE_BACKEND;

const PORTFOLIO_PATHS: Record<ProjectManagementPortfolioCardKey, string> = {
  summary: 'summary',
  heatmap: 'heatmap',
  capacity: 'capacity',
  risks: 'risks',
  bottlenecks: 'dependencies',
  cadence: 'cadence',
};

const PLAN_PATHS: Record<ProjectManagementPlanCardKey, string> = {
  header: 'header',
  milestones: 'milestones',
  capacity: 'capacity',
  risks: 'risks',
  dependencies: 'dependencies',
  progress: 'progress',
  changeLog: 'change-log',
  aiSuggestions: 'ai-suggestions',
};

export const projectManagementApi = {
  async getPortfolioAggregate(workspaceId: string): Promise<PortfolioAggregate> {
    if (USE_MOCK) {
      return getMockPortfolioAggregate(workspaceId);
    }
    return fetchJson<PortfolioAggregate>(`/project-management/portfolio?workspaceId=${encodeURIComponent(workspaceId)}`);
  },

  async getPortfolioSection<K extends ProjectManagementPortfolioCardKey>(cardKey: K, workspaceId: string): Promise<PortfolioSectionMap[K]> {
    if (USE_MOCK) {
      return getMockPortfolioSection(cardKey, workspaceId);
    }
    const data = await fetchJson<PortfolioSectionMap[K]['data']>(
      `/project-management/portfolio/${PORTFOLIO_PATHS[cardKey]}?workspaceId=${encodeURIComponent(workspaceId)}`,
    );
    return {
      data,
      error: null,
    } as PortfolioSectionMap[K];
  },

  async getPlanAggregate(projectId: string): Promise<PlanAggregate> {
    if (USE_MOCK) {
      return getMockPlanAggregate(projectId);
    }
    return fetchJson<PlanAggregate>(`/project-management/plan/${encodeURIComponent(projectId)}`);
  },

  async getPlanSection<K extends ProjectManagementPlanCardKey>(cardKey: K, projectId: string): Promise<PlanSectionMap[K]> {
    if (USE_MOCK) {
      return getMockPlanSection(cardKey, projectId);
    }
    const data = await fetchJson<PlanSectionMap[K]['data']>(
      `/project-management/plan/${encodeURIComponent(projectId)}/${PLAN_PATHS[cardKey]}`,
    );
    return {
      data,
      error: null,
    } as PlanSectionMap[K];
  },

  async transitionMilestone(projectId: string, milestoneId: string, request: TransitionMilestoneRequest) {
    if (USE_MOCK) {
      return transitionMockMilestone(projectId, milestoneId, request);
    }
    return postJson(`/project-management/plan/${encodeURIComponent(projectId)}/milestones/${encodeURIComponent(milestoneId)}/transition`, request);
  },

  async saveCapacity(projectId: string, request: SaveCapacityBatchRequest) {
    if (USE_MOCK) {
      return saveMockCapacity(projectId, request);
    }
    return patchJson(`/project-management/plan/${encodeURIComponent(projectId)}/capacity`, request);
  },

  async transitionRisk(projectId: string, riskId: string, request: TransitionRiskRequest) {
    if (USE_MOCK) {
      return transitionMockRisk(projectId, riskId, request);
    }
    return postJson(`/project-management/plan/${encodeURIComponent(projectId)}/risks/${encodeURIComponent(riskId)}/transition`, request);
  },

  async transitionDependency(projectId: string, dependencyId: string, request: TransitionDependencyRequest) {
    if (USE_MOCK) {
      return transitionMockDependency(projectId, dependencyId, request);
    }
    return postJson(`/project-management/plan/${encodeURIComponent(projectId)}/dependencies/${encodeURIComponent(dependencyId)}/transition`, request);
  },

  async counterSignDependency(projectId: string, dependencyId: string, request: CounterSignDependencyRequest) {
    if (USE_MOCK) {
      return counterSignMockDependency(projectId, dependencyId, request);
    }
    return postJson(`/project-management/plan/${encodeURIComponent(projectId)}/dependencies/${encodeURIComponent(dependencyId)}/countersign`, request);
  },

  async acceptAiSuggestion(projectId: string, suggestionId: string): Promise<AiSuggestionActionResult> {
    if (USE_MOCK) {
      return acceptMockSuggestion(projectId, suggestionId);
    }
    return postJson(`/project-management/plan/${encodeURIComponent(projectId)}/ai-suggestions/${encodeURIComponent(suggestionId)}/accept`);
  },

  async dismissAiSuggestion(projectId: string, suggestionId: string, reason?: string | null): Promise<AiSuggestionActionResult> {
    if (USE_MOCK) {
      return dismissMockSuggestion(projectId, suggestionId, { reason });
    }
    return postJson(`/project-management/plan/${encodeURIComponent(projectId)}/ai-suggestions/${encodeURIComponent(suggestionId)}/dismiss`, { reason });
  },
};
