import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { router } from './index';
import { useSessionStore } from '@/shell/stores/sessionStore';
import * as authApi from '@/shared/api/authApi';
import * as workspaceApi from '@/shared/api/workspaceApi';

describe('platform route guard', () => {
  beforeEach(async () => {
    setActivePinia(createPinia());
    vi.restoreAllMocks();
    vi.spyOn(authApi, 'getAuthProviders').mockResolvedValue([
      { provider: 'manual', label: 'Staff ID', enabled: true, startUrl: null },
      { provider: 'guest', label: 'Guest', enabled: true, startUrl: null },
    ]);
    vi.spyOn(workspaceApi, 'resolveWorkspaceByKey').mockResolvedValue({
      workspaceId: 'ws-default-001',
      workspaceKey: 'payment-gateway-pro',
      name: 'Payment Gateway Pro',
      applicationId: 'app-payment-gateway-pro',
      snowGroupId: 'snow-fin-tech-ops',
      profileId: 'standard-java-sdd',
    });
    window.history.replaceState({}, '', '/');
  });

  it('redirects non-admin staff users away from Platform Center', async () => {
    vi.spyOn(authApi, 'getCurrentUser').mockResolvedValue({
      mode: 'staff',
      authProvider: 'manual',
      staffId: '43910000',
      displayName: 'Alice',
      staffName: 'Alice Chen',
      avatarUrl: null,
      roles: ['WORKSPACE_VIEWER'],
      readOnly: false,
      scopes: [{ scopeType: 'application', scopeId: 'app-payment-gateway-pro' }],
    });

    await router.push('/payment-gateway-pro/platform/templates');
    await router.isReady();

    expect(router.currentRoute.value.path).toBe('/403');
    expect(router.currentRoute.value.query.redirect).toBe('/payment-gateway-pro/platform/templates');
  });

  it('allows platform admins into Platform Center', async () => {
    const sessionStore = useSessionStore();
    sessionStore.currentUser = {
      mode: 'staff',
      authProvider: 'manual',
      staffId: '43910516',
      displayName: 'Platform Admin',
      staffName: null,
      avatarUrl: null,
      roles: ['PLATFORM_ADMIN'],
      readOnly: false,
      scopes: [{ scopeType: 'platform', scopeId: '*' }],
    };
    sessionStore.initialized = true;

    await router.push('/payment-gateway-pro/platform/templates');
    await router.isReady();

    expect(router.currentRoute.value.path).toBe('/payment-gateway-pro/platform/templates');
  });
});
