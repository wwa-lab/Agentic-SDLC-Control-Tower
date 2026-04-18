<script setup lang="ts">
import { computed } from 'vue';
import type { RunStatus } from '../types';

interface Props {
  status: RunStatus;
}

const props = defineProps<Props>();

const statusClass = computed(() => `build-status--${props.status.toLowerCase()}`);
</script>

<template>
  <span class="build-status" :class="statusClass">
    <span class="build-status__dot"></span>
    <span>{{ status }}</span>
  </span>
</template>

<style scoped>
.build-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 9px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  border: 1px solid transparent;
}

.build-status__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.build-status--success {
  color: var(--cb-status-success);
  background: rgba(78, 222, 163, 0.12);
  border-color: rgba(78, 222, 163, 0.25);
}

.build-status--success .build-status__dot {
  background: var(--cb-status-success);
}

.build-status--failure {
  color: var(--cb-status-failure);
  background: rgba(255, 180, 171, 0.12);
  border-color: rgba(255, 180, 171, 0.25);
}

.build-status--failure .build-status__dot {
  background: var(--cb-status-failure);
}

.build-status--running,
.build-status--queued {
  color: var(--cb-status-running);
  background: rgba(245, 158, 11, 0.12);
  border-color: rgba(245, 158, 11, 0.25);
}

.build-status--running .build-status__dot,
.build-status--queued .build-status__dot {
  background: var(--cb-status-running);
  animation: pulse 1.4s infinite ease-in-out;
}

.build-status--cancelled {
  color: var(--cb-status-cancelled);
  background: rgba(148, 163, 184, 0.14);
  border-color: rgba(148, 163, 184, 0.25);
}

.build-status--cancelled .build-status__dot {
  background: var(--cb-status-cancelled);
}

.build-status--neutral {
  color: var(--cb-status-neutral);
  background: rgba(148, 163, 184, 0.12);
  border-color: rgba(148, 163, 184, 0.2);
}

.build-status--neutral .build-status__dot {
  background: var(--cb-status-neutral);
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.18); opacity: 1; }
}
</style>

