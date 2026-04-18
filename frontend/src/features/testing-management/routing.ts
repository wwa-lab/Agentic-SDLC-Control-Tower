import type { LocationQuery, RouteLocationNormalizedLoaded, Router } from 'vue-router';
import { TM_DEFAULT_WORKSPACE_ID } from './types';

function readString(value: LocationQuery[string]) {
  return typeof value === 'string' && value.trim() ? value : null;
}

export function resolveWorkspaceIdFromQuery(query: LocationQuery) {
  return readString(query.workspaceId) ?? TM_DEFAULT_WORKSPACE_ID;
}

export function buildWorkspaceQuery(route: RouteLocationNormalizedLoaded, workspaceId: string) {
  return {
    ...route.query,
    workspaceId,
  };
}

export async function updateWorkspaceQuery(
  router: Router,
  route: RouteLocationNormalizedLoaded,
  workspaceId: string,
) {
  await router.replace({
    query: buildWorkspaceQuery(route, workspaceId),
  });
}
