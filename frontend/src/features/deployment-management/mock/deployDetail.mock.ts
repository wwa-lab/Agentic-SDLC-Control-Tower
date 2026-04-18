import type { DeployDetailAggregate, DeployHeader, DeployStageRow, ApprovalEvent, DeployArtifactRefCard, OpenIncidentContextDto } from '../types/deploy';

function stages(state: import('../types/enums').DeployState): DeployStageRow[] {
  const base: DeployStageRow[] = [
    { stageId: 'stg-1', name: 'Checkout', order: 1, state: 'SUCCESS', startedAt: '2026-04-17T10:00:00Z', completedAt: '2026-04-17T10:00:15Z', durationSec: 15 },
    { stageId: 'stg-2', name: 'Build', order: 2, state: 'SUCCESS', startedAt: '2026-04-17T10:00:15Z', completedAt: '2026-04-17T10:01:30Z', durationSec: 75 },
    { stageId: 'stg-3', name: 'Unit Tests', order: 3, state: 'SUCCESS', startedAt: '2026-04-17T10:01:30Z', completedAt: '2026-04-17T10:03:00Z', durationSec: 90 },
    { stageId: 'stg-4', name: 'Deploy to Target', order: 4, state: 'SUCCESS', startedAt: '2026-04-17T10:03:00Z', completedAt: '2026-04-17T10:04:00Z', durationSec: 60 },
    { stageId: 'stg-5', name: 'Smoke Tests', order: 5, state: 'SUCCESS', startedAt: '2026-04-17T10:04:00Z', completedAt: '2026-04-17T10:05:30Z', durationSec: 90 },
  ];
  if (state === 'FAILED') {
    return base.map((s, i) => i === 4 ? { ...s, state: 'FAILURE' as const } : s);
  }
  if (state === 'IN_PROGRESS') {
    return [
      ...base.slice(0, 3),
      { ...base[3], state: 'IN_PROGRESS' as const, completedAt: undefined, durationSec: undefined },
      { stageId: 'stg-5', name: 'Smoke Tests', order: 5, state: 'NOT_STARTED', startedAt: undefined, completedAt: undefined, durationSec: undefined },
    ];
  }
  return base;
}

const artifactRef: DeployArtifactRefCard = {
  buildArtifactRef: { sliceId: 'code-build', buildArtifactId: 'ba-042' },
  buildArtifactResolved: true,
  buildSummary: { pipelineName: 'order-service-build', commitCount: 8, commitRangeHeadSha: 'abc0000de', commitRangeBaseSha: 'xyz9999ab' },
};

function makeHeader(overrides: Partial<DeployHeader> & Pick<DeployHeader, 'deployId' | 'state'>): DeployHeader {
  return {
    releaseId: 'release-042',
    releaseVersion: '2026.04.17-0042',
    applicationId: 'app-order-svc',
    environmentName: 'prod',
    jenkinsJobFullName: 'commerce/order-service/prod-deploy',
    jenkinsBuildNumber: 142,
    jenkinsBuildUrl: 'https://jenkins.example.com/job/commerce/job/order-service/job/prod-deploy/142/',
    trigger: 'MANUAL',
    actor: 'jane.pm',
    startedAt: '2026-04-17T10:00:00Z',
    completedAt: '2026-04-17T10:05:30Z',
    durationSec: 330,
    isCurrentRevision: true,
    isRollback: false,
    unresolvedFlag: false,
    ...overrides,
  };
}

function incidentCtx(deployId: string, env: string): OpenIncidentContextDto {
  return { applicationId: 'app-order-svc', environmentName: env, deployId, releaseVersion: '2026.04.17-0042', deployUrl: `/deployment/deploys/${deployId}` };
}

const PROD_APPROVAL: ApprovalEvent[] = [
  { approvalId: 'appr-001', stageId: 'stg-4', stageName: 'Deploy to Target', approverDisplayName: 'Jane PM', approverMemberId: 'member-jane', approverRole: 'PM', decision: 'APPROVED', gatePolicyVersion: 'gate-v1', rationale: 'Release is clean — all QA signoff complete, no open P1 bugs.', decidedAt: '2026-04-17T10:02:50Z' },
];

