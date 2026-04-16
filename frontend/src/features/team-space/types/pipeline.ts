import type { HealthAggregate, SdlcNodeKey } from './enums';

export interface PipelineCounters {
  readonly requirementsInflow7d: number;
  readonly storiesDecomposing: number;
  readonly specsGenerating: number;
  readonly specsInReview: number;
  readonly specsBlocked: number;
  readonly specsApprovedAwaitingDownstream: number;
}

export interface PipelineBlocker {
  readonly kind: 'SPEC_BLOCKED' | 'REQ_NO_STORY' | 'STORY_NO_SPEC';
  readonly targetId: string;
  readonly targetTitle: string;
  readonly ageDays: number;
  readonly filterDeeplink: string;
}

export interface ChainNodeHealth {
  readonly nodeKey: SdlcNodeKey;
  readonly health: HealthAggregate;
  readonly isExecutionHub: boolean;
}

export interface RequirementPipeline {
  readonly counters: PipelineCounters;
  readonly blockers: ReadonlyArray<PipelineBlocker>;
  readonly chain: ReadonlyArray<ChainNodeHealth>;
  readonly blockerThresholdDays: number;
}
