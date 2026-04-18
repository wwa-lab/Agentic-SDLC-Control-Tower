import type { LeadershipOwnership } from '../types/leadership';
import { resolveProjectMockSeed } from './catalog';

export function leadership(projectId: string): LeadershipOwnership {
  return resolveProjectMockSeed(projectId).leadership;
}
