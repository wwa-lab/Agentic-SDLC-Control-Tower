import { setActivePinia, createPinia } from 'pinia';
import { beforeEach, describe, it, expect, vi } from 'vitest';
import {
  isZipArchive,
  buildCombinedFileLabel,
  buildBatchUploadLabel,
  describeImportedFile,
  describeImportedFiles,
  combineImportedText,
  mapImportSourceType,
} from '../utils/importFiles';
import { useImportStore } from '../stores/importStore';
import type { RequirementDraft } from '../types/requirement';

vi.mock('../api/requirementApi', () => ({
  requirementApi: {
    normalizeRequirement: vi.fn(),
    createRequirement: vi.fn(),
    createSourceReference: vi.fn(),
    startRequirementImport: vi.fn(),
    getRequirementImportStatus: vi.fn(),
  },
}));

// ── helpers ──────────────────────────────────────────────────────────────────

function makeFile(name: string, content = '', type = 'text/plain'): File {
  return new File([content], name, { type });
}

function makeDraft(overrides: Partial<RequirementDraft> = {}): RequirementDraft {
  return {
    title: 'Test Requirement',
    priority: 'Medium',
    category: 'Functional',
    summary: 'A summary',
    businessJustification: 'Because',
    acceptanceCriteria: ['Criterion A'],
    assumptions: [],
    constraints: [],
    missingInfo: [],
    openQuestions: [],
    aiSuggestedFields: [],
    ...overrides,
  };
}

// ── isZipArchive ─────────────────────────────────────────────────────────────

describe('isZipArchive', () => {
  it('returns true for .zip files', () => {
    expect(isZipArchive('archive.zip')).toBe(true);
    expect(isZipArchive('UPPER.ZIP')).toBe(true);
  });

  it('returns false for non-zip files', () => {
    expect(isZipArchive('file.pdf')).toBe(false);
    expect(isZipArchive('file.docx')).toBe(false);
    expect(isZipArchive('')).toBe(false);
    expect(isZipArchive(null)).toBe(false);
    expect(isZipArchive(undefined)).toBe(false);
  });
});

// ── buildCombinedFileLabel ────────────────────────────────────────────────────

describe('buildCombinedFileLabel', () => {
  it('returns null for empty array', () => {
    expect(buildCombinedFileLabel([])).toBeNull();
  });

  it('returns the file name for single file', () => {
    expect(buildCombinedFileLabel(['spec.md'])).toBe('spec.md');
  });

  it('joins two names with comma', () => {
    expect(buildCombinedFileLabel(['a.txt', 'b.txt'])).toBe('a.txt, b.txt');
  });

  it('summarises three or more with "+ N more"', () => {
    expect(buildCombinedFileLabel(['a.txt', 'b.txt', 'c.txt'])).toBe('a.txt + 2 more');
    expect(buildCombinedFileLabel(['a', 'b', 'c', 'd'])).toBe('a + 3 more');
  });
});

// ── buildBatchUploadLabel ─────────────────────────────────────────────────────

describe('buildBatchUploadLabel', () => {
  it('uses singular for 1 file', () => {
    expect(buildBatchUploadLabel(1)).toBe('1 file');
  });

  it('uses plural for multiple files', () => {
    expect(buildBatchUploadLabel(3)).toBe('3 files');
  });
});

// ── describeImportedFile ──────────────────────────────────────────────────────

describe('describeImportedFile', () => {
  it.each([
    ['archive.zip', '[ZIP package upload: archive.zip]'],
    ['report.docx', '[Document upload: report.docx]'],
    ['photo.png', '[Image upload: photo.png]'],
    ['photo.jpg', '[Image upload: photo.jpg]'],
    ['scan.pdf', '[PDF upload: scan.pdf]'],
    ['data.xlsx', '[Spreadsheet upload: data.xlsx]'],
    ['notes.txt', '[File upload: notes.txt]'],
  ])('describes %s correctly', (name, expected) => {
    expect(describeImportedFile(makeFile(name))).toBe(expected);
  });
});

// ── describeImportedFiles ─────────────────────────────────────────────────────

describe('describeImportedFiles', () => {
  it('returns empty string for no files', () => {
    expect(describeImportedFiles([])).toBe('');
  });

  it('delegates to describeImportedFile for one file', () => {
    expect(describeImportedFiles([makeFile('spec.md')])).toBe('[File upload: spec.md]');
  });

  it('returns batch label for multiple files', () => {
    const files = [makeFile('a.txt'), makeFile('b.txt')];
    const label = describeImportedFiles(files);
    expect(label).toContain('Batch upload');
    expect(label).toContain('2 files');
  });
});

// ── combineImportedText ───────────────────────────────────────────────────────

