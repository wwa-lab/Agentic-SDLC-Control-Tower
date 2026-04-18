import { DEFAULT_RUN_QUERY } from '../constants';
import type {
  AggregateMetrics,
  ApiResponse,
  AutonomyLevel,
  EvidenceLink,
  MetricValue,
  MetricsSummary,
  Page,
  Policy,
  PolicyTrailEntry,
  Run,
  RunDetailResponse,
  RunQuery,
  RunStep,
  Skill,
  SkillDetailResponse,
  StageCoverage,
  TriggeredByType,
  TriggerSourceType,
} from '../types';
import { STAGE_LABELS, SDLC_STAGE_ORDER } from '../constants';

type MockScenario =
  | 'partial-metrics'
  | 'error-metrics'
  | 'error-stage-coverage'
  | 'error-skills'
  | 'error-runs'
  | 'empty-skills'
  | 'empty-runs'
  | 'empty-metrics'
  | '404-skill'
  | '404-run';

interface SkillSeed extends Omit<Skill, 'lastExecutedAt' | 'successRate30d'> {
  inputContract: string;
  outputContract: string;
  policy: Omit<Policy, 'skillKey'>;
}

interface RunSeed {
  id: string;
  skillKey: string;
  skillName: string;
  status: Run['status'];
  triggerSourceType: TriggerSourceType;
  triggerSourcePage: string | null;
  triggerSourceUrl: string | null;
  triggeredBy: string;
  triggeredByType: TriggeredByType;
  startedAt: string;
  endedAt: string | null;
  durationMs: number | null;
  outcomeSummary: string;
  auditRecordId: string | null;
  autonomyLevel: AutonomyLevel;
  timeSavedMinutes: number;
  inputSummary: Record<string, unknown>;
  outputSummary: Record<string, unknown>;
  stepBreakdown: RunStep[];
  policyTrail: PolicyTrailEntry[];
  evidenceLinks: EvidenceLink[];
}

interface WorkspaceFixture {
  skills: Skill[];
  skillDetails: Record<string, SkillDetailResponse>;
  runs: Run[];
  runDetails: Record<string, RunDetailResponse>;
  metrics: MetricsSummary;
  stageCoverage: StageCoverage;
}

function addHours(base: Date, hours: number): Date {
  return new Date(base.getTime() + hours * 60 * 60 * 1000);
}

function isoDaysAgo(daysAgo: number, hour = 9, minute = 0): string {
  const now = new Date();
  const date = new Date(Date.UTC(
    now.getUTCFullYear(),
    now.getUTCMonth(),
    now.getUTCDate() - daysAgo,
    hour,
    minute,
    0,
    0,
  ));
  return date.toISOString();
}

function makeRunSteps(startedAt: string, template: Array<{ name: string; status: Run['status']; minutes: number; note?: string }>): RunStep[] {
  let cursor = new Date(startedAt);
  return template.map((step, index) => {
    const endedAt = step.status === 'running' ? null : addHours(cursor, step.minutes / 60).toISOString();
    const next = endedAt ? new Date(endedAt) : cursor;
    const record: RunStep = {
      ordinal: index + 1,
      name: step.name,
      status: step.status,
      startedAt: cursor.toISOString(),
      endedAt,
      durationMs: endedAt ? step.minutes * 60 * 1000 : null,
      note: step.note,
    };
    cursor = next;
    return record;
  });
}

function buildRunSeed(
  id: string,
  skillKey: string,
  skillName: string,
  status: Run['status'],
  startedAt: string,
  options: Partial<Omit<RunSeed, 'id' | 'skillKey' | 'skillName' | 'status' | 'startedAt'>> = {},
): RunSeed {
  const defaultDuration = status === 'running' ? null : 18 * 60 * 1000;
  const endedAt = status === 'running' ? null : addHours(new Date(startedAt), (defaultDuration ?? 0) / 3600000).toISOString();
  return {
    id,
    skillKey,
    skillName,
    status,
    triggerSourceType: options.triggerSourceType ?? 'page',
    triggerSourcePage: options.triggerSourcePage ?? '/dashboard',
    triggerSourceUrl: options.triggerSourceUrl ?? options.triggerSourcePage ?? '/dashboard',
    triggeredBy: options.triggeredBy ?? 'platform-bot',
    triggeredByType: options.triggeredByType ?? 'ai',
    startedAt,
    endedAt,
    durationMs: options.durationMs ?? defaultDuration,
    outcomeSummary: options.outcomeSummary ?? `${skillName} completed with ${status.replace('_', ' ')} status.`,
    auditRecordId: options.auditRecordId ?? `aud_${id}`,
    autonomyLevel: options.autonomyLevel ?? 'L1-Assist',
    timeSavedMinutes: options.timeSavedMinutes ?? 18,
    inputSummary: options.inputSummary ?? { scope: 'workspace-default', requestId: id },
    outputSummary: options.outputSummary ?? { summary: `${skillName} generated an operational recommendation.` },
    stepBreakdown: options.stepBreakdown ?? makeRunSteps(startedAt, [
      { name: 'collect-context', status: 'succeeded', minutes: 4 },
      { name: 'analyze-signal', status: status === 'failed' ? 'failed' : 'succeeded', minutes: 8, note: status === 'failed' ? 'Correlation confidence dropped below threshold.' : undefined },
      { name: 'publish-outcome', status, minutes: status === 'running' ? 0 : 6, note: status === 'pending_approval' ? 'Awaiting human approval.' : undefined },
    ]),
    policyTrail: options.policyTrail ?? [
      { rule: 'workspace-autonomy-check', decision: 'allowed', at: startedAt },
      { rule: status === 'pending_approval' ? 'risky-action-gated' : 'default-policy', decision: status === 'pending_approval' ? 'held-for-approval' : 'allowed', at: endedAt ?? startedAt, note: status === 'pending_approval' ? 'Approval requested from SRE Lead.' : undefined },
    ],
    evidenceLinks: options.evidenceLinks ?? [],
  };
}

