import type { ProjectSpaceAggregate } from '../types/aggregate';
import { createMockAggregate } from './catalog';

export function aggregate(projectId: string): ProjectSpaceAggregate {
  return createMockAggregate(projectId);
}
