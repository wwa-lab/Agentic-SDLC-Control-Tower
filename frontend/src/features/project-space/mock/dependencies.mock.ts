import type { DependencyMap } from '../types/dependencies';
import { resolveProjectMockSeed } from './catalog';

export function dependencies(projectId: string): DependencyMap {
  return resolveProjectMockSeed(projectId).dependencies;
}
