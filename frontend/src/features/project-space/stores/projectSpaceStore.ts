import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { ApiError } from '@/shared/api/client';
import { projectSpaceApi } from '../api/projectSpaceApi';
import {
  PROJECT_SPACE_CARD_KEYS,
  createLoadingCardState,
  type ProjectSpaceAggregate,
  type ProjectSpaceCardKey,
} from '../types/aggregate';
import type { ProjectSummary } from '../types/summary';

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

export const useProjectSpaceStore = defineStore('projectSpace', () => {
  const projectId = ref<string | null>(null);
  const workspaceId = ref<string | null>(null);
  const aggregate = ref<ProjectSpaceAggregate | null>(null);
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  const errorStatus = ref<number | null>(null);
  const loadingCards = ref(createLoadingCardState());

  const isForbidden = computed(() => errorStatus.value === 403);
  const isNotFound = computed(() => errorStatus.value === 404);

  async function initProject(nextProjectId: string) {
    projectId.value = nextProjectId;
    isLoading.value = true;
    error.value = null;
    errorStatus.value = null;
    loadingCards.value = createLoadingCardState();

    try {
      aggregate.value = await projectSpaceApi.getAggregate(nextProjectId);
      workspaceId.value = aggregate.value.workspaceId;
      return;
    } catch (err) {
      aggregate.value = null;
      error.value = toUserMessage(err, 'Failed to load Project Space');
      errorStatus.value = toStatus(err);

      // Fall back to per-card hydration for server-side aggregate failures only.
      if (errorStatus.value == null || errorStatus.value >= 500) {
        const fallback = await hydrateBySections(nextProjectId);
        if (fallback) {
          aggregate.value = fallback;
          workspaceId.value = fallback.workspaceId;
          error.value = null;
          errorStatus.value = null;
        }
      }
    } finally {
      isLoading.value = false;
    }
  }

  async function hydrateBySections(nextProjectId: string): Promise<ProjectSpaceAggregate | null> {
    const results = await Promise.all(
      PROJECT_SPACE_CARD_KEYS.map(async cardKey => {
        try {
          return [cardKey, await projectSpaceApi.getSection(cardKey, nextProjectId)] as const;
        } catch (err) {
          return [cardKey, { data: null, error: toUserMessage(err, `Failed to load ${cardKey}`) }] as const;
        }
      }),
    );

    const sectionMap = Object.fromEntries(results) as Omit<ProjectSpaceAggregate, 'projectId' | 'workspaceId'>;
    const derivedWorkspaceId = sectionMap.summary.data?.workspaceId ?? workspaceId.value ?? null;
    const allFailed = Object.values(sectionMap).every(section => section.error);

    if (allFailed || !derivedWorkspaceId) {
      return null;
    }

    return {
      projectId: nextProjectId,
      workspaceId: derivedWorkspaceId,
      ...sectionMap,
    };
  }

  async function switchProject(nextProjectId: string) {
    if (projectId.value === nextProjectId && aggregate.value) {
      return;
    }
    await initProject(nextProjectId);
  }

  async function refreshCard(cardKey: ProjectSpaceCardKey) {
    if (!projectId.value || !aggregate.value) {
      return;
    }

    loadingCards.value[cardKey] = true;
    try {
      const section = await projectSpaceApi.getSection(cardKey, projectId.value);
      const summarySection = cardKey === 'summary' ? (section as { data: ProjectSummary | null }).data : null;
      aggregate.value = {
        ...aggregate.value,
        ...(summarySection
          ? {
              workspaceId: summarySection.workspaceId,
            }
          : {}),
        [cardKey]: section,
      };
      if (summarySection) {
        workspaceId.value = summarySection.workspaceId;
      }
    } catch (err) {
      aggregate.value = {
        ...aggregate.value,
        [cardKey]: { data: null, error: toUserMessage(err, `Failed to refresh ${cardKey}`) },
      };
    } finally {
      loadingCards.value[cardKey] = false;
    }
  }

  async function retryCard(cardKey: ProjectSpaceCardKey) {
    await refreshCard(cardKey);
  }

  function reset() {
    projectId.value = null;
    workspaceId.value = null;
    aggregate.value = null;
    isLoading.value = false;
    error.value = null;
    errorStatus.value = null;
    loadingCards.value = createLoadingCardState();
  }

  return {
    projectId,
    workspaceId,
    aggregate,
    isLoading,
    error,
    errorStatus,
    isForbidden,
    isNotFound,
    loadingCards,
    initProject,
    switchProject,
    refreshCard,
    retryCard,
    reset,
  };
});
