import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { codeBuildApi } from '../api/codeBuildApi';
import { CodeBuildApiError, isCodeBuildApiError } from '../errors';
import {
  CB_DEFAULT_AUTONOMY,
  CB_DEFAULT_CATALOG_FILTER,
  CB_DEFAULT_TRACEABILITY_FILTER,
  CB_DEFAULT_WORKSPACE_ID,
  CB_DEFAULT_ROLE,
  CODE_BUILD_CATALOG_CARD_KEYS,
  CODE_BUILD_PR_CARD_KEYS,
  CODE_BUILD_REPO_CARD_KEYS,
  CODE_BUILD_RUN_CARD_KEYS,
  CODE_BUILD_TRACEABILITY_CARD_KEYS,
  type CatalogAggregate,
  type CatalogCardKey,
  type CatalogFilter,
  type CodeBuildViewerContext,
  type PrCardKey,
  type PrDetailAggregate,
  type RegenerateAiPrReviewRequest,
  type RegenerateAiTriageRequest,
  type RepoCardKey,
  type RepoDetailAggregate,
  type RerunRunRequest,
  type RunCardKey,
  type RunDetailAggregate,
  type TraceabilityAggregate,
  type TraceabilityCardKey,
  type TraceabilityFilter,
} from '../types';
import { canManageRole, toUserMessage } from '../utils';

function createLoadingState<K extends string>(keys: readonly K[]): Record<K, boolean> {
  return Object.fromEntries(keys.map(key => [key, false])) as Record<K, boolean>;
}

function toStatus(error: unknown) {
  return error instanceof CodeBuildApiError ? error.status : null;
}

