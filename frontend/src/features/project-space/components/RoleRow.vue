<script setup lang="ts">
import type { RoleAssignment } from '../types/leadership';

interface Props {
  assignment: RoleAssignment;
}

defineProps<Props>();

function roleLabel(role: RoleAssignment['role']): string {
  return role.replaceAll('_', ' ');
}
</script>

<template>
  <div class="role-row">
    <div>
      <p class="text-label">{{ roleLabel(assignment.role) }}</p>
      <strong v-if="assignment.displayName">{{ assignment.displayName }}</strong>
      <span v-else class="status-chip status-chip--muted">Not assigned</span>
    </div>

    <div class="role-row__meta">
      <span class="status-chip" :class="`status-chip--${assignment.oncallStatus.toLowerCase()}`">
        {{ assignment.oncallStatus }}
      </span>
      <span
        class="status-chip"
        :class="assignment.backupPresent ? 'status-chip--healthy' : 'status-chip--muted'"
      >
        {{ assignment.backupPresent ? `Backup: ${assignment.backupDisplayName}` : 'No backup' }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.role-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-separator);
}

.role-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.role-row:first-child {
  padding-top: 0;
}

.role-row__meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 0.625rem;
  letter-spacing: 0.08em;
  font-family: var(--font-tech);
}

.status-chip--on,
.status-chip--healthy {
  background: rgba(78, 222, 163, 0.12);
  color: var(--color-health-emerald);
}

.status-chip--off,
.status-chip--upcoming {
  background: rgba(245, 158, 11, 0.12);
  color: var(--color-approval-amber);
}

.status-chip--none,
.status-chip--muted {
  background: rgba(148, 163, 184, 0.12);
  color: var(--color-on-surface-variant);
}

@media (max-width: 1200px) {
  .role-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .role-row__meta {
    justify-content: flex-start;
  }
}
</style>
