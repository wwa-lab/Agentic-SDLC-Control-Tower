import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

async function runWithConcurrency<T>(
  items: ReadonlyArray<string>,
  limit: number,
  task: (id: string) => Promise<T>,
): Promise<T[]> {
  const results: T[] = [];
  for (let i = 0; i < items.length; i += limit) {
    const batch = items.slice(i, i + limit);
    const batchResults = await Promise.all(batch.map(task));
    results.push(...batchResults);
  }
  return results;
}
import type {
  AgentRun,
  DocumentReview,
  GraphNode,
  KnowledgeGraph,
  PipelineProfile,
  RequirementCategory,
  RequirementControlPlaneListSummary,
  RequirementDetail,
  RequirementFilters,
  RequirementList,
  RequirementListItem,
  RequirementPriority,
  RequirementStatus,
  RequirementTraceability,
  SddDocumentContent,
  SddDocumentIndex,
  SortField,
  SourceReference,
  StatusDistribution,
  ViewMode,
} from '../types/requirement';
import { requirementApi } from '../api/requirementApi';
import { MOCK_REQUIREMENT_DETAILS, MOCK_REQUIREMENT_LIST } from '../mockData';
import { getActiveProfile, getAllProfiles, getProfileById } from '../profiles';
import { devLog, mapSortField, toUserMessage } from '../utils/storeUtils';
import { summarizeTraceability } from '../utils/controlPlane';

const USE_MOCK = import.meta.env.DEV && !import.meta.env.VITE_USE_BACKEND;
const PROFILE_STORAGE_KEY = 'requirement.activeProfileId';

const COMPLETED_STATUSES = new Set<RequirementStatus>(['Delivered', 'Archived']);

const PRIORITY_ORDER: Record<RequirementPriority, number> = {
  Critical: 0,
  High: 1,
  Medium: 2,
  Low: 3,
};

const STATUS_ORDER: Record<RequirementStatus, number> = {
  Draft: 0,
  'In Review': 1,
  Approved: 2,
  'In Progress': 3,
  Delivered: 4,
  Archived: 5,
};

// Fixed set of mock summaries keyed by requirement id — no more fragile regex derivation
const MOCK_SUMMARY_FIXTURES: Record<string, Pick<
  RequirementControlPlaneListSummary,
  'status' | 'message' | 'missingDocumentCount' | 'staleReviewCount' | 'sourceCount'
>> = {
  default_fresh:    { status: 'FRESH',                        message: 'Sources, docs, and reviews are aligned',    missingDocumentCount: 0, staleReviewCount: 0, sourceCount: 1 },
  default_missing:  { status: 'MISSING_DOCUMENT',             message: '5 expected docs missing',                   missingDocumentCount: 5, staleReviewCount: 0, sourceCount: 1 },
  default_stale:    { status: 'DOCUMENT_CHANGED_AFTER_REVIEW', message: 'Document changed after business review',   missingDocumentCount: 0, staleReviewCount: 1, sourceCount: 1 },
  default_missing2: { status: 'MISSING_DOCUMENT',             message: '2 expected docs missing',                   missingDocumentCount: 2, staleReviewCount: 0, sourceCount: 1 },
};

function getMockFixtureKey(index: number): keyof typeof MOCK_SUMMARY_FIXTURES {
  if (index % 3 === 0) return 'default_missing';
  if (index % 2 === 0) return 'default_missing2';
  if (index === 5)     return 'default_stale';
  return 'default_fresh';
}

