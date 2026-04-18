import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import FilterForm from '../components/filter/FilterForm.vue';
import type { ReportDefinitionDto, ReportRunRequest } from '../types';

const definition: ReportDefinitionDto = {
  reportKey: 'eff.lead-time',
  category: 'efficiency',
  name: 'Delivery Lead Time',
  description: 'Time from requirement ready to deploy.',
  supportedScopes: ['org', 'workspace', 'project'],
  supportedGroupings: ['team', 'project', 'requirementType'],
  defaultGrouping: 'team',
  chartType: 'histogram',
  drilldownColumns: [],
  status: 'enabled',
};

const request: ReportRunRequest = {
  scope: 'workspace',
  scopeIds: ['ws-default-001'],
  timeRange: { preset: 'last30d' },
  grouping: 'team',
  extraFilters: {},
};

describe('FilterForm', () => {
  it('emits apply for a valid request', async () => {
    const wrapper = mount(FilterForm, {
      props: {
        definition,
        modelValue: request,
      },
    });

    await wrapper.get('form').trigger('submit.prevent');

    expect(wrapper.emitted('apply')).toHaveLength(1);
    expect(wrapper.emitted('apply')?.[0]?.[0]).toMatchObject({
      scope: 'workspace',
      scopeIds: ['ws-default-001'],
      grouping: 'team',
    });
  });

  it('shows validation errors for invalid custom range', async () => {
    const wrapper = mount(FilterForm, {
      props: {
        definition,
        modelValue: {
          ...request,
          timeRange: {
            preset: 'custom',
            startAt: '2026-04-20T00:00:00Z',
            endAt: '2026-04-18T00:00:00Z',
          },
        },
      },
    });

    await wrapper.get('form').trigger('submit.prevent');

    expect(wrapper.text()).toContain('Custom range start must be before end.');
    expect(wrapper.emitted('apply')).toBeUndefined();
  });
});
