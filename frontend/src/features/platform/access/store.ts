import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { CursorPage, LoadState, PlatformUser, RoleAssignment } from '../shared/types';
import { MOCK_ROLE_ASSIGNMENTS } from './mocks';
import { withMockLatency, PC_USE_MOCK, pcDelete, pcGet, pcPost, pcPut } from '../shared/api';

interface UserPayload {
  staffId: string;
  displayName: string;
  staffName?: string | null;
  avatarUrl?: string | null;
  email?: string | null;
  profileSource: string;
  status: string;
}

interface AssignmentPayload {
  staffId: string;
  role: string;
  scopeType: string;
  scopeId: string;
}

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

  async function createUser(payload: UserPayload) {
    error.value = null;
    try {
      if (PC_USE_MOCK) {
        users.value = [{ ...payload, lastProfileSyncAt: null, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() }, ...users.value] as PlatformUser[];
      } else {
        await pcPost<PlatformUser>('/access/users', payload);
        await fetchAssignments();
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to create user';
      throw e;
    }
  }

  async function updateUser(staffId: string, payload: UserPayload) {
    error.value = null;
    try {
      if (PC_USE_MOCK) {
        users.value = users.value.map(user => user.staffId === staffId ? { ...user, ...payload, updatedAt: new Date().toISOString() } as PlatformUser : user);
      } else {
        await pcPut<PlatformUser>(`/access/users/${staffId}`, payload);
        await fetchAssignments();
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to update user';
      throw e;
    }
  }

  async function assignRole(payload: AssignmentPayload) {
    error.value = null;
    try {
      if (PC_USE_MOCK) {
        const user = users.value.find(item => item.staffId === payload.staffId);
        items.value = [{
          id: `ra-${payload.staffId}-${payload.role.toLowerCase()}-${payload.scopeType}-${payload.scopeId.replace('*', 'platform')}`,
          staffId: payload.staffId,
          userDisplayName: user?.displayName ?? payload.staffId,
          role: payload.role as RoleAssignment['role'],
          scopeType: payload.scopeType as RoleAssignment['scopeType'],
          scopeId: payload.scopeId,
          grantedBy: 'local',
          grantedAt: new Date().toISOString(),
        }, ...items.value];
      } else {
        await pcPost<RoleAssignment>('/access/assignments', payload);
        await fetchAssignments();
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to assign role';
      throw e;
    }
  }

  async function revokeRole(id: string) {
    error.value = null;
    try {
      if (PC_USE_MOCK) {
        if (items.value.find(item => item.id === id)?.role === 'PLATFORM_ADMIN' && platformAdminCount.value <= 1) {
          throw new Error('LAST_PLATFORM_ADMIN');
        }
        items.value = items.value.filter(item => item.id !== id);
      } else {
        await pcDelete(`/access/assignments/${id}`);
        await fetchAssignments();
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to revoke role';
      throw e;
    }
  }

  return {
    status,
    error,
    items,
    users,
    tableState,
    platformAdminCount,
    fetchAssignments,
    createUser,
    updateUser,
    assignRole,
    revokeRole,
  };
});
