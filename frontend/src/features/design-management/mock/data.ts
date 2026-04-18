import type { CatalogAggregate, TraceabilityAggregate, ViewerAggregate } from '../types';

const html = encodeURIComponent('<!doctype html><html><body style="background:#07111f;color:#e6eefc;font-family:Inter,sans-serif;padding:32px"><h1>Control Tower Dashboard</h1><p>Mock preview for Design Management development mode.</p></body></html>');

const catalogAggregate: CatalogAggregate = {
  summary: {
    data: {
      workspaceId: 'ws-default-001',
      totalArtifacts: 4,
      linkedArtifacts: 3,
      draftArtifacts: 1,
      publishedArtifacts: 2,
      retiredArtifacts: 1,
      coverageBuckets: {
        OK: 0,
        PARTIAL: 1,
        STALE: 2,
        MISSING: 1,
        UNKNOWN: 0,
      },
      lastRefreshedAt: '2026-04-17T08:23:00Z',
      advisory: '1 artifact has no spec links, 2 have stale coverage.',
    },
    error: null,
  },
  grid: {
    data: [
      {
        projectId: 'proj-42',
        projectName: 'Gateway Migration',
        artifacts: [
          {
            artifactId: 'art-2041',
            workspaceId: 'ws-default-001',
            projectId: 'proj-42',
            projectName: 'Gateway Migration',
            title: 'Control Tower Dashboard',
            format: 'STITCH',
            lifecycle: 'PUBLISHED',
            authors: [{ memberId: 'u-007', displayName: 'Grace Hopper' }, { memberId: 'u-003', displayName: 'Ada Lovelace' }],
            currentVersionId: 'art-2041-v2',
            currentVersionNumber: 2,
            lastUpdatedAt: '2026-04-17T08:20:00Z',
            linkedSpecCount: 2,
            worstCoverageStatus: 'STALE',
            aiSummaryReady: true,
          },
        ],
      },
      {
        projectId: 'proj-11',
        projectName: 'Card Issuance',
        artifacts: [
          {
            artifactId: 'art-2042',
            workspaceId: 'ws-default-001',
            projectId: 'proj-11',
            projectName: 'Card Issuance',
            title: 'Project Space Drilldown',
            format: 'HTML',
            lifecycle: 'PUBLISHED',
            authors: [{ memberId: 'u-003', displayName: 'Ada Lovelace' }],
            currentVersionId: 'art-2042-v1',
            currentVersionNumber: 1,
            lastUpdatedAt: '2026-04-16T09:40:00Z',
            linkedSpecCount: 2,
            worstCoverageStatus: 'PARTIAL',
            aiSummaryReady: true,
          },
        ],
      },
      {
        projectId: 'proj-55',
        projectName: 'Fraud Detection Expansion',
        artifacts: [
          {
            artifactId: 'art-2043',
            workspaceId: 'ws-default-001',
            projectId: 'proj-55',
            projectName: 'Fraud Detection Expansion',
            title: 'Incident Command Center',
            format: 'STITCH',
            lifecycle: 'DRAFT',
            authors: [{ memberId: 'u-007', displayName: 'Grace Hopper' }],
            currentVersionId: 'art-2043-v1',
            currentVersionNumber: 1,
            lastUpdatedAt: '2026-04-17T07:05:00Z',
            linkedSpecCount: 0,
            worstCoverageStatus: 'MISSING',
            aiSummaryReady: false,
          },
        ],
      },
      {
        projectId: 'proj-88',
        projectName: 'Legacy Queue Decommission',
        artifacts: [
          {
            artifactId: 'art-2044',
            workspaceId: 'ws-default-001',
            projectId: 'proj-88',
            projectName: 'Legacy Queue Decommission',
            title: 'Platform Center Access Matrix',
            format: 'HTML',
            lifecycle: 'RETIRED',
            authors: [{ memberId: 'u-003', displayName: 'Ada Lovelace' }],
            currentVersionId: 'art-2044-v1',
            currentVersionNumber: 1,
            lastUpdatedAt: '2026-04-14T03:15:00Z',
            linkedSpecCount: 1,
            worstCoverageStatus: 'STALE',
            aiSummaryReady: false,
          },
        ],
      },
    ],
    error: null,
  },
  filters: {
    data: {
      projects: ['proj-42', 'proj-11', 'proj-55', 'proj-88'],
      lifecycles: ['DRAFT', 'PUBLISHED', 'RETIRED'],
      coverageStatuses: ['OK', 'PARTIAL', 'STALE', 'MISSING', 'UNKNOWN'],
    },
    error: null,
  },
};

