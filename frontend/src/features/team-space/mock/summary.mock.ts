import type { WorkspaceSummary } from '../types/workspace';

export function summary(workspaceId: string): WorkspaceSummary {
  if (workspaceId === 'ws-legacy-001') {
    return {
      id: workspaceId,
      name: 'Legacy Mainframe',
      applicationId: 'app-legacy-core',
      applicationName: 'Legacy Core',
      snowGroupId: null,
      snowGroupName: null,
      activeProjectCount: 2,
      activeEnvironmentCount: 1,
      healthAggregate: 'YELLOW',
      ownerId: 'u-099',
      ownerDisplayName: 'Katherine Johnson',
      compatibilityMode: true,
      responsibilityBoundary: {
        applications: ['Legacy Core'],
        snowGroups: [],
        projectCount: 2,
      },
    };
  }

  return {
    id: workspaceId,
    name: 'Global SDLC Tower',
    applicationId: 'app-payment-gateway-pro',
    applicationName: 'Payment-Gateway-Pro',
    snowGroupId: 'snow-fin-tech-ops',
    snowGroupName: 'FIN-TECH-OPS',
    activeProjectCount: 7,
    activeEnvironmentCount: 4,
    healthAggregate: 'YELLOW',
    ownerId: 'u-007',
    ownerDisplayName: 'Grace Hopper',
    compatibilityMode: false,
    responsibilityBoundary: {
      applications: ['Payment-Gateway-Pro'],
      snowGroups: ['FIN-TECH-OPS'],
      projectCount: 7,
    },
  };
}
