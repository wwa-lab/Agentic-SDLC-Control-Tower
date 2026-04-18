export const PLATFORM_CHILD_ROUTES = [
  {
    path: '',
    name: 'platform-redirect',
    component: () => import('./templates/TemplatesView.vue'),
  },
  {
    path: 'templates',
    name: 'platform-templates',
    component: () => import('./templates/TemplatesView.vue'),
  },
  {
    path: 'templates/:id',
    name: 'platform-template-detail',
    component: () => import('./templates/TemplatesView.vue'),
  },
  {
    path: 'configurations',
    name: 'platform-configurations',
    component: () => import('./configurations/ConfigurationsView.vue'),
  },
  {
    path: 'configurations/:id',
    name: 'platform-configuration-detail',
    component: () => import('./configurations/ConfigurationsView.vue'),
  },
  {
    path: 'audit',
    name: 'platform-audit',
    component: () => import('./audit/AuditView.vue'),
  },
  {
    path: 'audit/:id',
    name: 'platform-audit-detail',
    component: () => import('./audit/AuditView.vue'),
  },
  {
    path: 'access',
    name: 'platform-access',
    component: () => import('./access/AccessView.vue'),
  },
  {
    path: 'policies',
    name: 'platform-policies',
    component: () => import('./policies/PoliciesView.vue'),
  },
  {
    path: 'policies/:id',
    name: 'platform-policy-detail',
    component: () => import('./policies/PoliciesView.vue'),
  },
  {
    path: 'integrations',
    name: 'platform-integrations',
    component: () => import('./integrations/IntegrationsView.vue'),
  },
  {
    path: 'integrations/:id',
    name: 'platform-integration-detail',
    component: () => import('./integrations/IntegrationsView.vue'),
  },
];
