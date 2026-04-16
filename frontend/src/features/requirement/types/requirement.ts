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

export type ViewMode = 'list' | 'kanban' | 'matrix';

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

export interface SpecTiering {
  readonly tiers: ReadonlyArray<SpecTier>;
  readonly defaultTier: SpecTier;
}

export interface PipelineProfile {
  readonly id: string;
  readonly name: string;
  readonly description: string;
  readonly chainNodes: ReadonlyArray<ChainNode>;
  readonly skills: ReadonlyArray<SkillBinding>;
  readonly entryPaths: ReadonlyArray<EntryPath>;
  readonly specTiering: SpecTiering | null;
  readonly usesOrchestrator: boolean;
  readonly traceabilityMode: 'per-layer' | 'shared-br';
}

export interface OrchestratorResult {
  readonly determinedPathId: string;
  readonly determinedTier: SpecTier | null;
  readonly confidence: AnalysisConfidence;
  readonly reasoning: string;
}

// ── Import Types (A12) ──

export type ImportStep = 'source' | 'normalizing' | 'review' | 'batch-preview' | 'batch-normalizing' | 'batch-review';
export type ImportSourceType = 'paste' | 'file' | 'email' | 'meeting';

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
}

export interface ImportState {
  readonly isOpen: boolean;
  readonly step: ImportStep;
  readonly sourceType: ImportSourceType;
  readonly rawInput: string;
  readonly fileName: string | null;
  readonly draft: RequirementDraft | null;
  readonly batchRows: ReadonlyArray<Record<string, string>>;
  readonly batchDrafts: ReadonlyArray<RequirementDraft>;
  readonly batchProgress: number;
  readonly batchTotal: number;
  readonly columnMapping: Record<string, string>;
}
