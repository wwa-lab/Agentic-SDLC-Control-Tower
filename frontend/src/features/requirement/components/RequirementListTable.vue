<script setup lang="ts">
import type { RequirementListItem, SortField } from '../types/requirement';
import PriorityBadge from './PriorityBadge.vue';
import RequirementStatusBadge from './RequirementStatusBadge.vue';
import CategoryBadge from './CategoryBadge.vue';

interface Props {
  requirements: ReadonlyArray<RequirementListItem>;
  sortField?: SortField;
  sortAsc?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  sortField: 'priority',
  sortAsc: true,
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
  overflow: hidden;
}

.table-header, .table-row {
  display: grid;
  grid-template-columns: 80px 1fr 70px 100px 100px 50px 50px 110px 100px;
  gap: 8px;
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
  font-size: 0.6875rem;
  color: var(--color-secondary);
}

.cell-body {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cell-muted {
  font-family: var(--font-ui);
  font-size: 0.625rem;
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
  font-size: 0.5625rem;
  min-width: 28px;
  text-align: right;
}

.col-stories, .col-specs {
  text-align: center;
}
</style>
