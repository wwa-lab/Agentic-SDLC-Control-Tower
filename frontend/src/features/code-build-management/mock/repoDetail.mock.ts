import type { BranchRow, RecentCommitRow, RepoAiSummary, RepoDetail, RepoHealthSummary, RepoHeader } from '../types';
import { createAiLineage } from '../utils';
import { aiPrReviewByPrId } from './aiPrReview.mock';
import { allCatalogRepoRows } from './catalog.mock';
import { prHeadersById } from './prDetail.mock';
import { runHeadersById } from './runDetail.mock';
import {
  traceabilityNoStoryIdRowsMock,
  traceabilityStoryRowsMock,
  traceabilityUnknownStoryRowsMock,
} from './traceability.mock';

const workspaceNames: Record<string, string> = {
  'ws-default-001': 'Global SDLC Tower',
  'ws-edge-002': 'Edge Release Lab',
};

const repoMap = Object.fromEntries(allCatalogRepoRows.map(row => [row.repoId, row]));

function createBranches(repoId: string): ReadonlyArray<BranchRow> {
  const repo = repoMap[repoId];
  return [
    {
      branch: repo.defaultBranch,
      isDefault: true,
      protected: true,
      headSha: `${repoId.replace('repo-', '')}aa11bb22cc33dd44ee55ff66778899aa00112233`,
      lastRunStatus: repo.defaultBranchStatus,
      updatedAt: repo.lastSyncedAt,
    },
    {
      branch: 'release/2026.04',
      isDefault: false,
      protected: true,
      headSha: `${repoId.replace('repo-', '')}bb11cc22dd33ee44ff55aa66778899bb00112233`,
      lastRunStatus: repo.defaultBranchStatus === 'SUCCESS' ? 'SUCCESS' : 'NEUTRAL',
      updatedAt: repo.lastSyncedAt,
    },
    {
      branch: 'feature/mock-observability',
      isDefault: false,
      protected: false,
      headSha: `${repoId.replace('repo-', '')}cc11dd22ee33ff44aa55bb66778899cc00112233`,
      lastRunStatus: 'NEUTRAL',
      updatedAt: repo.lastSyncedAt,
    },
  ];
}

function createRecentCommits(repoId: string): ReadonlyArray<RecentCommitRow> {
  const knownCommits = traceabilityStoryRowsMock
    .flatMap(row => row.commits)
    .filter(commit => commit.repoId === repoId)
    .map<RecentCommitRow>(commit => ({
      commitId: commit.commitId,
      sha: commit.sha,
      message: commit.message,
      author: commit.author,
      committedAt: commit.committedAt,
      storyLinkStatus: commit.storyLinkStatus,
      storyId: traceabilityStoryRowsMock.find(row => row.commits.some(item => item.commitId === commit.commitId))?.storyId ?? null,
      githubUrl: commit.githubUrl,
    }));

  const unknownCommits = traceabilityUnknownStoryRowsMock
    .filter(row => row.repoId === repoId)
    .map<RecentCommitRow>(row => ({
      commitId: row.commitId,
      sha: row.sha,
      message: row.message,
      author: 'Unknown author',
      committedAt: row.committedAt,
      storyLinkStatus: 'UNKNOWN_STORY',
      storyId: row.storyId,
      githubUrl: `https://github.com/${row.repoLabel.replace('wwa/', 'wwa/')}/commit/${row.sha}`,
    }));

  const noStoryCommits = traceabilityNoStoryIdRowsMock
    .filter(row => row.repoId === repoId)
    .map<RecentCommitRow>(row => ({
      commitId: row.commitId,
      sha: row.sha,
      message: row.message,
      author: row.author,
      committedAt: row.committedAt,
      storyLinkStatus: 'NO_STORY_ID',
      storyId: null,
      githubUrl: row.githubUrl,
    }));

  const combined = [...knownCommits, ...unknownCommits, ...noStoryCommits];
  if (combined.length > 0) {
    return combined.slice(0, 5);
  }

  return [
    {
      commitId: `${repoId}-commit-1`,
      sha: `${repoId.replace('repo-', '')}ddeeff00112233445566778899aabbccddeeff00`,
      message: 'chore: refresh repository seeds',
      author: 'System Seed',
      committedAt: repoMap[repoId].lastSyncedAt,
      storyLinkStatus: 'NO_STORY_ID',
      storyId: null,
      githubUrl: `${repoMap[repoId].githubUrl}/commit/${repoId.replace('repo-', '')}ddeeff00112233445566778899aabbccddeeff00`,
    },
  ];
}

