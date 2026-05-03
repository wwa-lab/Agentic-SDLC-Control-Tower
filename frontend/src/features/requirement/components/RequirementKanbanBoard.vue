<script setup lang="ts">
import type { RequirementListItem, RequirementControlPlaneListSummary, RequirementStatus } from '../types/requirement';

const COLUMNS: RequirementStatus[] = ['Draft', 'In Review', 'Approved', 'In Progress', 'Delivered', 'Archived'];

defineProps<{
  requirements: ReadonlyArray<RequirementListItem>;
  controlPlaneSummaries: Record<string, RequirementControlPlaneListSummary>;
}>();

const emit = defineEmits<{
  select: [id: string];
}>();
</script>

<template>
  <div class="kanban-board">
    <div v-for="col in COLUMNS" :key="col" class="kanban-column">
      <div class="kanban-header">
        <span class="kanban-title">{{ col }}</span>
        <span class="kanban-count">{{ requirements.filter(r => r.status === col).length }}</span>
      </div>
      <div class="kanban-cards">
        <div
          v-for="req in requirements.filter(r => r.status === col)"
          :key="req.id"
          class="kanban-card"
          @click="emit('select', req.id)"
        >
          <span class="kanban-card-id">{{ req.id }}</span>
          <span class="kanban-card-title">{{ req.title }}</span>
          <div class="kanban-card-badges">
            <span class="priority-mini" :class="`pri--${req.priority.toLowerCase()}`">{{ req.priority }}</span>
            <span class="category-mini" :class="`cat--${req.category.toLowerCase().replace(/[^a-z]/g, '-')}`">{{ req.category }}</span>
          </div>
          <div class="kanban-card-meta">
            <span>{{ req.storyCount }} stories</span>
            <span>{{ req.specCount }} specs</span>
          </div>
          <div v-if="controlPlaneSummaries[req.id]" class="kanban-control-plane">
            {{ controlPlaneSummaries[req.id].status.replaceAll('_', ' ') }}
          </div>
        </div>
        <div v-if="requirements.filter(r => r.status === col).length === 0" class="kanban-empty">
          No items
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
.pri--high     { color: var(--color-approval-amber); background: rgba(245, 158, 11, 0.12); }
.pri--medium   { color: var(--color-secondary); background: var(--color-secondary-tint); }
.pri--low      { color: var(--color-on-surface-variant); background: rgba(148, 163, 184, 0.1); }

.cat--functional     { color: var(--color-secondary); background: var(--color-secondary-tint); }
.cat--non-functional { color: var(--color-approval-amber); background: rgba(245, 158, 11, 0.1); }
.cat--technical      { color: #a78bfa; background: rgba(167, 139, 250, 0.1); }
.cat--business       { color: var(--color-health-emerald); background: rgba(78, 222, 163, 0.1); }

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
</style>
