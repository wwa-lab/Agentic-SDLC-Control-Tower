import type { SectionResult } from '@/shared/types/section';
import type {
  CatalogAggregate,
  CatalogCardKey,
  CatalogFilter,
  CatalogFilterOptions,
  CodeBuildViewerContext,
  PrCardKey,
  PrDetailAggregate,
  RepoCardKey,
  RepoDetailAggregate,
  RegenerateAiPrReviewRequest,
  RegenerateAiPrReviewResponse,
  RegenerateAiTriageRequest,
  RegenerateAiTriageResponse,
  RepoDetail,
  RerunRunRequest,
  RerunRunResponse,
  RunCardKey,
  RunDetailAggregate,
  TraceabilityAggregate,
  TraceabilityCardKey,
  TraceabilityFilter,
  AiTriagePayload,
} from '../types';
import { CodeBuildApiError } from '../errors';
import {
  autonomyMeetsMinimum,
  canManageRole,
  createAiLineage,
  filterAiReviewForRole,
  isNonTerminalStatus,
} from '../utils';
import { buildCatalogSummary } from './catalogSummary.mock';
import { getMockState, resetMockState } from './state';

const NOW = Date.parse('2026-04-18T04:30:00Z');

function section<T>(data: T | null, error: string | null = null): SectionResult<T> {
  return { data, error };
}

function repoHealthScore(status: string): number {
  switch (status) {
    case 'FAILURE':
      return 5;
    case 'CANCELLED':
      return 4;
    case 'RUNNING':
      return 3;
    case 'QUEUED':
      return 2;
    case 'NEUTRAL':
      return 1;
    default:
      return 0;
  }
}

function bumpClock() {
  const state = getMockState();
  state.counters.clock += 1;
  return new Date(NOW + state.counters.clock * 60_000).toISOString();
}

function nextSkillVersion(prefix: string) {
  const state = getMockState();
  state.counters[prefix.includes('triage') ? 'triage' : 'prReview'] += 1;
  const counter = prefix.includes('triage') ? state.counters.triage : state.counters.prReview;
  return `${prefix}@2.1.${counter}`;
}

function nextRerunId() {
  const state = getMockState();
  state.counters.rerun += 1;
  return `run-${state.counters.rerun}`;
}

function extractForcedCode(reason?: string) {
  if (!reason) {
    return null;
  }
  const supported = [
    'CB_AI_UNAVAILABLE',
    'CB_GH_RATE_LIMIT',
    'CB_TRIAGE_EVIDENCE_MISMATCH',
    'CB_AI_AUTONOMY_INSUFFICIENT',
    'CB_ROLE_REQUIRED',
    'CB_WORKSPACE_FORBIDDEN',
    'CB_WEBHOOK_SIGNATURE_INVALID',
    'CB_STALE_HEAD_SHA',
  ];
  return supported.find(code => reason.includes(code)) ?? null;
}

function assertWorkspace(workspaceId: string, context: CodeBuildViewerContext) {
  if (workspaceId !== context.workspaceId) {
    throw new CodeBuildApiError(403, 'CB_WORKSPACE_FORBIDDEN', 'This entity belongs to another workspace.');
  }
}

function filterCatalogSections(context: CodeBuildViewerContext, filters: CatalogFilter) {
  const state = getMockState();
  const sections = state.catalogSections
    .filter((entry: any) => entry.workspaceId === context.workspaceId)
    .map((entry: any) => ({
      ...entry,
      repos: [...entry.repos].filter((repo: any) => {
        if (filters.projectId !== 'ALL' && repo.projectId !== filters.projectId) {
          return false;
        }
        if (filters.visibility !== 'ALL' && repo.visibility !== filters.visibility) {
          return false;
        }
        if (filters.buildStatus !== 'ALL') {
          if (filters.buildStatus === 'NO_RUNS') {
            if (repo.hasRuns) {
              return false;
            }
          } else if (repo.defaultBranchStatus !== filters.buildStatus) {
            return false;
          }
        }
        if (filters.search.trim()) {
          const search = filters.search.trim().toLowerCase();
          const haystack = `${repo.owner}/${repo.name} ${repo.projectName}`.toLowerCase();
          if (!haystack.includes(search)) {
            return false;
          }
        }
        return true;
      }),
    }))
    .filter((entry: any) => entry.repos.length > 0)
    .map((entry: any) => ({
      ...entry,
      repos: entry.repos.sort((left: any, right: any) => {
        switch (filters.sort) {
          case 'ALPHABETICAL':
            return `${left.owner}/${left.name}`.localeCompare(`${right.owner}/${right.name}`);
          case 'HEALTH_WORST_FIRST':
            return repoHealthScore(right.defaultBranchStatus) - repoHealthScore(left.defaultBranchStatus)
              || `${left.owner}/${left.name}`.localeCompare(`${right.owner}/${right.name}`);
          default:
            return Date.parse(right.lastActivityAt ?? right.lastSyncedAt) - Date.parse(left.lastActivityAt ?? left.lastSyncedAt);
        }
      }),
    }));

  return sections;
}

