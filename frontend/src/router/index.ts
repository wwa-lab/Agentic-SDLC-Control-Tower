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
import { REPORT_CENTER_CHILD_ROUTES } from '@/features/reportcenter';
import { PLATFORM_CHILD_ROUTES } from '@/features/platform';

/**
 * The 13 required navigation entries as per spec §3.1.
 * Icon field matches the Lucide component name for decoupling.
 */
export const NAVIGATION_ITEMS: NavItem[] = [
  { key: 'dashboard', label: 'Dashboard', path: '/', icon: 'LayoutDashboard' },
  { key: 'team', label: 'Team Space', path: '/team', icon: 'Users' },
  { key: 'project-space', label: 'Project Space', path: '/project-space', icon: 'Box' },
  { key: 'requirements', label: 'Requirement Management', path: '/requirements', icon: 'FileText' },
  { key: 'project-management', label: 'Project Management', path: '/project-management', icon: 'GitBranch' },
  { key: 'design', label: 'Design Management', path: '/design-management', icon: 'Layers' },
  { key: 'code', label: 'Code & Build', path: '/code-build-management', icon: 'Code' },
  { key: 'testing', label: 'Testing', path: '/testing', icon: 'TestTube', comingSoon: false },
  { key: 'deployment', label: 'Deployment', path: '/deployment', icon: 'Send' },
  { key: 'incidents', label: 'Incident Management', path: '/incidents', icon: 'AlertTriangle' },
  { key: 'ai-center', label: 'AI Center', path: '/ai-center', icon: 'Cpu' },
  { key: 'reports', label: 'Reports', path: '/reports', icon: 'BarChart' },
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
const PAGE_CONFIGS: Record<string, Pick<ShellPageConfig, 'subtitle' | 'actions' | 'showAiPanel'>> = {
  dashboard: {
    subtitle: 'Cross-stage health and operational overview',
    actions: [
      { key: 'export', label: 'EXPORT DATA' },
      { key: 'ai-sync', label: 'AI SYNC', variant: 'ai' },
    ],
  },
  team: {
    subtitle: 'Workspace operating model, risks, templates, metrics, and project spread',
    actions: [
      { key: 'refresh', label: 'REFRESH GRID' },
      { key: 'ai-brief', label: 'AI BRIEF', variant: 'ai' },
    ],
  },
  'project-space': {
    subtitle: 'Project execution and environment status',
    actions: [
      { key: 'export', label: 'EXPORT DATA' },
    ],
  },
  requirements: {
    subtitle: 'Control plane for sources, SDD documents, reviews, and freshness',
    showAiPanel: false,
  },
  'project-management': {
    subtitle: 'Portfolio command center and per-project plan execution workspace',
    actions: [
      { key: 'open-plan', label: 'OPEN PLAN' },
      { key: 'ai-brief', label: 'AI BRIEF', variant: 'ai' },
    ],
  },
  design: {
    subtitle: 'Artifact catalog, preview, and spec traceability',
    actions: [
      { key: 'traceability', label: 'TRACEABILITY' },
      { key: 'ai-summary', label: 'AI SUMMARY', variant: 'ai' },
    ],
  },
  code: {
    subtitle: 'Read-only repository, pull request, run, and traceability observability',
    actions: [
      { key: 'traceability', label: 'TRACEABILITY' },
      { key: 'repo-lens', label: 'REPO LENS' },
      { key: 'ai-triage', label: 'AI TRIAGE', variant: 'ai' },
    ],
  },
  testing: {
    subtitle: 'QA lifecycle visibility across plans, cases, runs, and requirement coverage',
    actions: [
      { key: 'traceability', label: 'TRACEABILITY' },
      { key: 'open-catalog', label: 'PLAN GRID' },
      { key: 'ai-drafts', label: 'AI DRAFTS', variant: 'ai' },
    ],
  },
  deployment: {
    subtitle: 'Read-only Jenkins deployment observability with AI release notes and traceability',
    actions: [
      { key: 'traceability', label: 'TRACEABILITY' },
      { key: 'ai-notes', label: 'AI NOTES', variant: 'ai' },
    ],
  },
  incidents: {
    subtitle: 'AI-native operations command center',
    actions: [
      { key: 'ai-diagnose', label: 'AI DIAGNOSE', variant: 'ai' },
    ],
  },
  'ai-center': {
    subtitle: 'Skill registry posture, adoption metrics, and execution evidence',
    actions: [
      { key: 'refresh-ai-center', label: 'REFRESH RUNS' },
    ],
  },
  reports: {
    subtitle: 'History-ready analytics, filter-driven evidence, and exportable snapshots',
    actions: [
      { key: 'open-history', label: 'OPEN HISTORY' },
      { key: 'export', label: 'EXPORT DATA' },
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
  team: () => import('@/features/team-space/TeamSpaceView.vue'),
  'project-space': () => import('@/features/project-space/ProjectSpaceView.vue'),
  requirements: () => import('@/features/requirement/RequirementManagementView.vue'),
  'project-management': () => import('@/features/project-management/ProjectManagementView.vue'),
  design: () => import('@/features/design-management/DesignManagementView.vue'),
  code: () => import('@/features/code-build-management/CodeBuildManagementView.vue'),
  testing: () => import('@/features/testing-management/TestingManagementView.vue'),
  deployment: () => import('@/features/deployment-management/views/CatalogView.vue'),
  incidents: () => import('@/features/incident/IncidentManagementView.vue'),
  'ai-center': () => import('@/features/ai-center/AiCenterView.vue'),
  reports: () => import('@/features/reportcenter/ReportCenterView.vue'),
  platform: () => import('@/features/platform/shell/PlatformShell.vue'),
};

/**
 * Child route definitions for modules that use nested routing.
 * Each key matches a NAVIGATION_ITEMS key; the parent gets `children`
 * and loses its own `name` (the default child takes the parent name).
 */
const CHILD_ROUTES: Record<string, Array<{ path: string; name: string; component: () => Promise<any> }>> = {
  requirements: [
    { path: '', name: 'requirements', component: () => import('@/features/requirement/views/RequirementListView.vue') },
    { path: 'skill-flow', name: 'requirement-skill-flow', component: () => import('@/features/requirement/views/RequirementSkillFlowView.vue') },
    { path: ':requirementId', name: 'requirement-detail', component: () => import('@/features/requirement/views/RequirementDetailView.vue') },
  ],
  'project-management': [
    { path: '', name: 'project-management', component: () => import('@/features/project-management/views/PortfolioView.vue') },
    { path: 'plan/:projectId', name: 'project-management-plan', component: () => import('@/features/project-management/views/PlanView.vue') },
  ],
  design: [
    { path: '', name: 'design-management', component: () => import('@/features/design-management/views/CatalogView.vue') },
    { path: 'artifacts/:artifactId', name: 'design-management-artifact', component: () => import('@/features/design-management/views/ViewerView.vue') },
    { path: 'traceability', name: 'design-management-traceability', component: () => import('@/features/design-management/views/TraceabilityView.vue') },
  ],
  code: [
    { path: '', name: 'code-build-management', component: () => import('@/features/code-build-management/views/CatalogView.vue') },
    { path: 'traceability', name: 'code-build-management-traceability', component: () => import('@/features/code-build-management/views/TraceabilityView.vue') },
    { path: 'repos/:repoId', name: 'code-build-management-repo', component: () => import('@/features/code-build-management/views/RepoDetailView.vue') },
    { path: 'prs/:prId', name: 'code-build-management-pr', component: () => import('@/features/code-build-management/views/PrDetailView.vue') },
    { path: 'runs/:runId', name: 'code-build-management-run', component: () => import('@/features/code-build-management/views/RunDetailView.vue') },
  ],
  testing: [
    { path: '', name: 'testing-management', component: () => import('@/features/testing-management/views/CatalogView.vue') },
    { path: 'plans/:planId', name: 'testing-management-plan', component: () => import('@/features/testing-management/views/PlanDetailView.vue') },
    { path: 'cases/:caseId', name: 'testing-management-case', component: () => import('@/features/testing-management/views/CaseDetailView.vue') },
    { path: 'runs/:runId', name: 'testing-management-run', component: () => import('@/features/testing-management/views/RunDetailView.vue') },
    { path: 'traceability', name: 'testing-management-traceability', component: () => import('@/features/testing-management/views/TraceabilityView.vue') },
  ],
  deployment: [
    { path: '', name: 'deployment', component: () => import('@/features/deployment-management/views/CatalogView.vue') },
    { path: 'traceability', name: 'deployment-traceability', component: () => import('@/features/deployment-management/views/TraceabilityView.vue') },
    { path: 'applications/:applicationId', name: 'deployment-application', component: () => import('@/features/deployment-management/views/ApplicationDetailView.vue') },
    { path: 'releases/:releaseId', name: 'deployment-release', component: () => import('@/features/deployment-management/views/ReleaseDetailView.vue') },
    { path: 'deploys/:deployId', name: 'deployment-deploy', component: () => import('@/features/deployment-management/views/DeployDetailView.vue') },
    { path: 'applications/:applicationId/environments/:environmentName', name: 'deployment-environment', component: () => import('@/features/deployment-management/views/EnvironmentDetailView.vue') },
  ],
  incidents: [
    { path: '', name: 'incidents', component: () => import('@/features/incident/views/IncidentListView.vue') },
    { path: ':incidentId', name: 'incident-detail', component: () => import('@/features/incident/views/IncidentDetailView.vue') },
  ],
  'ai-center': [
    { path: 'skills/:skillKey', name: 'ai-center-skill-detail', component: () => import('@/features/ai-center/components/SkillDetailPanel.vue') },
    { path: 'runs/:executionId', name: 'ai-center-run-detail', component: () => import('@/features/ai-center/components/RunDetailPanel.vue') },
  ],
  reports: REPORT_CENTER_CHILD_ROUTES,
  platform: PLATFORM_CHILD_ROUTES,
};

const routes = NAVIGATION_ITEMS.map(item => {
  const pageConfig = PAGE_CONFIGS[item.key];
  const children = CHILD_ROUTES[item.key];
  const resolvedPath = item.key === 'project-space'
    ? '/project-space/:projectId?'
    : item.path;
  const base = {
    path: resolvedPath,
    ...(item.key === 'testing' ? { alias: '/testing-management' } : {}),
    component: COMPONENT_MAP[item.key] || (() => import('@/features/placeholder/PlaceholderView.vue')),
    meta: {
      navKey: item.key,
      title: item.label,
      comingSoon: item.comingSoon,
      subtitle: pageConfig?.subtitle,
      actions: pageConfig?.actions as ReadonlyArray<ShellAction> | undefined,
      showAiPanel: pageConfig?.showAiPanel,
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
  scrollBehavior(to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth',
      };
    }
    return { top: 0 };
  },
});
