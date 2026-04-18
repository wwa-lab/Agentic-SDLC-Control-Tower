import type {
  DrilldownColumnSpec,
  DrilldownValue,
  ReportDefinitionDto,
  ReportRunHistoryEntry,
  ReportRunRequest,
  ReportScope,
  ReportScopeOption,
  TimeRange,
  TimeRangePreset,
} from './types';

export const REPORT_SCOPE_OPTIONS: Record<ReportScope, ReportScopeOption[]> = {
  org: [
    { id: 'org-default-001', label: 'Global SDLC Organization' },
  ],
  workspace: [
    { id: 'ws-default-001', label: 'Global SDLC Tower' },
    { id: 'ws-legacy-001', label: 'Legacy Mainframe' },
    { id: 'ws-degraded-001', label: 'Degraded Feed Workspace' },
    { id: 'ws-private-001', label: 'Restricted Finance Workspace' },
  ],
  project: [
    { id: 'proj-42', label: 'Gateway Migration' },
    { id: 'proj-11', label: 'Card Issuance' },
    { id: 'proj-55', label: 'Fraud Detection Expansion' },
    { id: 'proj-88', label: 'Legacy Queue Decommission' },
    { id: 'proj-07', label: 'Q1 Cost Reporting' },
    { id: 'proj-private-01', label: 'Restricted Treasury Controls' },
  ],
};

export const REPORT_PRESET_LABELS: Record<TimeRangePreset, string> = {
  last7d: 'Last 7 days',
  last30d: 'Last 30 days',
  last90d: 'Last 90 days',
  qtd: 'Quarter to date',
  ytd: 'Year to date',
  custom: 'Custom range',
};

export const REPORT_GROUPING_LABELS: Record<string, string> = {
  team: 'Team',
  project: 'Project',
  requirementType: 'Requirement type',
  stage: 'Stage',
  'week-team': 'Week by team',
  'week-project': 'Week by project',
  'stage-team': 'Stage by team',
  'owner-team': 'Owner by team',
};

export function defaultTimeRange(): TimeRange {
  return { preset: 'last30d' };
}

export function defaultRequestForDefinition(definition: ReportDefinitionDto): ReportRunRequest {
  const scope = definition.supportedScopes.includes('workspace')
    ? 'workspace'
    : definition.supportedScopes[0];
  return {
    scope,
    scopeIds: REPORT_SCOPE_OPTIONS[scope].slice(0, 1).map(option => option.id),
    timeRange: defaultTimeRange(),
    grouping: definition.defaultGrouping,
    extraFilters: {},
  };
}

export function findDefinition(catalog: { categories: Array<{ reports: ReportDefinitionDto[] }> } | null, reportKey: string) {
  return catalog?.categories.flatMap(category => category.reports).find(report => report.reportKey === reportKey) ?? null;
}

export function requestToQuery(request: ReportRunRequest) {
  return {
    scope: request.scope,
    scopeIds: request.scopeIds.join(','),
    preset: request.timeRange.preset,
    startAt: request.timeRange.startAt,
    endAt: request.timeRange.endAt,
    grouping: request.grouping,
    mockSectionError: typeof request.extraFilters?.mockSectionError === 'string'
      ? String(request.extraFilters.mockSectionError)
      : undefined,
  };
}

function parsePreset(value: unknown): TimeRangePreset {
  if (
    value === 'last7d' ||
    value === 'last30d' ||
    value === 'last90d' ||
    value === 'qtd' ||
    value === 'ytd' ||
    value === 'custom'
  ) {
    return value;
  }
  return 'last30d';
}

function parseScope(value: unknown): ReportScope {
  return value === 'org' || value === 'workspace' || value === 'project' ? value : 'workspace';
}

function isValidIso(value: string | undefined) {
  return Boolean(value && !Number.isNaN(Date.parse(value)));
}

