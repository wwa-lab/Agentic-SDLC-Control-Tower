import { ApiError } from '@/shared/api/client';

export interface CodeBuildErrorDetails {
  readonly resetAt?: string;
  readonly hint?: string;
  readonly entityId?: string;
}

export class CodeBuildApiError extends ApiError {
  constructor(
    status: number,
    public readonly code: string,
    message?: string,
    public readonly details: CodeBuildErrorDetails = {},
  ) {
    super(status, code, message ?? code);
    this.name = 'CodeBuildApiError';
  }
}

export function isCodeBuildApiError(error: unknown): error is CodeBuildApiError {
  return error instanceof CodeBuildApiError;
}

