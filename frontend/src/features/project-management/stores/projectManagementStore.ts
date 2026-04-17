import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { ApiError } from '@/shared/api/client';
import { projectManagementApi } from '../api/projectManagementApi';
import {
  PM_PLAN_CARD_KEYS,
  PM_PORTFOLIO_CARD_KEYS,
  type PlanAggregate,
  type ProjectManagementPlanCardKey,
  type ProjectManagementPortfolioCardKey,
  type SaveCapacityBatchRequest,
  type TransitionDependencyRequest,
  type TransitionMilestoneRequest,
  type TransitionRiskRequest,
} from '../types';

function toUserMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError && error.message) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

function toStatus(error: unknown): number | null {
  return error instanceof ApiError ? error.status : null;
}

function createLoadingState<K extends string>(keys: readonly K[]): Record<K, boolean> {
  return Object.fromEntries(keys.map(key => [key, false])) as Record<K, boolean>;
}

export const useProjectManagementStore = defineStore('projectManagement', () => {
  const workspaceId = ref<string | null>(null);
  const projectId = ref<string | null>(null);

  const portfolio = ref<import('../types').PortfolioAggregate | null>(null);
  const plan = ref<PlanAggregate | null>(null);

  const portfolioLoading = ref(false);
  const planLoading = ref(false);

  const portfolioError = ref<string | null>(null);
  const portfolioErrorStatus = ref<number | null>(null);
  const planError = ref<string | null>(null);
  const planErrorStatus = ref<number | null>(null);

  const portfolioLoadingCards = ref(createLoadingState(PM_PORTFOLIO_CARD_KEYS));
  const planLoadingCards = ref(createLoadingState(PM_PLAN_CARD_KEYS));

  const mutationMessage = ref<string | null>(null);
  const mutationError = ref<string | null>(null);
  const mutating = ref(false);

  const isPortfolioForbidden = computed(() => portfolioErrorStatus.value === 403);
  const isPlanForbidden = computed(() => planErrorStatus.value === 403);
  const isPlanNotFound = computed(() => planErrorStatus.value === 404);
  const currentPlanRevision = computed(() => plan.value?.header.data?.planRevision ?? 0);

  async function initPortfolio(nextWorkspaceId: string) {
    workspaceId.value = nextWorkspaceId;
    portfolioLoading.value = true;
    portfolioError.value = null;
    portfolioErrorStatus.value = null;

    try {
      portfolio.value = await projectManagementApi.getPortfolioAggregate(nextWorkspaceId);
    } catch (error) {
      portfolio.value = null;
      portfolioError.value = toUserMessage(error, 'Failed to load Project Management portfolio.');
      portfolioErrorStatus.value = toStatus(error);
    } finally {
      portfolioLoading.value = false;
    }
  }

  async function initPlan(nextProjectId: string) {
    projectId.value = nextProjectId;
    planLoading.value = true;
    planError.value = null;
    planErrorStatus.value = null;

    try {
      plan.value = await projectManagementApi.getPlanAggregate(nextProjectId);
      workspaceId.value = plan.value.header.data?.workspaceId ?? workspaceId.value;
    } catch (error) {
      plan.value = null;
      planError.value = toUserMessage(error, 'Failed to load project plan.');
      planErrorStatus.value = toStatus(error);
    } finally {
      planLoading.value = false;
    }
  }

  async function refreshPortfolioCard(cardKey: ProjectManagementPortfolioCardKey) {
    if (!workspaceId.value || !portfolio.value) {
      return;
    }
    portfolioLoadingCards.value[cardKey] = true;
    try {
      const section = await projectManagementApi.getPortfolioSection(cardKey, workspaceId.value);
      portfolio.value = {
        ...portfolio.value,
        [cardKey]: section,
      };
    } catch (error) {
      portfolio.value = {
        ...portfolio.value,
        [cardKey]: {
          data: null,
          error: toUserMessage(error, `Failed to refresh ${cardKey}.`),
        },
      };
    } finally {
      portfolioLoadingCards.value[cardKey] = false;
    }
  }

  async function refreshPlanCard(cardKey: ProjectManagementPlanCardKey) {
    if (!projectId.value || !plan.value) {
      return;
    }
    planLoadingCards.value[cardKey] = true;
    try {
      const section = await projectManagementApi.getPlanSection(cardKey, projectId.value);
      plan.value = {
        ...plan.value,
        [cardKey]: section,
      };
      if (cardKey === 'header') {
        const headerSection = section as PlanAggregate['header'];
        if (headerSection.data?.workspaceId) {
          workspaceId.value = headerSection.data.workspaceId;
        }
      }
    } catch (error) {
      plan.value = {
        ...plan.value,
        [cardKey]: {
          data: null,
          error: toUserMessage(error, `Failed to refresh ${cardKey}.`),
        },
      };
    } finally {
      planLoadingCards.value[cardKey] = false;
    }
  }

  async function reloadPlanAfterMutation(successMessage: string) {
    if (!projectId.value) {
      return;
    }
    await initPlan(projectId.value);
    if (workspaceId.value) {
      await initPortfolio(workspaceId.value);
    }
    mutationMessage.value = successMessage;
  }

  async function runMutation(work: () => Promise<void>, successMessage: string) {
    mutating.value = true;
    mutationError.value = null;
    mutationMessage.value = null;
    try {
      await work();
      await reloadPlanAfterMutation(successMessage);
    } catch (error) {
      mutationError.value = toUserMessage(error, 'Project Management action failed.');
      throw error;
    } finally {
      mutating.value = false;
    }
  }

  async function transitionMilestone(milestoneId: string, request: TransitionMilestoneRequest) {
    if (!projectId.value) {
      return;
    }
    await runMutation(async () => {
      await projectManagementApi.transitionMilestone(projectId.value as string, milestoneId, request);
    }, 'Milestone plan updated.');
  }

  async function saveCapacity(request: SaveCapacityBatchRequest) {
    if (!projectId.value) {
      return;
    }
    await runMutation(async () => {
      await projectManagementApi.saveCapacity(projectId.value as string, request);
    }, 'Capacity allocation saved.');
  }

  async function transitionRisk(riskId: string, request: TransitionRiskRequest) {
    if (!projectId.value) {
      return;
    }
    await runMutation(async () => {
      await projectManagementApi.transitionRisk(projectId.value as string, riskId, request);
    }, 'Risk registry updated.');
  }

  async function transitionDependency(dependencyId: string, request: TransitionDependencyRequest) {
    if (!projectId.value) {
      return;
    }
    await runMutation(async () => {
      await projectManagementApi.transitionDependency(projectId.value as string, dependencyId, request);
    }, 'Dependency lane updated.');
  }

  async function counterSignDependency(dependencyId: string, planRevision: number) {
    if (!projectId.value) {
      return;
    }
    await runMutation(async () => {
      await projectManagementApi.counterSignDependency(projectId.value as string, dependencyId, { planRevision });
    }, 'Dependency counter-sign completed.');
  }

  async function acceptAiSuggestion(suggestionId: string) {
    if (!projectId.value) {
      return;
    }
    await runMutation(async () => {
      await projectManagementApi.acceptAiSuggestion(projectId.value as string, suggestionId);
    }, 'AI suggestion accepted.');
  }

  async function dismissAiSuggestion(suggestionId: string, reason?: string | null) {
    if (!projectId.value) {
      return;
    }
    await runMutation(async () => {
      await projectManagementApi.dismissAiSuggestion(projectId.value as string, suggestionId, reason);
    }, 'AI suggestion dismissed for 24 hours.');
  }

  function clearMutationFeedback() {
    mutationMessage.value = null;
    mutationError.value = null;
  }

  function reset() {
    workspaceId.value = null;
    projectId.value = null;
    portfolio.value = null;
    plan.value = null;
    portfolioLoading.value = false;
    planLoading.value = false;
    portfolioError.value = null;
    portfolioErrorStatus.value = null;
    planError.value = null;
    planErrorStatus.value = null;
    portfolioLoadingCards.value = createLoadingState(PM_PORTFOLIO_CARD_KEYS);
    planLoadingCards.value = createLoadingState(PM_PLAN_CARD_KEYS);
    mutationMessage.value = null;
    mutationError.value = null;
    mutating.value = false;
  }

  return {
    workspaceId,
    projectId,
    portfolio,
    plan,
    portfolioLoading,
    planLoading,
    portfolioError,
    portfolioErrorStatus,
    planError,
    planErrorStatus,
    portfolioLoadingCards,
    planLoadingCards,
    isPortfolioForbidden,
    isPlanForbidden,
    isPlanNotFound,
    currentPlanRevision,
    mutationMessage,
    mutationError,
    mutating,
    initPortfolio,
    initPlan,
    refreshPortfolioCard,
    refreshPlanCard,
    transitionMilestone,
    saveCapacity,
    transitionRisk,
    transitionDependency,
    counterSignDependency,
    acceptAiSuggestion,
    dismissAiSuggestion,
    clearMutationFeedback,
    reset,
  };
});
