import { fetchJson } from './client';
import type { WorkspaceContext } from '@/shared/types/shell';

export function getWorkspaceContext(): Promise<WorkspaceContext> {
  return fetchJson<WorkspaceContext>('/workspace-context');
}
