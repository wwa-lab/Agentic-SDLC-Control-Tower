import type { SectionResult } from '@/shared/types/section';
import type {
  AiSuggestion,
  AiSuggestionActionResult,
  CapacityCell,
  CapacityMemberRef,
  CapacityMilestoneRef,
  CadenceMetric,
  ChangeLogEntry,
  ChangeLogPage,
  CounterSignDependencyRequest,
  Dependency,
  DependencyBottleneck,
  DismissAiSuggestionRequest,
  HealthIndicator,
  Milestone,
  PlanAggregate,
  PlanCapacityMatrix,
  PlanHeader,
  PortfolioAggregate,
  PortfolioCapacity,
  PortfolioCapacityRow,
  PortfolioHeatmap,
  PortfolioHeatmapCell,
  PortfolioHeatmapRow,
  PortfolioRiskConcentration,
  PortfolioSummary,
  ProgressNode,
  RiskItem,
  SaveCapacityBatchRequest,
  SeverityCategoryCount,
  TransitionDependencyRequest,
  TransitionMilestoneRequest,
  TransitionRiskRequest,
} from '../types';

interface PlanRuntime {
  header: Mutable<PlanHeader>;
  milestones: Mutable<Milestone>[];
  capacity: Mutable<PlanCapacityMatrix>;
  risks: Mutable<RiskItem>[];
  dependencies: Mutable<Dependency>[];
  progress: Mutable<ProgressNode>[];
  changeLog: Mutable<ChangeLogPage>;
  aiSuggestions: Mutable<AiSuggestion>[];
}

interface ProjectSeed {
  readonly projectId: string;
  readonly projectName: string;
  readonly applicationId: string;
  readonly applicationName: string;
  readonly workspaceId: string;
  readonly workspaceName: string;
  readonly lifecycleStage: string;
  readonly planHealth: HealthIndicator;
  readonly autonomyLevel: string;
  readonly planRevision: number;
  readonly updatedAt: string;
}

const MEMBER_DIRECTORY: Record<string, string> = {
  'u-003': 'Ada Lovelace',
  'u-007': 'Grace Hopper',
  'u-011': 'Alan Turing',
  'u-012': 'Katherine Johnson',
  'u-015': 'Barbara Liskov',
  'u-020': 'Margaret Hamilton',
  'u-021': 'Carol Shaw',
  'u-030': 'Linus Torvalds',
  'u-044': 'Hiro Tanaka',
};

const DEFAULT_MEMBERS: ReadonlyArray<CapacityMemberRef> = [
  { id: 'u-011', displayName: MEMBER_DIRECTORY['u-011'], hasBackup: true, onCall: false },
  { id: 'u-020', displayName: MEMBER_DIRECTORY['u-020'], hasBackup: true, onCall: false },
  { id: 'u-030', displayName: MEMBER_DIRECTORY['u-030'], hasBackup: true, onCall: true },
];

const SEEDS: ReadonlyArray<ProjectSeed> = [
  {
    projectId: 'proj-42',
    projectName: 'Gateway Migration',
    applicationId: 'app-gateway',
    applicationName: 'Payment Gateway Pro',
    workspaceId: 'ws-default-001',
    workspaceName: 'Global SDLC Tower',
    lifecycleStage: 'DELIVERY',
    planHealth: 'YELLOW',
    autonomyLevel: 'PM_ROUTED',
    planRevision: 4,
    updatedAt: '2026-04-17T10:05:00Z',
  },
  {
    projectId: 'proj-11',
    projectName: 'Card Issuance',
    applicationId: 'app-cards',
    applicationName: 'Card Issuance Core',
    workspaceId: 'ws-default-001',
    workspaceName: 'Global SDLC Tower',
    lifecycleStage: 'DELIVERY',
    planHealth: 'GREEN',
    autonomyLevel: 'AUTO_WITH_GUARDRAILS',
    planRevision: 2,
    updatedAt: '2026-04-17T09:00:00Z',
  },
  {
    projectId: 'proj-55',
    projectName: 'Fraud Detection Expansion',
    applicationId: 'app-fraud',
    applicationName: 'Fraud Mesh',
    workspaceId: 'ws-default-001',
    workspaceName: 'Global SDLC Tower',
    lifecycleStage: 'DISCOVERY',
    planHealth: 'YELLOW',
    autonomyLevel: 'REVIEW_REQUIRED',
    planRevision: 3,
    updatedAt: '2026-04-17T09:15:00Z',
  },
  {
    projectId: 'proj-88',
    projectName: 'Legacy Queue Decommission',
    applicationId: 'app-queue',
    applicationName: 'Legacy Queue Runtime',
    workspaceId: 'ws-default-001',
    workspaceName: 'Global SDLC Tower',
    lifecycleStage: 'RETIRING',
    planHealth: 'RED',
    autonomyLevel: 'INCIDENT_ESCORTED',
    planRevision: 5,
    updatedAt: '2026-04-17T10:05:00Z',
  },
  {
    projectId: 'proj-07',
    projectName: 'Q1 Cost Reporting',
    applicationId: 'app-reporting',
    applicationName: 'Finance Command',
    workspaceId: 'ws-default-001',
    workspaceName: 'Global SDLC Tower',
    lifecycleStage: 'STEADY_STATE',
    planHealth: 'GREEN',
    autonomyLevel: 'AUTO_WITH_GUARDRAILS',
    planRevision: 1,
    updatedAt: '2026-04-16T10:05:00Z',
  },
  {
    projectId: 'proj-degraded-001',
    projectName: 'Degraded Project Feed',
    applicationId: 'app-feed',
    applicationName: 'Signal Mesh',
    workspaceId: 'ws-default-001',
    workspaceName: 'Global SDLC Tower',
    lifecycleStage: 'DELIVERY',
    planHealth: 'YELLOW',
    autonomyLevel: 'REVIEW_REQUIRED',
    planRevision: 1,
    updatedAt: '2026-04-17T08:30:00Z',
  },
];

