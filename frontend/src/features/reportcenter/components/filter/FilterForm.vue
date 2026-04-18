<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import type { ReportDefinitionDto, ReportRunRequest } from '../../types';
import { validateRequest } from '../../utils';
import ScopeSelector from './ScopeSelector.vue';
import TimeRangeSelector from './TimeRangeSelector.vue';
import EntityMultiSelect from './EntityMultiSelect.vue';
import GroupingSelector from './GroupingSelector.vue';

const props = defineProps<{
  definition: ReportDefinitionDto;
  modelValue: ReportRunRequest;
  showMockTools?: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: ReportRunRequest];
  apply: [value: ReportRunRequest];
  reset: [];
}>();

const request = ref<ReportRunRequest>({
  ...props.modelValue,
  scopeIds: [...props.modelValue.scopeIds],
  timeRange: { ...props.modelValue.timeRange },
  extraFilters: props.modelValue.extraFilters ? { ...props.modelValue.extraFilters } : {},
});

watch(
  () => props.modelValue,
  value => {
    request.value = {
      ...value,
      scopeIds: [...value.scopeIds],
      timeRange: { ...value.timeRange },
      extraFilters: value.extraFilters ? { ...value.extraFilters } : {},
    };
  },
  { deep: true },
);

const validation = computed(() => validateRequest(props.definition, request.value));

function patch(next: Partial<ReportRunRequest>) {
  request.value = {
    ...request.value,
    ...next,
    scopeIds: next.scopeIds ? [...next.scopeIds] : request.value.scopeIds,
    timeRange: next.timeRange ? { ...next.timeRange } : request.value.timeRange,
    extraFilters: next.extraFilters ? { ...next.extraFilters } : request.value.extraFilters,
  };
  emit('update:modelValue', request.value);
}

function handleApply() {
  if (!validation.value.valid) {
    return;
  }
  emit('apply', request.value);
}

function updateMockSectionError(value: string) {
  const extraFilters = { ...(request.value.extraFilters ?? {}) };
  if (!value) {
    delete extraFilters.mockSectionError;
  } else {
    extraFilters.mockSectionError = value;
  }
  patch({ extraFilters });
}
</script>

<template>
  <form class="filter-form" @submit.prevent="handleApply">
    <div class="filter-form__grid">
      <ScopeSelector
        :model-value="request.scope"
        :supported-scopes="definition.supportedScopes"
        @update:model-value="patch({ scope: $event, scopeIds: [] })"
      />
      <GroupingSelector
        :model-value="request.grouping"
        :supported-groupings="definition.supportedGroupings"
        @update:model-value="patch({ grouping: $event })"
      />
      <TimeRangeSelector
        :model-value="request.timeRange"
        @update:model-value="patch({ timeRange: $event })"
      />
    </div>

    <EntityMultiSelect
      :scope="request.scope"
      :model-value="request.scopeIds"
      @update:model-value="patch({ scopeIds: $event })"
    />

    <label v-if="showMockTools" class="field">
      <span class="text-label">Mock failure injection</span>
      <select class="field__control" :value="String(request.extraFilters?.mockSectionError ?? '')" @change="updateMockSectionError(($event.target as HTMLSelectElement).value)">
        <option value="">None</option>
        <option value="headline">Headline</option>
        <option value="series">Series</option>
        <option value="drilldown">Drilldown</option>
      </select>
    </label>

    <div v-if="!validation.valid" class="filter-form__errors">
      <p v-for="error in validation.errors" :key="error">{{ error }}</p>
    </div>

    <div class="filter-form__actions">
      <button class="btn-machined" type="button" @click="$emit('reset')">Reset</button>
      <button class="btn-machined btn-ai" type="submit" :disabled="!validation.valid">Apply Filters</button>
    </div>
  </form>
</template>

<style scoped>
.filter-form {
  display: grid;
  gap: 16px;
  border: var(--border-ghost);
  border-radius: 18px;
  background:
    radial-gradient(circle at top right, color-mix(in srgb, var(--accent-history) 20%, transparent), transparent 38%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-report) 92%, transparent), var(--color-surface-container-low));
  padding: 18px;
}

.filter-form__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.field {
  display: grid;
  gap: 8px;
}

.field__control {
  border: var(--border-ghost);
  border-radius: 10px;
  background: var(--color-surface-container-high);
  color: var(--color-on-surface);
  min-height: 40px;
  padding: 0 12px;
}

.filter-form__errors {
  display: grid;
  gap: 6px;
  padding: 12px 14px;
  border-radius: 12px;
  background: color-mix(in srgb, var(--color-error) 16%, transparent);
  color: var(--color-error);
  font-size: 0.875rem;
}

.filter-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 960px) {
  .filter-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
