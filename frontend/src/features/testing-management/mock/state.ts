import type { SectionResult } from '@/shared/types/section';
import type {
  CaseDetailAggregate,
  CaseRevision,
  CaseRunOutcome,
  CatalogAggregate,
  CatalogFilter,
  CatalogFilters,
  CatalogPlanRow,
  CatalogSummary,
  CoverageStatus,
  PlanCaseRef,
  PlanCaseRow,
  PlanCoverageRow,
  PlanDetailAggregate,
  PlanHeader,
  PlanRunRow,
  RequirementChip,
  RunCaseResultRow,
  RunCoverageAggregate,
  RunDetailAggregate,
  RunHeader,
  TraceabilityAggregate,
  TraceabilityReqRow,
  TraceabilitySummary,
  AiDraftRow,
  AiInsights,
  CaseDetail,
  MemberRef,
} from '../types';

const NOW = new Date('2026-04-18T12:00:00Z').getTime();
const WORKSPACE_ID = 'ws-default-001';
const WORKSPACE_NAME = 'Global SDLC Tower';

function section<T>(data: T, error: string | null = null): SectionResult<T> {
  return { data, error };
}

function iso(value: string) {
  return new Date(value).toISOString();
}

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}

function member(memberId: string, displayName: string): MemberRef {
  return { memberId, displayName };
}

function reqChip(
  reqId: string,
  title: string,
  chipColor: CoverageStatus,
  options: Partial<RequirementChip> = {},
): RequirementChip {
  return {
    reqId,
    storyId: options.storyId ?? reqId.replace('REQ', 'ST'),
    title,
    projectId: options.projectId ?? 'proj-42',
    linkStatus: options.linkStatus ?? 'VERIFIED',
    chipColor,
    routePath: options.routePath ?? `/requirements/${reqId}`,
  };
}

function recentRun(
  runId: string,
  environmentId: string,
  environmentName: string,
  triggerSource: PlanRunRow['triggerSource'],
  state: PlanRunRow['state'],
  durationSec: number | null,
  passCount: number,
  failCount: number,
  skipCount: number,
  actor: MemberRef,
  startedAt: string,
  completedAt: string | null,
): PlanRunRow {
  return {
    runId,
    environmentId,
    environmentName,
    triggerSource,
    state,
    durationSec,
    passCount,
    failCount,
    skipCount,
    actor,
    startedAt,
    completedAt,
  };
}

const mina = member('u-020', 'Mina Kwan');
const arun = member('u-021', 'Arun Patel');
const zoe = member('u-022', 'Zoe Hart');
const ian = member('u-023', 'Ian Flores');

export const catalogPlanRows: ReadonlyArray<CatalogPlanRow> = [
  {
    planId: 'plan-auth-001',
    projectId: 'proj-42',
    projectName: 'Identity Gateway',
    workspaceId: WORKSPACE_ID,
    name: 'Gateway Authentication Regression',
    releaseTarget: '2026.04',
    owner: mina,
    state: 'ACTIVE',
    linkedCaseCount: 3,
    coverageLed: 'GREEN',
    description: 'Regression pack for auth, session continuity, and audit trail coverage.',
    createdAt: iso('2026-04-15T08:00:00Z'),
    updatedAt: iso('2026-04-18T03:15:00Z'),
  },
  {
    planId: 'plan-rbac-001',
    projectId: 'proj-11',
    projectName: 'Access Control Console',
    workspaceId: WORKSPACE_ID,
    name: 'Role Matrix Verification',
    releaseTarget: '2026.05',
    owner: mina,
    state: 'ACTIVE',
    linkedCaseCount: 2,
    coverageLed: 'AMBER',
    description: 'Access-role validation pack for admin, reviewer, and viewer journeys.',
    createdAt: iso('2026-04-12T08:00:00Z'),
    updatedAt: iso('2026-04-17T16:00:00Z'),
  },
  {
    planId: 'plan-perf-001',
    projectId: 'proj-55',
    projectName: 'Checkout Reliability',
    workspaceId: WORKSPACE_ID,
    name: 'Latency Guardrail Suite',
    releaseTarget: '2026.05',
    owner: arun,
    state: 'ACTIVE',
    linkedCaseCount: 1,
    coverageLed: 'RED',
    description: 'Performance and soak coverage for the API reliability backlog.',
    createdAt: iso('2026-04-11T09:00:00Z'),
    updatedAt: iso('2026-04-18T01:10:00Z'),
  },
  {
    planId: 'plan-legacy-001',
    projectId: 'proj-88',
    projectName: 'Legacy Exports',
    workspaceId: WORKSPACE_ID,
    name: 'Legacy Exit Smoke Pack',
    releaseTarget: '2026.03',
    owner: arun,
    state: 'ARCHIVED',
    linkedCaseCount: 1,
    coverageLed: 'GREY',
    description: 'Archived smoke coverage retained for audit reference only.',
    createdAt: iso('2026-03-20T10:00:00Z'),
    updatedAt: iso('2026-04-10T09:00:00Z'),
  },
  {
    planId: 'plan-tenant-002',
    projectId: 'proj-77',
    projectName: 'Tenant Workspace',
    workspaceId: WORKSPACE_ID,
    name: 'Tenant Isolation Contract Suite',
    releaseTarget: '2026.05',
    owner: zoe,
    state: 'ACTIVE',
    linkedCaseCount: 2,
    coverageLed: 'GREEN',
    description: 'Cross-tenant access probes and data-boundary validation for shared control APIs.',
    createdAt: iso('2026-04-14T02:00:00Z'),
    updatedAt: iso('2026-04-18T06:30:00Z'),
  },
  {
    planId: 'plan-mobile-001',
    projectId: 'proj-64',
    projectName: 'Mobile Surface',
    workspaceId: WORKSPACE_ID,
    name: 'Session Recovery Mobile Sweep',
    releaseTarget: '2026.05',
    owner: ian,
    state: 'ACTIVE',
    linkedCaseCount: 2,
    coverageLed: 'AMBER',
    description: 'Mobile-specific recovery, token refresh, and offline recovery journeys.',
    createdAt: iso('2026-04-16T01:00:00Z'),
    updatedAt: iso('2026-04-18T05:40:00Z'),
  },
  {
    planId: 'plan-search-001',
    projectId: 'proj-63',
    projectName: 'Search Platform',
    workspaceId: WORKSPACE_ID,
    name: 'Search Resiliency Backstop',
    releaseTarget: '2026.06',
    owner: zoe,
    state: 'DRAFT',
    linkedCaseCount: 1,
    coverageLed: 'RED',
    description: 'Draft suite to harden timeout, fallback, and cache invalidation behavior.',
    createdAt: iso('2026-04-17T04:20:00Z'),
    updatedAt: iso('2026-04-18T02:05:00Z'),
  },
  {
    planId: 'plan-observability-001',
    projectId: 'proj-31',
    projectName: 'Observability Core',
    workspaceId: WORKSPACE_ID,
    name: 'Probe Freshness Smoke',
    releaseTarget: '2026.05',
    owner: ian,
    state: 'DRAFT',
    linkedCaseCount: 1,
    coverageLed: 'GREY',
    description: 'No-runs-yet smoke plan for telemetry freshness and chart hydration.',
    createdAt: iso('2026-04-18T00:20:00Z'),
    updatedAt: iso('2026-04-18T00:30:00Z'),
  },
] as const;