function createHealthSummary(repoId: string): RepoHealthSummary {
  const repo = repoMap[repoId];
  const successRuns = repo.sparkline.filter(point => point.status === 'SUCCESS').length;
  const activeRuns = repo.sparkline.filter(point => point.status !== 'NEUTRAL').length || 1;
  return {
    successRate: Number(((successRuns / activeRuns) * 100).toFixed(1)),
    medianDurationSeconds: repoId === 'repo-5' ? 498 : repoId === 'repo-9' ? 610 : 372,
    failingWorkflows: repoId === 'repo-5'
      ? [
          { workflowName: 'release-matrix', failures: 5 },
          { workflowName: 'contract-regression', failures: 3 },
          { workflowName: 'cache-warm', failures: 2 },
        ]
      : [
          { workflowName: 'verify-pull-request', failures: repo.defaultBranchStatus === 'FAILURE' ? 2 : 1 },
          { workflowName: 'main', failures: 1 },
        ],
    last14Runs: repo.sparkline,
    advisory: repoId === 'repo-9'
      ? 'Repository is stale. The last sync is over 30 days old.'
      : repoId === 'repo-5'
        ? 'Flaky workflow concentration is clustered in the release matrix.'
        : 'Default branch health is stable over the trailing 14 runs.',
  };
}

function createAiSummary(repoId: string): RepoAiSummary {
  const generatedAt = repoId === 'repo-9' ? '2026-03-12T10:30:00Z' : '2026-04-18T04:18:00Z';
  const skillVersion = 'cb-repo-summary@1.3.0';
  return {
    summary: repoId === 'repo-5'
      ? 'Checkout CI is losing time in the release matrix and should collapse duplicate smoke lanes.'
      : repoId === 'repo-9'
        ? 'Spec archive has gone quiet and should be verified against current ownership.'
        : 'Repository trend is healthy with no new systemic regressions over the last 14 days.',
    deltaSummary: repoId === 'repo-5'
      ? 'Failure density is up 11% versus the prior window.'
      : repoId === 'repo-9'
        ? 'No activity change detected because the repo has been idle for 37 days.'
        : 'Success rate improved by 6% versus the prior 14-day window.',
    generatedAt,
    skillVersion,
    lineage: createAiLineage(skillVersion, generatedAt)!,
  };
}

export const repoDetailsById: Record<string, RepoDetail> = Object.fromEntries(
  allCatalogRepoRows.map(row => {
    const header: RepoHeader = {
      repoId: row.repoId,
      owner: row.owner,
      name: row.name,
      visibility: row.visibility,
      defaultBranch: row.defaultBranch,
      workspaceId: row.workspaceId,
      workspaceName: workspaceNames[row.workspaceId],
      projectId: row.projectId,
      projectName: row.projectName,
      lastSyncedAt: row.lastSyncedAt,
      defaultBranchProtection: row.defaultBranch === 'main' || row.defaultBranch.startsWith('release/')
        ? 'Protected'
        : 'Standard',
      lastActivityAt: row.lastActivityAt,
      githubUrl: row.githubUrl,
    };

    const recentRuns = Object.values(runHeadersById)
      .filter(run => run.repoId === row.repoId)
      .map(run => ({
        runId: run.runId,
        runNumber: run.runNumber,
        workflowName: run.workflowName,
        status: run.status,
        branch: run.branch,
        sha: run.sha,
        trigger: run.trigger,
        durationSeconds: run.durationSeconds,
        actor: run.actor,
        createdAt: run.createdAt,
        updatedAt: run.updatedAt,
        githubUrl: run.githubUrl,
      }));

    const recentPrs = Object.values(prHeadersById)
      .filter(pr => pr.repoId === row.repoId)
      .map(pr => ({
        prId: pr.prId,
        number: pr.number,
        title: pr.title,
        state: pr.state,
        author: pr.author,
        baseBranch: pr.baseBranch,
        headBranch: pr.headBranch,
        updatedAt: pr.updatedAt,
        aiCounts: aiPrReviewByPrId[pr.prId]?.noteCounts ?? { blocker: 0, major: 0, minor: 0, info: 0 },
        githubUrl: pr.githubUrl,
      }));

    return [
      row.repoId,
      {
        header,
        recentRuns,
        recentPrs,
        recentCommits: createRecentCommits(row.repoId),
        branches: createBranches(row.repoId),
        healthSummary: createHealthSummary(row.repoId),
        aiSummary: createAiSummary(row.repoId),
      },
    ];
  }),
);