const viewerAggregates: Record<string, ViewerAggregate> = {
  'art-2041': {
    header: {
      data: {
        artifactId: 'art-2041',
        workspaceId: 'ws-default-001',
        projectId: 'proj-42',
        projectName: 'Gateway Migration',
        title: 'Control Tower Dashboard',
        format: 'STITCH',
        lifecycle: 'PUBLISHED',
        authors: [{ memberId: 'u-007', displayName: 'Grace Hopper' }, { memberId: 'u-003', displayName: 'Ada Lovelace' }],
        currentVersionId: 'art-2041-v2',
        currentVersionNumber: 2,
        registeredAt: '2026-04-11T08:00:00Z',
        registeredBy: { memberId: 'u-007', displayName: 'Grace Hopper' },
        lastUpdatedAt: '2026-04-17T08:20:00Z',
        rawUrl: `data:text/html;charset=utf-8,${html}`,
      },
      error: null,
    },
    versions: {
      data: [
        {
          versionId: 'art-2041-v2',
          versionNumber: 2,
          label: 'v2',
          sizeBytes: 864,
          changeLogNote: 'Aligned shell density and command panel placement',
          createdBy: { memberId: 'u-003', displayName: 'Ada Lovelace' },
          createdAt: '2026-04-17T08:20:00Z',
          current: true,
        },
        {
          versionId: 'art-2041-v1',
          versionNumber: 1,
          label: 'v1',
          sizeBytes: 612,
          changeLogNote: 'Initial registration snapshot',
          createdBy: { memberId: 'u-007', displayName: 'Grace Hopper' },
          createdAt: '2026-04-11T08:00:00Z',
          current: false,
        },
      ],
      error: null,
    },
    linkedSpecs: {
      data: [
        {
          linkId: 'dsl-2041-1',
          specId: 'SPEC-001',
          requirementId: 'REQ-0001',
          requirementRoute: '/requirements/REQ-0001',
          specTitle: 'SSO Authentication Flow Specification',
          specState: 'APPROVED',
          coversRevision: 9,
          specLatestRevision: 9,
          declaredCoverage: 'FULL',
          coverageStatus: 'OK',
          linkedBy: { memberId: 'u-007', displayName: 'Grace Hopper' },
          linkedAt: '2026-04-17T08:22:00Z',
          why: 'OK: link covers latest spec revision 9',
        },
        {
          linkId: 'dsl-2041-2',
          specId: 'SPEC-040',
          requirementId: 'REQ-0005',
          requirementRoute: '/requirements/REQ-0005',
          specTitle: 'Audit Event Schema Specification',
          specState: 'APPROVED',
          coversRevision: 2,
          specLatestRevision: 3,
          declaredCoverage: 'FULL',
          coverageStatus: 'STALE',
          linkedBy: { memberId: 'u-007', displayName: 'Grace Hopper' },
          linkedAt: '2026-04-17T08:22:30Z',
          why: 'STALE: link covers spec revision 2; latest is 3',
        },
      ],
      error: null,
    },
    aiSummary: {
      data: {
        summaryId: 'dais-2041-v2',
        artifactId: 'art-2041',
        versionId: 'art-2041-v2',
        skillVersion: 'artifact-summarizer@1.0.0',
        status: 'SUCCESS',
        summaryText: 'Dashboard mock emphasizing SDLC chain status, incident counters, and an AI operations brief.',
        keyElements: ['SDLC chain strip', 'Incident counter group', 'AI command brief'],
        errorMessage: null,
        generatedAt: '2026-04-17T08:23:00Z',
      },
      error: null,
    },
    changeLog: {
      data: [
        {
          id: 'dlog-2041-05',
          entryType: 'AI_SUMMARY_REGENERATED',
          actorMemberId: 'u-007',
          actorDisplayName: 'Grace Hopper',
          reason: 'AI summary refreshed after v2 publish',
          beforeJson: null,
          afterJson: '{"summaryId":"dais-2041-v2"}',
          occurredAt: '2026-04-17T08:23:00Z',
        },
        {
          id: 'dlog-2041-03',
          entryType: 'VERSION_PUBLISHED',
          actorMemberId: 'u-003',
          actorDisplayName: 'Ada Lovelace',
          reason: 'Refined dashboard density',
          beforeJson: '{"versionId":"art-2041-v1"}',
          afterJson: '{"versionId":"art-2041-v2"}',
          occurredAt: '2026-04-17T08:20:00Z',
        },
      ],
      error: null,
    },
  },
};

const traceabilityAggregate: TraceabilityAggregate = {
  matrix: {
    data: [
      {
        specId: 'SPEC-040',
        specTitle: 'Audit Event Schema Specification',
        requirementId: 'REQ-0005',
        requirementRoute: '/requirements/REQ-0005',
        projectId: 'proj-42',
        projectName: 'Gateway Migration',
        latestRevision: 3,
        specState: 'APPROVED',
        overallCoverageStatus: 'STALE',
        cells: [
          {
            artifactId: 'art-2041',
            artifactTitle: 'Control Tower Dashboard',
            versionId: 'art-2041-v2',
            coverageStatus: 'STALE',
            lifecycle: 'PUBLISHED',
            viewerRoute: '/design-management/artifacts/art-2041',
          },
        ],
      },
      {
        specId: 'SPEC-030',
        specTitle: 'Oracle 23ai Migration Specification',
        requirementId: 'REQ-0004',
        requirementRoute: '/requirements/REQ-0004',
        projectId: 'proj-55',
        projectName: 'Fraud Detection Expansion',
        latestRevision: 7,
        specState: 'REVIEW',
        overallCoverageStatus: 'MISSING',
        cells: [],
      },
    ],
    error: null,
  },
  summary: {
    data: {
      specCount: 2,
      artifactCount: 4,
      buckets: [
        { status: 'STALE', count: 1, percentage: 50 },
        { status: 'MISSING', count: 1, percentage: 50 },
      ],
    },
    error: null,
  },
  gaps: {
    data: [
      {
        specId: 'SPEC-030',
        specTitle: 'Oracle 23ai Migration Specification',
        requirementId: 'REQ-0004',
        requirementRoute: '/requirements/REQ-0004',
        projectId: 'proj-55',
        projectName: 'Fraud Detection Expansion',
        latestRevision: 7,
        specState: 'REVIEW',
      },
    ],
    error: null,
  },
};

export async function getMockCatalogAggregate(): Promise<CatalogAggregate> {
  return catalogAggregate;
}

export async function getMockViewerAggregate(artifactId: string): Promise<ViewerAggregate> {
  return viewerAggregates[artifactId] ?? viewerAggregates['art-2041'];
}

export async function getMockTraceabilityAggregate(): Promise<TraceabilityAggregate> {
  return traceabilityAggregate;
}
