<script setup lang="ts">
import { reactive, ref } from 'vue';
import { onMounted } from 'vue';
import { useConfigurationsStore } from './store';
import CatalogTable from '../shared/CatalogTable.vue';
import StatusBadge from '../shared/StatusBadge.vue';
import ScopeChip from '../shared/ScopeChip.vue';
import type { ConfigurationSummary, ScopeType } from '../shared/types';

const store = useConfigurationsStore();
const busy = ref(false);
const configForm = reactive({
  editingId: '',
  key: '',
  kind: 'component',
  scopeType: 'platform' as ScopeType,
  scopeId: '*',
  parentId: '',
  status: 'active',
  bodyText: '{\n  "enabled": true\n}',
});

const columns = [
  { key: 'key', label: 'Key', width: '25%' },
  { key: 'kind', label: 'Kind' },
  { key: 'scopeType', label: 'Scope' },
  { key: 'status', label: 'Status' },
  { key: 'hasDrift', label: 'Drift' },
  { key: 'lastModifiedAt', label: 'Last Modified' },
  { key: 'actions', label: '' },
];

onMounted(() => { store.fetchCatalog(); });

function editConfiguration(row: ConfigurationSummary) {
  configForm.editingId = row.id;
  configForm.key = row.key;
  configForm.kind = row.kind;
  configForm.scopeType = row.scopeType;
  configForm.scopeId = row.scopeId;
  configForm.parentId = row.parentId ?? '';
  configForm.status = row.status;
  store.fetchDetail(row.id).then(() => {
    configForm.bodyText = JSON.stringify(store.detail?.body ?? { enabled: true }, null, 2);
  });
}

function resetForm() {
  configForm.editingId = '';
  configForm.key = '';
  configForm.kind = 'component';
  configForm.scopeType = 'platform';
  configForm.scopeId = '*';
  configForm.parentId = '';
  configForm.status = 'active';
  configForm.bodyText = '{\n  "enabled": true\n}';
}

async function saveConfiguration() {
  if (!configForm.key || !configForm.scopeId) return;
  busy.value = true;
  try {
    const body = JSON.parse(configForm.bodyText) as Record<string, unknown>;
    const payload = {
      key: configForm.key,
      kind: configForm.kind,
      scopeType: configForm.scopeType,
      scopeId: configForm.scopeType === 'platform' ? '*' : configForm.scopeId,
      parentId: configForm.parentId || null,
      status: configForm.status,
      body,
    };
    if (configForm.editingId) {
      await store.updateConfiguration(configForm.editingId, payload);
    } else {
      await store.createConfiguration(payload);
    }
    resetForm();
  } catch (e) {
    store.error = e instanceof Error ? e.message : 'Invalid configuration body';
  } finally {
    busy.value = false;
  }
}

async function deactivateConfiguration(row: ConfigurationSummary) {
  busy.value = true;
  try {
    await store.fetchDetail(row.id);
    await store.updateConfiguration(row.id, {
      key: row.key,
      kind: row.kind,
      scopeType: row.scopeType,
      scopeId: row.scopeId,
      parentId: row.parentId,
      status: 'inactive',
      body: (store.detail?.body ?? { enabled: true }) as Record<string, unknown>,
    });
  } finally {
    busy.value = false;
  }
}
</script>

<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 16px;">Configurations</h2>

    <section class="config-actions" aria-label="Configuration actions">
      <p v-if="store.error" class="error-banner">{{ store.error }}</p>
      <form class="config-form" @submit.prevent="saveConfiguration">
        <input v-model="configForm.key" placeholder="Config key" />
        <select v-model="configForm.kind">
          <option value="page">page</option>
          <option value="field">field</option>
          <option value="component">component</option>
          <option value="flow-rule">flow-rule</option>
          <option value="view-rule">view-rule</option>
          <option value="notification">notification</option>
          <option value="ai-config">ai-config</option>
        </select>
        <select v-model="configForm.scopeType">
          <option value="platform">platform</option>
          <option value="application">application</option>
          <option value="snow_group">snow_group</option>
          <option value="workspace">workspace</option>
          <option value="project">project</option>
        </select>
        <input v-model="configForm.scopeId" placeholder="Scope ID" />
        <input v-model="configForm.parentId" placeholder="Parent ID" />
        <select v-model="configForm.status">
          <option value="active">active</option>
          <option value="inactive">inactive</option>
        </select>
        <textarea v-model="configForm.bodyText" spellcheck="false" />
        <div class="form-actions">
          <button class="btn-machined" type="submit" :disabled="busy">{{ configForm.editingId ? 'Save configuration' : 'Create configuration' }}</button>
          <button class="link-button" type="button" :disabled="busy" @click="resetForm">Reset</button>
        </div>
      </form>
    </section>

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
      <template #cell-actions="{ row }">
        <button class="link-button" type="button" :disabled="busy" @click.stop="editConfiguration(row as unknown as ConfigurationSummary)">Edit</button>
        <button
          class="link-button"
          type="button"
          :disabled="busy || String(row.status) === 'inactive'"
          @click.stop="deactivateConfiguration(row as unknown as ConfigurationSummary)"
        >
          Deactivate
        </button>
      </template>
    </CatalogTable>
  </div>
</template>

<style scoped>
.config-actions {
  display: grid;
  gap: 10px;
  margin-bottom: 16px;
}

.config-form {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 8px;
  align-items: start;
}

.config-form input,
.config-form select,
.config-form textarea {
  min-width: 0;
  min-height: 34px;
  padding: 6px 8px;
  border: var(--border-subtle);
  border-radius: var(--radius-xs);
  background: var(--color-surface-container-low);
  color: var(--color-on-surface);
}

.config-form textarea {
  grid-column: 1 / -1;
  min-height: 96px;
  font-family: var(--font-mono, monospace);
  resize: vertical;
}

.form-actions {
  grid-column: 1 / -1;
  display: flex;
  gap: 10px;
  align-items: center;
}

.error-banner {
  margin: 0;
  padding: 8px 10px;
  border: 1px solid var(--color-error);
  border-radius: var(--radius-xs);
  color: var(--color-error);
  background: var(--color-error-container);
}

.link-button {
  border: 0;
  background: transparent;
  color: var(--color-primary);
  cursor: pointer;
  font-weight: 700;
  margin-right: 8px;
}

.link-button:disabled {
  color: var(--color-on-surface-variant);
  cursor: not-allowed;
}

.drift-indicator {
  font-size: 11px;
  font-weight: 600;
  color: #ffc107;
  background: rgba(255,193,7,0.12);
  padding: 2px 6px;
  border-radius: 3px;
  border: 1px dashed #ffc107;
}

@media (max-width: 980px) {
  .config-form {
    grid-template-columns: 1fr;
  }
}
</style>
