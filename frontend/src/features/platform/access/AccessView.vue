<script setup lang="ts">
import { reactive, ref } from 'vue';
import { onMounted } from 'vue';
import { useAccessStore } from './store';
import CatalogTable from '../shared/CatalogTable.vue';
import ScopeChip from '../shared/ScopeChip.vue';
import StatusBadge from '../shared/StatusBadge.vue';

const store = useAccessStore();
const busy = ref(false);
const userForm = reactive({
  editingStaffId: '',
  staffId: '',
  displayName: '',
  email: '',
  status: 'active',
});
const assignmentForm = reactive({
  staffId: '',
  role: 'WORKSPACE_VIEWER',
  scopeType: 'workspace',
  scopeId: 'ws-default-001',
});

const columns = [
  { key: 'staffId', label: 'Staff ID' },
  { key: 'userDisplayName', label: 'User', width: '25%' },
  { key: 'role', label: 'Role' },
  { key: 'scopeType', label: 'Scope' },
  { key: 'grantedBy', label: 'Granted By' },
  { key: 'grantedAt', label: 'Granted At' },
  { key: 'actions', label: '' },
];

onMounted(() => { store.fetchAssignments(); });

function editUser(user: { staffId: string; displayName: string; email: string | null; status: string }) {
  userForm.editingStaffId = user.staffId;
  userForm.staffId = user.staffId;
  userForm.displayName = user.displayName;
  userForm.email = user.email ?? '';
  userForm.status = user.status;
}

function resetUserForm() {
  userForm.editingStaffId = '';
  userForm.staffId = '';
  userForm.displayName = '';
  userForm.email = '';
  userForm.status = 'active';
}

async function saveUser() {
  if (!userForm.staffId || !userForm.displayName) return;
  busy.value = true;
  try {
    const payload = {
      staffId: userForm.staffId,
      displayName: userForm.displayName,
      email: userForm.email || null,
      profileSource: 'manual',
      status: userForm.status,
    };
    if (userForm.editingStaffId) {
      await store.updateUser(userForm.editingStaffId, payload);
    } else {
      await store.createUser(payload);
    }
    assignmentForm.staffId = userForm.staffId;
    resetUserForm();
  } finally {
    busy.value = false;
  }
}

async function deactivateUser(user: { staffId: string; displayName: string; email: string | null; status: string }) {
  busy.value = true;
  try {
    await store.updateUser(user.staffId, {
      staffId: user.staffId,
      displayName: user.displayName,
      email: user.email,
      profileSource: 'manual',
      status: 'inactive',
    });
  } finally {
    busy.value = false;
  }
}

function hasPlatformAdminGrant(staffId: string) {
  return store.items.some(item => item.staffId === staffId && item.role === 'PLATFORM_ADMIN');
}

async function assignRole() {
  if (!assignmentForm.staffId || !assignmentForm.scopeId) return;
  busy.value = true;
  try {
    await store.assignRole({ ...assignmentForm });
  } finally {
    busy.value = false;
  }
}

async function revokeRole(id: string) {
  busy.value = true;
  try {
    await store.revokeRole(id);
  } finally {
    busy.value = false;
  }
}
</script>

