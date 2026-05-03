/**
 * Requirement Management Type Definitions
 */
import type { SectionResult } from '@/shared/types/section';
import type { SdlcChain } from '@/shared/types/sdlc-chain';

export type { SectionResult, SdlcChain };

// ── Enums / Union Types ──

export type RequirementPriority = 'Critical' | 'High' | 'Medium' | 'Low';

export type RequirementStatus =
  | 'Draft'
  | 'In Review'
  | 'Approved'
  | 'In Progress'
  | 'Delivered'
  | 'Archived';

export type RequirementCategory =
  | 'Functional'
  | 'Non-Functional'
  | 'Technical'
  | 'Business';

export type RequirementSource = 'Manual' | 'Imported' | 'AI-Generated';

export type StoryStatus = 'Draft' | 'Ready' | 'In Progress' | 'Done';

export type SpecStatus = 'Draft' | 'Review' | 'Approved' | 'Implemented';

export type AnalysisConfidence = 'High' | 'Medium' | 'Low';

export type SortField = 'priority' | 'status' | 'recency' | 'title';

export type ViewMode = 'list' | 'kanban' | 'matrix' | 'graph';

// ── List Types ──

export interface StatusDistribution {
  readonly draft: number;
  readonly inReview: number;
  readonly approved: number;
  readonly inProgress: number;
  readonly delivered: number;
  readonly archived: number;
}

export interface RequirementListItem {
  readonly id: string;
  readonly title: string;
  readonly priority: RequirementPriority;
  readonly status: RequirementStatus;
  readonly category: RequirementCategory;
  readonly storyCount: number;
  readonly specCount: number;
  readonly completeness: number;
  readonly completenessScore?: number;
  readonly assignee?: string;
  readonly createdAt?: string;
  readonly updatedAt: string;
}

export interface RequirementFilters {
  readonly priority?: RequirementPriority;
  readonly status?: RequirementStatus;
  readonly category?: RequirementCategory;
  readonly search?: string;
  readonly showCompleted: boolean;
}

export interface RequirementList {
  readonly statusDistribution: StatusDistribution;
  readonly requirements: ReadonlyArray<RequirementListItem>;
  readonly items?: ReadonlyArray<RequirementListItem>;
  readonly totalCount?: number;
}

// ── Detail Types ──

export interface RequirementHeader {
  readonly id: string;
  readonly title: string;
  readonly priority: RequirementPriority;
  readonly status: RequirementStatus;
  readonly category: RequirementCategory;
  readonly source: RequirementSource;
  readonly assignee: string;
  readonly completenessScore: number;
  readonly storyCount: number;
  readonly specCount: number;
  readonly createdAt: string;
  readonly updatedAt: string;
}

export interface AcceptanceCriterion {
  readonly id: string;
  readonly text: string;
  readonly isMet: boolean;
}

export interface RequirementDescription {
  readonly summary: string;
  readonly businessJustification: string;
  readonly acceptanceCriteria: ReadonlyArray<AcceptanceCriterion>;
  readonly assumptions: ReadonlyArray<string>;
  readonly constraints: ReadonlyArray<string>;
}

export interface LinkedStory {
  readonly id: string;
  readonly title: string;
  readonly status: StoryStatus;
  readonly specId: string | null;
  readonly specStatus: SpecStatus | null;
}

export interface LinkedSpec {
  readonly id: string;
  readonly title: string;
  readonly status: SpecStatus;
  readonly version: string;
}

export interface LinkedStoriesSection {
  readonly stories: ReadonlyArray<LinkedStory>;
  readonly totalCount: number;
}

export interface LinkedSpecsSection {
  readonly specs: ReadonlyArray<LinkedSpec>;
  readonly totalCount: number;
}

export interface AiAnalysis {
  readonly completenessScore: number;
  readonly missingElements: ReadonlyArray<string>;
  readonly similarRequirements: ReadonlyArray<{ readonly id: string; readonly similarity: number }>;
  readonly impactAssessment: string;
  readonly suggestions: ReadonlyArray<string>;
}

export interface RequirementDetail {
  readonly header: SectionResult<RequirementHeader>;
  readonly description: SectionResult<RequirementDescription>;
  readonly linkedStories: SectionResult<LinkedStoriesSection>;
  readonly linkedSpecs: SectionResult<LinkedSpecsSection>;
  readonly aiAnalysis: SectionResult<AiAnalysis>;
  readonly sdlcChain: SectionResult<SdlcChain>;
}