const SKILL_SEEDS: SkillSeed[] = [
  {
    id: 'sk_incident_diagnosis',
    key: 'incident-diagnosis',
    name: 'Incident Diagnosis',
    category: 'runtime',
    subCategory: 'Incident',
    status: 'active',
    defaultAutonomy: 'L2-Auto-with-approval',
    owner: 'platform-sre',
    description: 'Correlates signals, recent changes, and topology to form a root-cause hypothesis.',
    stages: ['incident', 'learning'],
    version: 3,
    inputContract: 'IncidentId, SignalSet, RecentChangeWindow',
    outputContract: 'RootCauseHypothesis, EvidenceSet, ConfidenceScore, RecommendedActions',
    policy: {
      autonomyLevel: 'L2-Auto-with-approval',
      approvalRequiredActions: ['restart-service', 'scale-up-resources', 'rollback-deployment'],
      authorizedApproverRoles: ['Workspace Admin', 'SRE Lead'],
      riskThresholds: { blastRadius: 'namespace', maxRevenueImpactUsd: 2000 },
      lastChangedAt: '2026-04-02T12:00:00Z',
      lastChangedBy: 'platform-admin@corp.example',
    },
  },
  {
    id: 'sk_req_to_story',
    key: 'req-to-user-story',
    name: 'Requirement to User Story',
    category: 'delivery',
    subCategory: 'Requirement',
    status: 'active',
    defaultAutonomy: 'L1-Assist',
    owner: 'platform-ai',
    description: 'Transforms raw requirement intake into ready-to-estimate user stories.',
    stages: ['requirement', 'user-story'],
    version: 2,
    inputContract: 'Requirement narrative, constraints, stakeholders',
    outputContract: 'UserStorySet, acceptanceCriteria, assumptions',
    policy: {
      autonomyLevel: 'L1-Assist',
      approvalRequiredActions: ['create-jira-story'],
      authorizedApproverRoles: ['Product Owner', 'Analyst Lead'],
      riskThresholds: { maxStoriesPerBatch: 12 },
      lastChangedAt: '2026-03-26T09:30:00Z',
      lastChangedBy: 'pm-platform@corp.example',
    },
  },
  {
    id: 'sk_story_to_spec',
    key: 'user-story-to-spec',
    name: 'User Story to Spec',
    category: 'delivery',
    subCategory: 'Specification',
    status: 'active',
    defaultAutonomy: 'L1-Assist',
    owner: 'platform-ai',
    description: 'Produces spec drafts, assumptions, and API surface notes from approved stories.',
    stages: ['user-story', 'spec'],
    version: 4,
    inputContract: 'User stories, glossary, non-functional constraints',
    outputContract: 'SpecDraft, contractCandidates, unresolvedQuestions',
    policy: {
      autonomyLevel: 'L1-Assist',
      approvalRequiredActions: ['publish-spec'],
      authorizedApproverRoles: ['Tech Lead', 'Architect'],
      riskThresholds: { minTraceabilityCoverage: 0.8 },
      lastChangedAt: '2026-03-20T16:15:00Z',
      lastChangedBy: 'architect@corp.example',
    },
  },
  {
    id: 'sk_arch_blueprint',
    key: 'architecture-blueprint',
    name: 'Architecture Blueprint',
    category: 'delivery',
    subCategory: 'Architecture',
    status: 'beta',
    defaultAutonomy: 'L1-Assist',
    owner: 'platform-architecture',
    description: 'Creates architecture views, integration boundaries, and deployment topology sketches.',
    stages: ['architecture', 'design'],
    version: 1,
    inputContract: 'Spec sections, integration map, capacity assumptions',
    outputContract: 'ContextDiagram, containerLayout, riskLog',
    policy: {
      autonomyLevel: 'L1-Assist',
      approvalRequiredActions: ['publish-architecture'],
      authorizedApproverRoles: ['Chief Architect'],
      riskThresholds: { maxExternalDependenciesAdded: 2 },
      lastChangedAt: '2026-04-05T10:10:00Z',
      lastChangedBy: 'chief.architect@corp.example',
    },
  },
  {
    id: 'sk_task_breakdown',
    key: 'task-breakdown-orchestrator',
    name: 'Task Breakdown Orchestrator',
    category: 'delivery',
    subCategory: 'Planning',
    status: 'active',
    defaultAutonomy: 'L1-Assist',
    owner: 'delivery-ops',
    description: 'Breaks approved designs into delivery tasks, owners, and sequencing hints.',
    stages: ['design', 'tasks'],
    version: 5,
    inputContract: 'Design decisions, milestones, team capacity',
    outputContract: 'TaskGraph, ownerRecommendations, criticalPath',
    policy: {
      autonomyLevel: 'L1-Assist',
      approvalRequiredActions: ['create-task-batch'],
      authorizedApproverRoles: ['Delivery Manager'],
      riskThresholds: { maxTasksPerBatch: 40 },
      lastChangedAt: '2026-03-18T08:20:00Z',
      lastChangedBy: 'delivery.manager@corp.example',
    },
  },
  {
    id: 'sk_pr_review',
    key: 'pr-risk-review',
    name: 'PR Risk Review',
    category: 'runtime',
    subCategory: 'Code Review',
    status: 'active',
    defaultAutonomy: 'L2-Auto-with-approval',
    owner: 'platform-devex',
    description: 'Reviews pull requests for risk hotspots, test gaps, and release posture.',
    stages: ['code', 'test'],
    version: 6,
    inputContract: 'PullRequest, changedFiles, baseline metrics',
    outputContract: 'RiskSummary, reviewComments, releaseRecommendation',
    policy: {
      autonomyLevel: 'L2-Auto-with-approval',
      approvalRequiredActions: ['block-merge'],
      authorizedApproverRoles: ['Tech Lead', 'Release Manager'],
      riskThresholds: { maxCriticalFindingsForAutoComment: 3 },
      lastChangedAt: '2026-04-08T13:45:00Z',
      lastChangedBy: 'devex.lead@corp.example',
    },
  },
  {
    id: 'sk_deploy_guard',
    key: 'deploy-rollback-guard',
    name: 'Deploy Rollback Guard',
    category: 'runtime',
    subCategory: 'Deployment',
    status: 'active',
    defaultAutonomy: 'L3-Auto',
    owner: 'platform-sre',
    description: 'Evaluates release health and can roll back within guardrails when failure thresholds trip.',
    stages: ['deploy', 'incident'],
    version: 2,
    inputContract: 'ReleaseId, canary metrics, error budgets',
    outputContract: 'RollbackDecision, impactSummary, followUpTasks',
    policy: {
      autonomyLevel: 'L3-Auto',
      approvalRequiredActions: [],
      authorizedApproverRoles: ['SRE Lead'],
      riskThresholds: { maxAffectedUsersPercent: 5, rollbackWindowMinutes: 20 },
      lastChangedAt: '2026-04-10T06:00:00Z',
      lastChangedBy: 'sre.governance@corp.example',
    },
  },
  {
    id: 'sk_learning_digest',
    key: 'learning-digest',
    name: 'Learning Digest',
    category: 'delivery',
    subCategory: 'Learning',
    status: 'deprecated',
    defaultAutonomy: 'L0-Manual',
    owner: 'knowledge-team',
    description: 'Compiles retro actions and recurring patterns into a weekly learning digest.',
    stages: ['learning'],
    version: 1,
    inputContract: 'Retrospective notes, incidents, metrics deltas',
    outputContract: 'DigestDraft, taggedThemes, recommendedFollowUps',
    policy: {
      autonomyLevel: 'L0-Manual',
      approvalRequiredActions: ['publish-learning-digest'],
      authorizedApproverRoles: ['Knowledge Manager'],
      riskThresholds: { redactionRequired: true },
      lastChangedAt: '2026-02-14T15:40:00Z',
      lastChangedBy: 'knowledge.manager@corp.example',
    },
  },
];

