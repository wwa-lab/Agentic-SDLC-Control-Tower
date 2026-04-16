<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useRequirementStore } from '../stores/requirementStore';
import StatusDistribution from '../components/StatusDistribution.vue';
import RequirementListTable from '../components/RequirementListTable.vue';
import PriorityMatrix from '../components/PriorityMatrix.vue';
import ImportPanel from '../components/ImportPanel.vue';
import type { RequirementPriority, RequirementStatus, RequirementCategory, ViewMode, SortField } from '../types/requirement';

const router = useRouter();
const store = useRequirementStore();

onMounted(() => {
  store.fetchRequirementList();
});

function handleSelect(requirementId: string) {
  router.push({ name: 'requirement-detail', params: { requirementId } });
}

function setFilterPriority(value: string) {
  store.setFilters({ priority: (value || undefined) as RequirementPriority | undefined });
}

function setFilterStatus(value: string) {
  store.setFilters({ status: (value || undefined) as RequirementStatus | undefined });
}

function setFilterCategory(value: string) {
  store.setFilters({ category: (value || undefined) as RequirementCategory | undefined });
}

function setSearch(value: string) {
  store.setFilters({ search: value || undefined });
}

function toggleCompleted() {
  store.setFilters({ showCompleted: !store.filters.showCompleted });
}

function switchView(mode: ViewMode) {
  store.setViewMode(mode);
}

function handleSort(field: SortField) {
  store.setSortField(field);
}

function handleStatusFilter(status: RequirementStatus) {
  // Toggle: if already filtering by this status, clear it
  const current = store.filters.status;
  store.setFilters({ status: current === status ? undefined : status });
}
</script>

<template>
  <div class="list-view">
    <!-- Status Distribution Strip -->
    <StatusDistribution :distribution="store.statusDistribution" @filter="handleStatusFilter" />

    <!-- Filter Bar -->
    <div class="filter-bar">
      <div class="filter-group">
        <button class="import-btn" @click="store.openImport()">+ Import</button>
        <select class="filter-select" @change="setFilterPriority(($event.target as HTMLSelectElement).value)">
          <option value="">All Priorities</option>
          <option value="Critical">Critical</option>
          <option value="High">High</option>
          <option value="Medium">Medium</option>
          <option value="Low">Low</option>
        </select>
        <select class="filter-select" @change="setFilterStatus(($event.target as HTMLSelectElement).value)">
          <option value="">All Statuses</option>
          <option value="Draft">Draft</option>
          <option value="In Review">In Review</option>
          <option value="Approved">Approved</option>
          <option value="In Progress">In Progress</option>
          <option value="Delivered">Delivered</option>
          <option value="Archived">Archived</option>
        </select>
        <select class="filter-select" @change="setFilterCategory(($event.target as HTMLSelectElement).value)">
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
        <button
          class="tab-toggle"
          :class="{ 'tab--active': store.filters.showCompleted }"
          @click="toggleCompleted"
        >
          {{ store.filters.showCompleted ? 'Hide Completed' : 'Show Completed' }}
        </button>
        <div class="view-toggle">
          <button
            class="view-btn"
            :class="{ 'view-btn--active': store.viewMode === 'list' }"
            @click="switchView('list')"
          >List</button>
          <button
            class="view-btn"
            :class="{ 'view-btn--active': store.viewMode === 'kanban' }"
            @click="switchView('kanban')"
          >Kanban</button>
          <button
            class="view-btn"
            :class="{ 'view-btn--active': store.viewMode === 'matrix' }"
            @click="switchView('matrix')"
          >Matrix</button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="store.listLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>Loading requirements...</span>
    </div>

    <!-- Error -->
    <div v-else-if="store.listError" class="error-state">
      <span class="error-text">{{ store.listError }}</span>
      <button class="retry-btn" @click="store.fetchRequirementList()">Retry</button>
    </div>

    <!-- Empty -->
    <div v-else-if="store.sortedRequirements.length === 0" class="empty-state">
      <span class="empty-icon">+</span>
      <span class="empty-text">No requirements yet — create your first requirement to begin the SDD chain</span>
    </div>

    <!-- List View -->
    <RequirementListTable
      v-else-if="store.viewMode === 'list'"
      :requirements="store.sortedRequirements"
      :sort-field="store.sortField"
      :sort-asc="store.sortAsc"
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
          <span class="kanban-count">{{ store.sortedRequirements.filter(r => r.status === col).length }}</span>
        </div>
        <div class="kanban-cards">
          <div
            v-for="req in store.sortedRequirements.filter(r => r.status === col)"
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
          </div>
          <div v-if="store.sortedRequirements.filter(r => r.status === col).length === 0" class="kanban-empty">
            No items
          </div>
        </div>
      </div>
    </div>

    <!-- Matrix View -->
    <PriorityMatrix
      v-else-if="store.viewMode === 'matrix'"
      :requirements="store.sortedRequirements"
      @select="handleSelect"
    />

    <!-- Import Panel -->
    <ImportPanel />
  </div>
</template>

<style scoped>
.list-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 24px 24px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-group { display: flex; gap: 8px; flex-wrap: wrap; }

.import-btn {
  background: linear-gradient(135deg, var(--color-secondary), #a78bfa);
  border: none;
  color: #0b1326;
  padding: 6px 14px;
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: opacity 0.2s ease;
}

.import-btn:hover { opacity: 0.85; }

.filter-select, .filter-search {
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 6px 12px;
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
  cursor: pointer;
}

.filter-search {
  min-width: 180px;
  cursor: text;
}

.filter-search::placeholder { color: var(--color-on-surface-variant); opacity: 0.6; }

.view-controls { display: flex; gap: 8px; align-items: center; }

.tab-toggle {
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 6px 12px;
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-on-surface-variant);
  cursor: pointer;
  transition: all 0.2s ease;
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
  padding: 6px 12px;
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-on-surface-variant);
  cursor: pointer;
  transition: all 0.2s ease;
}

.view-btn--active {
  background: var(--color-surface-container-high);
  color: var(--color-secondary);
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
</style>
