<script setup lang="ts">
import { AUTONOMY_TOOLTIPS } from '../constants';
import type { Skill } from '../types';
import { formatPercent, formatRelativeTime, statusColor, statusLabel } from '../utils';

interface Props {
  skill: Skill;
  selected: boolean;
}

defineProps<Props>();

defineEmits<{
  select: [skillKey: string];
}>();
</script>

<template>
  <tr :class="{ 'skill-row--selected': selected }">
    <td>
      <button class="skill-row__button" type="button" @click="$emit('select', skill.key)">
        <span class="skill-row__key text-tech">{{ skill.key }}</span>
        <strong>{{ skill.name }}</strong>
      </button>
    </td>
    <td>{{ skill.category }}</td>
    <td>
      <span class="skill-chip-inline" role="status" :style="{ '--chip-color': statusColor(skill.status) }">
        {{ statusLabel(skill.status) }}
      </span>
    </td>
    <td>
      <span class="skill-chip-inline" role="status" :title="AUTONOMY_TOOLTIPS[skill.defaultAutonomy]">
        {{ skill.defaultAutonomy }}
      </span>
    </td>
    <td>{{ skill.owner }}</td>
    <td>{{ formatRelativeTime(skill.lastExecutedAt) }}</td>
    <td>{{ formatPercent(skill.successRate30d) }}</td>
  </tr>
</template>

<style scoped>
tr {
  border-top: 1px solid var(--border-separator);
}

.skill-row__button {
  border: none;
  background: transparent;
  color: inherit;
  width: 100%;
  padding: 10px 0;
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 4px;
  cursor: pointer;
}

.skill-row__key {
  color: var(--color-secondary);
  font-size: 0.78rem;
}

.skill-row--selected {
  background: rgba(137, 206, 255, 0.07);
}

.skill-chip-inline {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 4px 8px;
  border: 1px solid color-mix(in srgb, var(--chip-color, var(--color-secondary)) 40%, transparent);
  background: color-mix(in srgb, var(--chip-color, var(--color-secondary)) 10%, transparent);
  color: var(--chip-color, var(--color-secondary));
  font-size: 0.75rem;
}
</style>
