<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useRequirementStore } from '../stores/requirementStore';
import StatusDistribution from '../components/StatusDistribution.vue';
import RequirementListTable from '../components/RequirementListTable.vue';
import PriorityMatrix from '../components/PriorityMatrix.vue';
import SddKnowledgeGraph from '../components/SddKnowledgeGraph.vue';
import ProfileSelector from '../components/ProfileSelector.vue';
import ProfileWorkflowMap from '../components/ProfileWorkflowMap.vue';
import ControlPlaneSummaryStrip from '../components/ControlPlaneSummaryStrip.vue';
import ImportPanel from '../components/ImportPanel.vue';
import type { FreshnessStatus, RequirementPriority, RequirementStatus, RequirementCategory, ViewMode, SortField } from '../types/requirement';
import { RefreshCw, Sparkles, Workflow } from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();
const store = useRequirementStore();
type TriageQueue = 'all' | 'stale' | 'missing' | 'errors' | 'aligned';
const activeQueue = ref<TriageQueue>('all');

const TEAM_SPACE_FILTER_LABELS: Record<string, string> = {
  'recent-inflow': 'Recent requirement inflow',
  'stories-decomposing': 'Stories decomposing',
  'specs-generating': 'Specs generating',
  'specs-in-review': 'Specs in review',
  'blocked-specs': 'Blocked specs',
  'approved-awaiting': 'Approved specs awaiting downstream',
};

const teamSpaceFilterLabel = computed(() => {
  const filter = route.query.filter;
  return typeof filter === 'string' ? TEAM_SPACE_FILTER_LABELS[filter] ?? null : null;
});

const attentionCount = computed(() => {
  const overview = store.controlPlaneOverview;
  return overview.stale + overview.missing + overview.errors;
});

const activeQueueLabel = computed(() => {
  switch (activeQueue.value) {
    case 'stale':
      return 'Review changed sources and stale approvals';
    case 'missing':
      return 'Restore missing sources or SDD documents';
    case 'errors':
      return 'Fix control-plane load errors';
    case 'aligned':
      return 'Aligned and ready';
    case 'all':
    default:
      return 'All active requirements';
  }
});

function queueForStatus(status?: FreshnessStatus): TriageQueue {
  if (status === 'FRESH') return 'aligned';
  if (status === 'ERROR') return 'errors';
  if (status === 'SOURCE_CHANGED' || status === 'DOCUMENT_CHANGED_AFTER_REVIEW') return 'stale';
  if (status === 'MISSING_SOURCE' || status === 'MISSING_DOCUMENT') return 'missing';
  return 'missing';
}

const visibleRequirements = computed(() => {
  if (activeQueue.value === 'all') return store.sortedRequirements;
  return store.sortedRequirements.filter(requirement => {
    const summary = store.controlPlaneSummaries[requirement.id];
    return queueForStatus(summary?.status) === activeQueue.value;
  });
});

const queueCards = computed(() => [
  {
    id: 'stale' as const,
    label: 'Review changes',
    count: store.controlPlaneOverview.stale,
    hint: 'Source or document changed after review',
  },
  {
    id: 'missing' as const,
    label: 'Fill gaps',
    count: store.controlPlaneOverview.missing,
    hint: 'Missing source links or SDD documents',
  },
  {
    id: 'errors' as const,
    label: 'Fix errors',
    count: store.controlPlaneOverview.errors,
    hint: 'Summary failed to load',
  },
  {
    id: 'aligned' as const,
    label: 'Ready',
    count: store.controlPlaneOverview.fresh,
    hint: 'Sources, docs, and reviews aligned',
  },
]);

