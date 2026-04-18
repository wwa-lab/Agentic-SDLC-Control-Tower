import type { ReleaseDetailAggregate, ReleaseHeader, ReleaseCommitRow, ReleaseDeployRow, AiReleaseNotes } from '../types/release';
import type { StoryChip } from '../types/traceability';

const commits = (count: number): ReleaseCommitRow[] =>
  Array.from({ length: count }, (_, i) => ({
    sha: `abc${String(i).padStart(4, '0')}def1234567890abcdef1234567890abcdef`,
    shortSha: `abc${String(i).padStart(4, '0')}de`,
    author: ['alice', 'bob', 'carol', 'dave'][i % 4],
    message: [`fix: resolve cart total rounding`, `feat: add bulk discount logic`, `chore: update deps`, `refactor: extract payment validator`][i % 4],
    committedAt: new Date(Date.UTC(2026, 3, 17, 10 - i)).toISOString(),
    storyIds: i % 2 === 0 ? [`STORY-${100 + i}`] : [],
  }));

const stories = (count: number): StoryChip[] =>
  Array.from({ length: count }, (_, i) => ({
    storyId: `STORY-${100 + i * 2}`,
    status: i < count - 1 ? 'VERIFIED' as const : 'UNVERIFIED' as const,
    title: [`Fix cart rounding`, `Add bulk discounts`, `Improve checkout flow`, `Audit logging`][i % 4],
    projectId: 'proj-commerce',
  }));

const deployRows = (releaseVersion: string): ReleaseDeployRow[] => [
  { deployId: 'deploy-r-001', environmentName: 'dev', state: 'SUCCEEDED', startedAt: '2026-04-16T09:00:00Z', durationSec: 90, isCurrentRevision: false, isRollback: false },
  { deployId: 'deploy-r-002', environmentName: 'qa', state: 'SUCCEEDED', startedAt: '2026-04-16T11:00:00Z', durationSec: 120, isCurrentRevision: false, isRollback: false },
  { deployId: 'deploy-r-003', environmentName: 'staging', state: 'SUCCEEDED', startedAt: '2026-04-16T14:00:00Z', durationSec: 180, isCurrentRevision: false, isRollback: false },
  { deployId: 'deploy-r-004', environmentName: 'prod', state: 'SUCCEEDED', startedAt: '2026-04-17T10:00:00Z', durationSec: 240, approverDisplayName: 'Jane PM', isCurrentRevision: true, isRollback: false },
];

const DEPLOYED_HEADER: ReleaseHeader = {
  releaseId: 'release-042',
  releaseVersion: '2026.04.17-0042',
  applicationId: 'app-order-svc',
  workspaceId: 'ws-default-001',
  state: 'DEPLOYED',
  createdAt: '2026-04-17T08:00:00Z',
  createdBy: 'jenkins-bot',
  buildArtifactRef: { sliceId: 'code-build', buildArtifactId: 'ba-042' },
  buildArtifactResolved: true,
  buildArtifactSha: 'abc0000def1234567890abcdef1234567890abcdef',
  jenkinsSourceUrl: 'https://jenkins.example.com/job/commerce/job/order-service/42/',
};

const NOTES_SUCCESS: AiReleaseNotes = {
  status: 'SUCCESS',
  keyedOnReleaseId: 'release-042',
  skillVersion: 'release-notes-v3',
  generatedAt: '2026-04-17T09:00:00Z',
  narrative: '## Release 2026.04.17-0042\n\nThis release includes **3 bug fixes** and **1 new feature**:\n\n- Fixed cart total rounding error that caused 1-cent discrepancies\n- Added bulk discount logic for orders over 50 items\n- Updated third-party payment SDK to v4.2.1\n- Extracted payment validator for better testability',
  diffNarrative: 'Compared to the previous prod release (2026.04.15-0038), this release adds 8 commits across 4 stories.',
  riskHint: 'LOW',
  evidence: [
    { kind: 'commit', id: 'abc0000de', label: 'fix: resolve cart total rounding' },
    { kind: 'story', id: 'STORY-100', label: 'Fix cart rounding' },
  ],
};

export const MOCK_DEPLOYED_RELEASE: ReleaseDetailAggregate = {
  header: { data: DEPLOYED_HEADER, error: null },
  commits: { data: commits(8), error: null },
  linkedStories: { data: stories(4), error: null },
  deploys: { data: deployRows('2026.04.17-0042'), error: null },
  aiNotes: { data: NOTES_SUCCESS, error: null },
};

export const MOCK_EVIDENCE_MISMATCH_RELEASE: ReleaseDetailAggregate = {
  header: { data: { ...DEPLOYED_HEADER, releaseId: 'release-041', releaseVersion: '2026.04.17-0041' }, error: null },
  commits: { data: commits(5), error: null },
  linkedStories: { data: stories(2), error: null },
  deploys: { data: deployRows('2026.04.17-0041').slice(0, 2), error: null },
  aiNotes: { data: {
    status: 'EVIDENCE_MISMATCH',
    keyedOnReleaseId: 'release-041',
    skillVersion: 'release-notes-v3',
    generatedAt: '2026-04-17T08:30:00Z',
    narrative: '## Release 2026.04.17-0041\n\nNote: evidence mismatch detected — one cited commit does not exist in the release.',
    evidence: [
      { kind: 'commit', id: 'FABRICATED_SHA', label: 'does not exist' },
    ],
    error: { code: 'DP_RELEASE_NOTES_EVIDENCE_MISMATCH', message: 'Evidence references a commit not present in this release' },
  }, error: null },
};

export const MOCK_PREPARED_RELEASE: ReleaseDetailAggregate = {
  header: { data: { ...DEPLOYED_HEADER, releaseId: 'release-043', releaseVersion: '2026.04.18-0043', state: 'PREPARED', createdAt: '2026-04-18T06:00:00Z' }, error: null },
  commits: { data: commits(3), error: null },
  linkedStories: { data: stories(1), error: null },
  deploys: { data: [], error: null },
  aiNotes: { data: { status: 'PENDING', keyedOnReleaseId: 'release-043' }, error: null },
};

export const MOCK_SUPERSEDED_RELEASE: ReleaseDetailAggregate = {
  header: { data: { ...DEPLOYED_HEADER, releaseId: 'release-038', releaseVersion: '2026.04.15-0038', state: 'SUPERSEDED' }, error: null },
  commits: { data: commits(6), error: null },
  linkedStories: { data: stories(3), error: null },
  deploys: { data: deployRows('2026.04.15-0038'), error: null },
  aiNotes: { data: { ...NOTES_SUCCESS, keyedOnReleaseId: 'release-038', status: 'STALE' }, error: null },
};

export const MOCK_RELEASE_MAP: Record<string, ReleaseDetailAggregate> = {
  'release-042': MOCK_DEPLOYED_RELEASE,
  'release-041': MOCK_EVIDENCE_MISMATCH_RELEASE,
  'release-043': MOCK_PREPARED_RELEASE,
  'release-038': MOCK_SUPERSEDED_RELEASE,
};
