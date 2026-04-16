import { createRouter, createWebHistory } from 'vue-router';
import type { NavItem, ShellPageConfig, ShellAction } from '@/shared/types/shell';
import {
  LayoutDashboard,
  Users,
  Box,
  FileText,
  GitBranch,
  Layers,
  Code,
  TestTube,
  Send,
  AlertTriangle,
  Cpu,
  BarChart,
  Settings
} from 'lucide-vue-next';

/**
 * The 13 required navigation entries as per spec §3.1.
 * Icon field matches the Lucide component name for decoupling.
 */
export const NAVIGATION_ITEMS: NavItem[] = [
  { key: 'dashboard', label: 'Dashboard', path: '/', icon: 'LayoutDashboard' },
  { key: 'team', label: 'Team Space', path: '/team', icon: 'Users', comingSoon: true },
  { key: 'project-space', label: 'Project Space', path: '/project-space', icon: 'Box' },
  { key: 'requirements', label: 'Requirement Management', path: '/requirements', icon: 'FileText', comingSoon: true },
  { key: 'project-management', label: 'Project Management', path: '/project-management', icon: 'GitBranch', comingSoon: true },
  { key: 'design', label: 'Design Management', path: '/design', icon: 'Layers', comingSoon: true },
  { key: 'code', label: 'Code & Build', path: '/code', icon: 'Code', comingSoon: true },
  { key: 'testing', label: 'Testing', path: '/testing', icon: 'TestTube', comingSoon: true },
  { key: 'deployment', label: 'Deployment', path: '/deployment', icon: 'Send', comingSoon: true },
  { key: 'incidents', label: 'Incident Management', path: '/incidents', icon: 'AlertTriangle' },
  { key: 'ai-center', label: 'AI Center', path: '/ai-center', icon: 'Cpu', comingSoon: true },
  { key: 'reports', label: 'Report Center', path: '/reports', icon: 'BarChart', comingSoon: true },
  { key: 'platform', label: 'Platform Center', path: '/platform', icon: 'Settings' }
];

/**
 * Lucide icon components keyed by nav item key.
 * Exported so PrimaryNav can render icons without hardcoding its own map.
 */
export const ICON_MAP: Record<string, any> = {
  dashboard: LayoutDashboard,
  team: Users,
  'project-space': Box,
  requirements: FileText,
  'project-management': GitBranch,
  design: Layers,
  code: Code,
  testing: TestTube,
  deployment: Send,
  incidents: AlertTriangle,
  'ai-center': Cpu,
  reports: BarChart,
  platform: Settings
};

/**
 * Page-specific shell configuration for Round 1 pages.
 * Uses ShellPageConfig contract (spec §7).
 */
const PAGE_CONFIGS: Record<string, Pick<ShellPageConfig, 'subtitle' | 'actions'>> = {
  dashboard: {
    subtitle: 'Cross-stage health and operational overview',
    actions: [
      { key: 'export', label: 'EXPORT DATA' },
      { key: 'ai-sync', label: 'AI SYNC', variant: 'ai' },
    ],
  },
  'project-space': {
    subtitle: 'Project execution and environment status',
    actions: [
      { key: 'export', label: 'EXPORT DATA' },
    ],
  },
  incidents: {
    subtitle: 'AI-native operations command center',
    actions: [
      { key: 'ai-diagnose', label: 'AI DIAGNOSE', variant: 'ai' },
    ],
  },
  platform: {
    subtitle: 'Governance and platform capability hub',
    actions: [
      { key: 'audit', label: 'AUDIT LOG' },
    ],
  },
};

const COMPONENT_MAP: Record<string, () => Promise<any>> = {
  dashboard: () => import('@/features/dashboard/DashboardView.vue'),
  'project-space': () => import('@/features/project-space/ProjectSpaceView.vue'),
  incidents: () => import('@/features/incident/IncidentManagementView.vue'),
  platform: () => import('@/features/platform/PlatformCenterView.vue'),
};

/**
 * Child route definitions for modules that use nested routing.
 * Each key matches a NAVIGATION_ITEMS key; the parent gets `children`
 * and loses its own `name` (the default child takes the parent name).
 */
const CHILD_ROUTES: Record<string, Array<{ path: string; name: string; component: () => Promise<any> }>> = {
  incidents: [
    { path: '', name: 'incidents', component: () => import('@/features/incident/views/IncidentListView.vue') },
    { path: ':incidentId', name: 'incident-detail', component: () => import('@/features/incident/views/IncidentDetailView.vue') },
  ],
};

const routes = NAVIGATION_ITEMS.map(item => {
  const pageConfig = PAGE_CONFIGS[item.key];
  const children = CHILD_ROUTES[item.key];
  const base = {
    path: item.path,
    component: COMPONENT_MAP[item.key] || (() => import('@/features/placeholder/PlaceholderView.vue')),
    meta: {
      navKey: item.key,
      title: item.label,
      comingSoon: item.comingSoon,
      subtitle: pageConfig?.subtitle,
      actions: pageConfig?.actions as ReadonlyArray<ShellAction> | undefined,
    },
  };

  // Modules with child routes: parent is a router-view host, name lives on default child
  if (children) {
    return { ...base, children };
  }

  return { ...base, name: item.key };
});

export const router = createRouter({
  history: createWebHistory(),
  routes,
});
