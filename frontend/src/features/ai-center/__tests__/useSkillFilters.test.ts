import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ref } from 'vue';
import { useSkillFilters } from '../composables/useSkillFilters';
import { DEFAULT_SKILL_FILTERS } from '../constants';
import { getMockFixture } from '../api/mocks';
import type { SkillFilterState } from '../types';

describe('useSkillFilters', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('filters by category, owner, and debounced search', async () => {
    const fixture = getMockFixture('ws-default-001');
    const skills = ref(fixture.skills);
    const filters = ref<SkillFilterState>({ ...DEFAULT_SKILL_FILTERS });
    const setFilters = (patch: Partial<SkillFilterState>) => {
      filters.value = { ...filters.value, ...patch };
    };

    const { filteredSkills, toggleArrayFilter, setSearch } = useSkillFilters(skills, filters, setFilters);

    toggleArrayFilter('category', 'runtime');
    toggleArrayFilter('owner', 'platform-sre');
    setSearch('incident');
    await vi.advanceTimersByTimeAsync(160);

    expect(filteredSkills.value.map(skill => skill.key)).toEqual(['incident-diagnosis']);
  });

  it('sorts alphabetically when requested', () => {
    const fixture = getMockFixture('ws-default-001');
    const skills = ref(fixture.skills);
    const filters = ref<SkillFilterState>({ ...DEFAULT_SKILL_FILTERS, sortBy: 'name' });
    const setFilters = (patch: Partial<SkillFilterState>) => {
      filters.value = { ...filters.value, ...patch };
    };

    const { filteredSkills } = useSkillFilters(skills, filters, setFilters);

    expect(filteredSkills.value[0].name).toBe('Architecture Blueprint');
  });
});
