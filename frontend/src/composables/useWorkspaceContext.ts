import { ref, readonly } from 'vue';
import type { WorkspaceContext } from '@/types/shell';

/**
 * Composable for shared workspace context.
 * V1 provides static mocked data as per Phase A requirements.
 * 
 * TESTING NULL FALLBACKS:
 * To test the shell's graceful fallback for optional fields,
 * comment out the 'FULL MOCK' block and uncomment the 'SPARSE MOCK' block.
 */
export function useWorkspaceContext() {
  // --- FULL MOCK (Default) ---
  const context = ref<WorkspaceContext>({
    workspace: 'Global SDLC Tower',
    application: 'Payment-Gateway-Pro',
    snowGroup: 'FIN-TECH-OPS',
    project: 'Q2-Cloud-Migration',
    environment: 'Production'
  });

  // --- SPARSE MOCK (Uncomment to test null/optional fallbacks) ---
  /*
  const context = ref<WorkspaceContext>({
    workspace: 'Empty Workspace',
    application: 'Unknown App',
    snowGroup: null,
    project: null,
    environment: null
  });
  */

  return {
    context: readonly(context)
  };
}
