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

async function readErrorMessage(response: Response): Promise<string | undefined> {
  const body = await response.text();
  if (!body) {
    return undefined;
  }

  try {
    const envelope = JSON.parse(body) as ApiEnvelope<unknown>;
    if (typeof envelope.error === 'string' && envelope.error.trim()) {
      return envelope.error;
    }
  } catch {
    return body;
  }

  return body;
}

export async function fetchJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`);
  if (!response.ok) {
    throw new ApiError(response.status, response.statusText, await readErrorMessage(response));
  }
  const envelope: ApiEnvelope<T> = await response.json();
  if (envelope.error) {
    throw new ApiError(response.status, envelope.error, envelope.error);
  }
  return envelope.data as T;
}

export async function postJson<T>(path: string, body?: unknown): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body != null ? JSON.stringify(body) : undefined,
  });
  if (!response.ok) {
    throw new ApiError(response.status, response.statusText, await readErrorMessage(response));
  }
  const envelope: ApiEnvelope<T> = await response.json();
  if (envelope.error) {
    throw new ApiError(response.status, envelope.error, envelope.error);
  }
  return envelope.data as T;
}

export async function postFormData<T>(path: string, body: FormData): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    body,
  });
  if (!response.ok) {
    throw new ApiError(response.status, response.statusText, await readErrorMessage(response));
  }
  const envelope: ApiEnvelope<T> = await response.json();
  if (envelope.error) {
    throw new ApiError(response.status, envelope.error, envelope.error);
  }
  return envelope.data as T;
}
