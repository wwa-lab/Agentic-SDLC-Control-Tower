import type { TraceabilityAggregate, TraceabilityReleaseRow, TraceabilityDeployGroup, StoryChip } from '../types/traceability';

const STORY_REGISTRY: Record<string, StoryChip> = {
  'STORY-100': { storyId: 'STORY-100', status: 'VERIFIED', title: 'Fix cart rounding', projectId: 'proj-commerce' },
  'STORY-102': { storyId: 'STORY-102', status: 'VERIFIED', title: 'Add bulk discounts', projectId: 'proj-commerce' },
  'STORY-104': { storyId: 'STORY-104', status: 'VERIFIED', title: 'Improve checkout flow', projectId: 'proj-commerce' },
  'STORY-106': { storyId: 'STORY-106', status: 'UNVERIFIED', title: 'Audit logging improvements', projectId: 'proj-platform' },
  'STORY-108': { storyId: 'STORY-108', status: 'VERIFIED', title: 'SSO token refresh', projectId: 'proj-identity' },
  'STORY-110': { storyId: 'STORY-110', status: 'VERIFIED', title: 'Notification channel config', projectId: 'proj-platform' },
  'STORY-112': { storyId: 'STORY-112', status: 'VERIFIED', title: 'Search index rebuild', projectId: 'proj-data' },
  'STORY-114': { storyId: 'STORY-114', status: 'VERIFIED', title: 'ETL retry logic', projectId: 'proj-data' },
  'STORY-116': { storyId: 'STORY-116', status: 'UNKNOWN_STORY', title: undefined, projectId: undefined },
  'STORY-118': { storyId: 'STORY-118', status: 'VERIFIED', title: 'Shared library auth update', projectId: 'proj-identity' },
  'STORY-120': { storyId: 'STORY-120', status: 'VERIFIED', title: 'Payment retry backoff', projectId: 'proj-commerce' },
  'STORY-122': { storyId: 'STORY-122', status: 'VERIFIED', title: 'Config hot-reload', projectId: 'proj-platform' },
};

function releasesFor(storyId: string): TraceabilityReleaseRow[] {
  if (storyId === 'STORY-118') {
    return [
      { releaseId: 'release-042', releaseVersion: '2026.04.17-0042', applicationId: 'app-order-svc', applicationName: 'Order Service', createdAt: '2026-04-17T08:00:00Z', state: 'DEPLOYED' },
      { releaseId: 'release-sso-055', releaseVersion: '2026.04.17-0055', applicationId: 'app-sso-portal', applicationName: 'SSO Portal', createdAt: '2026-04-17T07:00:00Z', state: 'DEPLOYED' },
    ];
  }
  if (storyId === 'STORY-100') {
    return [
      { releaseId: 'release-042', releaseVersion: '2026.04.17-0042', applicationId: 'app-order-svc', applicationName: 'Order Service', createdAt: '2026-04-17T08:00:00Z', state: 'DEPLOYED' },
    ];
  }
  if (storyId === 'STORY-116') {
    return [];
  }
  return [
    { releaseId: 'release-042', releaseVersion: '2026.04.17-0042', applicationId: 'app-order-svc', applicationName: 'Order Service', createdAt: '2026-04-17T08:00:00Z', state: 'DEPLOYED' },
  ];
}

function deploysFor(storyId: string): TraceabilityDeployGroup[] {
  if (storyId === 'STORY-116') return [];
  if (storyId === 'STORY-118') {
    return [
      { environmentName: 'prod', kind: 'PROD', deploys: [
        { deployId: 'deploy-succ-001', releaseVersion: '2026.04.17-0042', state: 'SUCCEEDED', startedAt: '2026-04-17T10:00:00Z', isCurrentRevision: true, isRollback: false },
        { deployId: 'deploy-sso-001', releaseVersion: '2026.04.17-0055', state: 'SUCCEEDED', startedAt: '2026-04-17T11:00:00Z', isCurrentRevision: true, isRollback: false },
      ] },
      { environmentName: 'staging', kind: 'STAGING', deploys: [
        { deployId: 'deploy-r-003', releaseVersion: '2026.04.17-0042', state: 'SUCCEEDED', startedAt: '2026-04-16T14:00:00Z', isCurrentRevision: false, isRollback: false },
      ] },
    ];
  }
  return [
    { environmentName: 'prod', kind: 'PROD', deploys: [
      { deployId: 'deploy-succ-001', releaseVersion: '2026.04.17-0042', state: 'SUCCEEDED', startedAt: '2026-04-17T10:00:00Z', isCurrentRevision: true, isRollback: false },
    ] },
    { environmentName: 'staging', kind: 'STAGING', deploys: [
      { deployId: 'deploy-r-003', releaseVersion: '2026.04.17-0042', state: 'SUCCEEDED', startedAt: '2026-04-16T14:00:00Z', isCurrentRevision: false, isRollback: false },
    ] },
    { environmentName: 'dev', kind: 'DEV', deploys: [
      { deployId: 'deploy-r-001', releaseVersion: '2026.04.17-0042', state: 'SUCCEEDED', startedAt: '2026-04-16T09:00:00Z', isCurrentRevision: false, isRollback: false },
    ] },
  ];
}

export function lookupStory(storyId: string): StoryChip {
  return STORY_REGISTRY[storyId] ?? { storyId, status: 'UNKNOWN_STORY' };
}

export function buildTraceability(storyId: string): TraceabilityAggregate {
  const chip = lookupStory(storyId);
  return {
    storyChip: chip,
    releases: { data: releasesFor(storyId), error: null },
    deploysByEnvironment: { data: deploysFor(storyId), error: null },
    upstreamAvailable: chip.status !== 'UNKNOWN_STORY',
  };
}

export const MOCK_STORY_IDS = Object.keys(STORY_REGISTRY);
