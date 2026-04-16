<script setup lang="ts">
import { ref, computed } from 'vue';

interface Props {
  rows: ReadonlyArray<Record<string, string>>;
  columnMapping: Record<string, string>;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  normalizeSelected: [selectedIndices: number[]];
  updateMapping: [column: string, target: string];
}>();

const selectedIndices = ref<Set<number>>(new Set());

const sourceColumns = computed(() => {
  if (props.rows.length === 0) return [];
  return Object.keys(props.rows[0]);
});

const TARGET_FIELDS = ['title', 'priority', 'category', 'summary', 'businessJustification', '(skip)'];

function toggleRow(idx: number) {
  const next = new Set(selectedIndices.value);
  if (next.has(idx)) {
    next.delete(idx);
  } else {
    next.add(idx);
  }
  selectedIndices.value = next;
}

function toggleAll() {
  if (selectedIndices.value.size === props.rows.length) {
    selectedIndices.value = new Set();
  } else {
    selectedIndices.value = new Set(props.rows.map((_, i) => i));
  }
}

function handleNormalize() {
  emit('normalizeSelected', [...selectedIndices.value]);
}
</script>

<template>
  <div class="batch-preview">
    <div class="batch-header">
      <span class="batch-count">{{ rows.length }} rows detected</span>
      <span class="batch-selected">{{ selectedIndices.size }} selected</span>
    </div>

    <!-- Column Mapping -->
    <div class="mapping-section">
      <span class="mapping-title">Column Mapping</span>
      <div class="mapping-grid">
        <div v-for="col in sourceColumns" :key="col" class="mapping-row">
          <span class="mapping-source">{{ col }}</span>
          <span class="mapping-arrow">→</span>
          <select
            class="mapping-select"
            :value="columnMapping[col] ?? '(skip)'"
            @change="emit('updateMapping', col, ($event.target as HTMLSelectElement).value)"
          >
            <option v-for="t in TARGET_FIELDS" :key="t" :value="t">{{ t }}</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Preview Table -->
    <div class="table-wrapper">
      <table class="preview-table">
        <thead>
          <tr>
            <th class="col-check">
              <input type="checkbox" :checked="selectedIndices.size === rows.length" @change="toggleAll" />
            </th>
            <th v-for="col in sourceColumns" :key="col">{{ col }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(row, idx) in rows"
            :key="idx"
            :class="{ 'row--selected': selectedIndices.has(idx) }"
          >
            <td class="col-check">
              <input type="checkbox" :checked="selectedIndices.has(idx)" @change="toggleRow(idx)" />
            </td>
            <td v-for="col in sourceColumns" :key="col">{{ row[col] }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <button
      class="normalize-btn"
      :disabled="selectedIndices.size === 0"
      @click="handleNormalize"
    >
      Normalize Selected ({{ selectedIndices.size }})
    </button>
  </div>
</template>

<style scoped>
.batch-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.batch-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-count, .batch-selected {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
}

.batch-selected { color: var(--color-secondary); }

.mapping-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px;
  background: var(--color-surface-container-low);
  border-radius: var(--radius-sm);
}

.mapping-title {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}

.mapping-grid {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mapping-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mapping-source {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-on-surface);
  min-width: 120px;
}

.mapping-arrow {
  color: var(--color-on-surface-variant);
  opacity: 0.4;
}

.mapping-select {
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 3px 8px;
  font-family: var(--font-ui);
  font-size: 0.625rem;
  color: var(--color-on-surface);
}

.table-wrapper {
  overflow-x: auto;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
}

.preview-table {
  width: 100%;
  border-collapse: collapse;
  font-family: var(--font-ui);
  font-size: 0.6875rem;
}

.preview-table th, .preview-table td {
  padding: 6px 10px;
  text-align: left;
  border-bottom: 1px solid var(--border-separator);
  white-space: nowrap;
}

.preview-table th {
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
  background: var(--color-surface-container-low);
}

.preview-table td {
  color: var(--color-on-surface);
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.col-check { width: 32px; text-align: center; }

.row--selected { background: var(--color-secondary-tint); }

.normalize-btn {
  background: linear-gradient(135deg, var(--color-secondary), #a78bfa);
  border: none;
  color: #0b1326;
  padding: 8px 20px;
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  cursor: pointer;
  align-self: flex-end;
}

.normalize-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.normalize-btn:hover:not(:disabled) { opacity: 0.85; }
</style>
