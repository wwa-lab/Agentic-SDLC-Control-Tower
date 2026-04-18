import type { ProjectSpaceAggregate } from '../types/aggregate';
import type { SdlcChainState } from '../types/chain';
import type { DependencyMap } from '../types/dependencies';
import type { EnvironmentMatrix } from '../types/environments';
import {
  ACCOUNTABLE_ROLE_ORDER,
  DEFAULT_PROJECT_ID,
  type HealthAggregate,
  type ProjectLifecycleStage,
  type RiskSeverity,
} from '../types/enums';
import type { LeadershipOwnership } from '../types/leadership';
import type { MilestoneHub } from '../types/milestones';
import type { RiskRegistry } from '../types/risks';
import type { MemberRef, ProjectSummary } from '../types/summary';

interface ProjectMockSeed {
  readonly summary: ProjectSummary;
  readonly leadership: LeadershipOwnership;
  readonly chain: SdlcChainState;
  readonly milestones: MilestoneHub;
  readonly dependencies: DependencyMap;
  readonly risks: RiskRegistry;
  readonly environments: EnvironmentMatrix;
}

const members: Record<string, MemberRef> = {
  grace: { memberId: 'u-007', displayName: 'Grace Hopper' },
  ada: { memberId: 'u-003', displayName: 'Ada Lovelace' },
  alan: { memberId: 'u-011', displayName: 'Alan Turing' },
  margaret: { memberId: 'u-020', displayName: 'Margaret Hamilton' },
  barbara: { memberId: 'u-015', displayName: 'Barbara Liskov' },
  carol: { memberId: 'u-021', displayName: 'Carol Shaw' },
  linus: { memberId: 'u-030', displayName: 'Linus Torvalds' },
  katherine: { memberId: 'u-012', displayName: 'Katherine Johnson' },
  hiro: { memberId: 'u-044', displayName: 'Hiro Tanaka' },
};

function summary(
  id: string,
  name: string,
  lifecycleStage: ProjectLifecycleStage,
  healthAggregate: HealthAggregate,
  factors: ReadonlyArray<ProjectSummary['healthFactors'][number]>,
  counters: ProjectSummary['counters'],
) : ProjectSummary {
  return {
    id,
    name,
    workspaceId: 'ws-default-001',
    workspaceName: 'Global SDLC Tower',
    applicationId: 'app-payment-gateway-pro',
    applicationName: 'Payment-Gateway-Pro',
    lifecycleStage,
    healthAggregate,
    healthFactors: factors,
    pm: members.grace,
    techLead: members.alan,
    activeMilestone: {
      id: `MS-${id.toUpperCase()}-02`,
      label: lifecycleStage === 'RETIRING' ? 'Cutover Completion' : 'Alpha Release',
      targetDate: lifecycleStage === 'RETIRING' ? '2026-05-28' : '2026-05-01',
    },
    counters,
    lastUpdatedAt: '2026-04-17T10:05:00Z',
    teamSpaceLink: '/team?workspaceId=ws-default-001',
  };
}

function leadership(projectId: string): LeadershipOwnership {
  const enabled = false;
  return {
    assignments: [
      {
        role: ACCOUNTABLE_ROLE_ORDER[0],
        memberId: members.grace.memberId,
        displayName: members.grace.displayName,
        oncallStatus: 'OFF',
        backupPresent: true,
        backupMemberId: members.katherine.memberId,
        backupDisplayName: members.katherine.displayName,
      },
      {
        role: ACCOUNTABLE_ROLE_ORDER[1],
        memberId: members.ada.memberId,
        displayName: members.ada.displayName,
        oncallStatus: 'OFF',
        backupPresent: false,
        backupMemberId: null,
        backupDisplayName: null,
      },
      {
        role: ACCOUNTABLE_ROLE_ORDER[2],
        memberId: members.alan.memberId,
        displayName: members.alan.displayName,
        oncallStatus: 'ON',
        backupPresent: true,
        backupMemberId: members.barbara.memberId,
        backupDisplayName: members.barbara.displayName,
      },
      {
        role: ACCOUNTABLE_ROLE_ORDER[3],
        memberId: members.margaret.memberId,
        displayName: members.margaret.displayName,
        oncallStatus: 'OFF',
        backupPresent: true,
        backupMemberId: members.carol.memberId,
        backupDisplayName: members.carol.displayName,
      },
      {
        role: ACCOUNTABLE_ROLE_ORDER[4],
        memberId: members.linus.memberId,
        displayName: members.linus.displayName,
        oncallStatus: projectId === 'proj-88' ? 'ON' : 'UPCOMING',
        backupPresent: projectId !== 'proj-88',
        backupMemberId: projectId === 'proj-88' ? null : members.hiro.memberId,
        backupDisplayName: projectId === 'proj-88' ? null : members.hiro.displayName,
      },
      {
        role: ACCOUNTABLE_ROLE_ORDER[5],
        memberId: projectId === 'proj-11' ? members.hiro.memberId : null,
        displayName: projectId === 'proj-11' ? members.hiro.displayName : null,
        oncallStatus: projectId === 'proj-11' ? 'OFF' : 'NONE',
        backupPresent: false,
        backupMemberId: null,
        backupDisplayName: null,
      },
    ],
    accessManagementLink: {
      url: `/platform?view=access&projectId=${projectId}`,
      enabled,
    },
  };
}