const RUN_SEEDS: RunSeed[] = [
  buildRunSeed('run_inc_001', 'incident-diagnosis', 'Incident Diagnosis', 'succeeded', isoDaysAgo(1, 14, 1), {
    triggerSourcePage: '/incidents/INC-0422',
    triggerSourceUrl: '/incidents/INC-0422',
    triggeredBy: 'auto-detector',
    triggeredByType: 'ai',
    autonomyLevel: 'L2-Auto-with-approval',
    durationMs: 29120,
    timeSavedMinutes: 25,
    outcomeSummary: 'Correlated DEP-9981 to the latency regression and recommended rollback.',
    inputSummary: { incidentId: 'INC-0422', signals: ['high-5xx-rate', 'p99-latency-spike'], recentChangeWindowMinutes: 60 },
    outputSummary: { rootCauseHypothesis: 'DEP-9981 introduced an OrderService null-check regression.', confidence: 0.92, recommendedActions: ['rollback:DEP-9981'] },
    stepBreakdown: makeRunSteps(isoDaysAgo(1, 14, 1), [
      { name: 'gather-signals', status: 'succeeded', minutes: 6 },
      { name: 'correlate-changes', status: 'succeeded', minutes: 10 },
      { name: 'form-hypothesis', status: 'succeeded', minutes: 10 },
      { name: 'propose-actions', status: 'succeeded', minutes: 3 },
    ]),
    policyTrail: [
      { rule: 'autonomy<=L2', decision: 'allowed', at: isoDaysAgo(1, 14, 1) },
      { rule: 'action=rollback gated', decision: 'held-for-approval', at: isoDaysAgo(1, 14, 2), note: 'Escalated to SRE Lead' },
    ],
    evidenceLinks: [
      { title: 'Deploy DEP-9981', type: 'deploy', sourceSystem: 'internal', url: '/deployments/DEP-9981' },
      { title: 'Grafana latency dashboard', type: 'metric', sourceSystem: 'grafana', url: 'https://grafana.internal/d/api-latency' },
      { title: 'MR !4123 order-service', type: 'code', sourceSystem: 'gitlab', url: 'https://gitlab.example/org/order-service/-/merge_requests/4123' },
    ],
  }),
  buildRunSeed('run_pr_001', 'pr-risk-review', 'PR Risk Review', 'pending_approval', isoDaysAgo(1, 9, 20), {
    triggerSourcePage: '/code/pulls/PR-1842',
    triggerSourceUrl: '/code/pulls/PR-1842',
    triggeredBy: 'release-bot',
    triggeredByType: 'system',
    autonomyLevel: 'L2-Auto-with-approval',
    durationMs: 21800,
    timeSavedMinutes: 16,
    outcomeSummary: 'Found 2 high-risk files and is waiting for merge-block approval.',
    inputSummary: { prId: 'PR-1842', filesChanged: 17, service: 'payments-api' },
    outputSummary: { summary: 'Two merge blockers proposed', findings: 4, riskScore: 0.84 },
    evidenceLinks: [
      { title: 'Coverage diff', type: 'test', sourceSystem: 'internal', url: '/code/pulls/PR-1842/coverage' },
    ],
  }),
  buildRunSeed('run_req_001', 'req-to-user-story', 'Requirement to User Story', 'succeeded', isoDaysAgo(2, 11, 5), {
    triggerSourcePage: '/requirements/REQ-118',
    triggerSourceUrl: '/requirements/REQ-118',
    triggeredBy: 'maria.chen',
    triggeredByType: 'human',
    autonomyLevel: 'L1-Assist',
    durationMs: 16400,
    timeSavedMinutes: 35,
    outcomeSummary: 'Generated 7 Jira-ready stories for omnichannel returns.',
    inputSummary: { requirementId: 'REQ-118', domain: 'returns', stakeholders: 3 },
    outputSummary: { createdStories: 7, unresolvedQuestions: 2 },
  }),
  buildRunSeed('run_spec_001', 'user-story-to-spec', 'User Story to Spec', 'succeeded', isoDaysAgo(3, 10, 10), {
    triggerSourcePage: '/requirements/REQ-118',
    triggerSourceUrl: '/requirements/REQ-118',
    triggeredBy: 'maria.chen',
    triggeredByType: 'human',
    autonomyLevel: 'L1-Assist',
    durationMs: 24200,
    timeSavedMinutes: 42,
    outcomeSummary: 'Drafted spec sections and API contracts for the returns flow.',
  }),
  buildRunSeed('run_arch_001', 'architecture-blueprint', 'Architecture Blueprint', 'failed', isoDaysAgo(4, 15, 35), {
    triggerSourcePage: '/design-management',
    triggerSourceUrl: '/design-management',
    triggeredBy: 'chief-architect-bot',
    triggeredByType: 'ai',
    autonomyLevel: 'L1-Assist',
    durationMs: 18600,
    timeSavedMinutes: 8,
    outcomeSummary: 'Stopped after service topology import failed validation.',
    outputSummary: { summary: 'Topology file schema mismatch', confidence: 0.41 },
  }),
  buildRunSeed('run_tasks_001', 'task-breakdown-orchestrator', 'Task Breakdown Orchestrator', 'succeeded', isoDaysAgo(5, 13, 0), {
    triggerSourcePage: '/project-management/plan/proj-42',
    triggerSourceUrl: '/project-management/plan/proj-42',
    triggeredBy: 'delivery-manager',
    triggeredByType: 'human',
    durationMs: 15100,
    timeSavedMinutes: 28,
    outcomeSummary: 'Produced 19 sequenced tasks with critical-path markers.',
  }),
  buildRunSeed('run_deploy_001', 'deploy-rollback-guard', 'Deploy Rollback Guard', 'rolled_back', isoDaysAgo(6, 18, 12), {
    triggerSourceType: 'webhook',
    triggerSourcePage: '/deployments/DEP-9901',
    triggerSourceUrl: '/deployments/DEP-9901',
    triggeredBy: 'deploy-webhook',
    triggeredByType: 'system',
    autonomyLevel: 'L3-Auto',
    durationMs: 14800,
    timeSavedMinutes: 52,
    outcomeSummary: 'Canary metrics breached SLO and rollback executed automatically.',
    evidenceLinks: [
      { title: 'Rollback execution log', type: 'deploy', sourceSystem: 'argo', url: 'https://argo.internal/rollbacks/DEP-9901' },
      { title: 'SLO burn alert', type: 'metric', sourceSystem: 'grafana', url: 'https://grafana.internal/d/slo-burn' },
    ],
  }),
  buildRunSeed('run_inc_002', 'incident-diagnosis', 'Incident Diagnosis', 'rejected', isoDaysAgo(7, 8, 45), {
    triggerSourcePage: '/incidents/INC-0417',
    triggerSourceUrl: '/incidents/INC-0417',
    triggeredBy: 'auto-detector',
    triggeredByType: 'ai',
    autonomyLevel: 'L2-Auto-with-approval',
    durationMs: 19500,
    timeSavedMinutes: 12,
    outcomeSummary: 'Rollback recommendation rejected after on-call confirmed external dependency outage.',
  }),
  buildRunSeed('run_pr_002', 'pr-risk-review', 'PR Risk Review', 'failed', isoDaysAgo(8, 16, 25), {
    triggerSourcePage: '/code/pulls/PR-1831',
    triggerSourceUrl: '/code/pulls/PR-1831',
    triggeredBy: 'release-bot',
    triggeredByType: 'system',
    durationMs: 9100,
    timeSavedMinutes: 5,
    outcomeSummary: 'Review aborted because diff artifacts were unavailable.',
  }),
  buildRunSeed('run_req_002', 'req-to-user-story', 'Requirement to User Story', 'succeeded', isoDaysAgo(9, 9, 10), {
    triggerSourcePage: '/requirements/REQ-102',
    triggerSourceUrl: '/requirements/REQ-102',
    triggeredBy: 'amal.gupta',
    triggeredByType: 'human',
    durationMs: 12400,
    timeSavedMinutes: 31,
    outcomeSummary: 'Produced 5 user stories and 11 acceptance criteria.',
  }),
  buildRunSeed('run_spec_002', 'user-story-to-spec', 'User Story to Spec', 'pending_approval', isoDaysAgo(10, 11, 55), {
    triggerSourcePage: '/requirements/REQ-102',
    triggerSourceUrl: '/requirements/REQ-102',
    triggeredBy: 'amal.gupta',
    triggeredByType: 'human',
    durationMs: 19800,
    timeSavedMinutes: 24,
    outcomeSummary: 'Spec draft is waiting for publication approval.',
  }),
  buildRunSeed('run_arch_002', 'architecture-blueprint', 'Architecture Blueprint', 'succeeded', isoDaysAgo(11, 14, 5), {
    triggerSourcePage: '/design-management/artifacts/arch-220',
    triggerSourceUrl: '/design-management/artifacts/arch-220',
    triggeredBy: 'architecture-bot',
    triggeredByType: 'ai',
    durationMs: 23200,
    timeSavedMinutes: 19,
  }),
  buildRunSeed('run_tasks_002', 'task-breakdown-orchestrator', 'Task Breakdown Orchestrator', 'succeeded', isoDaysAgo(12, 10, 15), {
    triggerSourcePage: '/project-management/plan/proj-51',
    triggerSourceUrl: '/project-management/plan/proj-51',
    triggeredBy: 'delivery-manager',
    triggeredByType: 'human',
    durationMs: 17100,
    timeSavedMinutes: 26,
  }),
  buildRunSeed('run_deploy_002', 'deploy-rollback-guard', 'Deploy Rollback Guard', 'succeeded', isoDaysAgo(13, 19, 30), {
    triggerSourceType: 'webhook',
    triggerSourcePage: '/deployments/DEP-9872',
    triggerSourceUrl: '/deployments/DEP-9872',
    triggeredBy: 'deploy-webhook',
    triggeredByType: 'system',
    autonomyLevel: 'L3-Auto',
    durationMs: 13200,
    timeSavedMinutes: 38,
  }),
  buildRunSeed('run_inc_003', 'incident-diagnosis', 'Incident Diagnosis', 'failed', isoDaysAgo(14, 7, 50), {
    triggerSourcePage: '/incidents/INC-0412',
    triggerSourceUrl: '/incidents/INC-0412',
    triggeredBy: 'auto-detector',
    triggeredByType: 'ai',
    autonomyLevel: 'L2-Auto-with-approval',
    durationMs: 8400,
    timeSavedMinutes: 3,
    outcomeSummary: 'Signal correlation timed out before change windows were hydrated.',
  }),
  buildRunSeed('run_pr_003', 'pr-risk-review', 'PR Risk Review', 'succeeded', isoDaysAgo(15, 15, 40), {
    triggerSourcePage: '/code/pulls/PR-1818',
    triggerSourceUrl: '/code/pulls/PR-1818',
    triggeredBy: 'release-bot',
    triggeredByType: 'system',
    durationMs: 17600,
    timeSavedMinutes: 13,
  }),
  buildRunSeed('run_req_003', 'req-to-user-story', 'Requirement to User Story', 'running', isoDaysAgo(16, 8, 25), {
    triggerSourcePage: '/requirements/REQ-097',
    triggerSourceUrl: '/requirements/REQ-097',
    triggeredBy: 'sofia.alvarez',
    triggeredByType: 'human',
    durationMs: null,
    autonomyLevel: 'L1-Assist',
    timeSavedMinutes: 0,
    outcomeSummary: 'Still clustering requirement statements and deduplicating actors.',
    stepBreakdown: makeRunSteps(isoDaysAgo(16, 8, 25), [
      { name: 'collect-source', status: 'succeeded', minutes: 4 },
      { name: 'cluster-intent', status: 'running', minutes: 0, note: 'Waiting on KB enrichment.' },
    ]),
  }),
  buildRunSeed('run_spec_003', 'user-story-to-spec', 'User Story to Spec', 'succeeded', isoDaysAgo(17, 9, 55), {
    triggerSourcePage: '/requirements/REQ-091',
    triggerSourceUrl: '/requirements/REQ-091',
    triggeredBy: 'sofia.alvarez',
    triggeredByType: 'human',
    durationMs: 22300,
    timeSavedMinutes: 37,
  }),
  buildRunSeed('run_arch_003', 'architecture-blueprint', 'Architecture Blueprint', 'succeeded', isoDaysAgo(18, 14, 40), {
    triggerSourcePage: '/design-management/artifacts/arch-219',
    triggerSourceUrl: '/design-management/artifacts/arch-219',
    triggeredBy: 'chief-architect-bot',
    triggeredByType: 'ai',
    durationMs: 20800,
    timeSavedMinutes: 16,
  }),
  buildRunSeed('run_tasks_003', 'task-breakdown-orchestrator', 'Task Breakdown Orchestrator', 'failed', isoDaysAgo(19, 12, 35), {
    triggerSourcePage: '/project-management/plan/proj-88',
    triggerSourceUrl: '/project-management/plan/proj-88',
    triggeredBy: 'delivery-manager',
    triggeredByType: 'human',
    durationMs: 6900,
    timeSavedMinutes: 2,
    outcomeSummary: 'Task graph generation stopped because milestone dates were missing.',
  }),
  buildRunSeed('run_deploy_003', 'deploy-rollback-guard', 'Deploy Rollback Guard', 'succeeded', isoDaysAgo(20, 17, 45), {
    triggerSourceType: 'webhook',
    triggerSourcePage: '/deployments/DEP-9832',
    triggerSourceUrl: '/deployments/DEP-9832',
    triggeredBy: 'deploy-webhook',
    triggeredByType: 'system',
    autonomyLevel: 'L3-Auto',
    durationMs: 11600,
    timeSavedMinutes: 34,
  }),
  buildRunSeed('run_inc_004', 'incident-diagnosis', 'Incident Diagnosis', 'succeeded', isoDaysAgo(21, 6, 15), {
    triggerSourcePage: '/incidents/INC-0401',
    triggerSourceUrl: '/incidents/INC-0401',
    triggeredBy: 'auto-detector',
    triggeredByType: 'ai',
    autonomyLevel: 'L2-Auto-with-approval',
    durationMs: 24100,
    timeSavedMinutes: 21,
  }),
  buildRunSeed('run_pr_004', 'pr-risk-review', 'PR Risk Review', 'succeeded', isoDaysAgo(23, 16, 10), {
    triggerSourcePage: '/code/pulls/PR-1777',
    triggerSourceUrl: '/code/pulls/PR-1777',
    triggeredBy: 'release-bot',
    triggeredByType: 'system',
    durationMs: 15400,
    timeSavedMinutes: 11,
  }),
  buildRunSeed('run_req_004', 'req-to-user-story', 'Requirement to User Story', 'succeeded', isoDaysAgo(24, 10, 45), {
    triggerSourcePage: '/requirements/REQ-072',
    triggerSourceUrl: '/requirements/REQ-072',
    triggeredBy: 'maria.chen',
    triggeredByType: 'human',
    durationMs: 13800,
    timeSavedMinutes: 29,
  }),
  buildRunSeed('run_learning_001', 'learning-digest', 'Learning Digest', 'succeeded', isoDaysAgo(27, 9, 5), {
    triggerSourceType: 'schedule',
    triggerSourcePage: null,
    triggerSourceUrl: null,
    triggeredBy: 'weekly-retro-job',
    triggeredByType: 'system',
    autonomyLevel: 'L0-Manual',
    durationMs: 18400,
    timeSavedMinutes: 14,
    outcomeSummary: 'Compiled a weekly digest before the skill was deprecated.',
  }),
];

