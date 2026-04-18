import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { ApiError } from '@/shared/api/client';
import { designManagementApi } from '../api/designManagementApi';
import type { CatalogAggregate, TraceabilityAggregate, ViewerAggregate } from '../types';

function toMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError && error.message) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

function toStatus(error: unknown) {
  return error instanceof ApiError ? error.status : null;
}

export const useDesignManagementStore = defineStore('designManagement', () => {
  const workspaceId = ref<string | null>(null);
  const artifactId = ref<string | null>(null);
  const versionId = ref<string | null>(null);

  const catalog = ref<CatalogAggregate | null>(null);
  const viewer = ref<ViewerAggregate | null>(null);
  const traceability = ref<TraceabilityAggregate | null>(null);

  const catalogLoading = ref(false);
  const viewerLoading = ref(false);
  const traceabilityLoading = ref(false);

  const catalogError = ref<string | null>(null);
  const viewerError = ref<string | null>(null);
  const traceabilityError = ref<string | null>(null);

  const catalogErrorStatus = ref<number | null>(null);
  const viewerErrorStatus = ref<number | null>(null);
  const traceabilityErrorStatus = ref<number | null>(null);

  const isViewerForbidden = computed(() => viewerErrorStatus.value === 403);
  const isViewerNotFound = computed(() => viewerErrorStatus.value === 404);

  async function initCatalog(nextWorkspaceId: string) {
    workspaceId.value = nextWorkspaceId;
    catalogLoading.value = true;
    catalogError.value = null;
    catalogErrorStatus.value = null;
    try {
      catalog.value = await designManagementApi.getCatalogAggregate(nextWorkspaceId);
    } catch (error) {
      catalog.value = null;
      catalogError.value = toMessage(error, 'Failed to load design artifact catalog.');
      catalogErrorStatus.value = toStatus(error);
    } finally {
      catalogLoading.value = false;
    }
  }

  async function openArtifact(nextArtifactId: string, nextVersionId?: string | null) {
    artifactId.value = nextArtifactId;
    versionId.value = nextVersionId ?? null;
    viewerLoading.value = true;
    viewerError.value = null;
    viewerErrorStatus.value = null;
    try {
      viewer.value = await designManagementApi.getViewerAggregate(nextArtifactId, nextVersionId);
      workspaceId.value = viewer.value.header.data?.workspaceId ?? workspaceId.value;
    } catch (error) {
      viewer.value = null;
      viewerError.value = toMessage(error, 'Failed to load design artifact viewer.');
      viewerErrorStatus.value = toStatus(error);
    } finally {
      viewerLoading.value = false;
    }
  }

  async function initTraceability(nextWorkspaceId: string) {
    workspaceId.value = nextWorkspaceId;
    traceabilityLoading.value = true;
    traceabilityError.value = null;
    traceabilityErrorStatus.value = null;
    try {
      traceability.value = await designManagementApi.getTraceabilityAggregate(nextWorkspaceId);
    } catch (error) {
      traceability.value = null;
      traceabilityError.value = toMessage(error, 'Failed to load design traceability.');
      traceabilityErrorStatus.value = toStatus(error);
    } finally {
      traceabilityLoading.value = false;
    }
  }

  function reset() {
    workspaceId.value = null;
    artifactId.value = null;
    versionId.value = null;
    catalog.value = null;
    viewer.value = null;
    traceability.value = null;
    catalogLoading.value = false;
    viewerLoading.value = false;
    traceabilityLoading.value = false;
    catalogError.value = null;
    viewerError.value = null;
    traceabilityError.value = null;
    catalogErrorStatus.value = null;
    viewerErrorStatus.value = null;
    traceabilityErrorStatus.value = null;
  }

  return {
    workspaceId,
    artifactId,
    versionId,
    catalog,
    viewer,
    traceability,
    catalogLoading,
    viewerLoading,
    traceabilityLoading,
    catalogError,
    viewerError,
    traceabilityError,
    catalogErrorStatus,
    viewerErrorStatus,
    traceabilityErrorStatus,
    isViewerForbidden,
    isViewerNotFound,
    initCatalog,
    openArtifact,
    initTraceability,
    reset,
  };
});
