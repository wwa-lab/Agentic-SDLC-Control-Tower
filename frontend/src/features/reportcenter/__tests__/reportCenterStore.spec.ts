import { setActivePinia, createPinia } from 'pinia';
import { beforeEach, describe, expect, it } from 'vitest';
import { useReportCenterStore } from '../stores/reportCenterStore';

describe('reportCenterStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('loads mock catalog and runs a mock report', async () => {
    const store = useReportCenterStore();
    store.useMockData = true;

    await store.fetchCatalog();
    await store.selectReport('eff.lead-time');
    await store.runReport('eff.lead-time', {
      scope: 'workspace',
      scopeIds: ['ws-default-001'],
      timeRange: { preset: 'last30d' },
      grouping: 'team',
      extraFilters: {},
    });

    expect(store.catalog?.categories[0]?.reports).toHaveLength(5);
    expect(store.activeResult?.reportKey).toBe('eff.lead-time');
    expect(store.activeResult?.headline.data?.length).toBeGreaterThan(0);
  });
});
