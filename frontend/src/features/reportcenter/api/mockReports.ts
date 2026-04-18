import type {
  CatalogDto,
  ExportJobDto,
  ReportDefinitionDto,
  ReportExportHistoryEntry,
  ReportRunHistoryEntry,
  ReportRunRequest,
  ReportRunResult,
  SeriesPoint,
} from '../types';
import { buildScopeSummary, buildTimeRangeLabel, formatMinutes } from '../utils';

const enabledReports: ReportDefinitionDto[] = [
  {
    reportKey: 'eff.lead-time',
    category: 'efficiency',
    name: 'Delivery Lead Time',
    description: 'Time from requirement ready to deploy.',
    supportedScopes: ['org', 'workspace', 'project'],
    supportedGroupings: ['team', 'project', 'requirementType'],
    defaultGrouping: 'team',
    chartType: 'histogram',
    drilldownColumns: [
      { key: 'team', label: 'Team', type: 'string', format: null },
      { key: 'p50', label: 'Median (min)', type: 'duration', format: 'minutes' },
      { key: 'p75', label: 'p75 (min)', type: 'duration', format: 'minutes' },
      { key: 'p95', label: 'p95 (min)', type: 'duration', format: 'minutes' },
    ],
    status: 'enabled',
  },
  {
    reportKey: 'eff.cycle-time',
    category: 'efficiency',
    name: 'Cycle Time by Stage',
    description: 'Time in each SDLC stage.',
    supportedScopes: ['org', 'workspace', 'project'],
    supportedGroupings: ['stage', 'team', 'project'],
    defaultGrouping: 'stage',
    chartType: 'stacked-bar',
    drilldownColumns: [
      { key: 'stage', label: 'Stage', type: 'string', format: null },
      { key: 'team', label: 'Team', type: 'string', format: null },
      { key: 'avgMin', label: 'Avg (min)', type: 'duration', format: 'minutes' },
    ],
    status: 'enabled',
  },
  {
    reportKey: 'eff.throughput',
    category: 'efficiency',
    name: 'Throughput',
    description: 'Items completed per week.',
    supportedScopes: ['org', 'workspace', 'project'],
    supportedGroupings: ['week-team', 'week-project'],
    defaultGrouping: 'week-team',
    chartType: 'grouped-bar',
    drilldownColumns: [
      { key: 'weekStart', label: 'Week', type: 'date', format: 'iso8601' },
      { key: 'group', label: 'Group', type: 'string', format: null },
      { key: 'count', label: 'Count', type: 'number', format: null },
    ],
    status: 'enabled',
  },
  {
    reportKey: 'eff.wip',
    category: 'efficiency',
    name: 'Work In Progress',
    description: 'Active items per stage with aging buckets.',
    supportedScopes: ['org', 'workspace', 'project'],
    supportedGroupings: ['stage-team', 'owner-team'],
    defaultGrouping: 'stage-team',
    chartType: 'heatmap',
    drilldownColumns: [
      { key: 'stage', label: 'Stage', type: 'string', format: null },
      { key: 'team', label: 'Team', type: 'string', format: null },
      { key: 'ageBucket', label: 'Age', type: 'string', format: null },
      { key: 'count', label: 'Count', type: 'number', format: null },
    ],
    status: 'enabled',
  },
  {
    reportKey: 'eff.flow-efficiency',
    category: 'efficiency',
    name: 'Flow Efficiency',
    description: 'Active work time divided by total cycle time per stage.',
    supportedScopes: ['org', 'workspace', 'project'],
    supportedGroupings: ['stage', 'team'],
    defaultGrouping: 'stage',
    chartType: 'horizontal-bar',
    drilldownColumns: [
      { key: 'stage', label: 'Stage', type: 'string', format: null },
      { key: 'flowEff', label: 'Flow Eff.', type: 'number', format: 'percent-1' },
      { key: 'activeMin', label: 'Active (min)', type: 'duration', format: 'minutes' },
      { key: 'totalMin', label: 'Total (min)', type: 'duration', format: 'minutes' },
    ],
    status: 'enabled',
  },
];