function buildCatalogFilterOptions(context: CodeBuildViewerContext, filters: CatalogFilter): CatalogFilterOptions {
  const state = getMockState();
  const projects = state.catalogSections
    .filter((entry: any) => entry.workspaceId === context.workspaceId)
    .map((entry: any) => ({ id: entry.projectId, name: entry.projectName }));

  return {
    projects,
    statuses: ['ALL', 'SUCCESS', 'FAILURE', 'RUNNING', 'QUEUED', 'CANCELLED', 'NEUTRAL', 'NO_RUNS'],
    visibilities: ['ALL', 'PRIVATE', 'PUBLIC'],
    sorts: ['RECENT_ACTIVITY', 'ALPHABETICAL', 'HEALTH_WORST_FIRST'],
    active: filters,
  };
}

function getRepoDetail(repoId: string, context: CodeBuildViewerContext): RepoDetail {
  const detail = getMockState().repoDetails[repoId];
  if (!detail) {
    throw new CodeBuildApiError(404, 'CB_REPO_NOT_FOUND', 'Repository could not be found.');
  }
  assertWorkspace(detail.header.workspaceId, context);
  return detail;
}

function getPrDetail(prId: string, context: CodeBuildViewerContext) {
  const detail = getMockState().prDetails[prId];
  if (!detail) {
    throw new CodeBuildApiError(404, 'CB_PR_NOT_FOUND', 'Pull request could not be found.');
  }
  assertWorkspace(detail.header.workspaceId, context);
  return detail;
}

function getRunDetail(runId: string, context: CodeBuildViewerContext) {
  const detail = getMockState().runDetails[runId];
  if (!detail) {
    throw new CodeBuildApiError(404, 'CB_RUN_NOT_FOUND', 'Run could not be found.');
  }
  assertWorkspace(detail.header.workspaceId, context);
  return detail;
}

function projectWorkspaceMap() {
  const state = getMockState();
  return Object.fromEntries(
    state.catalogSections.map((entry: any) => [entry.projectId, entry.workspaceId]),
  ) as Record<string, string>;
}

function repoProjectMap() {
  const state = getMockState();
  return Object.fromEntries(
    Object.values(state.repoDetails).map((detail: any) => [detail.header.repoId, detail.header.projectId]),
  ) as Record<string, string>;
}

function traceabilityWithinRange(committedAt: string, rangeDays: number) {
  return NOW - Date.parse(committedAt) <= rangeDays * 24 * 60 * 60 * 1000;
}

function filterTraceability(context: CodeBuildViewerContext, filters: TraceabilityFilter) {
  const state = getMockState();
  const projectToWorkspace = projectWorkspaceMap();
  const repoToProject = repoProjectMap();

  const storyRows = state.traceabilityStoryRows
    .filter((row: any) => projectToWorkspace[row.projectId] === context.workspaceId)
    .filter((row: any) => filters.projectId === 'ALL' || row.projectId === filters.projectId)
    .filter((row: any) => filters.storyState === 'ALL' || row.state === filters.storyState)
    .filter((row: any) => filters.linkStatus === 'ALL' || row.worstStatus === filters.linkStatus)
    .filter((row: any) => row.commits.some((commit: any) => traceabilityWithinRange(commit.committedAt, filters.rangeDays)));

  const unknownStory = state.traceabilityUnknownStoryRows
    .filter((row: any) => projectToWorkspace[repoToProject[row.repoId]] === context.workspaceId)
    .filter((row: any) => filters.projectId === 'ALL' || repoToProject[row.repoId] === filters.projectId)
    .filter((row: any) => filters.linkStatus === 'ALL' || filters.linkStatus === 'UNKNOWN_STORY')
    .filter((row: any) => row.ageDays <= filters.rangeDays);

  const noStoryId = state.traceabilityNoStoryIdRows
    .filter((row: any) => projectToWorkspace[repoToProject[row.repoId]] === context.workspaceId)
    .filter((row: any) => filters.projectId === 'ALL' || repoToProject[row.repoId] === filters.projectId)
    .filter((row: any) => filters.linkStatus === 'ALL' || filters.linkStatus === 'NO_STORY_ID')
    .filter((row: any) => traceabilityWithinRange(row.committedAt, filters.rangeDays));

  return {
    storyRows,
    unknownStory,
    noStoryId,
  };
}

