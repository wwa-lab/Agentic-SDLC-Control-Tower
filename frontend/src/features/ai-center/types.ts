import type { SectionResult } from '@/shared/types/section';

export type { SectionResult };

export type SkillStatus = 'active' | 'beta' | 'deprecated';

export type AutonomyLevel = 'L0-Manual' | 'L1-Assist' | 'L2-Auto-with-approval' | 'L3-Auto';

export type SkillCategory = 'delivery' | 'runtime';

export type ExecutionStatus =
  | 'running'
  | 'succeeded'
  | 'failed'
  | 'pending_approval'
  | 'rejected'
  | 'rolled_back';

export type TriggeredByType = 'ai' | 'human' | 'system';

export type TriggerSourceType = 'page' | 'schedule' | 'webhook' | 'manual';

export type SdlcStageKey =
  | 'requirement'
  | 'user-story'
  | 'spec'
  | 'architecture'
  | 'design'
  | 'tasks'
  | 'code'
  | 'test'
  | 'deploy'
  | 'incident'
  | 'learning';

export interface Skill {
  id: string;
  key: string;
  name: string;
  category: SkillCategory;
  subCategory?: string;
  status: SkillStatus;
  defaultAutonomy: AutonomyLevel;
  owner: string;
  description: string;
  stages: SdlcStageKey[];
  lastExecutedAt: string | null;
  successRate30d: number | null;
  version: number;
}

export interface Policy {
  skillKey: string;
  autonomyLevel: AutonomyLevel;
  approvalRequiredActions: string[];
  authorizedApproverRoles: string[];
  riskThresholds: Record<string, unknown>;
  lastChangedAt: string;
  lastChangedBy: string;
}

export interface AggregateMetrics {
  successRate: number;
  avgDurationMs: number;
  adoptionTrend: 'up' | 'down' | 'flat';
  totalRuns30d: number;
}

export interface SkillDetail extends Skill {
  inputContract: string;
  outputContract: string;
  policy: Policy;
  recentRuns: Run[];
  aggregateMetrics: AggregateMetrics;
}

export interface Run {
  id: string;
  skillKey: string;
  skillName: string;
  status: ExecutionStatus;
  triggerSourceType: TriggerSourceType;
  triggerSourcePage: string | null;
  triggerSourceUrl: string | null;
  triggeredBy: string;
  triggeredByType: TriggeredByType;
  startedAt: string;
  endedAt: string | null;
  durationMs: number | null;
  outcomeSummary: string;
  auditRecordId: string | null;
}

export interface EvidenceLink {
  title: string;
  type: string;
  sourceSystem: string;
  url: string;
}

export interface RunStep {
  ordinal: number;
  name: string;
  status: ExecutionStatus;
  startedAt: string;
  endedAt: string | null;
  durationMs: number | null;
  note?: string;
}

export interface PolicyTrailEntry {
  rule: string;
  decision: 'allowed' | 'held-for-approval' | 'denied' | 'bypassed';
  at: string;
  note?: string;
}

export interface RunDetail extends Run {
  inputSummary: Record<string, unknown>;
  outputSummary: Record<string, unknown>;
  stepBreakdown: RunStep[];
  policyTrail: PolicyTrailEntry[];
  evidenceLinks: EvidenceLink[];
  autonomyLevel: AutonomyLevel;
  timeSavedMinutes: number;
}

export interface MetricValue {
  value: number;
  unit: '%' | 'hours' | 'count';
  delta: number;
  trend: 'up' | 'down' | 'flat';
  isPositive: boolean;
}

export interface MetricsSummary {
  window: '30d' | '7d' | '24h';
  aiUsageRate: SectionResult<MetricValue>;
  adoptionRate: SectionResult<MetricValue>;
  autoExecSuccessRate: SectionResult<MetricValue>;
  timeSavedHours: SectionResult<MetricValue>;
  stageCoverageCount: SectionResult<MetricValue>;
}

export interface StageCoverageEntry {
  stageKey: SdlcStageKey;
  stageLabel: string;
  activeSkillCount: number;
  covered: boolean;
}

export type StageCoverage = StageCoverageEntry[];

export interface Page<T> {
  items: T[];
  page: number;
  size: number;
  total: number;
  hasMore: boolean;
}

export interface ApiResponse<T> {
  data: T | null;
  error: string | null;
}

export interface SkillDetailResponse {
  skill: Skill;
  inputContract: string;
  outputContract: string;
  policy: Policy;
  recentRuns: Run[];
  aggregateMetrics: AggregateMetrics;
}

export interface RunDetailResponse {
  run: Run;
  inputSummary: Record<string, unknown>;
  outputSummary: Record<string, unknown>;
  stepBreakdown: RunStep[];
  policyTrail: PolicyTrailEntry[];
  evidenceLinks: EvidenceLink[];
  autonomyLevel: AutonomyLevel;
  timeSavedMinutes: number;
}

export interface StageCoverageResponse {
  entries: StageCoverage;
}

export type AiCenterWindow = '30d' | '7d' | '24h';

export interface RunQuery {
  skillKey?: string[];
  status?: ExecutionStatus[];
  triggerSourcePage?: string;
  startedAfter?: string;
  startedBefore?: string;
  triggeredByType?: TriggeredByType;
  page?: number;
  size?: number;
}

export interface SkillFilterState {
  category: SkillCategory[];
  status: SkillStatus[];
  autonomy: AutonomyLevel[];
  owner: string[];
  search: string;
  sortBy: 'lastExecutedAt' | 'name' | 'successRate30d';
}

export interface RunFilterFormState {
  skillKey: string[];
  status: ExecutionStatus[];
  triggerSourcePage: string;
  startedAfter: string;
  startedBefore: string;
  triggeredByType: TriggeredByType | '';
}

export interface LoadableState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

export type CardState = 'normal' | 'loading' | 'empty' | 'error';
