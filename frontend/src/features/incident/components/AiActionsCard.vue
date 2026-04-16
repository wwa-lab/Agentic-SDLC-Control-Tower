<script setup lang="ts">
import { ref } from 'vue';
import type { IncidentActions, SectionResult } from '../types/incident';
import IncidentCard from './IncidentCard.vue';

interface Props {
  actions: SectionResult<IncidentActions>;
  isLoading?: boolean;
}

defineProps<Props>();
const emit = defineEmits<{
  approve: [actionId: string];
  reject: [payload: { actionId: string; reason: string }];
}>();

const rejectingActionId = ref<string | null>(null);
const rejectReason = ref('');

function startReject(actionId: string) {
  rejectingActionId.value = actionId;
  rejectReason.value = '';
}

function cancelReject() {
  rejectingActionId.value = null;
  rejectReason.value = '';
}

function submitReject(actionId: string) {
  if (!rejectReason.value.trim()) return;
  emit('reject', { actionId, reason: rejectReason.value.trim() });
  rejectingActionId.value = null;
  rejectReason.value = '';
}

const STATUS_CLASS: Record<string, string> = {
  pending: 'action-status--pending',
  approved: 'action-status--approved',
  rejected: 'action-status--rejected',
  executing: 'action-status--executing',
  executed: 'action-status--executed',
  rolled_back: 'action-status--rolled-back',
};
</script>

<template>
  <IncidentCard title="AI Actions" :is-loading="isLoading" :error="actions.error">
    <div v-if="actions.data" class="actions-content">
      <div v-for="action in actions.data.actions" :key="action.id" class="action-item">
        <div class="action-header">
          <span class="action-id">{{ action.id }}</span>
          <span class="action-status" :class="STATUS_CLASS[action.executionStatus]">
            {{ action.executionStatus.replace(/_/g, ' ') }}
          </span>
        </div>
        <p class="action-desc">{{ action.description }}</p>
        <div class="action-meta">
          <span class="meta-text">{{ action.impactAssessment }}</span>
          <span v-if="action.isRollbackable" class="rollback-indicator">↩ Rollbackable</span>
        </div>
        <div v-if="action.policyRef" class="policy-ref">
          {{ action.policyRef }}
        </div>

        <!-- Approve/Reject controls for pending actions -->
        <div v-if="action.executionStatus === 'pending'" class="action-controls">
          <template v-if="rejectingActionId === action.id">
            <input
              v-model="rejectReason"
              class="reject-input"
              placeholder="Reason for rejection..."
              @keyup.enter="submitReject(action.id)"
              @keyup.escape="cancelReject"
            />
            <div class="reject-btns">
              <button class="btn-cancel" @click="cancelReject">Cancel</button>
              <button class="btn-reject-confirm" :disabled="!rejectReason.trim()" @click="submitReject(action.id)">Reject</button>
            </div>
          </template>
          <template v-else>
            <button class="btn-machined btn-reject" @click="startReject(action.id)">Reject</button>
            <button class="btn-machined btn-approve" @click="emit('approve', action.id)">Approve</button>
          </template>
        </div>
      </div>

      <div v-if="actions.data.actions.length === 0" class="empty-state">
        No actions proposed
      </div>
    </div>
  </IncidentCard>
</template>

<style scoped>
.actions-content { display: flex; flex-direction: column; gap: 12px; }

.action-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: var(--color-surface-container-low);
  border-radius: var(--radius-sm);
}

.action-header { display: flex; justify-content: space-between; align-items: center; }

.action-id {
  font-family: var(--font-tech);
  font-size: 0.6875rem;
  color: var(--color-secondary);
}

.action-status {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 2px 6px;
  border-radius: 2px;
}

.action-status--pending { color: var(--color-approval-amber); background: rgba(245, 158, 11, 0.1); }
.action-status--approved { color: var(--color-health-emerald); background: rgba(78, 222, 163, 0.1); }
.action-status--rejected { color: var(--color-incident-crimson); background: var(--color-incident-tint); }
.action-status--executing { color: var(--color-secondary); background: var(--color-secondary-tint); }
.action-status--executed { color: var(--color-health-emerald); background: rgba(78, 222, 163, 0.1); }
.action-status--rolled-back { color: var(--color-incident-crimson); background: var(--color-incident-tint); }

.action-desc {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface);
  margin: 0;
}

.action-meta { display: flex; justify-content: space-between; align-items: center; gap: 8px; }

.meta-text {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface-variant);
}

.rollback-indicator {
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  color: var(--color-secondary);
}

.policy-ref {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
  opacity: 0.7;
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 2px;
}

.action-controls { display: flex; gap: 8px; align-items: center; margin-top: 4px; }

.btn-machined {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 6px 16px;
  border-radius: 2px;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-reject {
  background: var(--color-surface-container-highest);
  color: var(--color-on-surface-variant);
}
.btn-reject:hover { color: var(--color-incident-crimson); }

.btn-approve {
  background: var(--color-secondary);
  color: var(--color-on-secondary-container);
  box-shadow: 0 0 8px var(--color-secondary-tint);
}
.btn-approve:hover { box-shadow: 0 0 12px var(--color-secondary-glow); }

.reject-input {
  flex: 1;
  background: var(--color-surface-container-highest);
  border: 1px solid var(--color-outline-variant);
  border-radius: 2px;
  padding: 6px 8px;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface);
}

.reject-input::placeholder { color: var(--color-on-surface-variant); opacity: 0.5; }

.reject-btns { display: flex; gap: 4px; }

.btn-cancel {
  font-family: var(--font-ui); font-size: 0.5625rem; padding: 4px 8px;
  background: transparent; border: none; color: var(--color-on-surface-variant);
  cursor: pointer; text-transform: uppercase;
}

.btn-reject-confirm {
  font-family: var(--font-ui); font-size: 0.5625rem; padding: 4px 8px;
  background: var(--color-incident-crimson); border: none; color: #fff;
  cursor: pointer; border-radius: 2px; text-transform: uppercase;
}
.btn-reject-confirm:disabled { opacity: 0.4; cursor: not-allowed; }

.empty-state {
  font-family: var(--font-ui); font-size: 0.75rem;
  color: var(--color-on-surface-variant); text-align: center; padding: 16px;
}
</style>
