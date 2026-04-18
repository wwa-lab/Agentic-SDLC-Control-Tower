import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { ApiError } from '@/shared/api/client';
import { projectSpaceApi } from '../api/projectSpaceApi';
import { createMockAggregate } from '../mock/catalog';
import { useProjectSpaceStore } from './projectSpaceStore';

describe('projectSpaceStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.restoreAllMocks();
  });

  it('hydrates aggregate state from the aggregate endpoint', async () => {
    vi.spyOn(projectSpaceApi, 'getAggregate').mockResolvedValue(createMockAggregate('proj-42'));

    const store = useProjectSpaceStore();
    await store.initProject('proj-42');

    expect(store.projectId).toBe('proj-42');
    expect(store.workspaceId).toBe('ws-default-001');
    expect(store.aggregate?.summary.data?.name).toBe('Gateway Migration');
    expect(store.error).toBeNull();
  });

  it('falls back to section hydration when the aggregate endpoint fails', async () => {
    const aggregate = createMockAggregate('proj-42');
    vi.spyOn(projectSpaceApi, 'getAggregate').mockRejectedValue(
      new ApiError(500, 'Server Error', 'Aggregate failed'),
    );
    vi.spyOn(projectSpaceApi, 'getSection').mockImplementation(async cardKey => {
      return aggregate[cardKey];
    });

    const store = useProjectSpaceStore();
    await store.initProject('proj-42');

    expect(store.error).toBeNull();
    expect(store.aggregate?.dependencies.data?.upstream.length).toBe(2);
    expect(store.aggregate?.risks.data?.total).toBe(2);
  });
});
