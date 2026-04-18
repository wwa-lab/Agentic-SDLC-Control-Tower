<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { usePoliciesStore } from './store';
import CatalogTable from '../shared/CatalogTable.vue';
import DetailPanel from '../shared/DetailPanel.vue';
import StatusBadge from '../shared/StatusBadge.vue';
import ScopeChip from '../shared/ScopeChip.vue';
import { POLICY_CATEGORY_LABELS } from '../shared/constants';

const route = useRoute();
const router = useRouter();
const store = usePoliciesStore();

const columns = [
  { key: 'name', label: 'Name', width: '25%' },
  { key: 'category', label: 'Category' },
  { key: 'scopeType', label: 'Scope' },
  { key: 'status', label: 'Status' },
  { key: 'boundTo', label: 'Bound To' },
  { key: 'version', label: 'Ver' },
];

onMounted(() => { store.fetchPolicies(); });

watch(() => route.params.id, (id) => {
  store.selectPolicy(typeof id === 'string' ? id : null);
}, { immediate: true });

function onRowClick(row: Record<string, unknown>) {
  router.push(`/platform/policies/${row.id}`);
}

function onClose() {
  router.push('/platform/policies');
}
</script>

<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 16px;">Policies</h2>

    <CatalogTable
      :rows="(store.items as unknown as Record<string, unknown>[])"
      :columns="columns"
      :state="store.tableState"
      :error-message="store.error ?? undefined"
      empty-message="No policies found"
      :selected-id="store.selectedId ?? undefined"
      @row-click="onRowClick"
      @retry="store.fetchPolicies"
    >
      <template #cell-category="{ value }">
        {{ POLICY_CATEGORY_LABELS[value as keyof typeof POLICY_CATEGORY_LABELS] ?? value }}
      </template>
      <template #cell-scopeType="{ row }">
        <ScopeChip :scope-type="String(row.scopeType) as any" :scope-id="String(row.scopeId)" />
      </template>
      <template #cell-status="{ value }">
        <StatusBadge :status="String(value)" />
      </template>
      <template #cell-boundTo="{ value }">
        <span v-if="value" class="mono">{{ value }}</span>
        <span v-else>—</span>
      </template>
    </CatalogTable>

    <DetailPanel
      :open="store.selectedPolicy != null"
      :title="store.selectedPolicy?.name ?? 'Policy'"
      @close="onClose"
    >
      <template v-if="store.selectedPolicy">
        <div class="detail-grid">
          <div><span class="label">Key</span><span class="mono">{{ store.selectedPolicy.key }}</span></div>
          <div><span class="label">Category</span><span>{{ POLICY_CATEGORY_LABELS[store.selectedPolicy.category] }}</span></div>
          <div><span class="label">Status</span><StatusBadge :status="store.selectedPolicy.status" /></div>
          <div><span class="label">Bound To</span><span class="mono">{{ store.selectedPolicy.boundTo ?? '—' }}</span></div>
          <div><span class="label">Version</span><span>{{ store.selectedPolicy.version }}</span></div>
          <div><span class="label">Created By</span><span>{{ store.selectedPolicy.createdBy }}</span></div>
        </div>

        <h3 v-if="store.selectedExceptions.length > 0" style="margin-top: 24px; font-size: 14px; font-weight: 600;">
          Exceptions ({{ store.selectedExceptions.length }})
        </h3>
        <div v-for="ex in store.selectedExceptions" :key="ex.id" class="exception-row">
          <span>{{ ex.reason }}</span>
          <span class="meta">Expires: {{ new Date(ex.expiresAt).toLocaleDateString() }}</span>
        </div>
      </template>
    </DetailPanel>
  </div>
</template>

<style scoped>
.detail-grid { display: grid; grid-template-columns: 1fr; gap: 12px; }
.detail-grid > div { display: flex; gap: 8px; align-items: baseline; }
.label { font-size: 12px; color: var(--color-on-surface-variant, #999); min-width: 80px; }
.mono { font-family: var(--font-mono, monospace); font-size: 13px; }
.exception-row { padding: 8px 0; border-bottom: 1px solid var(--color-border-subtle, #1a1a1a); font-size: 13px; }
.meta { display: block; font-size: 11px; color: var(--color-on-surface-variant, #999); margin-top: 4px; }
</style>
