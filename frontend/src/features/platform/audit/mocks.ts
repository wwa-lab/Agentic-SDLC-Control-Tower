import type { AuditRecord, AuditCategory, AuditOutcome, ActorType, ScopeType } from '../shared/types';

function aud(id: string, ts: string, actor: string, actorType: ActorType, category: AuditCategory, action: string, objectType: string, objectId: string, scopeType: ScopeType, scopeId: string, outcome: AuditOutcome): AuditRecord {
  return { id, timestamp: ts, actor, actorType, category, action, objectType, objectId, scopeType, scopeId, outcome, evidenceRef: null, payload: {} };
}

export const MOCK_AUDIT_RECORDS: AuditRecord[] = [
  aud('aud-001', '2026-04-18T09:30:00Z', 'admin@sdlctower.local', 'user', 'config_change', 'template.publish', 'template', 'tmpl-001', 'platform', '*', 'success'),
  aud('aud-002', '2026-04-18T09:15:00Z', 'admin@sdlctower.local', 'user', 'permission_change', 'role.assign', 'role-assignment', 'ra-002', 'platform', '*', 'success'),
  aud('aud-003', '2026-04-18T08:45:00Z', 'auto-pipeline', 'skill', 'ai_execution', 'skill.execute', 'skill', 'sk-001', 'workspace', 'ws-default', 'success'),
  aud('aud-004', '2026-04-18T08:30:00Z', 'system', 'system', 'deployment_event', 'deploy.start', 'deploy', 'dep-100', 'workspace', 'ws-default', 'success'),
  aud('aud-005', '2026-04-18T08:00:00Z', 'admin@sdlctower.local', 'user', 'config_change', 'configuration.override', 'configuration', 'cfg-002', 'workspace', 'ws-default', 'success'),
  aud('aud-006', '2026-04-17T22:00:00Z', 'auto-detector', 'skill', 'incident_event', 'incident.diagnose', 'incident', 'INC-0425', 'workspace', 'ws-default', 'success'),
  aud('aud-007', '2026-04-17T20:00:00Z', 'admin@sdlctower.local', 'user', 'approval_decision', 'action.approve', 'incident-action', 'act-010', 'workspace', 'ws-default', 'success'),
  aud('aud-008', '2026-04-17T18:00:00Z', 'system', 'system', 'policy_hit', 'policy.evaluate', 'policy', 'pol-001', 'platform', '*', 'success'),
  aud('aud-009', '2026-04-17T16:00:00Z', 'admin@sdlctower.local', 'user', 'config_change', 'template.create', 'template', 'tmpl-005', 'platform', '*', 'success'),
  aud('aud-010', '2026-04-17T14:00:00Z', 'admin@sdlctower.local', 'user', 'permission_change', 'role.revoke', 'role-assignment', 'ra-old', 'workspace', 'ws-default', 'success'),
  aud('aud-011', '2026-04-17T12:00:00Z', 'auto-pipeline', 'skill', 'skill_execution', 'skill.req-to-story', 'skill', 'sk-001', 'workspace', 'ws-default', 'success'),
  aud('aud-012', '2026-04-17T10:00:00Z', 'admin@sdlctower.local', 'user', 'config_change', 'policy.activate', 'policy', 'pol-002', 'platform', '*', 'success'),
  aud('aud-013', '2026-04-17T08:00:00Z', 'system', 'system', 'integration.test', 'connection.test', 'connection', 'conn-001', 'workspace', 'ws-default', 'success'),
  aud('aud-014', '2026-04-16T22:00:00Z', 'admin@sdlctower.local', 'user', 'ai_suggestion', 'ai.suggest', 'requirement', 'REQ-101', 'workspace', 'ws-default', 'success'),
  aud('aud-015', '2026-04-16T20:00:00Z', 'system', 'system', 'deployment_event', 'deploy.rollback', 'deploy', 'dep-099', 'workspace', 'ws-default', 'rolled_back'),
  aud('aud-016', '2026-04-16T18:00:00Z', 'admin@sdlctower.local', 'user', 'permission_change', 'role.assign', 'role-assignment', 'ra-003', 'platform', '*', 'success'),
  aud('aud-017', '2026-04-16T16:00:00Z', 'auto-pipeline', 'skill', 'ai_execution', 'skill.execute', 'skill', 'sk-006', 'workspace', 'ws-default', 'failure'),
  aud('aud-018', '2026-04-16T14:00:00Z', 'admin@sdlctower.local', 'user', 'approval_decision', 'action.reject', 'incident-action', 'act-008', 'workspace', 'ws-default', 'rejected'),
  ...Array.from({ length: 32 }, (_, i) => aud(`aud-${19 + i}`, `2026-04-${String(15 - Math.floor(i / 4)).padStart(2, '0')}T${String(8 + (i % 12)).padStart(2, '0')}:00:00Z`, i % 3 === 0 ? 'system' : 'admin@sdlctower.local', i % 3 === 0 ? 'system' : 'user', (['config_change', 'permission_change', 'ai_execution', 'deployment_event', 'policy_hit', 'skill_execution'] as const)[i % 6], `action.${i}`, 'entity', `ent-${i}`, 'platform', '*', 'success')),
];