const STATUS_TO_HEALTH: Record<string, HealthIndicator> = {
  NOT_STARTED: 'UNKNOWN',
  IN_PROGRESS: 'GREEN',
  AT_RISK: 'YELLOW',
  COMPLETED: 'GREEN',
  SLIPPED: 'RED',
  ARCHIVED: 'UNKNOWN',
  PROPOSED: 'UNKNOWN',
  NEGOTIATING: 'YELLOW',
  APPROVED: 'GREEN',
  REJECTED: 'RED',
  RESOLVED: 'GREEN',
  IDENTIFIED: 'YELLOW',
  ACKNOWLEDGED: 'YELLOW',
  MITIGATING: 'YELLOW',
  ESCALATED: 'RED',
};

const PROGRESS_NODES = [
  'Requirements',
  'Analysis',
  'Design',
  'Plan',
  'Build',
  'Test',
  'Deploy',
  'Operate',
  'Incident',
  'Risk',
  'Governance',
] as const;

let logSequence = 900;

type Mutable<T> =
  T extends ReadonlyArray<infer U> ? Mutable<U>[] :
  T extends object ? { -readonly [K in keyof T]: Mutable<T[K]> } :
  T;

function section<T>(data: T): SectionResult<T> {
  return {
    data,
    error: null,
  };
}

function clone<T>(value: T): T {
  return structuredClone(value);
}

function memberName(memberId: string | null): string | null {
  return memberId ? MEMBER_DIRECTORY[memberId] ?? memberId : null;
}

function severityRank(severity: string): number {
  return ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].indexOf(severity);
}

function toJson(value: unknown): string {
  return JSON.stringify(value, null, 2);
}

function computeTotals(cells: ReadonlyArray<CapacityCell>) {
  const rowTotals: Record<string, number> = {};
  const columnTotals: Record<string, number> = {};

  cells.forEach(cell => {
    rowTotals[cell.memberId] = (rowTotals[cell.memberId] ?? 0) + cell.percent;
    columnTotals[cell.milestoneId] = (columnTotals[cell.milestoneId] ?? 0) + cell.percent;
  });

  return { rowTotals, columnTotals };
}

function buildCapacity(
  planRevision: number,
  milestones: ReadonlyArray<CapacityMilestoneRef>,
  cells: ReadonlyArray<Omit<CapacityCell, 'planRevision'>>,
): PlanCapacityMatrix {
  const normalizedCells: CapacityCell[] = cells.map(cell => ({
    ...cell,
    planRevision,
  }));
  const totals = computeTotals(normalizedCells);
  return {
    milestones,
    members: clone(DEFAULT_MEMBERS),
    cells: normalizedCells,
    rowTotals: totals.rowTotals,
    columnTotals: totals.columnTotals,
    underThreshold: Object.values(totals.rowTotals).filter(total => total < 45).length,
    planRevision,
  };
}

function makeLogEntry(
  projectId: string,
  action: string,
  targetType: string,
  targetId: string,
  beforeJson: string | null,
  afterJson: string | null,
  reason: string | null,
  actorType: 'HUMAN' | 'AI' = 'HUMAN',
  actorMemberId: string | null = actorType === 'HUMAN' ? 'u-007' : null,
): ChangeLogEntry {
  logSequence += 1;
  return {
    id: `pm-log-${logSequence}`,
    projectId,
    actorType,
    actorMemberId,
    actorDisplayName: memberName(actorMemberId),
    skillExecutionId: actorType === 'AI' ? `skill-pm-${logSequence}` : null,
    action,
    targetType,
    targetId,
    beforeJson,
    afterJson,
    reason,
    correlationId: `corr-pm-${logSequence}`,
    auditLinkId: `audit-pm-${logSequence}`,
    at: new Date().toISOString(),
  };
}

function buildProgress(projectId: string, health: HealthIndicator, throughputBase: number): ProgressNode[] {
  return PROGRESS_NODES.map((node, index) => ({
    node,
    throughput: Math.max(0, throughputBase + (index % 3) - (health === 'RED' ? 2 : 0)),
    priorThroughput: Math.max(0, throughputBase + ((index + 1) % 3) - 1),
    health: index > 6 && health !== 'GREEN' ? health : index === 0 ? 'GREEN' : 'YELLOW',
    slipped: health === 'RED' && index >= 5,
    deepLink:
      node === 'Requirements'
        ? '/requirements'
        : node === 'Incident'
          ? '/incidents'
          : node === 'Governance'
            ? '/platform'
            : `/project-space/${projectId}`,
  }));
}

