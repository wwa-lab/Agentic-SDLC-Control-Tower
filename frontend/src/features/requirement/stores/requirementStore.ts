import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { ApiError } from '@/shared/api/client';
import type {
  ImportSourceType,
  ImportState,
  OrchestratorResult,
  PipelineProfile,
  RequirementCategory,
  RequirementDetail,
  RequirementDraft,
  RequirementFilters,
  RequirementImportStatus,
  RequirementList,
  RequirementListItem,
  RequirementPriority,
  RequirementSourceInput,
  RequirementStatus,
  SortField,
  StatusDistribution,
  ViewMode,
} from '../types/requirement';
import { requirementApi } from '../api/requirementApi';
import { MOCK_REQUIREMENT_DETAILS, MOCK_REQUIREMENT_LIST } from '../mockData';
import { getActiveProfile } from '../profiles';

const USE_MOCK = import.meta.env.DEV && !import.meta.env.VITE_USE_BACKEND;
const DEFAULT_KB_NAME = import.meta.env.VITE_REQUIREMENT_KB_NAME?.trim() || 'requirement-intake';

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

function mapSortField(field: SortField): string {
  return field === 'recency' ? 'updatedAt' : field;
}

function toUserMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError && error.message) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

function mapImportSource(sourceType: ImportSourceType): RequirementSourceInput['sourceType'] {
  switch (sourceType) {
    case 'file':
      return 'FILE';
    case 'email':
      return 'EMAIL';
    case 'meeting':
      return 'MEETING';
    case 'paste':
    default:
      return 'TEXT';
  }
}

function isZipArchive(fileName: string | null | undefined): boolean {
  return Boolean(fileName && fileName.toLowerCase().endsWith('.zip'));
}

function buildCombinedFileLabel(fileNames: ReadonlyArray<string>): string | null {
  if (fileNames.length === 0) return null;
  if (fileNames.length === 1) return fileNames[0];
  if (fileNames.length === 2) return `${fileNames[0]}, ${fileNames[1]}`;
  return `${fileNames[0]} + ${fileNames.length - 1} more`;
}

function buildBatchUploadLabel(fileCount: number): string {
  return fileCount === 1 ? '1 file' : `${fileCount} files`;
}

function describeImportedFile(file: File): string {
  const lowerName = file.name.toLowerCase();
  if (lowerName.endsWith('.zip')) return `[ZIP package upload: ${file.name}]`;
  if (lowerName.endsWith('.docx')) return `[Document upload: ${file.name}]`;
  if (lowerName.endsWith('.png') || lowerName.endsWith('.jpg') || lowerName.endsWith('.jpeg') || lowerName.endsWith('.webp')) {
    return `[Image upload: ${file.name}]`;
  }
  if (lowerName.endsWith('.pdf')) return `[PDF upload: ${file.name}]`;
  if (lowerName.endsWith('.xlsx')) return `[Spreadsheet upload: ${file.name}]`;
  return `[File upload: ${file.name}]`;
}

function describeImportedFiles(files: ReadonlyArray<File>): string {
  if (files.length === 0) return '';
  if (files.length === 1) return describeImportedFile(files[0]);
  return `[Batch upload: ${buildBatchUploadLabel(files.length)} including ${buildCombinedFileLabel(files.map(file => file.name))}]`;
}