function chain(projectId: string, specCount: number, incidentCount: number, health: HealthAggregate): SdlcChainState {
  const workspaceQuery = 'workspaceId=ws-default-001';
  return {
    nodes: [
      {
        nodeKey: 'REQUIREMENT',
        label: 'Requirement',
        count: specCount + 5,
        health: 'GREEN',
        isExecutionHub: false,
        deepLink: `/requirements?projectId=${projectId}&${workspaceQuery}`,
        enabled: true,
      },
      {
        nodeKey: 'USER_STORY',
        label: 'User Story',
        count: specCount + 11,
        health: specCount > 5 ? 'YELLOW' : 'GREEN',
        isExecutionHub: false,
        deepLink: `/requirements?projectId=${projectId}&node=story&${workspaceQuery}`,
        enabled: true,
      },
      {
        nodeKey: 'SPEC',
        label: 'Spec',
        count: specCount,
        health,
        isExecutionHub: true,
        deepLink: `/requirements?projectId=${projectId}&node=spec&${workspaceQuery}`,
        enabled: true,
      },
      {
        nodeKey: 'ARCHITECTURE',
        label: 'Architecture',
        count: 3,
        health: 'GREEN',
        isExecutionHub: false,
        deepLink: `/design?projectId=${projectId}&node=arch&${workspaceQuery}`,
        enabled: false,
      },
      {
        nodeKey: 'DESIGN',
        label: 'Design',
        count: 5,
        health: 'GREEN',
        isExecutionHub: false,
        deepLink: `/design?projectId=${projectId}&${workspaceQuery}`,
        enabled: false,
      },
      {
        nodeKey: 'TASKS',
        label: 'Tasks',
        count: specCount * 6,
        health: specCount > 5 ? 'YELLOW' : 'GREEN',
        isExecutionHub: false,
        deepLink: `/code?projectId=${projectId}&node=tasks&${workspaceQuery}`,
        enabled: false,
      },
      {
        nodeKey: 'CODE',
        label: 'Code & Build',
        count: null,
        health: health === 'RED' ? 'YELLOW' : 'GREEN',
        isExecutionHub: false,
        deepLink: `/code?projectId=${projectId}&${workspaceQuery}`,
        enabled: false,
      },
      {
        nodeKey: 'TEST',
        label: 'Test',
        count: 12,
        health: health === 'RED' ? 'YELLOW' : 'GREEN',
        isExecutionHub: false,
        deepLink: `/testing?projectId=${projectId}&${workspaceQuery}`,
        enabled: false,
      },
      {
        nodeKey: 'DEPLOY',
        label: 'Deploy',
        count: 3,
        health: projectId === 'proj-88' ? 'YELLOW' : 'GREEN',
        isExecutionHub: false,
        deepLink: `/deployment?projectId=${projectId}&${workspaceQuery}`,
        enabled: false,
      },
      {
        nodeKey: 'INCIDENT',
        label: 'Incident',
        count: incidentCount,
        health: incidentCount > 0 ? (incidentCount > 1 ? 'RED' : 'YELLOW') : 'GREEN',
        isExecutionHub: false,
        deepLink: `/incidents?projectId=${projectId}&${workspaceQuery}`,
        enabled: true,
      },
      {
        nodeKey: 'LEARNING',
        label: 'Learning',
        count: null,
        health: 'GREEN',
        isExecutionHub: false,
        deepLink: `/reports/learning?projectId=${projectId}&${workspaceQuery}`,
        enabled: false,
      },
    ],
  };
}

