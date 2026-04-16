import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { DashboardSummary } from '../types/dashboard';
import { dashboardApi } from '../api/dashboardApi';
import { MOCK_DASHBOARD_DATA } from '../mockData';

const USE_MOCK = import.meta.env.DEV && !import.meta.env.VITE_USE_BACKEND;

export const useDashboardStore = defineStore('dashboard', () => {
  const summary = ref<DashboardSummary | null>(null);
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  async function fetchSummary() {
    isLoading.value = true;
    error.value = null;
    try {
      summary.value = USE_MOCK
        ? MOCK_DASHBOARD_DATA
        : await dashboardApi.getSummary();
    } catch (err) {
      console.error('Failed to fetch dashboard summary:', err);
      error.value = 'Failed to load dashboard data. Please try again later.';
    } finally {
      isLoading.value = false;
    }
  }

  return {
    summary,
    isLoading,
    error,
    fetchSummary
  };
});
