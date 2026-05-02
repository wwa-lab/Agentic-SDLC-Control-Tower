import { describe, it, expect } from 'vitest';
import { queueForStatus, summarizeTraceability } from '../utils/controlPlane';
import type { RequirementTraceability } from '../types/requirement';

// ── helpers ──────────────────────────────────────────────────────────────────

function makeTrace(overrides: Partial<RequirementTraceability> = {}): RequirementTraceability {
  return {
    requirementId: 'REQ-001',
    sources: [],
    documents: { requirementId: 'REQ-001', profileId: 'standard-sdd', workspace: null, stages: [] },
    reviews: [],
    agentRuns: [],
    artifactLinks: [],
    freshness: [],
    ...overrides,
  };
}

function makeStage(missing: boolean) {
  return {
    id: missing ? null : 'DOC-1',
    sddType: 'spec',
    stageLabel: 'Spec',
    title: 'Spec',
    repoFullName: null,
    branchOrRef: null,
    path: 'docs/spec.md',
    latestCommitSha: null,
    latestBlobSha: null,
    githubUrl: null,
    status: missing ? 'MISSING' : 'IN_REVIEW',
    freshnessStatus: missing ? 'MISSING_DOCUMENT' as const : 'FRESH' as const,
    missing,
  };
}

// ── queueForStatus ────────────────────────────────────────────────────────────

describe('queueForStatus', () => {
  it('maps FRESH to aligned', () => {
    expect(queueForStatus('FRESH')).toBe('aligned');
  });

  it('maps ERROR to errors', () => {
    expect(queueForStatus('ERROR')).toBe('errors');
  });

  it.each(['SOURCE_CHANGED', 'DOCUMENT_CHANGED_AFTER_REVIEW'] as const)(
    'maps %s to stale',
    (status) => {
      expect(queueForStatus(status)).toBe('stale');
    },
  );

  it.each(['MISSING_SOURCE', 'MISSING_DOCUMENT'] as const)(
    'maps %s to missing',
    (status) => {
      expect(queueForStatus(status)).toBe('missing');
    },
  );

  it('maps undefined to missing', () => {
    expect(queueForStatus(undefined)).toBe('missing');
  });

  it('maps UNKNOWN to missing', () => {
    expect(queueForStatus('UNKNOWN')).toBe('missing');
  });
});

// ── summarizeTraceability ─────────────────────────────────────────────────────

describe('summarizeTraceability', () => {
  it('returns FRESH when all freshness items are FRESH', () => {
    const trace = makeTrace({
      freshness: [
        { subjectType: 'SOURCE', subjectId: 'SRC-1', status: 'FRESH', message: 'ok' },
        { subjectType: 'DOCUMENT', subjectId: 'DOC-1', status: 'FRESH', message: 'ok' },
      ],
    });
    const result = summarizeTraceability(trace);
    expect(result.status).toBe('FRESH');
    expect(result.message).toBe('Sources, docs, and reviews are aligned');
  });

  it('prefers ERROR over other statuses', () => {
    const trace = makeTrace({
      freshness: [
        { subjectType: 'SOURCE', subjectId: 'SRC-1', status: 'FRESH', message: '' },
        { subjectType: 'DOC', subjectId: 'DOC-1', status: 'ERROR', message: 'fail' },
        { subjectType: 'DOC', subjectId: 'DOC-2', status: 'MISSING_DOCUMENT', message: '' },
      ],
    });
    expect(summarizeTraceability(trace).status).toBe('ERROR');
  });

  it('prefers DOCUMENT_CHANGED_AFTER_REVIEW over SOURCE_CHANGED', () => {
    const trace = makeTrace({
      freshness: [
        { subjectType: 'S', subjectId: '1', status: 'SOURCE_CHANGED', message: '' },
        { subjectType: 'D', subjectId: '2', status: 'DOCUMENT_CHANGED_AFTER_REVIEW', message: '' },
      ],
    });
    expect(summarizeTraceability(trace).status).toBe('DOCUMENT_CHANGED_AFTER_REVIEW');
  });

  it('counts missing document stages', () => {
    const trace = makeTrace({
      documents: {
        requirementId: 'REQ-001',
        profileId: 'p',
        workspace: null,
        stages: [makeStage(false), makeStage(true), makeStage(true)],
      },
      freshness: [{ subjectType: 'D', subjectId: '1', status: 'MISSING_DOCUMENT', message: '' }],
    });
    const result = summarizeTraceability(trace);
    expect(result.missingDocumentCount).toBe(2);
    expect(result.documentCount).toBe(1);
    expect(result.message).toBe('2 expected docs missing');
  });

  it('counts stale reviews', () => {
    const now = new Date().toISOString();
    const trace = makeTrace({
      reviews: [
        { id: 'R1', documentId: 'D1', requirementId: 'REQ-001', decision: 'APPROVED', comment: null,
          reviewerId: 'u1', reviewerType: 'BUSINESS', commitSha: 'abc', blobSha: 'def',
          anchorType: null, anchorValue: null, stale: true, createdAt: now },
        { id: 'R2', documentId: 'D2', requirementId: 'REQ-001', decision: 'APPROVED', comment: null,
          reviewerId: 'u1', reviewerType: 'BUSINESS', commitSha: 'abc', blobSha: 'def',
          anchorType: null, anchorValue: null, stale: false, createdAt: now },
      ],
    });
    expect(summarizeTraceability(trace).staleReviewCount).toBe(1);
  });

  it('counts sources and artifact links', () => {
    const now = new Date().toISOString();
    const trace = makeTrace({
      sources: [
        { id: 'S1', requirementId: 'REQ-001', sourceType: 'JIRA', externalId: 'J-1',
          title: 'Story', url: 'jira://J-1', sourceUpdatedAt: now,
          fetchedAt: now, freshnessStatus: 'FRESH', errorMessage: null },
        { id: 'S2', requirementId: 'REQ-001', sourceType: 'CONFLUENCE', externalId: 'C-1',
          title: 'Doc', url: 'conf://C-1', sourceUpdatedAt: now,
          fetchedAt: now, freshnessStatus: 'FRESH', errorMessage: null },
      ],
      artifactLinks: [
        { id: 'A1', executionId: 'E1', requirementId: 'REQ-001', artifactType: 'PR',
          storageType: 'GITHUB', title: 'PR #1', uri: 'https://github.com/pr/1',
          repoFullName: null, path: null, commitSha: null, blobSha: null,
          status: 'ACTIVE', createdAt: now },
      ],
    });
    const result = summarizeTraceability(trace);
    expect(result.sourceCount).toBe(2);
    expect(result.artifactCount).toBe(1);
  });

  it('returns MISSING_SOURCE when no sources and no freshness', () => {
    const trace = makeTrace({
      freshness: [{ subjectType: 'S', subjectId: '1', status: 'MISSING_SOURCE', message: '' }],
    });
    expect(summarizeTraceability(trace).status).toBe('MISSING_SOURCE');
    expect(summarizeTraceability(trace).message).toBe('No authoritative source linked');
  });
});
