import type { AiRowStatus, AiAutonomyLevel, ProjectRole } from '../types/enums';

type ErrorCode =
  | 'DP_AI_UNAVAILABLE'
  | 'DP_JENKINS_UNREACHABLE'
  | 'DP_RELEASE_NOTES_EVIDENCE_MISMATCH'
  | 'DP_BUILD_ARTIFACT_PENDING'
  | 'DP_RELEASE_UNRESOLVED'
  | 'DP_AI_AUTONOMY_INSUFFICIENT'
  | 'DP_ROLE_REQUIRED'
  | 'DP_WORKSPACE_FORBIDDEN'
  | 'DP_STORY_NOT_FOUND'
  | 'DP_INGEST_SIGNATURE_INVALID'
  | 'DP_RATE_LIMITED';

interface CommandResult<T> {
  readonly ok: boolean;
  readonly data?: T;
  readonly error?: { readonly code: ErrorCode; readonly message: string };
}

function roll(percent: number): boolean {
  return Math.random() * 100 < percent;
}

function err(code: ErrorCode, message: string): CommandResult<never> {
  return { ok: false, error: { code, message } };
}

function ok<T>(data: T): CommandResult<T> {
  return { ok: true, data };
}

export function simulateRegenerate(
  kind: 'release-notes' | 'deploy-summary' | 'workspace-summary',
  role: ProjectRole,
  autonomy: AiAutonomyLevel,
  workspaceId: string,
  targetWorkspaceId: string,
): CommandResult<{ status: AiRowStatus }> {
  if (workspaceId !== targetWorkspaceId) {
    return err('DP_WORKSPACE_FORBIDDEN', 'Cannot access entities from another workspace');
  }
  if (role !== 'PM' && role !== 'TECH_LEAD') {
    return err('DP_ROLE_REQUIRED', 'Only PM or Tech Lead can regenerate AI content');
  }
  if (autonomy === 'DISABLED' || autonomy === 'OBSERVATION') {
    return err('DP_AI_AUTONOMY_INSUFFICIENT', 'AI autonomy level must be SUPERVISED or higher');
  }
  if (roll(5)) {
    return err('DP_AI_UNAVAILABLE', 'AI skill service is temporarily unavailable');
  }
  if (kind === 'release-notes' && roll(3)) {
    return ok({ status: 'EVIDENCE_MISMATCH' as const });
  }
  return ok({ status: 'PENDING' as const });
}

export function simulateBackfillReload(): CommandResult<void> {
  if (roll(2)) {
    return err('DP_JENKINS_UNREACHABLE', 'Jenkins instance did not respond within timeout');
  }
  return ok(undefined);
}

export function simulateReleaseView(): CommandResult<void> {
  if (roll(2)) {
    return err('DP_BUILD_ARTIFACT_PENDING', 'Build artifact not yet available from Code & Build');
  }
  if (roll(1)) {
    return err('DP_RELEASE_UNRESOLVED', 'Release version could not be resolved — awaiting Jenkins metadata');
  }
  return ok(undefined);
}

export function simulateStoryLookup(storyId: string): CommandResult<void> {
  if (storyId === 'STORY-999') {
    return err('DP_STORY_NOT_FOUND', `Story ${storyId} not found in upstream registry`);
  }
  return ok(undefined);
}

export function simulateWebhookSignature(tampered: boolean): CommandResult<void> {
  if (tampered) {
    return err('DP_INGEST_SIGNATURE_INVALID', 'Webhook signature verification failed');
  }
  return ok(undefined);
}

let lastRegenTimestamp = 0;

export function simulateRateLimit(): CommandResult<void> {
  const now = Date.now();
  if (now - lastRegenTimestamp < 60_000) {
    return err('DP_RATE_LIMITED', 'Rate limit exceeded — try again in 60 seconds');
  }
  lastRegenTimestamp = now;
  return ok(undefined);
}
