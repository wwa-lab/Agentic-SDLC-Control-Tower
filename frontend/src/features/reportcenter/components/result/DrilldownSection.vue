<script setup lang="ts">
import type { DrilldownResult, SectionResult } from '../../types';
import { formatDrilldownValue } from '../../utils';
import SectionError from './SectionError.vue';
import SectionSkeleton from './SectionSkeleton.vue';

defineProps<{
  section: SectionResult<DrilldownResult>;
  loading: boolean;
}>();

defineEmits<{
  retry: [];
}>();
</script>

<template>
  <SectionSkeleton v-if="loading" :height="280" />
  <SectionError v-else-if="section.error" :message="section.error" @retry="$emit('retry')" />
  <section v-else-if="section.data" class="drilldown-shell">
    <div class="drilldown-shell__header">
      <div>
        <p class="text-label">Drilldown</p>
        <h3>Source rows</h3>
      </div>
      <span class="text-body-sm">{{ section.data.rows.length }} of {{ section.data.totalRows }}</span>
    </div>

    <div class="drilldown-table">
      <table>
        <thead>
          <tr>
            <th v-for="column in section.data.columns" :key="column.key">{{ column.label }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in section.data.rows" :key="index">
            <td v-for="column in section.data.columns" :key="column.key">
              {{ formatDrilldownValue(column, row[column.key] ?? null) }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.drilldown-shell {
  display: grid;
  gap: 14px;
  border: var(--border-ghost);
  border-radius: 18px;
  background: linear-gradient(180deg, color-mix(in srgb, var(--surface-report) 92%, transparent), var(--color-surface-container-low));
  padding: 18px;
}

.drilldown-shell__header {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: end;
}

.drilldown-table {
  overflow: auto;
  border: var(--border-ghost);
  border-radius: 14px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 10px 12px;
  text-align: left;
  font-size: 0.8125rem;
  border-bottom: 1px solid var(--border-separator);
}

th {
  position: sticky;
  top: 0;
  background: var(--color-surface-container-high);
}
</style>