const authCases: ReadonlyArray<PlanCaseRow> = [
  {
    caseId: 'case-auth-4201',
    planId: 'plan-auth-001',
    title: 'Card payment happy path',
    type: 'FUNCTIONAL',
    priority: 'P0',
    state: 'ACTIVE',
    linkedReqs: [
      reqChip('REQ-0001', 'Session continuity across payment auth', 'GREEN', { projectId: 'proj-42' }),
      reqChip('REQ-0005', 'Audit trail completeness', 'GREEN', { projectId: 'proj-42' }),
    ],
    lastRunStatus: 'PASS',
    lastRunAt: iso('2026-04-18T06:18:00Z'),
  },
  {
    caseId: 'case-auth-4202',
    planId: 'plan-auth-001',
    title: '3DS timeout fallback',
    type: 'REGRESSION',
    priority: 'P1',
    state: 'ACTIVE',
    linkedReqs: [
      reqChip('REQ-0001', 'Session continuity across payment auth', 'GREEN', { projectId: 'proj-42' }),
    ],
    lastRunStatus: 'FAIL',
    lastRunAt: iso('2026-04-17T09:48:40Z'),
  },
  {
    caseId: 'case-ai-4203',
    planId: 'plan-auth-001',
    title: 'AI draft for audit timeline completeness',
    type: 'FUNCTIONAL',
    priority: 'P2',
    state: 'DRAFT',
    linkedReqs: [
      reqChip('REQ-0010', 'Evidence chain coverage for audit timeline', 'GREY', {
        projectId: 'proj-42',
        linkStatus: 'UNVERIFIED',
      }),
    ],
    lastRunStatus: null,
    lastRunAt: null,
  },
];

const rbacCases: ReadonlyArray<PlanCaseRow> = [
  {
    caseId: 'case-rbac-1101',
    planId: 'plan-rbac-001',
    title: 'Role matrix edit permissions',
    type: 'FUNCTIONAL',
    priority: 'P1',
    state: 'ACTIVE',
    linkedReqs: [
      reqChip('REQ-0002', 'Role-based edit authorization', 'AMBER', { projectId: 'proj-11' }),
    ],
    lastRunStatus: 'PASS',
    lastRunAt: iso('2026-04-18T02:34:08Z'),
  },
  {
    caseId: 'case-color-1102',
    planId: 'plan-rbac-001',
    title: 'Requirement chip color matrix',
    type: 'SMOKE',
    priority: 'P2',
    state: 'ACTIVE',
    linkedReqs: [
      reqChip('REQ-0006', 'Profile management access checks', 'GREEN', { projectId: 'proj-11' }),
      reqChip('REQ-0002', 'Role-based edit authorization', 'AMBER', { projectId: 'proj-11' }),
      reqChip('REQ-4040', 'Unknown linked requirement', 'RED', { projectId: null, linkStatus: 'UNKNOWN_REQ' }),
      reqChip('REQ-0010', 'Evidence chain coverage for audit timeline', 'GREY', {
        projectId: 'proj-11',
        linkStatus: 'UNVERIFIED',
      }),
    ],
    lastRunStatus: 'PASS',
    lastRunAt: iso('2026-04-18T02:35:42Z'),
  },
];

const perfCases: ReadonlyArray<PlanCaseRow> = [
  {
    caseId: 'case-perf-5501',
    planId: 'plan-perf-001',
    title: 'API latency under load',
    type: 'PERF',
    priority: 'P0',
    state: 'ACTIVE',
    linkedReqs: [
      reqChip('REQ-0003', 'p95 latency below 200ms', 'RED', { projectId: 'proj-55' }),
    ],
    lastRunStatus: 'FAIL',
    lastRunAt: iso('2026-04-18T00:55:30Z'),
  },
];

const legacyCases: ReadonlyArray<PlanCaseRow> = [
  {
    caseId: 'case-legacy-8801',
    planId: 'plan-legacy-001',
    title: 'Legacy export smoke',
    type: 'SMOKE',
    priority: 'P3',
    state: 'DEPRECATED',
    linkedReqs: [
      reqChip('REQ-0009', 'Legacy export audit support', 'GREY', { projectId: 'proj-88' }),
    ],
    lastRunStatus: null,
    lastRunAt: null,
  },
];

const tenantCases: ReadonlyArray<PlanCaseRow> = [
  {
    caseId: 'case-tenant-7701',
    planId: 'plan-tenant-002',
    title: 'Workspace token cannot cross tenant boundary',
    type: 'SECURITY',
    priority: 'P0',
    state: 'ACTIVE',
    linkedReqs: [
      reqChip('REQ-0012', 'Tenant boundary separation', 'GREEN', { projectId: 'proj-77' }),
    ],
    lastRunStatus: 'PASS',
    lastRunAt: iso('2026-04-18T06:26:00Z'),
  },
  {
    caseId: 'case-tenant-7702',
    planId: 'plan-tenant-002',
    title: 'Cross-tenant search index remains isolated',
    type: 'REGRESSION',
    priority: 'P1',
    state: 'ACTIVE',
    linkedReqs: [
      reqChip('REQ-0013', 'Workspace search isolation', 'GREEN', { projectId: 'proj-77' }),
    ],
    lastRunStatus: 'PASS',
    lastRunAt: iso('2026-04-18T06:27:00Z'),
  },
];

const mobileCases: ReadonlyArray<PlanCaseRow> = [
  {
    caseId: 'case-mobile-6401',
    planId: 'plan-mobile-001',
    title: 'Refresh token recovery after offline wake',
    type: 'REGRESSION',
    priority: 'P1',
    state: 'ACTIVE',
    linkedReqs: [
      reqChip('REQ-0014', 'Mobile recovery tokens', 'AMBER', { projectId: 'proj-64' }),
    ],
    lastRunStatus: 'FAIL',
    lastRunAt: iso('2026-04-18T05:15:00Z'),
  },
  {
    caseId: 'case-mobile-6402',
    planId: 'plan-mobile-001',
    title: 'Offline banner clears after connectivity returns',
    type: 'SMOKE',
    priority: 'P2',
    state: 'ACTIVE',
    linkedReqs: [
      reqChip('REQ-0015', 'Offline recovery banner behavior', 'GREEN', { projectId: 'proj-64' }),
    ],
    lastRunStatus: 'PASS',
    lastRunAt: iso('2026-04-18T05:12:00Z'),
  },
];

const searchCases: ReadonlyArray<PlanCaseRow> = [
  {
    caseId: 'case-search-6301',
    planId: 'plan-search-001',
    title: 'Result fallback engages when ranking service times out',
    type: 'FUNCTIONAL',
    priority: 'P1',
    state: 'DRAFT',
    linkedReqs: [
      reqChip('REQ-0016', 'Search ranking drift alerting', 'RED', { projectId: 'proj-63' }),
    ],
    lastRunStatus: null,
    lastRunAt: null,
  },
];

