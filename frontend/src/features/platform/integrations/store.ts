import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Connection, AdapterDescriptor, ConnectionTestResult, LoadState } from '../shared/types';
import { MOCK_CONNECTIONS, MOCK_ADAPTERS, MOCK_TEST_RESULT_OK, MOCK_TEST_RESULT_FAIL } from './mocks';
import { withMockLatency, PC_USE_MOCK } from '../shared/api';

type ConnectionInput = Omit<Connection, 'id' | 'lastSyncAt' | 'lastTestAt' | 'lastTestOk'> & {
  readonly id?: string | null;
};

const STORAGE_KEY = 'platform.integrations.connections';

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
        connections.value = await withMockLatency(readStoredConnections);
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
        const result = await withMockLatency(() =>
          conn?.lastTestOk === false ? MOCK_TEST_RESULT_FAIL : MOCK_TEST_RESULT_OK
        );
        testResult.value = result;
        connections.value = connections.value.map(item => item.id === id
          ? { ...item, lastTestAt: new Date().toISOString(), lastTestOk: result.ok, status: result.ok ? 'enabled' : 'error' }
          : item
        );
        persistConnections();
      }
    } finally {
      testLoading.value = false;
    }
  }

  function selectConnection(id: string | null) {
    selectedId.value = id;
    testResult.value = null;
  }

  async function saveConnection(input: ConnectionInput) {
    const now = new Date().toISOString();
    const normalized: Connection = {
      id: input.id?.trim() || `conn-${input.kind}-${Date.now()}`,
      kind: input.kind,
      scopeWorkspaceId: input.scopeWorkspaceId.trim(),
      applicationId: normalizeOptional(input.applicationId),
      applicationName: normalizeOptional(input.applicationName),
      snowGroupId: normalizeOptional(input.snowGroupId),
      snowGroupName: normalizeOptional(input.snowGroupName),
      baseUrl: normalizeOptional(input.baseUrl),
      credentialRef: input.credentialRef.trim(),
      syncMode: input.syncMode,
      pullSchedule: normalizeOptional(input.pullSchedule),
      pushUrl: normalizeOptional(input.pushUrl),
      status: input.status,
      lastSyncAt: connections.value.find(conn => conn.id === input.id)?.lastSyncAt ?? null,
      lastTestAt: connections.value.find(conn => conn.id === input.id)?.lastTestAt ?? now,
      lastTestOk: connections.value.find(conn => conn.id === input.id)?.lastTestOk ?? null,
    };

    connections.value = connections.value.some(conn => conn.id === normalized.id)
      ? connections.value.map(conn => conn.id === normalized.id ? normalized : conn)
      : [normalized, ...connections.value];

    selectedId.value = normalized.id;
    persistConnections();
    return normalized;
  }

  function deleteConnection(id: string) {
    connections.value = connections.value.filter(conn => conn.id !== id);
    if (selectedId.value === id) selectedId.value = null;
    persistConnections();
  }

  function resetConnections() {
    connections.value = [...MOCK_CONNECTIONS];
    selectedId.value = null;
    testResult.value = null;
    persistConnections();
  }

  function readStoredConnections() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return [...MOCK_CONNECTIONS];
      const parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed.map(hydrateConnection) : [...MOCK_CONNECTIONS];
    } catch {
      return [...MOCK_CONNECTIONS];
    }
  }

  function persistConnections() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(connections.value));
  }

  function normalizeOptional(value: string | null) {
    const normalized = value?.trim();
    return normalized ? normalized : null;
  }

  function hydrateConnection(value: Connection) {
    return {
      ...value,
      applicationId: value.applicationId ?? null,
      applicationName: value.applicationName ?? null,
      snowGroupId: value.snowGroupId ?? null,
      snowGroupName: value.snowGroupName ?? null,
      baseUrl: value.baseUrl ?? null,
    };
  }

  return {
    status,
    error,
    connections,
    adapters,
    selectedId,
    testResult,
    testLoading,
    tableState,
    selectedConnection,
    fetchConnections,
    testConnection,
    selectConnection,
    saveConnection,
    deleteConnection,
    resetConnections,
  };
});
