import type { LocationQuery, Router, RouteLocationNormalizedLoaded } from 'vue-router';
import {
  CB_DEFAULT_AUTONOMY,
  CB_DEFAULT_ROLE,
  CB_DEFAULT_WORKSPACE_ID,
  CODE_BUILD_AUTONOMY_OPTIONS,
  CODE_BUILD_ROLE_OPTIONS,
  type AiAutonomyLevel,
  type CodeBuildViewerContext,
  type ProjectRole,
} from './types';

function readString(value: LocationQuery[string]) {
  return typeof value === 'string' && value.trim() ? value : null;
}

export function resolveViewerContextFromQuery(query: LocationQuery): CodeBuildViewerContext {
  const workspaceId = readString(query.workspaceId) ?? CB_DEFAULT_WORKSPACE_ID;
  const role = readString(query.role);
  const autonomyLevel = readString(query.autonomy);

  return {
    workspaceId,
    role: (CODE_BUILD_ROLE_OPTIONS.includes(role as ProjectRole) ? role : CB_DEFAULT_ROLE) as ProjectRole,
    autonomyLevel: (
      CODE_BUILD_AUTONOMY_OPTIONS.includes(autonomyLevel as AiAutonomyLevel) ? autonomyLevel : CB_DEFAULT_AUTONOMY
    ) as AiAutonomyLevel,
  };
}

export function buildContextQuery(route: RouteLocationNormalizedLoaded, context: CodeBuildViewerContext) {
  return {
    ...route.query,
    workspaceId: context.workspaceId,
    role: context.role,
    autonomy: context.autonomyLevel,
  };
}

export async function updateContextQuery(
  router: Router,
  route: RouteLocationNormalizedLoaded,
  nextContext: CodeBuildViewerContext,
) {
  await router.replace({
    query: buildContextQuery(route, nextContext),
  });
}