async function readImportedFile(file: File): Promise<string> {
  const lowerName = file.name.toLowerCase();
  if (
    lowerName.endsWith('.txt')
    || lowerName.endsWith('.csv')
    || lowerName.endsWith('.md')
    || lowerName.endsWith('.html')
    || lowerName.endsWith('.htm')
    || lowerName.endsWith('.markdown')
    || lowerName.endsWith('.json')
    || lowerName.endsWith('.yaml')
    || lowerName.endsWith('.yml')
    || lowerName.endsWith('.xml')
    || lowerName.endsWith('.eml')
    || lowerName.endsWith('.msg')
    || lowerName.endsWith('.vtt')
    || file.type.startsWith('text/')
  ) {
    return file.text();
  }
  if (lowerName.endsWith('.png') || lowerName.endsWith('.jpg') || lowerName.endsWith('.jpeg') || lowerName.endsWith('.webp')) {
    return `[Image upload: ${file.name}]`;
  }
  if (lowerName.endsWith('.pdf')) {
    return `[PDF upload: ${file.name}]`;
  }
  if (lowerName.endsWith('.xlsx')) {
    return `[Spreadsheet upload: ${file.name}]`;
  }
  if (lowerName.endsWith('.docx')) {
    return `[Document upload: ${file.name}]`;
  }
  if (lowerName.endsWith('.zip')) {
    return `[ZIP package upload: ${file.name}]`;
  }
  return `[Binary upload: ${file.name}]`;
}

function combineImportedText(files: ReadonlyArray<File>, contents: ReadonlyArray<string>): string {
  if (files.length === 0) return '';
  if (files.length === 1) return contents[0] ?? '';

  return files
    .map((file, index) => `Source file: ${file.name}\n${contents[index] ?? describeImportedFile(file)}`)
    .join('\n\n');
}

