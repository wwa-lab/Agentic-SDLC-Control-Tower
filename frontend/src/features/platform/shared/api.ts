import { fetchJson } from '@/shared/api/client';
import { PC_USE_MOCK } from './constants';

const BASE = '/platform';

export async function pcGet<T>(path: string, params?: Record<string, string>): Promise<T> {
  const qs = params ? '?' + new URLSearchParams(params).toString() : '';
  return fetchJson<T>(`${BASE}${path}${qs}`);
}

export async function pcPost<T>(path: string, body?: unknown): Promise<T> {
  const res = await fetch(`/api/v1${BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body != null ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `POST ${path} failed: ${res.status}`);
  }
  if (res.status === 204) return undefined as T;
  const envelope = await res.json();
  if (envelope.error) throw new Error(envelope.error.message ?? envelope.error);
  return envelope.data as T;
}

export async function pcDelete(path: string): Promise<void> {
  const res = await fetch(`/api/v1${BASE}${path}`, { method: 'DELETE' });
  if (!res.ok && res.status !== 204) {
    const text = await res.text();
    throw new Error(text || `DELETE ${path} failed: ${res.status}`);
  }
}

const MOCK_LATENCY_MS = import.meta.env.MODE === 'test' ? 0 : 80;

export async function withMockLatency<T>(fn: () => T): Promise<T> {
  if (MOCK_LATENCY_MS > 0) {
    await new Promise(r => setTimeout(r, MOCK_LATENCY_MS));
  }
  return fn();
}

export { PC_USE_MOCK };
