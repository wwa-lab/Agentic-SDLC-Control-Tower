import { EXECUTION_STATUS_LABELS, SKILL_STATUS_LABELS, STATUS_COLOR_TOKENS } from './constants';
import type { ExecutionStatus, SkillStatus } from './types';

export function formatPercent(value: number | null | undefined): string {
  if (value == null) {
    return 'n/a';
  }
  return `${Math.round(value * 100)}%`;
}

export function formatMetricValue(value: number, unit: '%' | 'hours' | 'count'): string {
  if (unit === '%') {
    return `${value.toFixed(1)}%`;
  }
  if (unit === 'hours') {
    return `${value.toFixed(1)}h`;
  }
  return `${Math.round(value)}`;
}

export function formatDelta(value: number, unit: '%' | 'hours' | 'count'): string {
  const sign = value > 0 ? '+' : value < 0 ? '-' : '';
  const absolute = Math.abs(value);
  if (unit === '%') {
    return `${sign}${absolute.toFixed(1)} pts`;
  }
  if (unit === 'hours') {
    return `${sign}${absolute.toFixed(1)}h`;
  }
  return `${sign}${Math.round(absolute)}`;
}

export function formatDuration(durationMs: number | null | undefined): string {
  if (durationMs == null) {
    return 'In progress';
  }
  if (durationMs < 60_000) {
    return `${Math.round(durationMs / 1000)}s`;
  }
  if (durationMs < 3_600_000) {
    return `${Math.round(durationMs / 60_000)}m`;
  }
  return `${(durationMs / 3_600_000).toFixed(1)}h`;
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return 'n/a';
  }
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

export function formatRelativeTime(value: string | null | undefined): string {
  if (!value) {
    return 'Never';
  }

  const diff = new Date(value).getTime() - Date.now();
  const minutes = Math.round(diff / 60_000);
  const formatter = new Intl.RelativeTimeFormat('en', { numeric: 'auto' });

  if (Math.abs(minutes) < 60) {
    return formatter.format(minutes, 'minute');
  }

  const hours = Math.round(minutes / 60);
  if (Math.abs(hours) < 24) {
    return formatter.format(hours, 'hour');
  }

  const days = Math.round(hours / 24);
  return formatter.format(days, 'day');
}

export function statusLabel(status: SkillStatus | ExecutionStatus): string {
  if (status in SKILL_STATUS_LABELS) {
    return SKILL_STATUS_LABELS[status as SkillStatus];
  }
  return EXECUTION_STATUS_LABELS[status as ExecutionStatus];
}

export function statusColor(status: SkillStatus | ExecutionStatus): string {
  return STATUS_COLOR_TOKENS[status];
}

export function prettyJson(value: Record<string, unknown>): string {
  return JSON.stringify(value, null, 2);
}