export const useRequirementStore = defineStore('requirement', () => {
  let importPollingHandle: number | null = null;
  let importPollingToken = 0;

  const listData = ref<RequirementList | null>(null);
  const listLoading = ref(false);
  const listError = ref<string | null>(null);
  const filters = ref<RequirementFilters>({ showCompleted: false });
  const viewMode = ref<ViewMode>('list');
  const sortField = ref<SortField>('priority');
  const sortAsc = ref(true);

  const detail = ref<RequirementDetail | null>(null);
  const detailLoading = ref(false);
  const detailError = ref<string | null>(null);
  const selectedRequirementId = ref<string | null>(null);

  const activeProfile = ref<PipelineProfile>(getActiveProfile());
  const orchestratorResult = ref<OrchestratorResult | null>(null);
  const skillMessage = ref<string | null>(null);

  const INITIAL_IMPORT: ImportState = {
    isOpen: false,
    step: 'source',
    sourceType: 'paste',
    rawInput: '',
    kbName: DEFAULT_KB_NAME,
    fileName: null,
    fileSize: null,
    fileNames: [],
    fileCount: 0,
    error: null,
    draft: null,
    importId: null,
    taskId: null,
    importStatus: null,
    importMessage: null,
    importDatasetId: null,
    importFiles: [],
    supportedFileTypes: [],
    unsupportedFileTypes: [],
    importSuccessCount: 0,
    importFailureCount: 0,
    importUpdatedAt: null,
    batchRows: [],
    batchDrafts: [],
    batchProgress: 0,
    batchTotal: 0,
    columnMapping: {},
  };

  const importState = ref<ImportState>({ ...INITIAL_IMPORT });

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
    if (!USE_MOCK) {
      return filteredRequirements.value;
    }

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
      return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
    });
    return items;
  });

  const statusDistribution = computed<StatusDistribution>(
    () => listData.value?.statusDistribution ?? {
      draft: 0,
      inReview: 0,
      approved: 0,
      inProgress: 0,
      delivered: 0,
      archived: 0,
    },
  );

  function buildSourceInput(textOverride?: string): RequirementSourceInput {
    const fileNames = importState.value.fileNames;
    return {
      sourceType: mapImportSource(importState.value.sourceType),
      text: textOverride ?? importState.value.rawInput,
      fileName: buildCombinedFileLabel(fileNames) ?? importState.value.fileName,
      fileSize: importState.value.fileSize,
      fileNames,
      fileCount: importState.value.fileCount,
      kbName: importState.value.kbName,
    };
  }

  function stopImportPolling() {
    importPollingToken += 1;
    if (importPollingHandle !== null) {
      window.clearTimeout(importPollingHandle);
      importPollingHandle = null;
    }
  }

  function applyImportStatus(status: RequirementImportStatus, sourceAttachment: RequirementSourceInput) {
    importState.value = {
      ...importState.value,
      step: status.draft ? 'review' : 'processing',
      draft: status.draft
        ? {
            ...status.draft,
            sourceAttachment,
          }
        : null,
      importId: status.importId,
      taskId: status.taskId,
      importStatus: status.status,
      importMessage: status.message,
      importDatasetId: status.datasetId,
      importFiles: status.files,
      supportedFileTypes: status.supportedFileTypes,
      unsupportedFileTypes: status.unsupportedFileTypes,
      importSuccessCount: status.numberOfSuccesses,
      importFailureCount: status.numberOfFailures,
      importUpdatedAt: status.updatedAt,
      error: null,
    };
  }

  function scheduleImportPoll(importId: string, sourceAttachment: RequirementSourceInput, token: number, delayMs = 1500) {
    importPollingHandle = window.setTimeout(async () => {
      if (token !== importPollingToken || !importState.value.isOpen) {
        return;
      }

      try {
        const status = await requirementApi.getRequirementImportStatus(importId);
        if (token !== importPollingToken || !importState.value.isOpen) {
          return;
        }

        applyImportStatus(status, sourceAttachment);

        if (status.draft) {
          importPollingHandle = null;
          return;
        }

        if (status.status === 'FAILED') {
          importPollingHandle = null;
          importState.value = {
            ...importState.value,
            step: 'source',
            error: status.message || 'Knowledge base import failed.',
          };
          return;
        }

        scheduleImportPoll(importId, sourceAttachment, token, 1500);
      } catch (error) {
        if (token !== importPollingToken || !importState.value.isOpen) {
          return;
        }
        importPollingHandle = null;
        importState.value = {
          ...importState.value,
          step: 'source',
          error: toUserMessage(error, 'Failed to refresh knowledge base import status.'),
        };
      }
    }, delayMs);
  }

  async function fetchRequirementList() {
    listLoading.value = true;
    listError.value = null;
    try {
      if (USE_MOCK) {
        listData.value = MOCK_REQUIREMENT_LIST;
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
      listData.value = {
        ...result,
        requirements: result.requirements ?? result.items ?? [],
      };
    } catch (error) {
      console.error('Failed to fetch requirement list:', error);
      listError.value = toUserMessage(error, 'Failed to load requirements. Please try again later.');
    } finally {
      listLoading.value = false;
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
      }
    } catch (error) {
      console.error('Failed to fetch requirement detail:', error);
      detailError.value = toUserMessage(error, 'Failed to load requirement detail. Please try again later.');
    } finally {
      detailLoading.value = false;
    }
  }

  async function generateStories(requirementId: string) {
    if (USE_MOCK) {
      console.info(`[stub] Story generation triggered for ${requirementId}`);
      return;
    }
    const result = await requirementApi.triggerStoryGeneration(requirementId);
    skillMessage.value = result.message;
  }

  async function generateSpec(requirementId: string, storyIds: ReadonlyArray<string>) {
    if (storyIds.length === 0) {
      throw new Error('At least one linked story is required to generate a spec');
    }
    if (USE_MOCK) {
      console.info(`[stub] Spec generation triggered for ${requirementId}`, storyIds);
      return;
    }
    const result = await requirementApi.triggerSpecGeneration(requirementId, storyIds);
    skillMessage.value = result.message;
  }

  async function runAnalysis(requirementId: string) {
    if (USE_MOCK) {
      console.info(`[stub] AI analysis triggered for ${requirementId}`);
      return;
    }
    const result = await requirementApi.triggerAnalysis(requirementId);
    skillMessage.value = result.message;
  }

  function setFilters(newFilters: Partial<RequirementFilters>) {
    const previous = filters.value;
    filters.value = { ...filters.value, ...newFilters };

    if (!USE_MOCK) {
      const serverRelevantChanged = previous.priority !== filters.value.priority
        || previous.status !== filters.value.status
        || previous.category !== filters.value.category
        || previous.search !== filters.value.search;
      if (serverRelevantChanged) {
        void fetchRequirementList();
      }
    }
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
    if (!USE_MOCK) {
      void fetchRequirementList();
    }
  }

  function clearDetail() {
    detail.value = null;
    detailError.value = null;
    selectedRequirementId.value = null;
    orchestratorResult.value = null;
    skillMessage.value = null;
  }

  async function loadActiveProfile(workspaceId?: string) {
    orchestratorResult.value = null;
    skillMessage.value = null;
    if (USE_MOCK) {
      activeProfile.value = getActiveProfile(workspaceId);
      return;
    }
    try {
      activeProfile.value = await requirementApi.getActiveProfile();
    } catch (error) {
      console.error('Failed to load active profile:', error);
      activeProfile.value = getActiveProfile(workspaceId);
    }
  }

  async function invokeProfileSkill(requirementId: string, skillId: string) {
    skillMessage.value = null;
    if (USE_MOCK) {
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

    if (!activeProfile.value.usesOrchestrator) {
      if (skillId === 'req-to-user-story') {
        await generateStories(requirementId);
        return;
      }
      if (skillId === 'user-story-to-spec') {
        const stories = detail.value?.linkedStories.data?.stories ?? [];
        const candidate = stories.find(story => story.specId === null) ?? stories[0];
        if (!candidate) {
          throw new Error('No linked stories are available to generate a spec');
        }
        await generateSpec(requirementId, [candidate.id]);
        return;
      }
    }

    const result = await requirementApi.invokeProfileSkill(requirementId, skillId);
    orchestratorResult.value = result.orchestratorResult ?? null;
    skillMessage.value = result.message;
  }

  function openImport() {
    stopImportPolling();
    importState.value = { ...INITIAL_IMPORT, isOpen: true };
  }

  function closeImport() {
    stopImportPolling();
    importState.value = { ...INITIAL_IMPORT };
  }

  function setImportSource(source: ImportSourceType) {
    importState.value = { ...importState.value, sourceType: source, error: null };
  }

  function setRawInput(text: string) {
    importState.value = { ...importState.value, rawInput: text, error: null };
  }

  async function triggerNormalization() {
    if (!importState.value.rawInput.trim() && importState.value.fileCount === 0) {
      importState.value = {
        ...importState.value,
        error: 'Provide source content before running normalization.',
      };
      return;
    }

    importState.value = { ...importState.value, step: 'normalizing', error: null };

    if (USE_MOCK) {
      const batchUpload = importState.value.fileCount > 1;
      const zipArchive = importState.value.fileNames.some(fileName => isZipArchive(fileName));
      const sourceLabel = buildCombinedFileLabel(importState.value.fileNames) ?? importState.value.fileName;
      const draftTitle = batchUpload
        ? `Imported requirement package from ${buildBatchUploadLabel(importState.value.fileCount)}`
        : zipArchive && sourceLabel
          ? `Imported requirement package from ${sourceLabel}`
          : importState.value.rawInput.slice(0, 80) || 'Untitled Requirement';
      const draftSummary = batchUpload
        ? `Imported ${buildBatchUploadLabel(importState.value.fileCount)} for KB ${importState.value.kbName}: ${sourceLabel}. Review the file bundle and confirm which source is authoritative before creating the requirement.`
        : zipArchive && sourceLabel
          ? `Imported mixed-source archive from ${sourceLabel}. Review the package manifest and identify the authoritative files before confirming the requirement.`
          : importState.value.rawInput.slice(0, 220);
      window.setTimeout(() => {
        const draft: RequirementDraft = {
          title: draftTitle,
          priority: 'Medium',
          category: 'Functional',
          summary: draftSummary,
          businessJustification: 'Business value to be determined based on stakeholder input.',
          acceptanceCriteria: zipArchive
            ? ['Archive contents and source-of-truth documents are identified before confirmation']
            : ['Criterion extracted from input text'],
          assumptions: ['Input is accurate and complete'],
          constraints: [],
          missingInfo: batchUpload
            ? [
              'Business justification needs elaboration',
              'Priority not specified in source',
              'Multiple uploaded files were provided — confirm the single source of truth before confirmation',
            ]
            : zipArchive
            ? [
              'Business justification needs elaboration',
              'Priority not specified in source',
              'ZIP package contents are not unpacked in mock mode — add a manifest or highlight the authoritative files',
            ]
            : ['Business justification needs elaboration', 'Priority not specified in source'],
          openQuestions: batchUpload
            ? ['Who is the primary stakeholder?', 'Should these uploaded files be merged into one requirement or split into several?']
            : zipArchive
            ? ['Who is the primary stakeholder?', 'Which files inside the ZIP package should drive normalization?']
            : ['Who is the primary stakeholder?', 'What is the target timeline?'],
          aiSuggestedFields: ['title', 'priority', 'category', 'summary'],
          sourceAttachment: buildSourceInput(),
        };
        importState.value = { ...importState.value, step: 'review', draft };
      }, 600);
      return;
    }

    try {
      const draft = await requirementApi.normalizeRequirement({
        rawInput: buildSourceInput(),
        profileId: activeProfile.value.id,
      });
      importState.value = {
        ...importState.value,
        step: 'review',
        draft: {
          ...draft,
          sourceAttachment: buildSourceInput(),
        },
      };
    } catch (error) {
      console.error('Failed to normalize requirement source:', error);
      importState.value = {
        ...importState.value,
        step: 'source',
        error: toUserMessage(error, 'Failed to normalize source material.'),
      };
    }
  }

  async function confirmDraft(draft: RequirementDraft) {
    if (USE_MOCK) {
      console.info('[stub] Requirement created from import draft:', draft.title);
      closeImport();
      return;
    }

    await requirementApi.createRequirement({
      title: draft.title,
      priority: draft.priority,
      category: draft.category,
      summary: draft.summary,
      businessJustification: draft.businessJustification,
      acceptanceCriteria: draft.acceptanceCriteria,
      assumptions: draft.assumptions,
      constraints: draft.constraints,
      sourceAttachment: draft.sourceAttachment ?? buildSourceInput(),
    });
    await fetchRequirementList();
    closeImport();
  }

  function discardDraft() {
    stopImportPolling();
    importState.value = {
      ...importState.value,
      step: 'source',
      error: null,
      draft: null,
      importId: null,
      taskId: null,
      importStatus: null,
      importMessage: null,
      importDatasetId: null,
      importFiles: [],
      supportedFileTypes: [],
      unsupportedFileTypes: [],
      importSuccessCount: 0,
      importFailureCount: 0,
      importUpdatedAt: null,
      batchDrafts: [],
      batchRows: [],
      batchProgress: 0,
      batchTotal: 0,
    };
  }

  async function handleFileImport(files: File[]) {
    const uploadedFiles = [...files];
    const fileNames = uploadedFiles.map(file => file.name);
    const totalFileSize = uploadedFiles.reduce((total, file) => total + file.size, 0);

    importState.value = {
      ...importState.value,
      fileName: buildCombinedFileLabel(fileNames),
      fileSize: totalFileSize,
      fileNames,
      fileCount: uploadedFiles.length,
      rawInput: describeImportedFiles(uploadedFiles),
      error: null,
    };

    if (!USE_MOCK) {
      stopImportPolling();
      importState.value = {
        ...importState.value,
        step: 'processing',
        importId: null,
        taskId: null,
        importStatus: 'SUBMITTING',
        importMessage: 'Submitting files to the knowledge base...',
        importDatasetId: null,
        importFiles: [],
        supportedFileTypes: [],
        unsupportedFileTypes: [],
        importSuccessCount: 0,
        importFailureCount: 0,
        importUpdatedAt: null,
      };
      try {
        const receipt = await requirementApi.startRequirementImport(
          uploadedFiles,
          importState.value.kbName,
          activeProfile.value.id,
        );
        const sourceAttachment = buildSourceInput();
        applyImportStatus(receipt, sourceAttachment);
        if (receipt.draft) {
          return;
        }
        if (receipt.status === 'FAILED') {
          importState.value = {
            ...importState.value,
            step: 'source',
            error: receipt.message || 'Knowledge base import failed.',
          };
          return;
        }
        const pollingToken = ++importPollingToken;
        scheduleImportPoll(receipt.importId, sourceAttachment, pollingToken, 1200);
      } catch (error) {
        console.error('Failed to upload and normalize import file:', error);
        importState.value = {
          ...importState.value,
          step: 'source',
          error: toUserMessage(error, 'Failed to process the selected file.'),
        };
      }
      return;
    }

    try {
      const text = combineImportedText(uploadedFiles, await Promise.all(uploadedFiles.map(readImportedFile)));
      importState.value = {
        ...importState.value,
        rawInput: text,
      };
      await triggerNormalization();
    } catch (error) {
      console.error('Failed to read import file:', error);
      importState.value = {
        ...importState.value,
        step: 'source',
        error: toUserMessage(error, 'Failed to read the selected file.'),
      };
    }
  }

  function updateColumnMapping(column: string, target: string) {
    importState.value = {
      ...importState.value,
      columnMapping: { ...importState.value.columnMapping, [column]: target },
    };
  }

  async function normalizeSelected(indices: number[]) {
    const total = indices.length;
    importState.value = { ...importState.value, step: 'batch-normalizing', batchProgress: 0, batchTotal: total };

    if (USE_MOCK) {
      let current = 0;
      const interval = window.setInterval(() => {
        current += 1;
        importState.value = { ...importState.value, batchProgress: current };
        if (current >= total) {
          window.clearInterval(interval);
          const drafts: RequirementDraft[] = indices.map(idx => ({
            title: `Imported Requirement ${idx + 1}`,
            priority: 'Medium',
            category: 'Functional',
            summary: '',
            businessJustification: '',
            acceptanceCriteria: [],
            assumptions: [],
            constraints: [],
            missingInfo: ['Business justification required'],
            openQuestions: [],
            aiSuggestedFields: ['title', 'priority', 'category'],
          }));
          importState.value = { ...importState.value, step: 'batch-review', batchDrafts: drafts };
        }
      }, 250);
      return;
    }

    const drafts: RequirementDraft[] = [];
    for (let index = 0; index < indices.length; index += 1) {
      const row = importState.value.batchRows[indices[index]] ?? {};
      const text = Object.entries(row).map(([key, value]) => `${key}: ${value}`).join('\n');
      const draft = await requirementApi.normalizeRequirement({
        rawInput: buildSourceInput(text),
        profileId: activeProfile.value.id,
      });
      drafts.push(draft);
      importState.value = { ...importState.value, batchProgress: index + 1 };
    }

    importState.value = { ...importState.value, step: 'batch-review', batchDrafts: drafts };
  }

  async function confirmAllDrafts() {
    if (USE_MOCK) {
      console.info(`[stub] ${importState.value.batchDrafts.length} requirements created from batch import`);
      closeImport();
      return;
    }

    for (const draft of importState.value.batchDrafts) {
      await requirementApi.createRequirement({
        title: draft.title,
        priority: draft.priority,
        category: draft.category,
        summary: draft.summary,
        businessJustification: draft.businessJustification,
        acceptanceCriteria: draft.acceptanceCriteria,
        assumptions: draft.assumptions,
        constraints: draft.constraints,
        sourceAttachment: draft.sourceAttachment ?? buildSourceInput(),
      });
    }

    await fetchRequirementList();
    closeImport();
  }

  return {
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
    detail,
    detailLoading,
    detailError,
    selectedRequirementId,
    activeProfile,
    orchestratorResult,
    skillMessage,
    importState,
    fetchRequirementList,
    fetchRequirementDetail,
    generateStories,
    generateSpec,
    runAnalysis,
    setFilters,
    setViewMode,
    setSortField,
    clearDetail,
    loadActiveProfile,
    invokeProfileSkill,
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