function clamp(value: number, minimum: number, maximum: number): number {
  return Math.max(minimum, Math.min(maximum, value));
}

function workspaceOffset(workspaceId: string): number {
  return Array.from(workspaceId).reduce((sum, char) => sum + char.charCodeAt(0), 0) % 7;
}

function successRateForRuns(runs: RunSeed[]): number | null {
  const completed = runs.filter(run => run.status !== 'running');
  if (!completed.length) {
    return null;
  }
  const successful = completed.filter(run => run.status === 'succeeded').length;
  return Number((successful / completed.length).toFixed(2));
}

function recentRunsForSkill(skillKey: string): RunSeed[] {
  return RUN_SEEDS
    .filter(run => run.skillKey === skillKey)
    .sort((left, right) => right.startedAt.localeCompare(left.startedAt))
    .slice(0, 10);
}

function aggregateMetricsForSkill(skillKey: string): AggregateMetrics {
  const runs = recentRunsForSkill(skillKey);
  const completed = runs.filter(run => run.durationMs != null);
  const successRate = successRateForRuns(runs) ?? 0;
  const avgDurationMs = completed.length
    ? Math.round(completed.reduce((sum, run) => sum + (run.durationMs ?? 0), 0) / completed.length)
    : 0;
  return {
    successRate,
    avgDurationMs,
    adoptionTrend: successRate >= 0.85 ? 'up' : successRate >= 0.7 ? 'flat' : 'down',
    totalRuns30d: RUN_SEEDS.filter(run => run.skillKey === skillKey).length,
  };
}

