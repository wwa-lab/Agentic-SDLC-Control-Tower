import { computed, ref, watch, type Ref } from 'vue';
import { DEFAULT_SKILL_FILTERS } from '../constants';
import type { Skill, SkillFilterState } from '../types';

function includesAny<T>(haystack: T[], needles: T[]): boolean {
  if (!needles.length) {
    return true;
  }
  return needles.some(value => haystack.includes(value));
}

function compareNullableDates(left: string | null, right: string | null): number {
  if (left == null && right == null) {
    return 0;
  }
  if (left == null) {
    return 1;
  }
  if (right == null) {
    return -1;
  }
  return right.localeCompare(left);
}

export function useSkillFilters(
  skills: Ref<Skill[]>,
  filters: Ref<SkillFilterState>,
  setFilters: (patch: Partial<SkillFilterState>) => void,
) {
  const debouncedSearch = ref(filters.value.search);
  let timer: ReturnType<typeof setTimeout> | null = null;

  watch(
    () => filters.value.search,
    value => {
      if (timer) {
        clearTimeout(timer);
      }
      timer = setTimeout(() => {
        debouncedSearch.value = value.trim().toLowerCase();
      }, 150);
    },
    { immediate: true },
  );

  const filteredSkills = computed(() => {
    const resolved = skills.value.filter(skill => {
      if (filters.value.category.length && !filters.value.category.includes(skill.category)) {
        return false;
      }
      if (filters.value.status.length && !filters.value.status.includes(skill.status)) {
        return false;
      }
      if (filters.value.autonomy.length && !filters.value.autonomy.includes(skill.defaultAutonomy)) {
        return false;
      }
      if (filters.value.owner.length && !filters.value.owner.includes(skill.owner)) {
        return false;
      }
      if (debouncedSearch.value) {
        const haystack = `${skill.key} ${skill.name}`.toLowerCase();
        if (!haystack.includes(debouncedSearch.value)) {
          return false;
        }
      }
      return true;
    });

    return [...resolved].sort((left, right) => {
      if (filters.value.sortBy === 'name') {
        return left.name.localeCompare(right.name);
      }
      if (filters.value.sortBy === 'successRate30d') {
        return (right.successRate30d ?? -1) - (left.successRate30d ?? -1);
      }
      return compareNullableDates(left.lastExecutedAt, right.lastExecutedAt);
    });
  });

  function toggleArrayFilter<K extends 'category' | 'status' | 'autonomy' | 'owner'>(key: K, value: SkillFilterState[K][number]) {
    const current = filters.value[key] as Array<SkillFilterState[K][number]>;
    const next = current.includes(value)
      ? current.filter(entry => entry !== value)
      : [...current, value];
    setFilters({ [key]: next } as Partial<SkillFilterState>);
  }

  function setSearch(search: string) {
    setFilters({ search });
  }

  function setSortBy(sortBy: SkillFilterState['sortBy']) {
    setFilters({ sortBy });
  }

  function resetFilters() {
    setFilters({ ...DEFAULT_SKILL_FILTERS });
  }

  const hasActiveFilters = computed(() =>
    Boolean(
      filters.value.category.length ||
      filters.value.status.length ||
      filters.value.autonomy.length ||
      filters.value.owner.length ||
      filters.value.search.trim(),
    ),
  );

  return {
    filteredSkills,
    debouncedSearch,
    hasActiveFilters,
    setSearch,
    setSortBy,
    toggleArrayFilter,
    resetFilters,
    includesAny,
  };
}
