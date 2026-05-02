import { defineStore } from 'pinia';
import { computed, readonly, ref } from 'vue';
import type { WorkspaceContext } from '@/shared/types/shell';
import { getWorkspaceContext } from '@/shared/api/workspaceApi';

/**
 * Pinia store for workspace context.
 * Single source of truth — replaces the old singleton composable.
 */
export const useWorkspaceStore = defineStore('workspace', () => {
  const baseContext = ref<WorkspaceContext>({
    workspaceId: null,
    workspace: '',
    applicationId: null,
    application: '',
    snowGroupId: null,
    snowGroup: null,
    projectId: null,
    project: null,
    environment: null,
    demoMode: false
  });
  const routeContext = ref<Partial<WorkspaceContext> | null>(null);

  const loading = ref(false);
  const error = ref<string | null>(null);

  const context = computed<WorkspaceContext>(() => ({
    workspaceId: routeContext.value?.workspaceId ?? baseContext.value.workspaceId,
    workspace: routeContext.value?.workspace ?? baseContext.value.workspace,
    applicationId: routeContext.value?.applicationId ?? baseContext.value.applicationId,
    application: routeContext.value?.application ?? baseContext.value.application,
    snowGroupId: routeContext.value?.snowGroupId ?? baseContext.value.snowGroupId,
    snowGroup: routeContext.value?.snowGroup ?? baseContext.value.snowGroup,
    projectId: routeContext.value?.projectId ?? baseContext.value.projectId,
    project: routeContext.value?.project ?? baseContext.value.project,
    environment: routeContext.value?.environment ?? baseContext.value.environment,
    demoMode: routeContext.value?.demoMode ?? baseContext.value.demoMode,
  }));

  async function load(): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      baseContext.value = await getWorkspaceContext();
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load workspace context';
    } finally {
      loading.value = false;
    }
  }

  function setRouteContext(next: Partial<WorkspaceContext>) {
    routeContext.value = next;
  }

  function clearRouteContext() {
    routeContext.value = null;
  }

  return {
    context,
    loading: readonly(loading),
    error: readonly(error),
    load,
    setRouteContext,
    clearRouteContext,
  };
});
