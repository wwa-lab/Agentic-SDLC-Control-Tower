import type { SectionResult } from '@/shared/types/section';

export const PM_DEFAULT_WORKSPACE_ID = 'ws-default-001';
export const PM_DEFAULT_PROJECT_ID = 'proj-42';
export const PM_PROJECT_ID_PATTERN = /^proj-[a-z0-9-]+$/;
export const PM_WORKSPACE_ID_PATTERN = /^ws-[a-z0-9-]+$/;

export type HealthIndicator = 'GREEN' | 'YELLOW' | 'RED' | 'UNKNOWN';
export type MilestoneStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'AT_RISK' | 'COMPLETED' | 'SLIPPED' | 'ARCHIVED';
export type SlippageRiskScore = 'LOW' | 'MEDIUM' | 'HIGH' | 'NONE';
export type RiskSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
export type RiskCategory = 'TECHNICAL' | 'SECURITY' | 'DELIVERY' | 'DEPENDENCY' | 'GOVERNANCE';
export type RiskState = 'IDENTIFIED' | 'ACKNOWLEDGED' | 'MITIGATING' | 'RESOLVED' | 'ESCALATED';
export type DependencyDirection = 'UPSTREAM' | 'DOWNSTREAM';
export type DependencyRelationship = 'API' | 'DATA' | 'SCHEDULE' | 'SLA';
export type DependencyResolutionState = 'PROPOSED' | 'NEGOTIATING' | 'APPROVED' | 'REJECTED' | 'AT_RISK' | 'RESOLVED';
export type AiSuggestionKind = 'SLIPPAGE' | 'REBALANCE' | 'MITIGATION' | 'DEP_RESOLUTION';
export type AiSuggestionState = 'PENDING' | 'ACCEPTED' | 'DISMISSED';
export type CadenceTrend = 'UP' | 'DOWN' | 'FLAT';

export interface PortfolioSummary {
  readonly workspaceId: string;
  readonly activeProjects: number;
  readonly redProjects: number;
  readonly atRiskOrSlippedMilestones: number;
  readonly criticalRisks: number;
  readonly blockedDependencies: number;
  readonly pendingApprovals: number;
  readonly aiPendingReview: number;
  readonly lastRefreshedAt: string;
}

export interface PortfolioHeatmapCell {
  readonly windowLabel: string;
  readonly dominantStatus: HealthIndicator;
}

export interface PortfolioHeatmapRow {
  readonly projectId: string;
  readonly projectName: string;
  readonly cells: ReadonlyArray<PortfolioHeatmapCell>;
}

export interface PortfolioHeatmap {
  readonly window: string;
  readonly columns: ReadonlyArray<string>;
  readonly rows: ReadonlyArray<PortfolioHeatmapRow>;
}

export interface PortfolioCapacityProject {
  readonly projectId: string;
  readonly projectName: string;
}

export interface PortfolioCapacityCell {
  readonly projectId: string;
  readonly percent: number;
}

export interface PortfolioCapacityRow {
  readonly memberId: string;
  readonly displayName: string;
  readonly totalPercent: number;
  readonly flag: string;
  readonly cells: ReadonlyArray<PortfolioCapacityCell>;
}

export interface PortfolioCapacity {
  readonly projects: ReadonlyArray<PortfolioCapacityProject>;
  readonly rows: ReadonlyArray<PortfolioCapacityRow>;
  readonly underThreshold: number;
}

export interface SeverityCategoryCount {
  readonly severity: RiskSeverity;
  readonly category: RiskCategory;
  readonly count: number;
}

export interface DependencyBottleneck {
  readonly dependencyId: string;
  readonly sourceProjectId: string;
  readonly sourceProjectName: string;
  readonly targetProjectId: string | null;
  readonly targetDescriptor: string;
  readonly external: boolean;
  readonly relationship: DependencyRelationship;
  readonly blockerReason: string | null;
  readonly ownerTeam: string;
  readonly daysBlocked: number;
  readonly aiProposalId: string | null;
}

export interface CadenceMetric {
  readonly key: string;
  readonly window: string;
  readonly value: number;
  readonly deltaAbs: number;
  readonly trend: CadenceTrend;
}

export interface CapacityMilestoneRef {
  readonly id: string;
  readonly label: string;
  readonly ordering: number;
}

