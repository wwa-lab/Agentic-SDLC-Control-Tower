<script setup lang="ts">
import { ArrowUpRight, ServerCog } from 'lucide-vue-next';
import type { Environment } from '../types/environments';
import DriftIndicator from './DriftIndicator.vue';

interface Props {
  environment: Environment;
}

defineProps<Props>();

defineEmits<{
  open: [url: string];
}>();

function formatTimestamp(value: string): string {
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}
</script>

<template>
  <div class="environment-tile">
    <div class="environment-tile__header">
      <div>
        <p class="text-label">{{ environment.kind }}</p>
        <strong>{{ environment.label }}</strong>
      </div>
      <span class="gate-chip" :class="`gate-chip--${environment.gateStatus.toLowerCase()}`">
        {{ environment.gateStatus }}
      </span>
    </div>

    <div class="environment-tile__version text-tech">
      <span>{{ environment.versionRef }}</span>
      <span>{{ environment.buildId }}</span>
    </div>

    <div class="environment-tile__meta text-body-sm">
      <span>Last deployed {{ formatTimestamp(environment.lastDeployedAt) }}</span>
      <span>{{ environment.approver?.displayName ?? `${environment.health} health` }}</span>
    </div>

    <div class="environment-tile__footer">
      <DriftIndicator v-if="environment.drift" :drift="environment.drift" />
      <button
        class="environment-tile__action"
        :disabled="!environment.deploymentLink.enabled"
        @click="$emit('open', environment.deploymentLink.url)"
      >
        <ServerCog :size="14" />
        <span>Deployment</span>
        <ArrowUpRight :size="14" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.environment-tile {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(255, 255, 255, 0.03);
}

.environment-tile__header,
.environment-tile__version,
.environment-tile__meta,
.environment-tile__footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.gate-chip {
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 0.625rem;
  font-family: var(--font-tech);
}

.gate-chip--auto { background: rgba(78, 222, 163, 0.12); color: var(--color-health-emerald); }
.gate-chip--approval_required { background: rgba(245, 158, 11, 0.12); color: var(--color-approval-amber); }
.gate-chip--blocked { background: rgba(255, 180, 171, 0.12); color: var(--color-incident-crimson); }

.environment-tile__action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  color: var(--color-secondary);
  cursor: pointer;
}

.environment-tile__action:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

@media (max-width: 1200px) {
  .environment-tile__header,
  .environment-tile__version,
  .environment-tile__meta,
  .environment-tile__footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
