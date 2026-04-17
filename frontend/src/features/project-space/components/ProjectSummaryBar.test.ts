import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import ProjectSummaryBar from './ProjectSummaryBar.vue';
import { summary as summaryMock } from '../mock/summary.mock';

describe('ProjectSummaryBar', () => {
  it('renders the seeded project summary and exposes the workspace back-link', async () => {
    const wrapper = mount(ProjectSummaryBar, {
      props: {
        section: {
          data: summaryMock('proj-42'),
          error: null,
        },
      },
    });

    expect(wrapper.text()).toContain('Gateway Migration');
    expect(wrapper.text()).toContain('Payment-Gateway-Pro');
    expect(wrapper.text()).toContain('Active Specs');
    expect(wrapper.text()).toContain('7');

    await wrapper.get('.workspace-link').trigger('click');
    expect(wrapper.emitted('openLink')?.[0]).toEqual(['/team?workspaceId=ws-default-001']);
  });

  it('renders the retry state when the summary section errors', () => {
    const wrapper = mount(ProjectSummaryBar, {
      props: {
        section: {
          data: null,
          error: 'Project summary unavailable',
        },
      },
    });

    expect(wrapper.text()).toContain('Project Summary Unavailable');
    expect(wrapper.text()).toContain('Project summary unavailable');
  });
});
