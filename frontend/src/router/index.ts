import { createRouter, createWebHistory } from 'vue-router';
import type { NavItem, ShellPageConfig } from '@/types/shell';

/**
 * The 13 required navigation entries as per spec §3.1
 */
export const NAVIGATION_ITEMS: NavItem[] = [
  { key: 'dashboard', label: 'Dashboard', path: '/' },
  { key: 'team', label: 'Team Space', path: '/team', comingSoon: true },
  { key: 'project-space', label: 'Project Space', path: '/project-space' },
  { key: 'requirements', label: 'Requirement Management', path: '/requirements', comingSoon: true },
  { key: 'project-management', label: 'Project Management', path: '/project-management', comingSoon: true },
  { key: 'design', label: 'Design Management', path: '/design', comingSoon: true },
  { key: 'code', label: 'Code & Build', path: '/code', comingSoon: true },
  { key: 'testing', label: 'Testing', path: '/testing', comingSoon: true },
  { key: 'deployment', label: 'Deployment', path: '/deployment', comingSoon: true },
  { key: 'incidents', label: 'Incident Management', path: '/incidents' },
  { key: 'ai-center', label: 'AI Center', path: '/ai-center', comingSoon: true },
  { key: 'reports', label: 'Report Center', path: '/reports', comingSoon: true },
  { key: 'platform', label: 'Platform Center', path: '/platform' }
];

/**
 * Page-specific shell configuration for Round 1 pages.
 * Provides subtitle and actions via ShellPageConfig contract (spec §7).
 */
const PAGE_CONFIGS: Record<string, Partial<ShellPageConfig>> = {
  dashboard: {
    subtitle: 'Cross-stage health and operational overview',
    actions: [
      { key: 'export', label: 'EXPORT DATA' },
      { key: 'ai', label: 'AI SYNC' }
    ]
  },
  'project-space': {
    subtitle: 'Project execution and environment status',
    actions: [
      { key: 'export', label: 'EXPORT DATA' }
    ]
  },
  incidents: {
    subtitle: 'AI-native operations command center',
    actions: [
      { key: 'ai', label: 'AI DIAGNOSE' }
    ]
  },
  platform: {
    subtitle: 'Governance and platform capability hub',
    actions: [
      { key: 'audit', label: 'AUDIT LOG' }
    ]
  }
};

const COMPONENT_MAP: Record<string, any> = {
  dashboard: () => import('@/views/DashboardView.vue'),
  'project-space': () => import('@/views/ProjectSpaceView.vue'),
  incidents: () => import('@/views/IncidentManagementView.vue'),
  platform: () => import('@/views/PlatformCenterView.vue'),
};

const routes = NAVIGATION_ITEMS.map(item => {
  const pageConfig = PAGE_CONFIGS[item.key];
  return {
    path: item.path,
    name: item.key,
    component: COMPONENT_MAP[item.key] || (() => import('@/views/PlaceholderView.vue')),
    meta: {
      navKey: item.key,
      title: item.label,
      comingSoon: item.comingSoon,
      subtitle: pageConfig?.subtitle,
      actions: pageConfig?.actions
    }
  };
});

export const router = createRouter({
  history: createWebHistory(),
  routes
});
