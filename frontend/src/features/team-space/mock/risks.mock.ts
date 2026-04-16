import type { TeamRiskRadar } from '../types/risks';

export function risks(workspaceId: string): TeamRiskRadar {
  return {
    groups: {
      INCIDENT: [
        {
          id: 'RISK-1001',
          category: 'INCIDENT',
          severity: 'CRITICAL',
          title: 'P1: payment-service outage',
          detail: 'Payment throughput 0 rps for 12m',
          ageDays: 0,
          primaryAction: {
            label: 'Open Incident',
            url: '/incidents/INC-0422',
          },
        },
      ],
      APPROVAL: [
        {
          id: 'RISK-1002',
          category: 'APPROVAL',
          severity: 'HIGH',
          title: '4 spec approvals pending > 3d',
          detail: 'Oldest approval is waiting on SPEC-0088',
          ageDays: 3,
          primaryAction: {
            label: 'Review approvals',
            url: `/platform?view=approvals&workspaceId=${workspaceId}`,
          },
        },
      ],
      CONFIG_DRIFT: [
        {
          id: 'RISK-1003',
          category: 'CONFIG_DRIFT',
          severity: 'MEDIUM',
          title: 'Approval mode overridden at project level for 3 projects',
          detail: 'Projects: proj-42, proj-55, proj-88',
          ageDays: 12,
          primaryAction: {
            label: 'View in Platform Center',
            url: `/platform?view=config&workspaceId=${workspaceId}&section=approval`,
          },
          skillAttribution: {
            skillName: 'policy-drift-check',
            executionId: 'skill-run-3321',
          },
        },
      ],
      PROJECT: [],
      DEPENDENCY: [],
    },
    lastRefreshed: '2026-04-17T10:00:00Z',
    total: 3,
  };
}
