<script setup lang="ts">
import { onMounted } from 'vue';
import { useAccessStore } from './store';
import CatalogTable from '../shared/CatalogTable.vue';
import ScopeChip from '../shared/ScopeChip.vue';
import StatusBadge from '../shared/StatusBadge.vue';

const store = useAccessStore();

const columns = [
  { key: 'userDisplayName', label: 'User', width: '25%' },
  { key: 'role', label: 'Role' },
  { key: 'scopeType', label: 'Scope' },
  { key: 'grantedBy', label: 'Granted By' },
  { key: 'grantedAt', label: 'Granted At' },
];

onMounted(() => { store.fetchAssignments(); });
</script>

<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 16px;">Access Control</h2>

    <CatalogTable
      :rows="(store.items as unknown as Record<string, unknown>[])"
      :columns="columns"
      :state="store.tableState"
      :error-message="store.error ?? undefined"
      empty-message="No role assignments found"
      @retry="store.fetchAssignments"
    >
      <template #cell-role="{ value }">
        <StatusBadge :status="String(value) === 'PLATFORM_ADMIN' ? 'active' : 'draft'" />
        <span style="margin-left: 6px; font-size: 12px;">{{ value }}</span>
      </template>
      <template #cell-scopeType="{ row }">
        <ScopeChip :scope-type="String(row.scopeType) as any" :scope-id="String(row.scopeId)" />
      </template>
      <template #cell-grantedAt="{ value }">
        {{ value ? new Date(String(value)).toLocaleDateString() : '—' }}
      </template>
    </CatalogTable>
  </div>
</template>