function buildPlan(seed: ProjectSeed): PlanRuntime {
  const milestoneRefs: CapacityMilestoneRef[] = [
    { id: `${seed.projectId}-m1`, label: 'Scope Lock', ordering: 1 },
    { id: `${seed.projectId}-m2`, label: 'Execution Lane', ordering: 2 },
    { id: `${seed.projectId}-m3`, label: 'Release Gate', ordering: 3 },
  ];

  const milestoneStatus2 =
    seed.projectId === 'proj-88'
      ? 'SLIPPED'
      : seed.projectId === 'proj-42' || seed.projectId === 'proj-55' || seed.projectId === 'proj-degraded-001'
        ? 'AT_RISK'
        : 'IN_PROGRESS';

  const milestones: Milestone[] = [
    {
      id: `${seed.projectId}-m1`,
      projectId: seed.projectId,
      label: 'Scope Lock',
      description: 'Requirements, interfaces, and operational guardrails are locked.',
      targetDate: '2026-04-22',
      status: 'COMPLETED',
      percentComplete: 100,
      ownerMemberId: 'u-007',
      ownerDisplayName: memberName('u-007'),
      slippageReason: null,
      ordering: 1,
      slippage: {
        score: 'NONE',
        factors: [],
        computedAt: seed.updatedAt,
      },
      planRevision: seed.planRevision,
      createdAt: '2026-04-05T08:00:00Z',
      completedAt: '2026-04-20T16:00:00Z',
      archivedAt: null,
    },
    {
      id: `${seed.projectId}-m2`,
      projectId: seed.projectId,
      label: seed.projectId === 'proj-07' ? 'Monthly Refresh' : 'Execution Lane',
      description: 'Delivery plan is reconciled across PM, tech lead, and dependency owners.',
      targetDate: seed.projectId === 'proj-88' ? '2026-04-28' : '2026-05-01',
      status: milestoneStatus2 as Milestone['status'],
      percentComplete: seed.projectId === 'proj-88' ? 46 : seed.projectId === 'proj-07' ? 78 : 68,
      ownerMemberId: 'u-011',
      ownerDisplayName: memberName('u-011'),
      slippageReason:
        seed.projectId === 'proj-88'
          ? 'Rollback rehearsal remains below confidence threshold.'
          : seed.projectId === 'proj-42'
            ? 'Identity provider contract drifted one sprint.'
            : seed.projectId === 'proj-55'
              ? 'Governance exception still pending.'
              : seed.projectId === 'proj-degraded-001'
                ? 'Feed quality is unstable across the last 48h.'
                : null,
      ordering: 2,
      slippage: {
        score: seed.projectId === 'proj-88' ? 'HIGH' : milestoneStatus2 === 'AT_RISK' ? 'MEDIUM' : 'LOW',
        factors:
          milestoneStatus2 === 'IN_PROGRESS'
            ? [{ label: 'Cross-team cadence', evidence: 'No blocking signal on the last checkpoint.' }]
            : [
                { label: 'Dependency drift', evidence: 'Counterpart delivery date moved inside the same sprint.' },
                { label: 'Approval latency', evidence: 'Escalation remains open in governance queue.' },
              ],
        computedAt: seed.updatedAt,
      },
      planRevision: seed.planRevision,
      createdAt: '2026-04-10T08:00:00Z',
      completedAt: null,
      archivedAt: null,
    },
    {
      id: `${seed.projectId}-m3`,
      projectId: seed.projectId,
      label: seed.projectId === 'proj-07' ? 'Quarter Close' : 'Release Gate',
      description: 'Readiness checkpoint for live release or steady-state review.',
      targetDate: seed.projectId === 'proj-88' ? '2026-05-16' : '2026-05-20',
      status: seed.projectId === 'proj-07' ? 'IN_PROGRESS' : 'NOT_STARTED',
      percentComplete: seed.projectId === 'proj-07' ? 35 : 12,
      ownerMemberId: 'u-020',
      ownerDisplayName: memberName('u-020'),
      slippageReason: null,
      ordering: 3,
      slippage: {
        score: seed.projectId === 'proj-07' ? 'LOW' : 'MEDIUM',
        factors: [{ label: 'Upstream gating', evidence: 'Release gate remains coupled to shared approval lane.' }],
        computedAt: seed.updatedAt,
      },
      planRevision: seed.planRevision,
      createdAt: '2026-04-12T08:00:00Z',
      completedAt: null,
      archivedAt: null,
    },
  ];

  const capacity = buildCapacity(
    seed.planRevision,
    milestoneRefs,
    [
      {
        id: `${seed.projectId}-cap-1`,
        memberId: 'u-011',
        milestoneId: `${seed.projectId}-m2`,
        percent: seed.projectId === 'proj-42' ? 65 : seed.projectId === 'proj-88' ? 80 : 45,
        justification: seed.projectId === 'proj-88' ? 'Incident escort for rollback rehearsals.' : null,
        windowStart: '2026-04-28',
        windowEnd: '2026-05-04',
      },
      {
        id: `${seed.projectId}-cap-2`,
        memberId: 'u-020',
        milestoneId: `${seed.projectId}-m2`,
        percent: seed.projectId === 'proj-55' ? 60 : 35,
        justification: null,
        windowStart: '2026-04-28',
        windowEnd: '2026-05-04',
      },
      {
        id: `${seed.projectId}-cap-3`,
        memberId: 'u-030',
        milestoneId: `${seed.projectId}-m3`,
        percent: seed.projectId === 'proj-88' ? 55 : 20,
        justification: seed.projectId === 'proj-88' ? 'Rollback drills extend into support window.' : null,
        windowStart: '2026-05-05',
        windowEnd: '2026-05-11',
      },
    ],
  );

  const risks: RiskItem[] = [
    {
      id: `${seed.projectId}-risk-1`,
      projectId: seed.projectId,
      title:
        seed.projectId === 'proj-88'
          ? 'Rollback automation remains unstable in staging'
          : seed.projectId === 'proj-07'
            ? 'Report dependency on cost feed freshness'
            : 'Counterpart delivery lane is slipping',
      severity: seed.projectId === 'proj-88' ? 'CRITICAL' : seed.projectId === 'proj-07' ? 'LOW' : 'HIGH',
      category: seed.projectId === 'proj-07' ? 'DEPENDENCY' : 'DELIVERY',
      state: seed.projectId === 'proj-88' ? 'ESCALATED' : seed.projectId === 'proj-07' ? 'MITIGATING' : 'MITIGATING',
      ownerMemberId: seed.projectId === 'proj-07' ? 'u-020' : 'u-011',
      ownerDisplayName: memberName(seed.projectId === 'proj-07' ? 'u-020' : 'u-011'),
      mitigationNote:
        seed.projectId === 'proj-88'
          ? 'Rollback drill added to nightly validation window.'
          : 'Dependency review added to each daily checkpoint.',
      resolutionNote: null,
      linkedIncidentId: seed.projectId === 'proj-88' ? 'INC-0422' : null,
      ageDays: seed.projectId === 'proj-88' ? 9 : 4,
      detectedAt: '2026-04-13T12:00:00Z',
      resolvedAt: null,
      planRevision: seed.planRevision,
    },
    {
      id: `${seed.projectId}-risk-2`,
      projectId: seed.projectId,
      title:
        seed.projectId === 'proj-11'
          ? 'PCI sign-off waiting on evidence pack'
          : seed.projectId === 'proj-07'
            ? 'Quarter close checklist has single reviewer'
            : 'Approval cycle time exceeds weekly target',
      severity: seed.projectId === 'proj-11' || seed.projectId === 'proj-07' ? 'MEDIUM' : 'HIGH',
      category: 'GOVERNANCE',
      state: seed.projectId === 'proj-11' ? 'ACKNOWLEDGED' : seed.projectId === 'proj-07' ? 'IDENTIFIED' : 'IDENTIFIED',
      ownerMemberId: 'u-007',
      ownerDisplayName: memberName('u-007'),
      mitigationNote: seed.projectId === 'proj-11' ? 'Evidence pack queued for next review board.' : null,
      resolutionNote: null,
      linkedIncidentId: null,
      ageDays: seed.projectId === 'proj-07' ? 2 : 5,
      detectedAt: '2026-04-14T15:00:00Z',
      resolvedAt: null,
      planRevision: seed.planRevision,
    },
  ];

  const dependencies: Dependency[] = [
    {
      id: `${seed.projectId}-dep-1`,
      projectId: seed.projectId,
      targetRef: seed.projectId === 'proj-42' ? 'proj-11' : 'identity-service-v2',
      targetProjectId: seed.projectId === 'proj-42' ? 'proj-11' : null,
      targetDescriptor: seed.projectId === 'proj-42' ? 'Card Issuance' : 'identity-service-v2',
      external: seed.projectId !== 'proj-42',
      direction: 'UPSTREAM',
      relationship: 'API',
      ownerTeam: 'Identity Platform',
      health: seed.projectId === 'proj-88' ? 'RED' : seed.projectId === 'proj-42' ? 'YELLOW' : 'GREEN',
      resolutionState:
        seed.projectId === 'proj-88'
          ? 'AT_RISK'
          : seed.projectId === 'proj-42'
            ? 'NEGOTIATING'
            : 'APPROVED',
      blockerReason:
        seed.projectId === 'proj-42'
          ? 'Token exchange contract moved one sprint.'
          : seed.projectId === 'proj-88'
            ? 'Rollback dependency exceeds window SLA.'
            : null,
      contractCommitment: seed.projectId === 'proj-11' ? 'Reviewed in sprint 19 contract sync.' : null,
      rejectionReason: null,
      counterSignatureMemberId: seed.projectId === 'proj-11' ? 'u-007' : null,
      counterSignedAt: seed.projectId === 'proj-11' ? '2026-04-16T10:00:00Z' : null,
      daysBlocked: seed.projectId === 'proj-11' ? 0 : seed.projectId === 'proj-88' ? 8 : 4,
      planRevision: seed.planRevision,
    },
    {
      id: `${seed.projectId}-dep-2`,
      projectId: seed.projectId,
      targetRef: seed.projectId === 'proj-55' ? 'proj-42' : 'governance-board',
      targetProjectId: seed.projectId === 'proj-55' ? 'proj-42' : null,
      targetDescriptor: seed.projectId === 'proj-55' ? 'Gateway Migration' : 'Governance Board',
      external: seed.projectId !== 'proj-55',
      direction: 'DOWNSTREAM',
      relationship: 'SCHEDULE',
      ownerTeam: 'Governance Operations',
      health: seed.projectId === 'proj-55' ? 'YELLOW' : 'GREEN',
      resolutionState: seed.projectId === 'proj-55' ? 'NEGOTIATING' : 'PROPOSED',
      blockerReason: seed.projectId === 'proj-55' ? 'Dependency on fraud model sign-off.' : null,
      contractCommitment: null,
      rejectionReason: null,
      counterSignatureMemberId: null,
      counterSignedAt: null,
      daysBlocked: seed.projectId === 'proj-55' ? 3 : 1,
      planRevision: seed.planRevision,
    },
  ];

  const aiSuggestions: AiSuggestion[] = [
    {
      id: `${seed.projectId}-sug-1`,
      projectId: seed.projectId,
      kind: seed.projectId === 'proj-42' ? 'REBALANCE' : seed.projectId === 'proj-88' ? 'MITIGATION' : 'SLIPPAGE',
      targetType: seed.projectId === 'proj-42' ? 'CAPACITY_ALLOCATION' : seed.projectId === 'proj-88' ? 'RISK' : 'MILESTONE',
      targetId: seed.projectId === 'proj-42' ? `${seed.projectId}-cap-1` : seed.projectId === 'proj-88' ? `${seed.projectId}-risk-1` : `${seed.projectId}-m2`,
      payload:
        seed.projectId === 'proj-42'
          ? 'Shift 10% of Alan Turing allocation to Margaret Hamilton for the current execution lane.'
          : seed.projectId === 'proj-88'
            ? 'Escalate rollback instability with a dedicated incident escort and freeze nonessential scope.'
            : 'Advance a mitigation review before the release gate slips into the next sprint.',
      confidence: seed.projectId === 'proj-88' ? 0.93 : 0.81,
      state: 'PENDING',
      skillExecutionId: `skill-${seed.projectId}-1`,
      suppressionUntil: null,
      createdAt: seed.updatedAt,
      resolvedAt: null,
    },
  ];

  const header: PlanHeader = {
    projectId: seed.projectId,
    projectName: seed.projectName,
    workspaceId: seed.workspaceId,
    workspaceName: seed.workspaceName,
    applicationId: seed.applicationId,
    applicationName: seed.applicationName,
    lifecycleStage: seed.lifecycleStage,
    planHealth: seed.planHealth,
    planHealthFactors:
      seed.planHealth === 'GREEN'
        ? ['Execution cadence is stable.', 'No blocking approvals remain open.']
        : seed.planHealth === 'RED'
          ? ['Rollback confidence is below threshold.', 'Escalated risk remains unresolved.']
          : ['Dependency lane is at risk.', 'Approval latency is above target.'],
    nextMilestone: {
      id: `${seed.projectId}-m2`,
      label: milestones[1].label,
      targetDate: milestones[1].targetDate,
    },
    pmMemberId: 'u-007',
    pmDisplayName: memberName('u-007') ?? 'PM',
    pmBackupMemberId: 'u-012',
    autonomyLevel: seed.autonomyLevel,
    lastUpdatedAt: seed.updatedAt,
    planRevision: seed.planRevision,
  };

  const changeLogEntries = [
    makeLogEntry(seed.projectId, 'CREATE', 'MILESTONE', milestones[0].id, null, toJson({ status: milestones[0].status }), 'Seed milestone baseline'),
    makeLogEntry(seed.projectId, 'TRANSITION', 'MILESTONE', milestones[1].id, null, toJson({ status: milestones[1].status }), milestones[1].slippageReason),
    makeLogEntry(seed.projectId, 'UPDATE', 'CAPACITY_ALLOCATION', `${seed.projectId}-cap-1`, null, toJson({ percent: capacity.cells[0]?.percent }), capacity.cells[0]?.justification ?? null),
    makeLogEntry(seed.projectId, 'TRANSITION', 'RISK', risks[0].id, null, toJson({ state: risks[0].state }), risks[0].mitigationNote),
    makeLogEntry(seed.projectId, 'UPDATE', 'DEPENDENCY', dependencies[0].id, null, toJson({ state: dependencies[0].resolutionState }), dependencies[0].blockerReason),
  ];

  return clone({
    header,
    milestones,
    capacity,
    risks,
    dependencies,
    progress: buildProgress(seed.projectId, seed.planHealth, seed.projectId === 'proj-88' ? 2 : seed.projectId === 'proj-07' ? 5 : 4),
    changeLog: {
      entries: changeLogEntries,
      page: 0,
      total: changeLogEntries.length,
    },
    aiSuggestions,
  }) as PlanRuntime;
}

