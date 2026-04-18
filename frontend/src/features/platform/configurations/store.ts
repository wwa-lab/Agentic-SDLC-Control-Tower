import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { ConfigurationSummary, LoadState } from '../shared/types';
import { MOCK_CONFIGURATIONS } from './mocks';
import { withMockLatency, PC_USE_MOCK } from '../shared/api';

export const useConfigurationsStore = defineStore('platform-configurations', () => {
  const status = ref<LoadState>('idle');
  const error = ref<string | null>(null);
  const items = ref<ConfigurationSummary[]>([]);
  const selectedId = ref<string | null>(null);

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
      }
      status.value = 'ready';
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load configurations';
      status.value = 'error';
    }
  }

  function selectRow(id: string | null) { selectedId.value = id; }

  return { status, error, items, selectedId, tableState, fetchCatalog, selectRow };
});