<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 16px;">Access Control</h2>

    <section v-if="store.users.length" class="user-strip" aria-label="Staff users">
      <article v-for="user in store.users" :key="user.staffId" class="user-card">
        <img v-if="user.avatarUrl" :src="user.avatarUrl" alt="" />
        <span v-else class="avatar-fallback">{{ user.displayName.slice(0, 1) }}</span>
        <div>
          <strong>{{ user.staffName ?? user.displayName }}</strong>
          <small>{{ user.staffId }} · {{ user.profileSource }} · {{ user.status }}</small>
        </div>
        <div class="user-card-actions">
          <button class="link-button" type="button" :disabled="busy" @click="editUser(user)">Edit</button>
          <button
            class="link-button"
            type="button"
            :disabled="busy || user.status === 'inactive' || (hasPlatformAdminGrant(user.staffId) && store.platformAdminCount <= 1)"
            @click="deactivateUser(user)"
          >
            Deactivate
          </button>
        </div>
      </article>
    </section>

    <section class="access-actions" aria-label="Access actions">
      <p v-if="store.error" class="error-banner">{{ store.error }}</p>

      <form class="action-form" @submit.prevent="saveUser">
        <input v-model="userForm.staffId" placeholder="Staff ID" :disabled="Boolean(userForm.editingStaffId)" />
        <input v-model="userForm.displayName" placeholder="Display name" />
        <input v-model="userForm.email" placeholder="Email" />
        <select v-model="userForm.status">
          <option value="active">active</option>
          <option value="inactive">inactive</option>
        </select>
        <button class="btn-machined" type="submit" :disabled="busy">{{ userForm.editingStaffId ? 'Save user' : 'Create user' }}</button>
      </form>

      <form class="action-form" @submit.prevent="assignRole">
        <select v-model="assignmentForm.staffId">
          <option value="" disabled>Staff ID</option>
          <option v-for="user in store.users" :key="user.staffId" :value="user.staffId">
            {{ user.staffId }} · {{ user.displayName }}
          </option>
        </select>
        <select v-model="assignmentForm.role">
          <option value="PLATFORM_ADMIN">PLATFORM_ADMIN</option>
          <option value="WORKSPACE_ADMIN">WORKSPACE_ADMIN</option>
          <option value="WORKSPACE_MEMBER">WORKSPACE_MEMBER</option>
          <option value="WORKSPACE_VIEWER">WORKSPACE_VIEWER</option>
          <option value="AUDITOR">AUDITOR</option>
        </select>
        <select v-model="assignmentForm.scopeType">
          <option value="platform">platform</option>
          <option value="application">application</option>
          <option value="snow_group">snow_group</option>
          <option value="workspace">workspace</option>
          <option value="project">project</option>
        </select>
        <input v-model="assignmentForm.scopeId" placeholder="Scope ID" />
        <button class="btn-machined" type="submit" :disabled="busy">Assign role</button>
      </form>
    </section>

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
      <template #cell-actions="{ row }">
        <button
          class="link-button"
          type="button"
          :disabled="busy || (String(row.role) === 'PLATFORM_ADMIN' && store.platformAdminCount <= 1)"
          @click.stop="revokeRole(String(row.id))"
        >
          Revoke
        </button>
      </template>
    </CatalogTable>
  </div>
</template>

<style scoped>
.user-strip {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.user-card {
  min-height: 58px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: var(--border-subtle);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
}

.user-card > div:nth-of-type(1) {
  min-width: 0;
  flex: 1;
}

.user-card-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.user-card img,
.avatar-fallback {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--color-secondary-container);
  color: var(--color-on-secondary-container);
  font-weight: 800;
}

.user-card strong,
.user-card small {
  display: block;
}

.user-card small {
  margin-top: 3px;
  color: var(--color-on-surface-variant);
  font-size: 11px;
}

.access-actions {
  display: grid;
  gap: 10px;
  margin-bottom: 16px;
}

.error-banner {
  margin: 0;
  padding: 8px 10px;
  border: 1px solid var(--color-error);
  border-radius: var(--radius-xs);
  color: var(--color-error);
  background: var(--color-error-container);
}

.action-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr)) auto;
  gap: 8px;
  align-items: center;
}

.action-form input,
.action-form select {
  min-width: 0;
  min-height: 34px;
  padding: 6px 8px;
  border: var(--border-subtle);
  border-radius: var(--radius-xs);
  background: var(--color-surface-container-low);
  color: var(--color-on-surface);
}

.link-button {
  border: 0;
  background: transparent;
  color: var(--color-primary);
  cursor: pointer;
  font-weight: 700;
}

.link-button:disabled {
  color: var(--color-on-surface-variant);
  cursor: not-allowed;
}

@media (max-width: 860px) {
  .action-form {
    grid-template-columns: 1fr;
  }
}
</style>
