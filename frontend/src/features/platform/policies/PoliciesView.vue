<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { usePoliciesStore } from './store';
import CatalogTable from '../shared/CatalogTable.vue';
import DetailPanel from '../shared/DetailPanel.vue';
import StatusBadge from '../shared/StatusBadge.vue';
import ScopeChip from '../shared/ScopeChip.vue';
import { POLICY_CATEGORY_LABELS } from '../shared/constants';
import type { Policy } from '../shared/types';

const route = useRoute();
const router = useRouter();
const store = usePoliciesStore();
const actionError = ref<string | null>(null);
const saving = ref(false);
const editingId = ref<string | null>(null);
const form = reactive({
  key: '',
  name: '',
  category: 'approval' as Policy['category'],
  scopeType: 'platform' as Policy['scopeType'],
  scopeId: '*',
  boundTo: '',
  status: 'active' as Policy['status'],
  bodyText: '{\n  "required": true\n}',
});
const exceptionForm = reactive({
  reason: '',
  requesterId: '',
  approverId: '',
  expiresAt: '2099-01-01T00:00:00Z',
});

const columns = [
  { key: 'name', label: 'Name', width: '25%' },
  { key: 'category', label: 'Category' },
  { key: 'scopeType', label: 'Scope' },
  { key: 'status', label: 'Status' },
  { key: 'boundTo', label: 'Bound To' },
  { key: 'version', label: 'Ver' },
];
const categories = Object.keys(POLICY_CATEGORY_LABELS) as Policy['category'][];
const statuses: Policy['status'][] = ['draft', 'active', 'inactive'];
const scopeTypes: Policy['scopeType'][] = ['platform', 'application', 'snow_group', 'workspace', 'project'];
const isEditing = computed(() => editingId.value != null);

store.fetchPolicies();

watch(() => route.params.id, (id) => {
  void store.selectPolicy(typeof id === 'string' ? id : null);
}, { immediate: true });

function onRowClick(row: Record<string, unknown>) {
  router.push(`/platform/policies/${row.id}`);
}

function onClose() {
  router.push('/platform/policies');
}

function resetForm() {
  editingId.value = null;
  form.key = '';
  form.name = '';
  form.category = 'approval';
  form.scopeType = 'platform';
  form.scopeId = '*';
  form.boundTo = '';
  form.status = 'active';
  form.bodyText = '{\n  "required": true\n}';
}

function editPolicy(policy: Policy) {
  editingId.value = policy.id;
  form.key = policy.key;
  form.name = policy.name;
  form.category = policy.category;
  form.scopeType = policy.scopeType;
  form.scopeId = policy.scopeId;
  form.boundTo = policy.boundTo ?? '';
  form.status = 'active';
  form.bodyText = JSON.stringify(policy.body, null, 2);
}

async function submitPolicy() {
  actionError.value = null;
  saving.value = true;
  try {
    const body = JSON.parse(form.bodyText);
    const payload = {
      key: form.key,
      name: form.name,
      category: form.category,
      scopeType: form.scopeType,
      scopeId: form.scopeId,
      boundTo: form.boundTo.trim() ? form.boundTo.trim() : null,
      status: form.status,
      body,
    };
    const saved = isEditing.value && editingId.value
      ? await store.updatePolicy(editingId.value, payload)
      : await store.createPolicy(payload);
    resetForm();
    router.push(`/platform/policies/${saved.id}`);
  } catch (e) {
    actionError.value = e instanceof Error ? e.message : 'Failed to save policy';
  } finally {
    saving.value = false;
  }
}

async function setPolicyStatus(policy: Policy, status: 'active' | 'inactive') {
  actionError.value = null;
  try {
    const saved = status === 'active'
      ? await store.activatePolicy(policy.id)
      : await store.deactivatePolicy(policy.id);
    if (store.selectedId === policy.id) {
      await store.selectPolicy(saved.id);
    }
  } catch (e) {
    actionError.value = e instanceof Error ? e.message : 'Failed to update policy';
  }
}

async function submitException(policy: Policy) {
  actionError.value = null;
  try {
    await store.addException(policy.id, { ...exceptionForm });
    exceptionForm.reason = '';
  } catch (e) {
    actionError.value = e instanceof Error ? e.message : 'Failed to add exception';
  }
}

async function revokeException(policyId: string, exceptionId: string) {
  actionError.value = null;
  try {
    await store.revokeException(policyId, exceptionId);
  } catch (e) {
    actionError.value = e instanceof Error ? e.message : 'Failed to revoke exception';
  }
}
</script>

