import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import BuildStatusBadge from './BuildStatusBadge.vue';

describe('BuildStatusBadge', () => {
  it('renders the supplied build status token', () => {
    const wrapper = mount(BuildStatusBadge, {
      props: {
        status: 'RUNNING',
      },
    });

    expect(wrapper.text()).toContain('RUNNING');
    expect(wrapper.classes()).toContain('build-status--running');
  });
});

