import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { ApiError } from '@/shared/api/client';
import { getMockCatalog, getMockExportHistory, getMockHistory, getMockRunResult, requestMockExport, buildMockHistoryEntry } from '../api/mockReports';
import { reportCenterApi } from '../api/reportCenterApi';
import type {
  CatalogDto,
  ExportFormat,
  ExportJobDto,
  ReportDefinitionDto,
  ReportExportHistoryEntry,
  ReportRunHistoryEntry,
  ReportRunRequest,
  ReportRunResult,
} from '../types';
import { defaultRequestForDefinition, findDefinition } from '../utils';

function toMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError && error.message) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

export const useReportCenterStore = defineStore('reportCenter', () => {
  const useMockData = ref(false);
  const catalog = ref<CatalogDto | null>(null);
  const catalogLoading = ref(false);
  const catalogError = ref<string | null>(null);

  const activeDefinition = ref<ReportDefinitionDto | null>(null);
  const activeRequest = ref<ReportRunRequest | null>(null);
  const activeResult = ref<ReportRunResult | null>(null);
  const runLoading = ref(false);
  const runError = ref<string | null>(null);

  const history = ref<ReportRunHistoryEntry[]>([]);
  const historyLoading = ref(false);
  const historyError = ref<string | null>(null);

  const exportsHistory = ref<ReportExportHistoryEntry[]>([]);
  const exportsLoading = ref(false);
  const exportsError = ref<string | null>(null);

  const exportJob = ref<ExportJobDto | null>(null);
  const exportLoading = ref(false);
  const exportError = ref<string | null>(null);

  const hasLoadedCatalog = computed(() => catalog.value !== null);

  async function fetchCatalog(force = false) {
    if (catalog.value && !force) {
      return catalog.value;
    }
    catalogLoading.value = true;
    catalogError.value = null;
    try {
      catalog.value = useMockData.value ? getMockCatalog() : await reportCenterApi.getCatalog();
      return catalog.value;
    } catch (error) {
      catalog.value = null;
      catalogError.value = toMessage(error, 'Failed to load report catalog.');
      throw error;
    } finally {
      catalogLoading.value = false;
    }
  }

  async function selectReport(reportKey: string) {
    await fetchCatalog();
    activeDefinition.value = findDefinition(catalog.value, reportKey);
    if (!activeDefinition.value) {
      throw new Error(`Report ${reportKey} not found`);
    }
    if (!activeRequest.value || activeResult.value?.reportKey !== reportKey) {
      activeRequest.value = defaultRequestForDefinition(activeDefinition.value);
    }
    activeResult.value = null;
    runError.value = null;
    return activeDefinition.value;
  }

  function setActiveRequest(request: ReportRunRequest) {
    activeRequest.value = {
      ...request,
      scopeIds: [...request.scopeIds],
      timeRange: { ...request.timeRange },
      extraFilters: request.extraFilters ? { ...request.extraFilters } : {},
    };
  }

  function resetActiveRequest() {
    if (activeDefinition.value) {
      activeRequest.value = defaultRequestForDefinition(activeDefinition.value);
    }
  }

  async function runReport(reportKey: string, request: ReportRunRequest) {
    activeRequest.value = request;
    runLoading.value = true;
    runError.value = null;
    try {
      activeResult.value = useMockData.value
        ? await getMockRunResult(reportKey, request)
        : await reportCenterApi.runReport(reportKey, request);

      if (activeDefinition.value) {
        const entry = buildMockHistoryEntry(activeResult.value, activeDefinition.value);
        history.value = [entry, ...history.value].slice(0, 50);
      }
      return activeResult.value;
    } catch (error) {
      activeResult.value = null;
      runError.value = toMessage(error, 'Failed to run report.');
      throw error;
    } finally {
      runLoading.value = false;
    }
  }

  async function fetchHistory(reportKey?: string) {
    historyLoading.value = true;
    historyError.value = null;
    try {
      history.value = useMockData.value ? getMockHistory() : await reportCenterApi.getHistory(reportKey);
      return history.value;
    } catch (error) {
      history.value = [];
      historyError.value = toMessage(error, 'Failed to load report history.');
      throw error;
    } finally {
      historyLoading.value = false;
    }
  }

  async function fetchExportHistory() {
    exportsLoading.value = true;
    exportsError.value = null;
    try {
      exportsHistory.value = useMockData.value ? getMockExportHistory() : await reportCenterApi.getExportHistory();
      return exportsHistory.value;
    } catch (error) {
      exportsHistory.value = [];
      exportsError.value = toMessage(error, 'Failed to load export history.');
      throw error;
    } finally {
      exportsLoading.value = false;
    }
  }

  async function requestExport(reportKey: string, request: ReportRunRequest, format: ExportFormat) {
    exportLoading.value = true;
    exportError.value = null;
    try {
      exportJob.value = useMockData.value
        ? await requestMockExport(reportKey, request, format)
        : await reportCenterApi.exportReport(reportKey, request, format);
      return exportJob.value;
    } catch (error) {
      exportJob.value = null;
      exportError.value = toMessage(error, 'Failed to request export.');
      throw error;
    } finally {
      exportLoading.value = false;
    }
  }

  async function pollExport(exportId: string) {
    if (useMockData.value && exportJob.value?.id === exportId) {
      return exportJob.value;
    }
    exportJob.value = await reportCenterApi.getExport(exportId);
    return exportJob.value;
  }

  function resetDetail() {
    activeDefinition.value = null;
    activeRequest.value = null;
    activeResult.value = null;
    runLoading.value = false;
    runError.value = null;
    exportJob.value = null;
    exportLoading.value = false;
    exportError.value = null;
  }

  function resetAll() {
    catalog.value = null;
    catalogLoading.value = false;
    catalogError.value = null;
    history.value = [];
    historyLoading.value = false;
    historyError.value = null;
    exportsHistory.value = [];
    exportsLoading.value = false;
    exportsError.value = null;
    resetDetail();
  }

  return {
    useMockData,
    catalog,
    catalogLoading,
    catalogError,
    activeDefinition,
    activeRequest,
    activeResult,
    runLoading,
    runError,
    history,
    historyLoading,
    historyError,
    exportsHistory,
    exportsLoading,
    exportsError,
    exportJob,
    exportLoading,
    exportError,
    hasLoadedCatalog,
    fetchCatalog,
    selectReport,
    setActiveRequest,
    resetActiveRequest,
    runReport,
    fetchHistory,
    fetchExportHistory,
    requestExport,
    pollExport,
    resetDetail,
    resetAll,
  };
});
