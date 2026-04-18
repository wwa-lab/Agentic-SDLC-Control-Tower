import type { RiskRegistry } from '../types/risks';
import { resolveProjectMockSeed } from './catalog';

export function risks(projectId: string): RiskRegistry {
  return resolveProjectMockSeed(projectId).risks;
}