function milestones(projectId: string, lateReason: string | null): MilestoneHub {
  return {
    milestones: [
      {
        id: `MS-${projectId.toUpperCase()}-01`,
        label: 'Discovery Complete',
        targetDate: '2026-03-15',
        status: 'COMPLETED',
        percentComplete: 100,
        owner: members.grace,
        isCurrent: false,
        slippageReason: null,
      },
      {
        id: `MS-${projectId.toUpperCase()}-02`,
        label: 'Alpha Release',
        targetDate: projectId === 'proj-88' ? '2026-04-24' : '2026-05-01',
        status: projectId === 'proj-88' ? 'AT_RISK' : 'IN_PROGRESS',
        percentComplete: projectId === 'proj-88' ? 42 : 60,
        owner: members.grace,
        isCurrent: true,
        slippageReason: projectId === 'proj-88' ? 'Rollback automation remains unstable' : null,
      },
      {
        id: `MS-${projectId.toUpperCase()}-03`,
        label: projectId === 'proj-88' ? 'Cutover Completion' : 'GA Launch',
        targetDate: projectId === 'proj-88' ? '2026-05-28' : '2026-06-30',
        status: lateReason ? 'AT_RISK' : 'NOT_STARTED',
        percentComplete: lateReason ? 20 : null,
        owner: lateReason ? members.grace : null,
        isCurrent: false,
        slippageReason: lateReason,
      },
    ],
    projectManagementLink: {
      url: `/project-management?projectId=${projectId}`,
      enabled: false,
    },
  };
}

function dependencies(projectId: string): DependencyMap {
  return {
    upstream: [
      {
        id: `${projectId}-up-identity`,
        targetName: 'Identity-Service-V2',
        targetRef: 'proj-identity-v2',
        targetProjectId: null,
        external: true,
        direction: 'UPSTREAM',
        relationship: 'API',
        ownerTeam: 'Identity Platform',
        health: projectId === 'proj-42' ? 'YELLOW' : 'GREEN',
        blockerReason: projectId === 'proj-42' ? 'Token exchange API is slipping one sprint' : null,
        primaryAction: null,
      },
      {
        id: `${projectId}-up-risk`,
        targetName: 'Compliance Evidence Pipeline',
        targetRef: 'proj-11',
        targetProjectId: 'proj-11',
        external: false,
        direction: 'UPSTREAM',
        relationship: 'DATA',
        ownerTeam: 'Governance Ops',
        health: 'GREEN',
        blockerReason: null,
        primaryAction: {
          label: 'Open Project Space',
          url: '/project-space/proj-11',
        },
      },
    ],
    downstream: [
      {
        id: `${projectId}-down-runtime`,
        targetName: 'Customer Checkout Runtime',
        targetRef: 'runtime-checkout',
        targetProjectId: null,
        external: true,
        direction: 'DOWNSTREAM',
        relationship: 'SLA',
        ownerTeam: 'Checkout Experience',
        health: projectId === 'proj-88' ? 'RED' : 'YELLOW',
        blockerReason: projectId === 'proj-88' ? 'Rollback window exceeds support SLA' : 'Release window is tightly coupled',
        primaryAction: {
          label: 'Open Incident',
          url: '/incidents/INC-0422',
        },
      },
      {
        id: `${projectId}-down-ops`,
        targetName: 'Fraud Detection Expansion',
        targetRef: 'proj-55',
        targetProjectId: 'proj-55',
        external: false,
        direction: 'DOWNSTREAM',
        relationship: 'SCHEDULE',
        ownerTeam: 'Fraud Platform',
        health: projectId === 'proj-11' ? 'GREEN' : 'YELLOW',
        blockerReason: null,
        primaryAction: {
          label: 'Open Project Space',
          url: '/project-space/proj-55',
        },
      },
    ],
  };
}

