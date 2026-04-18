import { ApiError } from '@/shared/api/client';
import type { CoverageStatus, DraftOrigin, TestResultOutcome, TestRunState } from './types';

export function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return 'Not available';
  }
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

export function formatRelativeTime(value: string | null | undefined) {
  if (!value) {
    return 'No recent activity';
  }

  const diffMs = new Date(value).getTime() - Date.now();
  const diffMinutes = Math.round(diffMs / 60000);
  const rtf = new Intl.RelativeTimeFormat('en', { numeric: 'auto' });

  if (Math.abs(diffMinutes) < 60) {
    return rtf.format(diffMinutes, 'minute');
  }

  const diffHours = Math.round(diffMinutes / 60);
  if (Math.abs(diffHours) < 24) {
    return rtf.format(diffHours, 'hour');
  }

  const diffDays = Math.round(diffHours / 24);
  return rtf.format(diffDays, 'day');
}

export function formatDuration(seconds: number | null | undefined) {
  if (seconds == null) {
    return 'n/a';
  }
  if (seconds < 60) {
    return `${seconds}s`;
  }
  const minutes = Math.floor(seconds / 60);
  const remaining = seconds % 60;
  return `${minutes}m ${remaining}s`;
}

export function formatPercent(value: number | null | undefined) {
  if (value == null) {
    return '0%';
  }
  return `${Math.round(value * 100)}%`;
}

export function outcomeTone(status: TestRunState | TestResultOutcome) {
  switch (status) {
    case 'PASSED':
    case 'PASS':
      return 'success';
    case 'RUNNING':
      return 'running';
    case 'FAILED':
    case 'FAIL':
    case 'ERROR':
      return 'danger';
    case 'SKIP':
    case 'ABORTED':
    case 'INGEST_FAILED':
      return 'muted';
    default:
      return 'muted';
  }
}

export function coverageLabel(status: CoverageStatus) {
  switch (status) {
    case 'GREEN':
      return 'Healthy';
    case 'AMBER':
      return 'At Risk';
    case 'RED':
      return 'Broken';
    case 'GREY':
      return 'Not Run';
    default:
      return status;
  }
}

export function originLabel(origin: DraftOrigin) {
  switch (origin) {
    case 'AI_DRAFT':
      return 'AI Draft';
    case 'IMPORTED':
      return 'Imported';
    case 'MANUAL':
      return 'Manual';
    default:
      return origin;
  }
}

export function truncateExcerpt(value: string | null | undefined, maxLength = 320) {
  if (!value) {
    return null;
  }
  if (value.length <= maxLength) {
    return value;
  }
  return `${value.slice(0, maxLength).trimEnd()}...`;
}

export function toUserMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError) {
    return error.message || fallback;
  }
  if (error instanceof Error && error.message.trim()) {
    return error.message;
  }
  return fallback;
}

export function toCoverageVars(status: CoverageStatus) {
  switch (status) {
    case 'GREEN':
      return {
        tone: 'var(--tm-coverage-green)',
        background: 'var(--tm-coverage-green-soft)',
      };
    case 'AMBER':
      return {
        tone: 'var(--tm-coverage-amber)',
        background: 'var(--tm-coverage-amber-soft)',
      };
    case 'RED':
      return {
        tone: 'var(--tm-coverage-red)',
        background: 'var(--tm-coverage-red-soft)',
      };
    case 'GREY':
    default:
      return {
        tone: 'var(--tm-coverage-grey)',
        background: 'var(--tm-coverage-grey-soft)',
      };
  }
}