const initialPlans = Object.fromEntries(SEEDS.map(seed => [seed.projectId, buildPlan(seed)])) as Record<string, PlanRuntime>;

const runtime = {
  plans: clone(initialPlans),
};

function ensurePlan(projectId: string): PlanRuntime {
  const plan = runtime.plans[projectId];
  if (!plan) {
    throw new Error(`PM_NOT_FOUND: Project ${projectId} not found`);
  }
  return plan;
}

function ensureRevision(currentRevision: number, providedRevision: number) {
  if (currentRevision !== providedRevision) {
    throw new Error(`PM_STALE_REVISION: expected ${currentRevision}, received ${providedRevision}`);
  }
}

function updateHeaderDerived(plan: PlanRuntime) {
  const nextMilestone = plan.milestones
    .filter(milestone => !['COMPLETED', 'ARCHIVED'].includes(milestone.status))
    .sort((left, right) => left.ordering - right.ordering)[0] ?? null;

  const factors: string[] = [];
  const hasCriticalRisk = plan.risks.some(risk => risk.state !== 'RESOLVED' && risk.severity === 'CRITICAL');
  const hasSlippedMilestone = plan.milestones.some(milestone => milestone.status === 'SLIPPED');
  const hasAtRiskDependency = plan.dependencies.some(dependency => dependency.resolutionState === 'AT_RISK');
  const hasNegotiation = plan.dependencies.some(dependency => dependency.resolutionState === 'NEGOTIATING');

  let health: HealthIndicator = 'GREEN';

  if (hasCriticalRisk || hasSlippedMilestone) {
    health = 'RED';
    if (hasCriticalRisk) {
      factors.push('Critical risk still requires mitigation.');
    }
    if (hasSlippedMilestone) {
      factors.push('At least one milestone has formally slipped.');
    }
  } else if (hasAtRiskDependency || hasNegotiation || plan.milestones.some(milestone => milestone.status === 'AT_RISK')) {
    health = 'YELLOW';
    if (hasAtRiskDependency) {
      factors.push('One or more dependencies are at risk.');
    }
    if (hasNegotiation) {
      factors.push('Counter-sign approval is still open.');
    }
    if (plan.milestones.some(milestone => milestone.status === 'AT_RISK')) {
      factors.push('Milestone execution needs intervention.');
    }
  } else {
    factors.push('Execution cadence is stable.');
    factors.push('No blocking approvals remain open.');
  }

  const totals = computeTotals(plan.capacity.cells);
  plan.capacity = {
    ...plan.capacity,
    rowTotals: totals.rowTotals,
    columnTotals: totals.columnTotals,
    underThreshold: Object.values(totals.rowTotals).filter(total => total < 45).length,
    planRevision: plan.header.planRevision,
  };
  plan.header = {
    ...plan.header,
    planHealth: health,
    planHealthFactors: factors,
    nextMilestone: nextMilestone
      ? {
          id: nextMilestone.id,
          label: nextMilestone.label,
          targetDate: nextMilestone.targetDate,
        }
      : null,
  };
}

