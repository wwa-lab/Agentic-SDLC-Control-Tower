import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import type { AuthProviderOption, CurrentUser } from '@/shared/types/shell';
import {
  continueAsGuest,
  getAuthProviders,
  getCurrentUser,
  login as loginApi,
  logout as logoutApi
} from '@/shared/api/authApi';

export const useSessionStore = defineStore('session', () => {
  const currentUser = ref<CurrentUser | null>(null);
  const providers = ref<AuthProviderOption[]>([]);
  const loading = ref(false);
  const initialized = ref(false);
  const error = ref<string | null>(null);

  const isAuthenticated = computed(() => currentUser.value != null);
  const isGuest = computed(() => currentUser.value?.mode === 'guest');
  const isReadOnly = computed(() => currentUser.value?.readOnly === true);

  async function init() {
    loading.value = true;
    error.value = null;
    try {
      providers.value = await getAuthProviders();
      try {
        currentUser.value = await getCurrentUser();
      } catch {
        currentUser.value = null;
      }
    } catch (e) {
      providers.value = fallbackProviders();
      error.value = e instanceof Error ? e.message : 'Unable to initialize session';
    } finally {
      initialized.value = true;
      loading.value = false;
    }
  }

  async function login(staffId: string, password: string) {
    loading.value = true;
    error.value = null;
    try {
      currentUser.value = await loginApi(staffId, password);
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Login failed';
      throw e;
    } finally {
      loading.value = false;
    }
  }

  async function guest() {
    loading.value = true;
    error.value = null;
    try {
      currentUser.value = await continueAsGuest();
    } finally {
      loading.value = false;
    }
  }

  async function logout() {
    await logoutApi();
    currentUser.value = null;
  }

  function startTeamBook(provider: AuthProviderOption) {
    if (provider.enabled && provider.startUrl) {
      window.location.assign(provider.startUrl);
    }
  }

  function fallbackProviders(): AuthProviderOption[] {
    return [
      { provider: 'manual', label: 'Staff ID', enabled: true, startUrl: null },
      { provider: 'guest', label: 'Guest', enabled: true, startUrl: null },
    ];
  }

  return {
    currentUser,
    providers,
    loading,
    initialized,
    error,
    isAuthenticated,
    isGuest,
    isReadOnly,
    init,
    login,
    guest,
    logout,
    startTeamBook,
  };
});
