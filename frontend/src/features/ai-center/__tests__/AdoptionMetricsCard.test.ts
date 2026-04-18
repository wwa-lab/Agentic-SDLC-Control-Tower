import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import AdoptionMetricsCard from '../components/AdoptionMetricsCard.vue';
import { getMockFixture } from '../api/mocks';

describe('AdoptionMetricsCard', () => {
  const fixture = getMockFixture('ws-default-001');

  it('renders the normal state with five metric tiles', () => {
    const wrapper = mount(AdoptionMetricsCard, {
      props: {
        metrics: fixture.metrics,
        loading: false,
        error: null,
      },
    });

    expect(wrapper.text()).toContain('Adoption Metrics');
    expect(wrapper.findAllComponents({ name: 'MetricTile' }).length).toBe(5);
  });

  it('renders loading skeletons', () => {
    const wrapper = mount(AdoptionMetricsCard, {
      props: {
        metrics: null,
        loading: true,
        error: null,
      },
    });

    expect(wrapper.attributes('data-state')).toBeUndefined();
    expect(wrapper.find('[data-state="loading"]').exists()).toBe(true);
  });

  it('renders the empty state when every section is empty', () => {
    const wrapper = mount(AdoptionMetricsCard, {
      props: {
        metrics: {
          window: '30d',
          aiUsageRate: { data: null, error: null },
          adoptionRate: { data: null, error: null },
          autoExecSuccessRate: { data: null, error: null },
          timeSavedHours: { data: null, error: null },
          stageCoverageCount: { data: null, error: null },
        },
        loading: false,
        error: null,
      },
    });

    expect(wrapper.find('[data-state="empty"]').exists()).toBe(true);
  });

  it('renders the error state', () => {
    const wrapper = mount(AdoptionMetricsCard, {
      props: {
        metrics: null,
        loading: false,
        error: 'Metrics unavailable',
      },
    });

    expect(wrapper.find('[data-state="error"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('Metrics unavailable');
  });

  it('renders partial tile failures without blanking the whole card', () => {
    const wrapper = mount(AdoptionMetricsCard, {
      props: {
        metrics: {
          ...fixture.metrics,
          aiUsageRate: { data: null, error: 'Aggregation timed out' },
        },
        loading: false,
        error: null,
      },
    });

    expect(wrapper.find('[data-state="normal"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('Aggregation timed out');
  });
});