const observabilityCases: ReadonlyArray<PlanCaseRow> = [
  {
    caseId: 'case-observe-3101',
    planId: 'plan-observability-001',
    title: 'Probe freshness cards hydrate under degraded telemetry',
    type: 'SMOKE',
    priority: 'P2',
    state: 'DRAFT',
    linkedReqs: [
      reqChip('REQ-0018', 'Observability probe freshness', 'GREY', { projectId: 'proj-31' }),
    ],
    lastRunStatus: null,
    lastRunAt: null,
  },
];

const planCasesById: Record<string, ReadonlyArray<PlanCaseRow>> = {
  'plan-auth-001': authCases,
  'plan-rbac-001': rbacCases,
  'plan-perf-001': perfCases,
  'plan-legacy-001': legacyCases,
  'plan-tenant-002': tenantCases,
  'plan-mobile-001': mobileCases,
  'plan-search-001': searchCases,
  'plan-observability-001': observabilityCases,
};

const planCoverageById: Record<string, ReadonlyArray<PlanCoverageRow>> = {
  'plan-auth-001': [
    { reqId: 'REQ-0001', reqTitle: 'Session continuity across payment auth', linkedCaseCount: 2, aggregateStatus: 'GREEN', mostRecentAt: iso('2026-04-18T06:19:00Z') },
    { reqId: 'REQ-0005', reqTitle: 'Audit trail completeness', linkedCaseCount: 1, aggregateStatus: 'GREEN', mostRecentAt: iso('2026-04-18T06:18:00Z') },
    { reqId: 'REQ-0010', reqTitle: 'Evidence chain coverage for audit timeline', linkedCaseCount: 1, aggregateStatus: 'GREY', mostRecentAt: null },
  ],
  'plan-rbac-001': [
    { reqId: 'REQ-0002', reqTitle: 'Role-based edit authorization', linkedCaseCount: 2, aggregateStatus: 'AMBER', mostRecentAt: iso('2026-04-18T02:35:42Z') },
    { reqId: 'REQ-0006', reqTitle: 'Profile management access checks', linkedCaseCount: 1, aggregateStatus: 'GREEN', mostRecentAt: iso('2026-04-18T02:35:42Z') },
    { reqId: 'REQ-4040', reqTitle: 'Unknown linked requirement', linkedCaseCount: 1, aggregateStatus: 'RED', mostRecentAt: iso('2026-04-18T02:35:42Z') },
    { reqId: 'REQ-0010', reqTitle: 'Evidence chain coverage for audit timeline', linkedCaseCount: 1, aggregateStatus: 'GREY', mostRecentAt: null },
  ],
  'plan-perf-001': [
    { reqId: 'REQ-0003', reqTitle: 'p95 latency below 200ms', linkedCaseCount: 1, aggregateStatus: 'RED', mostRecentAt: iso('2026-04-18T00:55:30Z') },
  ],
  'plan-legacy-001': [
    { reqId: 'REQ-0009', reqTitle: 'Legacy export audit support', linkedCaseCount: 1, aggregateStatus: 'GREY', mostRecentAt: null },
  ],
  'plan-tenant-002': [
    { reqId: 'REQ-0012', reqTitle: 'Tenant boundary separation', linkedCaseCount: 1, aggregateStatus: 'GREEN', mostRecentAt: iso('2026-04-18T06:26:00Z') },
    { reqId: 'REQ-0013', reqTitle: 'Workspace search isolation', linkedCaseCount: 1, aggregateStatus: 'GREEN', mostRecentAt: iso('2026-04-18T06:27:00Z') },
  ],
  'plan-mobile-001': [
    { reqId: 'REQ-0014', reqTitle: 'Mobile recovery tokens', linkedCaseCount: 1, aggregateStatus: 'AMBER', mostRecentAt: iso('2026-04-18T05:15:00Z') },
    { reqId: 'REQ-0015', reqTitle: 'Offline recovery banner behavior', linkedCaseCount: 1, aggregateStatus: 'GREEN', mostRecentAt: iso('2026-04-18T05:12:00Z') },
  ],
  'plan-search-001': [
    { reqId: 'REQ-0016', reqTitle: 'Search ranking drift alerting', linkedCaseCount: 1, aggregateStatus: 'RED', mostRecentAt: null },
  ],
  'plan-observability-001': [
    { reqId: 'REQ-0018', reqTitle: 'Observability probe freshness', linkedCaseCount: 1, aggregateStatus: 'GREY', mostRecentAt: null },
  ],
};

const planRunsById: Record<string, ReadonlyArray<PlanRunRow>> = {
  'plan-auth-001': [
    recentRun('run-auth-002', 'env-stage-001', 'Shared Staging', 'MANUAL_UPLOAD', 'PASSED', 588, 2, 0, 1, mina, iso('2026-04-18T06:10:00Z'), iso('2026-04-18T06:19:48Z')),
    recentRun('run-auth-001', 'env-stage-001', 'Shared Staging', 'CI_WEBHOOK', 'FAILED', 612, 1, 1, 0, mina, iso('2026-04-17T09:40:00Z'), iso('2026-04-17T09:50:12Z')),
  ],
  'plan-rbac-001': [
    recentRun('run-rbac-002', 'env-stage-001', 'Shared Staging', 'CI_WEBHOOK', 'RUNNING', null, 1, 0, 0, mina, iso('2026-04-18T02:30:00Z'), null),
    recentRun('run-rbac-001', 'env-stage-001', 'Shared Staging', 'MANUAL_UPLOAD', 'PASSED', 248, 2, 0, 0, mina, iso('2026-04-08T06:00:00Z'), iso('2026-04-08T06:04:08Z')),
  ],
  'plan-perf-001': [
    recentRun('run-perf-001', 'env-perf-001', 'Load Lab', 'CI_WEBHOOK', 'FAILED', 930, 0, 1, 0, arun, iso('2026-04-18T00:40:00Z'), iso('2026-04-18T00:55:30Z')),
  ],
  'plan-legacy-001': [],
  'plan-tenant-002': [],
  'plan-mobile-001': [],
  'plan-search-001': [],
  'plan-observability-001': [],
};

const aiDraftsByPlanId: Record<string, ReadonlyArray<AiDraftRow>> = {
  'plan-auth-001': [
    {
      caseId: 'case-ai-4203',
      title: 'AI draft for audit timeline completeness',
      sourceReqId: 'REQ-0010',
      skillVersion: 'tm-drafter@0.3.1',
      draftedAt: iso('2026-04-17T12:00:00Z'),
      draft: true,
    },
  ],
  'plan-rbac-001': [],
  'plan-perf-001': [],
  'plan-legacy-001': [],
  'plan-tenant-002': [],
  'plan-mobile-001': [],
  'plan-search-001': [
    {
      caseId: 'case-search-6301',
      title: 'Result fallback engages when ranking service times out',
      sourceReqId: 'REQ-0016',
      skillVersion: 'tm-drafter@0.3.1',
      draftedAt: iso('2026-04-18T01:20:00Z'),
      draft: true,
    },
  ],
  'plan-observability-001': [],
};

