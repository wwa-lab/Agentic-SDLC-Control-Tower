import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { ApiError } from '@/shared/api/client';
import { teamSpaceApi } from '../api/teamSpaceApi';
import {
  createLoadingCardState,
  type TeamSpaceAggregate,
  type TeamSpaceCardKey,
} from '../types/aggregate';

function toUserMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError && error.message) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

export const useTeamSpaceStore = defineStore('teamSpace', () => {
  const workspaceId = ref<string | null>(null);
  const aggregate = ref<TeamSpaceAggregate | null>(null);
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  const loadingCards = ref(createLoadingCardState());

  const isForbidden = computed(() => error.value?.toLowerCase().includes('denied') ?? false);

  async function initWorkspace(nextWorkspaceId: string) {
    workspaceId.value = nextWorkspaceId;
    isLoading.value = true;
    error.value = null;
    loadingCards.value = createLoadingCardState();

    try {
      aggregate.value = await teamSpaceApi.getAggregate(nextWorkspaceId);
    } catch (err) {
      aggregate.value = null;
      error.value = toUserMessage(err, 'Failed to load Team Space');
    } finally {
      isLoading.value = false;
    }
  }

  async function switchWorkspace(nextWorkspaceId: string) {
    if (workspaceId.value === nextWorkspaceId && aggregate.value) {
      return;
    }
    await initWorkspace(nextWorkspaceId);
  }

  async function refreshCard(cardKey: TeamSpaceCardKey) {
    if (!workspaceId.value || !aggregate.value) {
      return;
    }

    loadingCards.value[cardKey] = true;
    try {
      aggregate.value = {
        ...aggregate.value,
        [cardKey]: await teamSpaceApi.getSection(cardKey, workspaceId.value),
      };
    } catch (err) {
      const message = toUserMessage(err, `Failed to refresh ${cardKey}`);
      aggregate.value = {
        ...aggregate.value,
        [cardKey]: { data: null, error: message },
      };
    } finally {
      loadingCards.value[cardKey] = false;
    }
  }

  async function retryCard(cardKey: TeamSpaceCardKey) {
    await refreshCard(cardKey);
  }

  function reset() {
    workspaceId.value = null;
    aggregate.value = null;
    isLoading.value = false;
    error.value = null;
    loadingCards.value = createLoadingCardState();
  }

  return {
    workspaceId,
    aggregate,
    isLoading,
    error,
    isForbidden,
    loadingCards,
    initWorkspace,
    switchWorkspace,
    retryCard,
    refreshCard,
    reset,
  };
});
