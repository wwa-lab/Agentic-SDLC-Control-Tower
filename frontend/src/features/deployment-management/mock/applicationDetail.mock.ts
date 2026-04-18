import type { ApplicationDetailAggregate, ApplicationHeader, EnvironmentRow, RecentReleaseRow, RecentDeployRow, TraceSummaryRow, ApplicationAiInsight } from '../types/application';

const HEALTHY_HEADER: ApplicationHeader = {
  applicationId: 'app-order-svc',
  name: 'Order Service',
  projectId: 'proj-commerce',
  workspaceId: 'ws-default-001',
  runtimeLabel: 'jvm',
  jenkinsFolderPath: 'commerce/order-service',
  jenkinsFolderUrl: 'https://jenkins.example.com/job/commerce/job/order-service/',
  lastDeployAt: '2026-04-17T14:30:00Z',
  description: 'Core order processing microservice',
};

const HEALTHY_ENVS: EnvironmentRow[] = [
  { environmentName: 'dev', kind: 'DEV', currentRevision: '2026.04.17-0042', currentRevisionReleaseId: 'release-042', currentDeployState: 'SUCCEEDED', currentDeployedAt: '2026-04-17T14:30:00Z', priorRevision: '2026.04.17-0041', lastGoodRevision: '2026.04.17-0042', isRolledBack: false, rolledBackToReleaseVersion: null },
  { environmentName: 'qa', kind: 'TEST', currentRevision: '2026.04.17-0041', currentRevisionReleaseId: 'release-041', currentDeployState: 'SUCCEEDED', currentDeployedAt: '2026-04-17T12:00:00Z', priorRevision: '2026.04.16-0040', lastGoodRevision: '2026.04.17-0041', isRolledBack: false, rolledBackToReleaseVersion: null },
  { environmentName: 'staging', kind: 'STAGING', currentRevision: '2026.04.16-0039', currentRevisionReleaseId: 'release-039', currentDeployState: 'SUCCEEDED', currentDeployedAt: '2026-04-16T16:00:00Z', priorRevision: '2026.04.15-0038', lastGoodRevision: '2026.04.16-0039', isRolledBack: false, rolledBackToReleaseVersion: null },
  { environmentName: 'prod', kind: 'PROD', currentRevision: '2026.04.15-0038', currentRevisionReleaseId: 'release-038', currentDeployState: 'SUCCEEDED', currentDeployedAt: '2026-04-15T10:00:00Z', priorRevision: '2026.04.12-0035', lastGoodRevision: '2026.04.15-0038', isRolledBack: false, rolledBackToReleaseVersion: null },
];

const HEALTHY_RELEASES: RecentReleaseRow[] = Array.from({ length: 10 }, (_, i) => ({
  releaseId: `release-${42 - i}`.padStart(3, '0'),
  releaseVersion: `2026.04.${17 - Math.floor(i / 3)}-00${42 - i}`,
  buildArtifactRef: { sliceId: 'code-build' as const, buildArtifactId: `ba-${42 - i}` },
  createdAt: new Date(Date.UTC(2026, 3, 17 - Math.floor(i / 3), 10 + i)).toISOString(),
  state: i === 0 ? 'DEPLOYED' as const : i < 3 ? 'DEPLOYED' as const : 'SUPERSEDED' as const,
  storyCount: 2 + (i % 4),
}));

const HEALTHY_DEPLOYS: RecentDeployRow[] = Array.from({ length: 12 }, (_, i) => ({
  deployId: `deploy-h-${100 + i}`,
  releaseVersion: `2026.04.${17 - Math.floor(i / 4)}-00${42 - Math.floor(i / 2)}`,
  environmentName: ['dev', 'qa', 'staging', 'prod'][i % 4],
  state: 'SUCCEEDED' as const,
  startedAt: new Date(Date.UTC(2026, 3, 17 - Math.floor(i / 4), 8 + i)).toISOString(),
  durationSec: 120 + i * 30,
  isCurrentRevision: i < 4,
  isRollback: false,
}));

const HEALTHY_TRACE: TraceSummaryRow[] = [
  { environmentName: 'dev', storiesLast30d: 48, deploysLast30d: 60 },
  { environmentName: 'qa', storiesLast30d: 42, deploysLast30d: 50 },
  { environmentName: 'staging', storiesLast30d: 30, deploysLast30d: 35 },
  { environmentName: 'prod', storiesLast30d: 22, deploysLast30d: 25 },
];

const HEALTHY_AI: ApplicationAiInsight = {
  status: 'SUCCESS',
  generatedAt: '2026-04-17T15:00:00Z',
  narrative: 'Order Service is running clean across all environments. 30-day deployment frequency is above target at 2.1/day. No rollbacks in the last 30 days. Consider promoting the current staging build to prod.',
  skillVersion: 'deploy-insights-v2',
};

export const MOCK_HEALTHY_APP: ApplicationDetailAggregate = {
  header: { data: HEALTHY_HEADER, error: null },
  environments: { data: HEALTHY_ENVS, error: null },
  recentReleases: { data: HEALTHY_RELEASES, error: null },
  recentDeploys: { data: HEALTHY_DEPLOYS, error: null },
  traceSummary: { data: HEALTHY_TRACE, error: null },
  aiInsights: { data: HEALTHY_AI, error: null },
};

const FLAKY_HEADER: ApplicationHeader = {
  applicationId: 'app-inventory',
  name: 'Inventory Service',
  projectId: 'proj-commerce',
  workspaceId: 'ws-default-001',
  runtimeLabel: 'jvm',
  jenkinsFolderPath: 'commerce/inventory-service',
  jenkinsFolderUrl: 'https://jenkins.example.com/job/commerce/job/inventory-service/',
  lastDeployAt: '2026-04-14T18:00:00Z',
  description: 'Inventory and warehouse management',
};