function buildTraceabilitySummary(filtered: ReturnType<typeof filterTraceability>) {
  return {
    known: filtered.storyRows.filter((row: any) => row.worstStatus === 'KNOWN').length,
    unknownStory: filtered.unknownStory.length,
    noStoryId: filtered.noStoryId.length,
    ambiguous: filtered.storyRows.filter((row: any) => row.worstStatus === 'AMBIGUOUS').length,
    advisory: 'Unknown stories remain visible so requirement owners can repair the link rather than losing the evidence trail.',
  };
}

function maybeRandom(probability: number) {
  return Math.random() < probability;
}

function createFallbackPrReview(headSha: string) {
  const generatedAt = bumpClock();
  const skillVersion = nextSkillVersion('cb-pr-review');
  return {
    status: 'SUCCESS' as const,
    summary: 'AI review regenerated for the current head.',
    headSha,
    noteCounts: { blocker: 0, major: 1, minor: 2, info: 1 },
    notes: [
      {
        id: `regen-pr-${generatedAt}`,
        severity: 'MAJOR' as const,
        title: 'Verify the refreshed card state',
        body: 'The regenerated review suggests a quick UI pass to confirm the refreshed data matches the current head.',
        filePath: 'frontend/src/features/code-build-management/views/PrDetailView.vue',
        line: 1,
        evidence: 'mock regeneration',
      },
      {
        id: `regen-pr-minor-${generatedAt}`,
        severity: 'MINOR' as const,
        title: 'Tooltip copy remains consistent',
        body: 'No regressions found in the traceability tooltip copy.',
        filePath: 'frontend/src/features/code-build-management/components/StoryLinkStatusChip.vue',
        line: 1,
        evidence: 'mock regeneration',
      },
      {
        id: `regen-pr-info-${generatedAt}`,
        severity: 'INFO' as const,
        title: 'Skill run completed',
        body: 'Mock AI review completed successfully.',
        filePath: null,
        line: null,
        evidence: 'mock regeneration',
      },
    ],
    generatedAt,
    skillVersion,
    lineage: createAiLineage(skillVersion, generatedAt),
    failureCode: null,
    stale: false,
  };
}

function createFallbackTriage(runId: string, jobId: string, stepId: string): AiTriagePayload {
  const generatedAt = bumpClock();
  const skillVersion = nextSkillVersion('cb-triage');
  return {
    status: 'SUCCESS' as const,
    summary: 'Triage regenerated with the currently available step evidence.',
    rows: [
      {
        id: `regen-triage-${generatedAt}`,
        status: 'SUCCESS' as const,
        title: 'Re-run focused on the failing step',
        summary: 'The regenerated triage recommends checking the UI state reset before re-running the suite.',
        probableCause: 'State reset during route transition.',
        evidence: {
          runId,
          jobId,
          stepId,
        },
        confidence: 0.84,
        retryable: false,
        errorCode: null,
        generatedAt,
      },
    ],
    generatedAt,
    skillVersion,
    lineage: createAiLineage(skillVersion, generatedAt),
  };
}

export async function readCatalogAggregate(context: CodeBuildViewerContext, filters: CatalogFilter): Promise<CatalogAggregate> {
  const sections = filterCatalogSections(context, filters);
  const rows = sections.flatMap((entry: any) => entry.repos);
  return {
    summary: section(buildCatalogSummary(rows, context.workspaceId)),
    filters: section(buildCatalogFilterOptions(context, filters)),
    grid: section(sections),
  };
}