export const MOCK_SUCCEEDED_PROD: DeployDetailAggregate = {
  header: { data: makeHeader({ deployId: 'deploy-succ-001', state: 'SUCCEEDED' }), error: null },
  stageTimeline: { data: stages('SUCCEEDED'), error: null },
  approvals: { data: PROD_APPROVAL, error: null },
  artifactRef: { data: artifactRef, error: null },
  openIncidentContext: incidentCtx('deploy-succ-001', 'prod'),
};

export const MOCK_FAILED_PROD: DeployDetailAggregate = {
  header: { data: makeHeader({ deployId: 'deploy-fail-001', state: 'FAILED', completedAt: '2026-04-17T10:05:30Z' }), error: null },
  stageTimeline: { data: stages('FAILED'), error: null },
  approvals: { data: PROD_APPROVAL, error: null },
  artifactRef: { data: artifactRef, error: null },
  openIncidentContext: incidentCtx('deploy-fail-001', 'prod'),
};

export const MOCK_ROLLBACK_TRIGGER: DeployDetailAggregate = {
  header: { data: makeHeader({ deployId: 'deploy-rb-001', state: 'ROLLED_BACK', trigger: 'ROLLBACK', isRollback: true, releaseVersion: '2026.04.12-0028', releaseId: 'release-inv-028' }), error: null },
  stageTimeline: { data: stages('SUCCEEDED'), error: null },
  approvals: { data: [{ approvalId: 'appr-002', stageId: 'stg-4', stageName: 'Deploy to Target', approverDisplayName: 'Bob TechLead', approverMemberId: 'member-bob', approverRole: 'TECH_LEAD', decision: 'APPROVED', gatePolicyVersion: 'gate-v1', rationale: 'Emergency rollback approved — prod regression on order totals.', decidedAt: '2026-04-14T18:10:00Z' }], error: null },
  artifactRef: { data: { ...artifactRef, buildArtifactRef: { sliceId: 'code-build', buildArtifactId: 'ba-inv-028' } }, error: null },
  openIncidentContext: incidentCtx('deploy-rb-001', 'prod'),
  followedByRollback: undefined,
};

export const MOCK_ROLLBACK_VERSION: DeployDetailAggregate = {
  header: { data: makeHeader({ deployId: 'deploy-rb-002', state: 'ROLLED_BACK', environmentName: 'staging', isRollback: true, releaseVersion: '2026.04.13-0029', releaseId: 'release-inv-029' }), error: null },
  stageTimeline: { data: stages('SUCCEEDED'), error: null },
  approvals: { data: [], error: null },
  artifactRef: { data: artifactRef, error: null },
  openIncidentContext: incidentCtx('deploy-rb-002', 'staging'),
};

export const MOCK_IN_PROGRESS: DeployDetailAggregate = {
  header: { data: makeHeader({ deployId: 'deploy-ip-001', state: 'IN_PROGRESS', environmentName: 'dev', completedAt: undefined, durationSec: undefined, isCurrentRevision: false }), error: null },
  stageTimeline: { data: stages('IN_PROGRESS'), error: null },
  approvals: { data: [], error: null },
  artifactRef: { data: artifactRef, error: null },
  openIncidentContext: incidentCtx('deploy-ip-001', 'dev'),
};

export const MOCK_DEPLOY_MAP: Record<string, DeployDetailAggregate> = {
  'deploy-succ-001': MOCK_SUCCEEDED_PROD,
  'deploy-fail-001': MOCK_FAILED_PROD,
  'deploy-rb-001': MOCK_ROLLBACK_TRIGGER,
  'deploy-rb-002': MOCK_ROLLBACK_VERSION,
  'deploy-ip-001': MOCK_IN_PROGRESS,
};
