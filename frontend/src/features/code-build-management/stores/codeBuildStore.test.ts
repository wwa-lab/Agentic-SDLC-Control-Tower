import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { codeBuildApi, codeBuildMockApi } from '../api/codeBuildApi';
import { useCodeBuildStore } from './codeBuildStore';

describe('codeBuildStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.restoreAllMocks();
    codeBuildMockApi.resetMockState();
  });

  it('hydrates catalog state for the current workspace', async () => {
    const store = useCodeBuildStore();
    await store.initCatalog();

    expect(store.catalog?.summary.data?.workspaceId).toBe('ws-default-001');
    expect(store.catalog?.grid.data?.length).toBeGreaterThan(0);
  });

  it('reloads PR aggregate when the head SHA fence trips', async () => {
    const aggregateSpy = vi.spyOn(codeBuildApi, 'getPrAggregate');
    const store = useCodeBuildStore();

    store.setViewerContext({ role: 'PM' });
    await store.openPr('pr-42');
    await store.regenerateAiPrReview('pr-42', {
      prevHeadSha: 'stale-head',
      reason: '',
    });

    expect(aggregateSpy).toHaveBeenCalledTimes(2);
    expect(store.mutationMessage).toContain('head changed');
  });

  it('gates blocker bodies by role in the loaded PR aggregate', async () => {
    const store = useCodeBuildStore();

    store.setViewerContext({ role: 'DEVELOPER' });
    await store.openPr('pr-42');
    expect(store.pr?.aiReview.data?.notes[0].body).toContain('Visible to PM + Tech Lead');

    store.setViewerContext({ role: 'PM' });
    await store.openPr('pr-42');
    expect(store.pr?.aiReview.data?.notes[0].body).toContain('traceability chip');
  });

  it('surfaces GitHub rate-limit metadata after rerun failure', async () => {
    const store = useCodeBuildStore();
    store.setViewerContext({ role: 'PM' });
    await store.openRun('run-991');
    await store.rerunRun('run-991', { reason: 'CB_GH_RATE_LIMIT' });

    expect(store.mutationCode).toBe('CB_GH_RATE_LIMIT');
    expect(store.rateLimitResetAt).not.toBeNull();
  });
});