export async function readCatalogSection(cardKey: CatalogCardKey, context: CodeBuildViewerContext, filters: CatalogFilter) {
  const aggregate = await readCatalogAggregate(context, filters);
  return aggregate[cardKey];
}

export async function readRepoAggregate(repoId: string, context: CodeBuildViewerContext): Promise<RepoDetailAggregate> {
  const detail = getRepoDetail(repoId, context);
  return {
    header: section(detail.header),
    recentRuns: section(detail.recentRuns),
    recentPrs: section(detail.recentPrs),
    recentCommits: section(detail.recentCommits),
    branches: section(detail.branches),
    healthSummary: section(detail.healthSummary),
    aiSummary: section(detail.aiSummary),
  };
}

export async function readRepoSection(cardKey: RepoCardKey, repoId: string, context: CodeBuildViewerContext) {
  const aggregate = await readRepoAggregate(repoId, context);
  return aggregate[cardKey];
}

export async function readPrAggregate(prId: string, context: CodeBuildViewerContext): Promise<PrDetailAggregate> {
  const detail = getPrDetail(prId, context);
  return {
    header: section(detail.header),
    checks: section(detail.checks),
    reviews: section(detail.reviews),
    commits: section(detail.commits),
    aiReview: section(filterAiReviewForRole(detail.aiReview, context.role)),
  };
}

export async function readPrSection(cardKey: PrCardKey, prId: string, context: CodeBuildViewerContext) {
  const aggregate = await readPrAggregate(prId, context);
  return aggregate[cardKey];
}

export async function readRunAggregate(runId: string, context: CodeBuildViewerContext): Promise<RunDetailAggregate> {
  const detail = getRunDetail(runId, context);
  return {
    header: section(detail.header),
    jobs: section(detail.jobs),
    steps: section(detail.steps),
    logs: section(detail.logs),
    aiTriage: section(detail.aiTriage),
    rerun: section(detail.rerun),
  };
}

export async function readRunSection(cardKey: RunCardKey, runId: string, context: CodeBuildViewerContext) {
  const aggregate = await readRunAggregate(runId, context);
  return aggregate[cardKey];
}

export async function readTraceabilityAggregate(
  context: CodeBuildViewerContext,
  filters: TraceabilityFilter,
): Promise<TraceabilityAggregate> {
  const filtered = filterTraceability(context, filters);
  return {
    summary: section(buildTraceabilitySummary(filtered)),
    storyRows: section(filtered.storyRows),
    unknownStory: section(filtered.unknownStory),
    noStoryId: section(filtered.noStoryId),
  };
}

export async function readTraceabilitySection(
  cardKey: TraceabilityCardKey,
  context: CodeBuildViewerContext,
  filters: TraceabilityFilter,
) {
  const aggregate = await readTraceabilityAggregate(context, filters);
  return aggregate[cardKey];
}

export async function regenerateAiPrReview(
  prId: string,
  request: RegenerateAiPrReviewRequest,
  context: CodeBuildViewerContext,
): Promise<RegenerateAiPrReviewResponse> {
  const detail = getPrDetail(prId, context);
  const forced = extractForcedCode(request.reason);

  if (forced === 'CB_WORKSPACE_FORBIDDEN') {
    throw new CodeBuildApiError(403, 'CB_WORKSPACE_FORBIDDEN', 'This entity belongs to another workspace.');
  }
  if (!canManageRole(context.role) || forced === 'CB_ROLE_REQUIRED') {
    throw new CodeBuildApiError(403, 'CB_ROLE_REQUIRED', 'Only PM and Tech Lead can regenerate AI reviews.');
  }
  if (!autonomyMeetsMinimum(context.autonomyLevel, 'SUPERVISED') || forced === 'CB_AI_AUTONOMY_INSUFFICIENT') {
    throw new CodeBuildApiError(409, 'CB_AI_AUTONOMY_INSUFFICIENT', 'Workspace autonomy must be SUPERVISED or above.');
  }
  if (request.prevHeadSha !== detail.header.headSha || forced === 'CB_STALE_HEAD_SHA') {
    throw new CodeBuildApiError(409, 'CB_STALE_HEAD_SHA', 'The PR head advanced. Refresh and try again.');
  }
  if (forced === 'CB_AI_UNAVAILABLE' || maybeRandom(0.05)) {
    throw new CodeBuildApiError(503, 'CB_AI_UNAVAILABLE', 'AI review service is temporarily unavailable.');
  }

  const nextPayload = detail.aiReview.status === 'SUCCESS'
    ? {
        ...detail.aiReview,
        generatedAt: bumpClock(),
        skillVersion: nextSkillVersion('cb-pr-review'),
        lineage: null,
        failureCode: null,
        stale: false,
      }
    : createFallbackPrReview(detail.header.headSha);

  const resolvedPayload = {
    ...nextPayload,
    lineage: createAiLineage(nextPayload.skillVersion, nextPayload.generatedAt),
    failureCode: null,
    stale: false,
  };

  getMockState().prDetails[prId].aiReview = resolvedPayload;
  getMockState().repoDetails[detail.header.repoId].recentPrs = getMockState().repoDetails[detail.header.repoId].recentPrs.map((row: any) => (
    row.prId === prId ? { ...row, aiCounts: resolvedPayload.noteCounts } : row
  ));

  return {
    aiReview: section(filterAiReviewForRole(resolvedPayload, context.role)),
    message: 'AI PR review regenerated.',
    errorCode: null,
  };
}