const aiInsightsByPlanId: Record<string, AiInsights> = {
  'plan-auth-001': {
    autonomyLevel: 'OBSERVATION',
    pendingDrafts: 1,
    activeCases: 2,
    passRateLast7d: 0.67,
    narrative: 'Auth regression is mostly healthy. One flaky 3DS branch still blocks approval of the AI draft.',
  },
  'plan-rbac-001': {
    autonomyLevel: 'SUPERVISED',
    pendingDrafts: 0,
    activeCases: 2,
    passRateLast7d: 0.91,
    narrative: 'RBAC coverage is broad, but one requirement is only partially verified across reviewer and admin personas.',
  },
  'plan-perf-001': {
    autonomyLevel: 'OBSERVATION',
    pendingDrafts: 0,
    activeCases: 1,
    passRateLast7d: 0.0,
    narrative: 'Perf suite is red because the latest soak crossed the latency guardrail for the full burst window.',
  },
  'plan-legacy-001': {
    autonomyLevel: 'DISABLED',
    pendingDrafts: 0,
    activeCases: 0,
    passRateLast7d: 0.0,
    narrative: 'Archived plan retained for audit lineage only. No new drafts are expected.',
  },
  'plan-tenant-002': {
    autonomyLevel: 'SUPERVISED',
    pendingDrafts: 0,
    activeCases: 2,
    passRateLast7d: 1.0,
    narrative: 'Tenant isolation suite is healthy and ready to anchor a release-readiness narrative.',
  },
  'plan-mobile-001': {
    autonomyLevel: 'OBSERVATION',
    pendingDrafts: 0,
    activeCases: 2,
    passRateLast7d: 0.5,
    narrative: 'Recovery flows are mixed. Offline rejoin remains healthy, while token refresh still regresses after cold wake.',
  },
  'plan-search-001': {
    autonomyLevel: 'OBSERVATION',
    pendingDrafts: 1,
    activeCases: 0,
    passRateLast7d: 0.0,
    narrative: 'Draft plan is waiting on requirements hardening before execution begins.',
  },
  'plan-observability-001': {
    autonomyLevel: 'OBSERVATION',
    pendingDrafts: 0,
    activeCases: 0,
    passRateLast7d: 0.0,
    narrative: 'No runs yet. This plan is queued behind dashboard telemetry cleanup.',
  },
};

function planHeader(row: CatalogPlanRow): PlanHeader {
  return {
    planId: row.planId,
    projectId: row.projectId,
    projectName: row.projectName,
    workspaceId: row.workspaceId,
    workspaceName: WORKSPACE_NAME,
    name: row.name,
    description: row.description,
    releaseTarget: row.releaseTarget,
    owner: row.owner,
    state: row.state,
    createdAt: row.createdAt,
    updatedAt: row.updatedAt,
  };
}

function planAggregate(row: CatalogPlanRow): PlanDetailAggregate {
  return {
    header: section(planHeader(row)),
    cases: section(planCasesById[row.planId] ?? []),
    coverage: section(planCoverageById[row.planId] ?? []),
    recentRuns: section(planRunsById[row.planId] ?? []),
    draftInbox: section(aiDraftsByPlanId[row.planId] ?? []),
    aiInsights: section(aiInsightsByPlanId[row.planId]),
  };
}

const planDetailsById: Record<string, PlanDetailAggregate> = Object.fromEntries(
  catalogPlanRows.map(row => [row.planId, planAggregate(row)]),
);

const runCaseResultsById: Record<string, ReadonlyArray<RunCaseResultRow>> = {
  'run-auth-002': [
    {
      resultId: 'result-auth-5201',
      caseId: 'case-auth-4201',
      title: 'Card payment happy path',
      outcome: 'PASS',
      durationSec: 205,
      failureExcerpt: null,
      lastPassedAt: iso('2026-04-18T06:18:00Z'),
      createdAt: iso('2026-04-18T06:18:00Z'),
    },
    {
      resultId: 'result-auth-5202',
      caseId: 'case-auth-4202',
      title: '3DS timeout fallback',
      outcome: 'PASS',
      durationSec: 221,
      failureExcerpt: null,
      lastPassedAt: iso('2026-04-18T06:19:20Z'),
      createdAt: iso('2026-04-18T06:19:20Z'),
    },
    {
      resultId: 'result-auth-5203',
      caseId: 'case-ai-4203',
      title: 'AI draft for audit timeline completeness',
      outcome: 'SKIP',
      durationSec: 12,
      failureExcerpt: 'Draft candidate is visible but excluded from release-gating runs until approval.',
      lastPassedAt: null,
      createdAt: iso('2026-04-18T06:19:30Z'),
    },
  ],
  'run-auth-001': [
    {
      resultId: 'result-auth-4201',
      caseId: 'case-auth-4201',
      title: 'Card payment happy path',
      outcome: 'PASS',
      durationSec: 210,
      failureExcerpt: null,
      lastPassedAt: iso('2026-04-17T09:43:30Z'),
      createdAt: iso('2026-04-17T09:43:30Z'),
    },
    {
      resultId: 'result-auth-4202',
      caseId: 'case-auth-4202',
      title: '3DS timeout fallback',
      outcome: 'FAIL',
      durationSec: 402,
      failureExcerpt: '3ds timeout while awaiting callback. bearer token already redacted. Payment remained recoverable, but the retry CTA never surfaced to the user. The webhook trace confirms callback receipt lag exceeded the guardrail and the fallback branch did not mark the session safe for resume.',
      lastPassedAt: iso('2026-04-15T09:12:00Z'),
      createdAt: iso('2026-04-17T09:48:40Z'),
    },
  ],
  'run-rbac-002': [
    {
      resultId: 'result-rbac-2101',
      caseId: 'case-rbac-1101',
      title: 'Role matrix edit permissions',
      outcome: 'PASS',
      durationSec: 118,
      failureExcerpt: null,
      lastPassedAt: iso('2026-04-18T02:33:18Z'),
      createdAt: iso('2026-04-18T02:33:18Z'),
    },
    {
      resultId: 'result-rbac-2102',
      caseId: 'case-color-1102',
      title: 'Requirement chip color matrix',
      outcome: 'SKIP',
      durationSec: 21,
      failureExcerpt: 'Execution still in progress while requirement re-resolution catches up.',
      lastPassedAt: iso('2026-04-08T06:04:08Z'),
      createdAt: iso('2026-04-18T02:35:42Z'),
    },
  ],
  'run-rbac-001': [
    {
      resultId: 'result-rbac-1101',
      caseId: 'case-rbac-1101',
      title: 'Role matrix edit permissions',
      outcome: 'PASS',
      durationSec: 248,
      failureExcerpt: null,
      lastPassedAt: iso('2026-04-08T06:04:08Z'),
      createdAt: iso('2026-04-08T06:04:08Z'),
    },
  ],
  'run-perf-001': [
    {
      resultId: 'result-perf-5501',
      caseId: 'case-perf-5501',
      title: 'API latency under load',
      outcome: 'FAIL',
      durationSec: 930,
      failureExcerpt: 'p95 latency breached threshold and sustained saturation for the full burst window. Heap stayed stable, but downstream cache contention kept the request queue pinned until the job stopped.',
      lastPassedAt: iso('2026-04-10T00:55:30Z'),
      createdAt: iso('2026-04-18T00:55:30Z'),
    },
  ],
};