// ── Pipeline Profile Types (A0.5) ──

export type SpecTier = 'L1' | 'L2' | 'L3';

export interface ChainNode {
  readonly id: string;
  readonly label: string;
  readonly artifactType: string;
  readonly isExecutionHub: boolean;
}

export interface EntryPath {
  readonly id: string;
  readonly label: string;
  readonly description: string;
}

export interface SkillBinding {
  readonly skillId: string;
  readonly label: string;
  readonly triggerPoint: string;
}

export interface SkillDocumentContract {
  readonly skillId: string;
  readonly label: string;
  readonly description: string;
  readonly inputDocuments: ReadonlyArray<string>;
  readonly outputDocuments: ReadonlyArray<string>;
  readonly dependsOnSkills: ReadonlyArray<string>;
}

export interface DocumentDependencyDefinition {
  readonly from: string;
  readonly to: string;
  readonly reason: string;
}

export interface SpecTiering {
  readonly tiers: ReadonlyArray<SpecTier>;
  readonly defaultTier: SpecTier;
}

export interface DocumentStageDefinition {
  readonly sddType: string;
  readonly label: string;
  readonly pathPattern: string;
  readonly artifactType: string;
  readonly expectedTier?: SpecTier | null;
  readonly traceabilityKey?: string | null;
}

export interface PipelineProfile {
  readonly id: string;
  readonly name: string;
  readonly description: string;
  readonly chainNodes: ReadonlyArray<ChainNode>;
  readonly skills: ReadonlyArray<SkillBinding>;
  readonly skillDocumentContracts?: ReadonlyArray<SkillDocumentContract>;
  readonly skillFlowDocuments?: ReadonlyArray<DocumentStageDefinition>;
  readonly documentDependencies?: ReadonlyArray<DocumentDependencyDefinition>;
  readonly entryPaths: ReadonlyArray<EntryPath>;
  readonly documentStages: ReadonlyArray<DocumentStageDefinition>;
  readonly specTiering: SpecTiering | null;
  readonly usesOrchestrator: boolean;
  readonly traceabilityMode: 'per-layer' | 'shared-br';
}

export type FreshnessStatus =
  | 'FRESH'
  | 'SOURCE_CHANGED'
  | 'DOCUMENT_CHANGED_AFTER_REVIEW'
  | 'MISSING_DOCUMENT'
  | 'MISSING_SOURCE'
  | 'UNKNOWN'
  | 'ERROR';

export type DocumentQualityBand = 'EXCELLENT' | 'GOOD' | 'BLOCKED';

export interface DocumentQualityDimension {
  readonly key: string;
  readonly label: string;
  readonly score: number;
  readonly maxScore: number;
}

export interface DocumentQualityFinding {
  readonly severity: string;
  readonly section: string;
  readonly message: string;
}

export interface DocumentQualityGate {
  readonly executionId?: string;
  readonly documentId?: string;
  readonly requirementId?: string;
  readonly profileId?: string;
  readonly sddType?: string;
  readonly score: number;
  readonly band: DocumentQualityBand;
  readonly label?: string;
  readonly passed: boolean;
  readonly threshold: number;
  readonly rubricVersion?: string;
  readonly commitSha?: string;
  readonly blobSha?: string;
  readonly dimensions?: ReadonlyArray<DocumentQualityDimension>;
  readonly summary: string;
  readonly findings: ReadonlyArray<string | DocumentQualityFinding>;
  readonly stale?: boolean;
  readonly scoredAt?: string;
}

export type RequirementControlSourceType = 'JIRA' | 'CONFLUENCE' | 'GITHUB' | 'KB' | 'UPLOAD' | 'URL';

export interface SourceReference {
  readonly id: string;
  readonly requirementId: string;
  readonly sourceType: RequirementControlSourceType;
  readonly externalId: string | null;
  readonly title: string;
  readonly url: string;
  readonly sourceUpdatedAt: string | null;
  readonly fetchedAt: string | null;
  readonly freshnessStatus: FreshnessStatus;
  readonly errorMessage: string | null;
}

export interface SddDocumentStage {
  readonly id: string | null;
  readonly sddType: string;
  readonly stageLabel: string;
  readonly title: string;
  readonly repoFullName: string | null;
  readonly branchOrRef: string | null;
  readonly path: string | null;
  readonly latestCommitSha: string | null;
  readonly latestBlobSha: string | null;
  readonly githubUrl: string | null;
  readonly status: string;
  readonly freshnessStatus: FreshnessStatus;
  readonly missing: boolean;
  readonly qualityGate?: DocumentQualityGate | null;
}