describe('combineImportedText', () => {
  it('returns empty string for no files', () => {
    expect(combineImportedText([], [])).toBe('');
  });

  it('returns sole content directly for one file', () => {
    expect(combineImportedText([makeFile('a.txt')], ['hello'])).toBe('hello');
  });

  it('prefixes each file content with its name for multiple files', () => {
    const files = [makeFile('a.txt'), makeFile('b.txt')];
    const result = combineImportedText(files, ['content A', 'content B']);
    expect(result).toContain('Source file: a.txt');
    expect(result).toContain('Source file: b.txt');
    expect(result).toContain('content A');
    expect(result).toContain('content B');
  });
});

// ── mapImportSourceType ───────────────────────────────────────────────────────

describe('mapImportSourceType', () => {
  it.each([
    ['file', 'FILE'],
    ['email', 'EMAIL'],
    ['meeting', 'MEETING'],
    ['paste', 'TEXT'],
  ] as const)('maps %s → %s', (input, expected) => {
    expect(mapImportSourceType(input)).toBe(expected);
  });
});

// ── importStore state management ──────────────────────────────────────────────

describe('importStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.stubEnv('DEV', true);
    vi.stubGlobal('localStorage', {
      getItem: vi.fn(() => null),
      setItem: vi.fn(),
      removeItem: vi.fn(),
    });
  });

  it('starts with isOpen=false', () => {
    const store = useImportStore();
    expect(store.importState.isOpen).toBe(false);
  });

  it('openImport sets isOpen=true and resets to source step', () => {
    const store = useImportStore();
    store.openImport();
    expect(store.importState.isOpen).toBe(true);
    expect(store.importState.step).toBe('source');
    expect(store.importState.error).toBeNull();
  });

  it('closeImport resets all state', () => {
    const store = useImportStore();
    store.openImport();
    store.setRawInput('some text');
    store.closeImport();
    expect(store.importState.isOpen).toBe(false);
    expect(store.importState.rawInput).toBe('');
  });

  it('setImportSource changes sourceType and clears error', () => {
    const store = useImportStore();
    store.openImport();
    store.setImportSource('email');
    expect(store.importState.sourceType).toBe('email');
    expect(store.importState.error).toBeNull();
  });

  it('setRawInput updates text and clears error', () => {
    const store = useImportStore();
    store.openImport();
    store.setRawInput('hello world');
    expect(store.importState.rawInput).toBe('hello world');
    expect(store.importState.error).toBeNull();
  });

  it('updateColumnMapping adds new mappings immutably', () => {
    const store = useImportStore();
    store.updateColumnMapping('col1', 'title');
    store.updateColumnMapping('col2', 'priority');
    expect(store.importState.columnMapping).toEqual({ col1: 'title', col2: 'priority' });
  });

  it('updateColumnMapping overwrites existing key', () => {
    const store = useImportStore();
    store.updateColumnMapping('col1', 'title');
    store.updateColumnMapping('col1', 'summary');
    expect(store.importState.columnMapping['col1']).toBe('summary');
  });

  it('discardDraft resets to source step and clears draft fields', () => {
    const store = useImportStore();
    store.openImport();
    // Simulate a draft being present via internal state mutation (allowed in tests)
    store.importState.step = 'review';
    store.importState.draft = makeDraft();
    store.importState.importId = 'imp-123';

    store.discardDraft();

    expect(store.importState.step).toBe('source');
    expect(store.importState.draft).toBeNull();
    expect(store.importState.importId).toBeNull();
  });

  describe('triggerNormalization', () => {
    it('sets error when rawInput is empty and no files', async () => {
      const store = useImportStore();
      store.openImport();
      await store.triggerNormalization('profile-1');
      expect(store.importState.error).toBeTruthy();
      expect(store.importState.step).toBe('source');
    });

    it('moves to normalizing then review in mock mode', async () => {
      vi.useFakeTimers();
      const store = useImportStore();
      store.openImport();
      store.setRawInput('Requirement: support SSO login');

      const promise = store.triggerNormalization('profile-1');
      expect(store.importState.step).toBe('normalizing');

      vi.runAllTimers();
      await promise;

      expect(store.importState.step).toBe('review');
      expect(store.importState.draft).not.toBeNull();
      vi.useRealTimers();
    });
  });

  describe('confirmDraft (mock mode)', () => {
    it('closes import without calling API', async () => {
      const store = useImportStore();
      store.openImport();
      const afterConfirm = vi.fn().mockResolvedValue(undefined);
      await store.confirmDraft(makeDraft(), afterConfirm);
      // In mock mode confirmDraft skips afterConfirm and closes
      expect(store.importState.isOpen).toBe(false);
    });
  });

  describe('confirmAllDrafts (mock mode)', () => {
    it('closes import without calling API', async () => {
      const store = useImportStore();
      store.openImport();
      store.importState.batchDrafts = [makeDraft(), makeDraft()];
      const afterConfirm = vi.fn().mockResolvedValue(undefined);
      await store.confirmAllDrafts(afterConfirm);
      expect(store.importState.isOpen).toBe(false);
    });
  });
});
