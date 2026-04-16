export type HealthAggregate = 'GREEN' | 'YELLOW' | 'RED' | 'UNKNOWN';

export type RiskSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export type RiskCategory =
  | 'PROJECT'
  | 'DEPENDENCY'
  | 'APPROVAL'
  | 'CONFIG_DRIFT'
  | 'INCIDENT';

export type ProjectHealthStratum =
  | 'HEALTHY'
  | 'AT_RISK'
  | 'CRITICAL'
  | 'ARCHIVED';

export type TrendDirection = 'UP' | 'DOWN' | 'FLAT';

export type CoverageGapKind =
  | 'ONCALL_GAP'
  | 'ROLE_UNFILLED'
  | 'BACKUP_MISSING';

export type AiAutonomyLevel =
  | 'SUGGEST_ONLY'
  | 'HUMAN_IN_LOOP'
  | 'AUTONOMOUS_AUDITED';

export type OperatingMode =
  | 'STANDARD'
  | 'HIGH_GOVERNANCE'
  | 'FAST_PATH';

export type ApprovalMode =
  | 'AUTO'
  | 'REVIEWER_REQUIRED'
  | 'MULTI_APPROVER';

export type TemplateKind =
  | 'PAGE'
  | 'POLICY'
  | 'WORKFLOW'
  | 'SKILL_PACK'
  | 'AI_DEFAULT';

export type MetricKey =
  | 'delivery.cycleTime'
  | 'delivery.throughput'
  | 'quality.defectRate'
  | 'quality.testPassRate'
  | 'stability.mttr'
  | 'stability.incidentFrequency'
  | 'governance.approvalCoverage'
  | 'governance.auditCompleteness'
  | 'ai.participationRate'
  | 'ai.acceptanceRate';

export type OncallStatus = 'ON' | 'OFF' | 'UPCOMING' | 'NONE';

export type LifecycleStage =
  | 'DISCOVERY'
  | 'DELIVERY'
  | 'STEADY_STATE'
  | 'RETIRING';

export type MetricUnit = 'DAYS' | 'PERCENT' | 'COUNT' | 'HOURS';

export type SdlcNodeKey =
  | 'REQUIREMENT'
  | 'USER_STORY'
  | 'SPEC'
  | 'ARCHITECTURE'
  | 'DESIGN'
  | 'TASKS'
  | 'CODE'
  | 'TEST'
  | 'DEPLOY'
  | 'INCIDENT'
  | 'LEARNING';

export type AccountableArea = 'DELIVERY' | 'APPROVAL' | 'INCIDENT' | 'GOVERNANCE';
