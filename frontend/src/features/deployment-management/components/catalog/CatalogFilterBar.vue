<script setup lang="ts">
import { ref, watch } from 'vue';
import type { CatalogFilters } from '../../types/catalog';
import type { EnvironmentKind, HealthLed } from '../../types/enums';

const props = defineProps<{ filters: CatalogFilters }>();
const emit = defineEmits<{ change: [filters: CatalogFilters] }>();

const search = ref(props.filters.search ?? '');
const envKind = ref<EnvironmentKind | ''>(props.filters.environmentKind ?? '');
const ledFilter = ref<HealthLed | ''>(props.filters.deployStatus ?? '');
const window = ref(props.filters.window ?? '7d');

let debounceTimer: ReturnType<typeof setTimeout> | undefined;

function emitChange() {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    emit('change', {
      search: search.value || undefined,
      environmentKind: envKind.value || undefined,
      deployStatus: ledFilter.value || undefined,
      window: window.value as '24h' | '7d' | '30d',
    });
  }, 250);
}

watch([search, envKind, ledFilter, window], emitChange);
</script>

<template>
  <div class="filter-bar">
    <input v-model="search" class="search-input" type="text" placeholder="Search applications..." />
    <select v-model="envKind" class="filter-select">
      <option value="">All Environments</option>
      <option value="DEV">DEV</option>
      <option value="TEST">QA / Test</option>
      <option value="STAGING">Staging</option>
      <option value="PROD">Prod</option>
    </select>
    <select v-model="ledFilter" class="filter-select">
      <option value="">All Health</option>
      <option value="GREEN">Green</option>
      <option value="AMBER">Amber</option>
      <option value="RED">Red</option>
      <option value="UNKNOWN">Unknown</option>
    </select>
    <select v-model="window" class="filter-select">
      <option value="24h">24h</option>
      <option value="7d">7 days</option>
      <option value="30d">30 days</option>
    </select>
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  gap: 8px;
  padding: 8px 0;
  flex-wrap: wrap;
}
.search-input, .filter-select {
  padding: 6px 10px;
  border-radius: var(--radius-sm);
  border: var(--border-ghost);
  background: var(--color-surface-container-high);
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.8rem;
}
.search-input { flex: 1; min-width: 200px; }
.search-input::placeholder { color: var(--color-on-surface-variant); }
.filter-select { min-width: 120px; }
</style>
