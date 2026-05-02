import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { AuditRecord, CursorPage, LoadState } from '../shared/types';
import { MOCK_AUDIT_RECORDS } from './mocks';
import { withMockLatency, PC_USE_MOCK, pcGet } from '../shared/api';

export const useAuditStore = defineStore('platform-audit', () => {
  const status = ref<LoadState>('idle');
  const error = ref<string | null>(null);
  const items = ref<AuditRecord[]>([]);
  const cursor = ref<string | null>(null);
  const total = ref<number | null>(null);

  const tableState = computed(() => {
    if (status.value === 'loading') return 'loading' as const;
    if (status.value === 'error') return 'error' as const;
    if (items.value.length === 0) return 'empty' as const;
    return 'normal' as const;
  });

  async function fetchAudit() {
    status.value = 'loading';
    error.value = null;
    try {
      if (PC_USE_MOCK) {
        items.value = await withMockLatency(() => MOCK_AUDIT_RECORDS);
        total.value = MOCK_AUDIT_RECORDS.length;
        cursor.value = null;
      } else {
        const page = await pcGet<CursorPage<AuditRecord>>('/audit');
        items.value = page.data;
        total.value = page.pagination.total;
        cursor.value = page.pagination.nextCursor;
      }
      status.value = 'ready';
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load audit log';
      status.value = 'error';
    }
  }

  return { status, error, items, cursor, total, tableState, fetchAudit };
});
