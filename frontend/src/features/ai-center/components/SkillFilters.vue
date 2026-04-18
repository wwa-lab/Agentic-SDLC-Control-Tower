<script setup lang="ts">
import { Search } from 'lucide-vue-next';
import { AUTONOMY_TOOLTIPS, SKILL_CATEGORY_LABELS, SKILL_STATUS_LABELS } from '../constants';
import type { AutonomyLevel, SkillFilterState, SkillStatus, SkillCategory } from '../types';

interface Props {
  filters: SkillFilterState;
  owners: string[];
}

defineProps<Props>();

defineEmits<{
  search: [value: string];
  sort: [value: SkillFilterState['sortBy']];
  toggleCategory: [value: SkillCategory];
  toggleStatus: [value: SkillStatus];
  toggleAutonomy: [value: AutonomyLevel];
  toggleOwner: [value: string];
  reset: [];
}>();

const autonomyLevels: AutonomyLevel[] = ['L0-Manual', 'L1-Assist', 'L2-Auto-with-approval', 'L3-Auto'];
const categories: SkillCategory[] = ['delivery', 'runtime'];
const statuses: SkillStatus[] = ['active', 'beta', 'deprecated'];
</script>

<template>
  <div class="skill-filters">
    <label class="skill-filters__search">
      <Search :size="14" />
      <input
        :value="filters.search"
        type="search"
        placeholder="Search by skill key or name"
        @input="$emit('search', ($event.target as HTMLInputElement).value)"
      />
    </label>

    <select class="skill-filters__sort" :value="filters.sortBy" @change="$emit('sort', ($event.target as HTMLSelectElement).value as SkillFilterState['sortBy'])">
      <option value="lastExecutedAt">Last Executed</option>
      <option value="name">Name A-Z</option>
      <option value="successRate30d">Success Rate</option>
    </select>

    <div class="skill-filters__groups">
      <div class="skill-filters__group">
        <span class="text-label">Category</span>
        <button
          v-for="value in categories"
          :key="value"
          class="skill-chip"
          type="button"
          :aria-pressed="filters.category.includes(value)"
          @click="$emit('toggleCategory', value)"
        >
          {{ SKILL_CATEGORY_LABELS[value] }}
        </button>
      </div>

      <div class="skill-filters__group">
        <span class="text-label">Status</span>
        <button
          v-for="value in statuses"
          :key="value"
          class="skill-chip"
          type="button"
          role="status"
          :aria-label="`Filter skills by ${SKILL_STATUS_LABELS[value]}`"
          :aria-pressed="filters.status.includes(value)"
          @click="$emit('toggleStatus', value)"
        >
          {{ SKILL_STATUS_LABELS[value] }}
        </button>
      </div>

      <div class="skill-filters__group">
        <span class="text-label">Autonomy</span>
        <button
          v-for="value in autonomyLevels"
          :key="value"
          class="skill-chip"
          type="button"
          role="status"
          :title="AUTONOMY_TOOLTIPS[value]"
          :aria-label="AUTONOMY_TOOLTIPS[value]"
          :aria-pressed="filters.autonomy.includes(value)"
          @click="$emit('toggleAutonomy', value)"
        >
          {{ value }}
        </button>
      </div>

      <div v-if="owners.length" class="skill-filters__group">
        <span class="text-label">Owner</span>
        <button
          v-for="owner in owners"
          :key="owner"
          class="skill-chip"
          type="button"
          :aria-pressed="filters.owner.includes(owner)"
          @click="$emit('toggleOwner', owner)"
        >
          {{ owner }}
        </button>
      </div>
    </div>

    <button class="skill-filters__reset" type="button" @click="$emit('reset')">Reset filters</button>
  </div>
</template>

<style scoped>
.skill-filters {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skill-filters__search {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  border: var(--border-ghost);
  background: rgba(255, 255, 255, 0.03);
}

.skill-filters__search input,
.skill-filters__sort {
  background: transparent;
  color: var(--color-on-surface);
  border: none;
  width: 100%;
}

.skill-filters__sort {
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 10px 12px;
}

.skill-filters__groups {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skill-filters__group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.skill-chip {
  border: var(--border-ghost);
  border-radius: 999px;
  padding: 6px 10px;
  background: rgba(255, 255, 255, 0.02);
  color: var(--color-on-surface);
  cursor: pointer;
  transition: transform 0.12s ease, opacity 0.12s ease, background 0.12s ease;
}

.skill-chip[aria-pressed="true"] {
  background: rgba(137, 206, 255, 0.16);
  box-shadow: inset 0 0 0 1px rgba(137, 206, 255, 0.32);
}

.skill-chip:hover {
  transform: translateY(-1px);
}

.skill-filters__reset {
  align-self: flex-start;
  border: none;
  background: transparent;
  color: var(--color-secondary);
  cursor: pointer;
}
</style>