function runHeaderFor(planId: string, runId: string): RunHeader | null {
  const row = catalogPlanRows.find(plan => plan.planId === planId);
  const run = (planRunsById[planId] ?? []).find(entry => entry.runId === runId);
  if (!row || !run) {
    return null;
  }
  return {
    runId,
    planId,
    projectId: row.projectId,
    planName: row.name,
    environmentId: run.environmentId,
    environmentName: run.environmentName,
    environmentKind: run.environmentId === 'env-perf-001' ? 'EPHEMERAL' : 'STAGING',
    triggerSource: run.triggerSource,
    actor: run.actor,
    state: run.state,
    externalRunId: run.triggerSource === 'CI_WEBHOOK' ? `gha-${runId.split('-').slice(-1)[0]}` : `manual-${runId.split('-').slice(-1)[0]}`,
    durationSec: run.durationSec,
    startedAt: run.startedAt,
    completedAt: run.completedAt,
  };
}

function runCoverage(runId: string): RunCoverageAggregate {
  if (runId.startsWith('run-auth')) {
    return {
      coveredRequirementCount: 3,
      coveredRequirements: [
        reqChip('REQ-0001', 'Session continuity across payment auth', 'GREEN', { projectId: 'proj-42' }),
        reqChip('REQ-0005', 'Audit trail completeness', 'GREEN', { projectId: 'proj-42' }),
        reqChip('REQ-0010', 'Evidence chain coverage for audit timeline', 'GREY', {
          projectId: 'proj-42',
          linkStatus: 'UNVERIFIED',
        }),
      ],
    };
  }
  if (runId.startsWith('run-rbac')) {
    return {
      coveredRequirementCount: 4,
      coveredRequirements: [
        reqChip('REQ-0002', 'Role-based edit authorization', 'AMBER', { projectId: 'proj-11' }),
        reqChip('REQ-0006', 'Profile management access checks', 'GREEN', { projectId: 'proj-11' }),
        reqChip('REQ-4040', 'Unknown linked requirement', 'RED', { projectId: null, linkStatus: 'UNKNOWN_REQ' }),
        reqChip('REQ-0010', 'Evidence chain coverage for audit timeline', 'GREY', {
          projectId: 'proj-11',
          linkStatus: 'UNVERIFIED',
        }),
      ],
    };
  }
  return {
    coveredRequirementCount: 1,
    coveredRequirements: [
      reqChip('REQ-0003', 'p95 latency below 200ms', 'RED', { projectId: 'proj-55' }),
    ],
  };
}

const runDetailsById: Record<string, RunDetailAggregate> = {
  'run-auth-002': {
    header: section(runHeaderFor('plan-auth-001', 'run-auth-002') as RunHeader),
    caseResults: section(runCaseResultsById['run-auth-002']),
    coverage: section(runCoverage('run-auth-002')),
  },
  'run-auth-001': {
    header: section(runHeaderFor('plan-auth-001', 'run-auth-001') as RunHeader),
    caseResults: section(runCaseResultsById['run-auth-001']),
    coverage: section(runCoverage('run-auth-001')),
  },
  'run-rbac-002': {
    header: section(runHeaderFor('plan-rbac-001', 'run-rbac-002') as RunHeader),
    caseResults: section(runCaseResultsById['run-rbac-002']),
    coverage: section(runCoverage('run-rbac-002')),
  },
  'run-rbac-001': {
    header: section(runHeaderFor('plan-rbac-001', 'run-rbac-001') as RunHeader),
    caseResults: section(runCaseResultsById['run-rbac-001']),
    coverage: section(runCoverage('run-rbac-001')),
  },
  'run-perf-001': {
    header: section(runHeaderFor('plan-perf-001', 'run-perf-001') as RunHeader),
    caseResults: section(runCaseResultsById['run-perf-001']),
    coverage: section(runCoverage('run-perf-001')),
  },
};

function caseOutcome(
  resultId: string,
  runId: string,
  outcome: CaseRunOutcome['outcome'],
  failureExcerpt: string | null,
  lastPassedAt: string | null,
  environmentName: string,
  createdAt: string,
): CaseRunOutcome {
  return {
    resultId,
    runId,
    outcome,
    failureExcerpt,
    lastPassedAt,
    environmentName,
    createdAt,
  };
}

function caseRevision(
  revisionId: string,
  actor: MemberRef,
  timestamp: string,
  fieldDiff: Record<string, string>,
): CaseRevision {
  return { revisionId, actor, timestamp, fieldDiff };
}

function caseDetail(
  detail: CaseDetail,
  recentResults: ReadonlyArray<CaseRunOutcome>,
  revisions: ReadonlyArray<CaseRevision> = [],
): CaseDetailAggregate {
  return {
    detail: section(detail),
    recentResults: section(recentResults),
    revisions: section(revisions),
  };
}

