import { beforeEach, describe, expect, it } from 'vitest';
import { aiCenterApi } from '../api/aiCenterApi';

describe('aiCenterApi', () => {
  beforeEach(() => {
    window.history.replaceState({}, '', '/');
  });

  it('returns mocked metrics in the default scenario', async () => {
    const metrics = await aiCenterApi.getMetrics('ws-default-001');

    expect(metrics.window).toBe('30d');
    expect(metrics.aiUsageRate.data?.unit).toBe('%');
    expect(metrics.stageCoverageCount.data?.value).toBeGreaterThan(0);
  });

  it('maps mock envelope errors to thrown Error instances', async () => {
    window.history.replaceState({}, '', '/?aiCenterScenario=error-skills');

    await expect(aiCenterApi.getSkills('ws-default-001')).rejects.toThrow('Skill catalog failed to load.');
  });

  it('returns server-shaped paged runs from mocks', async () => {
    const page = await aiCenterApi.getRuns('ws-default-001', {
      status: ['pending_approval'],
      page: 1,
      size: 10,
    });

    expect(page.page).toBe(1);
    expect(page.size).toBe(10);
    expect(page.items.every(run => run.status === 'pending_approval')).toBe(true);
  });
});
