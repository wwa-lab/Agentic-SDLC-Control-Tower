<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useIncidentStore } from '../stores/incidentStore';
import SeverityDistribution from '../components/SeverityDistribution.vue';
import SeverityBadge from '../components/SeverityBadge.vue';
import StatusBadge from '../components/StatusBadge.vue';
import HandlerTypeBadge from '../components/HandlerTypeBadge.vue';
import type { Priority, IncidentStatus, HandlerType, SortField } from '../types/incident';

const router = useRouter();
const store = useIncidentStore();

onMounted(() => {
  store.fetchIncidentList();
});

function handleRowClick(incidentId: string) {
  router.push({ name: 'incident-detail', params: { incidentId } });
}

function toggleResolved() {
  store.setFilters({ showResolved: !store.filters.showResolved });
}

function setFilterPriority(p: Priority | undefined) {
  store.setFilters({ priority: p });
}

function setFilterHandler(h: HandlerType | undefined) {
  store.setFilters({ handlerType: h });
}

function formatDuration(iso: string): string {
  const match = iso.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
  if (!match) return iso;
  const h = match[1] || '0';
  const m = match[2] || '0';
  return `${h}h ${m}m`;
}
</script>

<template>
  <div class="list-view">
    <!-- Severity Strip -->
    <SeverityDistribution :distribution="store.severityDistribution" />

    <!-- Filters -->
    <div class="filter-bar">
      <div class="filter-group">
        <select class="filter-select" @change="setFilterPriority(($event.target as HTMLSelectElement).value as Priority || undefined)">
          <option value="">All Priorities</option>
          <option value="P1">P1</option>
          <option value="P2">P2</option>
          <option value="P3">P3</option>
          <option value="P4">P4</option>
        </select>
        <select class="filter-select" @change="setFilterHandler(($event.target as HTMLSelectElement).value as HandlerType || undefined)">
          <option value="">All Handlers</option>
          <option value="AI">AI</option>
          <option value="Human">Human</option>
          <option value="Hybrid">Hybrid</option>
        </select>
      </div>
      <button class="tab-toggle" :class="{ 'tab--active': store.filters.showResolved }" @click="toggleResolved">
        {{ store.filters.showResolved ? 'Show Active Only' : 'Include Resolved' }}
      </button>
    </div>

    <!-- Loading -->
    <div v-if="store.listLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>Loading incidents...</span>
    </div>

    <!-- Error -->
    <div v-else-if="store.listError" class="error-state">
      <span class="error-text">{{ store.listError }}</span>
      <button class="retry-btn" @click="store.fetchIncidentList()">Retry</button>
    </div>

    <!-- Empty -->
    <div v-else-if="store.filteredIncidents.length === 0" class="empty-state">
      <span class="empty-icon">✓</span>
      <span class="empty-text">All clear — no active incidents in this workspace</span>
    </div>

    <!-- Table -->
    <div v-else class="incident-table">
      <div class="table-header">
        <span class="col-id">ID</span>
        <span class="col-title">Title</span>
        <span class="col-priority">Priority</span>
        <span class="col-status">Status</span>
        <span class="col-handler">Handler</span>
        <span class="col-duration">Duration</span>
      </div>
      <div
        v-for="inc in store.filteredIncidents"
        :key="inc.id"
        class="table-row"
        @click="handleRowClick(inc.id)"
      >
        <span class="col-id cell-tech">{{ inc.id }}</span>
        <span class="col-title cell-body">{{ inc.title }}</span>
        <span class="col-priority"><SeverityBadge :priority="inc.priority" /></span>
        <span class="col-status"><StatusBadge :status="inc.status" /></span>
        <span class="col-handler"><HandlerTypeBadge :handler-type="inc.handlerType" /></span>
        <span class="col-duration cell-tech">{{ formatDuration(inc.duration) }}</span>
      </div>
    </div>
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
}

.filter-group { display: flex; gap: 8px; }

.filter-select {
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 6px 12px;
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
  cursor: pointer;
}

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

.tab--active {
  color: var(--color-secondary);
  border-color: var(--color-secondary);
}

.incident-table {
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.table-header, .table-row {
  display: grid;
  grid-template-columns: 80px 1fr 60px 140px 70px 70px;
  gap: 12px;
  align-items: center;
  padding: 10px 16px;
}

.table-header {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
  background: var(--color-surface-container-low);
}

.table-row {
  cursor: pointer;
  transition: background 0.15s ease;
  border-bottom: 1px solid var(--border-separator);
}

.table-row:hover { background: var(--nav-hover-bg); }
.table-row:last-child { border-bottom: none; }

.cell-tech { font-family: var(--font-tech); font-size: 0.6875rem; color: var(--color-secondary); }
.cell-body { font-family: var(--font-ui); font-size: 0.75rem; color: var(--color-on-surface); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

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
  font-size: 2rem; color: var(--color-health-emerald);
}

.empty-text { font-size: 0.875rem; }

@keyframes spin { to { transform: rotate(360deg); } }
</style>
