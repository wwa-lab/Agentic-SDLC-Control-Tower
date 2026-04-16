/**
 * Incident Management Type Definitions
 */
import type { SectionResult } from '@/shared/types/section';

export type { SectionResult };

// ── Enums / Union Types ──

export type Priority = 'P1' | 'P2' | 'P3' | 'P4';

export type IncidentStatus =
  | 'DETECTED'
  | 'AI_INVESTIGATING'
  | 'AI_DIAGNOSED'
  | 'ACTION_PROPOSED'
  | 'PENDING_APPROVAL'
  | 'EXECUTING'
  | 'RESOLVED'
  | 'LEARNING'
  | 'CLOSED'
  | 'ESCALATED'
  | 'MANUAL_OVERRIDE';

export type HandlerType = 'AI' | 'Human' | 'Hybrid';
export type ControlMode = 'Auto' | 'Approval' | 'Manual';
export type AutonomyLevel = 'Level1_Manual' | 'Level2_SuggestApprove' | 'Level3_AutoAudit';
export type DiagnosisEntryType = 'analysis' | 'finding' | 'suggestion' | 'conclusion';
export type SkillExecutionStatus = 'running' | 'completed' | 'failed' | 'pending_approval';
export type ActionType = 'automated' | 'requires_approval';
export type ActionExecutionStatus = 'pending' | 'approved' | 'rejected' | 'executing' | 'executed' | 'rolled_back';
export type GovernanceActionType = 'approve' | 'reject' | 'escalate' | 'override';
export type ConfidenceLevel = 'High' | 'Medium' | 'Low';
export type SdlcArtifactType = 'requirement' | 'spec' | 'design' | 'code' | 'test' | 'deploy';
export type SortField = 'priority' | 'status' | 'detectedAt' | 'duration';

// ── List Types ──

export interface SeverityDistribution {
  readonly p1: number;
  readonly p2: number;
  readonly p3: number;
  readonly p4: number;
}

export interface IncidentListItem {
  readonly id: string;
  readonly title: string;
  readonly priority: Priority;
  readonly status: IncidentStatus;
  readonly handlerType: HandlerType;
  readonly controlMode: ControlMode;
  readonly detectedAt: string;
  readonly duration: string;
}

export interface IncidentFilters {
  readonly priority?: Priority;
  readonly status?: IncidentStatus;
  readonly handlerType?: HandlerType;
  readonly showResolved: boolean;
}

export interface IncidentList {
  readonly severityDistribution: SeverityDistribution;
  readonly incidents: ReadonlyArray<IncidentListItem>;
}

// ── Detail Types ──

export interface IncidentHeader {
  readonly id: string;
  readonly title: string;
  readonly priority: Priority;
  readonly status: IncidentStatus;
  readonly handlerType: HandlerType;
  readonly controlMode: ControlMode;
  readonly autonomyLevel: AutonomyLevel;
  readonly detectedAt: string;
  readonly acknowledgedAt: string | null;
  readonly resolvedAt: string | null;
  readonly duration: string;
}

export interface DiagnosisEntry {
  readonly timestamp: string;
  readonly text: string;
  readonly entryType: DiagnosisEntryType;
}

export interface RootCauseHypothesis {
  readonly hypothesis: string;
  readonly confidence: ConfidenceLevel;
}

export interface DiagnosisFeed {
  readonly entries: ReadonlyArray<DiagnosisEntry>;
  readonly rootCause: RootCauseHypothesis | null;
  readonly affectedComponents: ReadonlyArray<string>;
}

export interface SkillExecution {
  readonly skillName: string;
  readonly startTime: string;
  readonly endTime: string | null;
  readonly status: SkillExecutionStatus;
  readonly inputSummary: string;
  readonly outputSummary: string;
}

export interface SkillTimeline {
  readonly executions: ReadonlyArray<SkillExecution>;
}

export interface IncidentAction {
  readonly id: string;
  readonly description: string;
  readonly actionType: ActionType;
  readonly executionStatus: ActionExecutionStatus;
  readonly timestamp: string;
  readonly impactAssessment: string;
  readonly isRollbackable: boolean;
  readonly policyRef: string | null;
}

export interface IncidentActions {
  readonly actions: ReadonlyArray<IncidentAction>;
}

export interface GovernanceEntry {
  readonly actor: string;
  readonly timestamp: string;
  readonly actionTaken: GovernanceActionType;
  readonly reason: string | null;
  readonly policyRef: string | null;
}

export interface Governance {
  readonly entries: ReadonlyArray<GovernanceEntry>;
}

export interface SdlcChainLink {
  readonly artifactType: SdlcArtifactType;
  readonly artifactId: string;
  readonly artifactTitle: string;
  readonly routePath: string;
}

export interface SdlcChain {
  readonly links: ReadonlyArray<SdlcChainLink>;
}

export interface AiLearning {
  readonly rootCause: string;
  readonly patternIdentified: string;
  readonly preventionRecommendations: ReadonlyArray<string>;
  readonly knowledgeBaseEntryCreated: boolean;
}

// ── Aggregates ──

export interface IncidentDetail {
  readonly header: SectionResult<IncidentHeader>;
  readonly diagnosis: SectionResult<DiagnosisFeed>;
  readonly skillTimeline: SectionResult<SkillTimeline>;
  readonly actions: SectionResult<IncidentActions>;
  readonly governance: SectionResult<Governance>;
  readonly sdlcChain: SectionResult<SdlcChain>;
  readonly learning: SectionResult<AiLearning>;
}

// ── Action Results ──

export interface ActionApprovalResult {
  readonly actionId: string;
  readonly newStatus: ActionExecutionStatus;
  readonly governanceEntry: GovernanceEntry;
}
