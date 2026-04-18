import { fetchJson, postJson } from '@/shared/api/client';
import type {
  CatalogDto,
  ExportFormat,
  ExportJobDto,
  ReportExportHistoryEntry,
  ReportRunHistoryEntry,
  ReportRunRequest,
  ReportRunResult,
} from '../types';

const BASE = '/reports';

export const reportCenterApi = {
  getCatalog(): Promise<CatalogDto> {
    return fetchJson<CatalogDto>(`${BASE}/catalog`);
  },

  runReport(reportKey: string, request: ReportRunRequest): Promise<ReportRunResult> {
    return postJson<ReportRunResult>(`${BASE}/${encodeURIComponent(reportKey)}/run`, request);
  },

  exportReport(reportKey: string, request: ReportRunRequest, format: ExportFormat): Promise<ExportJobDto> {
    return postJson<ExportJobDto>(`${BASE}/${encodeURIComponent(reportKey)}/export?format=${encodeURIComponent(format)}`, request);
  },

  getExport(exportId: string): Promise<ExportJobDto> {
    return fetchJson<ExportJobDto>(`${BASE}/exports/${encodeURIComponent(exportId)}`);
  },

  getHistory(reportKey?: string): Promise<ReportRunHistoryEntry[]> {
    const query = reportKey ? `?reportKey=${encodeURIComponent(reportKey)}` : '';
    return fetchJson<ReportRunHistoryEntry[]>(`${BASE}/history${query}`);
  },

  getExportHistory(): Promise<ReportExportHistoryEntry[]> {
    return fetchJson<ReportExportHistoryEntry[]>(`${BASE}/exports`);
  },
};