function stampRevision(plan: PlanRuntime) {
  plan.header = {
    ...plan.header,
    planRevision: plan.header.planRevision + 1,
    lastUpdatedAt: new Date().toISOString(),
  };
  plan.milestones = plan.milestones.map(milestone => ({
    ...milestone,
    planRevision: plan.header.planRevision,
  }));
  plan.capacity = {
    ...plan.capacity,
    cells: plan.capacity.cells.map(cell => ({
      ...cell,
      planRevision: plan.header.planRevision,
    })),
    planRevision: plan.header.planRevision,
  };
  plan.risks = plan.risks.map(risk => ({
    ...risk,
    planRevision: plan.header.planRevision,
  }));
  plan.dependencies = plan.dependencies.map(dependency => ({
    ...dependency,
    planRevision: plan.header.planRevision,
  }));
  updateHeaderDerived(plan);
}

function visibleSuggestions(plan: PlanRuntime): AiSuggestion[] {
  const now = Date.now();
  return plan.aiSuggestions.filter(suggestion => {
    if (suggestion.state !== 'PENDING') {
      return false;
    }
    if (!suggestion.suppressionUntil) {
      return true;
    }
    return new Date(suggestion.suppressionUntil).getTime() <= now;
  });
}

function aggregatePlan(plan: PlanRuntime): PlanAggregate {
  updateHeaderDerived(plan);
  return {
    header: section(clone(plan.header)),
    milestones: section(clone(plan.milestones)),
    capacity: section(clone(plan.capacity)),
    risks: section(clone(plan.risks)),
    dependencies: section(clone(plan.dependencies)),
    progress: section(clone(plan.progress)),
    changeLog: section({
      entries: clone(plan.changeLog.entries),
      page: 0,
      total: plan.changeLog.entries.length,
    }),
    aiSuggestions: section(clone(visibleSuggestions(plan))),
  };
}

function plansForWorkspace(workspaceId: string): PlanRuntime[] {
  return Object.values(runtime.plans).filter(plan => plan.header.workspaceId === workspaceId);
}