export const useRequirementStore = defineStore('requirement', () => {
  // ── List state ──
  const listData = ref<RequirementList | null>(null);
  const listLoading = ref(false);
  const listError = ref<string | null>(null);
  const filters = ref<RequirementFilters>({ showCompleted: false });
  const viewMode = ref<ViewMode>('list');
  const sortField = ref<SortField>('priority');
  const sortAsc = ref(true);

  // ── Detail state ──
  const detail = ref<RequirementDetail | null>(null);
  const detailLoading = ref(false);
  const detailError = ref<string | null>(null);
  const selectedRequirementId = ref<string | null>(null);

  // ── Profile state ──
  const availableProfiles = getAllProfiles();
  const activeProfile = ref<PipelineProfile>(readStoredProfile() ?? getActiveProfile());
  const orchestratorResult = ref<import('../types/requirement').OrchestratorResult | null>(null);
  const skillMessage = ref<string | null>(null);

  // ── Control plane state ──
  const sourceReferences = ref<ReadonlyArray<SourceReference>>([]);
  const sddDocuments = ref<SddDocumentIndex | null>(null);
  const selectedDocumentId = ref<string | null>(null);
  const selectedDocument = ref<SddDocumentContent | null>(null);
  const selectedDocumentLoading = ref(false);
  const selectedDocumentError = ref<string | null>(null);
  const documentReviews = ref<ReadonlyArray<DocumentReview>>([]);
  const agentRuns = ref<ReadonlyArray<AgentRun>>([]);
  const traceability = ref<RequirementTraceability | null>(null);
  const controlPlaneLoading = ref(false);
  const controlPlaneError = ref<string | null>(null);
  const controlPlaneSummaries = ref<Record<string, RequirementControlPlaneListSummary>>({});
  const controlPlaneSummaryLoading = ref(false);
  const githubSyncLoading = ref(false);
  const githubSyncError = ref<string | null>(null);

  // ── Knowledge graph state ──
  const knowledgeGraph = ref<KnowledgeGraph | null>(null);
  const knowledgeGraphLoading = ref(false);
  const knowledgeGraphError = ref<string | null>(null);

  // ── Computed ──

  const filteredRequirements = computed<ReadonlyArray<RequirementListItem>>(() => {
    if (!listData.value) return [];
    let items = [...(listData.value.requirements ?? listData.value.items ?? [])];
    if (!filters.value.showCompleted) {
      items = items.filter(req => !COMPLETED_STATUSES.has(req.status));
    }
    if (USE_MOCK) {
      items = items.filter(req => {
        if (filters.value.priority && req.priority !== filters.value.priority) return false;
        if (filters.value.status && req.status !== filters.value.status) return false;
        if (filters.value.category && req.category !== filters.value.category) return false;
        if (filters.value.search) {
          const term = filters.value.search.toLowerCase();
          return req.title.toLowerCase().includes(term) || req.id.toLowerCase().includes(term);
        }
        return true;
      });
    }
    return items;
  });

  const sortedRequirements = computed<ReadonlyArray<RequirementListItem>>(() => {
    if (!USE_MOCK) return filteredRequirements.value;
    const items = [...filteredRequirements.value];
    const dir = sortAsc.value ? 1 : -1;
    items.sort((a, b) => {
      let cmp = 0;
      switch (sortField.value) {
        case 'priority': cmp = PRIORITY_ORDER[a.priority] - PRIORITY_ORDER[b.priority]; break;
        case 'status':   cmp = STATUS_ORDER[a.status] - STATUS_ORDER[b.status]; break;
        case 'title':    cmp = a.title.localeCompare(b.title); break;
        case 'recency':  cmp = new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime(); break;
      }
      if (cmp !== 0) return cmp * dir;
      return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
    });
    return items;
  });

  const statusDistribution = computed<StatusDistribution>(
    () => listData.value?.statusDistribution ?? {
      draft: 0, inReview: 0, approved: 0, inProgress: 0, delivered: 0, archived: 0,
    },
  );

  const controlPlaneOverview = computed(() => {
    const summaries = Object.values(controlPlaneSummaries.value);
    return {
      total: summaries.length,
      fresh:    summaries.filter(s => s.status === 'FRESH').length,
      stale:    summaries.filter(s => s.status === 'SOURCE_CHANGED' || s.status === 'DOCUMENT_CHANGED_AFTER_REVIEW').length,
      missing:  summaries.filter(s => s.status === 'MISSING_DOCUMENT' || s.status === 'MISSING_SOURCE').length,
      errors:   summaries.filter(s => s.status === 'ERROR').length,
      sources:  summaries.reduce((n, s) => n + s.sourceCount, 0),
      documents: summaries.reduce((n, s) => n + s.documentCount, 0),
      artifacts: summaries.reduce((n, s) => n + s.artifactCount, 0),
    };
  });

  // ── Mock helpers ──

  function buildMockSummary(requirementId: string): RequirementControlPlaneListSummary {
    const index = MOCK_REQUIREMENT_LIST.requirements.findIndex(r => r.id === requirementId);
    const fixture = MOCK_SUMMARY_FIXTURES[getMockFixtureKey(index >= 0 ? index : 0)];
    return {
      requirementId,
      sourceCount: fixture.sourceCount,
      documentCount: Math.max(0, activeProfile.value.documentStages.length - fixture.missingDocumentCount),
      missingDocumentCount: fixture.missingDocumentCount,
      staleReviewCount: fixture.staleReviewCount,
      artifactCount: 0,
      status: fixture.status,
      message: fixture.message,
    };
  }

  function buildMockQualityGate(index: number) {
    const scores = [94, 86, 72, 91, 84, 76, 88, 93, 79, 82];
    const score = scores[index % scores.length] ?? 82;
    const band = score >= 90 ? 'EXCELLENT' : score >= 80 ? 'GOOD' : 'BLOCKED';
    const label = band === 'EXCELLENT' ? 'Excellent' : band === 'GOOD' ? 'Good' : 'Blocked';
    const passed = score >= 80;
    return {
      score,
      band,
      label,
      passed,
      threshold: 80,
      summary: passed
        ? `${label} quality. This document meets the minimum score for business review.`
        : 'Score is below 80. This document must be improved before downstream approval.',
      findings: passed
        ? ['Traceability and acceptance coverage are present.', 'No blocking completeness gaps detected.']
        : ['Acceptance criteria are incomplete or not testable enough.', 'Traceability to source evidence needs strengthening.'],
    } as const;
  }

  function buildMockKnowledgeGraph(): KnowledgeGraph {
    const nodes: GraphNode[] = activeProfile.value.documentStages.map(stage => ({
      id: `doc-type:${activeProfile.value.id}:${stage.sddType}`,
      kind: 'DOCUMENT',
      label: stage.label,
      properties: {
        docType: stage.sddType,
        profile: activeProfile.value.id,
        artifactType: stage.artifactType,
        pathPattern: stage.pathPattern,
        traceabilityKey: stage.traceabilityKey,
        expectedTier: stage.expectedTier,
        freshnessStatus: 'UNKNOWN',
      },
    }));
    const edges = nodes.slice(1).map((node, i) => ({
      id: `edge:${node.id}:DEPENDS_ON:${nodes[i].id}`,
      type: 'DEPENDS_ON',
      from: node.id,
      to: nodes[i].id,
      source: 'profile',
      confidence: 0.75,
      properties: { profile: activeProfile.value.id },
    }));
    return {
      scope: { profileId: activeProfile.value.id, provider: 'profile' },
      health: {
        nodeCount: nodes.length, edgeCount: edges.length,
        issueCount: 0, errorCount: 0, warningCount: 0, suggestionCount: 0,
        stale: false, lastGeneratedAt: null, lastImportedAt: null,
      },
      nodes, edges, issues: [], suggestions: [], lastSync: null,
    };
  }

  function buildMockControlPlane(id: string) {
    const now = '2026-04-27T09:00:00Z';
    sourceReferences.value = [{
      id: `SRC-${id}-JIRA`,
      requirementId: id,
      sourceType: 'JIRA',
      externalId: 'PAY-123',
      title: 'Payment reconciliation enhancement',
      url: 'jira://PAY-123',
      sourceUpdatedAt: now,
      fetchedAt: now,
      freshnessStatus: 'FRESH',
      errorMessage: null,
    }];
    sddDocuments.value = {
      requirementId: id,
      profileId: activeProfile.value.id,
      workspace: {
        id: `SDDW-${id}`,
        applicationId: 'payment-gateway',
        applicationName: 'Payment Gateway',
        snowGroup: 'APAC-PAYMENTS-L2',
        sourceRepoFullName: 'wwa-lab/payment-gateway-service',
        sddRepoFullName: 'wwa-lab/payment-gateway-sdd',
        baseBranch: 'main',
        workingBranch: 'project/PAY-2026-sso-upgrade',
        lifecycleStatus: 'IN_DEVELOPMENT',
        docsRoot: 'docs/',
        releasePrUrl: null,
        kbRepoFullName: 'wwa-lab/payment-gateway-knowledge-base',
        kbMainBranch: 'main',
        kbPreviewBranch: 'project/PAY-2026-sso-upgrade',
        graphManifestPath: '_graph/manifest.json',
      },
      stages: activeProfile.value.documentStages.map((stage, i) => ({
        id: i < 3 ? `DOC-${id}-${stage.sddType}` : null,
        sddType: stage.sddType,
        stageLabel: stage.label,
        title: stage.label,
        repoFullName: i < 3 ? 'wwa-lab/payment-gateway-sdd' : null,
        branchOrRef: i < 3 ? 'project/PAY-2026-sso-upgrade' : null,
        path: stage.pathPattern,
        latestCommitSha: i < 3 ? `mock-commit-${i}` : null,
        latestBlobSha: i < 3 ? `mock-blob-${i}` : null,
        githubUrl: i < 3 ? `https://github.com/wwa-lab/payment-gateway-sdd/blob/project/PAY-2026-sso-upgrade/${stage.pathPattern}` : null,
        status: i < 3 ? 'IN_REVIEW' : 'MISSING',
        freshnessStatus: i < 3 ? 'FRESH' : 'MISSING_DOCUMENT',
        missing: i >= 3,
        qualityGate: i < 3 ? buildMockQualityGate(i) : null,
      })),
    };
    documentReviews.value = [];
    agentRuns.value = [];
    traceability.value = {
      requirementId: id,
      sources: sourceReferences.value,
      documents: sddDocuments.value,
      reviews: [],
      agentRuns: [],
      artifactLinks: [],
      freshness: [
        { subjectType: 'SOURCE', subjectId: `SRC-${id}-JIRA`, status: 'FRESH', message: 'Payment reconciliation enhancement' },
      ],
    };
  }

  // ── Actions ──

  async function fetchRequirementList() {
    listLoading.value = true;
    listError.value = null;
    try {
      if (USE_MOCK) {
        listData.value = MOCK_REQUIREMENT_LIST;
        void fetchControlPlaneSummaries(MOCK_REQUIREMENT_LIST.requirements.map(r => r.id));
        return;
      }
      const result = await requirementApi.getRequirementList({
        priority: filters.value.priority,
        status: filters.value.status,
        category: filters.value.category,
        search: filters.value.search,
        sortBy: mapSortField(sortField.value),
        sortDirection: sortAsc.value ? 'asc' : 'desc',
      });
      listData.value = { ...result, requirements: result.requirements ?? result.items ?? [] };
      void fetchControlPlaneSummaries((result.requirements ?? result.items ?? []).map(r => r.id));
    } catch (error) {
      devLog.error('Failed to fetch requirement list:', error);
      listError.value = toUserMessage(error, 'Failed to load requirements. Please try again later.');
    } finally {
      listLoading.value = false;
    }
  }

  async function fetchControlPlaneSummaries(requirementIds: ReadonlyArray<string>) {
    const ids = [...new Set(requirementIds)].filter(Boolean);
    if (ids.length === 0) { controlPlaneSummaries.value = {}; return; }

    controlPlaneSummaryLoading.value = true;
    try {
      if (USE_MOCK) {
        controlPlaneSummaries.value = Object.fromEntries(ids.map(id => [id, buildMockSummary(id)]));
        return;
      }
      const entries = await runWithConcurrency(ids, 5, async id => {
        try {
          const trace = await requirementApi.getTraceability(id, activeProfile.value.id);
          return [id, summarizeTraceability(trace)] as const;
        } catch (error) {
          return [id, {
            requirementId: id,
            sourceCount: 0, documentCount: 0, missingDocumentCount: 0,
            staleReviewCount: 0, artifactCount: 0,
            status: 'ERROR',
            message: toUserMessage(error, 'Control plane summary failed'),
          } satisfies RequirementControlPlaneListSummary] as const;
        }
      });
      controlPlaneSummaries.value = Object.fromEntries(entries);
    } finally {
      controlPlaneSummaryLoading.value = false;
    }
  }

  async function fetchRequirementDetail(id: string) {
    detailLoading.value = true;
    detailError.value = null;
    selectedRequirementId.value = id;
    orchestratorResult.value = null;
    skillMessage.value = null;
    try {
      detail.value = USE_MOCK
        ? (MOCK_REQUIREMENT_DETAILS[id] ?? null)
        : await requirementApi.getRequirementDetail(id);
      if (!detail.value) {
        detailError.value = `Requirement not found: ${id}`;
      } else {
        void fetchControlPlane(id);
      }
    } catch (error) {
      devLog.error('Failed to fetch requirement detail:', error);
      detailError.value = toUserMessage(error, 'Failed to load requirement detail. Please try again later.');
    } finally {
      detailLoading.value = false;
    }
  }

  async function fetchControlPlane(requirementId: string) {
    controlPlaneLoading.value = true;
    controlPlaneError.value = null;
    selectedDocumentId.value = null;
    selectedDocument.value = null;
    selectedDocumentError.value = null;
    try {
      if (USE_MOCK) { buildMockControlPlane(requirementId); return; }
      const profileId = activeProfile.value.id;
      const [sources, documents, reviews, trace] = await Promise.all([
        requirementApi.getSourceReferences(requirementId),
        requirementApi.getSddDocuments(requirementId, profileId),
        requirementApi.getDocumentReviews(requirementId),
        requirementApi.getTraceability(requirementId, profileId),
      ]);
      sourceReferences.value = sources;
      sddDocuments.value = documents;
      documentReviews.value = reviews;
      traceability.value = trace;
      agentRuns.value = trace.agentRuns;
    } catch (error) {
      devLog.error('Failed to fetch requirement control plane:', error);
      controlPlaneError.value = toUserMessage(error, 'Failed to load control plane sections.');
    } finally {
      controlPlaneLoading.value = false;
    }
  }

  async function fetchKnowledgeGraph() {
    knowledgeGraphLoading.value = true;
    knowledgeGraphError.value = null;
    try {
      if (USE_MOCK) { knowledgeGraph.value = buildMockKnowledgeGraph(); return; }
      knowledgeGraph.value = await requirementApi.getKnowledgeGraph({
        profileId: activeProfile.value.id,
        includeIssues: true,
        includeSuggestions: true,
      });
    } catch (error) {
      devLog.error('Failed to fetch SDD knowledge graph:', error);
      knowledgeGraphError.value = toUserMessage(error, 'Backend graph is unavailable. Showing profile fallback.');
      knowledgeGraph.value = buildMockKnowledgeGraph();
    } finally {
      knowledgeGraphLoading.value = false;
    }
  }

  async function refreshSourceReference(sourceId: string) {
    if (USE_MOCK) return;
    const updated = await requirementApi.refreshSourceReference(sourceId);
    sourceReferences.value = sourceReferences.value.map(s => s.id === sourceId ? updated : s);
    if (selectedRequirementId.value) await fetchControlPlane(selectedRequirementId.value);
  }

  async function refreshGitHubDocuments(requirementIds?: ReadonlyArray<string>) {
    const ids = [...new Set(
      (requirementIds && requirementIds.length > 0
        ? requirementIds
        : sortedRequirements.value.map(r => r.id)
      ).filter(Boolean),
    )];

    githubSyncLoading.value = true;
    githubSyncError.value = null;
    skillMessage.value = null;
    try {
      if (ids.length === 0) { await fetchRequirementList(); return; }
      if (USE_MOCK) {
        if (selectedRequirementId.value && ids.includes(selectedRequirementId.value)) {
          buildMockControlPlane(selectedRequirementId.value);
        }
        skillMessage.value = ids.length === 1
          ? 'GitHub SDD index refreshed from the selected branch.'
          : `${ids.length} GitHub SDD indexes refreshed from selected branches.`;
        return;
      }
      await runWithConcurrency(ids, 5, id => requirementApi.refreshSddDocuments(id, activeProfile.value.id));
      if (selectedRequirementId.value && ids.includes(selectedRequirementId.value)) {
        await fetchControlPlane(selectedRequirementId.value);
      }
      await fetchRequirementList();
      skillMessage.value = ids.length === 1
        ? 'GitHub SDD index refreshed from the selected branch.'
        : `${ids.length} GitHub SDD indexes refreshed from selected branches.`;
    } catch (error) {
      devLog.error('Failed to refresh GitHub SDD documents:', error);
      githubSyncError.value = toUserMessage(error, 'Failed to refresh GitHub SDD documents.');
    } finally {
      githubSyncLoading.value = false;
    }
  }

  async function openSddDocument(documentId: string) {
    selectedDocumentId.value = documentId;
    selectedDocument.value = null;
    selectedDocumentError.value = null;
    selectedDocumentLoading.value = true;
    try {
      if (USE_MOCK) {
        const doc = sddDocuments.value?.stages.find(s => s.id === documentId);
        if (!doc) { selectedDocumentError.value = 'Document is no longer available in this index.'; return; }
        selectedDocument.value = {
          document: doc,
          markdown: `# ${doc.title}\n\nMock GitHub-backed Markdown for ${doc.path}.`,
          commitSha: doc.latestCommitSha ?? 'mock-commit',
          blobSha: doc.latestBlobSha ?? 'mock-blob',
          githubUrl: doc.githubUrl ?? '#',
          fetchedAt: new Date().toISOString(),
        };
        return;
      }
      selectedDocument.value = await requirementApi.getSddDocument(documentId);
    } catch (error) {
      devLog.error('Failed to fetch SDD document:', error);
      selectedDocumentError.value = toUserMessage(error, 'Failed to load Markdown from GitHub.');
    } finally {
      selectedDocumentLoading.value = false;
    }
  }

  async function runDocumentQualityGate(documentId: string) {
    const doc = sddDocuments.value?.stages.find(s => s.id === documentId);
    if (!doc) return;
    skillMessage.value = null;
    try {
      const gate = USE_MOCK
        ? buildMockQualityGate(sddDocuments.value?.stages.findIndex(s => s.id === documentId) ?? 0)
        : await requirementApi.runDocumentQualityGate(documentId, activeProfile.value.id);

      if (sddDocuments.value) {
        sddDocuments.value = {
          ...sddDocuments.value,
          stages: sddDocuments.value.stages.map(s => s.id === documentId ? { ...s, qualityGate: gate } : s),
        };
      }
      if (selectedDocument.value?.document.id === documentId) {
        selectedDocument.value = {
          ...selectedDocument.value,
          document: { ...selectedDocument.value.document, qualityGate: gate },
        };
      }
      skillMessage.value = `Document quality gate completed: ${gate.score} ${gate.label ?? gate.band}.`;
    } catch (error) {
      devLog.error('Failed to run document quality gate:', error);
      skillMessage.value = toUserMessage(error, 'Failed to run document quality gate.');
    }
  }

  async function createReview(documentId: string, decision: string, comment = '') {
    const normalizedDecision = decision.trim().toUpperCase();
    const reviewComment = comment.trim();
    if (normalizedDecision === 'REJECTED' && !reviewComment) {
      throw new Error('Rejection reason is required');
    }
    const doc = sddDocuments.value?.stages.find(s => s.id === documentId);
    if (!doc?.latestCommitSha || !doc.latestBlobSha) return;
    if (USE_MOCK) {
      documentReviews.value = [
        {
          id: `REV-${Date.now()}`,
          documentId,
          requirementId: selectedRequirementId.value ?? '',
          decision: normalizedDecision as DocumentReview['decision'],
          comment: reviewComment || null,
          reviewerId: 'mock-user',
          reviewerType: 'BUSINESS',
          commitSha: doc.latestCommitSha,
          blobSha: doc.latestBlobSha,
          anchorType: 'DOCUMENT',
          anchorValue: null,
          stale: false,
          createdAt: new Date().toISOString(),
        },
        ...documentReviews.value,
      ];
      return;
    }
    await requirementApi.createDocumentReview(documentId, {
      decision: normalizedDecision, comment: reviewComment,
      commitSha: doc.latestCommitSha, blobSha: doc.latestBlobSha,
    });
    if (selectedRequirementId.value) await fetchControlPlane(selectedRequirementId.value);
  }

  async function requestAgentRun(skillKey: string, targetStage: string) {
    if (!selectedRequirementId.value) return;
    if (USE_MOCK) {
      const executionId = `EXEC-${Date.now()}`;
      agentRuns.value = [
        {
          executionId,
          requirementId: selectedRequirementId.value,
          profileId: activeProfile.value.id,
          skillKey,
          targetStage,
          status: 'MANIFEST_READY',
          manifest: {},
          command: `/${skillKey} please help me complete ${targetStage} for ${selectedRequirementId.value}.`,
          callbackUrl: `http://localhost:8080/api/v1/requirements/agent-runs/${executionId}/callback`,
          stageEvents: [],
          outputSummary: null,
          errorMessage: null,
          artifactLinks: [],
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        },
        ...agentRuns.value,
      ];
      return;
    }
    const run = await requirementApi.createAgentRun(selectedRequirementId.value, {
      skillKey, targetStage, profileId: activeProfile.value.id,
    });
    agentRuns.value = [run, ...agentRuns.value];
    skillMessage.value = `CLI prompt ready: ${run.executionId}`;
  }

  async function confirmAgentRunMerge(executionId: string, prUrl: string) {
    const run = agentRuns.value.find(r => r.executionId === executionId && r.profileId === activeProfile.value.id);
    if (!run || !selectedRequirementId.value) return;
    const stageId = run.targetStage ?? sddDocuments.value?.stages[0]?.sddType ?? 'spec';
    const stageLabel = sddDocuments.value?.stages.find(s => s.sddType === stageId)?.stageLabel ?? stageId;
    if (USE_MOCK) {
      const event = {
        id: `STAGE-${Date.now()}`,
        executionId: run.executionId,
        requirementId: run.requirementId,
        profileId: run.profileId,
        stageId,
        stageLabel,
        state: 'DONE',
        message: 'GitHub PR merge confirmed manually.',
        outputPath: prUrl,
        errorMessage: null,
        createdAt: new Date().toISOString(),
      };
      agentRuns.value = agentRuns.value.map(r => r.executionId === run.executionId
        ? { ...r, status: 'COMPLETED', stageEvents: [...(r.stageEvents ?? []), event], updatedAt: new Date().toISOString() }
        : r);
      skillMessage.value = `GitHub merge confirmed for ${stageLabel}.`;
      return;
    }
    const result = await requirementApi.confirmAgentRunMerge(run.executionId, { prUrl });
    agentRuns.value = [
      result.run,
      ...agentRuns.value.filter(r => r.executionId !== result.run.executionId),
    ];
    sddDocuments.value = result.documents;
    await fetchControlPlane(selectedRequirementId.value);
    skillMessage.value = `GitHub merge confirmed for ${stageLabel}.`;
  }

  async function generateStories(requirementId: string) {
    if (USE_MOCK) { devLog.info(`[stub] Story generation triggered for ${requirementId}`); return; }
    const result = await requirementApi.triggerStoryGeneration(requirementId);
    skillMessage.value = result.message;
  }

  async function generateSpec(requirementId: string, storyIds: ReadonlyArray<string>) {
    if (storyIds.length === 0) throw new Error('At least one linked story is required to generate a spec');
    if (USE_MOCK) { devLog.info(`[stub] Spec generation triggered for ${requirementId}`, storyIds); return; }
    const result = await requirementApi.triggerSpecGeneration(requirementId, storyIds);
    skillMessage.value = result.message;
  }

  async function runAnalysis(requirementId: string) {
    if (USE_MOCK) { devLog.info(`[stub] AI analysis triggered for ${requirementId}`); return; }
    const result = await requirementApi.triggerAnalysis(requirementId);
    skillMessage.value = result.message;
  }

  function setFilters(newFilters: Partial<RequirementFilters>) {
    const previous = filters.value;
    filters.value = { ...filters.value, ...newFilters };
    if (!USE_MOCK) {
      const serverChanged =
        previous.priority !== filters.value.priority ||
        previous.status !== filters.value.status ||
        previous.category !== filters.value.category ||
        previous.search !== filters.value.search;
      if (serverChanged) void fetchRequirementList();
    }
  }

  function setViewMode(mode: ViewMode) {
    viewMode.value = mode;
    if (mode === 'graph') void fetchKnowledgeGraph();
  }

  function setSortField(field: SortField) {
    if (sortField.value === field) {
      sortAsc.value = !sortAsc.value;
    } else {
      sortField.value = field;
      sortAsc.value = true;
    }
    if (!USE_MOCK) void fetchRequirementList();
  }

  function clearDetail() {
    detail.value = null;
    detailError.value = null;
    selectedRequirementId.value = null;
    orchestratorResult.value = null;
    skillMessage.value = null;
    sourceReferences.value = [];
    sddDocuments.value = null;
    selectedDocumentId.value = null;
    selectedDocument.value = null;
    selectedDocumentLoading.value = false;
    selectedDocumentError.value = null;
    documentReviews.value = [];
    agentRuns.value = [];
    traceability.value = null;
    controlPlaneError.value = null;
  }

  function readStoredProfile(): PipelineProfile | null {
    if (typeof window === 'undefined') return null;
    const id = window.localStorage.getItem(PROFILE_STORAGE_KEY);
    return id ? getProfileById(id) ?? null : null;
  }

  function setActiveProfile(profileId: string) {
    const next = getProfileById(profileId);
    if (!next || next.id === activeProfile.value.id) return;
    activeProfile.value = next;
    orchestratorResult.value = null;
    skillMessage.value = null;
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(PROFILE_STORAGE_KEY, next.id);
    }
    if (listData.value) {
      void fetchControlPlaneSummaries(listData.value.requirements.map(r => r.id));
    }
    if (viewMode.value === 'graph') void fetchKnowledgeGraph();
    if (selectedRequirementId.value) void fetchControlPlane(selectedRequirementId.value);
  }

  async function loadActiveProfile(workspaceId?: string) {
    orchestratorResult.value = null;
    skillMessage.value = null;
    const stored = readStoredProfile();
    if (stored) { activeProfile.value = stored; return; }
    if (USE_MOCK) { activeProfile.value = getActiveProfile(workspaceId); return; }
    try {
      const backendProfile = await requirementApi.getActiveProfile();
      activeProfile.value = getProfileById(backendProfile.id) ?? backendProfile;
    } catch (error) {
      devLog.error('Failed to load active profile:', error);
      activeProfile.value = getActiveProfile(workspaceId);
    }
  }

  async function invokeProfileSkill(requirementId: string, skillId: string) {
    skillMessage.value = null;
    if (USE_MOCK) {
      if (activeProfile.value.usesOrchestrator) {
        orchestratorResult.value = {
          determinedPathId: 'enhancement',
          determinedTier: 'L2',
          confidence: 'High',
          reasoning: 'Existing RPG/COBOL source modification detected — enhancement path selected with L2 standard tier',
        };
      }
      devLog.info(`[stub] Profile skill ${skillId} invoked for ${requirementId}`);
      return;
    }
    if (!activeProfile.value.usesOrchestrator) {
      if (skillId === 'req-to-user-story') { await generateStories(requirementId); return; }
      if (skillId === 'user-story-to-spec') {
        const stories = detail.value?.linkedStories.data?.stories ?? [];
        const candidate = stories.find(s => s.specId === null) ?? stories[0];
        if (!candidate) throw new Error('No linked stories are available to generate a spec');
        await generateSpec(requirementId, [candidate.id]);
        return;
      }
    }
    const result = await requirementApi.invokeProfileSkill(requirementId, skillId);
    orchestratorResult.value = result.orchestratorResult ?? null;
    skillMessage.value = result.message;
  }

  return {
    // state
    listData, listLoading, listError,
    filters, viewMode, sortField, sortAsc,
    filteredRequirements, sortedRequirements, statusDistribution, controlPlaneOverview,
    detail, detailLoading, detailError, selectedRequirementId,
    availableProfiles, activeProfile, orchestratorResult, skillMessage,
    sourceReferences, sddDocuments, selectedDocumentId, selectedDocument,
    selectedDocumentLoading, selectedDocumentError,
    documentReviews, agentRuns, traceability,
    controlPlaneLoading, controlPlaneError,
    controlPlaneSummaries, controlPlaneSummaryLoading,
    githubSyncLoading, githubSyncError,
    knowledgeGraph, knowledgeGraphLoading, knowledgeGraphError,
    // actions
    fetchRequirementList, fetchRequirementDetail, fetchControlPlane,
    fetchControlPlaneSummaries, fetchKnowledgeGraph,
    refreshSourceReference, refreshGitHubDocuments,
    openSddDocument, runDocumentQualityGate,
    createReview, requestAgentRun, confirmAgentRunMerge,
    generateStories, generateSpec, runAnalysis,
    setFilters, setViewMode, setSortField, clearDetail,
    loadActiveProfile, setActiveProfile, invokeProfileSkill,
  };
});
