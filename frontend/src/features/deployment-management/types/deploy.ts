import type { SectionResult } from '@/shared/types/section';
import type { ApprovalDecision, DeployState, DeployStageState, DeployTrigger } from './enums';

export interface DeployHeader {
  readonly deployId: string;
  readonly releaseId: string;
  readonly releaseVersion: string;
  readonly applicationId: string;
  readonly environmentName: string;
  readonly jenkinsJobFullName: string;
  readonly jenkinsBuildNumber: number;
  readonly jenkinsBuildUrl: string;
  readonly trigger: DeployTrigger;
  readonly actor: string;
  readonly startedAt: string;
  readonly completedAt?: string;
  readonly durationSec?: number;
  readonly state: DeployState;
  readonly isCurrentRevision: boolean;
  readonly isRollback: boolean;
  readonly unresolvedFlag: boolean;
}

export interface DeployStageRow {
  readonly stageId: string;
  readonly name: string;
  readonly order: number;
  readonly state: DeployStageState;
  readonly startedAt?: string;
  readonly completedAt?: string;
  readonly durationSec?: number;
  readonly approverDisplayName?: string;
  readonly approvalDecision?: ApprovalDecision;
}

export interface ApprovalEvent {
  readonly approvalId: string;
  readonly stageId: string;
  readonly stageName: string;
  readonly approverDisplayName?: string;
  readonly approverMemberId?: string;
  readonly approverRole?: string;
  readonly decision: ApprovalDecision;
  readonly gatePolicyVersion: string;
  readonly rationale?: string;
  readonly decidedAt: string;
}

export interface DeployArtifactRefCard {
  readonly buildArtifactRef: { readonly sliceId: 'code-build'; readonly buildArtifactId: string };
  readonly buildArtifactResolved: boolean;
  readonly buildSummary?: {
    readonly pipelineName: string;
    readonly commitCount: number;
    readonly commitRangeHeadSha: string;
    readonly commitRangeBaseSha: string;
  };
}

export interface OpenIncidentContextDto {
  readonly applicationId: string;
  readonly environmentName: string;
  readonly deployId: string;
  readonly releaseVersion: string;
  readonly deployUrl: string;
  readonly summaryLine?: string;
}

export interface DeployDetailAggregate {
  readonly header: SectionResult<DeployHeader>;
  readonly stageTimeline: SectionResult<ReadonlyArray<DeployStageRow>>;
  readonly approvals: SectionResult<ReadonlyArray<ApprovalEvent>>;
  readonly artifactRef: SectionResult<DeployArtifactRefCard>;
  readonly openIncidentContext: OpenIncidentContextDto;
  readonly followedByRollback?: { readonly deployId: string; readonly deployUrl: string };
}