export interface SddWorkspace {
  readonly id: string;
  readonly applicationId: string;
  readonly applicationName: string;
  readonly snowGroup: string;
  readonly sourceRepoFullName: string;
  readonly sddRepoFullName: string;
  readonly baseBranch: string;
  readonly workingBranch: string;
  readonly lifecycleStatus: string;
  readonly docsRoot: string;
  readonly releasePrUrl: string | null;
  readonly kbRepoFullName: string;
  readonly kbMainBranch: string;
  readonly kbPreviewBranch: string;
  readonly graphManifestPath: string;
}

export interface SddDocumentIndex {
  readonly requirementId: string;
  readonly profileId: string;
  readonly workspace: SddWorkspace | null;
  readonly stages: ReadonlyArray<SddDocumentStage>;
}

export interface SddDocumentContent {
  readonly document: SddDocumentStage;
  readonly markdown: string;
  readonly commitSha: string;
  readonly blobSha: string;
  readonly githubUrl: string;
  readonly fetchedAt: string;
}

export type ReviewDecision = 'COMMENT' | 'APPROVED' | 'CHANGES_REQUESTED' | 'REJECTED';

export interface DocumentReview {
  readonly id: string;
  readonly documentId: string;
  readonly requirementId: string;
  readonly decision: ReviewDecision;
  readonly comment: string | null;
  readonly reviewerId: string;
  readonly reviewerType: string;
  readonly commitSha: string;
  readonly blobSha: string;
  readonly anchorType: string | null;
  readonly anchorValue: string | null;
  readonly stale: boolean;
  readonly createdAt: string;
}

export interface ArtifactLink {
  readonly id: string;
  readonly executionId: string;
  readonly requirementId: string;
  readonly artifactType: string;
  readonly storageType: string;
  readonly title: string;
  readonly uri: string;
  readonly repoFullName: string | null;
  readonly path: string | null;
  readonly commitSha: string | null;
  readonly blobSha: string | null;
  readonly status: string;
  readonly createdAt: string;
}

export interface AgentRun {
  readonly executionId: string;
  readonly requirementId: string;
  readonly profileId: string;
  readonly skillKey: string;
  readonly targetStage: string | null;
  readonly status: 'MANIFEST_READY' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'STALE_CONTEXT' | string;
  readonly manifest: Record<string, unknown>;
  readonly command?: string | null;
  readonly callbackUrl?: string | null;
  readonly stageEvents?: ReadonlyArray<AgentStageEvent>;
  readonly outputSummary: string | null;
  readonly errorMessage: string | null;
  readonly artifactLinks: ReadonlyArray<ArtifactLink>;
  readonly createdAt: string;
  readonly updatedAt: string;
}

export interface AgentStageEvent {
  readonly id: string;
  readonly executionId: string;
  readonly requirementId: string;
  readonly profileId: string;
  readonly stageId: string;
  readonly stageLabel: string | null;
  readonly state: string;
  readonly message: string | null;
  readonly outputPath: string | null;
  readonly errorMessage: string | null;
  readonly createdAt: string;
}

export interface AgentRunMergeConfirmation {
  readonly run: AgentRun;
  readonly event: AgentStageEvent;
  readonly documents: SddDocumentIndex;
}

export interface FreshnessItem {
  readonly subjectType: string;
  readonly subjectId: string;
  readonly status: FreshnessStatus;
  readonly message: string;
}

export interface RequirementTraceability {
  readonly requirementId: string;
  readonly sources: ReadonlyArray<SourceReference>;
  readonly documents: SddDocumentIndex;
  readonly reviews: ReadonlyArray<DocumentReview>;
  readonly agentRuns: ReadonlyArray<AgentRun>;
  readonly artifactLinks: ReadonlyArray<ArtifactLink>;
  readonly freshness: ReadonlyArray<FreshnessItem>;
}

export interface RequirementControlPlaneListSummary {
  readonly requirementId: string;
  readonly sourceCount: number;
  readonly documentCount: number;
  readonly missingDocumentCount: number;
  readonly staleReviewCount: number;
  readonly artifactCount: number;
  readonly status: FreshnessStatus;
  readonly message: string;
}

export interface OrchestratorResult {
  readonly determinedPathId: string;
  readonly determinedTier: SpecTier | null;
  readonly confidence: AnalysisConfidence;
  readonly reasoning: string;
}

