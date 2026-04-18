import type { ConfigurationSummary, DriftInfo } from '../shared/types';

export const MOCK_CONFIGURATIONS: ConfigurationSummary[] = [
  { id: 'cfg-001', key: 'req.page.layout', kind: 'page', scopeType: 'platform', scopeId: '*', parentId: null, status: 'active', hasDrift: false, lastModifiedAt: '2026-04-18T02:15:00Z' },
  { id: 'cfg-002', key: 'req.page.layout', kind: 'page', scopeType: 'workspace', scopeId: 'ws-default', parentId: 'cfg-001', status: 'active', hasDrift: true, lastModifiedAt: '2026-04-17T10:00:00Z' },
  { id: 'cfg-003', key: 'incident.auto-diagnosis', kind: 'ai-config', scopeType: 'platform', scopeId: '*', parentId: null, status: 'active', hasDrift: false, lastModifiedAt: '2026-04-16T09:00:00Z' },
  { id: 'cfg-004', key: 'incident.auto-diagnosis', kind: 'ai-config', scopeType: 'application', scopeId: 'app-fin', parentId: 'cfg-003', status: 'active', hasDrift: false, lastModifiedAt: '2026-04-15T14:00:00Z' },
  { id: 'cfg-005', key: 'dashboard.columns', kind: 'component', scopeType: 'platform', scopeId: '*', parentId: null, status: 'active', hasDrift: false, lastModifiedAt: '2026-04-14T08:00:00Z' },
  { id: 'cfg-006', key: 'dashboard.columns', kind: 'component', scopeType: 'project', scopeId: 'proj-alpha', parentId: 'cfg-005', status: 'active', hasDrift: true, lastModifiedAt: '2026-04-13T16:00:00Z' },
  { id: 'cfg-007', key: 'deploy.approval-required', kind: 'flow-rule', scopeType: 'platform', scopeId: '*', parentId: null, status: 'active', hasDrift: false, lastModifiedAt: '2026-04-12T10:00:00Z' },
  { id: 'cfg-008', key: 'notifications.slack-channel', kind: 'notification', scopeType: 'platform', scopeId: '*', parentId: null, status: 'active', hasDrift: false, lastModifiedAt: '2026-04-11T11:00:00Z' },
  { id: 'cfg-009', key: 'notifications.slack-channel', kind: 'notification', scopeType: 'workspace', scopeId: 'ws-default', parentId: 'cfg-008', status: 'active', hasDrift: false, lastModifiedAt: '2026-04-10T15:00:00Z' },
  { id: 'cfg-010', key: 'view.dark-mode', kind: 'view-rule', scopeType: 'platform', scopeId: '*', parentId: null, status: 'active', hasDrift: false, lastModifiedAt: '2026-04-09T08:00:00Z' },
  { id: 'cfg-011', key: 'field.req-priority-options', kind: 'field', scopeType: 'platform', scopeId: '*', parentId: null, status: 'active', hasDrift: false, lastModifiedAt: '2026-04-08T09:00:00Z' },
  { id: 'cfg-012', key: 'field.req-priority-options', kind: 'field', scopeType: 'application', scopeId: 'app-fin', parentId: 'cfg-011', status: 'active', hasDrift: true, lastModifiedAt: '2026-04-07T14:00:00Z' },
  { id: 'cfg-013', key: 'ai.model-preference', kind: 'ai-config', scopeType: 'platform', scopeId: '*', parentId: null, status: 'active', hasDrift: false, lastModifiedAt: '2026-04-06T10:00:00Z' },
  { id: 'cfg-014', key: 'ai.model-preference', kind: 'ai-config', scopeType: 'workspace', scopeId: 'ws-default', parentId: 'cfg-013', status: 'active', hasDrift: false, lastModifiedAt: '2026-04-05T11:00:00Z' },
  { id: 'cfg-015', key: 'ai.model-preference', kind: 'ai-config', scopeType: 'project', scopeId: 'proj-alpha', parentId: 'cfg-014', status: 'active', hasDrift: true, lastModifiedAt: '2026-04-04T16:00:00Z' },
  { id: 'cfg-016', key: 'deploy.canary-threshold', kind: 'flow-rule', scopeType: 'platform', scopeId: '*', parentId: null, status: 'inactive', hasDrift: false, lastModifiedAt: '2026-04-03T09:00:00Z' },
  { id: 'cfg-017', key: 'testing.coverage-target', kind: 'component', scopeType: 'platform', scopeId: '*', parentId: null, status: 'active', hasDrift: false, lastModifiedAt: '2026-04-02T10:00:00Z' },
  { id: 'cfg-018', key: 'testing.coverage-target', kind: 'component', scopeType: 'application', scopeId: 'app-fin', parentId: 'cfg-017', status: 'active', hasDrift: false, lastModifiedAt: '2026-04-01T14:00:00Z' },
  { id: 'cfg-019', key: 'incident.escalation-timeout', kind: 'flow-rule', scopeType: 'platform', scopeId: '*', parentId: null, status: 'active', hasDrift: false, lastModifiedAt: '2026-03-30T08:00:00Z' },
  { id: 'cfg-020', key: 'incident.escalation-timeout', kind: 'flow-rule', scopeType: 'workspace', scopeId: 'ws-default', parentId: 'cfg-019', status: 'active', hasDrift: false, lastModifiedAt: '2026-03-29T10:00:00Z' },
];

export const MOCK_DRIFT: DriftInfo = {
  hasDrift: true,
  driftFields: ['columns'],
  platformDefault: { columns: 2 },
  override: { columns: 3 },
};
