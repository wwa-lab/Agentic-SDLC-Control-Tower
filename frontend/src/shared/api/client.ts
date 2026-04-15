const API_BASE = '/api/v1';

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly statusText: string,
    message?: string
  ) {
    super(message ?? `API ${status}: ${statusText}`);
    this.name = 'ApiError';
  }

  get isUnauthorized(): boolean { return this.status === 401; }
  get isForbidden(): boolean { return this.status === 403; }
  get isNotFound(): boolean { return this.status === 404; }
  get isServerError(): boolean { return this.status >= 500; }
}

/**
 * Backend returns { data: T, error: string | null }.
 * This unwraps the envelope and throws on error.
 */
interface ApiEnvelope<T> {
  data: T | null;
  error: string | null;
}

export async function fetchJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`);
  if (!response.ok) {
    throw new ApiError(response.status, response.statusText);
  }
  const envelope: ApiEnvelope<T> = await response.json();
  if (envelope.error) {
    throw new ApiError(response.status, envelope.error, envelope.error);
  }
  return envelope.data as T;
}
