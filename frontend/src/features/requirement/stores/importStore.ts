import { defineStore } from 'pinia';
import { ref } from 'vue';
import type {
  ImportSourceType,
  ImportState,
  RequirementDraft,
  RequirementImportStatus,
  RequirementSourceInput,
} from '../types/requirement';
import { requirementApi } from '../api/requirementApi';
import {
  buildBatchUploadLabel,
  buildCombinedFileLabel,
  combineImportedText,
  describeImportedFiles,
  isZipArchive,
  mapImportSourceType,
  readImportedFile,
} from '../utils/importFiles';
import { toUserMessage, devLog } from '../utils/storeUtils';

const USE_MOCK = import.meta.env.DEV && !import.meta.env.VITE_USE_BACKEND;
const DEFAULT_KB_NAME = import.meta.env.VITE_REQUIREMENT_KB_NAME?.trim() || 'requirement-intake';

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

export const useImportStore = defineStore('requirementImport', () => {
  let pollingHandle: number | null = null;
  let pollingToken = 0;

  const importState = ref<ImportState>({ ...INITIAL_IMPORT });

  function buildSourceInput(textOverride?: string): RequirementSourceInput {
    const { fileNames, rawInput, fileName, fileSize, fileCount, kbName, sourceType } = importState.value;
    return {
      sourceType: mapImportSourceType(sourceType),
      text: textOverride ?? rawInput,
      fileName: buildCombinedFileLabel(fileNames) ?? fileName,
      fileSize,
      fileNames,
      fileCount,
      kbName,
    };
  }

  function stopPolling() {
    pollingToken += 1;
    if (pollingHandle !== null) {
      window.clearTimeout(pollingHandle);
      pollingHandle = null;
    }
  }

  function applyImportStatus(status: RequirementImportStatus, sourceAttachment: RequirementSourceInput) {
    importState.value = {
      ...importState.value,
      step: status.draft ? 'review' : 'processing',
      draft: status.draft ? { ...status.draft, sourceAttachment } : null,
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

  function scheduleImportPoll(
    importId: string,
    sourceAttachment: RequirementSourceInput,
    token: number,
    delayMs = 1500,
  ) {
    pollingHandle = window.setTimeout(async () => {
      if (token !== pollingToken || !importState.value.isOpen) return;
      try {
        const status = await requirementApi.getRequirementImportStatus(importId);
        if (token !== pollingToken || !importState.value.isOpen) return;

        applyImportStatus(status, sourceAttachment);

        if (status.draft) { pollingHandle = null; return; }

        if (status.status === 'FAILED') {
          pollingHandle = null;
          importState.value = {
            ...importState.value,
            step: 'source',
            error: status.message || 'Knowledge base import failed.',
          };
          return;
        }

        scheduleImportPoll(importId, sourceAttachment, token, 1500);
      } catch (error) {
        if (token !== pollingToken || !importState.value.isOpen) return;
        pollingHandle = null;
        importState.value = {
          ...importState.value,
          step: 'source',
          error: toUserMessage(error, 'Failed to refresh knowledge base import status.'),
        };
      }
    }, delayMs);
  }

  // ── Public actions ──

  function openImport() {
    stopPolling();
    importState.value = { ...INITIAL_IMPORT, isOpen: true };
  }

  function closeImport() {
    stopPolling();
    importState.value = { ...INITIAL_IMPORT };
  }

  function setImportSource(source: ImportSourceType) {
    importState.value = { ...importState.value, sourceType: source, error: null };
  }

  function setRawInput(text: string) {
    importState.value = { ...importState.value, rawInput: text, error: null };
  }

  function discardDraft() {
    stopPolling();
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

  function updateColumnMapping(column: string, target: string) {
    importState.value = {
      ...importState.value,
      columnMapping: { ...importState.value.columnMapping, [column]: target },
    };
  }

  async function triggerNormalization(activeProfileId: string) {
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
      const zipArchive = importState.value.fileNames.some(n => isZipArchive(n));
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
            ? ['Business justification needs elaboration', 'Priority not specified in source', 'Multiple uploaded files were provided — confirm the single source of truth before confirmation']
            : zipArchive
              ? ['Business justification needs elaboration', 'Priority not specified in source', 'ZIP package contents are not unpacked in mock mode — add a manifest or highlight the authoritative files']
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
        profileId: activeProfileId,
      });
      importState.value = {
        ...importState.value,
        step: 'review',
        draft: { ...draft, sourceAttachment: buildSourceInput() },
      };
    } catch (error) {
      devLog.error('Failed to normalize requirement source:', error);
      importState.value = {
        ...importState.value,
        step: 'source',
        error: toUserMessage(error, 'Failed to normalize source material.'),
      };
    }
  }

  async function handleFileImport(files: File[], activeProfileId: string) {
    const uploadedFiles = [...files];
    const fileNames = uploadedFiles.map(f => f.name);
    const totalFileSize = uploadedFiles.reduce((total, f) => total + f.size, 0);

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
      stopPolling();
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
          activeProfileId,
        );
        const sourceAttachment = buildSourceInput();
        applyImportStatus(receipt, sourceAttachment);
        if (receipt.draft) return;
        if (receipt.status === 'FAILED') {
          importState.value = {
            ...importState.value,
            step: 'source',
            error: receipt.message || 'Knowledge base import failed.',
          };
          return;
        }
        const token = ++pollingToken;
        scheduleImportPoll(receipt.importId, sourceAttachment, token, 1200);
      } catch (error) {
        devLog.error('Failed to upload and normalize import file:', error);
        importState.value = {
          ...importState.value,
          step: 'source',
          error: toUserMessage(error, 'Failed to process the selected file.'),
        };
      }
      return;
    }

    try {
      const text = combineImportedText(
        uploadedFiles,
        await Promise.all(uploadedFiles.map(readImportedFile)),
      );
      importState.value = { ...importState.value, rawInput: text };
      await triggerNormalization(activeProfileId);
    } catch (error) {
      devLog.error('Failed to read import file:', error);
      importState.value = {
        ...importState.value,
        step: 'source',
        error: toUserMessage(error, 'Failed to read the selected file.'),
      };
    }
  }

  async function normalizeSelected(indices: number[], activeProfileId: string) {
    const total = indices.length;
    importState.value = { ...importState.value, step: 'batch-normalizing', batchProgress: 0, batchTotal: total };

    if (USE_MOCK) {
      let current = 0;
      const interval = window.setInterval(() => {
        current += 1;
        importState.value = { ...importState.value, batchProgress: current };
        if (current >= total) {
          window.clearInterval(interval);
          const drafts: RequirementDraft[] = indices.map((idx) => ({
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
    for (let i = 0; i < indices.length; i += 1) {
      const row = importState.value.batchRows[indices[i]] ?? {};
      const text = Object.entries(row).map(([k, v]) => `${k}: ${v}`).join('\n');
      const draft = await requirementApi.normalizeRequirement({
        rawInput: buildSourceInput(text),
        profileId: activeProfileId,
      });
      drafts.push(draft);
      importState.value = { ...importState.value, batchProgress: i + 1 };
    }
    importState.value = { ...importState.value, step: 'batch-review', batchDrafts: drafts };
  }

  async function confirmDraft(draft: RequirementDraft, afterConfirm: () => Promise<void>) {
    if (USE_MOCK) {
      devLog.info('[stub] Requirement created from import draft:', draft.title);
      closeImport();
      return;
    }

    const source = draft.sourceAttachment ?? buildSourceInput();
    const created = await requirementApi.createRequirement({
      title: draft.title,
      priority: draft.priority,
      category: draft.category,
      summary: draft.summary,
      businessJustification: draft.businessJustification,
      acceptanceCriteria: draft.acceptanceCriteria,
      assumptions: draft.assumptions,
      constraints: draft.constraints,
      sourceAttachment: source,
    });
    if (source.fileName || source.text || source.kbName) {
      await requirementApi.createSourceReference(created.id, {
        sourceType: source.sourceType === 'FILE' ? 'UPLOAD' : 'URL',
        url: source.fileName ? `upload://${source.fileName}` : 'manual://intake',
        title: source.fileName ?? 'Manual intake source',
        externalId: source.fileName ?? source.sourceType,
      });
    }
    await afterConfirm();
    closeImport();
  }

  async function confirmAllDrafts(afterConfirm: () => Promise<void>) {
    if (USE_MOCK) {
      devLog.info(`[stub] ${importState.value.batchDrafts.length} requirements created from batch import`);
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
    await afterConfirm();
    closeImport();
  }

  return {
    importState,
    openImport,
    closeImport,
    stopPolling,
    setImportSource,
    setRawInput,
    discardDraft,
    updateColumnMapping,
    triggerNormalization,
    handleFileImport,
    normalizeSelected,
    confirmDraft,
    confirmAllDrafts,
  };
});
