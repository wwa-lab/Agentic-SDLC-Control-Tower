import type { EnvironmentMatrix } from '../types/environments';
import { resolveProjectMockSeed } from './catalog';

export function environments(projectId: string): EnvironmentMatrix {
  return resolveProjectMockSeed(projectId).environments;
}
