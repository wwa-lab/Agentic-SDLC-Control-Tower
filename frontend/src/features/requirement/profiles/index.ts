import type { PipelineProfile } from '../types/requirement';
import { STANDARD_SDD_PROFILE } from './standardSddProfile';
import { IBM_I_PROFILE } from './ibmIProfile';

const PROFILES: Record<string, PipelineProfile> = {
  'standard-sdd': STANDARD_SDD_PROFILE,
  'ibm-i': IBM_I_PROFILE,
};

/**
 * Resolve the active pipeline profile for a workspace.
 * V1: returns hardcoded profile based on workspace config.
 * Future: will call backend API for workspace-specific profile resolution.
 */
export function getActiveProfile(_workspaceId?: string): PipelineProfile {
  // V1: default to Standard SDD for all workspaces
  return STANDARD_SDD_PROFILE;
}

export function getProfileById(profileId: string): PipelineProfile | undefined {
  return PROFILES[profileId];
}

export function getAllProfiles(): ReadonlyArray<PipelineProfile> {
  return Object.values(PROFILES);
}

export { STANDARD_SDD_PROFILE, IBM_I_PROFILE };
