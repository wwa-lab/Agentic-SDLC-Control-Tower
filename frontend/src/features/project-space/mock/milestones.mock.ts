import type { MilestoneHub } from '../types/milestones';
import { resolveProjectMockSeed } from './catalog';

export function milestones(projectId: string): MilestoneHub {
  return resolveProjectMockSeed(projectId).milestones;
}