function syncFromRouteQuery() {
  const filter = typeof route.query.filter === 'string' ? route.query.filter : null;
  switch (filter) {
    case 'recent-inflow':
      store.setFilters({ status: undefined, showCompleted: false });
      store.setSortField('recency');
      break;
    case 'stories-decomposing':
      store.setFilters({ status: 'Draft', showCompleted: false });
      break;
    case 'specs-generating':
      store.setFilters({ status: 'Draft', showCompleted: false });
      break;
    case 'specs-in-review':
      store.setFilters({ status: 'In Review', showCompleted: false });
      break;
    case 'blocked-specs':
      store.setFilters({ status: 'In Review', showCompleted: false });
      break;
    case 'approved-awaiting':
      store.setFilters({ status: 'Approved', showCompleted: false });
      break;
    default:
      break;
  }
}

onMounted(() => {
  store.loadActiveProfile();
  store.fetchRequirementList();
  syncFromRouteQuery();
});

watch(
  () => route.query.filter,
  () => {
    syncFromRouteQuery();
  },
);

function handleSelect(requirementId: string) {
  router.push({ name: 'requirement-detail', params: { requirementId } });
}

function openSkillFlow() {
  router.push({ name: 'requirement-skill-flow' });
}

function setFilterPriority(value: string) {
  store.setFilters({ priority: (value || undefined) as RequirementPriority | undefined });
}

function setFilterStatus(value: string) {
  const status = (value || undefined) as RequirementStatus | undefined;
  store.setFilters({
    status,
    showCompleted: status === 'Delivered' || status === 'Archived'
      ? true
      : status
        ? false
        : store.filters.showCompleted,
  });
}

function setFilterCategory(value: string) {
  store.setFilters({ category: (value || undefined) as RequirementCategory | undefined });
}

function setSearch(value: string) {
  store.setFilters({ search: value || undefined });
}

function switchView(mode: ViewMode) {
  store.setViewMode(mode);
}

function handleSort(field: SortField) {
  store.setSortField(field);
}

function selectQueue(queue: TriageQueue) {
  activeQueue.value = activeQueue.value === queue ? 'all' : queue;
}

function handleStatusFilter(status: RequirementStatus) {
  // Toggle: if already filtering by this status, clear it
  const current = store.filters.status;
  store.setFilters({ status: current === status ? undefined : status });
}
</script>