const caseDetailsById: Record<string, CaseDetailAggregate> = {
  'case-auth-4201': caseDetail(
    {
      caseId: 'case-auth-4201',
      planId: 'plan-auth-001',
      projectId: 'proj-42',
      planName: 'Gateway Authentication Regression',
      title: 'Card payment happy path',
      type: 'FUNCTIONAL',
      priority: 'P0',
      state: 'ACTIVE',
      origin: 'MANUAL',
      owner: mina,
      preconditions: '- User is authenticated\n- Checkout basket is valid\n- Payment service sandbox is reachable',
      steps: '1. Open checkout\n2. Submit card details\n3. Complete 3DS approval\n4. Confirm payment status event',
      expectedResult: 'Payment completes, the receipt is issued, and the audit trail records the full identity chain.',
      linkedReqs: authCases[0].linkedReqs,
      linkedIncidents: [],
      createdAt: iso('2026-04-15T08:15:00Z'),
      updatedAt: iso('2026-04-18T06:19:48Z'),
    },
    [
      caseOutcome('result-auth-5201', 'run-auth-002', 'PASS', null, iso('2026-04-18T06:18:00Z'), 'Shared Staging', iso('2026-04-18T06:18:00Z')),
      caseOutcome('result-auth-4201', 'run-auth-001', 'PASS', null, iso('2026-04-17T09:43:30Z'), 'Shared Staging', iso('2026-04-17T09:43:30Z')),
      caseOutcome('result-auth-3201', 'run-auth-baseline-001', 'PASS', null, iso('2026-04-15T09:11:00Z'), 'Shared Staging', iso('2026-04-15T09:11:00Z')),
    ],
    [
      caseRevision('rev-auth-01', mina, iso('2026-04-16T11:20:00Z'), {
        expectedResult: 'Added explicit audit trail validation after sign-off from platform governance.',
      }),
    ],
  ),
  'case-auth-4202': caseDetail(
    {
      caseId: 'case-auth-4202',
      planId: 'plan-auth-001',
      projectId: 'proj-42',
      planName: 'Gateway Authentication Regression',
      title: '3DS timeout fallback',
      type: 'REGRESSION',
      priority: 'P1',
      state: 'ACTIVE',
      origin: 'MANUAL',
      owner: mina,
      preconditions: '- Upstream ACS is slow\n- Retry CTA is feature-flagged on\n- Session resume telemetry is enabled',
      steps: '1. Start payment\n2. Simulate 3DS timeout\n3. Observe fallback handling\n4. Validate resume telemetry',
      expectedResult: 'Fallback flow records a recoverable error, emits resume telemetry, and exposes a retry CTA.',
      linkedReqs: authCases[1].linkedReqs,
      linkedIncidents: [
        {
          incidentId: 'INC-2401',
          title: '3DS timeout on callback resume',
          severity: 'P1',
          routePath: '/incidents/INC-2401',
        },
      ],
      createdAt: iso('2026-04-15T08:20:00Z'),
      updatedAt: iso('2026-04-17T09:48:40Z'),
    },
    [
      caseOutcome(
        'result-auth-4202',
        'run-auth-001',
        'FAIL',
        '3ds timeout while awaiting callback. bearer token already redacted. The fallback branch never rendered the retry CTA.',
        iso('2026-04-15T09:12:00Z'),
        'Shared Staging',
        iso('2026-04-17T09:48:40Z'),
      ),
      caseOutcome('result-auth-3202', 'run-auth-baseline-001', 'PASS', null, iso('2026-04-15T09:12:00Z'), 'Shared Staging', iso('2026-04-15T09:12:00Z')),
      caseOutcome('result-auth-5202', 'run-auth-002', 'PASS', null, iso('2026-04-18T06:19:20Z'), 'Shared Staging', iso('2026-04-18T06:19:20Z')),
    ],
    [
      caseRevision('rev-auth-02', mina, iso('2026-04-17T10:01:00Z'), {
        steps: 'Expanded fallback validation to require retry CTA visibility.',
      }),
    ],
  ),
  'case-ai-4203': caseDetail(
    {
      caseId: 'case-ai-4203',
      planId: 'plan-auth-001',
      projectId: 'proj-42',
      planName: 'Gateway Authentication Regression',
      title: 'AI draft for audit timeline completeness',
      type: 'FUNCTIONAL',
      priority: 'P2',
      state: 'DRAFT',
      origin: 'AI_DRAFT',
      owner: mina,
      preconditions: '- Audit trail events are enabled',
      steps: '1. Trigger auth flow\n2. Review emitted events\n3. Compare timeline continuity across retries',
      expectedResult: 'Draft candidate checks audit completeness and cross-step correlation IDs.',
      linkedReqs: authCases[2].linkedReqs,
      linkedIncidents: [],
      createdAt: iso('2026-04-17T12:00:00Z'),
      updatedAt: iso('2026-04-17T12:00:00Z'),
    },
    [],
  ),
  'case-rbac-1101': caseDetail(
    {
      caseId: 'case-rbac-1101',
      planId: 'plan-rbac-001',
      projectId: 'proj-11',
      planName: 'Role Matrix Verification',
      title: 'Role matrix edit permissions',
      type: 'FUNCTIONAL',
      priority: 'P1',
      state: 'ACTIVE',
      origin: 'MANUAL',
      owner: mina,
      preconditions: '- Admin account exists\n- Reviewer account exists',
      steps: '1. Open role matrix\n2. Edit reviewer permissions\n3. Save changes',
      expectedResult: 'Only allowed cells persist and the audit event is emitted.',
      linkedReqs: rbacCases[0].linkedReqs,
      linkedIncidents: [],
      createdAt: iso('2026-04-12T08:15:00Z'),
      updatedAt: iso('2026-04-18T02:34:08Z'),
    },
    [
      caseOutcome('result-rbac-2101', 'run-rbac-002', 'PASS', null, iso('2026-04-18T02:33:18Z'), 'Shared Staging', iso('2026-04-18T02:33:18Z')),
      caseOutcome('result-rbac-1101', 'run-rbac-001', 'PASS', null, iso('2026-04-08T06:04:08Z'), 'Shared Staging', iso('2026-04-08T06:04:08Z')),
    ],
  ),
  'case-color-1102': caseDetail(
    {
      caseId: 'case-color-1102',
      planId: 'plan-rbac-001',
      projectId: 'proj-11',
      planName: 'Role Matrix Verification',
      title: 'Requirement chip color matrix',
      type: 'SMOKE',
      priority: 'P2',
      state: 'ACTIVE',
      origin: 'MANUAL',
      owner: arun,
      preconditions: '- Linked requirement lookup is reachable',
      steps: '1. Open case detail\n2. Inspect linked REQ chips\n3. Verify tooltip content for each state',
      expectedResult: 'GREEN, AMBER, RED, and GREY chip states are all visible.',
      linkedReqs: rbacCases[1].linkedReqs,
      linkedIncidents: [],
      createdAt: iso('2026-04-12T08:25:00Z'),
      updatedAt: iso('2026-04-18T02:35:42Z'),
    },
    [
      caseOutcome('result-rbac-2102', 'run-rbac-002', 'SKIP', 'Execution still in progress while requirement re-resolution catches up.', iso('2026-04-08T06:04:08Z'), 'Shared Staging', iso('2026-04-18T02:35:42Z')),
      caseOutcome('result-rbac-1202', 'run-rbac-001', 'PASS', null, iso('2026-04-08T06:04:08Z'), 'Shared Staging', iso('2026-04-08T06:04:08Z')),
    ],
  ),
  'case-perf-5501': caseDetail(
    {
      caseId: 'case-perf-5501',
      planId: 'plan-perf-001',
      projectId: 'proj-55',
      planName: 'Latency Guardrail Suite',
      title: 'API latency under load',
      type: 'PERF',
      priority: 'P0',
      state: 'ACTIVE',
      origin: 'MANUAL',
      owner: arun,
      preconditions: '- Load profile "checkout-burst" is ready\n- Read replica telemetry is enabled',
      steps: '1. Run 15 minute burst\n2. Capture p95 latency\n3. Compare queue saturation',
      expectedResult: 'p95 stays below 200ms for the full burst window.',
      linkedReqs: perfCases[0].linkedReqs,
      linkedIncidents: [
        {
          incidentId: 'INC-2510',
          title: 'Burst saturation on checkout cache cluster',
          severity: 'P1',
          routePath: '/incidents/INC-2510',
        },
      ],
      createdAt: iso('2026-04-11T09:10:00Z'),
      updatedAt: iso('2026-04-18T00:55:30Z'),
    },
    [
      caseOutcome('result-perf-5501', 'run-perf-001', 'FAIL', 'p95 latency breached threshold and sustained saturation for the full burst window.', iso('2026-04-10T00:55:30Z'), 'Load Lab', iso('2026-04-18T00:55:30Z')),
      caseOutcome('result-perf-4501', 'run-perf-previous-001', 'PASS', null, iso('2026-04-10T00:55:30Z'), 'Load Lab', iso('2026-04-10T00:55:30Z')),
    ],
  ),
  'case-legacy-8801': caseDetail(
    {
      caseId: 'case-legacy-8801',
      planId: 'plan-legacy-001',
      projectId: 'proj-88',
      planName: 'Legacy Exit Smoke Pack',
      title: 'Legacy export smoke',
      type: 'SMOKE',
      priority: 'P3',
      state: 'DEPRECATED',
      origin: 'IMPORTED',
      owner: arun,
      preconditions: '- Legacy report job exists',
      steps: '1. Export legacy report\n2. Validate archive packaging',
      expectedResult: 'Historical export still completes for audit replay.',
      linkedReqs: legacyCases[0].linkedReqs,
      linkedIncidents: [],
      createdAt: iso('2026-03-20T10:10:00Z'),
      updatedAt: iso('2026-04-10T09:00:00Z'),
    },
    [],
  ),
  'case-tenant-7701': caseDetail(
    {
      caseId: 'case-tenant-7701',
      planId: 'plan-tenant-002',
      projectId: 'proj-77',
      planName: 'Tenant Isolation Contract Suite',
      title: 'Workspace token cannot cross tenant boundary',
      type: 'SECURITY',
      priority: 'P0',
      state: 'ACTIVE',
      origin: 'MANUAL',
      owner: zoe,
      preconditions: '- Two tenant workspaces exist\n- Boundary logs enabled',
      steps: '1. Mint workspace token in tenant A\n2. Replay against tenant B endpoints',
      expectedResult: 'Requests are denied and audit evidence shows the rejected tenant mismatch.',
      linkedReqs: tenantCases[0].linkedReqs,
      linkedIncidents: [],
      createdAt: iso('2026-04-14T02:20:00Z'),
      updatedAt: iso('2026-04-18T06:26:00Z'),
    },
    [],
  ),
  'case-mobile-6401': caseDetail(
    {
      caseId: 'case-mobile-6401',
      planId: 'plan-mobile-001',
      projectId: 'proj-64',
      planName: 'Session Recovery Mobile Sweep',
      title: 'Refresh token recovery after offline wake',
      type: 'REGRESSION',
      priority: 'P1',
      state: 'ACTIVE',
      origin: 'MANUAL',
      owner: ian,
      preconditions: '- Device was offline for 10 minutes',
      steps: '1. Wake the app\n2. Trigger token refresh\n3. Inspect recovery path',
      expectedResult: 'Refresh succeeds without forcing a fresh login.',
      linkedReqs: mobileCases[0].linkedReqs,
      linkedIncidents: [],
      createdAt: iso('2026-04-16T01:10:00Z'),
      updatedAt: iso('2026-04-18T05:15:00Z'),
    },
    [],
  ),
  'case-search-6301': caseDetail(
    {
      caseId: 'case-search-6301',
      planId: 'plan-search-001',
      projectId: 'proj-63',
      planName: 'Search Resiliency Backstop',
      title: 'Result fallback engages when ranking service times out',
      type: 'FUNCTIONAL',
      priority: 'P1',
      state: 'DRAFT',
      origin: 'AI_DRAFT',
      owner: zoe,
      preconditions: '- Ranking service timeout injector exists',
      steps: '1. Force ranking timeout\n2. Observe fallback selection',
      expectedResult: 'Fallback feed renders with audit evidence and no user-facing error.',
      linkedReqs: searchCases[0].linkedReqs,
      linkedIncidents: [],
      createdAt: iso('2026-04-17T04:35:00Z'),
      updatedAt: iso('2026-04-18T01:20:00Z'),
    },
    [],
  ),
  'case-observe-3101': caseDetail(
    {
      caseId: 'case-observe-3101',
      planId: 'plan-observability-001',
      projectId: 'proj-31',
      planName: 'Probe Freshness Smoke',
      title: 'Probe freshness cards hydrate under degraded telemetry',
      type: 'SMOKE',
      priority: 'P2',
      state: 'DRAFT',
      origin: 'MANUAL',
      owner: ian,
      preconditions: '- Telemetry replay fixture is seeded',
      steps: '1. Delay incoming telemetry\n2. Verify card placeholder state',
      expectedResult: 'Page hydrates and clearly marks probe data stale without crashing charts.',
      linkedReqs: observabilityCases[0].linkedReqs,
      linkedIncidents: [],
      createdAt: iso('2026-04-18T00:22:00Z'),
      updatedAt: iso('2026-04-18T00:30:00Z'),
    },
    [],
  ),
};

