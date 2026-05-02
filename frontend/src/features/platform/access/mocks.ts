import type { RoleAssignment } from '../shared/types';

export const MOCK_ROLE_ASSIGNMENTS: RoleAssignment[] = [
  { id: 'ra-001', staffId: '43910516', userDisplayName: 'Platform Admin', role: 'PLATFORM_ADMIN', scopeType: 'platform', scopeId: '*', grantedBy: 'system', grantedAt: '2026-03-01T10:00:00Z' },
  { id: 'ra-002', staffId: '43910001', userDisplayName: 'Alice Chen', role: 'PLATFORM_ADMIN', scopeType: 'platform', scopeId: '*', grantedBy: '43910516', grantedAt: '2026-03-15T09:00:00Z' },
  { id: 'ra-003', staffId: '43910002', userDisplayName: 'Bob Smith', role: 'WORKSPACE_ADMIN', scopeType: 'workspace', scopeId: 'ws-default', grantedBy: '43910516', grantedAt: '2026-03-20T14:00:00Z' },
  { id: 'ra-004', staffId: '43910003', userDisplayName: 'Carol Wang', role: 'WORKSPACE_MEMBER', scopeType: 'workspace', scopeId: 'ws-default', grantedBy: '43910516', grantedAt: '2026-04-01T10:00:00Z' },
  { id: 'ra-005', staffId: '43910004', userDisplayName: 'Dave Johnson', role: 'WORKSPACE_VIEWER', scopeType: 'workspace', scopeId: 'ws-default', grantedBy: '43910002', grantedAt: '2026-04-05T11:00:00Z' },
  { id: 'ra-006', staffId: '43910005', userDisplayName: 'Eve Martinez', role: 'AUDITOR', scopeType: 'platform', scopeId: '*', grantedBy: '43910516', grantedAt: '2026-04-10T09:00:00Z' },
  { id: 'ra-007', staffId: '43910006', userDisplayName: 'Frank Lee', role: 'WORKSPACE_MEMBER', scopeType: 'workspace', scopeId: 'ws-default', grantedBy: '43910002', grantedAt: '2026-04-12T15:00:00Z' },
  { id: 'ra-008', staffId: '43910007', userDisplayName: 'Grace Kim', role: 'WORKSPACE_ADMIN', scopeType: 'workspace', scopeId: 'ws-fin', grantedBy: '43910516', grantedAt: '2026-04-14T10:00:00Z' },
];
