<script setup lang="ts">
import { onMounted } from 'vue';
import { useAccessStore } from './store';
import CatalogTable from '../shared/CatalogTable.vue';
import ScopeChip from '../shared/ScopeChip.vue';
import StatusBadge from '../shared/StatusBadge.vue';

const store = useAccessStore();

const columns = [
  { key: 'staffId', label: 'Staff ID' },
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

    <section v-if="store.users.length" class="user-strip" aria-label="Staff users">
      <article v-for="user in store.users" :key="user.staffId" class="user-card">
        <img v-if="user.avatarUrl" :src="user.avatarUrl" alt="" />
        <span v-else class="avatar-fallback">{{ user.displayName.slice(0, 1) }}</span>
        <div>
          <strong>{{ user.staffName ?? user.displayName }}</strong>
          <small>{{ user.staffId }} · {{ user.profileSource }} · {{ user.status }}</small>
        </div>
      </article>
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
</style>
