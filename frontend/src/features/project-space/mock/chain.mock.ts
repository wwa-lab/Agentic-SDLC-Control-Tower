import type { SdlcChainState } from '../types/chain';
import { resolveProjectMockSeed } from './catalog';

export function chain(projectId: string): SdlcChainState {
  return resolveProjectMockSeed(projectId).chain;
}
