import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { ApiError } from '@/shared/api/client';
import { DEFAULT_RUN_QUERY, DEFAULT_SKILL_FILTERS } from '../constants';
import { aiCenterApi } from '../api/aiCenterApi';
import type {
  LoadableState,
  MetricsSummary,
  Page,
  Run,
  RunDetail,
  RunQuery,
  Skill,
  SkillDetail,
  SkillFilterState,
  StageCoverage,
} from '../types';

function createLoadable<T>(): LoadableState<T> {
  return {
    data: null,
    loading: false,
    error: null,
  };
}

function toUserMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError && error.message) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

export const useAiCenterStore = defineStore('aiCenter', () => {
  const workspaceId = ref<string | null>(null);

  const metrics = ref<LoadableState<MetricsSummary>>(createLoadable());
  const stageCoverage = ref<LoadableState<StageCoverage>>(createLoadable());
  const skills = ref<LoadableState<Skill[]>>(createLoadable());
  const runs = ref<LoadableState<Page<Run>>>(createLoadable());
  const skillDetail = ref<LoadableState<SkillDetail>>(createLoadable());
  const runDetail = ref<LoadableState<RunDetail>>(createLoadable());

  const skillDetailCache = ref<Record<string, SkillDetail>>({});
  const runDetailCache = ref<Record<string, RunDetail>>({});

  const skillFilters = ref<SkillFilterState>({ ...DEFAULT_SKILL_FILTERS });
  const runFilters = ref<RunQuery>({ ...DEFAULT_RUN_QUERY });
  const loadedRunPages = ref(1);

  const hasGlobalError = computed(() =>
    Boolean(
      metrics.value.error &&
      stageCoverage.value.error &&
      skills.value.error &&
      runs.value.error,
    ),
  );

  async function init(nextWorkspaceId: string) {
    if (!nextWorkspaceId) {
      return;
    }

    if (workspaceId.value && workspaceId.value !== nextWorkspaceId) {
      clearCache();
    }

    workspaceId.value = nextWorkspaceId;
    loadedRunPages.value = 1;
    runFilters.value = { ...DEFAULT_RUN_QUERY };

    await Promise.all([
      refetchMetrics(),
      refetchStageCoverage(),
      refetchSkills(),
      refetchRuns({ resetPage: true }),
    ]);
  }

  async function refetchMetrics() {
    if (!workspaceId.value) return;
    metrics.value.loading = true;
    metrics.value.error = null;
    try {
      metrics.value.data = await aiCenterApi.getMetrics(workspaceId.value);
    } catch (error) {
      metrics.value.data = null;
      metrics.value.error = toUserMessage(error, 'Failed to load metrics');
    } finally {
      metrics.value.loading = false;
    }
  }

  async function refetchStageCoverage() {
    if (!workspaceId.value) return;
    stageCoverage.value.loading = true;
    stageCoverage.value.error = null;
    try {
      stageCoverage.value.data = await aiCenterApi.getStageCoverage(workspaceId.value);
    } catch (error) {
      stageCoverage.value.data = null;
      stageCoverage.value.error = toUserMessage(error, 'Failed to load stage coverage');
    } finally {
      stageCoverage.value.loading = false;
    }
  }

  async function refetchSkills() {
    if (!workspaceId.value) return;
    skills.value.loading = true;
    skills.value.error = null;
    try {
      skills.value.data = await aiCenterApi.getSkills(workspaceId.value);
    } catch (error) {
      skills.value.data = null;
      skills.value.error = toUserMessage(error, 'Failed to load skill catalog');
    } finally {
      skills.value.loading = false;
    }
  }

  async function fetchRunsPage(page: number) {
    if (!workspaceId.value) {
      throw new Error('Workspace is required');
    }
    return aiCenterApi.getRuns(workspaceId.value, {
      ...runFilters.value,
      page,
      size: runFilters.value.size ?? DEFAULT_RUN_QUERY.size,
    });
  }

  async function refetchRuns(options: { resetPage?: boolean } = {}) {
    if (!workspaceId.value) return;
    runs.value.loading = true;
    runs.value.error = null;

    if (options.resetPage) {
      loadedRunPages.value = 1;
      runFilters.value = {
        ...runFilters.value,
        page: 1,
        size: runFilters.value.size ?? DEFAULT_RUN_QUERY.size,
      };
    }

    try {
      const pageCount = options.resetPage ? 1 : loadedRunPages.value;
      const pages = await Promise.all(
        Array.from({ length: pageCount }, (_, index) => fetchRunsPage(index + 1)),
      );
      const latest = pages.at(-1)!;
      runs.value.data = {
        ...latest,
        items: pages.flatMap(entry => entry.items),
        page: pageCount,
      };
    } catch (error) {
      runs.value.data = null;
      runs.value.error = toUserMessage(error, 'Failed to load run history');
    } finally {
      runs.value.loading = false;
    }
  }

  async function loadMoreRuns() {
    if (!workspaceId.value || runs.value.loading || !runs.value.data?.hasMore) {
      return;
    }

    runs.value.loading = true;
    runs.value.error = null;
    const nextPage = loadedRunPages.value + 1;

    try {
      const response = await fetchRunsPage(nextPage);
      loadedRunPages.value = nextPage;
      runs.value.data = {
        ...response,
        items: [...(runs.value.data?.items ?? []), ...response.items],
        page: nextPage,
      };
    } catch (error) {
      runs.value.error = toUserMessage(error, 'Failed to load more runs');
    } finally {
      runs.value.loading = false;
    }
  }

  async function selectSkill(skillKey: string) {
    if (!workspaceId.value) return;
    skillDetail.value.loading = true;
    skillDetail.value.error = null;

    const cached = skillDetailCache.value[skillKey];
    if (cached) {
      skillDetail.value.data = cached;
      skillDetail.value.loading = false;
      return;
    }

    try {
      const detail = await aiCenterApi.getSkillDetail(workspaceId.value, skillKey);
      skillDetailCache.value = { ...skillDetailCache.value, [skillKey]: detail };
      skillDetail.value.data = detail;
    } catch (error) {
      skillDetail.value.data = null;
      skillDetail.value.error = toUserMessage(error, 'Failed to load skill detail');
    } finally {
      skillDetail.value.loading = false;
    }
  }

  async function selectRun(executionId: string) {
    if (!workspaceId.value) return;
    runDetail.value.loading = true;
    runDetail.value.error = null;

    const cached = runDetailCache.value[executionId];
    if (cached) {
      runDetail.value.data = cached;
      runDetail.value.loading = false;
      return;
    }

    try {
      const detail = await aiCenterApi.getRunDetail(workspaceId.value, executionId);
      runDetailCache.value = { ...runDetailCache.value, [executionId]: detail };
      runDetail.value.data = detail;
    } catch (error) {
      runDetail.value.data = null;
      runDetail.value.error = toUserMessage(error, 'Failed to load run detail');
    } finally {
      runDetail.value.loading = false;
    }
  }

  async function setRunFilters(patch: RunQuery) {
    runFilters.value = {
      ...DEFAULT_RUN_QUERY,
      ...runFilters.value,
      ...patch,
      page: 1,
    };
    await refetchRuns({ resetPage: true });
  }

  function setSkillFilters(patch: Partial<SkillFilterState>) {
    skillFilters.value = {
      ...skillFilters.value,
      ...patch,
    };
  }

  function clearSelectedSkill() {
    skillDetail.value = createLoadable();
  }

  function clearSelectedRun() {
    runDetail.value = createLoadable();
  }

  function clearCache() {
    metrics.value = createLoadable();
    stageCoverage.value = createLoadable();
    skills.value = createLoadable();
    runs.value = createLoadable();
    skillDetail.value = createLoadable();
    runDetail.value = createLoadable();
    skillDetailCache.value = {};
    runDetailCache.value = {};
    skillFilters.value = { ...DEFAULT_SKILL_FILTERS };
    runFilters.value = { ...DEFAULT_RUN_QUERY };
    loadedRunPages.value = 1;
  }

  function reset() {
    workspaceId.value = null;
    clearCache();
  }

  return {
    workspaceId,
    metrics,
    stageCoverage,
    skills,
    runs,
    skillDetail,
    runDetail,
    skillFilters,
    runFilters,
    hasGlobalError,
    init,
    refetchMetrics,
    refetchStageCoverage,
    refetchSkills,
    refetchRuns,
    loadMoreRuns,
    selectSkill,
    selectRun,
    setRunFilters,
    setSkillFilters,
    clearSelectedSkill,
    clearSelectedRun,
    reset,
  };
});
