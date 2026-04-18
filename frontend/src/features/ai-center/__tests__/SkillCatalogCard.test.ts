import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import SkillCatalogCard from '../components/SkillCatalogCard.vue';
import { getMockFixture } from '../api/mocks';
import { DEFAULT_SKILL_FILTERS } from '../constants';

describe('SkillCatalogCard', () => {
  const fixture = getMockFixture('ws-default-001');

  it('renders the normal state with skill rows', () => {
    const wrapper = mount(SkillCatalogCard, {
      props: {
        skills: fixture.skills,
        loading: false,
        error: null,
        filters: { ...DEFAULT_SKILL_FILTERS },
      },
    });

    expect(wrapper.find('[data-state="normal"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('Skill Catalog');
    expect(wrapper.text()).toContain('Incident Diagnosis');
  });

  it('renders loading rows', () => {
    const wrapper = mount(SkillCatalogCard, {
      props: {
        skills: [],
        loading: true,
        error: null,
        filters: { ...DEFAULT_SKILL_FILTERS },
      },
    });

    expect(wrapper.find('[data-state="loading"]').exists()).toBe(true);
  });

  it('renders the empty state for an empty catalog', () => {
    const wrapper = mount(SkillCatalogCard, {
      props: {
        skills: [],
        loading: false,
        error: null,
        filters: { ...DEFAULT_SKILL_FILTERS },
      },
    });

    expect(wrapper.find('[data-state="empty"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('No skills registered');
  });

  it('renders the error state', () => {
    const wrapper = mount(SkillCatalogCard, {
      props: {
        skills: [],
        loading: false,
        error: 'Catalog failed',
        filters: { ...DEFAULT_SKILL_FILTERS },
      },
    });

    expect(wrapper.find('[data-state="error"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('Catalog failed');
  });
});
