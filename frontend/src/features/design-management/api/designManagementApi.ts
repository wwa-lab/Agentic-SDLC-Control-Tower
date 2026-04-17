import { fetchJson } from '@/shared/api/client';
import type { CatalogAggregate, TraceabilityAggregate, ViewerAggregate } from '../types';
import { getMockCatalogAggregate, getMockTraceabilityAggregate, getMockViewerAggregate } from '../mock/data';

const USE_MOCK = import.meta.env.DEV && !import.meta.env.VITE_USE_BACKEND;

export const designManagementApi = {
  async getCatalogAggregate(workspaceId: string): Promise<CatalogAggregate> {
    if (USE_MOCK) {
      return getMockCatalogAggregate();
    }
    return fetchJson<CatalogAggregate>(`/design-management/catalog?workspaceId=${encodeURIComponent(workspaceId)}`);
  },

  async getViewerAggregate(artifactId: string, versionId?: string | null): Promise<ViewerAggregate> {
    if (USE_MOCK) {
      return getMockViewerAggregate(artifactId);
    }
    const query = versionId ? `?version=${encodeURIComponent(versionId)}` : '';
    return fetchJson<ViewerAggregate>(`/design-management/artifacts/${encodeURIComponent(artifactId)}${query}`);
  },

  async getTraceabilityAggregate(workspaceId: string): Promise<TraceabilityAggregate> {
    if (USE_MOCK) {
      return getMockTraceabilityAggregate();
    }
    return fetchJson<TraceabilityAggregate>(`/design-management/traceability?workspaceId=${encodeURIComponent(workspaceId)}`);
  },
};