function buildFixture(workspaceId: string): WorkspaceFixture {
  const offset = workspaceOffset(workspaceId);
  const skills = SKILL_SEEDS.map(seed => {
    const skillRuns = recentRunsForSkill(seed.key);
    return {
      id: seed.id,
      key: seed.key,
      name: seed.name,
      category: seed.category,
      subCategory: seed.subCategory,
      status: seed.status,
      defaultAutonomy: seed.defaultAutonomy,
      owner: seed.owner,
      description: seed.description,
      stages: seed.stages,
      lastExecutedAt: skillRuns[0]?.startedAt ?? null,
      successRate30d: successRateForRuns(RUN_SEEDS.filter(run => run.skillKey === seed.key)),
      version: seed.version,
    };
  });

  const runs = RUN_SEEDS
    .map<Run>(seed => ({
      id: seed.id,
      skillKey: seed.skillKey,
      skillName: seed.skillName,
      status: seed.status,
      triggerSourceType: seed.triggerSourceType,
      triggerSourcePage: seed.triggerSourcePage,
      triggerSourceUrl: seed.triggerSourceUrl,
      triggeredBy: seed.triggeredBy,
      triggeredByType: seed.triggeredByType,
      startedAt: seed.startedAt,
      endedAt: seed.endedAt,
      durationMs: seed.durationMs,
      outcomeSummary: seed.outcomeSummary,
      auditRecordId: seed.auditRecordId,
    }))
    .sort((left, right) => right.startedAt.localeCompare(left.startedAt));

  const skillDetails = Object.fromEntries(SKILL_SEEDS.map(seed => {
    const skill = skills.find(entry => entry.key === seed.key)!;
    const recentRuns = recentRunsForSkill(seed.key).map(run => runs.find(item => item.id === run.id)!);
    return [seed.key, {
      skill,
      inputContract: seed.inputContract,
      outputContract: seed.outputContract,
      policy: {
        skillKey: seed.key,
        ...seed.policy,
      },
      recentRuns,
      aggregateMetrics: aggregateMetricsForSkill(seed.key),
    } satisfies SkillDetailResponse];
  }));

  const runDetails = Object.fromEntries(RUN_SEEDS.map(seed => {
    const run = runs.find(item => item.id === seed.id)!;
    return [seed.id, {
      run,
      inputSummary: seed.inputSummary,
      outputSummary: seed.outputSummary,
      stepBreakdown: seed.stepBreakdown,
      policyTrail: seed.policyTrail,
      evidenceLinks: seed.evidenceLinks,
      autonomyLevel: seed.autonomyLevel,
      timeSavedMinutes: seed.timeSavedMinutes,
    } satisfies RunDetailResponse];
  }));

  const stageCoverage = SDLC_STAGE_ORDER.map(stageKey => {
    const activeSkillCount = skills.filter(skill => skill.status !== 'deprecated' && skill.stages.includes(stageKey)).length;
    return {
      stageKey,
      stageLabel: STAGE_LABELS[stageKey],
      activeSkillCount,
      covered: activeSkillCount > 0,
    };
  });

  const makeMetric = (value: number, unit: MetricValue['unit'], delta: number, isPositive: boolean): MetricValue => ({
    value: Number(value.toFixed(unit === 'count' ? 0 : 1)),
    unit,
    delta: Number(delta.toFixed(unit === 'count' ? 0 : 1)),
    trend: delta > 0 ? 'up' : delta < 0 ? 'down' : 'flat',
    isPositive: delta === 0 ? true : isPositive,
  });

  const metrics: MetricsSummary = {
    window: '30d',
    aiUsageRate: {
      data: makeMetric(clamp(58 + offset * 1.8, 40, 78), '%', 2.6 - offset * 0.3, true),
      error: null,
    },
    adoptionRate: {
      data: makeMetric(clamp(72 - offset * 1.2, 52, 88), '%', offset % 2 === 0 ? 3.2 : -1.8, offset % 2 === 0),
      error: null,
    },
    autoExecSuccessRate: {
      data: makeMetric(clamp(86 + offset * 1.1, 70, 96), '%', 1.4, true),
      error: null,
    },
    timeSavedHours: {
      data: makeMetric(clamp(118 + offset * 7, 80, 160), 'hours', 12 + offset, true),
      error: null,
    },
    stageCoverageCount: {
      data: makeMetric(stageCoverage.filter(entry => entry.covered).length, 'count', offset > 3 ? 1 : 0, true),
      error: null,
    },
  };

  return {
    skills,
    skillDetails,
    runs,
    runDetails,
    metrics,
    stageCoverage,
  };
}