<template>
  <div class="list-view">
    <ImportPanel />

    <div v-if="teamSpaceFilterLabel" class="team-space-filter-banner section-high">
      <span class="text-label">Team Space Filter</span>
      <span class="text-body-sm">{{ teamSpaceFilterLabel }}</span>
    </div>

    <section class="workbench-header">
      <div class="workbench-copy">
        <span class="eyebrow">Requirement Management</span>
        <h1>Requirement Workbench</h1>
        <p>Start with blocked work, then move ready requirements through source, SDD, review, and downstream handoff.</p>
      </div>
      <div class="workbench-actions">
        <button class="primary-btn" type="button" @click="store.openImport()">
          <Sparkles :size="15" />
          <span>New Requirement</span>
        </button>
        <button class="secondary-btn" :disabled="store.githubSyncLoading" type="button" @click="store.refreshGitHubDocuments()">
          <RefreshCw :size="14" />
          <span>{{ store.githubSyncLoading ? 'Syncing' : 'Sync Sources & SDD' }}</span>
        </button>
      </div>
    </section>

    <section class="triage-panel">
      <div class="triage-intro">
        <span class="eyebrow">Action Queue</span>
        <strong>{{ attentionCount }} requirements need action</strong>
        <p>{{ activeQueueLabel }}</p>
      </div>
      <div class="queue-grid">
        <button
          v-for="queue in queueCards"
          :key="queue.id"
          class="queue-card"
          :class="[`queue-card--${queue.id}`, { 'queue-card--active': activeQueue === queue.id }]"
          type="button"
          @click="selectQueue(queue.id)"
        >
          <span>{{ queue.label }}</span>
          <strong>{{ queue.count }}</strong>
          <small>{{ queue.hint }}</small>
        </button>
      </div>
    </section>

    <div v-if="store.githubSyncError" class="sync-error">
      {{ store.githubSyncError }}
    </div>

    <ControlPlaneSummaryStrip
      :total="store.controlPlaneOverview.total"
      :fresh="store.controlPlaneOverview.fresh"
      :stale="store.controlPlaneOverview.stale"
      :missing="store.controlPlaneOverview.missing"
      :errors="store.controlPlaneOverview.errors"
      :sources="store.controlPlaneOverview.sources"
      :documents="store.controlPlaneOverview.documents"
      :artifacts="store.controlPlaneOverview.artifacts"
      :is-loading="store.controlPlaneSummaryLoading"
    />

    <StatusDistribution :distribution="store.statusDistribution" @filter="handleStatusFilter" />

    <div class="filter-bar">
      <div class="filter-group">
        <select class="filter-select" :value="store.filters.priority ?? ''" @change="setFilterPriority(($event.target as HTMLSelectElement).value)">
          <option value="">All Priorities</option>
          <option value="Critical">Critical</option>
          <option value="High">High</option>
          <option value="Medium">Medium</option>
          <option value="Low">Low</option>
        </select>
        <select class="filter-select" :value="store.filters.status ?? ''" @change="setFilterStatus(($event.target as HTMLSelectElement).value)">
          <option value="">All Statuses</option>
          <option value="Draft">Draft</option>
          <option value="In Review">In Review</option>
          <option value="Approved">Approved</option>
          <option value="In Progress">In Progress</option>
          <option value="Delivered">Delivered</option>
          <option value="Archived">Archived</option>
        </select>
        <select class="filter-select" :value="store.filters.category ?? ''" @change="setFilterCategory(($event.target as HTMLSelectElement).value)">
          <option value="">All Categories</option>
          <option value="Functional">Functional</option>
          <option value="Non-Functional">Non-Functional</option>
          <option value="Technical">Technical</option>
          <option value="Business">Business</option>
        </select>
        <input
          class="filter-search"
          type="text"
          placeholder="Search requirements..."
          @input="setSearch(($event.target as HTMLInputElement).value)"
        />
      </div>
      <div class="view-controls">
        <div class="status-toggle">
          <button
            class="tab-toggle"
            :class="{ 'tab--active': !store.filters.showCompleted }"
            @click="store.setFilters({ showCompleted: false })"
          >
            Active
          </button>
          <button
            class="tab-toggle"
            :class="{ 'tab--active': store.filters.showCompleted }"
            @click="store.setFilters({ showCompleted: true })"
          >
            Completed
          </button>
        </div>
        <div class="view-toggle">
          <button
            class="view-btn"
            :class="{ 'view-btn--active': store.viewMode === 'list' }"
            @click="switchView('list')"
          >List</button>
          <button
            class="view-btn"
            :class="{ 'view-btn--active': store.viewMode === 'graph' }"
            @click="switchView('graph')"
          >Insights</button>
        </div>
      </div>
    </div>

    <details class="advanced-tools">
      <summary>Workflow Setup</summary>
      <div class="advanced-body">
        <div class="profile-row">
          <div class="profile-row-main">
            <ProfileSelector
              :profiles="store.availableProfiles"
              :model-value="store.activeProfile.id"
              @update:model-value="store.setActiveProfile"
            />
            <span class="profile-caption">{{ store.activeProfile.description }}</span>
          </div>
          <button class="skill-flow-btn" type="button" @click="openSkillFlow">
            <Workflow :size="14" />
            <span>Skill & Doc Flow</span>
          </button>
        </div>

        <ProfileWorkflowMap
          :profile="store.activeProfile"
          compact
          :primary-action-loading="store.githubSyncLoading"
          @primary-action="store.refreshGitHubDocuments()"
        />

        <div class="advanced-view-row">
          <span>Specialist Views</span>
          <button class="view-btn" :class="{ 'view-btn--active': store.viewMode === 'kanban' }" @click="switchView('kanban')">
            Kanban
          </button>
          <button class="view-btn" :class="{ 'view-btn--active': store.viewMode === 'matrix' }" @click="switchView('matrix')">
            Matrix
          </button>
        </div>
      </div>
    </details>

    <div v-if="store.listLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>Loading requirements…</span>
    </div>

    <div v-else-if="store.listError" class="error-state">
      <span class="error-text">{{ store.listError }}</span>
      <button class="retry-btn" @click="store.fetchRequirementList()">Retry</button>
    </div>

    <div v-else-if="visibleRequirements.length === 0" class="empty-state">
      <span class="empty-icon">+</span>
      <span class="empty-text">
        {{ store.sortedRequirements.length === 0 ? 'No requirements yet. Create your first requirement to begin the SDD chain.' : 'No requirements match this action queue.' }}
      </span>
      <button class="primary-btn" type="button" @click="store.openImport()">New Requirement</button>
    </div>

    <!-- List View -->
    <RequirementListTable
      v-else-if="store.viewMode === 'list'"
      :requirements="visibleRequirements"
      :sort-field="store.sortField"
      :sort-asc="store.sortAsc"
      :control-plane-summaries="store.controlPlaneSummaries"
      :control-plane-loading="store.controlPlaneSummaryLoading"
      @select="handleSelect"
      @sort="handleSort"
    />

    <!-- Kanban View (inline) -->
    <div v-else-if="store.viewMode === 'kanban'" class="kanban-board">
      <div
        v-for="col in ['Draft', 'In Review', 'Approved', 'In Progress', 'Delivered', 'Archived']"
        :key="col"
        class="kanban-column"
      >
        <div class="kanban-header">
          <span class="kanban-title">{{ col }}</span>
          <span class="kanban-count">{{ visibleRequirements.filter(r => r.status === col).length }}</span>
        </div>
        <div class="kanban-cards">
          <div
            v-for="req in visibleRequirements.filter(r => r.status === col)"
            :key="req.id"
            class="kanban-card"
            @click="handleSelect(req.id)"
          >
            <span class="kanban-card-id">{{ req.id }}</span>
            <span class="kanban-card-title">{{ req.title }}</span>
            <div class="kanban-card-badges">
              <span class="priority-mini" :class="`pri--${req.priority.toLowerCase()}`">{{ req.priority }}</span>
              <span class="category-mini" :class="`cat--${req.category.toLowerCase()}`">{{ req.category }}</span>
            </div>
            <div class="kanban-card-meta">
              <span>{{ req.storyCount }} stories</span>
              <span>{{ req.specCount }} specs</span>
            </div>
            <div v-if="store.controlPlaneSummaries[req.id]" class="kanban-control-plane">
              {{ store.controlPlaneSummaries[req.id].status.replaceAll('_', ' ') }}
            </div>
          </div>
          <div v-if="visibleRequirements.filter(r => r.status === col).length === 0" class="kanban-empty">
            No items
          </div>
        </div>
      </div>
    </div>

    <!-- Matrix View -->
    <PriorityMatrix
      v-else-if="store.viewMode === 'matrix'"
      :requirements="visibleRequirements"
      @select="handleSelect"
    />

    <!-- Knowledge Graph View -->
    <SddKnowledgeGraph
      v-else-if="store.viewMode === 'graph'"
      :profile="store.activeProfile"
      :requirements="visibleRequirements"
      :summaries="store.controlPlaneSummaries"
      :graph="store.knowledgeGraph"
      :loading="store.controlPlaneSummaryLoading || store.knowledgeGraphLoading"
      :error="store.knowledgeGraphError"
    />

  </div>
