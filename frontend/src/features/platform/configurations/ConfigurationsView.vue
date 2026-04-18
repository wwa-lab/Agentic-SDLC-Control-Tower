<script setup lang="ts">
import { onMounted } from 'vue';
import { useConfigurationsStore } from './store';
import CatalogTable from '../shared/CatalogTable.vue';
import StatusBadge from '../shared/StatusBadge.vue';
import ScopeChip from '../shared/ScopeChip.vue';

const store = useConfigurationsStore();

const columns = [
  { key: 'key', label: 'Key', width: '25%' },
  { key: 'kind', label: 'Kind' },
  { key: 'scopeType', label: 'Scope' },
  { key: 'status', label: 'Status' },
  { key: 'hasDrift', label: 'Drift' },
  { key: 'lastModifiedAt', label: 'Last Modified' },
];

onMounted(() => { store.fetchCatalog(); });
</script>

<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 16px;">Configurations</h2>

    <CatalogTable
      :rows="(store.items as unknown as Record<string, unknown>[])"
      :columns="columns"
      :state="store.tableState"
      :error-message="store.error ?? undefined"
      empty-message="No configurations found"
      @retry="store.fetchCatalog"
    >
      <template #cell-key="{ value }">
        <span style="font-family: var(--font-mono, monospace); font-size: 13px;">{{ value }}</span>
      </template>
      <template #cell-scopeType="{ row }">
        <ScopeChip :scope-type="String(row.scopeType) as any" :scope-id="String(row.scopeId)" />
      </template>
      <template #cell-status="{ value }">
        <StatusBadge :status="String(value)" />
      </template>
      <template #cell-hasDrift="{ value }">
        <span v-if="value" class="drift-indicator" title="Configuration has drifted from platform default">DRIFT</span>
        <span v-else>—</span>
      </template>
      <template #cell-lastModifiedAt="{ value }">
        {{ value ? new Date(String(value)).toLocaleDateString() : '—' }}
      </template>
    </CatalogTable>
  </div>
</template>

<style scoped>
.drift-indicator {
  font-size: 11px;
  font-weight: 600;
  color: #ffc107;
  background: rgba(255,193,7,0.12);
  padding: 2px 6px;
  border-radius: 3px;
  border: 1px dashed #ffc107;
}
</style>
