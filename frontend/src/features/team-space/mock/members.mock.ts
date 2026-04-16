import type { MemberMatrix } from '../types/members';

export function members(workspaceId: string): MemberMatrix {
  return {
    members: [
      {
        memberId: 'u-007',
        displayName: 'Grace Hopper',
        roles: ['TEAM_LEAD', 'APPROVER'],
        oncallStatus: 'OFF',
        keyPermissions: ['APPROVE', 'CONFIGURE'],
        lastActiveAt: '2026-04-17T09:42:00Z',
      },
      {
        memberId: 'u-011',
        displayName: 'Alan Turing',
        roles: ['SRE', 'ONCALL_PRIMARY'],
        oncallStatus: 'ON',
        keyPermissions: ['DEPLOY', 'TRIAGE'],
        lastActiveAt: '2026-04-17T10:05:00Z',
      },
      {
        memberId: 'u-003',
        displayName: 'Ada Lovelace',
        roles: ['ARCHITECT', 'APPROVER'],
        oncallStatus: 'UPCOMING',
        keyPermissions: ['REVIEW', 'APPROVE'],
        lastActiveAt: '2026-04-16T19:10:00Z',
      },
    ],
    coverageGaps: workspaceId === 'ws-legacy-001'
      ? [
          {
            kind: 'ONCALL_GAP',
            description: 'No primary oncall is configured for the next 24 hours',
            window: '2026-04-18 00:00 – 2026-04-19 00:00',
          },
        ]
      : [
          {
            kind: 'BACKUP_MISSING',
            description: 'No secondary oncall assigned for the 2026-04-19 – 2026-04-21 window',
            window: '2026-04-19 – 2026-04-21',
          },
        ],
    accessManagementLink: {
      url: `/platform?view=access&workspaceId=${workspaceId}`,
      enabled: false,
    },
  };
}
