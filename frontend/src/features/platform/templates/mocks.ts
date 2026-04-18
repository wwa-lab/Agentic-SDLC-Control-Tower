import type { TemplateSummary, TemplateDetail, TemplateVersion } from '../shared/types';

const VERSIONS: TemplateVersion[] = [
  { id: 'tver-001-v3', templateId: 'tmpl-001', version: 3, body: { defaultMilestones: ['Kickoff', 'Design', 'Build', 'Rollout'] }, createdAt: '2026-04-15T10:00:00Z', createdBy: 'admin@sdlctower.local' },
  { id: 'tver-001-v2', templateId: 'tmpl-001', version: 2, body: { defaultMilestones: ['Kickoff', 'Design', 'Build'] }, createdAt: '2026-03-20T10:00:00Z', createdBy: 'admin@sdlctower.local' },
  { id: 'tver-001-v1', templateId: 'tmpl-001', version: 1, body: { defaultMilestones: ['Kickoff', 'Build'] }, createdAt: '2026-03-01T10:00:00Z', createdBy: 'admin@sdlctower.local' },
];

export const MOCK_TEMPLATES: TemplateSummary[] = [
  { id: 'tmpl-001', key: 'project-default-v1', name: 'Project Default (V1)', kind: 'project', status: 'published', version: 3, ownerId: 'admin@sdlctower.local', lastModifiedAt: '2026-04-15T10:00:00Z' },
  { id: 'tmpl-002', key: 'page-requirement-layout', name: 'Requirement Page Layout', kind: 'page', status: 'published', version: 1, ownerId: 'admin@sdlctower.local', lastModifiedAt: '2026-04-10T08:00:00Z' },
  { id: 'tmpl-003', key: 'flow-sdd-pipeline', name: 'SDD Pipeline Flow', kind: 'flow', status: 'published', version: 2, ownerId: 'platform-ai', lastModifiedAt: '2026-04-12T14:00:00Z' },
  { id: 'tmpl-004', key: 'policy-autonomy-default', name: 'Autonomy Policy Default', kind: 'policy', status: 'published', version: 1, ownerId: 'admin@sdlctower.local', lastModifiedAt: '2026-03-28T09:00:00Z' },
  { id: 'tmpl-005', key: 'metric-dora-template', name: 'DORA Metrics Template', kind: 'metric', status: 'draft', version: 1, ownerId: 'platform-ai', lastModifiedAt: '2026-04-17T16:00:00Z' },
  { id: 'tmpl-006', key: 'ai-incident-diagnosis', name: 'AI Incident Diagnosis Template', kind: 'ai', status: 'published', version: 1, ownerId: 'platform-sre', lastModifiedAt: '2026-04-14T11:00:00Z' },
  { id: 'tmpl-007', key: 'page-dashboard-layout', name: 'Dashboard Page Layout', kind: 'page', status: 'deprecated', version: 4, ownerId: 'admin@sdlctower.local', lastModifiedAt: '2026-02-15T10:00:00Z' },
  { id: 'tmpl-008', key: 'flow-incident-response', name: 'Incident Response Flow', kind: 'flow', status: 'published', version: 1, ownerId: 'platform-sre', lastModifiedAt: '2026-04-05T09:00:00Z' },
  { id: 'tmpl-009', key: 'project-agile-v2', name: 'Agile Project (V2)', kind: 'project', status: 'draft', version: 1, ownerId: 'admin@sdlctower.local', lastModifiedAt: '2026-04-18T08:00:00Z' },
  { id: 'tmpl-010', key: 'ai-code-review', name: 'AI Code Review Template', kind: 'ai', status: 'published', version: 2, ownerId: 'platform-ai', lastModifiedAt: '2026-04-16T13:00:00Z' },
  { id: 'tmpl-011', key: 'policy-approval-chain', name: 'Approval Chain Policy', kind: 'policy', status: 'published', version: 1, ownerId: 'admin@sdlctower.local', lastModifiedAt: '2026-03-30T10:00:00Z' },
  { id: 'tmpl-012', key: 'metric-quality-gates', name: 'Quality Gate Metrics', kind: 'metric', status: 'published', version: 1, ownerId: 'platform-ai', lastModifiedAt: '2026-04-08T15:00:00Z' },
];

export const MOCK_TEMPLATE_DETAIL: TemplateDetail = {
  template: { ...MOCK_TEMPLATES[0], description: 'Default project template with milestone structure' },
  version: VERSIONS[0],
  inheritance: {
    defaultMilestones: {
      effectiveValue: ['Kickoff', 'Design', 'Build', 'Rollout'],
      winningLayer: 'platform',
      layers: { platform: ['Kickoff', 'Design', 'Build', 'Rollout'], application: null, snowGroup: null, project: null },
    },
  },
};

export const MOCK_VERSIONS = VERSIONS;
