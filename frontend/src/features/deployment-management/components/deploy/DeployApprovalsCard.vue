<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { ApprovalEvent } from '../../types/deploy';
import type { ApprovalDecision } from '../../types/enums';

defineProps<{ section: SectionResult<ReadonlyArray<ApprovalEvent>> }>();

const DECISION_CONFIG: Record<ApprovalDecision, { label: string; color: string }> = {
  APPROVED: { label: 'Approved', color: 'var(--color-health-emerald)' },
  REJECTED: { label: 'Rejected', color: 'var(--color-incident-crimson)' },
  TIMED_OUT: { label: 'Timed Out', color: 'var(--color-approval-amber)' },
};

function formatTimestamp(iso: string): string {
  return new Date(iso).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  });
}
</script>

<template>
  <div class="deploy-approvals-card card">
    <div class="card-title">Approvals</div>
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading approvals...</div>
    <template v-else>
      <div v-if="section.data.length === 0" class="card-empty">No approval events.</div>
      <ul v-else class="approval-list">
        <li
          v-for="event in section.data"
          :key="event.approvalId"
          class="approval-row"
        >
          <div class="approval-header">
            <span class="stage-name">{{ event.stageName }}</span>
            <span
              class="decision-badge"
              :style="{ '--decision-color': DECISION_CONFIG[event.decision].color }"
            >
              {{ DECISION_CONFIG[event.decision].label }}
            </span>
          </div>
          <div class="approval-meta">
            <span class="approver">{{ event.approverDisplayName ?? '(redacted)' }}</span>
            <span class="separator">&middot;</span>
            <span class="timestamp">{{ formatTimestamp(event.decidedAt) }}</span>
          </div>
          <p v-if="event.rationale" class="rationale">{{ event.rationale }}</p>
          <p v-else class="rationale rationale--redacted">(redacted)</p>
        </li>
      </ul>
    </template>
  </div>
</template>

<style scoped>
.deploy-approvals-card { padding: 16px; }
.card {
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}
.card-title {
  margin-bottom: 12px;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-on-surface);
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.card-error { color: var(--color-incident-crimson); font-size: 0.85rem; }
.card-skeleton { color: var(--color-on-surface-variant); font-size: 0.85rem; }
.card-empty { color: var(--color-on-surface-variant); font-size: 0.85rem; }

.approval-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.approval-row {
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
}

.approval-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.stage-name {
  font-family: var(--font-ui);
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-on-surface);
}
.decision-badge {
  display: inline-block;
  padding: 1px 6px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--decision-color);
  color: var(--decision-color);
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 600;
  text-transform: uppercase;
}

.approval-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
}
.separator { opacity: 0.5; }
.timestamp { font-family: var(--font-tech); font-size: 0.7rem; }

.rationale {
  margin: 6px 0 0;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  line-height: 1.4;
  color: var(--color-on-surface);
}
.rationale--redacted {
  color: var(--color-on-surface-variant);
  font-style: italic;
}
</style>
