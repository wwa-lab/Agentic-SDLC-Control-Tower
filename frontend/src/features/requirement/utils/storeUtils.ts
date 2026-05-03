import { ApiError } from '@/shared/api/client';
import type { SortField } from '../types/requirement';

export function toUserMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError && error.message) return error.message;
  if (error instanceof Error && error.message) return error.message;
  return fallback;
}

export function mapSortField(field: SortField): string {
  return field === 'recency' ? 'updatedAt' : field;
}

export const devLog = {
  error: (message: string, ...args: unknown[]) => {
    if (import.meta.env.DEV) console.error(message, ...args);
  },
  info: (message: string, ...args: unknown[]) => {
    if (import.meta.env.DEV) console.info(message, ...args);
  },
};
