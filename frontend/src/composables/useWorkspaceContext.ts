import { ref, readonly } from 'vue';
import type { WorkspaceContext } from '@/types/shell';
import { fetchJson } from '@/api/client';

const context = ref<WorkspaceContext>({
  workspace: '',
  application: '',
  snowGroup: null,
  project: null,
  environment: null
});

const loading = ref(true);
const error = ref<string | null>(null);
let initialized = false;

async function load(): Promise<void> {
  loading.value = true;
  error.value = null;
  try {
    context.value = await fetchJson<WorkspaceContext>('/workspace-context');
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to load workspace context';
  } finally {
    loading.value = false;
  }
}

/**
 * Shared workspace context composable (singleton).
 * Fetches from GET /api/v1/workspace-context on first use.
 */
export function useWorkspaceContext() {
  if (!initialized) {
    initialized = true;
    load();
  }

  return {
    context: readonly(context),
    loading: readonly(loading),
    error: readonly(error),
    reload: load
  };
}
