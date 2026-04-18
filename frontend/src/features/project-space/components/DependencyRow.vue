<script setup lang="ts">
import { ArrowUpRight, GitFork } from 'lucide-vue-next';
import type { Dependency } from '../types/dependencies';

interface Props {
  dependency: Dependency;
}

defineProps<Props>();

defineEmits<{
  open: [url: string];
}>();
</script>

<template>
  <div class="dependency-row" :class="{ 'dependency-row--blocked': dependency.blockerReason }">
    <div class="dependency-row__header">
      <div>
        <p class="text-label">{{ dependency.relationship }} / {{ dependency.ownerTeam }}</p>
        <strong>{{ dependency.targetName }}</strong>
      </div>
      <div class="dependency-row__chips">
        <span v-if="dependency.external" class="chip chip--muted">External</span>
        <span class="chip" :class="`chip--${dependency.health.toLowerCase()}`">{{ dependency.health }}</span>
      </div>
    </div>

    <p class="text-body-sm">{{ dependency.blockerReason ?? dependency.targetRef }}</p>

    <button
      v-if="dependency.primaryAction"
      class="dependency-row__action"
      @click="$emit('open', dependency.primaryAction.url)"
    >
      <GitFork :size="14" />
      <span>{{ dependency.primaryAction.label }}</span>
      <ArrowUpRight :size="14" />
    </button>
  </div>
</template>

<style scoped>
.dependency-row {
  padding: 12px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(255, 255, 255, 0.03);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dependency-row--blocked {
  border-color: rgba(255, 180, 171, 0.2);
  background: rgba(255, 180, 171, 0.06);
}

.dependency-row__header,
.dependency-row__chips,
.dependency-row__action {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.dependency-row__chips {
  justify-content: flex-end;
}

.chip {
  border-radius: 999px;
  padding: 4px 8px;
  font-size: 0.625rem;
  font-family: var(--font-tech);
}

.chip--green { background: rgba(78, 222, 163, 0.12); color: var(--color-health-emerald); }
.chip--yellow { background: rgba(245, 158, 11, 0.12); color: var(--color-approval-amber); }
.chip--red { background: rgba(255, 180, 171, 0.12); color: var(--color-incident-crimson); }
.chip--unknown,
.chip--muted { background: rgba(148, 163, 184, 0.12); color: var(--color-on-surface-variant); }

.dependency-row__action {
  width: fit-content;
  border: none;
  background: transparent;
  color: var(--color-secondary);
  cursor: pointer;
  padding: 0;
}
</style>