</template>

<style scoped>
.list-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 24px 24px;
}

.team-space-filter-banner {
  border-radius: var(--radius-sm);
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.profile-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
}

.profile-row-main {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.workbench-header,
.triage-panel,
.advanced-tools {
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
}

.workbench-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 18px;
}

.workbench-copy {
  min-width: 0;
}

.eyebrow {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

h1 {
  margin: 4px 0 6px;
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 1.375rem;
  line-height: 1.2;
  text-wrap: balance;
}

p {
  margin: 0;
  max-width: 760px;
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.8125rem;
  line-height: 1.5;
}

.workbench-actions {
  display: flex;
  flex: 0 0 auto;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.primary-btn,
.secondary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 34px;
  padding: 7px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.primary-btn {
  border: 1px solid var(--color-secondary);
  background: var(--color-secondary);
  color: var(--color-on-secondary-container);
}

.secondary-btn {
  border: 1px solid rgba(137, 206, 255, 0.45);
  background: var(--color-surface-container);
  color: var(--color-secondary);
}

.secondary-btn:disabled {
  cursor: progress;
  opacity: 0.65;
}

.primary-btn:hover,
.secondary-btn:hover {
  box-shadow: var(--shadow-card-hover);
}

.triage-panel {
  display: grid;
  grid-template-columns: minmax(220px, 0.9fr) minmax(0, 2.1fr);
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-color: rgba(245, 158, 11, 0.24);
  background: rgba(245, 158, 11, 0.08);
}

.triage-intro {
  min-width: 0;
}

.triage-intro strong {
  display: block;
  margin: 3px 0;
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.9375rem;
}

.queue-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.queue-card {
  min-width: 0;
  min-height: 82px;
  padding: 10px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  color: var(--color-on-surface);
  cursor: pointer;
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 4px;
  transition: background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.queue-card span {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.queue-card strong {
  color: var(--color-on-surface);
  font-family: var(--font-tech);
  font-size: 1.25rem;
  line-height: 1;
}

.queue-card small {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  line-height: 1.3;
  overflow-wrap: anywhere;
}

.queue-card:hover,
.queue-card--active {
  box-shadow: var(--shadow-card-hover);
}

.queue-card--active {
  border-color: var(--color-secondary);
  background: var(--color-secondary-tint);
}

.queue-card--stale { border-color: rgba(245, 158, 11, 0.35); }
.queue-card--missing { border-color: rgba(239, 83, 80, 0.35); }
.queue-card--errors { border-color: rgba(244, 67, 54, 0.42); }
.queue-card--aligned { border-color: rgba(129, 199, 132, 0.35); }

.profile-caption {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface-variant);
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-group { display: flex; gap: 8px; flex-wrap: wrap; }

.sync-error {
  padding: 9px 12px;
  border: 1px solid rgba(239, 83, 80, 0.35);
  border-radius: var(--radius-sm);
  background: rgba(239, 83, 80, 0.08);
  color: var(--color-error);
  font-family: var(--font-ui);
  font-size: 0.75rem;
}

.skill-flow-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 34px;
  padding: 7px 12px;
  border: 1px solid rgba(137, 206, 255, 0.45);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  color: var(--color-secondary);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.skill-flow-btn:hover {
  background: var(--color-secondary-tint);
}

.filter-select, .filter-search {
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  min-height: 34px;
  padding: 7px 12px;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface);
  cursor: pointer;
}

.filter-search {
  min-width: 220px;
  cursor: text;
}

.filter-search::placeholder { color: var(--color-on-surface-variant); opacity: 0.6; }

.view-controls { display: flex; gap: 8px; align-items: center; }

.status-toggle {
  display: inline-flex;
  gap: 6px;
}

.tab-toggle {
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  min-height: 34px;
  padding: 7px 12px;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-on-surface-variant);
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}

.tab--active { color: var(--color-secondary); border-color: var(--color-secondary); }

.view-toggle {
  display: flex;
  background: var(--color-surface-container-low);
  border-radius: var(--radius-sm);
  overflow: hidden;
  border: var(--border-ghost);
}

.view-btn {
  background: none;
  border: none;
  min-height: 34px;
  padding: 7px 12px;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-on-surface-variant);
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.view-btn--active {
  background: var(--color-surface-container-high);
  color: var(--color-secondary);
}

.advanced-tools {
  padding: 0;
  background: var(--color-surface-container-low);
}

.advanced-tools summary {
  cursor: pointer;
  padding: 10px 12px;
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.advanced-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 12px 12px;
}

.advanced-view-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.advanced-view-row > span {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

@media (max-width: 1180px) {
  .triage-panel {
    grid-template-columns: 1fr;
  }

  .queue-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .list-view {
    padding: 0 12px 18px;
  }

  .workbench-header {
    flex-direction: column;
  }

  .workbench-actions,
  .view-controls,
  .filter-group,
  .filter-search {
    width: 100%;
  }

  .primary-btn,
  .secondary-btn,
  .filter-select,
  .filter-search {
    flex: 1 1 100%;
  }

  .queue-grid {
    grid-template-columns: 1fr;
  }
}

/* Kanban Board */
.kanban-board {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  overflow-x: auto;
  min-height: 400px;
}

.kanban-column {
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 180px;
}

.kanban-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-separator);
}

.kanban-title {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}

.kanban-count {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-secondary);
  background: var(--color-secondary-tint);
  padding: 1px 6px;
  border-radius: 2px;
}

