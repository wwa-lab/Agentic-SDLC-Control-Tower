import type { ProjectDistribution } from '../types/projects';

export function projects(_workspaceId: string): ProjectDistribution {
  return {
    groups: {
      HEALTHY: [
        {
          id: 'proj-11',
          name: 'Card Issuance',
          lifecycleStage: 'DELIVERY',
          healthStratum: 'HEALTHY',
          primaryRisk: null,
          activeSpecCount: 4,
          openIncidentCount: 0,
          projectSpaceUrl: '/project-space/proj-11',
        },
      ],
      AT_RISK: [
        {
          id: 'proj-42',
          name: 'Gateway Migration',
          lifecycleStage: 'DELIVERY',
          healthStratum: 'AT_RISK',
          primaryRisk: '2 blocked specs',
          activeSpecCount: 7,
          openIncidentCount: 1,
          projectSpaceUrl: '/project-space/proj-42',
        },
        {
          id: 'proj-55',
          name: 'Fraud Detection Expansion',
          lifecycleStage: 'DISCOVERY',
          healthStratum: 'AT_RISK',
          primaryRisk: 'Pending approval policy exception',
          activeSpecCount: 3,
          openIncidentCount: 0,
          projectSpaceUrl: '/project-space/proj-55',
        },
      ],
      CRITICAL: [
        {
          id: 'proj-88',
          name: 'Legacy Queue Decommission',
          lifecycleStage: 'RETIRING',
          healthStratum: 'CRITICAL',
          primaryRisk: 'Incident hotspot and rollback instability',
          activeSpecCount: 2,
          openIncidentCount: 2,
          projectSpaceUrl: '/project-space/proj-88',
        },
      ],
      ARCHIVED: [
        {
          id: 'proj-07',
          name: 'Q1 Cost Reporting',
          lifecycleStage: 'STEADY_STATE',
          healthStratum: 'ARCHIVED',
          primaryRisk: null,
          activeSpecCount: 0,
          openIncidentCount: 0,
          projectSpaceUrl: '/project-space/proj-07',
        },
      ],
    },
    totals: {
      HEALTHY: 1,
      AT_RISK: 2,
      CRITICAL: 1,
      ARCHIVED: 1,
    },
  };
}