export function requestFromQuery(definition: ReportDefinitionDto, query: Record<string, unknown>) {
  const base = defaultRequestForDefinition(definition);
  const scope = parseScope(query.scope);
  const rawScopeIds = typeof query.scopeIds === 'string' ? query.scopeIds : '';
  const scopeIds = rawScopeIds
    .split(',')
    .map(value => value.trim())
    .filter(Boolean);
  const timeRange: TimeRange = {
    preset: parsePreset(query.preset),
  };
  if (typeof query.startAt === 'string' && isValidIso(query.startAt)) {
    timeRange.startAt = query.startAt;
  }
  if (typeof query.endAt === 'string' && isValidIso(query.endAt)) {
    timeRange.endAt = query.endAt;
  }

  return {
    scope,
    scopeIds: scopeIds.length > 0 ? scopeIds : base.scopeIds,
    timeRange,
    grouping:
      typeof query.grouping === 'string' && definition.supportedGroupings.includes(query.grouping)
        ? query.grouping
        : base.grouping,
    extraFilters: typeof query.mockSectionError === 'string'
      ? { mockSectionError: query.mockSectionError }
      : {},
  } satisfies ReportRunRequest;
}

export function validateRequest(definition: ReportDefinitionDto, request: ReportRunRequest) {
  const errors: string[] = [];
  if (!request.scopeIds.length) {
    errors.push('Select at least one scope.');
  }
  if (request.scopeIds.length > 20) {
    errors.push('Choose 20 scopes or fewer.');
  }
  if (!definition.supportedScopes.includes(request.scope)) {
    errors.push(`Scope ${request.scope} is not supported by ${definition.name}.`);
  }
  if (!definition.supportedGroupings.includes(request.grouping)) {
    errors.push(`Grouping ${request.grouping} is not supported by ${definition.name}.`);
  }

  if (request.timeRange.preset === 'custom') {
    if (!request.timeRange.startAt || !request.timeRange.endAt) {
      errors.push('Custom range requires both start and end.');
    } else {
      const start = new Date(request.timeRange.startAt);
      const end = new Date(request.timeRange.endAt);
      const diff = end.getTime() - start.getTime();
      if (!(diff > 0)) {
        errors.push('Custom range start must be before end.');
      }
      if (diff > 366 * 24 * 60 * 60 * 1000) {
        errors.push('Custom range cannot exceed 366 days.');
      }
    }
  }

  return {
    valid: errors.length === 0,
    errors,
  };
}

export function formatMinutes(minutes: number) {
  if (!Number.isFinite(minutes)) {
    return '—';
  }
  const days = Math.floor(minutes / 1440);
  const hours = Math.floor((minutes % 1440) / 60);
  const mins = Math.round(minutes % 60);
  const parts = [];
  if (days > 0) {
    parts.push(`${days}d`);
  }
  if (hours > 0) {
    parts.push(`${hours}h`);
  }
  if (mins > 0 || parts.length === 0) {
    parts.push(`${mins}m`);
  }
  return parts.slice(0, 2).join(' ');
}

export function formatTimestamp(value: string | null | undefined) {
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

export function formatTrend(value: number | null | undefined) {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return '—';
  }
  return `${value > 0 ? '+' : ''}${value.toFixed(1)}%`;
}

export function formatDrilldownValue(column: DrilldownColumnSpec, value: DrilldownValue) {
  if (value == null) {
    return '—';
  }
  if (column.type === 'duration' && typeof value === 'number') {
    return column.format === 'minutes' ? formatMinutes(value) : `${value}`;
  }
  if (column.type === 'date' && typeof value === 'string') {
    return new Intl.DateTimeFormat('en', { month: 'short', day: 'numeric' }).format(new Date(value));
  }
  if (column.format === 'percent-1' && typeof value === 'number') {
    return `${value.toFixed(1)}%`;
  }
  return `${value}`;
}

export function buildScopeSummary(scope: ReportScope, scopeIds: string[]) {
  const label = scope.charAt(0).toUpperCase() + scope.slice(1);
  return `${label}: ${scopeIds.length}`;
}

export function buildTimeRangeLabel(timeRange: TimeRange) {
  return REPORT_PRESET_LABELS[timeRange.preset] ?? timeRange.preset;
}

export function prefillQueryFromHistory(entry: ReportRunHistoryEntry) {
  return {
    preset: labelToPreset(entry.timeRangeLabel),
    grouping: entry.grouping,
  };
}

function labelToPreset(label: string): TimeRangePreset {
  return (Object.entries(REPORT_PRESET_LABELS).find(([, value]) => value === label)?.[0] as TimeRangePreset | undefined) ?? 'last30d';
}
