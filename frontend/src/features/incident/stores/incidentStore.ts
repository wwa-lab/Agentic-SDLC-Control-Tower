import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type {
  IncidentList,
  IncidentDetail,
  IncidentListItem,
  SeverityDistribution,
  IncidentFilters,
  IncidentAction,
  GovernanceEntry,
} from '../types/incident';
import type { SectionResult } from '@/shared/types/section';
import { incidentApi } from '../api/incidentApi';
import { MOCK_INCIDENT_LIST, MOCK_INCIDENT_DETAILS } from '../mockData';

const USE_MOCK = import.meta.env.DEV && !import.meta.env.VITE_USE_BACKEND;

const ACTIVE_STATUSES = new Set([
  'DETECTED', 'AI_INVESTIGATING', 'AI_DIAGNOSED', 'ACTION_PROPOSED',
  'PENDING_APPROVAL', 'EXECUTING', 'ESCALATED', 'MANUAL_OVERRIDE',
]);

export const useIncidentStore = defineStore('incident', () => {
  // ── List state ──
  const listData = ref<IncidentList | null>(null);
  const listLoading = ref(false);
  const listError = ref<string | null>(null);
  const filters = ref<IncidentFilters>({ showResolved: false });

  // ── Detail state ──
  const detail = ref<IncidentDetail | null>(null);
  const detailLoading = ref(false);
  const detailError = ref<string | null>(null);
  const selectedIncidentId = ref<string | null>(null);

  // ── Computed: filtered incidents ──
  const filteredIncidents = computed<ReadonlyArray<IncidentListItem>>(() => {
    if (!listData.value) return [];
    return listData.value.incidents.filter(inc => {
      if (!filters.value.showResolved && !ACTIVE_STATUSES.has(inc.status)) return false;
      if (filters.value.priority && inc.priority !== filters.value.priority) return false;
      if (filters.value.status && inc.status !== filters.value.status) return false;
      if (filters.value.handlerType && inc.handlerType !== filters.value.handlerType) return false;
      return true;
    });
  });

  const severityDistribution = computed<SeverityDistribution>(
    () => listData.value?.severityDistribution ?? { p1: 0, p2: 0, p3: 0, p4: 0 },
  );

  // ── Actions ──
  async function fetchIncidentList() {
    listLoading.value = true;
    listError.value = null;
    try {
      listData.value = USE_MOCK
        ? MOCK_INCIDENT_LIST
        : await incidentApi.getIncidentList();
    } catch (err) {
      console.error('Failed to fetch incident list:', err);
      listError.value = 'Failed to load incidents. Please try again later.';
    } finally {
      listLoading.value = false;
    }
  }

  async function fetchIncidentDetail(id: string) {
    detailLoading.value = true;
    detailError.value = null;
    selectedIncidentId.value = id;
    try {
      detail.value = USE_MOCK
        ? (MOCK_INCIDENT_DETAILS[id] ?? null)
        : await incidentApi.getIncidentDetail(id);
      if (!detail.value) {
        detailError.value = `Incident not found: ${id}`;
      }
    } catch (err) {
      console.error('Failed to fetch incident detail:', err);
      detailError.value = 'Failed to load incident detail. Please try again later.';
    } finally {
      detailLoading.value = false;
    }
  }

  async function approveAction(incidentId: string, actionId: string) {
    if (USE_MOCK) {
      _mockTransitionAction(actionId, 'approved', {
        actor: 'current.user',
        timestamp: new Date().toISOString(),
        actionTaken: 'approve',
        reason: null,
        policyRef: null,
      });
      return;
    }
    await incidentApi.approveAction(incidentId, actionId);
    await fetchIncidentDetail(incidentId);
  }

  async function rejectAction(incidentId: string, actionId: string, reason: string) {
    if (USE_MOCK) {
      _mockTransitionAction(actionId, 'rejected', {
        actor: 'current.user',
        timestamp: new Date().toISOString(),
        actionTaken: 'reject',
        reason,
        policyRef: null,
      });
      return;
    }
    await incidentApi.rejectAction(incidentId, actionId, reason);
    await fetchIncidentDetail(incidentId);
  }

  function setFilters(newFilters: Partial<IncidentFilters>) {
    filters.value = { ...filters.value, ...newFilters };
  }

  function clearDetail() {
    detail.value = null;
    detailError.value = null;
    selectedIncidentId.value = null;
  }

  // ── Mock helpers (local state mutation for Phase A) ──
  function _mockTransitionAction(
    actionId: string,
    newStatus: IncidentAction['executionStatus'],
    govEntry: GovernanceEntry,
  ) {
    if (!detail.value) return;
    const actionsSection = detail.value.actions;
    const govSection = detail.value.governance;
    if (!actionsSection.data || !govSection.data) return;

    const updatedActions = actionsSection.data.actions.map(a =>
      a.id === actionId ? { ...a, executionStatus: newStatus } : a,
    );
    const updatedGov = [...govSection.data.entries, govEntry];

    detail.value = {
      ...detail.value,
      actions: { data: { actions: updatedActions }, error: null } as SectionResult<typeof actionsSection.data>,
      governance: { data: { entries: updatedGov }, error: null } as SectionResult<typeof govSection.data>,
    };
  }

  return {
    // List
    listData,
    listLoading,
    listError,
    filters,
    filteredIncidents,
    severityDistribution,
    // Detail
    detail,
    detailLoading,
    detailError,
    selectedIncidentId,
    // Actions
    fetchIncidentList,
    fetchIncidentDetail,
    approveAction,
    rejectAction,
    setFilters,
    clearDetail,
  };
});
