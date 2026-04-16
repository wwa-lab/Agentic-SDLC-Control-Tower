import { fetchJson, postFormData, postJson } from '@/shared/api/client';
import type {
  PipelineProfile,
  RequirementDetail,
  RequirementDraft,
  RequirementImportStatus,
  RequirementList,
  RequirementListItem,
  RequirementSourceInput,
  SkillExecutionResult,
} from '../types/requirement';

export interface GenerationResult {
  readonly message: string;
  readonly executionId: string;
}

export interface RequirementListFilters {
  readonly status?: string;
  readonly priority?: string;
  readonly category?: string;
  readonly search?: string;
  readonly sortBy?: string;
  readonly sortDirection?: string;
}

interface NormalizeRequirementRequest {
  readonly rawInput: RequirementSourceInput;
  readonly profileId: string;
}

interface CreateRequirementRequest {
  readonly title: string;
  readonly priority: string;
  readonly category: string;
  readonly summary: string;
  readonly businessJustification: string;
  readonly acceptanceCriteria: ReadonlyArray<string>;
  readonly assumptions: ReadonlyArray<string>;
  readonly constraints: ReadonlyArray<string>;
  readonly sourceAttachment: RequirementSourceInput | null;
}

function buildQueryString(filters: RequirementListFilters): string {
  const params = new URLSearchParams();
  if (filters.status) params.set('status', filters.status);
  if (filters.priority) params.set('priority', filters.priority);
  if (filters.category) params.set('category', filters.category);
  if (filters.search) params.set('search', filters.search);
  if (filters.sortBy) params.set('sortBy', filters.sortBy);
  if (filters.sortDirection) params.set('sortDirection', filters.sortDirection);
  const query = params.toString();
  return query ? `?${query}` : '';
}

export const requirementApi = {
  async getRequirementList(filters: RequirementListFilters = {}): Promise<RequirementList> {
    return fetchJson<RequirementList>(`/requirements${buildQueryString(filters)}`);
  },

  async getRequirementDetail(id: string): Promise<RequirementDetail> {
    return fetchJson<RequirementDetail>(`/requirements/${id}`);
  },

  async getActiveProfile(): Promise<PipelineProfile> {
    return fetchJson<PipelineProfile>('/pipeline-profiles/active');
  },

  async triggerStoryGeneration(requirementId: string): Promise<GenerationResult> {
    return postJson<GenerationResult>(`/requirements/${requirementId}/generate-stories`);
  },

  async triggerSpecGeneration(requirementId: string, storyIds: ReadonlyArray<string>): Promise<GenerationResult> {
    return postJson<GenerationResult>(`/requirements/${requirementId}/generate-spec`, { storyIds });
  },

  async triggerLegacySpecGeneration(storyId: string): Promise<GenerationResult> {
    return postJson<GenerationResult>(`/requirements/stories/${storyId}/generate-spec`);
  },

  async triggerAnalysis(requirementId: string): Promise<GenerationResult> {
    return postJson<GenerationResult>(`/requirements/${requirementId}/analyze`);
  },

  async invokeProfileSkill(requirementId: string, skillId: string): Promise<SkillExecutionResult> {
    return postJson<SkillExecutionResult>(`/requirements/${requirementId}/invoke-skill`, { skillId });
  },

  async normalizeRequirement(request: NormalizeRequirementRequest): Promise<RequirementDraft> {
    return postJson<RequirementDraft>('/requirements/normalize', request);
  },

  async normalizeUploadedRequirement(
    files: ReadonlyArray<File>,
    kbName: string,
    profileId?: string
  ): Promise<RequirementDraft> {
    const formData = new FormData();
    for (const file of files) {
      formData.append('file', file);
    }
    formData.set('kb_name', kbName);
    if (profileId) {
      formData.set('profileId', profileId);
    }
    return postFormData<RequirementDraft>('/requirements/normalize', formData);
  },

  async startRequirementImport(
    files: ReadonlyArray<File>,
    kbName: string,
    profileId?: string
  ): Promise<RequirementImportStatus> {
    const formData = new FormData();
    for (const file of files) {
      formData.append('file', file);
    }
    formData.set('kb_name', kbName);
    if (profileId) {
      formData.set('profileId', profileId);
    }
    return postFormData<RequirementImportStatus>('/requirements/imports', formData);
  },

  async getRequirementImportStatus(importId: string): Promise<RequirementImportStatus> {
    return fetchJson<RequirementImportStatus>(`/requirements/imports/${importId}`);
  },

  async createRequirement(request: CreateRequirementRequest): Promise<RequirementListItem> {
    return postJson<RequirementListItem>('/requirements', request);
  },
};
