/**
 * Mock data for Project Space view.
 * Replace with API calls when backend slice is implemented.
 */
export const MOCK_PROJECT = {
  version: 'v2.4.0-STABLE',
  status: 'IN_FLIGHT',
  statusLed: 'led-emerald' as const,
  milestone: 'MS-7: CLOUD_HANDOFF',
} as const;

export const MOCK_ENVIRONMENTS = [
  { name: 'PROD', led: 'led-emerald' as const },
  { name: 'STAGE', led: 'led-emerald' as const },
  { name: 'UAT', led: 'led-amber' as const },
] as const;

export const MOCK_RESOURCES = [
  'EKS_CLUSTER_01',
  'RDS_POSTGRES_MASTER',
] as const;