<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 16px;">Policies</h2>
    <div v-if="actionError" class="error-banner">{{ actionError }}</div>

    <form class="policy-form" @submit.prevent="submitPolicy">
      <div class="field">
        <label>Key</label>
        <input v-model="form.key" required placeholder="release-approval" />
      </div>
      <div class="field">
        <label>Name</label>
        <input v-model="form.name" required placeholder="Release Approval" />
      </div>
      <div class="field">
        <label>Category</label>
        <select v-model="form.category">
          <option v-for="category in categories" :key="category" :value="category">
            {{ POLICY_CATEGORY_LABELS[category] }}
          </option>
        </select>
      </div>
      <div class="field">
        <label>Status</label>
        <select v-model="form.status">
          <option v-for="status in statuses" :key="status" :value="status">{{ status }}</option>
        </select>
      </div>
      <div class="field">
        <label>Scope</label>
        <select v-model="form.scopeType">
          <option v-for="scopeType in scopeTypes" :key="scopeType" :value="scopeType">{{ scopeType }}</option>
        </select>
      </div>
      <div class="field">
        <label>Scope ID</label>
        <input v-model="form.scopeId" required />
      </div>
      <div class="field wide">
        <label>Bound To</label>
        <input v-model="form.boundTo" placeholder="deploy.release" />
      </div>
      <div class="field wide">
        <label>Body</label>
        <textarea v-model="form.bodyText" rows="5" spellcheck="false" />
      </div>
      <div class="form-actions">
        <button type="submit" :disabled="saving">{{ isEditing ? 'Save New Version' : 'Create Policy' }}</button>
        <button v-if="isEditing" type="button" class="secondary" @click="resetForm">Cancel</button>
      </div>
    </form>

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
        <div class="detail-actions">
          <button type="button" @click="editPolicy(store.selectedPolicy)">Edit</button>
          <button v-if="store.selectedPolicy.status !== 'active'" type="button" @click="setPolicyStatus(store.selectedPolicy, 'active')">Activate</button>
          <button v-if="store.selectedPolicy.status !== 'inactive'" type="button" class="secondary" @click="setPolicyStatus(store.selectedPolicy, 'inactive')">Deactivate</button>
        </div>

        <pre class="body-preview">{{ JSON.stringify(store.selectedPolicy.body, null, 2) }}</pre>

        <h3 style="margin-top: 24px; font-size: 14px; font-weight: 600;">
          Exceptions ({{ store.selectedExceptions.length }})
        </h3>
        <form class="exception-form" @submit.prevent="submitException(store.selectedPolicy)">
          <input v-model="exceptionForm.reason" required placeholder="Reason" />
          <input v-model="exceptionForm.requesterId" required placeholder="Requester ID" />
          <input v-model="exceptionForm.approverId" required placeholder="Approver ID" />
          <input v-model="exceptionForm.expiresAt" required placeholder="2099-01-01T00:00:00Z" />
          <button type="submit">Add</button>
        </form>
        <div v-for="ex in store.selectedExceptions" :key="ex.id" class="exception-row">
          <span>{{ ex.reason }}</span>
          <span class="meta">
            Expires: {{ new Date(ex.expiresAt).toLocaleDateString() }}
            <template v-if="ex.revokedAt"> · Revoked: {{ new Date(ex.revokedAt).toLocaleDateString() }}</template>
          </span>
          <button v-if="!ex.revokedAt" type="button" class="link-button" @click="revokeException(store.selectedPolicy.id, ex.id)">Revoke</button>
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
.error-banner { margin-bottom: 12px; padding: 10px 12px; border: 1px solid #8b2d2d; color: #ffb4ab; background: rgba(139,45,45,0.18); border-radius: 6px; font-size: 13px; }
.policy-form { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-bottom: 18px; padding: 14px; border: 1px solid var(--color-border-subtle, #292929); border-radius: 8px; }
.field { display: flex; flex-direction: column; gap: 5px; min-width: 0; }
.field.wide { grid-column: span 2; }
.field label { font-size: 12px; color: var(--color-on-surface-variant, #999); }
input, select, textarea { width: 100%; box-sizing: border-box; border: 1px solid var(--color-border-subtle, #292929); border-radius: 6px; background: var(--color-surface, #101010); color: inherit; padding: 8px 10px; font: inherit; font-size: 13px; }
textarea { font-family: var(--font-mono, monospace); resize: vertical; }
button { border: 0; border-radius: 6px; padding: 8px 12px; background: var(--color-primary, #6ea8fe); color: #08111f; font-weight: 600; cursor: pointer; }
button.secondary { background: transparent; color: inherit; border: 1px solid var(--color-border-subtle, #292929); }
.form-actions, .detail-actions { display: flex; gap: 8px; align-items: flex-end; flex-wrap: wrap; }
.body-preview { margin-top: 16px; padding: 12px; border: 1px solid var(--color-border-subtle, #292929); border-radius: 6px; overflow: auto; font-size: 12px; }
.exception-form { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; margin: 12px 0; }
.exception-row { position: relative; padding: 8px 72px 8px 0; border-bottom: 1px solid var(--color-border-subtle, #1a1a1a); font-size: 13px; }
.meta { display: block; font-size: 11px; color: var(--color-on-surface-variant, #999); margin-top: 4px; }
.link-button { position: absolute; right: 0; top: 8px; padding: 0; color: var(--color-primary, #6ea8fe); background: transparent; font-size: 12px; }
@media (max-width: 920px) {
  .policy-form { grid-template-columns: 1fr; }
  .field.wide { grid-column: auto; }
  .exception-form { grid-template-columns: 1fr; }
}
</style>
