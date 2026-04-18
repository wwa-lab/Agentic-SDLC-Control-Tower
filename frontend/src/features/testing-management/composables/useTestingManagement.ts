import { computed } from 'vue';
import { TM_USE_MOCK } from '../api/testingManagementApi';
import { TM_DEFAULT_WORKSPACE_ID } from '../types';

export function useTestingManagement() {
  const isMockMode = computed(() => TM_USE_MOCK);
  const dataSourceLabel = computed(() => isMockMode.value ? 'Mock Workspace' : 'Backend Workspace');

  return {
    isMockMode,
    dataSourceLabel,
    defaultWorkspaceId: TM_DEFAULT_WORKSPACE_ID,
  };
}