export interface SkillExecutionResult {
  readonly executionId: string;
  readonly skillName: string;
  readonly status: string;
  readonly requirementId: string;
  readonly startedAt: string;
  readonly estimatedCompletionSeconds: number;
  readonly message: string;
  readonly orchestratorResult?: OrchestratorResult | null;
}

// ── Import Types (A12) ──

export type ImportStep = 'source' | 'normalizing' | 'processing' | 'review' | 'batch-preview' | 'batch-normalizing' | 'batch-review';
export type ImportSourceType = 'paste' | 'file' | 'email' | 'meeting';

export interface RequirementSourceInput {
  readonly sourceType: 'TEXT' | 'FILE' | 'EMAIL' | 'MEETING';
  readonly text: string;
  readonly fileName: string | null;
  readonly fileSize: number | null;
  readonly fileNames?: ReadonlyArray<string>;
  readonly fileCount?: number | null;
  readonly kbName?: string | null;
}

export type ImportInspectionStatus = 'PARSED' | 'MANUAL_REVIEW' | 'SKIPPED';

export interface ImportInspectionFile {
  readonly fileName: string;
  readonly fileType: string;
  readonly processingStatus: ImportInspectionStatus;
  readonly summary: string;
  readonly extractedCharacters?: number | null;
  readonly preview?: string | null;
}

export interface ImportInspection {
  readonly sourceFileName: string;
  readonly sourceKind: 'FILE' | 'ZIP' | string;
  readonly totalFiles: number;
  readonly parsedFiles: number;
  readonly manualReviewFiles: number;
  readonly skippedFiles: number;
  readonly files: ReadonlyArray<ImportInspectionFile>;
}

export interface RequirementDraft {
  readonly title: string;
  readonly priority: RequirementPriority;
  readonly category: RequirementCategory;
  readonly summary: string;
  readonly businessJustification: string;
  readonly acceptanceCriteria: ReadonlyArray<string>;
  readonly assumptions: ReadonlyArray<string>;
  readonly constraints: ReadonlyArray<string>;
  readonly missingInfo: ReadonlyArray<string>;
  readonly openQuestions: ReadonlyArray<string>;
  readonly aiSuggestedFields: ReadonlyArray<string>;
  readonly normalizedBy?: string;
  readonly normalizedAt?: string;
  readonly importInspection?: ImportInspection | null;
  readonly sourceAttachment?: RequirementSourceInput | null;
}

export interface RequirementImportFileStatus {
  readonly displayName: string;
  readonly sourceFileName: string;
  readonly sourceKind: string;
  readonly fileExtension: string | null;
  readonly fileSize: number;
  readonly supported: boolean;
  readonly providerStatus: string;
  readonly errorMessage?: string | null;
  readonly preview?: string | null;
  readonly providerDocumentId?: string | null;
}

export interface RequirementImportStatus {
  readonly importId: string;
  readonly taskId: string;
  readonly status: string;
  readonly message: string;
  readonly knowledgeBaseName: string;
  readonly datasetId: string | null;
  readonly totalNumberOfFiles: number;
  readonly numberOfSuccesses: number;
  readonly numberOfFailures: number;
  readonly totalSize: number;
  readonly unsupportedFileTypes: ReadonlyArray<string>;
  readonly supportedFileTypes: ReadonlyArray<string>;
  readonly files: ReadonlyArray<RequirementImportFileStatus>;
  readonly draft?: RequirementDraft | null;
  readonly createdAt: string;
  readonly updatedAt: string;
}

// ── SDD Knowledge Graph Types ──

export type GraphNodeKind = 'DOCUMENT' | 'PROGRAM' | 'FILE' | 'REQUIREMENT' | string;
export type GraphIssueSeverity = 'ERROR' | 'WARNING' | 'INFO' | string;
export type GraphImpactDirection = 'upstream' | 'downstream' | 'both';

export interface GraphScope {
  readonly workspaceId?: string | null;
  readonly applicationId?: string | null;
  readonly snowGroup?: string | null;
  readonly projectId?: string | null;
  readonly profileId?: string | null;
  readonly branch?: string | null;
  readonly provider: string;
}

export interface GraphHealth {
  readonly nodeCount: number;
  readonly edgeCount: number;
  readonly issueCount: number;
  readonly errorCount: number;
  readonly warningCount: number;
  readonly suggestionCount?: number;
  readonly stale: boolean;
  readonly lastGeneratedAt?: string | null;
  readonly lastImportedAt?: string | null;
}

