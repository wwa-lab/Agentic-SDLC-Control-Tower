<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useIntegrationsStore } from './store';
import CatalogTable from '../shared/CatalogTable.vue';
import DetailPanel from '../shared/DetailPanel.vue';
import StatusBadge from '../shared/StatusBadge.vue';
import { ADAPTER_KIND_LABELS } from '../shared/constants';
import type { AdapterKind, Connection, ConnectionStatus, SyncMode } from '../shared/types';

const route = useRoute();
const router = useRouter();
const store = useIntegrationsStore();
const isEditing = ref(false);
const isCreating = ref(false);

const columns = [
  { key: 'kind', label: 'Type' },
  { key: 'scopeWorkspaceId', label: 'Workspace' },
  { key: 'ownerScope', label: 'Team Scope' },
  { key: 'baseUrl', label: 'Base URL' },
  { key: 'syncMode', label: 'Mode' },
  { key: 'status', label: 'Status' },
  { key: 'lastTestOk', label: 'Health' },
  { key: 'lastSyncAt', label: 'Last Sync' },
];

interface ConnectionForm {
  id: string | null;
  kind: AdapterKind;
  scopeWorkspaceId: string;
  applicationId: string;
  applicationName: string;
  snowGroupId: string;
  snowGroupName: string;
  baseUrl: string;
  credentialRef: string;
  syncMode: SyncMode;
  pullSchedule: string;
  pushUrl: string;
  status: ConnectionStatus;
}

const form = reactive<ConnectionForm>({
  id: null,
  kind: 'jira',
  scopeWorkspaceId: 'ws-default',
  applicationId: 'app-payment-gateway-pro',
  applicationName: 'Payment-Gateway-Pro',
  snowGroupId: 'snow-fin-tech-ops',
  snowGroupName: 'FIN-TECH-OPS',
  baseUrl: '',
  credentialRef: '',
  syncMode: 'pull',
  pullSchedule: '0 */15 * * * *',
  pushUrl: '',
  status: 'enabled',
});

const supportedModes = computed<SyncMode[]>(() =>
  store.adapters.find(adapter => adapter.kind === form.kind)?.supportedModes ?? ['pull']
);

const panelOpen = computed(() => isCreating.value || store.selectedConnection != null);
const panelTitle = computed(() => {
  if (isCreating.value) return 'New Connection';
  return store.selectedConnection ? ADAPTER_KIND_LABELS[store.selectedConnection.kind] : 'Connection';
});

onMounted(() => { store.fetchConnections(); });

watch(() => route.params.id, (id) => {
  if (id === 'new') {
    store.selectConnection(null);
    resetForm();
    isCreating.value = true;
    isEditing.value = true;
    return;
  }
  isCreating.value = false;
  isEditing.value = false;
  store.selectConnection(typeof id === 'string' ? id : null);
}, { immediate: true });

watch(() => store.selectedConnection, (connection) => {
  if (connection && !isCreating.value && !isEditing.value) fillForm(connection);
}, { immediate: true });

function onRowClick(row: Record<string, unknown>) {
  router.push(`/platform/integrations/${row.id}`);
}

function onClose() {
  isCreating.value = false;
  isEditing.value = false;
  router.push('/platform/integrations');
}

function openCreate() {
  router.push('/platform/integrations/new');
}

function startEdit() {
  if (!store.selectedConnection) return;
  fillForm(store.selectedConnection);
  isEditing.value = true;
}

function cancelEdit() {
  if (isCreating.value) {
    onClose();
    return;
  }
  if (store.selectedConnection) fillForm(store.selectedConnection);
  isEditing.value = false;
}

async function saveConnection() {
  normalizeModeForKind();
  const saved = await store.saveConnection({
    id: form.id,
    kind: form.kind,
    scopeWorkspaceId: form.scopeWorkspaceId,
    applicationId: form.applicationId,
    applicationName: form.applicationName,
    snowGroupId: form.snowGroupId,
    snowGroupName: form.snowGroupName,
    baseUrl: form.baseUrl,
    credentialRef: form.credentialRef,
    syncMode: form.syncMode,
    pullSchedule: form.syncMode === 'pull' || form.syncMode === 'both' ? form.pullSchedule : null,
    pushUrl: form.syncMode === 'push' || form.syncMode === 'both' ? form.pushUrl : null,
    status: form.status,
  });
  isCreating.value = false;
  isEditing.value = false;
  router.push(`/platform/integrations/${saved.id}`);
}

