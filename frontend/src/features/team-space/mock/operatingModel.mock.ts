import type { TeamOperatingModel } from '../types/operatingModel';

export function operatingModel(workspaceId: string): TeamOperatingModel {
  return {
    operatingMode: {
      value: workspaceId === 'ws-legacy-001' ? 'HIGH_GOVERNANCE' : 'STANDARD',
      lineage: {
        origin: workspaceId === 'ws-legacy-001' ? 'WORKSPACE' : 'APPLICATION',
        overridden: workspaceId === 'ws-legacy-001',
        chain: [
          {
            origin: 'PLATFORM',
            value: 'STANDARD',
            setAt: '2026-01-10T00:00:00Z',
            setBy: 'platform-admin',
          },
          {
            origin: 'APPLICATION',
            value: 'STANDARD',
            setAt: '2026-02-01T00:00:00Z',
            setBy: 'app-owner-payments',
          },
        ],
      },
    },
    approvalMode: {
      value: workspaceId === 'ws-legacy-001' ? 'MULTI_APPROVER' : 'REVIEWER_REQUIRED',
      lineage: {
        origin: 'WORKSPACE',
        overridden: true,
        chain: [],
      },
    },
    aiAutonomyLevel: {
      value: 'HUMAN_IN_LOOP',
      lineage: {
        origin: 'PLATFORM',
        overridden: false,
        chain: [],
      },
    },
    oncallOwner: {
      value: {
        memberId: 'u-011',
        displayName: 'Alan Turing',
        rotationRef: 'rot-fin-tech-primary',
      },
      lineage: {
        origin: 'WORKSPACE',
        overridden: false,
        chain: [],
      },
    },
    accountableOwners: [
      { area: 'DELIVERY', memberId: 'u-007', displayName: 'Grace Hopper' },
      { area: 'APPROVAL', memberId: 'u-003', displayName: 'Ada Lovelace' },
      { area: 'INCIDENT', memberId: 'u-011', displayName: 'Alan Turing' },
      { area: 'GOVERNANCE', memberId: 'u-007', displayName: 'Grace Hopper' },
    ],
    platformCenterLink: {
      url: `/platform?view=config&workspaceId=${workspaceId}&section=operating-model`,
      enabled: false,
    },
  };
}
