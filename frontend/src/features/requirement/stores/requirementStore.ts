import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type {
  RequirementList,
  RequirementDetail,
  RequirementListItem,
  StatusDistribution,
  RequirementFilters,
  RequirementPriority,
  RequirementStatus,
  ViewMode,
  SortField,
  ImportState,
  ImportSourceType,
  RequirementDraft,
  PipelineProfile,
  OrchestratorResult,
} from '../types/requirement';
import { requirementApi } from '../api/requirementApi';
import { MOCK_REQUIREMENT_LIST, MOCK_REQUIREMENT_DETAILS } from '../mockData';
import { getActiveProfile } from '../profiles';

const USE_MOCK = import.meta.env.DEV && !import.meta.env.VITE_USE_BACKEND;

const COMPLETED_STATUSES = new Set(['Delivered', 'Archived']);

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
  const activeProfile = ref<PipelineProfile>(getActiveProfile());
  const orchestratorResult = ref<OrchestratorResult | null>(null);

  // ── Computed: filtered requirements ──
  const filteredRequirements = computed<ReadonlyArray<RequirementListItem>>(() => {
    if (!listData.value) return [];
    return listData.value.requirements.filter(req => {
      if (!filters.value.showCompleted && COMPLETED_STATUSES.has(req.status)) return false;
      if (filters.value.priority && req.priority !== filters.value.priority) return false;
      if (filters.value.status && req.status !== filters.value.status) return false;
      if (filters.value.category && req.category !== filters.value.category) return false;
      if (filters.value.search) {
        const term = filters.value.search.toLowerCase();
        if (!req.title.toLowerCase().includes(term) && !req.id.toLowerCase().includes(term)) {
          return false;
        }
      }
      return true;
    });
  });

  const sortedRequirements = computed<ReadonlyArray<RequirementListItem>>(() => {
    const items = [...filteredRequirements.value];
    const dir = sortAsc.value ? 1 : -1;
    items.sort((a, b) => {
      let cmp = 0;
      switch (sortField.value) {
        case 'priority':
          cmp = PRIORITY_ORDER[a.priority] - PRIORITY_ORDER[b.priority];
          break;
        case 'status':
          cmp = STATUS_ORDER[a.status] - STATUS_ORDER[b.status];
          break;
        case 'title':
          cmp = a.title.localeCompare(b.title);
          break;
        case 'recency':
          cmp = new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
          break;
      }
      if (cmp !== 0) return cmp * dir;
      // Secondary sort: recency (always descending)
      return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
    });
    return items;
  });

  const statusDistribution = computed<StatusDistribution>(
    () => listData.value?.statusDistribution ?? {
      draft: 0, inReview: 0, approved: 0, inProgress: 0, delivered: 0, archived: 0,
    },
  );

  // ── Actions ──
  async function fetchRequirementList() {
    listLoading.value = true;
    listError.value = null;
    try {
      listData.value = USE_MOCK
        ? MOCK_REQUIREMENT_LIST
        : await requirementApi.getRequirementList();
    } catch (err) {
      console.error('Failed to fetch requirement list:', err);
      listError.value = 'Failed to load requirements. Please try again later.';
    } finally {
      listLoading.value = false;
    }
  }

  async function fetchRequirementDetail(id: string) {
    detailLoading.value = true;
    detailError.value = null;
    selectedRequirementId.value = id;
    try {
      detail.value = USE_MOCK
        ? (MOCK_REQUIREMENT_DETAILS[id] ?? null)
        : await requirementApi.getRequirementDetail(id);
      if (!detail.value) {
        detailError.value = `Requirement not found: ${id}`;
      }
    } catch (err) {
      console.error('Failed to fetch requirement detail:', err);
      detailError.value = 'Failed to load requirement detail. Please try again later.';
    } finally {
      detailLoading.value = false;
    }
  }

  async function generateStories(requirementId: string) {
    if (USE_MOCK) {
      console.info(`[stub] Story generation triggered for ${requirementId}`);
      return;
    }
    await requirementApi.triggerStoryGeneration(requirementId);
  }

  async function generateSpec(storyId: string) {
    if (USE_MOCK) {
      console.info(`[stub] Spec generation triggered for story ${storyId}`);
      return;
    }
    await requirementApi.triggerSpecGeneration(storyId);
  }

  async function runAnalysis(requirementId: string) {
    if (USE_MOCK) {
      console.info(`[stub] AI analysis triggered for ${requirementId}`);
      return;
    }
    await requirementApi.triggerAnalysis(requirementId);
  }

  function setFilters(newFilters: Partial<RequirementFilters>) {
    filters.value = { ...filters.value, ...newFilters };
  }

  function setViewMode(mode: ViewMode) {
    viewMode.value = mode;
  }

  function setSortField(field: SortField) {
    if (sortField.value === field) {
      sortAsc.value = !sortAsc.value;
    } else {
      sortField.value = field;
      sortAsc.value = true;
    }
  }

  function clearDetail() {
    detail.value = null;
    detailError.value = null;
    selectedRequirementId.value = null;
    orchestratorResult.value = null;
  }

  function loadActiveProfile(_workspaceId?: string) {
    activeProfile.value = getActiveProfile(_workspaceId);
    orchestratorResult.value = null;
  }

  async function invokeProfileSkill(requirementId: string, skillId: string) {
    if (USE_MOCK) {
      // For IBM i orchestrator, return mock result with determined path and tier
      if (activeProfile.value.usesOrchestrator) {
        orchestratorResult.value = {
          determinedPathId: 'modification',
          determinedTier: 'L2',
          confidence: 'High',
          reasoning: 'Existing program detected — modification path selected with L2 standard tier',
        };
      }
      console.info(`[stub] Profile skill ${skillId} invoked for ${requirementId}`);
      return;
    }
    // Phase B: call backend invoke-skill endpoint
  }

  // ── Import state ──
  const INITIAL_IMPORT: ImportState = {
    isOpen: false,
    step: 'source',
    sourceType: 'paste',
    rawInput: '',
    fileName: null,
    draft: null,
    batchRows: [],
    batchDrafts: [],
    batchProgress: 0,
    batchTotal: 0,
    columnMapping: {},
  };

  const importState = ref<ImportState>({ ...INITIAL_IMPORT });

  function openImport() {
    importState.value = { ...INITIAL_IMPORT, isOpen: true };
  }

  function closeImport() {
    importState.value = { ...INITIAL_IMPORT };
  }

  function setImportSource(source: ImportSourceType) {
    importState.value = { ...importState.value, sourceType: source };
  }

  function setRawInput(text: string) {
    importState.value = { ...importState.value, rawInput: text };
  }

  function triggerNormalization() {
    importState.value = { ...importState.value, step: 'normalizing' };
    // Phase A: simulate AI normalization with mock draft
    setTimeout(() => {
      const mockDraft: RequirementDraft = {
        title: importState.value.rawInput.slice(0, 80) || 'Untitled Requirement',
        priority: 'Medium',
        category: 'Functional',
        summary: importState.value.rawInput.slice(0, 200),
        businessJustification: 'Business value to be determined based on stakeholder input.',
        acceptanceCriteria: ['Criterion extracted from input text'],
        assumptions: ['Input is accurate and complete'],
        constraints: [],
        missingInfo: ['Business justification needs elaboration', 'Priority not specified in source'],
        openQuestions: ['Who is the primary stakeholder?', 'What is the target timeline?'],
        aiSuggestedFields: ['title', 'priority', 'category', 'summary'],
      };
      importState.value = { ...importState.value, step: 'review', draft: mockDraft };
    }, 1200);
  }

  function confirmDraft(_draft: RequirementDraft) {
    console.info('[stub] Requirement created from import draft:', _draft.title);
    closeImport();
  }

  function discardDraft() {
    importState.value = { ...importState.value, step: 'source', draft: null, batchDrafts: [], batchRows: [] };
  }

  function handleFileImport(file: File) {
    const ext = file.name.split('.').pop()?.toLowerCase();
    importState.value = { ...importState.value, fileName: file.name };

    if (ext === 'xlsx' || ext === 'csv') {
      // Batch flow — Phase A: mock parsed rows
      const mockRows = [
        { Title: 'Imported Req 1', Priority: 'High', Category: 'Functional', Description: 'First imported requirement' },
        { Title: 'Imported Req 2', Priority: 'Medium', Category: 'Technical', Description: 'Second imported requirement' },
        { Title: 'Imported Req 3', Priority: 'Low', Category: 'Business', Description: 'Third imported requirement' },
      ];
      const autoMapping: Record<string, string> = {
        Title: 'title',
        Priority: 'priority',
        Category: 'category',
        Description: 'summary',
      };
      importState.value = {
        ...importState.value,
        step: 'batch-preview',
        batchRows: mockRows,
        columnMapping: autoMapping,
      };
    } else {
      // Single file flow — treat as text
      importState.value = { ...importState.value, rawInput: `[Content from ${file.name}]` };
      triggerNormalization();
    }
  }

  function updateColumnMapping(column: string, target: string) {
    importState.value = {
      ...importState.value,
      columnMapping: { ...importState.value.columnMapping, [column]: target },
    };
  }

  function normalizeSelected(indices: number[]) {
    const total = indices.length;
    importState.value = { ...importState.value, step: 'batch-normalizing', batchProgress: 0, batchTotal: total };
    // Phase A: simulate sequential normalization
    let current = 0;
    const interval = setInterval(() => {
      current++;
      importState.value = { ...importState.value, batchProgress: current };
      if (current >= total) {
        clearInterval(interval);
        // Build a reverse mapping: target field → source column name
        const mapping = importState.value.columnMapping;
        const reverseMap: Record<string, string> = {};
        for (const [sourceCol, targetField] of Object.entries(mapping)) {
          if (targetField !== '(skip)') {
            reverseMap[targetField] = sourceCol;
          }
        }

        const drafts: RequirementDraft[] = indices.map(idx => {
          const row = importState.value.batchRows[idx] ?? {};
          const mapped = (field: string) => {
            const sourceCol = reverseMap[field];
            return sourceCol ? (row[sourceCol] ?? '') : '';
          };
          return {
            title: mapped('title') || `Imported Requirement ${idx + 1}`,
            priority: (mapped('priority') as RequirementDraft['priority']) || 'Medium',
            category: (mapped('category') as RequirementDraft['category']) || 'Functional',
            summary: mapped('summary'),
            businessJustification: mapped('businessJustification'),
            acceptanceCriteria: [],
            assumptions: [],
            constraints: [],
            missingInfo: mapped('businessJustification') ? [] : ['Business justification required'],
            openQuestions: [],
            aiSuggestedFields: ['title', 'priority', 'category'],
          };
        });
        importState.value = { ...importState.value, step: 'batch-review', batchDrafts: drafts };
      }
    }, 400);
  }

  function confirmAllDrafts() {
    console.info(`[stub] ${importState.value.batchDrafts.length} requirements created from batch import`);
    closeImport();
  }

  return {
    // List
    listData,
    listLoading,
    listError,
    filters,
    viewMode,
    sortField,
    sortAsc,
    filteredRequirements,
    sortedRequirements,
    statusDistribution,
    // Detail
    detail,
    detailLoading,
    detailError,
    selectedRequirementId,
    // Actions
    fetchRequirementList,
    fetchRequirementDetail,
    generateStories,
    generateSpec,
    runAnalysis,
    setFilters,
    setViewMode,
    setSortField,
    clearDetail,
    // Profile
    activeProfile,
    orchestratorResult,
    loadActiveProfile,
    invokeProfileSkill,
    // Import
    importState,
    openImport,
    closeImport,
    setImportSource,
    setRawInput,
    triggerNormalization,
    confirmDraft,
    discardDraft,
    handleFileImport,
    updateColumnMapping,
    normalizeSelected,
    confirmAllDrafts,
  };
});
