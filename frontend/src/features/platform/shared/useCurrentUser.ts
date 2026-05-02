import { ref, computed } from 'vue';
import type { CurrentUser } from './types';

const currentUser = ref<CurrentUser>({
  staffId: '43910516',
  displayName: 'Platform Admin',
  staffName: null,
  avatarUrl: null,
  authProvider: 'manual',
  roles: ['PLATFORM_ADMIN'],
  scopes: [{ scopeType: 'platform', scopeId: '*' }],
});

const loading = ref(false);
const error = ref<string | null>(null);

export function useCurrentUser() {
  const isPlatformAdmin = computed(() =>
    currentUser.value.roles.includes('PLATFORM_ADMIN')
  );

  return {
    currentUser,
    loading,
    error,
    isPlatformAdmin,
  };
}
