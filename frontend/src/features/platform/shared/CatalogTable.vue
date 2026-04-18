<script setup lang="ts">
export interface ColumnDef {
  key: string;
  label: string;
  width?: string;
}

defineProps<{
  rows: Record<string, unknown>[];
  columns: ColumnDef[];
  state: 'loading' | 'error' | 'empty' | 'normal';
  errorMessage?: string;
  emptyMessage: string;
  emptyActionLabel?: string;
  selectedId?: string;
}>();

const emit = defineEmits<{
  rowClick: [row: Record<string, unknown>];
  retry: [];
  emptyAction: [];
}>();
</script>

<template>
  <div class="catalog-table">
    <table v-if="state === 'normal'" class="table">
      <thead>
        <tr>
          <th v-for="col in columns" :key="col.key" :style="col.width ? { width: col.width } : {}">
            {{ col.label }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="row in rows"
          :key="String(row.id)"
          class="row"
          :class="{ selected: selectedId === row.id }"
          @click="emit('rowClick', row)"
        >
          <td v-for="col in columns" :key="col.key">
            <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">
              {{ row[col.key] ?? '—' }}
            </slot>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-else-if="state === 'loading'" class="state-box">
      <div class="skeleton" v-for="i in 5" :key="i" />
    </div>

    <div v-else-if="state === 'error'" class="state-box error-state">
      <p>{{ errorMessage ?? 'Failed to load data' }}</p>
      <button class="btn-machined" @click="emit('retry')">Retry</button>
    </div>

    <div v-else-if="state === 'empty'" class="state-box empty-state">
      <p>{{ emptyMessage }}</p>
      <button v-if="emptyActionLabel" class="btn-machined" @click="emit('emptyAction')">
        {{ emptyActionLabel }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.catalog-table { width: 100%; }
.table { width: 100%; border-collapse: collapse; }
.table th {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant, #999);
  text-align: left;
  padding: 8px 12px;
  border-bottom: 1px solid var(--color-border, #2a2a2a);
}
.table td {
  font-size: 14px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border-subtle, #1a1a1a);
}
.row { cursor: pointer; transition: background-color 80ms ease-out; }
.row:hover { background: var(--color-surface-hover, rgba(255,255,255,0.04)); }
.row.selected { background: var(--color-surface-active, rgba(255,255,255,0.08)); }
.state-box { padding: 48px 24px; text-align: center; }
.skeleton {
  height: 40px;
  margin-bottom: 4px;
  border-radius: 4px;
  background: var(--color-surface-hover, rgba(255,255,255,0.04));
  animation: pulse 1.5s ease-in-out infinite;
}
@keyframes pulse { 0%,100% { opacity: 0.4; } 50% { opacity: 0.8; } }
</style>