function buildPortfolioSummary(workspaceId: string): PortfolioSummary {
  const plans = plansForWorkspace(workspaceId);
  return {
    workspaceId,
    activeProjects: plans.length,
    redProjects: plans.filter(plan => plan.header.planHealth === 'RED').length,
    atRiskOrSlippedMilestones: plans.flatMap(plan => plan.milestones).filter(milestone => ['AT_RISK', 'SLIPPED'].includes(milestone.status)).length,
    criticalRisks: plans.flatMap(plan => plan.risks).filter(risk => risk.state !== 'RESOLVED' && risk.severity === 'CRITICAL').length,
    blockedDependencies: plans.flatMap(plan => plan.dependencies).filter(dependency => ['NEGOTIATING', 'AT_RISK'].includes(dependency.resolutionState)).length,
    pendingApprovals: plans.flatMap(plan => plan.dependencies).filter(dependency => dependency.resolutionState === 'NEGOTIATING').length,
    aiPendingReview: plans.flatMap(plan => visibleSuggestions(plan)).length,
    lastRefreshedAt: new Date().toISOString(),
  };
}

function buildPortfolioHeatmap(workspaceId: string): PortfolioHeatmap {
  const columns = ['Week 17', 'Week 18', 'Week 19', 'Week 20'];
  const rows: PortfolioHeatmapRow[] = plansForWorkspace(workspaceId).map(plan => {
    const cells: PortfolioHeatmapCell[] = columns.map((column, index) => {
      const milestone = plan.milestones[Math.min(index, plan.milestones.length - 1)];
      return {
        windowLabel: column,
        dominantStatus: STATUS_TO_HEALTH[milestone?.status ?? plan.header.planHealth] ?? plan.header.planHealth,
      };
    });
    return {
      projectId: plan.header.projectId,
      projectName: plan.header.projectName,
      cells,
    };
  });

  return {
    window: '4-week',
    columns,
    rows,
  };
}

function buildPortfolioCapacity(workspaceId: string): PortfolioCapacity {
  const plans = plansForWorkspace(workspaceId);
  const projects = plans.map(plan => ({
    projectId: plan.header.projectId,
    projectName: plan.header.projectName,
  }));
  const memberIds = Array.from(new Set(plans.flatMap(plan => plan.capacity.members.map(member => member.id))));

  const rows: PortfolioCapacityRow[] = memberIds.map(memberId => {
    const cells = plans.map(plan => ({
      projectId: plan.header.projectId,
      percent: plan.capacity.rowTotals[memberId] ?? 0,
    }));
    const totalPercent = cells.reduce((sum, cell) => sum + cell.percent, 0);
    return {
      memberId,
      displayName: memberName(memberId) ?? memberId,
      totalPercent,
      flag: totalPercent > 100 ? 'OVER' : totalPercent >= 75 ? 'FOCUS' : 'BALANCED',
      cells,
    };
  }).sort((left, right) => right.totalPercent - left.totalPercent);

  return {
    projects,
    rows,
    underThreshold: rows.filter(row => row.totalPercent < 50).length,
  };
}

function buildPortfolioRisks(workspaceId: string): PortfolioRiskConcentration {
  const risks = plansForWorkspace(workspaceId)
    .flatMap(plan => plan.risks)
    .sort((left, right) => {
      const severityDelta = severityRank(left.severity) - severityRank(right.severity);
      return severityDelta !== 0 ? severityDelta : right.ageDays - left.ageDays;
    });

  const heatmap = new Map<string, number>();
  risks.forEach(risk => {
    const key = `${risk.severity}:${risk.category}`;
    heatmap.set(key, (heatmap.get(key) ?? 0) + 1);
  });

  const severityCategoryHeatmap: SeverityCategoryCount[] = Array.from(heatmap.entries()).map(([key, count]) => {
    const [severity, category] = key.split(':');
    return {
      severity: severity as SeverityCategoryCount['severity'],
      category: category as SeverityCategoryCount['category'],
      count,
    };
  });

  return {
    topRisks: risks.slice(0, 6),
    severityCategoryHeatmap,
  };
}

function buildPortfolioBottlenecks(workspaceId: string): ReadonlyArray<DependencyBottleneck> {
  return plansForWorkspace(workspaceId)
    .flatMap(plan =>
      plan.dependencies
        .filter(dependency => ['NEGOTIATING', 'AT_RISK'].includes(dependency.resolutionState))
        .map(dependency => ({
          dependencyId: dependency.id,
          sourceProjectId: plan.header.projectId,
          sourceProjectName: plan.header.projectName,
          targetProjectId: dependency.targetProjectId,
          targetDescriptor: dependency.targetDescriptor,
          external: dependency.external,
          relationship: dependency.relationship,
          blockerReason: dependency.blockerReason,
          ownerTeam: dependency.ownerTeam,
          daysBlocked: dependency.daysBlocked,
          aiProposalId: visibleSuggestions(plan).find(suggestion => suggestion.targetId === dependency.id)?.id ?? null,
        })),
    )
    .sort((left, right) => right.daysBlocked - left.daysBlocked);
}

function buildCadence(workspaceId: string): ReadonlyArray<CadenceMetric> {
  const plans = plansForWorkspace(workspaceId);
  const avgThroughput =
    plans.flatMap(plan => plan.progress).reduce((sum, node) => sum + node.throughput, 0) /
    Math.max(1, plans.flatMap(plan => plan.progress).length);

  return [
    {
      key: 'throughput',
      window: '14d',
      value: Number(avgThroughput.toFixed(1)),
      deltaAbs: 1.4,
      trend: 'UP',
    },
    {
      key: 'cycle-time',
      window: '30d',
      value: plans.some(plan => plan.header.planHealth === 'RED') ? 10.6 : 8.4,
      deltaAbs: plans.some(plan => plan.header.planHealth === 'RED') ? 1.1 : -0.6,
      trend: plans.some(plan => plan.header.planHealth === 'RED') ? 'DOWN' : 'UP',
    },
    {
      key: 'wip',
      window: 'current',
      value: plans.reduce((sum, plan) => sum + plan.milestones.filter(milestone => milestone.status === 'IN_PROGRESS').length, 0),
      deltaAbs: -1,
      trend: 'FLAT',
    },
  ];
}

export async function getMockPortfolioAggregate(workspaceId: string): Promise<PortfolioAggregate> {
  return {
    summary: section(buildPortfolioSummary(workspaceId)),
    heatmap: section(buildPortfolioHeatmap(workspaceId)),
    capacity: section(buildPortfolioCapacity(workspaceId)),
    risks: section(buildPortfolioRisks(workspaceId)),
    bottlenecks: section(buildPortfolioBottlenecks(workspaceId)),
    cadence: section(buildCadence(workspaceId)),
  };
}