function getScenarioFlags(): Set<MockScenario> {
  if (typeof window === 'undefined') {
    return new Set();
  }
  const raw = new URLSearchParams(window.location.search).get('aiCenterScenario');
  if (!raw) {
    return new Set();
  }
  return new Set(raw.split(',').map(flag => flag.trim()).filter(Boolean) as MockScenario[]);
}

function applyLatency<T>(payload: T): Promise<T> {
  return new Promise(resolve => {
    setTimeout(() => resolve(payload), 20);
  });
}

function errorResponse<T>(message: string): ApiResponse<T> {
  return {
    data: null,
    error: message,
  };
}

export function getMockFixture(workspaceId: string): WorkspaceFixture {
  return buildFixture(workspaceId || 'ws-default-001');
}

export async function mockGetMetrics(workspaceId: string): Promise<ApiResponse<MetricsSummary>> {
  const scenarios = getScenarioFlags();
  if (scenarios.has('error-metrics')) {
    return applyLatency(errorResponse('Metrics aggregation is temporarily unavailable.'));
  }
  if (scenarios.has('empty-metrics')) {
    return applyLatency({
      data: {
        window: '30d',
        aiUsageRate: { data: null, error: null },
        adoptionRate: { data: null, error: null },
        autoExecSuccessRate: { data: null, error: null },
        timeSavedHours: { data: null, error: null },
        stageCoverageCount: { data: null, error: null },
      },
      error: null,
    });
  }

  const metrics = structuredClone(getMockFixture(workspaceId).metrics);
  if (scenarios.has('partial-metrics')) {
    metrics.aiUsageRate = { data: null, error: 'Aggregation timed out' };
  }
  return applyLatency({ data: metrics, error: null });
}

