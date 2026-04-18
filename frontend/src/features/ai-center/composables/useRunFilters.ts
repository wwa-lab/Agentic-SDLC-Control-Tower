import { reactive, watch, computed } from 'vue';
import { DEFAULT_RUN_FILTER_FORM } from '../constants';
import type { RunFilterFormState, RunQuery } from '../types';

function toFormState(query: RunQuery): RunFilterFormState {
  return {
    skillKey: [...(query.skillKey ?? [])],
    status: [...(query.status ?? [])],
    triggerSourcePage: query.triggerSourcePage ?? '',
    startedAfter: query.startedAfter ?? '',
    startedBefore: query.startedBefore ?? '',
    triggeredByType: query.triggeredByType ?? '',
  };
}

function toQuery(form: RunFilterFormState): RunQuery {
  return {
    skillKey: [...form.skillKey],
    status: [...form.status],
    triggerSourcePage: form.triggerSourcePage || undefined,
    startedAfter: form.startedAfter || undefined,
    startedBefore: form.startedBefore || undefined,
    triggeredByType: form.triggeredByType || undefined,
  };
}

export function useRunFilters(initialQuery: RunQuery, emitChange: (query: RunQuery) => void) {
  const form = reactive<RunFilterFormState>(toFormState(initialQuery));

  watch(
    () => initialQuery,
    next => {
      Object.assign(form, toFormState(next));
    },
    { deep: true },
  );

  function applyFilters() {
    emitChange(toQuery(form));
  }

  function resetFilters() {
    Object.assign(form, DEFAULT_RUN_FILTER_FORM);
    emitChange({});
  }

  const hasActiveFilters = computed(() =>
    Boolean(
      form.skillKey.length ||
      form.status.length ||
      form.triggerSourcePage ||
      form.startedAfter ||
      form.startedBefore ||
      form.triggeredByType,
    ),
  );

  return {
    form,
    hasActiveFilters,
    applyFilters,
    resetFilters,
  };
}
