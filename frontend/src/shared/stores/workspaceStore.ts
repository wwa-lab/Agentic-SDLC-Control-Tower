import { defineStore } from 'pinia';
import { computed, readonly, ref } from 'vue';
import type { Workspace, WorkspaceContext } from '@/shared/types/shell';
import { getWorkspaceContext, listMyWorkspaces } from '@/shared/api/workspaceApi';
import { setActiveWorkspaceId } from '@/shared/api/client';

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
    demoMode: false,
  });
  const routeContext = ref<Partial<WorkspaceContext> | null>(null);
  const workspaces = ref<Workspace[]>([]);
  const activeWorkspace = ref<Workspace | null>(null);

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

  const activeWorkspaceKey = computed(() => activeWorkspace.value?.workspaceKey ?? null);

  async function load(): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      workspaces.value = await listMyWorkspaces();
      // For guests (empty list), fall back to the legacy demo context endpoint.
      if (workspaces.value.length === 0) {
        baseContext.value = await getWorkspaceContext();
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load workspace context';
    } finally {
      loading.value = false;
    }
  }

  function setActive(ws: Workspace): void {
    activeWorkspace.value = ws;
    baseContext.value = {
      workspaceId: ws.workspaceId,
      workspace: ws.name,
      applicationId: ws.applicationId,
      application: ws.applicationId,
      snowGroupId: ws.snowGroupId,
      snowGroup: ws.snowGroupId,
      projectId: null,
      project: null,
      environment: null,
      demoMode: false,
    };
    setActiveWorkspaceId(ws.workspaceId);
  }

  function setRouteContext(next: Partial<WorkspaceContext>) {
    routeContext.value = next;
  }

  function clearRouteContext() {
    routeContext.value = null;
  }

  return {
    context,
    workspaces: readonly(workspaces),
    activeWorkspace: readonly(activeWorkspace),
    activeWorkspaceKey,
    loading: readonly(loading),
    error: readonly(error),
    load,
    setActive,
    setRouteContext,
    clearRouteContext,
  };
});
