import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Policy, PolicyException, LoadState } from '../shared/types';
import { MOCK_POLICIES, MOCK_POLICY_EXCEPTIONS } from './mocks';
import { withMockLatency, PC_USE_MOCK } from '../shared/api';

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
      }
      status.value = 'ready';
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load policies';
      status.value = 'error';
    }
  }

  function selectPolicy(id: string | null) { selectedId.value = id; }

  return { status, error, items, selectedId, exceptions, tableState, selectedPolicy, selectedExceptions, fetchPolicies, selectPolicy };
});