export async function regenerateAiTriage(
  runId: string,
  request: RegenerateAiTriageRequest,
  context: CodeBuildViewerContext,
): Promise<RegenerateAiTriageResponse> {
  const detail = getRunDetail(runId, context);
  const forced = extractForcedCode(request.reason);

  if (forced === 'CB_WORKSPACE_FORBIDDEN') {
    throw new CodeBuildApiError(403, 'CB_WORKSPACE_FORBIDDEN', 'This entity belongs to another workspace.');
  }
  if (!canManageRole(context.role) || forced === 'CB_ROLE_REQUIRED') {
    throw new CodeBuildApiError(403, 'CB_ROLE_REQUIRED', 'Only PM and Tech Lead can regenerate AI triage.');
  }
  if (!autonomyMeetsMinimum(context.autonomyLevel, 'SUPERVISED') || forced === 'CB_AI_AUTONOMY_INSUFFICIENT') {
    throw new CodeBuildApiError(409, 'CB_AI_AUTONOMY_INSUFFICIENT', 'Workspace autonomy must be SUPERVISED or above.');
  }
  if (forced === 'CB_AI_UNAVAILABLE' || maybeRandom(0.05)) {
    throw new CodeBuildApiError(503, 'CB_AI_UNAVAILABLE', 'AI triage service is temporarily unavailable.');
  }

  const firstJobId = detail.steps[0]?.jobId ?? 'job-unknown';
  const firstStepId = request.stepIds?.[0] ?? detail.steps[0]?.steps[0]?.stepId ?? 'step-unknown';
  let nextPayload = createFallbackTriage(runId, firstJobId, firstStepId);
  let errorCode: string | null = null;

  if (forced === 'CB_TRIAGE_EVIDENCE_MISMATCH' || maybeRandom(0.03)) {
    nextPayload = {
      ...nextPayload,
      rows: [
      ...nextPayload.rows,
      {
        id: `regen-triage-mismatch-${bumpClock()}`,
        status: 'FAILED_EVIDENCE',
        title: 'Stale evidence row quarantined',
        summary: 'One AI row cited a non-existent step and was downgraded instead of blocking the rest of the triage set.',
        probableCause: 'Head moved between prompt capture and response synthesis.',
        evidence: {
          runId,
          jobId: detail.jobs[0]?.jobId ?? 'job-unknown',
          stepId: 'step-missing-evidence',
        },
        confidence: 0.19,
        retryable: true,
        errorCode: 'CB_TRIAGE_EVIDENCE_MISMATCH',
        generatedAt: bumpClock(),
      },
      ],
      summary: 'Triage regenerated with one quarantined evidence row.',
    };
    errorCode = 'CB_TRIAGE_EVIDENCE_MISMATCH';
  }

  getMockState().runDetails[runId].aiTriage = nextPayload;

  return {
    aiTriage: section(nextPayload),
    message: errorCode
      ? 'AI triage regenerated with one quarantined evidence row.'
      : 'AI triage regenerated.',
    errorCode,
  };
}