function risks(projectId: string, total: number): RiskRegistry {
  const severityRank: Record<RiskSeverity, number> = {
    CRITICAL: 0,
    HIGH: 1,
    MEDIUM: 2,
    LOW: 3,
  };
  const items: RiskRegistry['items'] = [
    {
      id: `${projectId}-risk-1`,
      title: projectId === 'proj-88' ? 'Rollback automation repeatedly fails smoke validation' : 'Identity dependency runway under 10 days',
      severity: (projectId === 'proj-88' ? 'CRITICAL' : 'HIGH') as RiskSeverity,
      category: 'DEPENDENCY',
      owner: members.alan,
      ageDays: projectId === 'proj-88' ? 9 : 4,
      latestNote: projectId === 'proj-88' ? 'Nightly rollback drill failed in STAGING' : 'Partner team moved completion to Sprint 19',
      primaryAction: {
        label: projectId === 'proj-88' ? 'Open Incident' : 'Review dependency lane',
        url: projectId === 'proj-88' ? '/incidents/INC-0422' : `/project-space/${projectId}`,
      },
      skillAttribution: {
        skillName: 'risk-registry-audit',
        executionId: `skill-run-${projectId}`,
      },
    },
    {
      id: `${projectId}-risk-2`,
      title: 'Approval backlog > 72h',
      severity: 'MEDIUM' as RiskSeverity,
      category: 'GOVERNANCE',
      owner: members.grace,
      ageDays: 3,
      latestNote: 'Architecture sign-off still pending',
      primaryAction: {
        label: 'Review approvals',
        url: `/platform?view=approvals&projectId=${projectId}`,
      },
    },
  ].slice(0, total) as RiskRegistry['items'];

  return {
    items: items
      .slice()
      .sort((left, right) => severityRank[left.severity] - severityRank[right.severity] || right.ageDays - left.ageDays),
    total,
    lastRefreshed: '2026-04-17T10:05:00Z',
  };
}

function environments(projectId: string, health: HealthAggregate): EnvironmentMatrix {
  return {
    environments: [
      {
        id: `${projectId}-env-dev`,
        label: 'DEV',
        kind: 'DEV',
        versionRef: 'v2.4.0-rc.6',
        buildId: 'build-1042',
        health: 'GREEN',
        gateStatus: 'AUTO',
        approver: null,
        lastDeployedAt: '2026-04-17T08:15:00Z',
        drift: null,
        deploymentLink: {
          url: `/deployment?projectId=${projectId}&envId=${projectId}-env-dev`,
          enabled: false,
        },
      },
      {
        id: `${projectId}-env-stage`,
        label: 'STAGING',
        kind: 'STAGING',
        versionRef: projectId === 'proj-88' ? 'v2.3.1-hotfix.3' : 'v2.4.0-rc.4',
        buildId: projectId === 'proj-88' ? 'build-998' : 'build-1035',
        health: health === 'RED' ? 'YELLOW' : 'GREEN',
        gateStatus: projectId === 'proj-42' ? 'APPROVAL_REQUIRED' : 'AUTO',
        approver: projectId === 'proj-42' ? members.ada : null,
        lastDeployedAt: '2026-04-17T06:45:00Z',
        drift: projectId === 'proj-88'
          ? {
              band: 'MAJOR',
              commitDelta: 18,
              sinceVersion: 'v2.3.1',
              description: 'STAGING trails PROD by 18 commits after rollback lock',
            }
          : {
              band: 'MINOR',
              commitDelta: 6,
              sinceVersion: 'v2.4.0',
              description: 'Awaiting final promotion batch',
            },
        deploymentLink: {
          url: `/deployment?projectId=${projectId}&envId=${projectId}-env-stage`,
          enabled: false,
        },
      },
      {
        id: `${projectId}-env-prod`,
        label: 'PROD',
        kind: 'PROD',
        versionRef: projectId === 'proj-88' ? 'v2.3.1' : 'v2.4.0',
        buildId: projectId === 'proj-88' ? 'build-975' : 'build-1028',
        health,
        gateStatus: projectId === 'proj-88' ? 'BLOCKED' : 'AUTO',
        approver: projectId === 'proj-88' ? members.linus : null,
        lastDeployedAt: '2026-04-16T23:50:00Z',
        drift: null,
        deploymentLink: {
          url: `/deployment?projectId=${projectId}&envId=${projectId}-env-prod`,
          enabled: false,
        },
      },
    ],
  };
}

