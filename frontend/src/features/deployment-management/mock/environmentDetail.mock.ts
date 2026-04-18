import type { EnvironmentDetailAggregate, EnvironmentTimelineEntry, EnvironmentMetrics, EnvironmentRevisions } from '../types/environment';

function timeline(count: number, hasRollback: boolean): EnvironmentTimelineEntry[] {
  return Array.from({ length: count }, (_, i) => ({
    deployId: `deploy-env-${300 + i}`,
    releaseVersion: `2026.04.${17 - Math.floor(i / 2)}-00${50 - i}`,
    state: (hasRollback && i === 2) ? 'ROLLED_BACK' as const
      : (i === 5) ? 'FAILED' as const
      : 'SUCCEEDED' as const,
    startedAt: new Date(Date.UTC(2026, 3, 17 - Math.floor(i / 2), 10 + i)).toISOString(),
    durationSec: 120 + i * 20,
    isRollback: hasRollback && i === 2,
  }));
}

const devRevisions: EnvironmentRevisions = {
  currentRevision: '2026.04.17-0050', currentDeployId: 'deploy-env-300',
  priorRevision: '2026.04.17-0049', priorDeployId: 'deploy-env-301',
  lastGoodRevision: '2026.04.17-0050', lastGoodDeployId: 'deploy-env-300',
  lastFailedRevision: null, lastFailedDeployId: null,
};

const devMetrics: EnvironmentMetrics = {
  changeFailureRate30d: 0.0, mttrSec30d: null, deployCount30d: 60, rollbackCount30d: 0, deploymentFrequency30d: 2.0,
};

export const MOCK_DEV_ENV: EnvironmentDetailAggregate = {
  header: { data: { applicationId: 'app-order-svc', environmentName: 'dev', kind: 'DEV' }, error: null },
  revisions: { data: devRevisions, error: null },
  timeline: { data: timeline(20, false), error: null },
  metrics: { data: devMetrics, error: null },
};

const prodRevisions: EnvironmentRevisions = {
  currentRevision: '2026.04.15-0038', currentDeployId: 'deploy-env-310',
  priorRevision: '2026.04.14-0037', priorDeployId: 'deploy-env-311',
  lastGoodRevision: '2026.04.15-0038', lastGoodDeployId: 'deploy-env-310',
  lastFailedRevision: '2026.04.10-0033', lastFailedDeployId: 'deploy-env-315',
};

const prodMetrics: EnvironmentMetrics = {
  changeFailureRate30d: 0.12, mttrSec30d: 1800, deployCount30d: 25, rollbackCount30d: 1, deploymentFrequency30d: 0.83,
};

export const MOCK_PROD_ENV: EnvironmentDetailAggregate = {
  header: { data: { applicationId: 'app-order-svc', environmentName: 'prod', kind: 'PROD' }, error: null },
  revisions: { data: prodRevisions, error: null },
  timeline: { data: timeline(15, true), error: null },
  metrics: { data: prodMetrics, error: null },
};

export const MOCK_STAGING_ENV: EnvironmentDetailAggregate = {
  header: { data: { applicationId: 'app-order-svc', environmentName: 'staging', kind: 'STAGING' }, error: null },
  revisions: { data: { currentRevision: '2026.04.16-0039', currentDeployId: 'deploy-env-320', priorRevision: '2026.04.15-0038', priorDeployId: 'deploy-env-321', lastGoodRevision: '2026.04.16-0039', lastGoodDeployId: 'deploy-env-320', lastFailedRevision: null, lastFailedDeployId: null }, error: null },
  timeline: { data: timeline(12, false), error: null },
  metrics: { data: { changeFailureRate30d: 0.0, mttrSec30d: null, deployCount30d: 35, rollbackCount30d: 0, deploymentFrequency30d: 1.17 }, error: null },
};

export const MOCK_QA_ENV: EnvironmentDetailAggregate = {
  header: { data: { applicationId: 'app-order-svc', environmentName: 'qa', kind: 'TEST' }, error: null },
  revisions: { data: { currentRevision: '2026.04.17-0041', currentDeployId: 'deploy-env-330', priorRevision: '2026.04.16-0040', priorDeployId: 'deploy-env-331', lastGoodRevision: '2026.04.17-0041', lastGoodDeployId: 'deploy-env-330', lastFailedRevision: '2026.04.12-0035', lastFailedDeployId: 'deploy-env-335' }, error: null },
  timeline: { data: timeline(18, false).map((e, i) => i === 6 ? { ...e, state: 'FAILED' as const } : e), error: null },
  metrics: { data: { changeFailureRate30d: 0.06, mttrSec30d: 900, deployCount30d: 50, rollbackCount30d: 0, deploymentFrequency30d: 1.67 }, error: null },
};

export const MOCK_ENVIRONMENT_MAP: Record<string, EnvironmentDetailAggregate> = {
  'app-order-svc:dev': MOCK_DEV_ENV,
  'app-order-svc:prod': MOCK_PROD_ENV,
  'app-order-svc:staging': MOCK_STAGING_ENV,
  'app-order-svc:qa': MOCK_QA_ENV,
};