export async function getMockPortfolioSection<K extends keyof PortfolioAggregate>(cardKey: K, workspaceId: string): Promise<PortfolioAggregate[K]> {
  const aggregate = await getMockPortfolioAggregate(workspaceId);
  return aggregate[cardKey];
}

export async function getMockPlanAggregate(projectId: string): Promise<PlanAggregate> {
  return aggregatePlan(ensurePlan(projectId));
}

export async function getMockPlanSection<K extends keyof PlanAggregate>(cardKey: K, projectId: string): Promise<PlanAggregate[K]> {
  const aggregate = await getMockPlanAggregate(projectId);
  return aggregate[cardKey];
}

export async function transitionMockMilestone(projectId: string, milestoneId: string, request: TransitionMilestoneRequest): Promise<Milestone> {
  const plan = ensurePlan(projectId);
  ensureRevision(plan.header.planRevision, request.planRevision);
  const index = plan.milestones.findIndex(milestone => milestone.id === milestoneId);
  if (index < 0) {
    throw new Error(`PM_NOT_FOUND: Milestone ${milestoneId} not found`);
  }
  const before = clone(plan.milestones[index]) as Milestone;
  const after = {
    ...before,
    status: request.to,
    slippageReason: request.slippageReason ?? before.slippageReason,
    targetDate: request.newTargetDate ?? before.targetDate,
    percentComplete:
      request.to === 'COMPLETED'
        ? 100
        : request.to === 'NOT_STARTED'
          ? 0
          : before.percentComplete,
    completedAt: request.to === 'COMPLETED' ? new Date().toISOString() : before.completedAt,
  } as Mutable<Milestone>;
  plan.milestones[index] = after;
  stampRevision(plan);
  const stamped = {
    ...after,
    planRevision: plan.header.planRevision,
  } as Mutable<Milestone>;
  plan.milestones[index] = stamped;
  plan.changeLog.entries.unshift(
    makeLogEntry(projectId, 'TRANSITION', 'MILESTONE', milestoneId, toJson({ status: before.status }), toJson({ status: stamped.status }), request.slippageReason ?? null),
  );
  plan.changeLog.total = plan.changeLog.entries.length;
  return clone(stamped) as Milestone;
}

export async function saveMockCapacity(projectId: string, request: SaveCapacityBatchRequest): Promise<PlanCapacityMatrix> {
  const plan = ensurePlan(projectId);
  request.edits.forEach(edit => ensureRevision(plan.header.planRevision, edit.planRevision));

  const nextCells = [...plan.capacity.cells];
  request.edits.forEach(edit => {
    const index = nextCells.findIndex(cell => cell.memberId === edit.memberId && cell.milestoneId === edit.milestoneId);
    if (index >= 0) {
      nextCells[index] = {
        ...nextCells[index],
        percent: edit.percent,
        justification: edit.justification ?? null,
      };
    }
  });

  const totals = computeTotals(nextCells);
  Object.entries(totals.rowTotals).forEach(([memberId, total]) => {
    const editForMember = request.edits.find(edit => edit.memberId === memberId);
    const justification = request.edits.find(edit => edit.memberId === memberId && edit.justification)?.justification;
    if (total > 100 && !(justification ?? editForMember?.justification)) {
      throw new Error(`PM_OVERALLOCATION_JUSTIFICATION_REQUIRED: ${memberId} is allocated at ${total}%`);
    }
  });

  plan.capacity = {
    ...plan.capacity,
    cells: nextCells,
  };
  stampRevision(plan);
  plan.capacity = {
    ...plan.capacity,
    cells: plan.capacity.cells.map(cell => ({
      ...cell,
      planRevision: plan.header.planRevision,
    })),
    planRevision: plan.header.planRevision,
  };
  plan.changeLog.entries.unshift(
    makeLogEntry(projectId, 'UPDATE', 'CAPACITY_ALLOCATION', projectId, null, toJson({ edits: request.edits.length }), 'Capacity rebalance saved'),
  );
  plan.changeLog.total = plan.changeLog.entries.length;
  return clone(plan.capacity);
}

export async function transitionMockRisk(projectId: string, riskId: string, request: TransitionRiskRequest): Promise<RiskItem> {
  const plan = ensurePlan(projectId);
  ensureRevision(plan.header.planRevision, request.planRevision);
  const index = plan.risks.findIndex(risk => risk.id === riskId);
  if (index < 0) {
    throw new Error(`PM_NOT_FOUND: Risk ${riskId} not found`);
  }
  if (request.to === 'ESCALATED' && !request.linkedIncidentId) {
    throw new Error('PM_INCIDENT_LINK_REQUIRED: Escalated risks require a linked incident id');
  }
  const before = clone(plan.risks[index]) as RiskItem;
  const after = {
    ...before,
    state: request.to,
    mitigationNote: request.mitigationNote ?? before.mitigationNote,
    resolutionNote: request.resolutionNote ?? before.resolutionNote,
    linkedIncidentId: request.linkedIncidentId ?? before.linkedIncidentId,
    resolvedAt: request.to === 'RESOLVED' ? new Date().toISOString() : null,
  } as Mutable<RiskItem>;
  plan.risks[index] = after;
  stampRevision(plan);
  const stamped = {
    ...after,
    planRevision: plan.header.planRevision,
  } as Mutable<RiskItem>;
  plan.risks[index] = stamped;
  plan.changeLog.entries.unshift(
    makeLogEntry(projectId, request.to === 'ESCALATED' ? 'ESCALATE' : 'TRANSITION', 'RISK', riskId, toJson({ state: before.state }), toJson({ state: stamped.state }), request.mitigationNote ?? request.resolutionNote ?? null),
  );
  plan.changeLog.total = plan.changeLog.entries.length;
  return clone(stamped) as RiskItem;
}

