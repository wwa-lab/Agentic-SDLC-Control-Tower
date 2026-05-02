import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { CursorPage, Policy, PolicyException, LoadState } from '../shared/types';
import { MOCK_POLICIES, MOCK_POLICY_EXCEPTIONS } from './mocks';
import { withMockLatency, PC_USE_MOCK, pcDelete, pcGet, pcPost, pcPut } from '../shared/api';

export interface UpsertPolicyPayload {
  key: string;
  name: string;
  category: Policy['category'];
  scopeType: Policy['scopeType'];
  scopeId: string;
  boundTo: string | null;
  status: Policy['status'];
  body: Record<string, unknown>;
}

export interface CreatePolicyExceptionPayload {
  reason: string;
  requesterId: string;
  approverId: string;
  expiresAt: string;
}

export const usePoliciesStore = defineStore('platform-policies', () => {
  const status = ref<LoadState>('idle');
  const error = ref<string | null>(null);
  const items = ref<Policy[]>([]);
  const selectedId = ref<string | null>(null);
  const exceptions = ref<PolicyException[]>([]);

  const tableState = computed(() => {
    if (status.value === 'loading') return 'loading' as const;
    if (status.value === 'error') return 'error' as const;
    if (items.value.length === 0) return 'empty' as const;
    return 'normal' as const;
  });

  const selectedPolicy = computed(() =>
    items.value.find(p => p.id === selectedId.value) ?? null
  );

  const selectedExceptions = computed(() =>
    exceptions.value.filter(e => e.policyId === selectedId.value)
  );

  async function fetchPolicies() {
    status.value = 'loading';
    error.value = null;
    try {
      if (PC_USE_MOCK) {
        items.value = await withMockLatency(() => MOCK_POLICIES);
        exceptions.value = MOCK_POLICY_EXCEPTIONS;
      } else {
        items.value = (await pcGet<CursorPage<Policy>>('/policies')).data;
      }
      status.value = 'ready';
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load policies';
      status.value = 'error';
    }
  }

  async function fetchDetail(id: string) {
    if (PC_USE_MOCK) return;
    const detail = await pcGet<Policy>(`/policies/${id}`);
    items.value = items.value.some(p => p.id === id)
      ? items.value.map(p => p.id === id ? detail : p)
      : [detail, ...items.value];
  }

  async function fetchExceptions(policyId: string) {
    if (PC_USE_MOCK) return;
    exceptions.value = exceptions.value.filter(e => e.policyId !== policyId);
    const rows = await pcGet<PolicyException[]>(`/policies/${policyId}/exceptions`);
    exceptions.value = [...exceptions.value, ...rows];
  }

  async function selectPolicy(id: string | null) {
    selectedId.value = id;
    if (id) {
      await fetchDetail(id);
      await fetchExceptions(id);
    }
  }

  async function createPolicy(payload: UpsertPolicyPayload) {
    const created = await pcPost<Policy>('/policies', payload);
    items.value = [created, ...items.value.filter(p => p.id !== created.id)];
    selectedId.value = created.id;
    return created;
  }

  async function updatePolicy(id: string, payload: UpsertPolicyPayload) {
    const updated = await pcPut<Policy>(`/policies/${id}`, payload);
    await fetchPolicies();
    selectedId.value = updated.id;
    return updated;
  }

  async function activatePolicy(id: string) {
    const updated = await pcPost<Policy>(`/policies/${id}/activate`);
    items.value = items.value.map(p => p.id === updated.id ? updated : p);
    return updated;
  }

  async function deactivatePolicy(id: string) {
    const updated = await pcPost<Policy>(`/policies/${id}/deactivate`);
    items.value = items.value.map(p => p.id === updated.id ? updated : p);
    return updated;
  }

  async function addException(policyId: string, payload: CreatePolicyExceptionPayload) {
    const created = await pcPost<PolicyException>(`/policies/${policyId}/exceptions`, payload);
    exceptions.value = [created, ...exceptions.value.filter(e => e.id !== created.id)];
    return created;
  }

  async function revokeException(policyId: string, exceptionId: string) {
    await pcDelete(`/policies/${policyId}/exceptions/${exceptionId}`);
    await fetchExceptions(policyId);
  }

  return {
    status,
    error,
    items,
    selectedId,
    exceptions,
    tableState,
    selectedPolicy,
    selectedExceptions,
    fetchPolicies,
    fetchDetail,
    fetchExceptions,
    selectPolicy,
    createPolicy,
    updatePolicy,
    activatePolicy,
    deactivatePolicy,
    addException,
    revokeException,
  };
});