export const useCodeBuildStore = defineStore('codeBuild', () => {
  const viewerContext = ref<CodeBuildViewerContext>({
    workspaceId: CB_DEFAULT_WORKSPACE_ID,
    role: CB_DEFAULT_ROLE,
    autonomyLevel: CB_DEFAULT_AUTONOMY,
  });

  const catalogFilters = ref<CatalogFilter>({ ...CB_DEFAULT_CATALOG_FILTER });
  const traceabilityFilters = ref<TraceabilityFilter>({ ...CB_DEFAULT_TRACEABILITY_FILTER });

  const repoId = ref<string | null>(null);
  const prId = ref<string | null>(null);
  const runId = ref<string | null>(null);

  const catalog = ref<CatalogAggregate | null>(null);
  const repo = ref<RepoDetailAggregate | null>(null);
  const pr = ref<PrDetailAggregate | null>(null);
  const run = ref<RunDetailAggregate | null>(null);
  const traceability = ref<TraceabilityAggregate | null>(null);

  const catalogLoading = ref(false);
  const repoLoading = ref(false);
  const prLoading = ref(false);
  const runLoading = ref(false);
  const traceabilityLoading = ref(false);

  const catalogLoadingCards = ref(createLoadingState(CODE_BUILD_CATALOG_CARD_KEYS));
  const repoLoadingCards = ref(createLoadingState(CODE_BUILD_REPO_CARD_KEYS));
  const prLoadingCards = ref(createLoadingState(CODE_BUILD_PR_CARD_KEYS));
  const runLoadingCards = ref(createLoadingState(CODE_BUILD_RUN_CARD_KEYS));
  const traceabilityLoadingCards = ref(createLoadingState(CODE_BUILD_TRACEABILITY_CARD_KEYS));

  const catalogError = ref<string | null>(null);
  const repoError = ref<string | null>(null);
  const prError = ref<string | null>(null);
  const runError = ref<string | null>(null);
  const traceabilityError = ref<string | null>(null);

  const catalogErrorStatus = ref<number | null>(null);
  const repoErrorStatus = ref<number | null>(null);
  const prErrorStatus = ref<number | null>(null);
  const runErrorStatus = ref<number | null>(null);
  const traceabilityErrorStatus = ref<number | null>(null);

  const mutationMessage = ref<string | null>(null);
  const mutationError = ref<string | null>(null);
  const mutationCode = ref<string | null>(null);
  const rateLimitResetAt = ref<string | null>(null);
  const mutating = ref(false);

  const canManage = computed(() => canManageRole(viewerContext.value.role));
  const canSeeBlockerBodies = computed(() => canManage.value);
  const currentPrHeadSha = computed(() => pr.value?.header.data?.headSha ?? null);

  const isRepoForbidden = computed(() => repoErrorStatus.value === 403);
  const isRepoNotFound = computed(() => repoErrorStatus.value === 404);
  const isPrForbidden = computed(() => prErrorStatus.value === 403);
  const isPrNotFound = computed(() => prErrorStatus.value === 404);
  const isRunForbidden = computed(() => runErrorStatus.value === 403);
  const isRunNotFound = computed(() => runErrorStatus.value === 404);

  function setViewerContext(next: Partial<CodeBuildViewerContext>) {
    viewerContext.value = {
      ...viewerContext.value,
      ...next,
    };
  }

  function clearMutationFeedback() {
    mutationMessage.value = null;
    mutationError.value = null;
    mutationCode.value = null;
    rateLimitResetAt.value = null;
  }

  function handleMutationFailure(error: unknown, fallback: string) {
    mutationError.value = toUserMessage(error, fallback);
    mutationCode.value = isCodeBuildApiError(error) ? error.code : null;
    rateLimitResetAt.value = isCodeBuildApiError(error) ? error.details.resetAt ?? null : null;
  }

  async function initCatalog(nextFilters?: Partial<CatalogFilter>) {
    catalogFilters.value = { ...catalogFilters.value, ...nextFilters };
    catalogLoading.value = true;
    catalogError.value = null;
    catalogErrorStatus.value = null;
    try {
      catalog.value = await codeBuildApi.getCatalogAggregate(viewerContext.value, catalogFilters.value);
    } catch (error) {
      catalog.value = null;
      catalogError.value = toUserMessage(error, 'Failed to load Code & Build catalog.');
      catalogErrorStatus.value = toStatus(error);
    } finally {
      catalogLoading.value = false;
    }
  }

  async function refreshCatalogCard(cardKey: CatalogCardKey) {
    if (!catalog.value) {
      return;
    }
    catalogLoadingCards.value[cardKey] = true;
    try {
      const section = await codeBuildApi.getCatalogSection(cardKey, viewerContext.value, catalogFilters.value);
      catalog.value = {
        ...catalog.value,
        [cardKey]: section,
      } as CatalogAggregate;
    } catch (error) {
      catalog.value = {
        ...catalog.value,
        [cardKey]: {
          data: null,
          error: toUserMessage(error, `Failed to refresh ${cardKey}.`),
        },
      } as CatalogAggregate;
    } finally {
      catalogLoadingCards.value[cardKey] = false;
    }
  }

  async function openRepo(nextRepoId: string) {
    repoId.value = nextRepoId;
    repoLoading.value = true;
    repoError.value = null;
    repoErrorStatus.value = null;
    try {
      repo.value = await codeBuildApi.getRepoAggregate(nextRepoId, viewerContext.value);
    } catch (error) {
      repo.value = null;
      repoError.value = toUserMessage(error, 'Failed to load repository detail.');
      repoErrorStatus.value = toStatus(error);
    } finally {
      repoLoading.value = false;
    }
  }

  async function refreshRepoCard(cardKey: RepoCardKey) {
    if (!repoId.value || !repo.value) {
      return;
    }
    repoLoadingCards.value[cardKey] = true;
    try {
      const section = await codeBuildApi.getRepoSection(cardKey, repoId.value, viewerContext.value);
      repo.value = {
        ...repo.value,
        [cardKey]: section,
      } as RepoDetailAggregate;
    } catch (error) {
      repo.value = {
        ...repo.value,
        [cardKey]: {
          data: null,
          error: toUserMessage(error, `Failed to refresh ${cardKey}.`),
        },
      } as RepoDetailAggregate;
    } finally {
      repoLoadingCards.value[cardKey] = false;
    }
  }

  async function openPr(nextPrId: string) {
    prId.value = nextPrId;
    prLoading.value = true;
    prError.value = null;
    prErrorStatus.value = null;
    try {
      pr.value = await codeBuildApi.getPrAggregate(nextPrId, viewerContext.value);
    } catch (error) {
      pr.value = null;
      prError.value = toUserMessage(error, 'Failed to load pull request detail.');
      prErrorStatus.value = toStatus(error);
    } finally {
      prLoading.value = false;
    }
  }

  async function refreshPrCard(cardKey: PrCardKey) {
    if (!prId.value || !pr.value) {
      return;
    }
    prLoadingCards.value[cardKey] = true;
    try {
      const section = await codeBuildApi.getPrSection(cardKey, prId.value, viewerContext.value);
      pr.value = {
        ...pr.value,
        [cardKey]: section,
      } as PrDetailAggregate;
    } catch (error) {
      pr.value = {
        ...pr.value,
        [cardKey]: {
          data: null,
          error: toUserMessage(error, `Failed to refresh ${cardKey}.`),
        },
      } as PrDetailAggregate;
    } finally {
      prLoadingCards.value[cardKey] = false;
    }
  }

  async function openRun(nextRunId: string) {
    runId.value = nextRunId;
    runLoading.value = true;
    runError.value = null;
    runErrorStatus.value = null;
    try {
      run.value = await codeBuildApi.getRunAggregate(nextRunId, viewerContext.value);
    } catch (error) {
      run.value = null;
      runError.value = toUserMessage(error, 'Failed to load run detail.');
      runErrorStatus.value = toStatus(error);
    } finally {
      runLoading.value = false;
    }
  }

  async function refreshRunCard(cardKey: RunCardKey) {
    if (!runId.value || !run.value) {
      return;
    }
    runLoadingCards.value[cardKey] = true;
    try {
      const section = await codeBuildApi.getRunSection(cardKey, runId.value, viewerContext.value);
      run.value = {
        ...run.value,
        [cardKey]: section,
      } as RunDetailAggregate;
    } catch (error) {
      run.value = {
        ...run.value,
        [cardKey]: {
          data: null,
          error: toUserMessage(error, `Failed to refresh ${cardKey}.`),
        },
      } as RunDetailAggregate;
    } finally {
      runLoadingCards.value[cardKey] = false;
    }
  }

  async function initTraceability(nextFilters?: Partial<TraceabilityFilter>) {
    traceabilityFilters.value = {
      ...traceabilityFilters.value,
      ...nextFilters,
    };
    traceabilityLoading.value = true;
    traceabilityError.value = null;
    traceabilityErrorStatus.value = null;
    try {
      traceability.value = await codeBuildApi.getTraceabilityAggregate(viewerContext.value, traceabilityFilters.value);
    } catch (error) {
      traceability.value = null;
      traceabilityError.value = toUserMessage(error, 'Failed to load traceability.');
      traceabilityErrorStatus.value = toStatus(error);
    } finally {
      traceabilityLoading.value = false;
    }
  }

  async function refreshTraceabilityCard(cardKey: TraceabilityCardKey) {
    if (!traceability.value) {
      return;
    }
    traceabilityLoadingCards.value[cardKey] = true;
    try {
      const section = await codeBuildApi.getTraceabilitySection(cardKey, viewerContext.value, traceabilityFilters.value);
      traceability.value = {
        ...traceability.value,
        [cardKey]: section,
      } as TraceabilityAggregate;
    } catch (error) {
      traceability.value = {
        ...traceability.value,
        [cardKey]: {
          data: null,
          error: toUserMessage(error, `Failed to refresh ${cardKey}.`),
        },
      } as TraceabilityAggregate;
    } finally {
      traceabilityLoadingCards.value[cardKey] = false;
    }
  }

  async function regenerateAiPrReview(nextPrId: string, request: RegenerateAiPrReviewRequest) {
    mutating.value = true;
    clearMutationFeedback();
    try {
      const response = await codeBuildApi.regenerateAiPrReview(nextPrId, request, viewerContext.value);
      if (pr.value) {
        pr.value = {
          ...pr.value,
          aiReview: response.aiReview,
        } as PrDetailAggregate;
      }
      mutationMessage.value = response.message;
    } catch (error) {
      if (isCodeBuildApiError(error) && error.code === 'CB_STALE_HEAD_SHA') {
        await openPr(nextPrId);
        mutationMessage.value = 'PR head changed. The latest aggregate was reloaded.';
      } else {
        handleMutationFailure(error, 'AI PR review regeneration failed.');
      }
    } finally {
      mutating.value = false;
    }
  }

  async function regenerateAiTriage(nextRunId: string, request: RegenerateAiTriageRequest) {
    mutating.value = true;
    clearMutationFeedback();
    try {
      const response = await codeBuildApi.regenerateAiTriage(nextRunId, request, viewerContext.value);
      if (run.value) {
        run.value = {
          ...run.value,
          aiTriage: response.aiTriage,
        } as RunDetailAggregate;
      }
      mutationMessage.value = response.message;
      mutationCode.value = response.errorCode ?? null;
    } catch (error) {
      handleMutationFailure(error, 'AI triage regeneration failed.');
    } finally {
      mutating.value = false;
    }
  }

  async function rerunRun(nextRunId: string, request: RerunRunRequest) {
    mutating.value = true;
    clearMutationFeedback();
    try {
      const response = await codeBuildApi.rerunRun(nextRunId, request, viewerContext.value);
      if (run.value) {
        run.value = {
          ...run.value,
          rerun: response.rerun,
        } as RunDetailAggregate;
      }
      mutationMessage.value = response.message;
    } catch (error) {
      handleMutationFailure(error, 'Workflow rerun failed.');
    } finally {
      mutating.value = false;
    }
  }

  function closeRepo() {
    repoId.value = null;
    repo.value = null;
    repoError.value = null;
    repoErrorStatus.value = null;
  }

  function closePr() {
    prId.value = null;
    pr.value = null;
    prError.value = null;
    prErrorStatus.value = null;
  }

  function closeRun() {
    runId.value = null;
    run.value = null;
    runError.value = null;
    runErrorStatus.value = null;
  }

  function reset() {
    viewerContext.value = {
      workspaceId: CB_DEFAULT_WORKSPACE_ID,
      role: CB_DEFAULT_ROLE,
      autonomyLevel: CB_DEFAULT_AUTONOMY,
    };
    catalogFilters.value = { ...CB_DEFAULT_CATALOG_FILTER };
    traceabilityFilters.value = { ...CB_DEFAULT_TRACEABILITY_FILTER };
    repoId.value = null;
    prId.value = null;
    runId.value = null;
    catalog.value = null;
    repo.value = null;
    pr.value = null;
    run.value = null;
    traceability.value = null;
    catalogLoading.value = false;
    repoLoading.value = false;
    prLoading.value = false;
    runLoading.value = false;
    traceabilityLoading.value = false;
    catalogLoadingCards.value = createLoadingState(CODE_BUILD_CATALOG_CARD_KEYS);
    repoLoadingCards.value = createLoadingState(CODE_BUILD_REPO_CARD_KEYS);
    prLoadingCards.value = createLoadingState(CODE_BUILD_PR_CARD_KEYS);
    runLoadingCards.value = createLoadingState(CODE_BUILD_RUN_CARD_KEYS);
    traceabilityLoadingCards.value = createLoadingState(CODE_BUILD_TRACEABILITY_CARD_KEYS);
    catalogError.value = null;
    repoError.value = null;
    prError.value = null;
    runError.value = null;
    traceabilityError.value = null;
    catalogErrorStatus.value = null;
    repoErrorStatus.value = null;
    prErrorStatus.value = null;
    runErrorStatus.value = null;
    traceabilityErrorStatus.value = null;
    clearMutationFeedback();
    mutating.value = false;
  }

  return {
    viewerContext,
    catalogFilters,
    traceabilityFilters,
    repoId,
    prId,
    runId,
    catalog,
    repo,
    pr,
    run,
    traceability,
    catalogLoading,
    repoLoading,
    prLoading,
    runLoading,
    traceabilityLoading,
    catalogLoadingCards,
    repoLoadingCards,
    prLoadingCards,
    runLoadingCards,
    traceabilityLoadingCards,
    catalogError,
    repoError,
    prError,
    runError,
    traceabilityError,
    catalogErrorStatus,
    repoErrorStatus,
    prErrorStatus,
    runErrorStatus,
    traceabilityErrorStatus,
    mutationMessage,
    mutationError,
    mutationCode,
    rateLimitResetAt,
    mutating,
    canManage,
    canSeeBlockerBodies,
    currentPrHeadSha,
    isRepoForbidden,
    isRepoNotFound,
    isPrForbidden,
    isPrNotFound,
    isRunForbidden,
    isRunNotFound,
    setViewerContext,
    initCatalog,
    refreshCatalogCard,
    openRepo,
    refreshRepoCard,
    closeRepo,
    openPr,
    refreshPrCard,
    closePr,
    openRun,
    refreshRunCard,
    closeRun,
    initTraceability,
    refreshTraceabilityCard,
    regenerateAiPrReview,
    regenerateAiTriage,
    rerunRun,
    clearMutationFeedback,
    reset,
  };
});

