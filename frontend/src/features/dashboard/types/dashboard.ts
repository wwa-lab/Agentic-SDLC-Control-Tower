/**
 * Dashboard Type Definitions
 */

import type { SectionResult } from '@/shared/types/section';

// Re-export for backwards compatibility with existing dashboard imports
export type { SectionResult };

export interface DashboardSummary {
  readonly sdlcHealth: SectionResult<ReadonlyArray<SdlcStageHealth>>;
  readonly deliveryMetrics: SectionResult<DeliveryMetrics>;
  readonly aiParticipation: SectionResult<AiParticipation>;
  readonly qualityMetrics: SectionResult<QualityMetrics>;
  readonly stabilityMetrics: SectionResult<StabilityMetrics>;
  readonly governanceMetrics: SectionResult<GovernanceMetrics>;
  readonly recentActivity: SectionResult<RecentActivity>;
  readonly valueStory: SectionResult<ValueStory>;
}

export type SdlcStatus = 'healthy' | 'warning' | 'critical' | 'inactive';

export interface SdlcStageHealth {
  readonly key: string;
  readonly label: string;
  readonly status: SdlcStatus;
  readonly itemCount: number;
  readonly isHub: boolean;
  readonly routePath: string;
}

export type TrendDirection = 'up' | 'down' | 'stable';

export interface MetricValue {
  readonly label: string;
  readonly value: string;
  readonly trend: TrendDirection;
  readonly trendIsPositive: boolean;
}

export interface DeliveryMetrics {
  readonly leadTime: MetricValue;
  readonly deployFrequency: MetricValue;
  readonly iterationCompletion: MetricValue;
  readonly bottleneckStage: string | null;
}

export interface AiInvolvement {
  readonly stageKey: string;
  readonly involved: boolean;
  readonly actionsCount: number;
}

export interface AiParticipation {
  readonly usageRate: MetricValue;
  readonly adoptionRate: MetricValue;
  readonly autoExecSuccess: MetricValue;
  readonly timeSaved: MetricValue;
  readonly stageInvolvement: ReadonlyArray<AiInvolvement>;
}

export interface QualityMetrics {
  readonly buildSuccessRate: MetricValue;
  readonly testPassRate: MetricValue;
  readonly defectDensity: MetricValue;
  readonly specCoverage: MetricValue;
}

export interface StabilityMetrics {
  readonly activeIncidents: number;
  readonly criticalIncidents: number;
  readonly changeFailureRate: MetricValue;
  readonly mttr: MetricValue;
  readonly rollbackRate: MetricValue;
}

export interface GovernanceMetrics {
  readonly templateReuse: MetricValue;
  readonly configDrift: MetricValue;
  readonly auditCoverage: MetricValue;
  readonly policyHitRate: MetricValue;
}

export type ActorType = 'ai' | 'human';

export interface ActivityEntry {
  readonly id: string;
  readonly actor: string;
  readonly actorType: ActorType;
  readonly action: string;
  readonly stageKey: string;
  readonly timestamp: string; // ISO 8601
}

export interface RecentActivity {
  readonly entries: ReadonlyArray<ActivityEntry>;
  readonly total: number;
}

export interface ValueStoryMetric {
  readonly label: string;
  readonly value: string;
  readonly description: string;
}

export interface ValueStory {
  readonly headline: string;
  readonly metrics: ReadonlyArray<ValueStoryMetric>;
}
