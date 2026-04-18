<script setup lang="ts">
import { Activity, RefreshCw } from 'lucide-vue-next';
import RunFilters from './RunFilters.vue';
import RunRow from './RunRow.vue';
import type { Page, Run, RunQuery, Skill } from '../types';

interface Props {
  runs: Page<Run> | null;
  loading: boolean;
  error: string | null;
  filters: RunQuery;
  skills: Skill[];
  selectedRunId?: string | null;
}

withDefaults(defineProps<Props>(), {
  selectedRunId: null,
});

defineEmits<{
  retry: [];
  refresh: [];
  loadMore: [];
  select: [executionId: string];
  changeFilters: [query: RunQuery];
}>();
</script>

<template>
  <section class="runs-card section-high">
    <header class="runs-card__header">
      <div>
        <p class="text-label">History</p>
        <h3>Run History</h3>
      </div>
      <button class="runs-card__refresh" type="button" aria-label="Refresh run history" @click="$emit('refresh')">
        <RefreshCw :size="16" />
      </button>
    </header>

    <RunFilters :filters="filters" :skills="skills" @change="$emit('changeFilters', $event)" />

    <div v-if="loading" class="runs-card__skeleton" data-state="loading">
      <div v-for="index in 4" :key="index" class="runs-card__skeleton-row"></div>
    </div>

    <div v-else-if="error" class="runs-card__error" data-state="error">
      <p class="text-label">Run history unavailable</p>
      <p class="text-body-sm">{{ error }}</p>
      <button class="btn-machined" type="button" @click="$emit('retry')">Retry</button>
    </div>

    <div v-else-if="!runs?.items.length" class="runs-card__empty" data-state="empty">
      <p class="text-label">No skill executions recorded</p>
      <p class="text-body-sm">No skill executions were recorded in the selected time range.</p>
      <button class="btn-machined" type="button" @click="$emit('changeFilters', {})">Reset filters</button>
    </div>

    <div v-else class="runs-card__table-wrap" data-state="normal">
      <div class="runs-card__meta">
        <span class="text-body-sm">{{ runs.total }} runs · page {{ runs.page }}</span>
        <Activity :size="16" />
      </div>
      <table class="runs-card__table">
        <thead>
          <tr>
            <th scope="col">Run</th>
            <th scope="col">Started</th>
            <th scope="col">Status</th>
            <th scope="col">Triggered By</th>
            <th scope="col">Source</th>
            <th scope="col">Duration</th>
          </tr>
        </thead>
        <tbody>
          <RunRow
            v-for="run in runs.items"
            :key="run.id"
            :run="run"
            :selected="run.id === selectedRunId"
            @select="$emit('select', $event)"
          />
        </tbody>
      </table>

      <button v-if="runs.hasMore" class="btn-machined" type="button" @click="$emit('loadMore')">Load more</button>
    </div>
  </section>
</template>

<style scoped>
.runs-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
}

.runs-card__header,
.runs-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.runs-card__refresh {
  border: var(--border-ghost);
  background: rgba(255, 255, 255, 0.03);
  color: var(--color-on-surface);
  width: 36px;
  height: 36px;
  border-radius: 999px;
  cursor: pointer;
}

.runs-card__table-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-x: auto;
}

.runs-card__table {
  width: 100%;
  border-collapse: collapse;
}

.runs-card__table th {
  padding-bottom: 10px;
  text-align: left;
  color: var(--color-on-surface-variant);
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.runs-card__error,
.runs-card__empty {
  padding: 16px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.runs-card__error {
  color: var(--color-incident-crimson);
}

.runs-card__skeleton {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.runs-card__skeleton-row {
  height: 48px;
  border-radius: var(--radius-sm);
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.03), rgba(137, 206, 255, 0.08), rgba(255, 255, 255, 0.03));
  background-size: 220% 100%;
  animation: runs-pulse 1.2s ease-in-out infinite;
}

@keyframes runs-pulse {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}
</style>
