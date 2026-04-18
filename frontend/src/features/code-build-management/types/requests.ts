import type { SectionResult } from '@/shared/types/section';
import type { AiPrReviewPayload } from './pr';
import type { AiTriagePayload, RunRerunState } from './run';

export interface RegenerateAiPrReviewRequest {
  readonly prevHeadSha: string;
  readonly reason?: string;
}

export interface RegenerateAiPrReviewResponse {
  readonly aiReview: SectionResult<AiPrReviewPayload>;
  readonly message: string;
  readonly errorCode?: string | null;
}

export interface RegenerateAiTriageRequest {
  readonly stepIds?: ReadonlyArray<string>;
  readonly reason?: string;
}

export interface RegenerateAiTriageResponse {
  readonly aiTriage: SectionResult<AiTriagePayload>;
  readonly message: string;
  readonly errorCode?: string | null;
}

export interface RerunRunRequest {
  readonly reason?: string;
}

export interface RerunRunResponse {
  readonly rerun: SectionResult<RunRerunState>;
  readonly message: string;
  readonly errorCode?: string | null;
}

