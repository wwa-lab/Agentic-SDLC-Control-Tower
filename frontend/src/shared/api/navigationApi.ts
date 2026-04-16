import { fetchJson } from './client';
import type { NavItem } from '@/shared/types/shell';

export function getNavigationEntries(): Promise<NavItem[]> {
  return fetchJson<NavItem[]>('/nav/entries');
}
