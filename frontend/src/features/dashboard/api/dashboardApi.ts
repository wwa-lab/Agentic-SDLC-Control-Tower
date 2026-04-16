import { fetchJson } from '@/shared/api/client';
import type { DashboardSummary } from '../types/dashboard';

/**
 * Dashboard API client
 */
export const dashboardApi = {
  async getSummary(): Promise<DashboardSummary> {
    return fetchJson<DashboardSummary>('/dashboard/summary');
  }
};
