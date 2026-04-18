<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useTemplatesStore } from './store';
import CatalogTable from '../shared/CatalogTable.vue';
import DetailPanel from '../shared/DetailPanel.vue';
import StatusBadge from '../shared/StatusBadge.vue';
import type { TemplateSummary } from '../shared/types';

const route = useRoute();
const router = useRouter();
const store = useTemplatesStore();

const columns = [
  { key: 'name', label: 'Name', width: '30%' },
  { key: 'kind', label: 'Kind' },
  { key: 'status', label: 'Status' },
  { key: 'version', label: 'Version' },
  { key: 'ownerId', label: 'Owner' },
  { key: 'lastModifiedAt', label: 'Last Modified' },
];

onMounted(() => { store.fetchCatalog(); });

watch(() => route.params.id, (id) => {
  if (typeof id === 'string') { store.fetchDetail(id); }
  else { store.clearSelection(); }
}, { immediate: true });

function onRowClick(row: Record<string, unknown>) {
  router.push(`/platform/templates/${row.id}`);
}

function onClose() {
  router.push('/platform/templates');
}
</script>

<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 16px;">Templates</h2>

    <CatalogTable
      :rows="(store.items as unknown as Record<string, unknown>[])"
      :columns="columns"
      :state="store.tableState"
      :error-message="store.error ?? undefined"
      empty-message="No templates found"
      :selected-id="store.selectedId ?? undefined"
      @row-click="onRowClick"
      @retry="store.fetchCatalog"
    >
      <template #cell-status="{ value }">
        <StatusBadge :status="String(value)" />
      </template>
      <template #cell-lastModifiedAt="{ value }">
        {{ value ? new Date(String(value)).toLocaleDateString() : '—' }}
      </template>
    </CatalogTable>

    <DetailPanel
      :open="store.selectedId != null"
      :title="store.detail?.template?.name ?? 'Template'"
      @close="onClose"
    >
      <template v-if="store.detail">
        <div class="detail-grid">
          <div><span class="label">Key</span><span class="mono">{{ store.detail.template.key }}</span></div>
          <div><span class="label">Kind</span><span>{{ store.detail.template.kind }}</span></div>
          <div><span class="label">Status</span><StatusBadge :status="store.detail.template.status" /></div>
          <div><span class="label">Version</span><span>{{ store.detail.version.version }}</span></div>
          <div><span class="label">Owner</span><span>{{ store.detail.template.ownerId }}</span></div>
          <div><span class="label">Description</span><span>{{ store.detail.template.description ?? '—' }}</span></div>
        </div>

        <h3 style="margin-top: 24px; font-size: 14px; font-weight: 600;">Version History</h3>
        <div v-for="v in store.versions" :key="v.id" class="version-row">
          <span class="mono">v{{ v.version }}</span>
          <span>{{ new Date(v.createdAt).toLocaleDateString() }}</span>
          <span>{{ v.createdBy }}</span>
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
.version-row { display: flex; gap: 16px; padding: 8px 0; border-bottom: 1px solid var(--color-border-subtle, #1a1a1a); font-size: 13px; }
</style>
