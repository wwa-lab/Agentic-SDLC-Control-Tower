import { setActivePinia, createPinia } from 'pinia';
import { beforeEach, describe, it, expect, vi } from 'vitest';
import { useRequirementStore } from '../stores/requirementStore';
import type { RequirementListItem, RequirementControlPlaneListSummary } from '../types/requirement';

// Mock dependencies so tests don't hit network or ESM boundary issues
vi.mock('../mockData', () => ({
  MOCK_REQUIREMENT_LIST: {
    statusDistribution: { draft: 2, inReview: 1, approved: 0, inProgress: 0, delivered: 0, archived: 0 },
    requirements: [
      { id: 'REQ-001', title: 'Alpha', priority: 'High', status: 'Draft', category: 'Functional', storyCount: 2, specCount: 1, completeness: 60, updatedAt: '2026-01-01T00:00:00Z' },
      { id: 'REQ-002', title: 'Beta', priority: 'Critical', status: 'In Review', category: 'Technical', storyCount: 0, specCount: 0, completeness: 30, updatedAt: '2026-01-02T00:00:00Z' },
      { id: 'REQ-003', title: 'Gamma', priority: 'Low', status: 'Delivered', category: 'Business', storyCount: 3, specCount: 2, completeness: 100, updatedAt: '2026-01-03T00:00:00Z' },
    ],
  },
  MOCK_REQUIREMENT_DETAILS: {},
}));

vi.mock('../profiles', () => ({
  getActiveProfile: () => ({
    id: 'standard-sdd',
    name: 'Standard SDD',
    description: 'Standard SDD profile',
    chainNodes: [],
    skills: [],
    entryPaths: [],
    documentStages: [
      { sddType: 'spec', label: 'Spec', pathPattern: 'docs/spec.md', artifactType: 'MARKDOWN' },
      { sddType: 'arch', label: 'Architecture', pathPattern: 'docs/arch.md', artifactType: 'MARKDOWN' },
    ],
    specTiering: null,
    usesOrchestrator: false,
    traceabilityMode: 'per-layer',
  }),
  getAllProfiles: () => [],
  getProfileById: () => null,
}));

vi.mock('../api/requirementApi', () => ({
  requirementApi: {
    getRequirementList: vi.fn(),
    getTraceability: vi.fn(),
  },
}));

vi.mock('@/shared/api/client', () => ({
  ApiError: class ApiError extends Error {},
}));

// ── helpers ──

function makeSummary(
  requirementId: string,
  status: RequirementControlPlaneListSummary['status'],
): RequirementControlPlaneListSummary {
  return {
    requirementId,
    sourceCount: 1,
    documentCount: 2,
    missingDocumentCount: 0,
    staleReviewCount: 0,
    artifactCount: 0,
    status,
    message: status,
  };
}

// ── tests ──

