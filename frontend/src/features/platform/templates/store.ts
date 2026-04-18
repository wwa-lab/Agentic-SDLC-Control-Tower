import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { TemplateSummary, TemplateDetail, TemplateVersion, LoadState } from '../shared/types';
import { MOCK_TEMPLATES, MOCK_TEMPLATE_DETAIL, MOCK_VERSIONS } from './mocks';
import { withMockLatency, PC_USE_MOCK } from '../shared/api';

export const useTemplatesStore = defineStore('platform-templates', () => {
  const status = ref<LoadState>('idle');
  const error = ref<string | null>(null);
  const items = ref<TemplateSummary[]>([]);
  const cursor = ref<string | null>(null);
  const total = ref<number | null>(null);
  const selectedId = ref<string | null>(null);
  const detail = ref<TemplateDetail | null>(null);
  const detailStatus = ref<LoadState>('idle');
  const versions = ref<TemplateVersion[]>([]);

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
        const data = await withMockLatency(() => MOCK_TEMPLATES);
        items.value = data;
        total.value = data.length;
        cursor.value = null;
      }
      status.value = 'ready';
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load templates';
      status.value = 'error';
    }
  }

  async function fetchDetail(id: string) {
    selectedId.value = id;
    detailStatus.value = 'loading';
    try {
      if (PC_USE_MOCK) {
        const found = MOCK_TEMPLATES.find(t => t.id === id);
        detail.value = found
          ? { ...MOCK_TEMPLATE_DETAIL, template: { ...found, description: `Description for ${found.name}` } }
          : null;
        versions.value = await withMockLatency(() => MOCK_VERSIONS);
      }
      detailStatus.value = 'ready';
    } catch {
      detailStatus.value = 'error';
    }
  }

  function clearSelection() {
    selectedId.value = null;
    detail.value = null;
    detailStatus.value = 'idle';
  }

  return { status, error, items, cursor, total, selectedId, detail, detailStatus, versions, tableState, fetchCatalog, fetchDetail, clearSelection };
});
