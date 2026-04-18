<script setup lang="ts">
import { computed, toRef } from 'vue';
import { Library } from 'lucide-vue-next';
import SkillFilters from './SkillFilters.vue';
import SkillRow from './SkillRow.vue';
import { useSkillFilters } from '../composables/useSkillFilters';
import type { Skill, SkillFilterState } from '../types';

interface Props {
  skills: Skill[];
  loading: boolean;
  error: string | null;
  filters: SkillFilterState;
  selectedSkillKey?: string | null;
}

const props = withDefaults(defineProps<Props>(), {
  selectedSkillKey: null,
});

const emit = defineEmits<{
  retry: [];
  select: [skillKey: string];
  setFilters: [patch: Partial<SkillFilterState>];
}>();

const owners = computed(() => Array.from(new Set(props.skills.map(skill => skill.owner))).sort());
const skillRef = toRef(props, 'skills');
const filterRef = toRef(props, 'filters');

const {
  filteredSkills,
  hasActiveFilters,
  resetFilters,
  setSearch,
  setSortBy,
  toggleArrayFilter,
} = useSkillFilters(skillRef, filterRef, patch => emit('setFilters', patch));
</script>

<template>
  <section class="catalog-card section-high">
    <header class="catalog-card__header">
      <div>
        <p class="text-label">Catalog</p>
        <h3>Skill Catalog</h3>
      </div>
      <Library :size="18" />
    </header>

    <SkillFilters
      :filters="filters"
      :owners="owners"
      @search="setSearch"
      @sort="setSortBy"
      @toggle-category="toggleArrayFilter('category', $event)"
      @toggle-status="toggleArrayFilter('status', $event)"
      @toggle-autonomy="toggleArrayFilter('autonomy', $event)"
      @toggle-owner="toggleArrayFilter('owner', $event)"
      @reset="resetFilters"
    />

    <div v-if="loading" class="catalog-card__skeleton" data-state="loading">
      <div v-for="index in 4" :key="index" class="catalog-card__skeleton-row"></div>
    </div>

    <div v-else-if="error" class="catalog-card__error" data-state="error">
      <p class="text-label">Skill catalog unavailable</p>
      <p class="text-body-sm">{{ error }}</p>
      <button class="btn-machined" type="button" @click="$emit('retry')">Retry</button>
    </div>

    <div v-else-if="!filteredSkills.length" class="catalog-card__empty" data-state="empty">
      <p class="text-label">{{ hasActiveFilters ? 'No matching skills' : 'No skills registered' }}</p>
      <p class="text-body-sm">
        {{ hasActiveFilters
          ? 'Try widening the filters or search term.'
          : 'Skills are registered via Platform Center -> Skill Registry.' }}
      </p>
      <button v-if="hasActiveFilters" class="btn-machined" type="button" @click="resetFilters">Reset filters</button>
    </div>

    <div v-else class="catalog-card__table-wrap" data-state="normal">
      <table class="catalog-card__table">
        <thead>
          <tr>
            <th scope="col">Skill</th>
            <th scope="col">Category</th>
            <th scope="col">Status</th>
            <th scope="col">Autonomy</th>
            <th scope="col">Owner</th>
            <th scope="col">Last Run</th>
            <th scope="col">30d Success</th>
          </tr>
        </thead>
        <tbody>
          <SkillRow
            v-for="skill in filteredSkills"
            :key="skill.id"
            :skill="skill"
            :selected="skill.key === selectedSkillKey"
            @select="$emit('select', $event)"
          />
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.catalog-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
}

.catalog-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.catalog-card__table-wrap {
  overflow-x: auto;
}

.catalog-card__table {
  width: 100%;
  border-collapse: collapse;
}

.catalog-card__table th {
  padding-bottom: 10px;
  text-align: left;
  color: var(--color-on-surface-variant);
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.catalog-card__error,
.catalog-card__empty {
  padding: 16px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.catalog-card__error {
  color: var(--color-incident-crimson);
}

.catalog-card__skeleton {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.catalog-card__skeleton-row {
  height: 48px;
  border-radius: var(--radius-sm);
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.03), rgba(137, 206, 255, 0.08), rgba(255, 255, 255, 0.03));
  background-size: 220% 100%;
  animation: catalog-pulse 1.2s ease-in-out infinite;
}

@keyframes catalog-pulse {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}
</style>