export async function mockGetStageCoverage(workspaceId: string): Promise<ApiResponse<{ entries: StageCoverage }>> {
  const scenarios = getScenarioFlags();
  if (scenarios.has('error-stage-coverage')) {
    return applyLatency(errorResponse('Stage coverage is unavailable.'));
  }
  return applyLatency({
    data: {
      entries: getMockFixture(workspaceId).stageCoverage,
    },
    error: null,
  });
}

export async function mockGetSkills(workspaceId: string): Promise<ApiResponse<Skill[]>> {
  const scenarios = getScenarioFlags();
  if (scenarios.has('error-skills')) {
    return applyLatency(errorResponse('Skill catalog failed to load.'));
  }
  if (scenarios.has('empty-skills')) {
    return applyLatency({ data: [], error: null });
  }
  return applyLatency({ data: getMockFixture(workspaceId).skills, error: null });
}

export async function mockGetSkillDetail(workspaceId: string, skillKey: string): Promise<ApiResponse<SkillDetailResponse>> {
  const scenarios = getScenarioFlags();
  if (scenarios.has('404-skill')) {
    return applyLatency(errorResponse(`Skill not found: ${skillKey}`));
  }

  const detail = getMockFixture(workspaceId).skillDetails[skillKey];
  if (!detail) {
    return applyLatency(errorResponse(`Skill not found: ${skillKey}`));
  }
  return applyLatency({ data: detail, error: null });
}

