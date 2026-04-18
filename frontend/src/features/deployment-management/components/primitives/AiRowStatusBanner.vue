<script setup lang="ts">
import type { AiRowStatus } from '../../types/enums';

defineProps<{
  status: AiRowStatus;
  errorMessage?: string;
  onRetry?: () => void;
}>();

const CONFIG: Record<AiRowStatus, { label: string; variant: string }> = {
  PENDING: { label: 'AI generation in progress...', variant: 'info' },
  SUCCESS: { label: '', variant: '' },
  FAILED: { label: 'AI generation failed', variant: 'error' },
  STALE: { label: 'AI content is stale — newer data available', variant: 'warning' },
  SUPERSEDED: { label: 'Superseded by a newer generation', variant: 'muted' },
  EVIDENCE_MISMATCH: { label: 'Evidence integrity check failed — content locked', variant: 'error' },
  SKIPPED: { label: 'AI generation skipped', variant: 'muted' },
};
</script>

<template>
  <div v-if="status !== 'SUCCESS'" class="ai-banner" :class="CONFIG[status].variant">
    <span class="message">{{ CONFIG[status].label }}</span>
    <span v-if="errorMessage" class="detail">{{ errorMessage }}</span>
    <button v-if="status === 'FAILED' && onRetry" class="retry-btn" @click="onRetry">Retry</button>
  </div>
</template>

<style scoped>
.ai-banner {
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.info { background: var(--color-secondary-tint); color: var(--color-secondary); }
.error { background: var(--color-incident-tint); color: var(--color-incident-crimson); }
.warning { background: rgba(245, 158, 11, 0.1); color: var(--color-approval-amber); }
.muted { background: var(--color-surface-container-high); color: var(--color-on-surface-variant); }
.detail { opacity: 0.8; font-size: 0.75rem; }
.retry-btn {
  margin-left: auto;
  padding: 2px 10px;
  border: 1px solid currentColor;
  border-radius: var(--radius-sm);
  background: transparent;
  color: inherit;
  cursor: pointer;
  font-size: 0.75rem;
  font-weight: 600;
}
.retry-btn:hover { background: rgba(255, 255, 255, 0.05); }
</style>
