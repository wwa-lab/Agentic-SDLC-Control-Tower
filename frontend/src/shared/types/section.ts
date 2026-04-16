/**
 * Per-section envelope — allows individual sections to fail independently.
 * Shared across all feature modules (dashboard, incident, etc.).
 */
export interface SectionResult<T> {
  readonly data: T | null;
  readonly error: string | null;
}
