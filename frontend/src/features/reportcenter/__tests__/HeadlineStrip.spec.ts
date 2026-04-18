import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import HeadlineStrip from '../components/result/HeadlineStrip.vue';

describe('HeadlineStrip', () => {
  it('renders metrics and trend direction', () => {
    const wrapper = mount(HeadlineStrip, {
      props: {
        loading: false,
        section: {
          data: [
            {
              key: 'p50',
              label: 'Median lead time',
              value: '3d 4h',
              numericValue: 4560,
              trend: -8.2,
              trendIsPositive: true,
            },
          ],
          error: null,
        },
      },
    });

    expect(wrapper.text()).toContain('Median lead time');
    expect(wrapper.text()).toContain('3d 4h');
    expect(wrapper.text()).toContain('-8.2%');
    expect(wrapper.html()).toContain('▲');
  });
});