.kanban-cards {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.kanban-card {
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  cursor: pointer;
  transition: background 0.15s ease, box-shadow 0.15s ease;
}

.kanban-card:hover {
  background: var(--nav-hover-bg);
  box-shadow: var(--shadow-card-hover);
}

.kanban-card-id {
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  color: var(--color-secondary);
}

.kanban-card-title {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
  line-height: 1.3;
}

.kanban-card-badges {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.priority-mini, .category-mini {
  font-family: var(--font-ui);
  font-size: 0.5rem;
  padding: 1px 4px;
  border-radius: 2px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.pri--critical { color: #fff; background: var(--color-incident-crimson); }
.pri--high { color: var(--color-approval-amber); background: rgba(245, 158, 11, 0.12); }
.pri--medium { color: var(--color-secondary); background: var(--color-secondary-tint); }
.pri--low { color: var(--color-on-surface-variant); background: rgba(148, 163, 184, 0.1); }

.cat--functional { color: var(--color-secondary); background: var(--color-secondary-tint); }
.cat--non-functional { color: var(--color-approval-amber); background: rgba(245, 158, 11, 0.1); }
.cat--technical { color: #a78bfa; background: rgba(167, 139, 250, 0.1); }
.cat--business { color: var(--color-health-emerald); background: rgba(78, 222, 163, 0.1); }

.kanban-card-meta {
  display: flex;
  gap: 8px;
  font-family: var(--font-ui);
  font-size: 0.5rem;
  color: var(--color-on-surface-variant);
}

.kanban-control-plane {
  align-self: flex-start;
  padding: 2px 6px;
  border-radius: 2px;
  background: rgba(148, 163, 184, 0.12);
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.5rem;
  line-height: 1.2;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.kanban-empty {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
  opacity: 0.5;
  text-align: center;
  padding: 20px 0;
}

/* States */
.loading-state, .error-state, .empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 12px; padding: 60px 0; color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
}

.loading-spinner {
  width: 32px; height: 32px;
  border: 2px solid var(--color-surface-container-highest);
  border-top-color: var(--color-secondary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.error-text { color: var(--color-incident-crimson); font-size: 0.75rem; }

.retry-btn {
  background: var(--color-surface-container-high); border: 1px solid var(--color-secondary);
  color: var(--color-secondary); padding: 6px 16px; border-radius: var(--radius-sm);
  cursor: pointer; font-family: var(--font-ui); font-size: 0.6875rem;
  text-transform: uppercase; letter-spacing: 0.04em;
}

.empty-icon {
  font-size: 2rem; color: var(--color-secondary); opacity: 0.5;
}

.empty-text { font-size: 0.875rem; }

@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 900px) {
  .workbench-header,
  .attention-panel {
    align-items: stretch;
    flex-direction: column;
  }

  .workbench-actions {
    justify-content: flex-start;
  }
}
</style>
