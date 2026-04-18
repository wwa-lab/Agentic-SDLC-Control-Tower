import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Connection, AdapterDescriptor, ConnectionTestResult, LoadState } from '../shared/types';
import { MOCK_CONNECTIONS, MOCK_ADAPTERS, MOCK_TEST_RESULT_OK, MOCK_TEST_RESULT_FAIL } from './mocks';
import { withMockLatency, PC_USE_MOCK } from '../shared/api';

export const useIntegrationsStore = defineStore('platform-integrations', () => {
  const status = ref<LoadState>('idle');
  const error = ref<string | null>(null);
  const connections = ref<Connection[]>([]);
  const adapters = ref<AdapterDescriptor[]>([]);
  const selectedId = ref<string | null>(null);
  const testResult = ref<ConnectionTestResult | null>(null);
  const testLoading = ref(false);

  const tableState = computed(() => {
    if (status.value === 'loading') return 'loading' as const;
    if (status.value === 'error') return 'error' as const;
    if (connections.value.length === 0) return 'empty' as const;
    return 'normal' as const;
  });

  const selectedConnection = computed(() =>
    connections.value.find(c => c.id === selectedId.value) ?? null
  );

  async function fetchConnections() {
    status.value = 'loading';
    error.value = null;
    try {
      if (PC_USE_MOCK) {
        connections.value = await withMockLatency(() => MOCK_CONNECTIONS);
        adapters.value = MOCK_ADAPTERS;
      }
      status.value = 'ready';
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load connections';
      status.value = 'error';
    }
  }

  async function testConnection(id: string) {
    testLoading.value = true;
    testResult.value = null;
    try {
      if (PC_USE_MOCK) {
        const conn = connections.value.find(c => c.id === id);
        testResult.value = await withMockLatency(() =>
          conn?.lastTestOk === false ? MOCK_TEST_RESULT_FAIL : MOCK_TEST_RESULT_OK
        );
      }
    } finally {
      testLoading.value = false;
    }
  }

  function selectConnection(id: string | null) {
    selectedId.value = id;
    testResult.value = null;
  }

  return { status, error, connections, adapters, selectedId, testResult, testLoading, tableState, selectedConnection, fetchConnections, testConnection, selectConnection };
});
