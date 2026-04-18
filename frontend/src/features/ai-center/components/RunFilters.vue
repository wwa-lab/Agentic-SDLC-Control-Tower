<script setup lang="ts">
import { Search } from 'lucide-vue-next';
import { EXECUTION_STATUS_LABELS } from '../constants';
import { useRunFilters } from '../composables/useRunFilters';
import type { RunQuery, Skill, ExecutionStatus, TriggeredByType } from '../types';

interface Props {
  filters: RunQuery;
  skills: Skill[];
}

const props = defineProps<Props>();

const emit = defineEmits<{
  change: [query: RunQuery];
}>();

const { form, hasActiveFilters, applyFilters, resetFilters } = useRunFilters(props.filters, query => emit('change', query));

const statuses: ExecutionStatus[] = ['running', 'succeeded', 'failed', 'pending_approval', 'rejected', 'rolled_back'];
const triggerTypes: Array<TriggeredByType> = ['ai', 'human', 'system'];

function toggleListItem(key: 'skillKey' | 'status', value: string) {
  const bucket = form[key];
  const index = bucket.indexOf(value as never);
  if (index >= 0) {
    bucket.splice(index, 1);
  } else {
    bucket.push(value as never);
  }
}
</script>

<template>
  <div class="run-filters">
    <label class="run-filters__input">
      <Search :size="14" />
      <input v-model="form.triggerSourcePage" type="search" placeholder="Trigger source page (exact match)" />
    </label>

    <div class="run-filters__date-row">
      <input v-model="form.startedAfter" class="run-filters__date" type="datetime-local" />
      <input v-model="form.startedBefore" class="run-filters__date" type="datetime-local" />
      <select v-model="form.triggeredByType" class="run-filters__date">
        <option value="">Any Triggered By</option>
        <option v-for="type in triggerTypes" :key="type" :value="type">{{ type }}</option>
      </select>
    </div>

    <div class="run-filters__group">
      <span class="text-label">Skill</span>
      <button
        v-for="skill in skills"
        :key="skill.key"
        class="run-chip"
        type="button"
        :aria-pressed="form.skillKey.includes(skill.key)"
        @click="toggleListItem('skillKey', skill.key)"
      >
        {{ skill.name }}
      </button>
    </div>

    <div class="run-filters__group">
      <span class="text-label">Status</span>
      <button
        v-for="status in statuses"
        :key="status"
        class="run-chip"
        type="button"
        role="status"
        :aria-label="EXECUTION_STATUS_LABELS[status]"
        :aria-pressed="form.status.includes(status)"
        @click="toggleListItem('status', status)"
      >
        {{ EXECUTION_STATUS_LABELS[status] }}
      </button>
    </div>

    <div class="run-filters__actions">
      <button class="btn-machined" type="button" @click="applyFilters">Apply</button>
      <button v-if="hasActiveFilters" class="run-filters__link" type="button" @click="resetFilters">Reset</button>
    </div>
  </div>
</template>

<style scoped>
.run-filters {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.run-filters__input,
.run-filters__date {
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.03);
  color: var(--color-on-surface);
}

.run-filters__input {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
}

.run-filters__input input,
.run-filters__date {
  width: 100%;
  padding: 10px 12px;
  border: none;
  background: transparent;
}

.run-filters__date-row,
.run-filters__group,
.run-filters__actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.run-chip {
  border: var(--border-ghost);
  border-radius: 999px;
  padding: 6px 10px;
  background: rgba(255, 255, 255, 0.02);
  color: var(--color-on-surface);
  cursor: pointer;
}

.run-chip[aria-pressed="true"] {
  background: rgba(137, 206, 255, 0.16);
  box-shadow: inset 0 0 0 1px rgba(137, 206, 255, 0.32);
}

.run-filters__link {
  border: none;
  background: transparent;
  color: var(--color-secondary);
  cursor: pointer;
}
</style>
