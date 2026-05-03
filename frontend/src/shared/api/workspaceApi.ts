import { fetchJson, postJson } from './client';
import type { Workspace, WorkspaceContext } from '@/shared/types/shell';

export function getWorkspaceContext(): Promise<WorkspaceContext> {
  return fetchJson<WorkspaceContext>('/workspace-context');
}

export function listMyWorkspaces(): Promise<Workspace[]> {
  return fetchJson<Workspace[]>('/auth/workspaces');
}

export function switchWorkspace(workspaceId: string): Promise<Workspace> {
  return postJson<Workspace>('/auth/workspace', { workspaceId });
}

export async function resolveWorkspaceByKey(key: string): Promise<Workspace | null> {
  try {
    return await fetchJson<Workspace>(`/auth/workspaces/by-key/${encodeURIComponent(key)}`);
  } catch {
    return null;
  }
}