function resetForm() {
  form.id = null;
  form.kind = 'jira';
  form.scopeWorkspaceId = 'ws-default';
  form.applicationId = 'app-payment-gateway-pro';
  form.applicationName = 'Payment-Gateway-Pro';
  form.snowGroupId = 'snow-fin-tech-ops';
  form.snowGroupName = 'FIN-TECH-OPS';
  form.baseUrl = '';
  form.credentialRef = '';
  form.syncMode = 'pull';
  form.pullSchedule = '0 */15 * * * *';
  form.pushUrl = '';
  form.status = 'enabled';
}

function fillForm(connection: Connection) {
  form.id = connection.id;
  form.kind = connection.kind;
  form.scopeWorkspaceId = connection.scopeWorkspaceId;
  form.applicationId = connection.applicationId ?? '';
  form.applicationName = connection.applicationName ?? '';
  form.snowGroupId = connection.snowGroupId ?? '';
  form.snowGroupName = connection.snowGroupName ?? '';
  form.baseUrl = connection.baseUrl ?? '';
  form.credentialRef = connection.credentialRef;
  form.syncMode = connection.syncMode;
  form.pullSchedule = connection.pullSchedule ?? '';
  form.pushUrl = connection.pushUrl ?? '';
  form.status = connection.status;
}

function normalizeModeForKind() {
  if (!supportedModes.value.includes(form.syncMode)) {
    form.syncMode = supportedModes.value[0] ?? 'pull';
  }
}
</script>

