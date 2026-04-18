import type { ScopeType, TemplateKind, PolicyCategory, AdapterKind, AuditCategory } from './types';

export const SCOPE_ORDER: readonly ScopeType[] = ['platform', 'application', 'workspace', 'project'];

export const TEMPLATE_KIND_LABELS: Record<TemplateKind, string> = {
  page: 'Page',
  flow: 'Flow',
  project: 'Project',
  policy: 'Policy',
  metric: 'Metric',
  ai: 'AI',
};

export const POLICY_CATEGORY_LABELS: Record<PolicyCategory, string> = {
  action: 'Action',
  approval: 'Approval',
  autonomy: 'Autonomy',
  'risk-threshold': 'Risk Threshold',
  exception: 'Exception',
};

export const ADAPTER_KIND_LABELS: Record<AdapterKind, string> = {
  jira: 'Jira',
  gitlab: 'GitLab',
  jenkins: 'Jenkins',
  servicenow: 'ServiceNow',
  'custom-webhook': 'Custom Webhook',
};

export const AUDIT_CATEGORY_LABELS: Record<AuditCategory, string> = {
  config_change: 'Config Change',
  permission_change: 'Permission Change',
  ai_suggestion: 'AI Suggestion',
  ai_execution: 'AI Execution',
  approval_decision: 'Approval Decision',
  skill_execution: 'Skill Execution',
  deployment_event: 'Deployment Event',
  incident_event: 'Incident Event',
  policy_hit: 'Policy Hit',
  'integration.test': 'Integration Test',
};

export const PLATFORM_SUBSECTIONS = [
  { key: 'templates', label: 'Templates', path: '/platform/templates' },
  { key: 'configurations', label: 'Configurations', path: '/platform/configurations' },
  { key: 'audit', label: 'Audit', path: '/platform/audit' },
  { key: 'access', label: 'Access', path: '/platform/access' },
  { key: 'policies', label: 'Policies', path: '/platform/policies' },
  { key: 'integrations', label: 'Integrations', path: '/platform/integrations' },
] as const;

export type SubSectionKey = (typeof PLATFORM_SUBSECTIONS)[number]['key'];

export const PC_USE_MOCK = import.meta.env.VITE_USE_MOCK_API === 'true'
  || typeof import.meta.env.VITE_USE_MOCK_API === 'undefined';
