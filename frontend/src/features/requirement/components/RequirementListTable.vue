<script setup lang="ts">
import type { RequirementControlPlaneListSummary, RequirementListItem, SortField } from '../types/requirement';
import PriorityBadge from './PriorityBadge.vue';
import RequirementStatusBadge from './RequirementStatusBadge.vue';
import CategoryBadge from './CategoryBadge.vue';
import FreshnessChip from './FreshnessChip.vue';

interface Props {
  requirements: ReadonlyArray<RequirementListItem>;
  sortField?: SortField;
  sortAsc?: boolean;
  controlPlaneSummaries?: Record<string, RequirementControlPlaneListSummary>;
  controlPlaneLoading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  sortField: 'priority',
  sortAsc: true,
  controlPlaneSummaries: () => ({}),
  controlPlaneLoading: false,
});

const emit = defineEmits<{
  select: [id: string];
  sort: [field: SortField];
}>();

function formatDate(iso: string): string {
  const d = new Date(iso);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function sortIndicator(field: SortField): string {
  if (props.sortField !== field) return '';
  return props.sortAsc ? ' \u25B2' : ' \u25BC';
}

function summaryFor(requirementId: string): RequirementControlPlaneListSummary | undefined {
  return props.controlPlaneSummaries[requirementId];
}

function currentStage(summary?: RequirementControlPlaneListSummary): string {
  switch (summary?.status) {
    case 'FRESH':
      return 'Ready for handoff';
    case 'DOCUMENT_CHANGED_AFTER_REVIEW':
      return 'Review stale';
    case 'SOURCE_CHANGED':
      return 'Source changed';
    case 'MISSING_SOURCE':
      return 'Source intake';
    case 'MISSING_DOCUMENT':
      return 'SDD generation';
    case 'ERROR':
      return 'Control-plane error';
    case 'UNKNOWN':
      return 'Needs triage';
    default:
      return props.controlPlaneLoading ? 'Loading stage' : 'Not indexed';
  }
}

function nextAction(summary?: RequirementControlPlaneListSummary): string {
  switch (summary?.status) {
    case 'FRESH':
      return 'Continue downstream';
    case 'DOCUMENT_CHANGED_AFTER_REVIEW':
      return 'Re-run business review';
    case 'SOURCE_CHANGED':
      return 'Refresh affected SDD docs';
    case 'MISSING_SOURCE':
      return 'Link authoritative source';
    case 'MISSING_DOCUMENT':
      return summary.missingDocumentCount > 1
        ? `Generate ${summary.missingDocumentCount} missing docs`
        : 'Generate missing doc';
    case 'ERROR':
      return 'Retry summary load';
    case 'UNKNOWN':
      return 'Inspect traceability';
    default:
      return props.controlPlaneLoading ? 'Reading control plane' : 'Open requirement';
  }
}
</script>

<template>
  <div class="requirement-table">
    <div class="table-header">
      <span class="col-id">ID</span>
      <span class="col-title col-sortable" @click="emit('sort', 'title')">Title{{ sortIndicator('title') }}</span>
      <span class="col-priority col-sortable" @click="emit('sort', 'priority')">Priority{{ sortIndicator('priority') }}</span>
      <span class="col-status col-sortable" @click="emit('sort', 'status')">Status{{ sortIndicator('status') }}</span>
      <span class="col-category">Category</span>
      <span class="col-stories">Stories</span>
      <span class="col-specs">Specs</span>
      <span class="col-current-stage">Current Stage</span>
      <span class="col-completeness">Completeness</span>
      <span class="col-updated col-sortable" @click="emit('sort', 'recency')">Updated{{ sortIndicator('recency') }}</span>
    </div>
    <div
      v-for="req in requirements"
      :key="req.id"
      class="table-row"
      @click="emit('select', req.id)"
    >
      <span class="col-id cell-tech">{{ req.id }}</span>
      <span class="col-title cell-body">{{ req.title }}</span>
      <span class="col-priority"><PriorityBadge :priority="req.priority" /></span>
      <span class="col-status"><RequirementStatusBadge :status="req.status" /></span>
      <span class="col-category"><CategoryBadge :category="req.category" /></span>
      <span class="col-stories cell-tech">{{ req.storyCount }}</span>
      <span class="col-specs cell-tech">{{ req.specCount }}</span>
      <span class="col-current-stage">
        <span class="stage-cell">
          <span class="stage-main">
            <FreshnessChip v-if="summaryFor(req.id)" :status="summaryFor(req.id)!.status" />
            <span v-else class="control-loading">{{ controlPlaneLoading ? 'Loading' : 'Pending' }}</span>
            <strong>{{ currentStage(summaryFor(req.id)) }}</strong>
          </span>
          <span class="control-meta">{{ nextAction(summaryFor(req.id)) }}</span>
        </span>
      </span>
      <span class="col-completeness">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: `${req.completeness}%` }"></div>
        </div>
        <span class="progress-text cell-tech">{{ req.completeness }}%</span>
      </span>
      <span class="col-updated cell-muted">{{ formatDate(req.updatedAt) }}</span>
    </div>
  </div>
</template>

<style scoped>
.requirement-table {
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  overflow-x: auto;
  overflow-y: hidden;
}

.table-header, .table-row {
  display: grid;
  grid-template-columns: 80px minmax(220px, 1fr) 70px 100px 100px 50px 50px minmax(210px, 0.9fr) 110px 100px;
  gap: 8px;
  align-items: center;
  padding: 12px 16px;
  min-width: 1120px;
}

.table-header {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
  background: var(--color-surface-container-low);
}

.col-sortable {
  cursor: pointer;
  user-select: none;
  transition: color 0.15s ease;
}

.col-sortable:hover { color: var(--color-secondary); }

.table-row {
  cursor: pointer;
  transition: background 0.15s ease;
  border-bottom: 1px solid var(--border-separator);
}

.table-row:hover { background: var(--nav-hover-bg); }
.table-row:last-child { border-bottom: none; }

.cell-tech {
  font-family: var(--font-tech);
  font-size: 0.75rem;
  color: var(--color-secondary);
}

.cell-body {
  font-family: var(--font-ui);
  font-size: 0.8125rem;
  color: var(--color-on-surface);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cell-muted {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
}

.col-completeness {
  display: flex;
  align-items: center;
  gap: 6px;
}

.progress-bar {
  flex: 1;
  height: 4px;
  background: var(--color-surface-container-low);
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--color-secondary);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 0.6875rem;
  min-width: 28px;
  text-align: right;
}

.col-stories, .col-specs {
  text-align: center;
}

.col-current-stage {
  min-width: 0;
}

.stage-cell {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
  align-items: flex-start;
}

.stage-main {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  max-width: 100%;
}

.stage-main strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 700;
}

.control-meta, .control-loading {
  overflow: hidden;
  max-width: 100%;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: var(--font-tech);
  font-size: 0.6875rem;
  color: var(--color-on-surface-variant);
}
</style>