<template>
  <div>
    <div class="view-header">
      <h2>Integrations</h2>
      <button class="btn-machined" type="button" @click="openCreate">Add Connection</button>
    </div>

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
      <template #cell-ownerScope="{ row }">
        <span class="scope-stack">
          <span class="scope-primary">{{ row.applicationName || row.applicationId || 'Platform default' }}</span>
          <span class="scope-muted">{{ row.snowGroupName || row.snowGroupId || 'No SNOW group' }}</span>
        </span>
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
      :open="panelOpen"
      :title="panelTitle"
      @close="onClose"
    >
      <template #header-actions>
        <button
          v-if="store.selectedConnection && !isEditing"
          class="panel-action"
          type="button"
          @click="startEdit"
        >
          Edit
        </button>
      </template>

      <form v-if="isEditing" class="config-form" @submit.prevent="saveConnection">
        <label class="field">
          <span>Type</span>
          <select v-model="form.kind" @change="normalizeModeForKind">
            <option v-for="adapter in store.adapters" :key="adapter.kind" :value="adapter.kind">
              {{ ADAPTER_KIND_LABELS[adapter.kind] }}
            </option>
          </select>
        </label>

        <label class="field">
          <span>Workspace</span>
          <input v-model.trim="form.scopeWorkspaceId" required autocomplete="off" />
        </label>

        <div class="field-pair">
          <label class="field">
            <span>Application ID</span>
            <input v-model.trim="form.applicationId" placeholder="app-payment-gateway-pro" autocomplete="off" />
          </label>

          <label class="field">
            <span>Application Name</span>
            <input v-model.trim="form.applicationName" placeholder="Payment-Gateway-Pro" autocomplete="off" />
          </label>
        </div>

        <div class="field-pair">
          <label class="field">
            <span>SNOW Group ID</span>
            <input v-model.trim="form.snowGroupId" placeholder="snow-fin-tech-ops" autocomplete="off" />
          </label>

          <label class="field">
            <span>SNOW Group Name</span>
            <input v-model.trim="form.snowGroupName" placeholder="FIN-TECH-OPS" autocomplete="off" />
          </label>
        </div>

        <label class="field">
          <span>Base URL</span>
          <input v-model.trim="form.baseUrl" type="url" placeholder="https://jira.company.com" autocomplete="off" />
        </label>

        <label class="field">
          <span>Credential Ref</span>
          <input v-model.trim="form.credentialRef" required placeholder="cred-jira-prod" autocomplete="off" />
        </label>

        <label class="field">
          <span>Sync Mode</span>
          <select v-model="form.syncMode">
            <option v-for="mode in supportedModes" :key="mode" :value="mode">{{ mode }}</option>
          </select>
        </label>

        <label v-if="form.syncMode === 'pull' || form.syncMode === 'both'" class="field">
          <span>Pull Schedule</span>
          <input v-model.trim="form.pullSchedule" placeholder="0 */15 * * * *" autocomplete="off" />
        </label>

        <label v-if="form.syncMode === 'push' || form.syncMode === 'both'" class="field">
          <span>Webhook URL</span>
          <input v-model.trim="form.pushUrl" type="url" placeholder="https://webhook.company.com/jira" autocomplete="off" />
        </label>

        <label class="field">
          <span>Status</span>
          <select v-model="form.status">
            <option value="enabled">enabled</option>
            <option value="disabled">disabled</option>
            <option value="error">error</option>
          </select>
        </label>

        <div class="form-actions">
          <button class="btn-machined" type="submit">Save</button>
          <button class="btn-secondary" type="button" @click="cancelEdit">Cancel</button>
        </div>
      </form>

      <template v-else-if="store.selectedConnection">
        <div class="detail-grid">
          <div><span class="label">Type</span><span>{{ ADAPTER_KIND_LABELS[store.selectedConnection.kind] }}</span></div>
          <div><span class="label">Workspace</span><span>{{ store.selectedConnection.scopeWorkspaceId }}</span></div>
          <div><span class="label">Application</span><span>{{ store.selectedConnection.applicationName ?? store.selectedConnection.applicationId ?? 'N/A' }}</span></div>
          <div><span class="label">SNOW Group</span><span>{{ store.selectedConnection.snowGroupName ?? store.selectedConnection.snowGroupId ?? 'N/A' }}</span></div>
          <div><span class="label">Base URL</span><span class="mono">{{ store.selectedConnection.baseUrl ?? 'N/A' }}</span></div>
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
.view-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.view-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.detail-grid { display: grid; grid-template-columns: 1fr; gap: 12px; }
.detail-grid > div { display: flex; gap: 8px; align-items: baseline; }
.label { font-size: 12px; color: var(--color-on-surface-variant, #999); min-width: 80px; }
.mono { font-family: var(--font-mono, monospace); font-size: 13px; }
.health-ok { color: #4caf50; font-weight: 600; font-size: 12px; }
.health-fail { color: #f44336; font-weight: 600; font-size: 12px; }
.scope-stack {
  display: grid;
  gap: 2px;
}

.scope-primary {
  color: var(--color-on-surface, #ddd);
}

.scope-muted {
  color: var(--color-on-surface-variant, #999);
  font-size: 12px;
}

.panel-action,
.btn-secondary {
  border: 1px solid var(--color-border, #2a2a2a);
  border-radius: 4px;
  background: transparent;
  color: var(--color-on-surface, #ddd);
  cursor: pointer;
  font-size: 12px;
  padding: 6px 10px;
}

.config-form {
  display: grid;
  gap: 14px;
}

.field {
  display: grid;
  gap: 6px;
}

.field-pair {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.field span {
  color: var(--color-on-surface-variant, #999);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.field input,
.field select {
  min-height: 36px;
  border: 1px solid var(--color-border, #2a2a2a);
  border-radius: 4px;
  background: var(--color-surface-container, #171717);
  color: var(--color-on-surface, #ddd);
  font: 13px var(--font-mono, monospace);
  padding: 8px 10px;
}

.form-actions {
  display: flex;
  gap: 8px;
  margin-top: 6px;
}

@media (max-width: 720px) {
  .field-pair { grid-template-columns: 1fr; }
}
</style>
