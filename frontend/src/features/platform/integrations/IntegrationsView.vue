<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useIntegrationsStore } from './store';
import CatalogTable from '../shared/CatalogTable.vue';
import DetailPanel from '../shared/DetailPanel.vue';
import StatusBadge from '../shared/StatusBadge.vue';
import { ADAPTER_KIND_LABELS } from '../shared/constants';

const route = useRoute();
const router = useRouter();
const store = useIntegrationsStore();

const columns = [
  { key: 'kind', label: 'Type' },
  { key: 'scopeWorkspaceId', label: 'Workspace' },
  { key: 'syncMode', label: 'Mode' },
  { key: 'status', label: 'Status' },
  { key: 'lastTestOk', label: 'Health' },
  { key: 'lastSyncAt', label: 'Last Sync' },
];

onMounted(() => { store.fetchConnections(); });

watch(() => route.params.id, (id) => {
  store.selectConnection(typeof id === 'string' ? id : null);
}, { immediate: true });

function onRowClick(row: Record<string, unknown>) {
  router.push(`/platform/integrations/${row.id}`);
}

function onClose() {
  router.push('/platform/integrations');
}
</script>

<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 16px;">Integrations</h2>

    <CatalogTable
      :rows="(store.connections as unknown as Record<string, unknown>[])"
      :columns="columns"
      :state="store.tableState"
      :error-message="store.error ?? undefined"
      empty-message="No connections configured"
      :selected-id="store.selectedId ?? undefined"
      @row-click="onRowClick"
      @retry="store.fetchConnections"
    >
      <template #cell-kind="{ value }">
        {{ ADAPTER_KIND_LABELS[value as keyof typeof ADAPTER_KIND_LABELS] ?? value }}
      </template>
      <template #cell-status="{ value }">
        <StatusBadge :status="String(value)" />
      </template>
      <template #cell-lastTestOk="{ value }">
        <span v-if="value === true" class="health-ok" title="Healthy">OK</span>
        <span v-else-if="value === false" class="health-fail" title="Failing">FAIL</span>
        <span v-else>—</span>
      </template>
      <template #cell-lastSyncAt="{ value }">
        {{ value ? new Date(String(value)).toLocaleString() : '—' }}
      </template>
    </CatalogTable>

    <DetailPanel
      :open="store.selectedConnection != null"
      :title="store.selectedConnection ? ADAPTER_KIND_LABELS[store.selectedConnection.kind] : 'Connection'"
      @close="onClose"
    >
      <template v-if="store.selectedConnection">
        <div class="detail-grid">
          <div><span class="label">Type</span><span>{{ ADAPTER_KIND_LABELS[store.selectedConnection.kind] }}</span></div>
          <div><span class="label">Workspace</span><span>{{ store.selectedConnection.scopeWorkspaceId }}</span></div>
          <div><span class="label">Credential</span><span class="mono">{{ store.selectedConnection.credentialRef }}</span></div>
          <div><span class="label">Sync Mode</span><span>{{ store.selectedConnection.syncMode }}</span></div>
          <div><span class="label">Status</span><StatusBadge :status="store.selectedConnection.status" /></div>
          <div v-if="store.selectedConnection.pullSchedule"><span class="label">Schedule</span><span class="mono">{{ store.selectedConnection.pullSchedule }}</span></div>
        </div>

        <div style="margin-top: 24px;">
          <button class="btn-machined" :disabled="store.testLoading" @click="store.testConnection(store.selectedConnection!.id)">
            {{ store.testLoading ? 'Testing...' : 'Test Connection' }}
          </button>
          <div v-if="store.testResult" style="margin-top: 12px; font-size: 13px;">
            <span :class="store.testResult.ok ? 'health-ok' : 'health-fail'">
              {{ store.testResult.ok ? 'SUCCESS' : 'FAILED' }}
            </span>
            <span style="margin-left: 8px;">{{ store.testResult.latencyMs }}ms</span>
            <span v-if="store.testResult.message" style="margin-left: 8px; color: var(--color-on-surface-variant, #999);">
              — {{ store.testResult.message }}
            </span>
          </div>
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
.health-ok { color: #4caf50; font-weight: 600; font-size: 12px; }
.health-fail { color: #f44336; font-weight: 600; font-size: 12px; }
</style>