function planCaseRefFrom(caseId: string): PlanCaseRef {
  const aggregate = caseDetailsById[caseId];
  const detail = aggregate.detail.data as CaseDetail;
  const recentResults = aggregate.recentResults.data ?? [];
  const latest = recentResults[0] ?? null;
  return {
    caseId,
    title: detail.title,
    planId: detail.planId,
    planName: detail.planName,
    lastRunStatus: latest?.outcome ?? null,
    lastRunAt: latest?.createdAt ?? null,
  };
}

const traceabilityRows: ReadonlyArray<TraceabilityReqRow> = [
  {
    reqId: 'REQ-0001',
    reqTitle: 'Session continuity across payment auth',
    storyId: 'ST-0001',
    projectId: 'proj-42',
    projectName: 'Identity Gateway',
    linkedCaseCount: 2,
    linkedPlanCount: 1,
    coverageStatus: 'GREEN',
    latestRunAt: iso('2026-04-18T06:19:20Z'),
    cases: [planCaseRefFrom('case-auth-4201'), planCaseRefFrom('case-auth-4202')],
  },
  {
    reqId: 'REQ-0005',
    reqTitle: 'Audit trail completeness',
    storyId: 'ST-0005',
    projectId: 'proj-42',
    projectName: 'Identity Gateway',
    linkedCaseCount: 1,
    linkedPlanCount: 1,
    coverageStatus: 'GREEN',
    latestRunAt: iso('2026-04-18T06:18:00Z'),
    cases: [planCaseRefFrom('case-auth-4201')],
  },
  {
    reqId: 'REQ-0010',
    reqTitle: 'Evidence chain coverage for audit timeline',
    storyId: 'ST-0010',
    projectId: 'proj-42',
    projectName: 'Identity Gateway',
    linkedCaseCount: 2,
    linkedPlanCount: 2,
    coverageStatus: 'GREY',
    latestRunAt: null,
    cases: [planCaseRefFrom('case-ai-4203'), planCaseRefFrom('case-color-1102')],
  },
  {
    reqId: 'REQ-0002',
    reqTitle: 'Role-based edit authorization',
    storyId: 'ST-0002',
    projectId: 'proj-11',
    projectName: 'Access Control Console',
    linkedCaseCount: 2,
    linkedPlanCount: 1,
    coverageStatus: 'AMBER',
    latestRunAt: iso('2026-04-18T02:35:42Z'),
    cases: [planCaseRefFrom('case-rbac-1101'), planCaseRefFrom('case-color-1102')],
  },
  {
    reqId: 'REQ-0006',
    reqTitle: 'Profile management access checks',
    storyId: 'ST-0006',
    projectId: 'proj-11',
    projectName: 'Access Control Console',
    linkedCaseCount: 1,
    linkedPlanCount: 1,
    coverageStatus: 'GREEN',
    latestRunAt: iso('2026-04-18T02:35:42Z'),
    cases: [planCaseRefFrom('case-color-1102')],
  },
  {
    reqId: 'REQ-4040',
    reqTitle: 'Unknown linked requirement',
    storyId: null,
    projectId: 'proj-11',
    projectName: 'Access Control Console',
    linkedCaseCount: 1,
    linkedPlanCount: 1,
    coverageStatus: 'RED',
    latestRunAt: iso('2026-04-18T02:35:42Z'),
    cases: [planCaseRefFrom('case-color-1102')],
  },
  {
    reqId: 'REQ-0003',
    reqTitle: 'p95 latency below 200ms',
    storyId: 'ST-0003',
    projectId: 'proj-55',
    projectName: 'Checkout Reliability',
    linkedCaseCount: 1,
    linkedPlanCount: 1,
    coverageStatus: 'RED',
    latestRunAt: iso('2026-04-18T00:55:30Z'),
    cases: [planCaseRefFrom('case-perf-5501')],
  },
  {
    reqId: 'REQ-0009',
    reqTitle: 'Legacy export audit support',
    storyId: 'ST-0009',
    projectId: 'proj-88',
    projectName: 'Legacy Exports',
    linkedCaseCount: 1,
    linkedPlanCount: 1,
    coverageStatus: 'GREY',
    latestRunAt: null,
    cases: [planCaseRefFrom('case-legacy-8801')],
  },
  {
    reqId: 'REQ-0012',
    reqTitle: 'Tenant boundary separation',
    storyId: 'ST-0012',
    projectId: 'proj-77',
    projectName: 'Tenant Workspace',
    linkedCaseCount: 1,
    linkedPlanCount: 1,
    coverageStatus: 'GREEN',
    latestRunAt: iso('2026-04-18T06:26:00Z'),
    cases: [planCaseRefFrom('case-tenant-7701')],
  },
  {
    reqId: 'REQ-0014',
    reqTitle: 'Mobile recovery tokens',
    storyId: 'ST-0014',
    projectId: 'proj-64',
    projectName: 'Mobile Surface',
    linkedCaseCount: 1,
    linkedPlanCount: 1,
    coverageStatus: 'AMBER',
    latestRunAt: iso('2026-04-18T05:15:00Z'),
    cases: [planCaseRefFrom('case-mobile-6401')],
  },
  {
    reqId: 'REQ-0016',
    reqTitle: 'Search ranking drift alerting',
    storyId: 'ST-0016',
    projectId: 'proj-63',
    projectName: 'Search Platform',
    linkedCaseCount: 1,
    linkedPlanCount: 1,
    coverageStatus: 'RED',
    latestRunAt: null,
    cases: [planCaseRefFrom('case-search-6301')],
  },
  {
    reqId: 'REQ-0018',
    reqTitle: 'Observability probe freshness',
    storyId: 'ST-0018',
    projectId: 'proj-31',
    projectName: 'Observability Core',
    linkedCaseCount: 1,
    linkedPlanCount: 1,
    coverageStatus: 'GREY',
    latestRunAt: null,
    cases: [planCaseRefFrom('case-observe-3101')],
  },
] as const;