function filterRuns(items: Run[], query: RunQuery): Run[] {
  return items.filter(run => {
    if (query.skillKey?.length && !query.skillKey.includes(run.skillKey)) {
      return false;
    }
    if (query.status?.length && !query.status.includes(run.status)) {
      return false;
    }
    if (query.triggerSourcePage && run.triggerSourcePage !== query.triggerSourcePage) {
      return false;
    }
    if (query.triggeredByType && run.triggeredByType !== query.triggeredByType) {
      return false;
    }
    if (query.startedAfter && run.startedAt < query.startedAfter) {
      return false;
    }
    if (query.startedBefore && run.startedAt >= query.startedBefore) {
      return false;
    }
    return true;
  });
}

export async function mockGetRuns(workspaceId: string, query: RunQuery = {}): Promise<ApiResponse<Page<Run>>> {
  const scenarios = getScenarioFlags();
  if (scenarios.has('error-runs')) {
    return applyLatency(errorResponse('Run history failed to load.'));
  }

  const merged = {
    ...DEFAULT_RUN_QUERY,
    ...query,
  };

  const allRuns = scenarios.has('empty-runs') ? [] : getMockFixture(workspaceId).runs;
  const filtered = filterRuns(allRuns, merged);
  const page = merged.page ?? 1;
  const size = merged.size ?? 50;
  const start = (page - 1) * size;
  const items = filtered.slice(start, start + size);

  return applyLatency({
    data: {
      items,
      page,
      size,
      total: filtered.length,
      hasMore: start + size < filtered.length,
    },
    error: null,
  });
}

export async function mockGetRunDetail(workspaceId: string, executionId: string): Promise<ApiResponse<RunDetailResponse>> {
  const scenarios = getScenarioFlags();
  if (scenarios.has('404-run')) {
    return applyLatency(errorResponse(`Skill execution not found: ${executionId}`));
  }

  const detail = getMockFixture(workspaceId).runDetails[executionId];
  if (!detail) {
    return applyLatency(errorResponse(`Skill execution not found: ${executionId}`));
  }
  return applyLatency({ data: detail, error: null });
}