export function getMockCatalog(): CatalogDto {
  return {
    categories: [
      { category: 'efficiency', label: 'Efficiency', reports: enabledReports },
      { category: 'quality', label: 'Quality', reports: [] },
      { category: 'stability', label: 'Stability', reports: [] },
      { category: 'governance', label: 'Governance', reports: [] },
      { category: 'ai-contribution', label: 'AI Contribution', reports: [] },
    ],
  };
}

function sectionError<T>(message: string) {
  return { data: null as T | null, error: message };
}

function makeLeadTimeSeries(grouping: string): SeriesPoint[] {
  const groupLabel = grouping === 'project' ? 'Gateway Migration' : grouping === 'requirementType' ? 'Business' : 'Team Alpha';
  const groupKey = grouping === 'project' ? 'proj-42' : grouping === 'requirementType' ? 'business' : 'team-alpha';
  return [
    { groupKey, groupLabel, x: 0, y: 3 },
    { groupKey, groupLabel, x: 1440, y: 12 },
    { groupKey, groupLabel, x: 2880, y: 18 },
    { groupKey, groupLabel, x: 4320, y: 9 },
    { groupKey, groupLabel, x: 5760, y: 4 },
  ];
}

function makeResult(reportKey: string, request: ReportRunRequest): ReportRunResult {
  const definition = enabledReports.find(report => report.reportKey === reportKey);
  if (!definition) {
    throw new Error(`Unknown report ${reportKey}`);
  }

  const base = {
    reportKey,
    snapshotAt: '2026-04-18T10:04:12Z',
    scope: request.scope,
    scopeIds: request.scopeIds,
    timeRange: request.timeRange,
    grouping: request.grouping,
    slow: reportKey === 'eff.wip',
  };

  const mockSectionError = request.extraFilters?.mockSectionError;

  if (reportKey === 'eff.lead-time') {
    return {
      ...base,
      headline: {
        data: [
          { key: 'p50', label: 'Median lead time', value: '3d 4h', numericValue: 4560, trend: -8.2, trendIsPositive: true },
          { key: 'p95', label: 'p95 lead time', value: '9d 2h', numericValue: 13200, trend: 2.1, trendIsPositive: false },
          { key: 'vol', label: 'Items completed', value: '184', numericValue: 184, trend: 6.4, trendIsPositive: true },
        ],
        error: mockSectionError === 'headline' ? 'Headline builder failed in mock mode.' : null,
      },
      series: mockSectionError === 'series'
        ? sectionError('Chart series builder failed in mock mode.')
        : { data: makeLeadTimeSeries(request.grouping), error: null },
      drilldown: mockSectionError === 'drilldown'
        ? sectionError('Drilldown builder failed in mock mode.')
        : {
            data: {
              columns: definition.drilldownColumns,
              rows: [
                { team: 'Team Alpha', p50: 4200, p75: 8100, p95: 13200 },
                { team: 'Team Beta', p50: 5100, p75: 9300, p95: 15100 },
              ],
              totalRows: 2,
            },
            error: null,
          },
    };
  }

  if (reportKey === 'eff.cycle-time') {
    return {
      ...base,
      headline: {
        data: [
          { key: 'avg', label: 'Average cycle time', value: '18h', numericValue: 1080, trend: -4.4, trendIsPositive: true },
          { key: 'review', label: 'Review bottleneck', value: '26%', numericValue: 26, trend: 3.2, trendIsPositive: false },
          { key: 'handoff', label: 'Handoffs', value: '31', numericValue: 31, trend: -1.3, trendIsPositive: true },
        ],
        error: null,
      },
      series: {
        data: [
          { groupKey: 'team-alpha', groupLabel: 'Team Alpha', x: 'Discovery', y: 210 },
          { groupKey: 'team-alpha', groupLabel: 'Team Alpha', x: 'Build', y: 320 },
          { groupKey: 'team-alpha', groupLabel: 'Team Alpha', x: 'Review', y: 280 },
          { groupKey: 'team-beta', groupLabel: 'Team Beta', x: 'Discovery', y: 180 },
          { groupKey: 'team-beta', groupLabel: 'Team Beta', x: 'Build', y: 360 },
          { groupKey: 'team-beta', groupLabel: 'Team Beta', x: 'Review', y: 340 },
        ],
        error: null,
      },
      drilldown: {
        data: {
          columns: definition.drilldownColumns,
          rows: [
            { stage: 'Discovery', team: 'Team Alpha', avgMin: 210 },
            { stage: 'Build', team: 'Team Alpha', avgMin: 320 },
            { stage: 'Review', team: 'Team Beta', avgMin: 340 },
          ],
          totalRows: 3,
        },
        error: null,
      },
    };
  }

  if (reportKey === 'eff.throughput') {
    return {
      ...base,
      headline: {
        data: [
          { key: 'week', label: 'Latest week', value: '24', numericValue: 24, trend: 12.2, trendIsPositive: true },
          { key: 'avg', label: 'Weekly average', value: '18', numericValue: 18, trend: 5.5, trendIsPositive: true },
          { key: 'variance', label: 'Variance', value: 'Low', numericValue: 1, trend: -2.3, trendIsPositive: true },
        ],
        error: null,
      },
      series: {
        data: [
          { groupKey: 'team-alpha', groupLabel: 'Team Alpha', x: '2026-03-23', y: 12 },
          { groupKey: 'team-beta', groupLabel: 'Team Beta', x: '2026-03-23', y: 7 },
          { groupKey: 'team-alpha', groupLabel: 'Team Alpha', x: '2026-03-30', y: 15 },
          { groupKey: 'team-beta', groupLabel: 'Team Beta', x: '2026-03-30', y: 9 },
          { groupKey: 'team-alpha', groupLabel: 'Team Alpha', x: '2026-04-06', y: 18 },
          { groupKey: 'team-beta', groupLabel: 'Team Beta', x: '2026-04-06', y: 11 },
        ],
        error: null,
      },
      drilldown: {
        data: {
          columns: definition.drilldownColumns,
          rows: [
            { weekStart: '2026-03-23T00:00:00Z', group: 'Team Alpha', count: 12 },
            { weekStart: '2026-03-30T00:00:00Z', group: 'Team Alpha', count: 15 },
            { weekStart: '2026-04-06T00:00:00Z', group: 'Team Beta', count: 11 },
          ],
          totalRows: 3,
        },
        error: null,
      },
    };
  }

  if (reportKey === 'eff.wip') {
    return {
      ...base,
      headline: {
        data: [
          { key: 'open', label: 'Open items', value: '47', numericValue: 47, trend: 8.1, trendIsPositive: false },
          { key: 'aged', label: 'Aged > 7d', value: '13', numericValue: 13, trend: 4.8, trendIsPositive: false },
          { key: 'blocked', label: 'Blocked stages', value: '2', numericValue: 2, trend: 0.0, trendIsPositive: null },
        ],
        error: null,
      },
      series: {
        data: [
          { groupKey: 'team-alpha', groupLabel: 'Team Alpha', x: 'Discovery|0-3d', y: 4 },
          { groupKey: 'team-alpha', groupLabel: 'Team Alpha', x: 'Discovery|3-7d', y: 2 },
          { groupKey: 'team-beta', groupLabel: 'Team Beta', x: 'Build|0-3d', y: 6 },
          { groupKey: 'team-beta', groupLabel: 'Team Beta', x: 'Review|7-14d', y: 5 },
          { groupKey: 'team-gamma', groupLabel: 'Team Gamma', x: 'Deploy|14d+', y: 3 },
        ],
        error: null,
      },
      drilldown: {
        data: {
          columns: definition.drilldownColumns,
          rows: [
            { stage: 'Discovery', team: 'Team Alpha', ageBucket: '0-3d', count: 4 },
            { stage: 'Build', team: 'Team Beta', ageBucket: '0-3d', count: 6 },
            { stage: 'Review', team: 'Team Beta', ageBucket: '7-14d', count: 5 },
          ],
          totalRows: 3,
        },
        error: null,
      },
    };
  }

  return {
    ...base,
    headline: {
      data: [
        { key: 'avg', label: 'Average flow efficiency', value: '46.3%', numericValue: 46.3, trend: 2.4, trendIsPositive: true },
        { key: 'best', label: 'Best stage', value: 'Deploy', numericValue: 1, trend: null, trendIsPositive: null },
        { key: 'drag', label: 'Largest drag', value: 'Review', numericValue: 1, trend: null, trendIsPositive: null },
      ],
      error: null,
    },
    series: {
      data: [
        { groupKey: 'stage-discovery', groupLabel: 'Discovery', x: 'Discovery', y: 52.1 },
        { groupKey: 'stage-build', groupLabel: 'Build', x: 'Build', y: 47.5 },
        { groupKey: 'stage-review', groupLabel: 'Review', x: 'Review', y: 31.8 },
        { groupKey: 'stage-deploy', groupLabel: 'Deploy', x: 'Deploy', y: 63.4 },
      ],
      error: null,
    },
    drilldown: {
      data: {
        columns: definition.drilldownColumns,
        rows: [
          { stage: 'Discovery', flowEff: 52.1, activeMin: 640, totalMin: 1228 },
          { stage: 'Build', flowEff: 47.5, activeMin: 910, totalMin: 1915 },
          { stage: 'Review', flowEff: 31.8, activeMin: 390, totalMin: 1225 },
        ],
        totalRows: 3,
      },
      error: null,
    },
  };
}

