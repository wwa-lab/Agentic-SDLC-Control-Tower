import type {
  HealthAggregate,
  HealthFactorSeverity,
  ProjectLifecycleStage,
} from './enums';

export interface ProjectCounters {
  readonly activeSpecs: number;
  readonly openIncidents: number;
  readonly pendingApprovals: number;
  readonly criticalHighRisks: number;
}

export interface HealthFactor {
  readonly label: string;
  readonly severity: HealthFactorSeverity;
}

export interface MemberRef {
  readonly memberId: string;
  readonly displayName: string;
}

export interface ActiveMilestoneRef {
  readonly id: string;
  readonly label: string;
  readonly targetDate: string;
}

export interface ProjectSummary {
  readonly id: string;
  readonly name: string;
  readonly workspaceId: string;
  readonly workspaceName: string;
  readonly applicationId: string;
  readonly applicationName: string;
  readonly lifecycleStage: ProjectLifecycleStage;
  readonly healthAggregate: HealthAggregate;
  readonly healthFactors: ReadonlyArray<HealthFactor>;
  readonly pm: MemberRef;
  readonly techLead: MemberRef;
  readonly activeMilestone: ActiveMilestoneRef | null;
  readonly counters: ProjectCounters;
  readonly lastUpdatedAt: string;
  readonly teamSpaceLink: string;
}
