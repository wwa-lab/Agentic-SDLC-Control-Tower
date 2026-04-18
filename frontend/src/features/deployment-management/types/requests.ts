import type { AiReleaseNotes } from './release';
import type { AiRowStatus } from './enums';

export interface RegenerateReleaseNotesRequest {
  readonly releaseId: string;
}

export interface RegenerateReleaseNotesResponse {
  readonly releaseId: string;
  readonly status: AiRowStatus;
  readonly generatedAt?: string;
  readonly notes?: AiReleaseNotes;
}

export interface RegenerateDeploySummaryRequest {
  readonly deployId: string;
}

export interface RegenerateDeploySummaryResponse {
  readonly deployId: string;
  readonly status: AiRowStatus;
  readonly generatedAt?: string;
}

export interface RegenerateWorkspaceSummaryRequest {
  readonly workspaceId: string;
}

export interface RegenerateWorkspaceSummaryResponse {
  readonly workspaceId: string;
  readonly status: AiRowStatus;
  readonly generatedAt?: string;
}
