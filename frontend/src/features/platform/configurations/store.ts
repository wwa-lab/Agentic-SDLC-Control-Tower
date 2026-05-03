import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { ConfigurationDetail, ConfigurationSummary, CursorPage, LoadState, ScopeType } from '../shared/types';
import { MOCK_CONFIGURATIONS } from './mocks';
import { withMockLatency, PC_USE_MOCK, pcGet, pcPost, pcPut } from '../shared/api';

interface ConfigurationPayload {
  key: string;
  kind: string;
  scopeType: ScopeType;
  scopeId: string;
  parentId?: string | null;
  status: string;
  body: Record<string, unknown>;
}

export const useConfigurationsStore = defineStore('platform-configurations', () => {
  const status = ref<LoadState>('idle');
  const error = ref<string | null>(null);
  const items = ref<ConfigurationSummary[]>([]);
  const selectedId = ref<string | null>(null);
  const detail = ref<ConfigurationDetail | null>(null);

  const tableState = computed(() => {
    if (status.value === 'loading') return 'loading' as const;
    if (status.value === 'error') return 'error' as const;
    if (items.value.length === 0) return 'empty' as const;
    return 'normal' as const;
  });

  async function fetchCatalog() {
    status.value = 'loading';
    error.value = null;
    try {
      if (PC_USE_MOCK) {
        items.value = await withMockLatency(() => MOCK_CONFIGURATIONS);
      } else {
        items.value = (await pcGet<CursorPage<ConfigurationSummary>>('/configurations')).data;
      }
      status.value = 'ready';
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load configurations';
      status.value = 'error';
    }
  }

  function selectRow(id: string | null) { selectedId.value = id; }

  async function fetchDetail(id: string) {
    selectedId.value = id;
    error.value = null;
    try {
      if (PC_USE_MOCK) {
        const found = items.value.find(item => item.id === id) ?? null;
        detail.value = found ? { ...found, body: { enabled: true }, platformDefaultBody: { enabled: true }, driftFields: found.hasDrift ? ['enabled'] : [] } : null;
      } else {
        detail.value = await pcGet<ConfigurationDetail>(`/configurations/${id}`);
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load configuration detail';
      throw e;
    }
  }

  async function createConfiguration(payload: ConfigurationPayload) {
    error.value = null;
    try {
      if (PC_USE_MOCK) {
        const now = new Date().toISOString();
        const created = { id: `cfg-${Date.now()}`, ...payload, hasDrift: payload.scopeType !== 'platform', lastModifiedAt: now } as ConfigurationSummary;
        items.value = [created, ...items.value];
      } else {
        await pcPost<ConfigurationDetail>('/configurations', payload);
        await fetchCatalog();
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to create configuration';
      throw e;
    }
  }

  async function updateConfiguration(id: string, payload: ConfigurationPayload) {
    error.value = null;
    try {
      if (PC_USE_MOCK) {
        items.value = items.value.map(item => item.id === id ? { ...item, ...payload, hasDrift: payload.scopeType !== 'platform', lastModifiedAt: new Date().toISOString() } as ConfigurationSummary : item);
      } else {
        await pcPut<ConfigurationDetail>(`/configurations/${id}`, payload);
        await fetchCatalog();
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to update configuration';
      throw e;
    }
  }

  return {
    status,
    error,
    items,
    selectedId,
    detail,
    tableState,
    fetchCatalog,
    fetchDetail,
    selectRow,
    createConfiguration,
    updateConfiguration,
  };
});
