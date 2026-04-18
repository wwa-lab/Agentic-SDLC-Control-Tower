import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { mount, flushPromises } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import AiCenterView from '../AiCenterView.vue';
import SkillDetailPanel from '../components/SkillDetailPanel.vue';
import RunDetailPanel from '../components/RunDetailPanel.vue';

describe('AiCenterView routing', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.useFakeTimers();
    window.history.replaceState({}, '', '/');
  });

  async function mountAt(path: string) {
    const pinia = createPinia();
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/ai-center',
          component: AiCenterView,
          children: [
            { path: 'skills/:skillKey', name: 'ai-center-skill-detail', component: SkillDetailPanel },
            { path: 'runs/:executionId', name: 'ai-center-run-detail', component: RunDetailPanel },
          ],
        },
      ],
    });

    await router.push(path);
    await router.isReady();

    const wrapper = mount({ template: '<router-view />' }, {
      global: {
        plugins: [pinia, router],
        stubs: {
          Teleport: true,
        },
      },
    });

    await vi.runAllTimersAsync();
    await flushPromises();

    return wrapper;
  }

  it('deep-links into skill detail', async () => {
    const wrapper = await mountAt('/ai-center/skills/incident-diagnosis');

    expect(wrapper.text()).toContain('AI Center');
    expect(wrapper.text()).toContain('Skill Detail');
    expect(wrapper.text()).toContain('Incident Diagnosis');
  });

  it('deep-links into run detail', async () => {
    const wrapper = await mountAt('/ai-center/runs/run_inc_001');

    expect(wrapper.text()).toContain('Run Detail');
    expect(wrapper.text()).toContain('Incident Diagnosis');
    expect(wrapper.text()).toContain('Trigger Source');
  });
});
