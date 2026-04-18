import type { SectionResult } from '@/shared/types/section';

export const DM_DEFAULT_WORKSPACE_ID = 'ws-default-001';
export const DM_ARTIFACT_ID_PATTERN = /^art-[a-z0-9-]+$/;

export type ArtifactLifecycle = 'DRAFT' | 'PUBLISHED' | 'RETIRED';
export type ArtifactFormat = 'STITCH' | 'HTML';
export type CoverageStatus = 'OK' | 'PARTIAL' | 'STALE' | 'MISSING' | 'UNKNOWN';
export type AiSummaryStatus = 'PENDING' | 'SUCCESS' | 'FAILED';

export interface MemberRef {
  readonly memberId: string;
  readonly displayName: string;
}

export interface CatalogSummary {
  readonly workspaceId: string;
  readonly totalArtifacts: number;
  readonly linkedArtifacts: number;
  readonly draftArtifacts: number;
  readonly publishedArtifacts: number;
  readonly retiredArtifacts: number;
  readonly coverageBuckets: Readonly<Record<string, number>>;
  readonly lastRefreshedAt: string;
  readonly advisory: string;
}

export interface CatalogArtifactRow {
  readonly artifactId: string;
  readonly workspaceId: string;
  readonly projectId: string;
  readonly projectName: string;
  readonly title: string;
  readonly format: ArtifactFormat;
  readonly lifecycle: ArtifactLifecycle;
  readonly authors: ReadonlyArray<MemberRef>;
  readonly currentVersionId: string;
  readonly currentVersionNumber: number;
  readonly lastUpdatedAt: string;
  readonly linkedSpecCount: number;
  readonly worstCoverageStatus: CoverageStatus;
  readonly aiSummaryReady: boolean;
}

export interface CatalogSection {
  readonly projectId: string;
  readonly projectName: string;
  readonly artifacts: ReadonlyArray<CatalogArtifactRow>;
}

export interface CatalogFilters {
  readonly projects: ReadonlyArray<string>;
  readonly lifecycles: ReadonlyArray<ArtifactLifecycle>;
  readonly coverageStatuses: ReadonlyArray<CoverageStatus>;
}

export interface CatalogAggregate {
  readonly summary: SectionResult<CatalogSummary>;
  readonly grid: SectionResult<ReadonlyArray<CatalogSection>>;
  readonly filters: SectionResult<CatalogFilters>;
}

export interface ArtifactHeader {
  readonly artifactId: string;
  readonly workspaceId: string;
  readonly projectId: string;
  readonly projectName: string;
  readonly title: string;
  readonly format: ArtifactFormat;
  readonly lifecycle: ArtifactLifecycle;
  readonly authors: ReadonlyArray<MemberRef>;
  readonly currentVersionId: string;
  readonly currentVersionNumber: number;
  readonly registeredAt: string;
  readonly registeredBy: MemberRef;
  readonly lastUpdatedAt: string;
  readonly rawUrl: string;
}

export interface ArtifactVersionRef {
  readonly versionId: string;
  readonly versionNumber: number;
  readonly label: string;
  readonly sizeBytes: number;
  readonly changeLogNote: string | null;
  readonly createdBy: MemberRef;
  readonly createdAt: string;
  readonly current: boolean;
}

export interface LinkedSpecRow {
  readonly linkId: string;
  readonly specId: string;
  readonly requirementId: string | null;
  readonly requirementRoute: string | null;
  readonly specTitle: string;
  readonly specState: string;
  readonly coversRevision: number;
  readonly specLatestRevision: number;
  readonly declaredCoverage: string;
  readonly coverageStatus: CoverageStatus;
  readonly linkedBy: MemberRef;
  readonly linkedAt: string;
  readonly why: string;
}

export interface AiSummaryPayload {
  readonly summaryId: string | null;
  readonly artifactId: string;
  readonly versionId: string;
  readonly skillVersion: string;
  readonly status: AiSummaryStatus;
  readonly summaryText: string | null;
  readonly keyElements: ReadonlyArray<string>;
  readonly errorMessage: string | null;
  readonly generatedAt: string | null;
}

export interface ChangeLogEntry {
  readonly id: string;
  readonly entryType: string;
  readonly actorMemberId: string | null;
  readonly actorDisplayName: string;
  readonly reason: string | null;
  readonly beforeJson: string | null;
  readonly afterJson: string | null;
  readonly occurredAt: string;
}

export interface ViewerAggregate {
  readonly header: SectionResult<ArtifactHeader>;
  readonly versions: SectionResult<ReadonlyArray<ArtifactVersionRef>>;
  readonly linkedSpecs: SectionResult<ReadonlyArray<LinkedSpecRow>>;
  readonly aiSummary: SectionResult<AiSummaryPayload>;
  readonly changeLog: SectionResult<ReadonlyArray<ChangeLogEntry>>;
}

export interface TraceabilityCell {
  readonly artifactId: string;
  readonly artifactTitle: string;
  readonly versionId: string;
  readonly coverageStatus: CoverageStatus;
  readonly lifecycle: ArtifactLifecycle;
  readonly viewerRoute: string;
}

export interface TraceabilityMatrixRow {
  readonly specId: string;
  readonly specTitle: string;
  readonly requirementId: string;
  readonly requirementRoute: string;
  readonly projectId: string;
  readonly projectName: string;
  readonly latestRevision: number;
  readonly specState: string;
  readonly cells: ReadonlyArray<TraceabilityCell>;
  readonly overallCoverageStatus: CoverageStatus;
}

export interface CoverageBucket {
  readonly status: CoverageStatus;
  readonly count: number;
  readonly percentage: number;
}

export interface TraceabilitySummary {
  readonly specCount: number;
  readonly artifactCount: number;
  readonly buckets: ReadonlyArray<CoverageBucket>;
}

export interface TraceabilityGap {
  readonly specId: string;
  readonly specTitle: string;
  readonly requirementId: string;
  readonly requirementRoute: string;
  readonly projectId: string;
  readonly projectName: string;
  readonly latestRevision: number;
  readonly specState: string;
}

export interface TraceabilityAggregate {
  readonly matrix: SectionResult<ReadonlyArray<TraceabilityMatrixRow>>;
  readonly summary: SectionResult<TraceabilitySummary>;
  readonly gaps: SectionResult<ReadonlyArray<TraceabilityGap>>;
}
