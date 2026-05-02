import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { CursorPage, LoadState, PlatformUser, RoleAssignment } from '../shared/types';
import { MOCK_ROLE_ASSIGNMENTS } from './mocks';
import { withMockLatency, PC_USE_MOCK, pcGet } from '../shared/api';

export const useAccessStore = defineStore('platform-access', () => {
  const status = ref<LoadState>('idle');
  const error = ref<string | null>(null);
  const items = ref<RoleAssignment[]>([]);
  const users = ref<PlatformUser[]>([]);

  const tableState = computed(() => {
    if (status.value === 'loading') return 'loading' as const;
    if (status.value === 'error') return 'error' as const;
    if (items.value.length === 0) return 'empty' as const;
    return 'normal' as const;
  });

  const platformAdminCount = computed(() =>
    items.value.filter(a => a.role === 'PLATFORM_ADMIN').length
  );

  async function fetchAssignments() {
    status.value = 'loading';
    error.value = null;
    try {
      if (PC_USE_MOCK) {
        items.value = await withMockLatency(() => MOCK_ROLE_ASSIGNMENTS);
        users.value = [];
      } else {
        items.value = (await pcGet<CursorPage<RoleAssignment>>('/access/assignments')).data;
        users.value = (await pcGet<CursorPage<PlatformUser>>('/access/users')).data;
      }
      status.value = 'ready';
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load access assignments';
      status.value = 'error';
    }
  }

  return { status, error, items, users, tableState, platformAdminCount, fetchAssignments };
});