export async function transitionMockDependency(projectId: string, dependencyId: string, request: TransitionDependencyRequest): Promise<Dependency> {
  const plan = ensurePlan(projectId);
  ensureRevision(plan.header.planRevision, request.planRevision);
  const index = plan.dependencies.findIndex(dependency => dependency.id === dependencyId);
  if (index < 0) {
    throw new Error(`PM_NOT_FOUND: Dependency ${dependencyId} not found`);
  }
  const before = clone(plan.dependencies[index]) as Dependency;
  const after = {
    ...before,
    resolutionState: request.to,
    rejectionReason: request.rejectionReason ?? before.rejectionReason,
    contractCommitment: request.contractCommitment ?? before.contractCommitment,
    health: STATUS_TO_HEALTH[request.to] ?? before.health,
  } as Mutable<Dependency>;
  plan.dependencies[index] = after;
  stampRevision(plan);
  const stamped = {
    ...after,
    planRevision: plan.header.planRevision,
  } as Mutable<Dependency>;
  plan.dependencies[index] = stamped;
  plan.changeLog.entries.unshift(
    makeLogEntry(projectId, 'TRANSITION', 'DEPENDENCY', dependencyId, toJson({ state: before.resolutionState }), toJson({ state: stamped.resolutionState }), request.rejectionReason ?? request.contractCommitment ?? null),
  );
  plan.changeLog.total = plan.changeLog.entries.length;
  return clone(stamped) as Dependency;
}

export async function counterSignMockDependency(projectId: string, dependencyId: string, request: CounterSignDependencyRequest): Promise<Dependency> {
  const plan = ensurePlan(projectId);
  ensureRevision(plan.header.planRevision, request.planRevision);
  const index = plan.dependencies.findIndex(dependency => dependency.id === dependencyId);
  if (index < 0) {
    throw new Error(`PM_NOT_FOUND: Dependency ${dependencyId} not found`);
  }
  const before = clone(plan.dependencies[index]) as Dependency;
  const after = {
    ...before,
    resolutionState: 'APPROVED',
    health: 'GREEN',
    counterSignatureMemberId: 'u-007',
    counterSignedAt: new Date().toISOString(),
  } as Mutable<Dependency>;
  plan.dependencies[index] = after;
  stampRevision(plan);
  const stamped = {
    ...after,
    planRevision: plan.header.planRevision,
  } as Mutable<Dependency>;
  plan.dependencies[index] = stamped;
  plan.changeLog.entries.unshift(
    makeLogEntry(projectId, 'COUNTERSIGN', 'DEPENDENCY', dependencyId, toJson({ state: before.resolutionState }), toJson({ state: stamped.resolutionState }), 'Counter-sign approved'),
  );
  plan.changeLog.total = plan.changeLog.entries.length;
  return clone(stamped) as Dependency;
}

function applyAcceptedSuggestion(plan: PlanRuntime, suggestion: AiSuggestion) {
  if (suggestion.targetType === 'MILESTONE') {
    const milestone = plan.milestones.find(item => item.id === suggestion.targetId);
    if (milestone) {
      milestone.status = 'AT_RISK';
      milestone.slippageReason = suggestion.payload;
    }
  }

  if (suggestion.targetType === 'CAPACITY_ALLOCATION') {
    const targetCell = plan.capacity.cells.find(cell => cell.id === suggestion.targetId);
    const peerCell = plan.capacity.cells.find(cell => cell.memberId === 'u-020' && cell.milestoneId === targetCell?.milestoneId);
    if (targetCell && peerCell) {
      targetCell.percent = Math.max(0, targetCell.percent - 10);
      peerCell.percent = peerCell.percent + 10;
    }
  }

  if (suggestion.targetType === 'RISK') {
    const risk = plan.risks.find(item => item.id === suggestion.targetId);
    if (risk) {
      risk.state = 'MITIGATING';
      risk.mitigationNote = suggestion.payload;
    }
  }

  if (suggestion.targetType === 'DEPENDENCY') {
    const dependency = plan.dependencies.find(item => item.id === suggestion.targetId);
    if (dependency) {
      dependency.resolutionState = 'NEGOTIATING';
      dependency.blockerReason = suggestion.payload;
      dependency.health = 'YELLOW';
    }
  }
}

export async function acceptMockSuggestion(projectId: string, suggestionId: string): Promise<AiSuggestionActionResult> {
  const plan = ensurePlan(projectId);
  const index = plan.aiSuggestions.findIndex(suggestion => suggestion.id === suggestionId);
  if (index < 0) {
    throw new Error(`PM_NOT_FOUND: AI suggestion ${suggestionId} not found`);
  }
  const before = plan.aiSuggestions[index];
  applyAcceptedSuggestion(plan, before);
  const after: AiSuggestion = {
    ...before,
    state: 'ACCEPTED',
    resolvedAt: new Date().toISOString(),
  };
  plan.aiSuggestions.splice(index, 1, after);
  stampRevision(plan);
  plan.changeLog.entries.unshift(
    makeLogEntry(projectId, 'ACCEPT_AI_SUGGESTION', 'AI_SUGGESTION', suggestionId, toJson({ state: before.state }), toJson({ state: after.state }), before.payload, 'AI', null),
  );
  plan.changeLog.total = plan.changeLog.entries.length;
  return {
    suggestion: clone(after),
    auditLinkId: `audit-pm-ai-${suggestionId}`,
  };
}

export async function dismissMockSuggestion(projectId: string, suggestionId: string, request?: DismissAiSuggestionRequest): Promise<AiSuggestionActionResult> {
  const plan = ensurePlan(projectId);
  const index = plan.aiSuggestions.findIndex(suggestion => suggestion.id === suggestionId);
  if (index < 0) {
    throw new Error(`PM_NOT_FOUND: AI suggestion ${suggestionId} not found`);
  }
  const before = plan.aiSuggestions[index];
  const after: AiSuggestion = {
    ...before,
    state: 'DISMISSED',
    suppressionUntil: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
    resolvedAt: new Date().toISOString(),
  };
  plan.aiSuggestions.splice(index, 1, after);
  stampRevision(plan);
  plan.changeLog.entries.unshift(
    makeLogEntry(projectId, 'DISMISS_AI_SUGGESTION', 'AI_SUGGESTION', suggestionId, toJson({ state: before.state }), toJson({ state: after.state }), request?.reason ?? null, 'HUMAN', 'u-007'),
  );
  plan.changeLog.total = plan.changeLog.entries.length;
  return {
    suggestion: clone(after),
    auditLinkId: `audit-pm-ai-${suggestionId}`,
  };
}
