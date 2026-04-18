import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import RunHistoryCard from '../components/RunHistoryCard.vue';
import { getMockFixture } from '../api/mocks';

describe('RunHistoryCard', () => {
  const fixture = getMockFixture('ws-default-001');

  it('renders the normal state with paged runs', () => {
    const wrapper = mount(RunHistoryCard, {
      props: {
        runs: {
          items: fixture.runs.slice(0, 5),
          page: 1,
          size: 50,
          total: fixture.runs.length,
          hasMore: true,
        },
        loading: false,
        error: null,
        filters: {},
        skills: fixture.skills,
      },
    });

    expect(wrapper.find('[data-state="normal"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('Run History');
    expect(wrapper.text()).toContain('Load more');
  });

  it('renders loading rows', () => {
    const wrapper = mount(RunHistoryCard, {
      props: {
        runs: null,
        loading: true,
        error: null,
        filters: {},
        skills: fixture.skills,
      },
    });

    expect(wrapper.find('[data-state="loading"]').exists()).toBe(true);
  });

  it('renders the empty state when there are no runs', () => {
    const wrapper = mount(RunHistoryCard, {
      props: {
        runs: {
          items: [],
          page: 1,
          size: 50,
          total: 0,
          hasMore: false,
        },
        loading: false,
        error: null,
        filters: {},
        skills: fixture.skills,
      },
    });

    expect(wrapper.find('[data-state="empty"]').exists()).toBe(true);
  });

  it('renders the error state', () => {
    const wrapper = mount(RunHistoryCard, {
      props: {
        runs: null,
        loading: false,
        error: 'Run history failed',
        filters: {},
        skills: fixture.skills,
      },
    });

    expect(wrapper.find('[data-state="error"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('Run history failed');
  });
});
