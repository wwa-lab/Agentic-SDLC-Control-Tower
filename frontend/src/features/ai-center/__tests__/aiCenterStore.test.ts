import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAiCenterStore } from '../stores/aiCenterStore';
import { aiCenterApi } from '../api/aiCenterApi';
import { getMockFixture } from '../api/mocks';

describe('aiCenterStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.restoreAllMocks();
  });

  it('initializes all four top-level sections in parallel', async () => {
    const fixture = getMockFixture('ws-default-001');
    vi.spyOn(aiCenterApi, 'getMetrics').mockResolvedValue(fixture.metrics);
    vi.spyOn(aiCenterApi, 'getStageCoverage').mockResolvedValue(fixture.stageCoverage);
    vi.spyOn(aiCenterApi, 'getSkills').mockResolvedValue(fixture.skills);
    vi.spyOn(aiCenterApi, 'getRuns').mockResolvedValue({
      items: fixture.runs.slice(0, 5),
      page: 1,
      size: 50,
      total: fixture.runs.length,
      hasMore: true,
    });

    const store = useAiCenterStore();
    await store.init('ws-default-001');

    expect(store.workspaceId).toBe('ws-default-001');
    expect(store.metrics.data?.window).toBe('30d');
    expect(store.stageCoverage.data?.length).toBe(11);
    expect(store.skills.data?.length).toBeGreaterThan(5);
    expect(store.runs.data?.items.length).toBe(5);
  });

  it('clears cached detail state when the workspace changes', async () => {
    const defaultFixture = getMockFixture('ws-default-001');
    const nextFixture = getMockFixture('ws-alt-002');

    vi.spyOn(aiCenterApi, 'getMetrics')
      .mockResolvedValueOnce(defaultFixture.metrics)
      .mockResolvedValueOnce(nextFixture.metrics);
    vi.spyOn(aiCenterApi, 'getStageCoverage')
      .mockResolvedValueOnce(defaultFixture.stageCoverage)
      .mockResolvedValueOnce(nextFixture.stageCoverage);
    vi.spyOn(aiCenterApi, 'getSkills')
      .mockResolvedValueOnce(defaultFixture.skills)
      .mockResolvedValueOnce(nextFixture.skills);
    vi.spyOn(aiCenterApi, 'getRuns')
      .mockResolvedValue({
        items: defaultFixture.runs.slice(0, 5),
        page: 1,
        size: 50,
        total: defaultFixture.runs.length,
        hasMore: true,
      });
    vi.spyOn(aiCenterApi, 'getSkillDetail')
      .mockResolvedValue({
        ...defaultFixture.skillDetails['incident-diagnosis'].skill,
        inputContract: defaultFixture.skillDetails['incident-diagnosis'].inputContract,
        outputContract: defaultFixture.skillDetails['incident-diagnosis'].outputContract,
        policy: defaultFixture.skillDetails['incident-diagnosis'].policy,
        recentRuns: defaultFixture.skillDetails['incident-diagnosis'].recentRuns,
        aggregateMetrics: defaultFixture.skillDetails['incident-diagnosis'].aggregateMetrics,
      });

    const store = useAiCenterStore();
    await store.init('ws-default-001');
    await store.selectSkill('incident-diagnosis');

    expect(store.skillDetail.data?.key).toBe('incident-diagnosis');

    await store.init('ws-alt-002');

    expect(store.skillDetail.data).toBeNull();
    expect(store.workspaceId).toBe('ws-alt-002');
  });

  it('resets run pagination and refetches when filters change', async () => {
    const fixture = getMockFixture('ws-default-001');
    const getRuns = vi.spyOn(aiCenterApi, 'getRuns');
    vi.spyOn(aiCenterApi, 'getMetrics').mockResolvedValue(fixture.metrics);
    vi.spyOn(aiCenterApi, 'getStageCoverage').mockResolvedValue(fixture.stageCoverage);
    vi.spyOn(aiCenterApi, 'getSkills').mockResolvedValue(fixture.skills);
    getRuns
      .mockResolvedValueOnce({
        items: fixture.runs.slice(0, 5),
        page: 1,
        size: 50,
        total: fixture.runs.length,
        hasMore: true,
      })
      .mockResolvedValueOnce({
        items: fixture.runs.filter(run => run.status === 'failed'),
        page: 1,
        size: 50,
        total: fixture.runs.filter(run => run.status === 'failed').length,
        hasMore: false,
      });

    const store = useAiCenterStore();
    await store.init('ws-default-001');
    await store.setRunFilters({ status: ['failed'] });

    expect(store.runFilters.status).toEqual(['failed']);
    expect(store.runFilters.page).toBe(1);
    expect(store.runs.data?.items.every(run => run.status === 'failed')).toBe(true);
    expect(getRuns).toHaveBeenLastCalledWith('ws-default-001', expect.objectContaining({ status: ['failed'], page: 1 }));
  });

  it('appends additional run pages when loading more', async () => {
    const fixture = getMockFixture('ws-default-001');
    vi.spyOn(aiCenterApi, 'getMetrics').mockResolvedValue(fixture.metrics);
    vi.spyOn(aiCenterApi, 'getStageCoverage').mockResolvedValue(fixture.stageCoverage);
    vi.spyOn(aiCenterApi, 'getSkills').mockResolvedValue(fixture.skills);
    const getRuns = vi.spyOn(aiCenterApi, 'getRuns');
    getRuns
      .mockResolvedValueOnce({
        items: fixture.runs.slice(0, 2),
        page: 1,
        size: 2,
        total: fixture.runs.length,
        hasMore: true,
      })
      .mockResolvedValueOnce({
        items: fixture.runs.slice(2, 4),
        page: 2,
        size: 2,
        total: fixture.runs.length,
        hasMore: true,
      });

    const store = useAiCenterStore();
    await store.init('ws-default-001');
    store.runFilters.size = 2;
    store.runs.data = {
      items: fixture.runs.slice(0, 2),
      page: 1,
      size: 2,
      total: fixture.runs.length,
      hasMore: true,
    };
    await store.loadMoreRuns();

    expect(store.runs.data?.items.length).toBe(4);
    expect(store.runs.data?.page).toBe(2);
  });
});