export interface CapacityMemberRef {
  readonly id: string;
  readonly displayName: string;
  readonly hasBackup: boolean;
  readonly onCall: boolean;
}

export interface CapacityCell {
  readonly id: string;
  readonly memberId: string;
  readonly milestoneId: string;
  readonly percent: number;
  readonly justification: string | null;
  readonly windowStart: string;
  readonly windowEnd: string;
  readonly planRevision: number;
}

export interface PlanCapacityMatrix {
  readonly milestones: ReadonlyArray<CapacityMilestoneRef>;
  readonly members: ReadonlyArray<CapacityMemberRef>;
  readonly cells: ReadonlyArray<CapacityCell>;
  readonly rowTotals: Readonly<Record<string, number>>;
  readonly columnTotals: Readonly<Record<string, number>>;
  readonly underThreshold: number;
  readonly planRevision: number;
}

export interface RiskItem {
  readonly id: string;
  readonly projectId: string;
  readonly title: string;
  readonly severity: RiskSeverity;
  readonly category: RiskCategory;
  readonly state: RiskState;
  readonly ownerMemberId: string | null;
  readonly ownerDisplayName: string | null;
  readonly mitigationNote: string | null;
  readonly resolutionNote: string | null;
  readonly linkedIncidentId: string | null;
  readonly ageDays: number;
  readonly detectedAt: string;
  readonly resolvedAt: string | null;
  readonly planRevision: number;
}

export interface PortfolioRiskConcentration {
  readonly topRisks: ReadonlyArray<RiskItem>;
  readonly severityCategoryHeatmap: ReadonlyArray<SeverityCategoryCount>;
}

export interface PlanHeaderMilestone {
  readonly id: string;
  readonly label: string;
  readonly targetDate: string;
}

export interface PlanHeader {
  readonly projectId: string;
  readonly projectName: string;
  readonly workspaceId: string;
  readonly workspaceName: string;
  readonly applicationId: string;
  readonly applicationName: string;
  readonly lifecycleStage: string;
  readonly planHealth: HealthIndicator;
  readonly planHealthFactors: ReadonlyArray<string>;
  readonly nextMilestone: PlanHeaderMilestone | null;
  readonly pmMemberId: string;
  readonly pmDisplayName: string;
  readonly pmBackupMemberId: string | null;
  readonly autonomyLevel: string;
  readonly lastUpdatedAt: string;
  readonly planRevision: number;
}

export interface MilestoneSlippageFactor {
  readonly label: string;
  readonly evidence: string;
}

export interface MilestoneSlippage {
  readonly score: SlippageRiskScore;
  readonly factors: ReadonlyArray<MilestoneSlippageFactor>;
  readonly computedAt: string;
}

export interface Milestone {
  readonly id: string;
  readonly projectId: string;
  readonly label: string;
  readonly description: string | null;
  readonly targetDate: string;
  readonly status: MilestoneStatus;
  readonly percentComplete: number | null;
  readonly ownerMemberId: string | null;
  readonly ownerDisplayName: string | null;
  readonly slippageReason: string | null;
  readonly ordering: number;
  readonly slippage: MilestoneSlippage;
  readonly planRevision: number;
  readonly createdAt: string;
  readonly completedAt: string | null;
  readonly archivedAt: string | null;
}

export interface Dependency {
  readonly id: string;
  readonly projectId: string;
  readonly targetRef: string;
  readonly targetProjectId: string | null;
  readonly targetDescriptor: string;
  readonly external: boolean;
  readonly direction: DependencyDirection;
  readonly relationship: DependencyRelationship;
  readonly ownerTeam: string;
  readonly health: HealthIndicator;
  readonly resolutionState: DependencyResolutionState;
  readonly blockerReason: string | null;
  readonly contractCommitment: string | null;
  readonly rejectionReason: string | null;
  readonly counterSignatureMemberId: string | null;
  readonly counterSignedAt: string | null;
  readonly daysBlocked: number;
  readonly planRevision: number;
}

export interface ProgressNode {
  readonly node: string;
  readonly throughput: number;
  readonly priorThroughput: number;
  readonly health: HealthIndicator;
  readonly slipped: boolean;
  readonly deepLink: string;
}

