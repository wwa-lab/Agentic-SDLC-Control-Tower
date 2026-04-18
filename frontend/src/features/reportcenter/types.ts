export type ReportCategory = 'efficiency' | 'quality' | 'stability' | 'governance' | 'ai-contribution';
export type ReportScope = 'org' | 'workspace' | 'project';
export type ChartType = 'histogram' | 'stacked-bar' | 'grouped-bar' | 'heatmap' | 'horizontal-bar';
export type TimeRangePreset = 'last7d' | 'last30d' | 'last90d' | 'qtd' | 'ytd' | 'custom';
export type ExportFormat = 'csv' | 'pdf';
export type ExportStatus = 'queued' | 'generating' | 'ready' | 'failed' | 'expired';
export type DrilldownValue = string | number | null;

export interface DrilldownColumnSpec {
  key: string;
  label: string;
  type: 'string' | 'number' | 'date' | 'duration';
  format?: string | null;
}

export interface ReportDefinitionDto {
  reportKey: string;
  category: ReportCategory;
  name: string;
  description: string;
  supportedScopes: ReportScope[];
  supportedGroupings: string[];
  defaultGrouping: string;
  chartType: ChartType;
  drilldownColumns: DrilldownColumnSpec[];
  status: 'enabled' | 'coming-soon';
}

export interface CatalogCategoryGroup {
  category: ReportCategory;
  label: string;
  reports: ReportDefinitionDto[];
}

export interface CatalogDto {
  categories: CatalogCategoryGroup[];
}

export interface TimeRange {
  preset: TimeRangePreset;
  startAt?: string;
  endAt?: string;
}

export interface ReportRunRequest {
  scope: ReportScope;
  scopeIds: string[];
  timeRange: TimeRange;
  grouping: string;
  extraFilters?: Record<string, unknown>;
}

export interface SectionResult<T> {
  data: T | null;
  error: string | null;
}

export interface HeadlineMetric {
  key: string;
  label: string;
  value: string;
  numericValue: number;
  trend?: number | null;
  trendIsPositive?: boolean | null;
}

export interface SeriesPoint {
  groupKey: string;
  groupLabel: string;
  x: string | number;
  y: number;
  secondary?: Record<string, number>;
}

export interface DrilldownRow {
  [key: string]: DrilldownValue;
}

export interface DrilldownResult {
  columns: DrilldownColumnSpec[];
  rows: DrilldownRow[];
  totalRows: number;
}

export interface ReportRunResult {
  reportKey: string;
  snapshotAt: string;
  scope: ReportScope;
  scopeIds: string[];
  timeRange: TimeRange;
  grouping: string;
  headline: SectionResult<HeadlineMetric[]>;
  series: SectionResult<SeriesPoint[]>;
  drilldown: SectionResult<DrilldownResult>;
  slow?: boolean | null;
}

export interface ExportJobDto {
  id: string;
  reportKey: string;
  format: ExportFormat;
  status: ExportStatus;
  downloadUrl?: string | null;
  errorMessage?: string | null;
  bytes?: number | null;
  createdAt: string;
  readyAt?: string | null;
  expiresAt?: string | null;
}

export interface ReportRunHistoryEntry {
  runId: string;
  reportKey: string;
  reportName: string;
  scope: ReportScope;
  scopeSummary: string;
  timeRangeLabel: string;
  grouping: string;
  runAt: string;
  durationMs: number;
}

export interface ReportExportHistoryEntry {
  exportId: string;
  reportKey: string;
  reportName: string;
  format: ExportFormat;
  status: ExportStatus;
  createdAt: string;
  expiresAt?: string | null;
  downloadUrl?: string | null;
  bytes?: number | null;
}

export interface ReportScopeOption {
  id: string;
  label: string;
}
