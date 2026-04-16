/**
 * Mock data for Platform Center view.
 * Replace with API calls when backend slice is implemented.
 */
export const MOCK_POLICIES = [
  { key: 'soc2', label: 'SOC2_COMPLIANCE', led: 'led-emerald' as const },
  { key: 'aws', label: 'AWS_SECURITY_GROUPS', led: 'led-emerald' as const },
  { key: 'deploy', label: 'DEPLOY_GATED_PR', led: 'led-emerald' as const },
] as const;

export const MOCK_AUDIT_ENTRIES = [
  { time: '08:12', actor: 'USER_ADMIN', action: 'updated SNOW Group FIN-TECH' },
  { time: '09:05', actor: 'AI_ENGINE', action: 'updated policy DEPLOY_GATED' },
  { time: '09:42', actor: 'SYSTEM', action: 'accessed Project Space v2.4' },
] as const;

export const PLATFORM_ACTIONS = [
  { key: 'manage-roles', label: 'MANAGE_ROLES' },
  { key: 'invite-team', label: 'INVITE_TEAM' },
] as const;
