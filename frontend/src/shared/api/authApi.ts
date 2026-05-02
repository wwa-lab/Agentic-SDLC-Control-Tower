import { fetchJson, postJson } from './client';
import type { AuthProviderOption, CurrentUser } from '@/shared/types/shell';

export function getAuthProviders(): Promise<AuthProviderOption[]> {
  return fetchJson<AuthProviderOption[]>('/auth/providers');
}

export function login(staffId: string, password: string): Promise<CurrentUser> {
  return postJson<CurrentUser>('/auth/login', { staffId, password });
}

export function continueAsGuest(): Promise<CurrentUser> {
  return postJson<CurrentUser>('/auth/guest');
}

export function getCurrentUser(): Promise<CurrentUser> {
  return fetchJson<CurrentUser>('/auth/me');
}

export function logout(): Promise<{ loggedOut: boolean }> {
  return postJson<{ loggedOut: boolean }>('/auth/logout');
}
