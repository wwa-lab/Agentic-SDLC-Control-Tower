import { fetchJson, postJson } from '@/shared/api/client';
import type { RequirementList, RequirementDetail } from '../types/requirement';

export interface GenerationResult {
  readonly message: string;
  readonly executionId: string;
}

export const requirementApi = {
  async getRequirementList(): Promise<RequirementList> {
    return fetchJson<RequirementList>('/requirements');
  },

  async getRequirementDetail(id: string): Promise<RequirementDetail> {
    return fetchJson<RequirementDetail>(`/requirements/${id}`);
  },

  async triggerStoryGeneration(requirementId: string): Promise<GenerationResult> {
    return postJson<GenerationResult>(`/requirements/${requirementId}/generate-stories`);
  },

  async triggerSpecGeneration(storyId: string): Promise<GenerationResult> {
    return postJson<GenerationResult>(`/requirements/stories/${storyId}/generate-spec`);
  },

  async triggerAnalysis(requirementId: string): Promise<GenerationResult> {
    return postJson<GenerationResult>(`/requirements/${requirementId}/analyze`);
  },
};
