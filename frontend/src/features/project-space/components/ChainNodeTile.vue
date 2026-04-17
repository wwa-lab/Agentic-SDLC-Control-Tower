<script setup lang="ts">
import { ArrowUpRight } from 'lucide-vue-next';
import type { ChainNodeHealth } from '../types/chain';

interface Props {
  node: ChainNodeHealth;
}

defineProps<Props>();

defineEmits<{
  navigate: [url: string];
}>();
</script>

<template>
  <button
    class="chain-node"
    :class="{
      'chain-node--execution-hub': node.isExecutionHub,
      'chain-node--disabled': !node.enabled,
      [`chain-node--${node.health.toLowerCase()}`]: true,
    }"
    :disabled="!node.enabled"
    :title="node.enabled ? `Open ${node.label}` : `${node.label} coming soon`"
    :aria-label="node.enabled ? `Open ${node.label}` : `${node.label} coming soon`"
    @click="$emit('navigate', node.deepLink)"
  >
    <span class="text-label">{{ node.label }}</span>
    <strong class="text-tech">{{ node.count ?? '--' }}</strong>
    <span class="chain-node__footer">
      <span>{{ node.health }}</span>
      <ArrowUpRight :size="14" />
    </span>
  </button>
</template>

<style scoped>
.chain-node {
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.03);
  color: inherit;
  border-radius: var(--radius-sm);
  padding: 14px 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 108px;
  cursor: pointer;
  text-align: left;
}

.chain-node--execution-hub {
  transform: scale(1.02);
  border-color: rgba(137, 206, 255, 0.28);
  box-shadow: 0 0 0 1px rgba(137, 206, 255, 0.12), 0 6px 18px rgba(137, 206, 255, 0.12);
}

.chain-node--disabled {
  opacity: 0.48;
  cursor: not-allowed;
}

.chain-node--green strong { color: var(--color-health-emerald); }
.chain-node--yellow strong { color: var(--color-approval-amber); }
.chain-node--red strong { color: var(--color-incident-crimson); }
.chain-node--unknown strong { color: var(--color-on-surface-variant); }

.chain-node__footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
}
</style>