function buildBuckets(rows: ReadonlyArray<TraceabilityReqRow> | ReadonlyArray<CatalogPlanRow>) {
  return rows.reduce<Record<CoverageStatus, number>>(
    (accumulator, row) => {
      const status = 'coverageStatus' in row ? row.coverageStatus : row.coverageLed;
      accumulator[status] += 1;
      return accumulator;
    },
    {
      GREEN: 0,
      AMBER: 0,
      RED: 0,
      GREY: 0,
    },
  );
}

function recentRunsForSummary(rows: ReadonlyArray<CatalogPlanRow>) {
  const planIds = new Set(rows.map(row => row.planId));
  return Object.entries(planRunsById)
    .filter(([planId]) => planIds.has(planId))
    .flatMap(([, runs]) => runs)
    .filter(run => NOW - new Date(run.startedAt).getTime() <= 7 * 24 * 60 * 60 * 1000);
}

function countActiveCases(rows: ReadonlyArray<CatalogPlanRow>) {
  return rows.reduce((count, row) => {
    const planCases = planCasesById[row.planId] ?? [];
    return count + planCases.filter(entry => entry.state === 'ACTIVE').length;
  }, 0);
}

function buildCatalogSummary(rows: ReadonlyArray<CatalogPlanRow>): CatalogSummary {
  const recentRuns = recentRunsForSummary(rows);
  const finishedRuns = recentRuns.filter(run => run.state === 'PASSED' || run.state === 'FAILED');
  const passedRuns = finishedRuns.filter(run => run.state === 'PASSED');
  const meanRunDurationSec = finishedRuns.length
    ? finishedRuns.reduce((sum, run) => sum + (run.durationSec ?? 0), 0) / finishedRuns.length
    : 0;

  return {
    workspaceId: WORKSPACE_ID,
    totalPlans: rows.length,
    totalActiveCases: countActiveCases(rows),
    runsLast7d: recentRuns.length,
    passRateLast7d: finishedRuns.length ? passedRuns.length / finishedRuns.length : 0,
    meanRunDurationSec,
    byCoverageLed: buildBuckets(rows),
  };
}

export function buildCatalogFilters(): CatalogFilters {
  return {
    projectIds: [...new Set(catalogPlanRows.map(row => row.projectId))],
    planStates: ['DRAFT', 'ACTIVE', 'ARCHIVED'],
    coverageStatuses: ['GREEN', 'AMBER', 'RED', 'GREY'],
  };
}

function matchesCatalogFilter(row: CatalogPlanRow, filters: CatalogFilter) {
  if (filters.projectId !== 'ALL' && row.projectId !== filters.projectId) {
    return false;
  }
  if (filters.planState !== 'ALL' && row.state !== filters.planState) {
    return false;
  }
  if (filters.coverageStatus !== 'ALL' && row.coverageLed !== filters.coverageStatus) {
    return false;
  }
  if (filters.search.trim()) {
    const needle = filters.search.trim().toLowerCase();
    const haystack = [row.name, row.projectName, row.description, row.releaseTarget, row.owner.displayName]
      .join(' ')
      .toLowerCase();
    return haystack.includes(needle);
  }
  return true;
}

export function buildCatalogAggregate(filters: CatalogFilter): CatalogAggregate {
  const filteredRows = catalogPlanRows.filter(row => matchesCatalogFilter(row, filters));
  return {
    summary: section(buildCatalogSummary(filteredRows)),
    grid: section(filteredRows),
    filters: section(buildCatalogFilters()),
  };
}

export function buildPlanDetailAggregate(planId: string): PlanDetailAggregate | null {
  const aggregate = planDetailsById[planId];
  return aggregate ? clone(aggregate) : null;
}

export function buildCaseDetailAggregate(caseId: string): CaseDetailAggregate | null {
  const aggregate = caseDetailsById[caseId];
  return aggregate ? clone(aggregate) : null;
}

export function buildRunDetailAggregate(runId: string): RunDetailAggregate | null {
  const aggregate = runDetailsById[runId];
  return aggregate ? clone(aggregate) : null;
}

export function buildTraceabilityAggregate(): TraceabilityAggregate {
  const summary: TraceabilitySummary = {
    workspaceId: WORKSPACE_ID,
    totalRequirements: traceabilityRows.length,
    buckets: buildBuckets(traceabilityRows),
  };
  return {
    summary: section(summary),
    reqRows: section(traceabilityRows),
  };
}