const MOCKS: Record<string, ProjectMockSeed> = {
  'proj-42': {
    summary: summary(
      'proj-42',
      'Gateway Migration',
      'DELIVERY',
      'YELLOW',
      [
        { label: '1 dependency risk is trending late', severity: 'WARN' },
        { label: '3 approvals are older than 72h', severity: 'INFO' },
      ],
      {
        activeSpecs: 7,
        openIncidents: 1,
        pendingApprovals: 3,
        criticalHighRisks: 1,
      },
    ),
    leadership: leadership('proj-42'),
    chain: chain('proj-42', 7, 1, 'YELLOW'),
    milestones: milestones('proj-42', 'Upstream identity-service-v2 slipped one sprint'),
    dependencies: dependencies('proj-42'),
    risks: risks('proj-42', 2),
    environments: environments('proj-42', 'GREEN'),
  },
  'proj-11': {
    summary: summary(
      'proj-11',
      'Card Issuance',
      'DELIVERY',
      'GREEN',
      [{ label: 'Execution remains inside plan', severity: 'INFO' }],
      {
        activeSpecs: 4,
        openIncidents: 0,
        pendingApprovals: 1,
        criticalHighRisks: 0,
      },
    ),
    leadership: leadership('proj-11'),
    chain: chain('proj-11', 4, 0, 'GREEN'),
    milestones: milestones('proj-11', null),
    dependencies: dependencies('proj-11'),
    risks: risks('proj-11', 0),
    environments: environments('proj-11', 'GREEN'),
  },
  'proj-55': {
    summary: summary(
      'proj-55',
      'Fraud Detection Expansion',
      'DISCOVERY',
      'YELLOW',
      [
        { label: 'Spec decomposition is still shallow', severity: 'WARN' },
        { label: 'Pending policy exception review', severity: 'INFO' },
      ],
      {
        activeSpecs: 3,
        openIncidents: 0,
        pendingApprovals: 2,
        criticalHighRisks: 1,
      },
    ),
    leadership: leadership('proj-55'),
    chain: chain('proj-55', 3, 0, 'YELLOW'),
    milestones: milestones('proj-55', 'Discovery scope depends on governance exception'),
    dependencies: dependencies('proj-55'),
    risks: risks('proj-55', 2),
    environments: environments('proj-55', 'GREEN'),
  },
  'proj-88': {
    summary: summary(
      'proj-88',
      'Legacy Queue Decommission',
      'RETIRING',
      'RED',
      [
        { label: 'Rollback automation is failing', severity: 'CRIT' },
        { label: '1 live incident remains open', severity: 'CRIT' },
      ],
      {
        activeSpecs: 2,
        openIncidents: 2,
        pendingApprovals: 2,
        criticalHighRisks: 2,
      },
    ),
    leadership: leadership('proj-88'),
    chain: chain('proj-88', 2, 2, 'RED'),
    milestones: milestones('proj-88', 'Cutover is blocked until rollback confidence recovers'),
    dependencies: dependencies('proj-88'),
    risks: risks('proj-88', 2),
    environments: environments('proj-88', 'RED'),
  },
  'proj-07': {
    summary: summary(
      'proj-07',
      'Q1 Cost Reporting',
      'STEADY_STATE',
      'GREEN',
      [{ label: 'Archived project remains stable', severity: 'INFO' }],
      {
        activeSpecs: 0,
        openIncidents: 0,
        pendingApprovals: 0,
        criticalHighRisks: 0,
      },
    ),
    leadership: leadership('proj-07'),
    chain: chain('proj-07', 0, 0, 'GREEN'),
    milestones: {
      milestones: [],
      projectManagementLink: {
        url: '/project-management?projectId=proj-07',
        enabled: false,
      },
    },
    dependencies: {
      upstream: [],
      downstream: [],
    },
    risks: risks('proj-07', 0),
    environments: environments('proj-07', 'GREEN'),
  },
};

export function resolveProjectMockSeed(projectId: string): ProjectMockSeed {
  return MOCKS[projectId] ?? MOCKS[DEFAULT_PROJECT_ID];
}

export function createMockAggregate(projectId: string): ProjectSpaceAggregate {
  const seed = resolveProjectMockSeed(projectId);
  return {
    projectId: seed.summary.id,
    workspaceId: seed.summary.workspaceId,
    summary: { data: seed.summary, error: null },
    leadership: { data: seed.leadership, error: null },
    chain: { data: seed.chain, error: null },
    milestones: { data: seed.milestones, error: null },
    dependencies: { data: seed.dependencies, error: null },
    risks: { data: seed.risks, error: null },
    environments: { data: seed.environments, error: null },
  };
}