describe('requirementStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.stubEnv('DEV', true);
    // jsdom's --localstorage-file warning leaves localStorage non-functional
    vi.stubGlobal('localStorage', {
      getItem: vi.fn(() => null),
      setItem: vi.fn(),
      removeItem: vi.fn(),
    });
  });

  describe('filteredRequirements', () => {
    it('hides Delivered and Archived by default', async () => {
      const store = useRequirementStore();
      await store.fetchRequirementList();

      // REQ-003 status is Delivered — should be hidden with default showCompleted=false
      expect(store.filteredRequirements.map(r => r.id)).not.toContain('REQ-003');
      expect(store.filteredRequirements.map(r => r.id)).toContain('REQ-001');
    });

    it('shows completed items when showCompleted=true', async () => {
      const store = useRequirementStore();
      await store.fetchRequirementList();
      store.setFilters({ showCompleted: true });
      expect(store.filteredRequirements.map(r => r.id)).toContain('REQ-003');
    });

    it('filters by priority', async () => {
      const store = useRequirementStore();
      await store.fetchRequirementList();
      store.setFilters({ priority: 'Critical' });
      expect(store.filteredRequirements.every(r => r.priority === 'Critical')).toBe(true);
    });

    it('filters by status', async () => {
      const store = useRequirementStore();
      await store.fetchRequirementList();
      store.setFilters({ status: 'Draft' });
      expect(store.filteredRequirements.every(r => r.status === 'Draft')).toBe(true);
    });

    it('filters by search term (title)', async () => {
      const store = useRequirementStore();
      await store.fetchRequirementList();
      store.setFilters({ search: 'alpha' });
      expect(store.filteredRequirements).toHaveLength(1);
      expect(store.filteredRequirements[0].id).toBe('REQ-001');
    });

    it('filters by search term (id)', async () => {
      const store = useRequirementStore();
      await store.fetchRequirementList();
      store.setFilters({ search: 'req-002' });
      expect(store.filteredRequirements).toHaveLength(1);
      expect(store.filteredRequirements[0].id).toBe('REQ-002');
    });
  });

  describe('sortedRequirements', () => {
    it('sorts by priority ascending by default', async () => {
      const store = useRequirementStore();
      await store.fetchRequirementList();
      const priorities = store.sortedRequirements.map(r => r.priority);
      // Critical(0) < High(1) — REQ-002 first, then REQ-001
      expect(priorities[0]).toBe('Critical');
      expect(priorities[1]).toBe('High');
    });

    it('toggles sort direction when same field selected twice', async () => {
      const store = useRequirementStore();
      await store.fetchRequirementList();
      store.setSortField('priority'); // already priority → toggle desc
      const priorities = store.sortedRequirements.map(r => r.priority);
      expect(priorities[0]).not.toBe('Critical');
    });

    it('sorts by title ascending', async () => {
      const store = useRequirementStore();
      await store.fetchRequirementList();
      store.setSortField('title');
      const titles = store.sortedRequirements.map(r => r.title);
      expect(titles).toEqual([...titles].sort((a, b) => a.localeCompare(b)));
    });
  });

  describe('controlPlaneOverview', () => {
    it('counts fresh, stale, missing, errors from summaries', async () => {
      const store = useRequirementStore();
      await store.fetchRequirementList();

      // Manually inject summaries to test overview without hitting API
      store.controlPlaneSummaries = {
        'REQ-001': makeSummary('REQ-001', 'FRESH'),
        'REQ-002': makeSummary('REQ-002', 'SOURCE_CHANGED'),
        'REQ-003': makeSummary('REQ-003', 'MISSING_DOCUMENT'),
        'REQ-004': makeSummary('REQ-004', 'ERROR'),
        'REQ-005': makeSummary('REQ-005', 'DOCUMENT_CHANGED_AFTER_REVIEW'),
      };

      const overview = store.controlPlaneOverview;
      expect(overview.total).toBe(5);
      expect(overview.fresh).toBe(1);
      expect(overview.stale).toBe(2); // SOURCE_CHANGED + DOCUMENT_CHANGED_AFTER_REVIEW
      expect(overview.missing).toBe(1);
      expect(overview.errors).toBe(1);
    });

    it('returns zero counts when no summaries', () => {
      const store = useRequirementStore();
      const overview = store.controlPlaneOverview;
      expect(overview.total).toBe(0);
      expect(overview.fresh).toBe(0);
      expect(overview.stale).toBe(0);
      expect(overview.missing).toBe(0);
      expect(overview.errors).toBe(0);
    });

    it('sums sources, documents, artifacts across summaries', async () => {
      const store = useRequirementStore();
      store.controlPlaneSummaries = {
        'A': { ...makeSummary('A', 'FRESH'), sourceCount: 2, documentCount: 3, artifactCount: 1 },
        'B': { ...makeSummary('B', 'FRESH'), sourceCount: 1, documentCount: 2, artifactCount: 0 },
      };
      expect(store.controlPlaneOverview.sources).toBe(3);
      expect(store.controlPlaneOverview.documents).toBe(5);
      expect(store.controlPlaneOverview.artifacts).toBe(1);
    });
  });

  describe('statusDistribution', () => {
    it('returns distribution from loaded list data', async () => {
      const store = useRequirementStore();
      await store.fetchRequirementList();
      expect(store.statusDistribution.draft).toBe(2);
      expect(store.statusDistribution.inReview).toBe(1);
    });

    it('returns zeros before list is loaded', () => {
      const store = useRequirementStore();
      expect(store.statusDistribution.draft).toBe(0);
    });
  });
});
