import { defineStore } from 'pinia';
import { ref, readonly } from 'vue';
import type { WorkspaceContext } from '@/shared/types/shell';
import { getWorkspaceContext } from '@/shared/api/workspaceApi';

/**
 * Pinia store for workspace context.
 * Single source of truth — replaces the old singleton composable.
 */
export const useWorkspaceStore = defineStore('workspace', () => {
  const context = ref<WorkspaceContext>({
    workspace: '',
    application: '',
    snowGroup: null,
    project: null,
    environment: null
  });

  const loading = ref(false);
  const error = ref<string | null>(null);

  async function load(): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      context.value = await getWorkspaceContext();
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load workspace context';
    } finally {
      loading.value = false;
    }
  }

  return {
    context: readonly(context),
    loading: readonly(loading),
    error: readonly(error),
    load,
  };
});