export interface GraphProviderHealth extends Pick<GraphHealth, 'nodeCount' | 'edgeCount' | 'issueCount' | 'stale'> {
  readonly provider: string;
  readonly available: boolean;
  readonly lastSync?: Record<string, unknown> | null;
  readonly lastImport?: Record<string, unknown> | null;
  readonly message?: string | null;
}

export interface GraphNode {
  readonly id: string;
  readonly kind: GraphNodeKind;
  readonly label: string;
  readonly properties: Record<string, unknown>;
}

export interface GraphEdge {
  readonly id: string;
  readonly type: string;
  readonly from: string;
  readonly to: string;
  readonly source: string;
  readonly confidence: number;
  readonly properties: Record<string, unknown>;
}

export interface GraphIssue {
  readonly id: string;
  readonly severity: GraphIssueSeverity;
  readonly code: string;
  readonly message: string;
  readonly nodeId?: string | null;
  readonly edgeId?: string | null;
  readonly properties: Record<string, unknown>;
}

export interface GraphSuggestion {
  readonly id: string;
  readonly type: string;
  readonly message: string;
  readonly nodeId?: string | null;
  readonly properties: Record<string, unknown>;
}

export interface KnowledgeGraph {
  readonly scope: GraphScope;
  readonly health: GraphHealth;
  readonly nodes: ReadonlyArray<GraphNode>;
  readonly edges: ReadonlyArray<GraphEdge>;
  readonly issues: ReadonlyArray<GraphIssue>;
  readonly suggestions?: ReadonlyArray<GraphSuggestion>;
  readonly lastSync?: Record<string, unknown> | null;
}

export interface GraphNodeDetail {
  readonly node: GraphNode | null;
  readonly incoming: ReadonlyArray<GraphEdge>;
  readonly outgoing: ReadonlyArray<GraphEdge>;
  readonly issues: ReadonlyArray<GraphIssue>;
}

export interface GraphImpactRequest {
  readonly nodeId: string;
  readonly direction?: GraphImpactDirection;
  readonly maxDepth?: number;
  readonly relationshipTypes?: ReadonlyArray<string>;
  readonly applicationId?: string;
  readonly snowGroup?: string;
  readonly branch?: string;
  readonly profileId?: string;
}

export interface GraphImpact {
  readonly startNodeId: string;
  readonly direction: GraphImpactDirection;
  readonly maxDepth: number;
  readonly paths: ReadonlyArray<{
    readonly depth: number;
    readonly nodes: ReadonlyArray<GraphNode>;
    readonly edges: ReadonlyArray<GraphEdge>;
  }>;
  readonly summary: {
    readonly impactedDocuments: number;
    readonly impactedPrograms: number;
    readonly impactedFiles: number;
    readonly staleReviews: number;
  };
}

export interface GraphFilters {
  readonly workspaceId?: string;
  readonly applicationId?: string;
  readonly snowGroup?: string;
  readonly projectId?: string;
  readonly profileId?: string;
  readonly branch?: string;
  readonly requirementId?: string;
  readonly nodeKinds?: ReadonlyArray<string>;
  readonly includeIssues?: boolean;
  readonly includeSuggestions?: boolean;
  readonly limit?: number;
}

export interface ImportState {
  readonly isOpen: boolean;
  readonly step: ImportStep;
  readonly sourceType: ImportSourceType;
  readonly rawInput: string;
  readonly kbName: string;
  readonly fileName: string | null;
  readonly fileSize: number | null;
  readonly fileNames: ReadonlyArray<string>;
  readonly fileCount: number;
  readonly error: string | null;
  readonly draft: RequirementDraft | null;
  readonly importId: string | null;
  readonly taskId: string | null;
  readonly importStatus: string | null;
  readonly importMessage: string | null;
  readonly importDatasetId: string | null;
  readonly importFiles: ReadonlyArray<RequirementImportFileStatus>;
  readonly supportedFileTypes: ReadonlyArray<string>;
  readonly unsupportedFileTypes: ReadonlyArray<string>;
  readonly importSuccessCount: number;
  readonly importFailureCount: number;
  readonly importUpdatedAt: string | null;
  readonly batchRows: ReadonlyArray<Record<string, string>>;
  readonly batchDrafts: ReadonlyArray<RequirementDraft>;
  readonly batchProgress: number;
  readonly batchTotal: number;
  readonly columnMapping: Record<string, string>;
}