export async function rerunRun(
  runId: string,
  request: RerunRunRequest,
  context: CodeBuildViewerContext,
): Promise<RerunRunResponse> {
  const detail = getRunDetail(runId, context);
  const forced = extractForcedCode(request.reason);

  if (forced === 'CB_WORKSPACE_FORBIDDEN') {
    throw new CodeBuildApiError(403, 'CB_WORKSPACE_FORBIDDEN', 'This entity belongs to another workspace.');
  }
  if (!canManageRole(context.role) || forced === 'CB_ROLE_REQUIRED') {
    throw new CodeBuildApiError(403, 'CB_ROLE_REQUIRED', 'Only PM and Tech Lead can rerun workflows.');
  }
  if (forced === 'CB_GH_RATE_LIMIT' || maybeRandom(0.02)) {
    const resetAt = new Date(NOW + 15 * 60 * 1000).toISOString();
    throw new CodeBuildApiError(429, 'CB_GH_RATE_LIMIT', 'GitHub Actions rerun rate limit reached.', {
      resetAt,
    });
  }

  const requestedRunId = nextRerunId();
  const attemptedAt = bumpClock();
  const rerunState = {
    canRerun: true,
    lastAttemptAt: attemptedAt,
    lastResultCode: null,
    lastResultMessage: `Workflow rerun requested as ${requestedRunId}.`,
    rateLimitResetAt: null,
    requestedRunId,
  };

  getMockState().runDetails[runId].rerun = rerunState;
  getMockState().runDetails[requestedRunId] = {
    ...detail,
    header: {
      ...detail.header,
      runId: requestedRunId,
      runNumber: Number(requestedRunId.replace('run-', '')),
      status: 'QUEUED',
      trigger: 'RERUN',
      createdAt: attemptedAt,
      updatedAt: attemptedAt,
      durationSeconds: null,
      githubUrl: detail.header.githubUrl.replace(`/runs/${detail.header.runNumber}`, `/runs/${requestedRunId.replace('run-', '')}`),
    },
    jobs: [],
    steps: [],
    logs: null,
    aiTriage: {
      status: 'PENDING',
      summary: 'Triage is pending for the queued rerun.',
      rows: [],
      generatedAt: null,
      skillVersion: null,
      lineage: null,
    },
    rerun: {
      canRerun: true,
      lastAttemptAt: null,
      lastResultCode: null,
      lastResultMessage: null,
      rateLimitResetAt: null,
      requestedRunId: null,
    },
  };

  const repoId = detail.header.repoId;
  const repo = getMockState().repoDetails[repoId];
  repo.recentRuns = [
    {
      runId: requestedRunId,
      runNumber: Number(requestedRunId.replace('run-', '')),
      workflowName: detail.header.workflowName,
      status: 'QUEUED',
      branch: detail.header.branch,
      sha: detail.header.sha,
      trigger: 'RERUN',
      durationSeconds: null,
      actor: context.role,
      createdAt: attemptedAt,
      updatedAt: attemptedAt,
      githubUrl: getMockState().runDetails[requestedRunId].header.githubUrl,
    },
    ...repo.recentRuns,
  ].slice(0, 6);

  const catalogRepo = getMockState().catalogSections
    .flatMap((entry: any) => entry.repos)
    .find((row: any) => row.repoId === repoId);
  if (catalogRepo && isNonTerminalStatus('QUEUED')) {
    catalogRepo.defaultBranchStatus = detail.header.branch === catalogRepo.defaultBranch ? 'QUEUED' : catalogRepo.defaultBranchStatus;
    catalogRepo.lastActivityAt = attemptedAt;
    catalogRepo.lastSyncedAt = attemptedAt;
    catalogRepo.sparkline = [{ runId: requestedRunId, status: 'QUEUED', label: 'workflow rerun queued' }, ...catalogRepo.sparkline].slice(0, 14);
  }

  return {
    rerun: section(rerunState),
    message: `Workflow rerun requested as ${requestedRunId}.`,
    errorCode: null,
  };
}

export async function simulateWebhookRoundTrip(fixtureName: string) {
  if (fixtureName === '__SIGNATURE_TRIGGER__') {
    throw new CodeBuildApiError(401, 'CB_WEBHOOK_SIGNATURE_INVALID', 'Webhook signature validation failed.');
  }
  return {
    deliveryId: `delivery-${fixtureName}`,
    eventType: fixtureName,
    acceptedAt: bumpClock(),
  };
}

export { resetMockState };
