import type { Lineage } from '@/shared/types/lineage';
import { ApiError } from '@/shared/api/client';
import type { AiPrReviewPayload } from './types/pr';
import type { AiAutonomyLevel, ProjectRole, RunStatus, StoryLinkStatus } from './types/enums';

const AUTONOMY_ORDER: Record<AiAutonomyLevel, number> = {
  DISABLED: 0,
  OBSERVATION: 1,
  SUPERVISED: 2,
  AUTONOMOUS: 3,
};

export function formatTimestamp(value: string | null | undefined, locale = 'en-US'): string {
  if (!value) {
    return 'n/a';
  }
  return new Intl.DateTimeFormat(locale, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

export function formatRelativeTime(value: string | null | undefined, now = Date.now(), locale = 'en-US'): string {
  if (!value) {
    return 'n/a';
  }
  const diffMs = new Date(value).getTime() - now;
  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' });
  const absMinutes = Math.round(diffMs / 60000);
  if (Math.abs(absMinutes) < 60) {
    return rtf.format(absMinutes, 'minute');
  }
  const absHours = Math.round(diffMs / 3600000);
  if (Math.abs(absHours) < 48) {
    return rtf.format(absHours, 'hour');
  }
  return rtf.format(Math.round(diffMs / 86400000), 'day');
}

export function formatDuration(durationSeconds: number | null | undefined): string {
  if (durationSeconds == null) {
    return 'n/a';
  }
  const minutes = Math.floor(durationSeconds / 60);
  const seconds = durationSeconds % 60;
  if (minutes === 0) {
    return `${seconds}s`;
  }
  return `${minutes}m ${seconds}s`;
}

export function createAiLineage(skillVersion: string | null, generatedAt: string | null): Lineage | null {
  if (!skillVersion || !generatedAt) {
    return null;
  }
  return {
    origin: 'AI_SKILL',
    overridden: false,
    chain: [
      {
        origin: 'AI_SKILL',
        value: skillVersion,
        setAt: generatedAt,
        setBy: 'code-build-mock',
      },
    ],
  };
}

export function canManageRole(role: ProjectRole): boolean {
  return role === 'PM' || role === 'TECH_LEAD';
}

export function canSeeBlockerBodies(role: ProjectRole): boolean {
  return canManageRole(role);
}

export function autonomyMeetsMinimum(current: AiAutonomyLevel, minimum: AiAutonomyLevel): boolean {
  return AUTONOMY_ORDER[current] >= AUTONOMY_ORDER[minimum];
}

export function isNonTerminalStatus(status: RunStatus): boolean {
  return status === 'RUNNING' || status === 'QUEUED';
}

export function isStaleSync(value: string | null | undefined, now = Date.now()): boolean {
  if (!value) {
    return false;
  }
  return now - new Date(value).getTime() > 10 * 60 * 1000;
}

export function filterAiReviewForRole(payload: AiPrReviewPayload, role: ProjectRole): AiPrReviewPayload {
  if (canSeeBlockerBodies(role)) {
    return payload;
  }
  return {
    ...payload,
    notes: payload.notes.map(note => {
      if (note.severity !== 'BLOCKER') {
        return note;
      }
      return {
        ...note,
        body: 'Visible to PM + Tech Lead on this project',
        bodyRestricted: true,
      };
    }),
  };
}

export function deriveStoryLinkStatus(
  candidateIds: ReadonlyArray<string>,
  resolvedStoryIds: ReadonlyArray<string>,
): StoryLinkStatus {
  const uniqueCandidates = [...new Set(candidateIds.map(value => value.trim()).filter(Boolean))];
  if (uniqueCandidates.length === 0) {
    return 'NO_STORY_ID';
  }
  if (uniqueCandidates.length > 1) {
    return 'AMBIGUOUS';
  }
  return resolvedStoryIds.length > 0 ? 'KNOWN' : 'UNKNOWN_STORY';
}

export function toUserMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError && error.message) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