export interface ChangeLogEntry {
  readonly id: string;
  readonly projectId: string;
  readonly actorType: 'HUMAN' | 'AI';
  readonly actorMemberId: string | null;
  readonly actorDisplayName: string | null;
  readonly skillExecutionId: string | null;
  readonly action: string;
  readonly targetType: string;
  readonly targetId: string;
  readonly beforeJson: string | null;
  readonly afterJson: string | null;
  readonly reason: string | null;
  readonly correlationId: string;
  readonly auditLinkId: string;
  readonly at: string;
}

export interface ChangeLogPage {
  readonly entries: ReadonlyArray<ChangeLogEntry>;
  readonly page: number;
  readonly total: number;
}

export interface AiSuggestion {
  readonly id: string;
  readonly projectId: string;
  readonly kind: AiSuggestionKind;
  readonly targetType: string;
  readonly targetId: string;
  readonly payload: string;
  readonly confidence: number;
  readonly state: AiSuggestionState;
  readonly skillExecutionId: string | null;
  readonly suppressionUntil: string | null;
  readonly createdAt: string;
  readonly resolvedAt: string | null;
}

export interface AiSuggestionActionResult {
  readonly suggestion: AiSuggestion;
  readonly auditLinkId: string;
}

export interface PortfolioAggregate {
  readonly summary: SectionResult<PortfolioSummary>;
  readonly heatmap: SectionResult<PortfolioHeatmap>;
  readonly capacity: SectionResult<PortfolioCapacity>;
  readonly risks: SectionResult<PortfolioRiskConcentration>;
  readonly bottlenecks: SectionResult<ReadonlyArray<DependencyBottleneck>>;
  readonly cadence: SectionResult<ReadonlyArray<CadenceMetric>>;
}

export interface PlanAggregate {
  readonly header: SectionResult<PlanHeader>;
  readonly milestones: SectionResult<ReadonlyArray<Milestone>>;
  readonly capacity: SectionResult<PlanCapacityMatrix>;
  readonly risks: SectionResult<ReadonlyArray<RiskItem>>;
  readonly dependencies: SectionResult<ReadonlyArray<Dependency>>;
  readonly progress: SectionResult<ReadonlyArray<ProgressNode>>;
  readonly changeLog: SectionResult<ChangeLogPage>;
  readonly aiSuggestions: SectionResult<ReadonlyArray<AiSuggestion>>;
}

export const PM_PORTFOLIO_CARD_KEYS = [
  'summary',
  'heatmap',
  'capacity',
  'risks',
  'bottlenecks',
  'cadence',
] as const;

export const PM_PLAN_CARD_KEYS = [
  'header',
  'milestones',
  'capacity',
  'risks',
  'dependencies',
  'progress',
  'changeLog',
  'aiSuggestions',
] as const;

export type ProjectManagementPortfolioCardKey = typeof PM_PORTFOLIO_CARD_KEYS[number];
export type ProjectManagementPlanCardKey = typeof PM_PLAN_CARD_KEYS[number];

export type PortfolioSectionMap = PortfolioAggregate;
export type PlanSectionMap = PlanAggregate;

export interface TransitionMilestoneRequest {
  readonly to: MilestoneStatus;
  readonly slippageReason?: string | null;
  readonly newTargetDate?: string | null;
  readonly planRevision: number;
}

export interface CapacityEditRequest {
  readonly memberId: string;
  readonly milestoneId: string;
  readonly percent: number;
  readonly justification?: string | null;
  readonly planRevision: number;
}

export interface SaveCapacityBatchRequest {
  readonly edits: ReadonlyArray<CapacityEditRequest>;
}

export interface TransitionRiskRequest {
  readonly to: RiskState;
  readonly mitigationNote?: string | null;
  readonly resolutionNote?: string | null;
  readonly linkedIncidentId?: string | null;
  readonly planRevision: number;
}

export interface TransitionDependencyRequest {
  readonly to: DependencyResolutionState;
  readonly rejectionReason?: string | null;
  readonly contractCommitment?: string | null;
  readonly planRevision: number;
}

export interface CounterSignDependencyRequest {
  readonly planRevision: number;
}

export interface DismissAiSuggestionRequest {
  readonly reason?: string | null;
}