export async function getMockRunResult(reportKey: string, request: ReportRunRequest): Promise<ReportRunResult> {
  await new Promise(resolve => window.setTimeout(resolve, 200));
  const result = makeResult(reportKey, request);
  if (result.headline.data) {
    result.headline.data = result.headline.data.map(metric => ({
      ...metric,
      value: metric.key.includes('p') ? formatMinutes(metric.numericValue) : metric.value,
    }));
  }
  return result;
}

export function getMockHistory(): ReportRunHistoryEntry[] {
  return [
    {
      runId: 'run-01HAA1',
      reportKey: 'eff.lead-time',
      reportName: 'Delivery Lead Time',
      scope: 'workspace',
      scopeSummary: 'Workspace: 2',
      timeRangeLabel: 'Last 30 days',
      grouping: 'team',
      runAt: '2026-04-18T10:04:12Z',
      durationMs: 1412,
    },
    {
      runId: 'run-01HAA0',
      reportKey: 'eff.throughput',
      reportName: 'Throughput',
      scope: 'project',
      scopeSummary: 'Project: 4',
      timeRangeLabel: 'Last 7 days',
      grouping: 'week-team',
      runAt: '2026-04-17T14:18:05Z',
      durationMs: 883,
    },
  ];
}

export function getMockExportHistory(): ReportExportHistoryEntry[] {
  return [
    {
      exportId: 'exp-01HXYZ1234',
      reportKey: 'eff.lead-time',
      reportName: 'Delivery Lead Time',
      format: 'pdf',
      status: 'ready',
      createdAt: '2026-04-18T10:04:30Z',
      expiresAt: '2026-04-25T10:04:30Z',
      downloadUrl: '/api/v1/reports/exports/exp-01HXYZ1234/file?sig=mock',
      bytes: 482113,
    },
  ];
}

export async function requestMockExport(reportKey: string, request: ReportRunRequest, format: 'csv' | 'pdf'): Promise<ExportJobDto> {
  const createdAt = new Date().toISOString();
  return {
    id: `exp-${Date.now()}`,
    reportKey,
    format,
    status: 'ready',
    createdAt,
    readyAt: createdAt,
    expiresAt: '2026-04-25T10:04:30Z',
    downloadUrl: `/api/v1/reports/exports/mock-${reportKey.replace(/\./g, '-')}/file?sig=mock`,
    bytes: format === 'pdf' ? 482113 : 9328,
  };
}

export function buildMockHistoryEntry(result: ReportRunResult, definition: ReportDefinitionDto): ReportRunHistoryEntry {
  return {
    runId: `run-${Date.now()}`,
    reportKey: result.reportKey,
    reportName: definition.name,
    scope: result.scope,
    scopeSummary: buildScopeSummary(result.scope, result.scopeIds),
    timeRangeLabel: buildTimeRangeLabel(result.timeRange),
    grouping: result.grouping,
    runAt: new Date().toISOString(),
    durationMs: result.slow ? 2640 : 780,
  };
}
