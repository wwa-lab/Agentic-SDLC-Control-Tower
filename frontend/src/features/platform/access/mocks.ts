import type { RoleAssignment } from '../shared/types';

export const MOCK_ROLE_ASSIGNMENTS: RoleAssignment[] = [
  { id: 'ra-001', userId: 'admin@sdlctower.local', userDisplayName: 'Platform Admin', role: 'PLATFORM_ADMIN', scopeType: 'platform', scopeId: '*', grantedBy: 'system', grantedAt: '2026-03-01T10:00:00Z' },
  { id: 'ra-002', userId: 'alice@corp.example', userDisplayName: 'Alice Chen', role: 'PLATFORM_ADMIN', scopeType: 'platform', scopeId: '*', grantedBy: 'admin@sdlctower.local', grantedAt: '2026-03-15T09:00:00Z' },
  { id: 'ra-003', userId: 'bob@corp.example', userDisplayName: 'Bob Smith', role: 'WORKSPACE_ADMIN', scopeType: 'workspace', scopeId: 'ws-default', grantedBy: 'admin@sdlctower.local', grantedAt: '2026-03-20T14:00:00Z' },
  { id: 'ra-004', userId: 'carol@corp.example', userDisplayName: 'Carol Wang', role: 'WORKSPACE_MEMBER', scopeType: 'workspace', scopeId: 'ws-default', grantedBy: 'admin@sdlctower.local', grantedAt: '2026-04-01T10:00:00Z' },
  { id: 'ra-005', userId: 'dave@corp.example', userDisplayName: 'Dave Johnson', role: 'WORKSPACE_VIEWER', scopeType: 'workspace', scopeId: 'ws-default', grantedBy: 'bob@corp.example', grantedAt: '2026-04-05T11:00:00Z' },
  { id: 'ra-006', userId: 'eve@corp.example', userDisplayName: 'Eve Martinez', role: 'AUDITOR', scopeType: 'platform', scopeId: '*', grantedBy: 'admin@sdlctower.local', grantedAt: '2026-04-10T09:00:00Z' },
  { id: 'ra-007', userId: 'frank@corp.example', userDisplayName: 'Frank Lee', role: 'WORKSPACE_MEMBER', scopeType: 'workspace', scopeId: 'ws-default', grantedBy: 'bob@corp.example', grantedAt: '2026-04-12T15:00:00Z' },
  { id: 'ra-008', userId: 'grace@corp.example', userDisplayName: 'Grace Kim', role: 'WORKSPACE_ADMIN', scopeType: 'workspace', scopeId: 'ws-fin', grantedBy: 'admin@sdlctower.local', grantedAt: '2026-04-14T10:00:00Z' },
];
