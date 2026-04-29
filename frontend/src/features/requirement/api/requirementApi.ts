import { fetchJson, postFormData, postJson } from '@/shared/api/client';
import type {
  PipelineProfile,
  AgentRun,
  DocumentReview,
  DocumentQualityGate,
  GraphFilters,
  GraphImpact,
  GraphImpactRequest,
  GraphNodeDetail,
  GraphProviderHealth,
  KnowledgeGraph,
  RequirementTraceability,
  RequirementDetail,
  RequirementDraft,
  RequirementImportStatus,
  RequirementList,
  RequirementListItem,
  RequirementSourceInput,
  SddDocumentContent,
  SddDocumentIndex,
  SkillExecutionResult,
  SourceReference,
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

function buildProfileQuery(profileId?: string): string {
  if (!profileId) return '';
  const params = new URLSearchParams({ profileId });
  return `?${params.toString()}`;
}

function buildGraphQuery(filters: GraphFilters = {}): string {
  const params = new URLSearchParams();
  if (filters.workspaceId) params.set('workspaceId', filters.workspaceId);
  if (filters.applicationId) params.set('applicationId', filters.applicationId);
  if (filters.snowGroup) params.set('snowGroup', filters.snowGroup);
  if (filters.projectId) params.set('projectId', filters.projectId);
  if (filters.profileId) params.set('profileId', filters.profileId);
  if (filters.branch) params.set('branch', filters.branch);
  if (filters.requirementId) params.set('requirementId', filters.requirementId);
  if (filters.nodeKinds?.length) params.set('nodeKinds', filters.nodeKinds.join(','));
  if (filters.includeIssues != null) params.set('includeIssues', String(filters.includeIssues));
  if (filters.includeSuggestions != null) params.set('includeSuggestions', String(filters.includeSuggestions));
  if (filters.limit != null) params.set('limit', String(filters.limit));
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

  async getSourceReferences(requirementId: string): Promise<ReadonlyArray<SourceReference>> {
    return fetchJson<ReadonlyArray<SourceReference>>(`/requirements/${requirementId}/sources`);
  },

  async createSourceReference(requirementId: string, request: { sourceType: string; url: string; title?: string; externalId?: string }): Promise<SourceReference> {
    return postJson<SourceReference>(`/requirements/${requirementId}/sources`, request);
  },

  async refreshSourceReference(sourceId: string): Promise<SourceReference> {
    return postJson<SourceReference>(`/requirements/sources/${sourceId}/refresh`);
  },

  async getSddDocuments(requirementId: string, profileId?: string): Promise<SddDocumentIndex> {
    return fetchJson<SddDocumentIndex>(`/requirements/${requirementId}/sdd-documents${buildProfileQuery(profileId)}`);
  },

  async refreshSddDocuments(requirementId: string, profileId?: string): Promise<SddDocumentIndex> {
    return postJson<SddDocumentIndex>(`/requirements/${requirementId}/sdd-documents/refresh${buildProfileQuery(profileId)}`);
  },

  async getSddDocument(documentId: string): Promise<SddDocumentContent> {
    return fetchJson<SddDocumentContent>(`/requirements/documents/${documentId}`);
  },

  async getDocumentQualityGate(documentId: string): Promise<DocumentQualityGate> {
    return fetchJson<DocumentQualityGate>(`/requirements/documents/${documentId}/quality-gate`);
  },

  async runDocumentQualityGate(documentId: string, profileId?: string): Promise<DocumentQualityGate> {
    return postJson<DocumentQualityGate>(`/requirements/documents/${documentId}/quality-gate-runs`, {
      profileId,
      triggerMode: 'MANUAL',
    });
  },

  async createDocumentReview(documentId: string, request: { decision: string; comment?: string; commitSha: string; blobSha: string }): Promise<DocumentReview> {
    return postJson<DocumentReview>(`/requirements/documents/${documentId}/reviews`, request);
  },

  async getDocumentReviews(requirementId: string): Promise<ReadonlyArray<DocumentReview>> {
    return fetchJson<ReadonlyArray<DocumentReview>>(`/requirements/${requirementId}/reviews`);
  },

  async createAgentRun(requirementId: string, request: { skillKey: string; targetStage: string; profileId?: string; notes?: string }): Promise<AgentRun> {
    return postJson<AgentRun>(`/requirements/${requirementId}/agent-runs`, request);
  },

  async getAgentRun(executionId: string): Promise<AgentRun> {
    return fetchJson<AgentRun>(`/requirements/agent-runs/${executionId}`);
  },

  async getTraceability(requirementId: string, profileId?: string): Promise<RequirementTraceability> {
    return fetchJson<RequirementTraceability>(`/requirements/${requirementId}/traceability${buildProfileQuery(profileId)}`);
  },

  async getKnowledgeGraph(filters: GraphFilters = {}): Promise<KnowledgeGraph> {
    return fetchJson<KnowledgeGraph>(`/requirements/knowledge-graph${buildGraphQuery(filters)}`);
  },

  async getGraphNode(nodeId: string, filters: GraphFilters = {}): Promise<GraphNodeDetail> {
    return fetchJson<GraphNodeDetail>(`/requirements/knowledge-graph/nodes/${encodeURIComponent(nodeId)}${buildGraphQuery(filters)}`);
  },

  async getGraphImpact(request: GraphImpactRequest): Promise<GraphImpact> {
    const params = new URLSearchParams({ nodeId: request.nodeId });
    if (request.direction) params.set('direction', request.direction);
    if (request.maxDepth != null) params.set('maxDepth', String(request.maxDepth));
    if (request.relationshipTypes?.length) params.set('relationshipTypes', request.relationshipTypes.join(','));
    if (request.applicationId) params.set('applicationId', request.applicationId);
    if (request.snowGroup) params.set('snowGroup', request.snowGroup);
    if (request.branch) params.set('branch', request.branch);
    if (request.profileId) params.set('profileId', request.profileId);
    return fetchJson<GraphImpact>(`/requirements/knowledge-graph/impact?${params.toString()}`);
  },

  async getGraphHealth(filters: GraphFilters = {}): Promise<GraphProviderHealth> {
    return fetchJson<GraphProviderHealth>(`/requirements/knowledge-graph/health${buildGraphQuery(filters)}`);
  },
};
