import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { testingManagementApi } from '../api/testingManagementApi';
import {
  TM_CASE_CARD_KEYS,
  TM_CATALOG_CARD_KEYS,
  TM_DEFAULT_CATALOG_FILTER,
  TM_DEFAULT_WORKSPACE_ID,
  TM_PLAN_CARD_KEYS,
  TM_RUN_CARD_KEYS,
  TM_TRACEABILITY_CARD_KEYS,
  type CaseCardKey,
  type CaseDetailAggregate,
  type CatalogAggregate,
  type CatalogCardKey,
  type CatalogFilter,
  type PlanCardKey,
  type PlanDetailAggregate,
  type RunCardKey,
  type RunDetailAggregate,
  type TraceabilityAggregate,
  type TraceabilityCardKey,
} from '../types';
import { toUserMessage } from '../utils';

function createLoadingState<K extends string>(keys: readonly K[]): Record<K, boolean> {
  return Object.fromEntries(keys.map(key => [key, false])) as Record<K, boolean>;
}

export const useTestingStore = defineStore('testingManagement', () => {
  const workspaceId = ref(TM_DEFAULT_WORKSPACE_ID);
  const catalogFilters = ref<CatalogFilter>({ ...TM_DEFAULT_CATALOG_FILTER });
  const traceabilitySearch = ref('');

  const planId = ref<string | null>(null);
  const caseId = ref<string | null>(null);
  const runId = ref<string | null>(null);

  const catalog = ref<CatalogAggregate | null>(null);
  const plan = ref<PlanDetailAggregate | null>(null);
  const caseAggregate = ref<CaseDetailAggregate | null>(null);
  const run = ref<RunDetailAggregate | null>(null);
  const traceability = ref<TraceabilityAggregate | null>(null);

  const catalogLoading = ref(false);
  const planLoading = ref(false);
  const caseLoading = ref(false);
  const runLoading = ref(false);
  const traceabilityLoading = ref(false);

  const catalogLoadingCards = ref(createLoadingState(TM_CATALOG_CARD_KEYS));
  const planLoadingCards = ref(createLoadingState(TM_PLAN_CARD_KEYS));
  const caseLoadingCards = ref(createLoadingState(TM_CASE_CARD_KEYS));
  const runLoadingCards = ref(createLoadingState(TM_RUN_CARD_KEYS));
  const traceabilityLoadingCards = ref(createLoadingState(TM_TRACEABILITY_CARD_KEYS));

  const catalogError = ref<string | null>(null);
  const planError = ref<string | null>(null);
  const caseError = ref<string | null>(null);
  const runError = ref<string | null>(null);
  const traceabilityError = ref<string | null>(null);

  const filteredTraceabilityRows = computed(() => {
    const rows = traceability.value?.reqRows.data ?? [];
    const needle = traceabilitySearch.value.trim().toLowerCase();
    if (!needle) {
      return rows;
    }
    return rows.filter(row =>
      [row.reqId, row.reqTitle, row.projectName, row.storyId ?? '']
        .join(' ')
        .toLowerCase()
        .includes(needle),
    );
  });

  function setWorkspaceId(nextWorkspaceId: string) {
    workspaceId.value = nextWorkspaceId || TM_DEFAULT_WORKSPACE_ID;
  }

  function setTraceabilitySearch(nextSearch: string) {
    traceabilitySearch.value = nextSearch;
  }

  async function initCatalog(nextFilters?: Partial<CatalogFilter>) {
    catalogFilters.value = { ...catalogFilters.value, ...nextFilters };
    catalogLoading.value = true;
    catalogError.value = null;
    try {
      catalog.value = await testingManagementApi.getCatalogAggregate(workspaceId.value, catalogFilters.value);
    } catch (error) {
      catalog.value = null;
      catalogError.value = toUserMessage(error, 'Failed to load Testing catalog.');
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
      const section = await testingManagementApi.getCatalogSection(cardKey, workspaceId.value, catalogFilters.value);
      catalog.value = {
        ...catalog.value,
        [cardKey]: section,
      } as CatalogAggregate;
    } catch (error) {
      catalog.value = {
        ...catalog.value,
        [cardKey]: { data: null, error: toUserMessage(error, `Failed to refresh ${cardKey}.`) },
      } as CatalogAggregate;
    } finally {
      catalogLoadingCards.value[cardKey] = false;
    }
  }

  async function openPlan(nextPlanId: string) {
    planId.value = nextPlanId;
    planLoading.value = true;
    planError.value = null;
    try {
      plan.value = await testingManagementApi.getPlanAggregate(nextPlanId);
    } catch (error) {
      plan.value = null;
      planError.value = toUserMessage(error, 'Failed to load test plan detail.');
    } finally {
      planLoading.value = false;
    }
  }

  async function refreshPlanCard(cardKey: PlanCardKey) {
    if (!plan.value || !planId.value) {
      return;
    }
    planLoadingCards.value[cardKey] = true;
    try {
      const section = await testingManagementApi.getPlanSection(cardKey, planId.value);
      plan.value = {
        ...plan.value,
        [cardKey]: section,
      } as PlanDetailAggregate;
    } catch (error) {
      plan.value = {
        ...plan.value,
        [cardKey]: { data: null, error: toUserMessage(error, `Failed to refresh ${cardKey}.`) },
      } as PlanDetailAggregate;
    } finally {
      planLoadingCards.value[cardKey] = false;
    }
  }

  async function openCase(nextCaseId: string) {
    caseId.value = nextCaseId;
    caseLoading.value = true;
    caseError.value = null;
    try {
      caseAggregate.value = await testingManagementApi.getCaseAggregate(nextCaseId);
    } catch (error) {
      caseAggregate.value = null;
      caseError.value = toUserMessage(error, 'Failed to load test case detail.');
    } finally {
      caseLoading.value = false;
    }
  }

  async function refreshCaseCard(cardKey: CaseCardKey) {
    if (!caseAggregate.value || !caseId.value) {
      return;
    }
    caseLoadingCards.value[cardKey] = true;
    try {
      const section = await testingManagementApi.getCaseSection(cardKey, caseId.value);
      caseAggregate.value = {
        ...caseAggregate.value,
        [cardKey]: section,
      } as CaseDetailAggregate;
    } catch (error) {
      caseAggregate.value = {
        ...caseAggregate.value,
        [cardKey]: { data: null, error: toUserMessage(error, `Failed to refresh ${cardKey}.`) },
      } as CaseDetailAggregate;
    } finally {
      caseLoadingCards.value[cardKey] = false;
    }
  }

  async function openRun(nextRunId: string) {
    runId.value = nextRunId;
    runLoading.value = true;
    runError.value = null;
    try {
      run.value = await testingManagementApi.getRunAggregate(nextRunId);
    } catch (error) {
      run.value = null;
      runError.value = toUserMessage(error, 'Failed to load test run detail.');
    } finally {
      runLoading.value = false;
    }
  }

  async function refreshRunCard(cardKey: RunCardKey) {
    if (!run.value || !runId.value) {
      return;
    }
    runLoadingCards.value[cardKey] = true;
    try {
      const section = await testingManagementApi.getRunSection(cardKey, runId.value);
      run.value = {
        ...run.value,
        [cardKey]: section,
      } as RunDetailAggregate;
    } catch (error) {
      run.value = {
        ...run.value,
        [cardKey]: { data: null, error: toUserMessage(error, `Failed to refresh ${cardKey}.`) },
      } as RunDetailAggregate;
    } finally {
      runLoadingCards.value[cardKey] = false;
    }
  }

  async function initTraceability() {
    traceabilityLoading.value = true;
    traceabilityError.value = null;
    try {
      traceability.value = await testingManagementApi.getTraceabilityAggregate(workspaceId.value);
    } catch (error) {
      traceability.value = null;
      traceabilityError.value = toUserMessage(error, 'Failed to load traceability coverage.');
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
      const section = await testingManagementApi.getTraceabilitySection(cardKey, workspaceId.value);
      traceability.value = {
        ...traceability.value,
        [cardKey]: section,
      } as TraceabilityAggregate;
    } catch (error) {
      traceability.value = {
        ...traceability.value,
        [cardKey]: { data: null, error: toUserMessage(error, `Failed to refresh ${cardKey}.`) },
      } as TraceabilityAggregate;
    } finally {
      traceabilityLoadingCards.value[cardKey] = false;
    }
  }

  return {
    workspaceId,
    catalogFilters,
    traceabilitySearch,
    catalog,
    plan,
    caseAggregate,
    run,
    traceability,
    filteredTraceabilityRows,
    catalogLoading,
    planLoading,
    caseLoading,
    runLoading,
    traceabilityLoading,
    catalogLoadingCards,
    planLoadingCards,
    caseLoadingCards,
    runLoadingCards,
    traceabilityLoadingCards,
    catalogError,
    planError,
    caseError,
    runError,
    traceabilityError,
    setWorkspaceId,
    setTraceabilitySearch,
    initCatalog,
    refreshCatalogCard,
    openPlan,
    refreshPlanCard,
    openCase,
    refreshCaseCard,
    openRun,
    refreshRunCard,
    initTraceability,
    refreshTraceabilityCard,
  };
});
