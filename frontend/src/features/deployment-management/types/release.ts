import type { SectionResult } from '@/shared/types/section';
import type { AiRowStatus, DeployState, ReleaseState } from './enums';
import type { StoryChip } from './traceability';

export interface ReleaseHeader {
  readonly releaseId: string;
  readonly releaseVersion: string;
  readonly applicationId: string;
  readonly workspaceId: string;
  readonly state: ReleaseState;
  readonly createdAt: string;
  readonly createdBy?: string;
  readonly buildArtifactRef: { readonly sliceId: 'code-build'; readonly buildArtifactId: string };
  readonly buildArtifactResolved: boolean;
  readonly buildArtifactSha?: string;
  readonly jenkinsSourceUrl: string;
}

export interface ReleaseCommitRow {
  readonly sha: string;
  readonly shortSha: string;
  readonly author: string;
  readonly message: string;
  readonly committedAt: string;
  readonly storyIds: ReadonlyArray<string>;
}

export interface ReleaseDeployRow {
  readonly deployId: string;
  readonly environmentName: string;
  readonly state: DeployState;
  readonly startedAt: string;
  readonly durationSec?: number;
  readonly approverDisplayName?: string;
  readonly isCurrentRevision: boolean;
  readonly isRollback: boolean;
}

export interface AiReleaseNotes {
  readonly status: AiRowStatus;
  readonly keyedOnReleaseId: string;
  readonly skillVersion?: string;
  readonly generatedAt?: string;
  readonly narrative?: string;
  readonly diffNarrative?: string;
  readonly riskHint?: 'LOW' | 'MEDIUM' | 'HIGH';
  readonly evidence?: ReadonlyArray<{ readonly kind: 'commit' | 'story'; readonly id: string; readonly label?: string }>;
  readonly error?: { readonly code: string; readonly message: string };
}

export interface ReleaseDetailAggregate {
  readonly header: SectionResult<ReleaseHeader>;
  readonly commits: SectionResult<ReadonlyArray<ReleaseCommitRow>>;
  readonly linkedStories: SectionResult<ReadonlyArray<StoryChip>>;
  readonly deploys: SectionResult<ReadonlyArray<ReleaseDeployRow>>;
  readonly aiNotes: SectionResult<AiReleaseNotes>;
  readonly capNotice?: { readonly kind: 'COMMIT_RANGE_CAP'; readonly appliedCommitCap: 100 };
}