const FLAKY_ENVS: EnvironmentRow[] = [
  { environmentName: 'dev', kind: 'DEV', currentRevision: '2026.04.17-0033', currentRevisionReleaseId: 'release-inv-033', currentDeployState: 'SUCCEEDED', currentDeployedAt: '2026-04-17T09:00:00Z', priorRevision: '2026.04.16-0032', lastGoodRevision: '2026.04.17-0033', isRolledBack: false, rolledBackToReleaseVersion: null },
  { environmentName: 'qa', kind: 'TEST', currentRevision: '2026.04.16-0032', currentRevisionReleaseId: 'release-inv-032', currentDeployState: 'SUCCEEDED', currentDeployedAt: '2026-04-16T14:00:00Z', priorRevision: '2026.04.15-0031', lastGoodRevision: '2026.04.16-0032', isRolledBack: false, rolledBackToReleaseVersion: null },
  { environmentName: 'staging', kind: 'STAGING', currentRevision: '2026.04.15-0031', currentRevisionReleaseId: 'release-inv-031', currentDeployState: 'SUCCEEDED', currentDeployedAt: '2026-04-15T12:00:00Z', priorRevision: '2026.04.14-0030', lastGoodRevision: '2026.04.15-0031', isRolledBack: false, rolledBackToReleaseVersion: null },
  { environmentName: 'prod', kind: 'PROD', currentRevision: '2026.04.12-0028', currentRevisionReleaseId: 'release-inv-028', currentDeployState: 'SUCCEEDED', currentDeployedAt: '2026-04-12T10:00:00Z', priorRevision: '2026.04.14-0030', lastGoodRevision: '2026.04.12-0028', isRolledBack: true, rolledBackToReleaseVersion: '2026.04.12-0028' },
];

export const MOCK_FLAKY_APP: ApplicationDetailAggregate = {
  header: { data: FLAKY_HEADER, error: null },
  environments: { data: FLAKY_ENVS, error: null },
  recentReleases: { data: HEALTHY_RELEASES.map(r => ({ ...r, releaseId: r.releaseId.replace('release-', 'release-inv-') })), error: null },
  recentDeploys: { data: HEALTHY_DEPLOYS.slice(0, 8).map((d, i) => ({
    ...d,
    deployId: `deploy-inv-${200 + i}`,
    state: i === 3 ? 'ROLLED_BACK' as const : i === 7 ? 'ROLLED_BACK' as const : d.state,
    isRollback: i === 3 || i === 7,
  })), error: null },
  traceSummary: { data: HEALTHY_TRACE, error: null },
  aiInsights: { data: {
    status: 'SUCCESS',
    generatedAt: '2026-04-17T15:00:00Z',
    narrative: 'Inventory Service has had 2 rollbacks in the last 30 days on PROD. The most recent deployment (2026.04.14-0030) was rolled back to 2026.04.12-0028. Root cause appears to be a database migration conflict. Recommend holding PROD deploys until staging passes a full regression.',
    skillVersion: 'deploy-insights-v2',
  }, error: null },
};

const NEW_HEADER: ApplicationHeader = {
  applicationId: 'app-ml-serving',
  name: 'ML Serving',
  projectId: 'proj-data',
  workspaceId: 'ws-default-001',
  runtimeLabel: 'python',
  jenkinsFolderPath: 'data/ml-serving',
  jenkinsFolderUrl: 'https://jenkins.example.com/job/data/job/ml-serving/',
  lastDeployAt: '2026-04-17T08:00:00Z',
  description: 'Model inference endpoint',
};

export const MOCK_NEW_APP: ApplicationDetailAggregate = {
  header: { data: NEW_HEADER, error: null },
  environments: { data: [
    { environmentName: 'dev', kind: 'DEV', currentRevision: '2026.04.17-0044', currentRevisionReleaseId: 'release-ml-001', currentDeployState: 'PENDING', currentDeployedAt: null, priorRevision: null, lastGoodRevision: null, isRolledBack: false, rolledBackToReleaseVersion: null },
    { environmentName: 'qa', kind: 'TEST', currentRevision: null, currentRevisionReleaseId: null, currentDeployState: null, currentDeployedAt: null, priorRevision: null, lastGoodRevision: null, isRolledBack: false, rolledBackToReleaseVersion: null },
    { environmentName: 'staging', kind: 'STAGING', currentRevision: null, currentRevisionReleaseId: null, currentDeployState: null, currentDeployedAt: null, priorRevision: null, lastGoodRevision: null, isRolledBack: false, rolledBackToReleaseVersion: null },
    { environmentName: 'prod', kind: 'PROD', currentRevision: null, currentRevisionReleaseId: null, currentDeployState: null, currentDeployedAt: null, priorRevision: null, lastGoodRevision: null, isRolledBack: false, rolledBackToReleaseVersion: null },
  ], error: null },
  recentReleases: { data: [{ releaseId: 'release-ml-001', releaseVersion: '2026.04.17-0044', buildArtifactRef: { sliceId: 'code-build', buildArtifactId: 'ba-ml-001' }, createdAt: '2026-04-17T07:00:00Z', state: 'PREPARED', storyCount: 1 }], error: null },
  recentDeploys: { data: [], error: null },
  traceSummary: { data: [], error: null },
  aiInsights: { data: { status: 'PENDING' }, error: null },
};

export const MOCK_APPLICATION_MAP: Record<string, ApplicationDetailAggregate> = {
  'app-order-svc': MOCK_HEALTHY_APP,
  'app-inventory': MOCK_FLAKY_APP,
  'app-ml-serving': MOCK_NEW_APP,
};
