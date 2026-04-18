<script setup lang="ts">
import { Bot, Cog, User } from 'lucide-vue-next';
import type { Run } from '../types';
import { formatDuration, formatRelativeTime, statusColor, statusLabel } from '../utils';

interface Props {
  run: Run;
  selected: boolean;
}

const props = defineProps<Props>();

defineEmits<{
  select: [executionId: string];
}>();

function triggerIcon() {
  if (props.run.triggeredByType === 'ai') return Bot;
  if (props.run.triggeredByType === 'human') return User;
  return Cog;
}
</script>

<template>
  <tr :class="{ 'run-row--selected': selected }">
    <td>
      <button class="run-row__button" type="button" @click="$emit('select', run.id)">
        <strong>{{ run.skillName }}</strong>
        <p class="text-body-sm">{{ run.outcomeSummary }}</p>
      </button>
    </td>
    <td>{{ formatRelativeTime(run.startedAt) }}</td>
    <td>
      <span class="run-chip-inline" role="status" :style="{ '--chip-color': statusColor(run.status) }">
        {{ statusLabel(run.status) }}
      </span>
    </td>
    <td>
      <span class="run-row__trigger">
        <component :is="triggerIcon()" :size="14" />
        {{ run.triggeredBy }}
      </span>
    </td>
    <td>{{ run.triggerSourcePage ?? 'Scheduled / manual' }}</td>
    <td>{{ formatDuration(run.durationMs) }}</td>
  </tr>
</template>

<style scoped>
tr {
  border-top: 1px solid var(--border-separator);
}

.run-row__button {
  width: 100%;
  padding: 10px 0;
  border: none;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.run-row__trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.run-chip-inline {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 4px 8px;
  border: 1px solid color-mix(in srgb, var(--chip-color, var(--color-secondary)) 40%, transparent);
  background: color-mix(in srgb, var(--chip-color, var(--color-secondary)) 10%, transparent);
  color: var(--chip-color, var(--color-secondary));
  font-size: 0.75rem;
}

.run-row--selected {
  background: rgba(137, 206, 255, 0.07);
}
</style>
