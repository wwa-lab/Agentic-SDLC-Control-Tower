import type { ProjectSummary } from '../types/summary';
import { resolveProjectMockSeed } from './catalog';

export function summary(projectId: string): ProjectSummary {
  return resolveProjectMockSeed(projectId).summary;
}
