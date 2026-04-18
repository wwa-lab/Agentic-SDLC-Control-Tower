import { ref, computed } from 'vue';
import type { CurrentUser } from './types';

const currentUser = ref<CurrentUser>({
  userId: 'admin@sdlctower.local',
  displayName: 'Platform Admin',
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
