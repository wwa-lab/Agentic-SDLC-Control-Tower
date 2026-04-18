<script setup lang="ts">
import { onMounted } from 'vue';
import { useAuditStore } from './store';
import CatalogTable from '../shared/CatalogTable.vue';
import ScopeChip from '../shared/ScopeChip.vue';
import { AUDIT_CATEGORY_LABELS } from '../shared/constants';

const store = useAuditStore();

const columns = [
  { key: 'timestamp', label: 'When', width: '15%' },
  { key: 'actor', label: 'Who' },
  { key: 'category', label: 'Category' },
  { key: 'action', label: 'Action' },
  { key: 'objectType', label: 'Entity' },
  { key: 'scopeType', label: 'Scope' },
  { key: 'outcome', label: 'Result' },
];

onMounted(() => { store.fetchAudit(); });
</script>

<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 16px;">Audit Log</h2>

    <CatalogTable
      :rows="(store.items as unknown as Record<string, unknown>[])"
      :columns="columns"
      :state="store.tableState"
      :error-message="store.error ?? undefined"
      empty-message="No audit events found"
      @retry="store.fetchAudit"
    >
      <template #cell-timestamp="{ value }">
        {{ value ? new Date(String(value)).toLocaleString() : '—' }}
      </template>
      <template #cell-category="{ value }">
        {{ AUDIT_CATEGORY_LABELS[value as keyof typeof AUDIT_CATEGORY_LABELS] ?? value }}
      </template>
      <template #cell-scopeType="{ row }">
        <ScopeChip :scope-type="String(row.scopeType) as any" :scope-id="String(row.scopeId)" />
      </template>
      <template #cell-outcome="{ value }">
        <span class="outcome" :class="String(value)">{{ value }}</span>
      </template>
    </CatalogTable>
  </div>
</template>

<style scoped>
.outcome { font-size: 12px; font-weight: 500; text-transform: uppercase; }
.outcome.success { color: #4caf50; }
.outcome.failure { color: #f44336; }
.outcome.rejected { color: #ff9800; }
.outcome.rolled_back { color: #9e9e9e; }
</style>
